/**
 * 主角概念图 → 透明 PNG 部件切片 + 骨骼 JSON
 * 用法: node slice-hero.js
 */
'use strict';

const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

const SRC = path.resolve(__dirname, '../../src/main/resources/文心一言/主角.jpeg');
const OUT_DIR = path.resolve(__dirname, '../../src/main/resources/static/art/char/hero_cyber');
const PARTS_DIR = path.join(OUT_DIR, 'parts');

/** 仅左侧全身立绘，避开中间头模/右侧特写 */
const SHEET_ROI = { left: 120, top: 80, width: 980, height: 3040 };

/**
 * 相对全身 bbox 的部件矩形 [x0,y0,x1,y1]，0~1
 * 注意：立绘为 3/4 侧，角色右手+武器在画面左侧
 */
const PART_BOXES = {
  cape:           [0.28, 0.10, 0.78, 0.48],
  hair:           [0.30, 0.00, 0.78, 0.18],
  head:           [0.32, 0.02, 0.74, 0.20],
  ear_l:          [0.62, 0.07, 0.74, 0.16],
  ear_r:          [0.30, 0.07, 0.42, 0.16],
  face:           [0.38, 0.07, 0.66, 0.18],
  brow:           [0.40, 0.075, 0.64, 0.115],
  eye:            [0.42, 0.09, 0.62, 0.145],
  mouth:          [0.44, 0.135, 0.60, 0.175],
  necklace:       [0.34, 0.16, 0.70, 0.28],
  torso:          [0.26, 0.15, 0.76, 0.50],
  belt:           [0.32, 0.45, 0.70, 0.54],
  // 角色左臂 ≈ 画面右侧
  shoulder_l:     [0.58, 0.18, 0.86, 0.34],
  upper_arm_l:    [0.60, 0.28, 0.92, 0.46],
  forearm_l:      [0.62, 0.42, 0.96, 0.58],
  hand_l:         [0.64, 0.54, 0.96, 0.68],
  shield:         [0.70, 0.40, 0.98, 0.62],
  // 角色右臂+武器 ≈ 画面左侧
  shoulder_r:     [0.10, 0.18, 0.40, 0.34],
  upper_arm_r:    [0.06, 0.28, 0.38, 0.46],
  forearm_r:      [0.02, 0.40, 0.36, 0.58],
  hand_r:         [0.02, 0.52, 0.34, 0.68],
  weapon:         [0.00, 0.30, 0.42, 0.68],
  ornament:       [0.40, 0.30, 0.62, 0.44],
  // 腿：画面左≈角色右腿前侧，画面右≈角色左腿
  leg_l:          [0.42, 0.48, 0.70, 0.82],
  leg_r:          [0.22, 0.48, 0.50, 0.82],
  foot_l:         [0.40, 0.78, 0.72, 0.99],
  foot_r:         [0.20, 0.78, 0.52, 0.99],
};

const PART_META = {
  cape:        { id: 'cape', name: '披风', bone: 'bone_cape', parent: 'torso', zIndex: 1, transformOrigin: '50% 8%', rotation: 0, scale: 1 },
  hair:        { id: 'hair', name: '头发', bone: 'bone_hair', parent: 'head', zIndex: 12, transformOrigin: '50% 85%', rotation: 0, scale: 1 },
  head:        { id: 'head', name: '头部', bone: 'bone_head', parent: 'torso', zIndex: 20, transformOrigin: '50% 90%', rotation: 0, scale: 1 },
  ear_l:       { id: 'ear_l', name: '左耳', bone: 'bone_ear_l', parent: 'head', zIndex: 21, transformOrigin: '80% 50%', rotation: 0, scale: 1 },
  ear_r:       { id: 'ear_r', name: '右耳', bone: 'bone_ear_r', parent: 'head', zIndex: 21, transformOrigin: '20% 50%', rotation: 0, scale: 1 },
  face:        { id: 'face', name: '脸部', bone: 'bone_face', parent: 'head', zIndex: 22, transformOrigin: '50% 50%', rotation: 0, scale: 1 },
  brow:        { id: 'brow', name: '眉毛', bone: 'bone_brow', parent: 'face', zIndex: 23, transformOrigin: '50% 100%', rotation: 0, scale: 1 },
  eye:         { id: 'eye', name: '眼睛', bone: 'bone_eye', parent: 'face', zIndex: 24, transformOrigin: '50% 50%', rotation: 0, scale: 1 },
  mouth:       { id: 'mouth', name: '嘴巴', bone: 'bone_mouth', parent: 'face', zIndex: 23, transformOrigin: '50% 0%', rotation: 0, scale: 1 },
  necklace:    { id: 'necklace', name: '项链', bone: 'bone_necklace', parent: 'torso', zIndex: 15, transformOrigin: '50% 0%', rotation: 0, scale: 1 },
  torso:       { id: 'torso', name: '躯干', bone: 'bone_torso', parent: null, zIndex: 10, transformOrigin: '50% 55%', rotation: 0, scale: 1 },
  belt:        { id: 'belt', name: '腰带', bone: 'bone_belt', parent: 'torso', zIndex: 11, transformOrigin: '50% 50%', rotation: 0, scale: 1 },
  shoulder_l:  { id: 'shoulder_l', name: '左肩', bone: 'bone_shoulder_l', parent: 'torso', zIndex: 14, transformOrigin: '70% 40%', rotation: 0, scale: 1 },
  shoulder_r:  { id: 'shoulder_r', name: '右肩', bone: 'bone_shoulder_r', parent: 'torso', zIndex: 16, transformOrigin: '30% 40%', rotation: 0, scale: 1 },
  upper_arm_l: { id: 'upper_arm_l', name: '左上臂', bone: 'bone_upper_arm_l', parent: 'shoulder_l', zIndex: 13, transformOrigin: '55% 12%', rotation: 0, scale: 1 },
  upper_arm_r: { id: 'upper_arm_r', name: '右上臂', bone: 'bone_upper_arm_r', parent: 'shoulder_r', zIndex: 17, transformOrigin: '45% 12%', rotation: 0, scale: 1 },
  forearm_l:   { id: 'forearm_l', name: '左前臂', bone: 'bone_forearm_l', parent: 'upper_arm_l', zIndex: 12, transformOrigin: '55% 10%', rotation: 0, scale: 1 },
  forearm_r:   { id: 'forearm_r', name: '右前臂', bone: 'bone_forearm_r', parent: 'upper_arm_r', zIndex: 18, transformOrigin: '45% 10%', rotation: 0, scale: 1 },
  hand_l:      { id: 'hand_l', name: '左手', bone: 'bone_hand_l', parent: 'forearm_l', zIndex: 19, transformOrigin: '55% 15%', rotation: 0, scale: 1 },
  hand_r:      { id: 'hand_r', name: '右手', bone: 'bone_hand_r', parent: 'forearm_r', zIndex: 25, transformOrigin: '45% 15%', rotation: 0, scale: 1 },
  weapon:      { id: 'weapon', name: '武器', bone: 'bone_weapon', parent: 'hand_r', zIndex: 26, transformOrigin: '30% 40%', rotation: 0, scale: 1 },
  shield:      { id: 'shield', name: '盾牌', bone: 'bone_shield', parent: 'hand_l', zIndex: 9, transformOrigin: '80% 40%', rotation: 0, scale: 1 },
  ornament:    { id: 'ornament', name: '饰品', bone: 'bone_ornament', parent: 'torso', zIndex: 14, transformOrigin: '50% 50%', rotation: 0, scale: 1 },
  leg_l:       { id: 'leg_l', name: '左腿', bone: 'bone_leg_l', parent: 'torso', zIndex: 6, transformOrigin: '50% 8%', rotation: 0, scale: 1 },
  leg_r:       { id: 'leg_r', name: '右腿', bone: 'bone_leg_r', parent: 'torso', zIndex: 7, transformOrigin: '50% 8%', rotation: 0, scale: 1 },
  foot_l:      { id: 'foot_l', name: '左脚', bone: 'bone_foot_l', parent: 'leg_l', zIndex: 5, transformOrigin: '50% 15%', rotation: 0, scale: 1 },
  foot_r:      { id: 'foot_r', name: '右脚', bone: 'bone_foot_r', parent: 'leg_r', zIndex: 8, transformOrigin: '50% 15%', rotation: 0, scale: 1 },
};

function colorDist(r, g, b, r0, g0, b0) {
  const dr = r - r0, dg = g - g0, db = b - b0;
  return Math.sqrt(dr * dr + dg * dg + db * db);
}

function looksLikePaper(r, g, b) {
  const avg = (r + g + b) / 3;
  const span = Math.max(r, g, b) - Math.min(r, g, b);
  // 灰白纸底：偏亮、低饱和，且略偏蓝灰
  return avg >= 155 && span <= 40 && b >= r - 8;
}

/** 四角洪水填充去背，返回 png buffer + 内容 bbox */
async function removeBgFlood(inputBuf) {
  const { data, info } = await sharp(inputBuf)
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });
  const { width, height, channels } = info;
  const N = width * height;
  const mark = new Uint8Array(N); // 1=bg
  const q = new Int32Array(N);
  let qh = 0, qt = 0;

  const seeds = [
    [0, 0], [width - 1, 0], [0, height - 1], [width - 1, height - 1],
    [Math.floor(width / 2), 0], [0, Math.floor(height / 2)],
    [20, 20], [width - 21, 20],
  ];

  function trySeed(x, y) {
    const i = y * width + x;
    if (mark[i]) return;
    const p = i * channels;
    if (!looksLikePaper(data[p], data[p + 1], data[p + 2])) return;
    mark[i] = 1;
    q[qt++] = i;
  }

  for (const [x, y] of seeds) trySeed(x, y);

  const THRESH = 42;
  while (qh < qt) {
    const i = q[qh++];
    const x = i % width;
    const y = (i - x) / width;
    const p = i * channels;
    const r0 = data[p], g0 = data[p + 1], b0 = data[p + 2];
    const neighbors = [[x - 1, y], [x + 1, y], [x, y - 1], [x, y + 1]];
    for (const [nx, ny] of neighbors) {
      if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
      const ni = ny * width + nx;
      if (mark[ni]) continue;
      const np = ni * channels;
      const r = data[np], g = data[np + 1], b = data[np + 2];
      if (!looksLikePaper(r, g, b) && colorDist(r, g, b, r0, g0, b0) > THRESH) continue;
      // 过暗像素不是纸底（角色本体）
      if ((r + g + b) / 3 < 140 && !looksLikePaper(r, g, b)) continue;
      if (colorDist(r, g, b, r0, g0, b0) <= THRESH || looksLikePaper(r, g, b)) {
        mark[ni] = 1;
        q[qt++] = ni;
      }
    }
  }

  // BFS：只保留最大前景连通域
  const visited = new Uint8Array(N);
  const q2 = new Int32Array(N);
  let bestMask = null;
  let bestSize = 0;
  for (let start = 0; start < N; start++) {
    if (mark[start] || visited[start]) continue;
    let qh2 = 0, qt2 = 0;
    q2[qt2++] = start;
    visited[start] = 1;
    const comp = [];
    while (qh2 < qt2) {
      const i = q2[qh2++];
      comp.push(i);
      const x = i % width;
      const y = (i - x) / width;
      const neigh = [i - 1, i + 1, i - width, i + width];
      const ok = [
        x > 0, x + 1 < width, y > 0, y + 1 < height,
      ];
      for (let k = 0; k < 4; k++) {
        if (!ok[k]) continue;
        const ni = neigh[k];
        if (mark[ni] || visited[ni]) continue;
        visited[ni] = 1;
        q2[qt2++] = ni;
      }
    }
    if (comp.length > bestSize) {
      bestSize = comp.length;
      bestMask = comp;
    }
  }
  if (!bestMask || bestSize < 1000) throw new Error('去背后有效像素过少，请调整阈值');

  const keep = new Uint8Array(N);
  for (const i of bestMask) keep[i] = 1;

  let minX = width, minY = height, maxX = 0, maxY = 0, count = 0;
  for (let i = 0; i < N; i++) {
    const p = i * channels;
    if (!keep[i]) {
      data[p + 3] = 0;
      continue;
    }
    count++;
    const x = i % width;
    const y = (i - x) / width;
    if (x < minX) minX = x;
    if (y < minY) minY = y;
    if (x > maxX) maxX = x;
    if (y > maxY) maxY = y;
  }

  const pad = 2;
  minX = Math.max(0, minX - pad);
  minY = Math.max(0, minY - pad);
  maxX = Math.min(width - 1, maxX + pad);
  maxY = Math.min(height - 1, maxY + pad);

  const png = await sharp(data, { raw: { width, height, channels: 4 } }).png().toBuffer();
  return {
    png,
    bbox: { left: minX, top: minY, width: maxX - minX + 1, height: maxY - minY + 1 },
    fgCount: count,
  };
}

async function trimPart(pngBuf) {
  const { data, info } = await sharp(pngBuf).ensureAlpha().raw().toBuffer({ resolveWithObject: true });
  const { width, height, channels } = info;
  let minX = width, minY = height, maxX = 0, maxY = 0, has = false;
  for (let y = 0; y < height; y++) {
    for (let x = 0; x < width; x++) {
      const a = data[(y * width + x) * channels + 3];
      if (a > 10) {
        has = true;
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
      }
    }
  }
  if (!has) {
    const empty = await sharp({
      create: { width: 2, height: 2, channels: 4, background: { r: 0, g: 0, b: 0, alpha: 0 } },
    }).png().toBuffer();
    return { png: empty, width: 2, height: 2, trimX: 0, trimY: 0, empty: true };
  }
  const tw = maxX - minX + 1;
  const th = maxY - minY + 1;
  const png = await sharp(pngBuf).extract({ left: minX, top: minY, width: tw, height: th }).png().toBuffer();
  return { png, width: tw, height: th, trimX: minX, trimY: minY, empty: false };
}

async function main() {
  fs.mkdirSync(PARTS_DIR, { recursive: true });
  // 清理旧切片
  for (const f of fs.readdirSync(PARTS_DIR)) {
    if (f.endsWith('.png')) fs.unlinkSync(path.join(PARTS_DIR, f));
  }

  console.log('source:', SRC);
  const roiBuf = await sharp(SRC).extract(SHEET_ROI).png().toBuffer();
  const { png: bodyPng, bbox, fgCount } = await removeBgFlood(roiBuf);
  console.log('fg pixels:', fgCount, 'bbox:', bbox);

  const bodyCrop = await sharp(bodyPng).extract(bbox).png().toBuffer();
  // 内部组装校验用（不作为交付大图）
  fs.writeFileSync(path.join(OUT_DIR, '_debug_body.png'), bodyCrop);

  const bodyW = bbox.width;
  const bodyH = bbox.height;
  const rootX = Math.round(bodyW * 0.5);
  const rootY = Math.round(bodyH * 0.52);

  const parts = [];
  for (const key of Object.keys(PART_BOXES)) {
    const [x0, y0, x1, y1] = PART_BOXES[key];
    const left = Math.max(0, Math.floor(x0 * bodyW));
    const top = Math.max(0, Math.floor(y0 * bodyH));
    const right = Math.min(bodyW, Math.ceil(x1 * bodyW));
    const bottom = Math.min(bodyH, Math.ceil(y1 * bodyH));
    const w = Math.max(1, right - left);
    const h = Math.max(1, bottom - top);

    const cropBuf = await sharp(bodyCrop).extract({ left, top, width: w, height: h }).png().toBuffer();
    const trimmed = await trimPart(cropBuf);
    const fileName = `${key}.png`;
    fs.writeFileSync(path.join(PARTS_DIR, fileName), trimmed.png);

    const absX = left + trimmed.trimX;
    const absY = top + trimmed.trimY;
    const meta = PART_META[key];
    parts.push({
      id: meta.id,
      name: meta.name,
      fileName,
      width: trimmed.width,
      height: trimmed.height,
      offsetX: absX - rootX,
      offsetY: absY - rootY,
      zIndex: meta.zIndex,
      transformOrigin: meta.transformOrigin,
      rotation: meta.rotation,
      scale: meta.scale,
      parent: meta.parent,
      bone: meta.bone,
    });
    console.log(
      'part', key.padEnd(12),
      `${trimmed.width}x${trimmed.height}`.padStart(11),
      'off', String(absX - rootX).padStart(5), String(absY - rootY).padStart(5),
      trimmed.empty ? 'EMPTY' : ''
    );
  }

  const config = {
    character: {
      name: '赛博哨兵',
      version: '1.0',
      baseScale: 1,
      baseOffsetX: 0,
      baseOffsetY: 0,
    },
    parts,
  };

  fs.writeFileSync(path.join(OUT_DIR, 'character.json'), JSON.stringify(config, null, 2), 'utf8');
  console.log('done ->', OUT_DIR);
}

main().catch((e) => {
  console.error(e);
  process.exit(1);
});
