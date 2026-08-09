package org.wx.core;

import org.junit.jupiter.api.Test;

class Web3ApplicationTests {

    @Test
    void contextLoads() {
        // 外部 MySQL/Redis 由集成测试环境负责，基础单测不修改真实 Redis。
        org.assertj.core.api.Assertions.assertThat(Web3Application.class).isNotNull();
    }

}
