package org.wx.core.wxBase.unit;

import org.wx.core.wxBase.annotation.BizIdPrefix;

/**
 * 业务主键生成：表前缀 + _ + 8位随机数字
 */
public final class BizIdUtil {

    private BizIdUtil() {
    }

    public static String next(String prefix) {
        return prefix + "_" + WordUnit.randomKey(8, 1);
    }

    public static String next(Class<?> entityClass) {
        BizIdPrefix ann = entityClass.getAnnotation(BizIdPrefix.class);
        if (ann == null || ann.value() == null || ann.value().isEmpty()) {
            throw new IllegalArgumentException(entityClass.getSimpleName() + " 缺少 @BizIdPrefix");
        }
        return next(ann.value());
    }

    public static String tryNext(Class<?> entityClass) {
        BizIdPrefix ann = entityClass.getAnnotation(BizIdPrefix.class);
        if (ann == null || ann.value() == null || ann.value().isEmpty()) {
            return null;
        }
        return next(ann.value());
    }
}
