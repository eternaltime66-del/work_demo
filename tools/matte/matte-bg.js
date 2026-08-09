/**
 * 边缘 flood-fill 抠底 → 真 alpha。
 * 自动识别浅底（棋盘/白灰）或深底（近黑），不伤霓虹发光主体。
 * Usage: node matte-bg.js <in.png> <out.png>
 */
const path = require('path');
const sharp = require('sharp');

function luma(r, g, b) {
  return 0.2126 * r + 0.7152 * g + 0.0722 * b;
}

function chroma(r, g, b) {
  return Math.max(r, g, b) - Math.min(r, g, b);
}

async function matte(input, output) {
  const { data, info } = await sharp(input)
    .ensureAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });

  const w = info.width;
  const h = info.height;
  const buf = Buffer.from(data);

  // 四角采样判断浅底 / 深底
  const corners = [
    [0, 0],
    [w - 1, 0],
    [0, h - 1],
    [w - 1, h - 1]
  ].map(([x, y]) => {
    const i = (y * w + x) * 4;
    return [buf[i], buf[i + 1], buf[i + 2]];
  });
  const avgL = corners.reduce((s, c) => s + luma(c[0], c[1], c[2]), 0) / corners.length;
  const lightBg = avgL >= 90;

  console.log(`size=${w}x${h} mode=${lightBg ? 'light' : 'dark'} cornerL=${avgL.toFixed(1)}`);

  const isBg = (r, g, b) => {
    const L = luma(r, g, b);
    const C = chroma(r, g, b);
    if (lightBg) {
      // 浅灰/白棋盘：高亮 + 低饱和（避开青/品红霓虹）
      return L >= 175 && C <= 28;
    }
    // 近黑底：很暗 + 低饱和（避开暗金属上的霓虹描边）
    return L <= 28 && C <= 22;
  };

  const visited = new Uint8Array(w * h);
  const q = [];
  const tryPush = (x, y) => {
    if (x < 0 || y < 0 || x >= w || y >= h) return;
    const id = y * w + x;
    if (visited[id]) return;
    const i = id * 4;
    if (buf[i + 3] === 0) {
      visited[id] = 1;
      q.push(id);
      return;
    }
    if (!isBg(buf[i], buf[i + 1], buf[i + 2])) return;
    visited[id] = 1;
    buf[i + 3] = 0;
    q.push(id);
  };

  for (let x = 0; x < w; x++) {
    tryPush(x, 0);
    tryPush(x, h - 1);
  }
  for (let y = 0; y < h; y++) {
    tryPush(0, y);
    tryPush(w - 1, y);
  }

  while (q.length) {
    const id = q.pop();
    const x = id % w;
    const y = (id / w) | 0;
    tryPush(x + 1, y);
    tryPush(x - 1, y);
    tryPush(x, y + 1);
    tryPush(x, y - 1);
  }

  // 边缘再收一圈：邻接透明的浅/深低饱和像素
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
      const r = copy[i];
      const g = copy[i + 1];
      const b = copy[i + 2];
      const L = luma(r, g, b);
      const C = chroma(r, g, b);
      if (lightBg) {
        if (L >= 150 && C <= 36) buf[i + 3] = 0;
      } else if (L <= 40 && C <= 28) {
        buf[i + 3] = 0;
      }
    }
  }

  // 右下角灰白水印
  const wx0 = Math.floor(w * 0.72);
  const wy0 = Math.floor(h * 0.88);
  for (let y = wy0; y < h; y++) {
    for (let x = wx0; x < w; x++) {
      const i = (y * w + x) * 4;
      if (buf[i + 3] === 0) continue;
      const L = luma(buf[i], buf[i + 1], buf[i + 2]);
      const C = chroma(buf[i], buf[i + 1], buf[i + 2]);
      if (C < 40 && L > 140) buf[i + 3] = 0;
    }
  }

  let minX = w;
  let minY = h;
  let maxX = 0;
  let maxY = 0;
  let opaque = 0;
  for (let y = 0; y < h; y++) {
    for (let x = 0; x < w; x++) {
      if (buf[(y * w + x) * 4 + 3] > 8) {
        opaque++;
        if (x < minX) minX = x;
        if (y < minY) minY = y;
        if (x > maxX) maxX = x;
        if (y > maxY) maxY = y;
      }
    }
  }
  if (opaque < 100) {
    throw new Error('matte failed: almost empty (' + opaque + ')');
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
  console.error('Usage: node matte-bg.js <in.png> <out.png>');
  process.exit(1);
}
matte(path.resolve(input), path.resolve(output)).catch((e) => {
  console.error(e);
  process.exit(1);
});
