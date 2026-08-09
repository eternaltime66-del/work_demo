package org.wx.core.wxBusiness.game.service;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.wx.core.wxBase.exception.WxApiException;
import org.wx.core.wxBusiness.game.entity.ActiveSkill;
import org.wx.core.wxBusiness.game.entity.enums.ActiveSkillType;
import org.wx.core.wxBusiness.game.mapper.ActiveSkillMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class GlobalNormalSkillTests {

    @Test
    void globalNormalCannotBeDeleted() {
        ActiveSkillMapper mapper = mock(ActiveSkillMapper.class);
        ActiveSkillService service = new ActiveSkillService();
        ReflectionTestUtils.setField(service, "baseMapper", mapper);
        ActiveSkill global = normal("global", ActiveSkillService.DEFAULT_NORMAL_CODE);
        when(mapper.selectById("global")).thenReturn(global);

        assertThatThrownBy(() -> service.removeWithChildren("global"))
                .isInstanceOf(WxApiException.class)
                .hasMessageContaining("不可删除");
    }

    @Test
    void roleWithoutNormalReadsGlobalFallbackWithoutCreatingBinding() {
        ActiveSkillService activeSkillService = mock(ActiveSkillService.class);
        PlayerRoleSkillService service = new PlayerRoleSkillService();
        ReflectionTestUtils.setField(service, "activeSkillService", activeSkillService);
        ActiveSkill small = new ActiveSkill();
        small.setId("small");
        small.setSkillType(ActiveSkillType.SMALL);
        ActiveSkill global = normal("global", ActiveSkillService.DEFAULT_NORMAL_CODE);
        when(activeSkillService.ensureDefaultNormalSkill()).thenReturn(global);

        List<ActiveSkill> resolved = service.appendGlobalNormalFallback(List.of(small));

        assertThat(resolved).extracting(ActiveSkill::getId).containsExactly("global", "small");
        verify(activeSkillService).ensureDefaultNormalSkill();
    }

    @Test
    void configuredNormalWinsAndDoesNotReadGlobalFallback() {
        ActiveSkillService activeSkillService = mock(ActiveSkillService.class);
        PlayerRoleSkillService service = new PlayerRoleSkillService();
        ReflectionTestUtils.setField(service, "activeSkillService", activeSkillService);
        ActiveSkill custom = normal("custom", "CUSTOM_NORMAL");

        assertThat(service.appendGlobalNormalFallback(List.of(custom))).containsExactly(custom);
    }

    private static ActiveSkill normal(String id, String code) {
        ActiveSkill skill = new ActiveSkill();
        skill.setId(id);
        skill.setCode(code);
        skill.setSkillType(ActiveSkillType.NORMAL);
        skill.setEnable(true);
        return skill;
    }
}
