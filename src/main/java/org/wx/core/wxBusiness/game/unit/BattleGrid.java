package org.wx.core.wxBusiness.game.unit;

import org.wx.core.wxBase.factory.ErrorFactory;

/**
 * 战斗格子：横6 竖5；占地为 高*宽
 */
public final class BattleGrid {

    public static final int COLS = 6;
    public static final int ROWS = 5;

    private BattleGrid() {
    }

    public static void assertPlace(Integer posCol, Integer posRow, Integer gridH, Integer gridW, String label) {
        ErrorFactory.throwError(posCol == null || posRow == null, label + "位置不能为空");
        ErrorFactory.throwError(gridH == null || gridW == null || gridH < 1 || gridW < 1, label + "占地无效");
        ErrorFactory.throwError(posCol < 0 || posRow < 0, label + "位置越界");
        ErrorFactory.throwError(posCol + gridW > COLS || posRow + gridH > ROWS,
                label + "超出格子范围（横" + COLS + "竖" + ROWS + "）");
    }

    public static boolean overlap(int c1, int r1, int h1, int w1, int c2, int r2, int h2, int w2) {
        return c1 < c2 + w2 && c1 + w1 > c2 && r1 < r2 + h2 && r1 + h1 > r2;
    }

    public static boolean[][] emptyBoard() {
        return new boolean[ROWS][COLS];
    }

    public static void mark(boolean[][] board, int posCol, int posRow, int gridH, int gridW) {
        ErrorFactory.throwError(!tryMark(board, posCol, posRow, gridH, gridW), "格子重叠");
    }

    /** 不抛错：可放置则标记并返回 true */
    public static boolean tryMark(boolean[][] board, int posCol, int posRow, int gridH, int gridW) {
        if (posCol < 0 || posRow < 0 || gridH < 1 || gridW < 1) {
            return false;
        }
        if (posCol + gridW > COLS || posRow + gridH > ROWS) {
            return false;
        }
        for (int r = posRow; r < posRow + gridH; r++) {
            for (int c = posCol; c < posCol + gridW; c++) {
                if (board[r][c]) {
                    return false;
                }
            }
        }
        for (int r = posRow; r < posRow + gridH; r++) {
            for (int c = posCol; c < posCol + gridW; c++) {
                board[r][c] = true;
            }
        }
        return true;
    }

    public static int nvl(Integer v, int def) {
        return v == null ? def : v;
    }
}
