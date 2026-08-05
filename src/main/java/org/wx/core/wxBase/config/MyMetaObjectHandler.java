package org.wx.core.wxBase.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.wx.core.wxBase.unit.BizIdUtil;

import java.util.Date;

/**
 * MyBatis-Plus 字段自动填充处理器
 * 自动填充 id（前缀_8位数字）、createTime、updateTime
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        fillBizId(metaObject);
        this.strictInsertFill(metaObject, "createTime", Date.class, new Date());
        this.strictInsertFill(metaObject, "updateTime", Date.class, new Date());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", Date.class, new Date());
    }

    private void fillBizId(MetaObject metaObject) {
        if (!metaObject.hasGetter("id") || !metaObject.hasSetter("id")) {
            return;
        }
        Object id = metaObject.getValue("id");
        if (id != null && StringUtils.hasText(String.valueOf(id))) {
            return;
        }
        Object entity = metaObject.getOriginalObject();
        if (entity == null) {
            return;
        }
        String nextId = BizIdUtil.tryNext(entity.getClass());
        if (nextId != null) {
            metaObject.setValue("id", nextId);
        }
    }
}
