# 美术素材位（战斗外）

路径约定见 `scenario/美术方案md/战斗外改造v1.md` §6。

本目录按模块预留，图片未到位时前端 `js/art.js` 会降级到 emoji / CSS 占位。

已到位（首批）：
- `char/MST_*/idle_4.png` — 8 只怪物战斗 idle（单帧，按占地比例）
- `face/MST_*.png` + `monster/MST_*-face.png` — 战斗焦点 / 出击小关头像
- `item/{wood,wood_armor,wood_stick,slime_gel,wolf_fang,forest_core}.png` — 物品图标（DB `icon` 已写 `/art/item/...`）

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
