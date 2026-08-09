package org.wx.core.wxBase.aop;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.annotation.TableId;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.wx.core.wxBase.annotation.RedisLock;
import org.wx.core.wxBase.factory.ErrorFactory;
import org.wx.core.wxBase.factory.RedisFactory;

import jakarta.annotation.Resource;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

/**
 * RedisLockAop 拦截器（基于自定义 RedisFactory 实现）
 * 保证分布式锁获取与释放完整性，避免多线程并发导致的数据不一致。
 *
 * @author 无心
 */
@Aspect
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RedisLockAop {

    // 注入你自定义的 RedisFactory
    @Resource
    private RedisFactory redisFactory;

    // 锁重试间隔（100毫秒）
    private static final long LOCK_RETRY_INTERVAL = 100L;
    // 最大重试次数（30次 = 3秒）
    private static final int MAX_RETRY_COUNT = 30;

    @Pointcut(value = "@annotation(org.wx.core.wxBase.annotation.RedisLock)")
    public void Pointcut() {}

    /**
     * 构建分布式锁的唯一Key
     */
    public String getKey(JoinPoint joinPoint) {
        Signature signature = joinPoint.getSignature();
        MethodSignature methodSignature = ((MethodSignature) signature);
        Method method = methodSignature.getMethod();
        RedisLock annotation = method.getAnnotation(RedisLock.class);
        String baseKey = annotation.key();
        List<String> keysToBuild;

        // 特殊逻辑：baseKey为"e"时，取参数对象的主键字段
        if ("e".equals(baseKey)) {
            Object[] args = joinPoint.getArgs();
            String[] parameterNames = methodSignature.getParameterNames();
            JSONObject paramJson = new JSONObject();
            for (int i = 0; i < args.length; i++) {
                paramJson.put(parameterNames[i], args[i]);
            }
            Object targetObject = paramJson.get(baseKey);
            if (targetObject != null) {
                String pkField = getPrimaryKeyFieldName(targetObject.getClass());
                if (pkField == null) {
                    ErrorFactory.redisLockError("Redis锁异常；未找到主键字段（@TableId）");
                }
                baseKey = baseKey + "." + pkField;
            } else {
                ErrorFactory.redisLockError("Redis锁异常；未找到参数对象：" + baseKey);
            }
        }

        // 拆分key规则，构建最终锁key
        keysToBuild = Arrays.asList(baseKey.split(","));
        Object[] args = joinPoint.getArgs();
        String[] parameterNames = methodSignature.getParameterNames();
        JSONObject paramJson = new JSONObject();
        for (int i = 0; i < args.length; i++) {
            Object val = args[i];
            String key = parameterNames[i];
            paramJson.put(key, val);
        }

        Map<String, String> lockMap = new LinkedHashMap<>();
        keysToBuild.forEach(item -> {
            List<String> levelKeys = Arrays.asList(item.split("\\."));
            String val = paramJson.toJSONString();
            String errorKey = "";
            for (int i = 0; i < levelKeys.size(); i++) {
                errorKey += (i == 0 ? "" : ".") + levelKeys.get(i);
                val = JSONObject.parseObject(val).getString(levelKeys.get(i));
                if (val == null) {
                    String msg = String.format("Redis锁异常;获取方法参数[%s]失败;值不能为空;", errorKey);
                    ErrorFactory.redisLockError(msg);
                }
            }
            lockMap.put(item, val);
        });

        // 拼接完整锁key（类名.方法名 + 业务参数）
        String className = joinPoint.getSignature().getDeclaringTypeName();
        String methodName = joinPoint.getSignature().getName();
        String fullMethod = className + "." + methodName;
        String lockKey = String.join("_", lockMap.values());

        // 是否绑定方法名到锁key
        if (annotation.bindMethod()) {
            lockKey = fullMethod + ":::" + lockKey;
        }

        return lockKey;
    }

    /**
     * 获取实体类的主键字段名（支持父类）
     */
    public String getPrimaryKeyFieldName(Class<?> clazz) {
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(TableId.class)) {
                return field.getName();
            }
        }
        Class<?> superClass = clazz.getSuperclass();
        if (superClass != null && !superClass.equals(Object.class)) {
            return getPrimaryKeyFieldName(superClass);
        }
        return null;
    }

    @Around("Pointcut()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
        String lockKey = getKey(pjp);
        Method method = ((MethodSignature) pjp.getSignature()).getMethod();
        RedisLock lock = method.getAnnotation(RedisLock.class);
        String ownerToken = UUID.randomUUID().toString();
        long leaseSeconds = Math.max(1L, lock.leaseSeconds());
        boolean acquired = redisFactory.tryLock(lockKey, ownerToken, leaseSeconds);
        if (!acquired && lock.loading()) {
            int retryCount = 0;
            while (!acquired && retryCount++ < MAX_RETRY_COUNT) {
                try {
                    Thread.sleep(LOCK_RETRY_INTERVAL);
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                    ErrorFactory.redisLockError("等待业务锁时线程被中断");
                }
                acquired = redisFactory.tryLock(lockKey, ownerToken, leaseSeconds);
            }
        }
        if (!acquired) {
            ErrorFactory.redisLockError();
        }
        try {
            return pjp.proceed();
        } finally {
            try {
                redisFactory.unlock(lockKey, ownerToken);
            } catch (Exception ignored) {
                // 锁有 TTL；释放异常不能覆盖原业务返回/异常。
            }
        }
    }
}
