/**
 * 把棋盘格伪透明底抠成真 alpha，并清掉右下角水印。
 * Usage: node matte-checker.js <in.png> <out.png>
 */
const fs = require('fs');
const path = require('path');
const sharp = require('sharp');

function colorDist(a, b) {
  return Math.abs(a[0] - b[0]) + Math.abs(a[1] - b[1]) + Math.abs(a[2] - b[2]);
}

function near(a, b, tol) {
  return colorDist(a, b) <= tol;
}

function detectTile(buf, w, h, c0) {
  const get = (x, y) => {
    const i = (y * w + x) * 4;
    return [buf[i], buf[i + 1], buf[i + 2]];
  };
  for (const size of [8, 10, 12, 16, 20, 24, 32, 40, 48, 64]) {
    if (size >= w || size >= h) continue;
    const c1 = get(size, 0);
    if (near(c0, c1, 18)) continue;
    let ok = true;
    for (let t = 0; t < 4 && ok; t++) {
      const x = t * size;
      if (x >= w) break;
      const expect = (t % 2 === 0) ? c0 : c1;
      if (!near(get(x, 0), expect, 28)) ok = false;
    }
    if (ok) return { size, c1 };
  }
  return { size: 16, c1: get(Math.min(16, w - 1), 0) };
}

async function matte(input, output) {
  const { data, info } = await sharp(input)
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });

  const w = info.width;
  const h = info.height;
  const buf = Buffer.from(data);
  const get = (x, y) => {
    const i = (y * w + x) * 4;
    return [buf[i], buf[i + 1], buf[i + 2], buf[i + 3]];
  };

  // 四角采样棋盘色
  const corners = [
    get(0, 0), get(w - 1, 0), get(0, h - 1), get(w - 1, h - 1)
  ];
  const c0 = corners[0].slice(0, 3);
  const { size: tile, c1 } = detectTile(buf, w, h, c0);
  const tol = 36;

  console.log(`size=${w}x${h} tile=${tile} c0=${c0.join(',')} c1=${c1.join(',')}`);

  // 1) 按棋盘期望色抠底
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const i = (y * w + x) * 4;
      const px = [buf[i], buf[i + 1], buf[i + 2]];
      const cx = Math.floor(x / tile);
      const cy = Math.floor(y / tile);
      const expect = ((cx + cy) % 2 === 0) ? c0 : c1;
      if (near(px, expect, tol)) {
        buf[i + 3] = 0;
      }
    }
  }

  // 2) 从边缘 flood-fill：仍接近棋盘色的连通区清掉（修边缘缝）
  const visited = new Uint8Array(w * h);
  const q = [];
  const push = (x, y) => {
    if (x < 0 || y < 0 || x >= w || y >= h) return;
    const id = y * w + x;
    if (visited[id]) return;
    const i = id * 4;
    if (buf[i + 3] === 0) {
      visited[id] = 1;
      q.push(id);
      return;
    }
    const px = [buf[i], buf[i + 1], buf[i + 2]];
    if (near(px, c0, tol + 12) || near(px, c1, tol + 12)) {
      visited[id] = 1;
      buf[i + 3] = 0;
      q.push(id);
    }
  };
  for (let x = 0; x < w; x++) {
    push(x, 0); push(x, h - 1);
  }
  for (let y = 0; y < h; y++) {
    push(0, y); push(w - 1, y);
  }
  while (q.length) {
    const id = q.pop();
    const x = id % w;
    const y = (id / w) | 0;
    push(x + 1, y); push(x - 1, y); push(x, y + 1); push(x, y - 1);
  }

  // 3) 清右下角水印：半透明浅色碎点
  const wx0 = Math.floor(w * 0.72);
  const wy0 = Math.floor(h * 0.88);
  for (let y = wy0; y < h; y++) {
    for (let x = wx0; x < w; x++) {
      const i = (y * w + x) * 4;
      if (buf[i + 3] === 0) continue;
      const px = [buf[i], buf[i + 1], buf[i + 2]];
      const bright = (px[0] + px[1] + px[2]) / 3;
      const chroma = Math.max(px[0], px[1], px[2]) - Math.min(px[0], px[1], px[2]);
      // 灰白水印字 / 残棋盘
      if (chroma < 40 && bright > 140) {
        buf[i + 3] = 0;
      } else if (near(px, c0, tol + 20) || near(px, c1, tol + 20)) {
        buf[i + 3] = 0;
      }
    }
  }

  // 4) 收缩半透明边缘：邻接全透明的低饱和像素再抠一层
  const copy = Buffer.from(buf);
  for (let y = 1; y < h - 1; y++) {
    for (let x = 1; x < w - 1; x++) {
      const i = (y * w + x) * 4;
      if (copy[i + 3] === 0) continue;
      let border = false;
      for (let dy = -1; dy <= 1 && !border; dy++) {
        for (let dx = -1; dx <= 1; dx++) {
          if (dx === 0 && dy === 0) continue;
          if (copy[((y + dy) * w + (x + dx)) * 4 + 3] === 0) {
            border = true;
            break;
          }
        }
      }
      if (!border) continue;
      const px = [copy[i], copy[i + 1], copy[i + 2]];
      if (near(px, c0, tol + 24) || near(px, c1, tol + 24)) {
        buf[i + 3] = 0;
      }
    }
  }

  // 裁切到不透明包围盒，略留边
  let minX = w, minY = h, maxX = 0, maxY = 0;
  let opaque = 0;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      const a = buf[(y * w + x) * 4 + 3];
      if (a > 8) {
        opaque++;
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
      }
    }
  }
  const pad = 8;
  minX = Math.max(0, minX - pad);
  minY = Math.max(0, minY - pad);
  maxX = Math.min(w - 1, maxX + pad);
  maxY = Math.min(h - 1, maxY + pad);
  const cw = maxX - minX + 1;
  const ch = maxY - minY + 1;
  console.log(`opaque=${opaque} crop=${cw}x${ch}`);

  await sharp(buf, { raw: { width: w, height: h, channels: 4 } })
    .extract({ left: minX, top: minY, width: cw, height: ch })
    .png()
    .toFile(output);

  console.log('wrote', output);
}

const input = process.argv[2];
const output = process.argv[3];
if (!input || !output) {
  console.error('Usage: node matte-checker.js <in.png> <out.png>');
  process.exit(1);
}
matte(path.resolve(input), path.resolve(output)).catch((e) => {
  console.error(e);
  process.exit(1);
});
