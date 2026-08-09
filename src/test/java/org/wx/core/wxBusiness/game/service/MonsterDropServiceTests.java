package org.wx.core.wxBusiness.game.service;

import org.junit.jupiter.api.Test;
import org.wx.core.wxBase.exception.WxApiException;
import org.wx.core.wxBusiness.game.entity.MonsterDrop;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MonsterDropServiceTests {

    @Test
    void probabilityBoundariesAreDeterministic() {
        assertThat(MonsterDropService.rollRate(BigDecimal.ZERO)).isFalse();
        assertThat(MonsterDropService.rollRate(BigDecimal.valueOf(-1))).isFalse();
        assertThat(MonsterDropService.rollRate(BigDecimal.valueOf(100))).isTrue();
        assertThat(MonsterDropService.rollRate(BigDecimal.valueOf(101))).isTrue();
    }

    @Test
    void invalidQuantityRangeIsRejectedBeforeDatabaseWrite() {
        MonsterDrop row = validRow();
        row.setMinQty(5);
        row.setMaxQty(4);
        assertThatThrownBy(() -> new MonsterDropService().savePrepared(row))
                .isInstanceOf(WxApiException.class)
                .hasMessageContaining("最大掉落数量");
    }

    @Test
    void zeroQuantityIsRejectedBeforeDatabaseWrite() {
        MonsterDrop row = validRow();
        row.setMinQty(0);
        assertThatThrownBy(() -> new MonsterDropService().savePrepared(row))
                .isInstanceOf(WxApiException.class)
                .hasMessageContaining("最小掉落数量");
    }

    private static MonsterDrop validRow() {
        MonsterDrop row = new MonsterDrop();
        row.setMonsterId("monster");
        row.setItemId("item");
        row.setDropRate(BigDecimal.TEN);
        row.setMinQty(1);
        row.setMaxQty(1);
        return row;
    }
}
