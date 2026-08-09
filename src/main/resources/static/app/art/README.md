# 美术素材位（战斗外）

路径约定见 `scenario/美术方案md/战斗外改造v1.md` §6。

本目录按模块预留，图片未到位时前端 `js/art.js` 会降级到 emoji / CSS 占位。

已到位（首批）：
- `char/hero-idle.png` · `char/hero/idle_4.png` — 主角立绘
- `char/MST_*/idle_4.png` — 怪物战斗 idle（含霓虹第1~2章 `MST_NEON_*`）
- `char/{code}/attack.png` · `hit.png` — 攻击帧 / 受击帧（战斗回放切换；当前为 idle 占位，可替换专属图）
- `face/MST_*.png` + `monster/MST_*-face.png` — 战斗焦点 / 出击小关头像
- `item/{wood,...}.png` — 旧占位物品
- `item/neon_*.png` — 霓虹第1~2章材料与装备图标（DB `icon` = `/art/item/neon_*.png`）

### 战斗动作帧约定

| 文件 | 用途 | 前端触发 |
|------|------|----------|
| `idle_4.png` | 常态 | 默认 |
| `attack.png` | 攻击姿态 | **仅普攻 CAST**：冲刺到位后切换 |
| `hit.png` | 受击姿态 | 普攻受击（随普攻流程）；**充能技（小/大）HIT** |

流程：
1. 普攻：攻击方位移到首个目标附近 → 攻方 `attack` + 受方 `hit`
2. 小技能 / 大招造成伤害：仅受方切 `hit`（无位移）
3. 缺文件时降级 idle，仍靠 CSS 姿态区分

```
art/
  brand/logo.png
  home/stage-bg.jpg
  home/stage-mid.png
  home/shadow.png
  char/hero-idle.png
  equip/slot-*.svg
  item/{code}.png
  frame/{rarity}.png
  chapter/{id}.jpg
  monster/{code}-face.png
  tab/{name}.svg
  hud/{coin,energy,exp}.png
  ui/star-*.svg
  ui/lock.svg
  layout/cell-tile.png
  craft/anvil-bg.jpg
  empty/{bag,recipe,level}.png
  fx/craft-burst.png
```
