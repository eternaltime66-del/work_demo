package org.wx.core.wxBase.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.wx.core.wxBase.annotation.RedisLock;
import org.wx.core.wxBase.factory.RedisFactory;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RedisLockAopTests {

    @Test
    void unlockUsesTheSameOwnerTokenThatAcquiredTheLock() throws Throwable {
        RedisFactory redis = mock(RedisFactory.class);
        when(redis.tryLock(anyString(), anyString(), anyLong())).thenReturn(true);
        RedisLockAop aspect = new RedisLockAop();
        ReflectionTestUtils.setField(aspect, "redisFactory", redis);

        ProceedingJoinPoint pjp = joinPoint("player-1", "done");
        assertThat(aspect.doAround(pjp)).isEqualTo("done");

        ArgumentCaptor<String> acquireToken = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> releaseToken = ArgumentCaptor.forClass(String.class);
        verify(redis).tryLock(org.mockito.ArgumentMatchers.eq("player-1"), acquireToken.capture(), anyLong());
        verify(redis).unlock(org.mockito.ArgumentMatchers.eq("player-1"), releaseToken.capture());
        assertThat(releaseToken.getValue()).isEqualTo(acquireToken.getValue());
    }

    private static ProceedingJoinPoint joinPoint(String uid, Object result) throws Throwable {
        Method method = Fixture.class.getDeclaredMethod("execute", String.class);
        MethodSignature signature = mock(MethodSignature.class);
        when(signature.getMethod()).thenReturn(method);
        when(signature.getParameterNames()).thenReturn(new String[]{"uid"});
        when(signature.getDeclaringTypeName()).thenReturn(Fixture.class.getName());
        when(signature.getName()).thenReturn(method.getName());

        ProceedingJoinPoint pjp = mock(ProceedingJoinPoint.class);
        when(pjp.getSignature()).thenReturn(signature);
        when(pjp.getArgs()).thenReturn(new Object[]{uid});
        when(pjp.proceed()).thenReturn(result);
        return pjp;
    }

    private static class Fixture {
        @RedisLock(key = "uid", bindMethod = false)
        public Object execute(String uid) {
            return uid;
        }
    }
}
