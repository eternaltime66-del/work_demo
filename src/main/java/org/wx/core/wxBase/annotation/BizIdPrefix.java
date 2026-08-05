package org.wx.core.wxBase.annotation;

import java.lang.annotation.*;

/**
 * 业务表主键前缀：新增时自动生成 {prefix}_ + 8位随机数字
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface BizIdPrefix {
    String value();
}
