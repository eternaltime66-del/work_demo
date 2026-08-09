#!/usr/bin/env node
/**
 * 主线 30×10 内容种子生成器（1A 整库重刷 + 2A 池化推进）
 * 用法: node tools/gen_mainline_seed.js
 * 输出: src/main/resources/sql/seed_mainline_30x10.sql
 */
'use strict';

const fs = require('fs');
const path = require('path');

const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'sql', 'seed_mainline_30x10.sql');
const TYPE_ID = 'STY_10000001';
const DEFAULT_NORMAL = 'ASK_71867187';
const NOW = 'NOW()';

const ACTS = [
  {
    id: 1, name: '林缘启程',
    chapters: ['乡间小路', '新手村', '村外森林', '幽影林地', '古树祭坛'],
    mats: [
      { code: 'wood', name: '木材', icon: '/art/item/wood.png' },
      { code: 'slime_gel', name: '史莱姆凝胶', icon: '/art/item/slime_gel.png' },
      { code: 'wolf_fang', name: '狼牙', icon: '/art/item/wolf_fang.png' },
      { code: 'forest_core', name: '森林之心', icon: '/art/item/forest_core.png' },
    ],
    normals: ['史莱姆', '木精灵', '林地野猪', '毒蘑菇精'],
    rares: ['狼骑兵', '荆棘射手'],
    epic: '古树守卫',
    bosses: ['木桩巨像', '林狼首领', '树精长老', '蘑菇王', '森林领主'],
  },
  {
    id: 2, name: '荒野商道',
    chapters: ['黄沙驿站', '匪徒峡谷', '商队遗址', '蝎巢荒原', '盗王营地'],
    mats: [
      { code: 'sand_ore', name: '沙铁' },
      { code: 'bandit_cloth', name: '匪布' },
      { code: 'scorpion_sting', name: '蝎刺' },
      { code: 'merchant_seal', name: '商队徽记' },
    ],
    normals: ['沙丘匪徒', '荒野鬣狗', '毒蝎崽', '流沙精灵'],
    rares: ['弯刀骑手', '沙蝎战士'],
    epic: '沙暴傀儡',
    bosses: ['驿站守卫长', '峡谷匪首', '遗址怨灵', '蝎巢女王', '盗王'],
  },
  {
    id: 3, name: '矿山矿脉',
    chapters: ['矿工营地', '浅层矿道', '晶石洞窟', '熔岩裂隙', '地心祭坛'],
    mats: [
      { code: 'iron_ore', name: '铁矿' },
      { code: 'crystal_shard', name: '晶石碎片' },
      { code: 'lava_core', name: '熔岩核' },
      { code: 'earth_heart', name: '地心之心' },
    ],
    normals: ['矿蛛', '岩鼠', '晶石蠕虫', '煤灰小鬼'],
    rares: ['岩甲兵', '钻地蜈蚣'],
    epic: '熔岩傀儡',
    bosses: ['监工魔像', '矿道领主', '晶石巨兽', '裂隙炎魔', '地心看守'],
  },
  {
    id: 4, name: '古城遗迹',
    chapters: ['残垣外廊', '亡骨回廊', '符文大厅', '石像庭院', '王陵密室'],
    mats: [
      { code: 'bone_dust', name: '骨尘' },
      { code: 'rune_fragment', name: '符文碎片' },
      { code: 'ancient_bronze', name: '古铜' },
      { code: 'royal_crest', name: '王徽' },
    ],
    normals: ['骷髅兵', '游荡幽魂', '符文蝙蝠', '墓穴老鼠'],
    rares: ['持盾骷髅', '符文射手'],
    epic: '活化石像',
    bosses: ['外廊守陵者', '骨龙幼体', '符文法师', '石像统领', '王陵怨王'],
  },
  {
    id: 5, name: '王都暗影',
    chapters: ['暗巷夜市', '贵族花园', '地下斗场', '禁卫要塞', '王座前厅'],
    mats: [
      { code: 'shadow_silk', name: '影绸' },
      { code: 'mithril', name: '秘银' },
      { code: 'assassin_blade', name: '刺客残刃' },
      { code: 'royal_core', name: '王权核心' },
    ],
    normals: ['暗巷刺客', '魔犬', '腐化侍卫', '影蝠'],
    rares: ['双刀刺客', '禁卫骑士'],
    epic: '影魔犬王',
    bosses: ['夜市黑市主', '花园魅影', '斗场霸主', '禁卫统领', '摄政暗影'],
  },
  {
    id: 6, name: '魔渊终章',
    chapters: ['魔渊入口', '血池走廊', '咒缚祭坛', '深渊王座', '魔渊之心'],
    mats: [
      { code: 'abyss_shard', name: '深渊碎片' },
      { code: 'demon_horn', name: '魔角' },
      { code: 'void_crystal', name: '虚空结晶' },
      { code: 'abyss_heart', name: '魔渊之心' },
    ],
    normals: ['魔裔杂兵', '血池蠕虫', '咒缚小鬼', '虚空蛾'],
    rares: ['魔裔枪兵', '血池女妖'],
    epic: '咒缚巨像',
    bosses: ['入口看守', '血池领主', '祭坛大祭司', '深渊将军', '魔渊主宰'],
  },
];

function esc(s) {
  if (s == null) return 'NULL';
  return "'" + String(s).replace(/\\/g, '\\\\').replace(/'/g, "''") + "'";
}
function n(v) { return v == null ? 'NULL' : String(v); }
function sqlBool(v) { return v ? '1' : '0'; }

function dmgFormula(mul) {
  const tokens = [
    { kind: 'PARAM', paramMode: 'READ', readRole: 'SELF', readCategory: 'ATTR', readKey: 'ATK' },
    { kind: 'OP', op: '*' },
    { kind: 'PARAM', paramMode: 'LITERAL', value: String(mul) },
  ];
  return JSON.stringify(tokens);
}
function healFormula(mul) {
  const tokens = [
    { kind: 'PARAM', paramMode: 'READ', readRole: 'SELF', readCategory: 'ATTR', readKey: 'ATK' },
    { kind: 'OP', op: '*' },
    { kind: 'PARAM', paramMode: 'LITERAL', value: String(mul) },
  ];
  return JSON.stringify(tokens);
}

function raritySize(r) {
  return { NORMAL: [1, 1], RARE: [1, 2], EPIC: [2, 2], BOSS: [2, 4] }[r];
}

function rarityMul(r) {
  return { NORMAL: 1, RARE: 1.6, EPIC: 2.4, BOSS: 5 }[r];
}

function statsFor(chapter, rarity) {
  const m = rarityMul(rarity);
  const atk = Math.round((4 + chapter * 2) * m);
  const hp = Math.round((25 + chapter * 18) * m * (rarity === 'BOSS' ? 1.2 : 1));
  const def = Math.round((chapter * 0.6) * m);
  const action = rarity === 'BOSS' ? 90 : rarity === 'EPIC' ? 95 : rarity === 'RARE' ? 85 : 80;
  return { atk, hp, def, action };
}

/** 简易放置：从敌方后排往前填，校验不重叠 */
function placeUnits(specs) {
  // specs: [{id, rarity}]
  const COLS = 6, ROWS = 5;
  const board = Array.from({ length: ROWS }, () => Array(COLS).fill(false));
  const out = [];
  const tryMark = (c, r, h, w) => {
    if (c < 0 || r < 0 || c + w > COLS || r + h > ROWS) return false;
    for (let rr = r; rr < r + h; rr++) {
      for (let cc = c; cc < c + w; cc++) {
        if (board[rr][cc]) return false;
      }
    }
    for (let rr = r; rr < r + h; rr++) {
      for (let cc = c; cc < c + w; cc++) board[rr][cc] = true;
    }
    return true;
  };
  // 优先靠后排（row 大）居中
  const candidates = [];
  for (let r = ROWS - 1; r >= 0; r--) {
    for (let c = 0; c < COLS; c++) candidates.push([c, r]);
  }
  // 居中优先
  candidates.sort((a, b) => {
    const da = Math.abs(a[0] - 2.5) + (ROWS - 1 - a[1]) * 0.1;
    const db = Math.abs(b[0] - 2.5) + (ROWS - 1 - b[1]) * 0.1;
    return da - db;
  });

  for (const sp of specs) {
    const [h, w] = raritySize(sp.rarity);
    let placed = false;
    for (const [c, r] of candidates) {
      if (tryMark(c, r, h, w)) {
        out.push({ monsterId: sp.id, posCol: c, posRow: r, rarity: sp.rarity });
        placed = true;
        break;
      }
    }
    if (!placed) throw new Error('无法放置 ' + sp.id + ' ' + sp.rarity);
  }
  return out;
}

function main() {
  const lines = [];
  const push = (s) => lines.push(s);

  push('-- ============================================================');
  push('-- 主线 30×10 内容种子（由 tools/gen_mainline_seed.js 生成）');
  push('-- 1A 整库重刷配置 + 2A 池化推进；保留 DEFAULT_NORMAL / 主角模板 / 主线类型');
  push('-- ============================================================');
  push('SET NAMES utf8mb4;');
  push('SET FOREIGN_KEY_CHECKS=0;');
  push('START TRANSACTION;');
  push('');

  // ---- CLEAN ----
  push('-- ---- 运行时脏引用 ----');
  push('DELETE FROM app_warehouse_item;');
  push('DELETE FROM app_battle_bag;');
  push('UPDATE app_player_equip SET weapon_item_id=NULL, armor_item_id=NULL, gloves_item_id=NULL, helmet_item_id=NULL, legs_item_id=NULL, accessory1_item_id=NULL, accessory2_item_id=NULL, accessory3_item_id=NULL;');
  push(`DELETE FROM app_player_role_skill WHERE skill_id <> '${DEFAULT_NORMAL}';`);
  push('');
  push('-- ---- 关卡链（保留 TYPE 根节点，重刷 CHAPTER/LEVEL）----');
  push('DELETE FROM app_stage_level_monster;');
  push("DELETE FROM app_stage WHERE kind = 'LEVEL';");
  push("DELETE FROM app_stage WHERE kind = 'CHAPTER';");
  push('-- 确保主线 TYPE 节点存在');
  push(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,CREATE_TIME,UPDATE_TIME)`);
  push(`SELECT ${esc(TYPE_ID)},NULL,'TYPE','主线','MAIN',0,1,NULL,${NOW},${NOW}`);
  push(`FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM app_stage WHERE id = ${esc(TYPE_ID)});`);
  push('');
  push('-- ---- 掉落 / 配方 / 物品扩展 ----');
  push('DELETE FROM app_monster_drop;');
  push('DELETE FROM app_recipe_material;');
  push('DELETE FROM app_recipe;');
  push('DELETE FROM app_item_default_skill;');
  push('DELETE FROM app_item_default_passive;');
  push('DELETE FROM app_item_weapon;');
  push('DELETE FROM app_item_armor;');
  push('DELETE FROM app_item_helmet;');
  push('DELETE FROM app_item_gloves;');
  push('DELETE FROM app_item_legs;');
  push('DELETE FROM app_item_accessory;');
  push('DELETE FROM app_item_material;');
  push('DELETE FROM app_item;');
  push('DELETE FROM app_monster;');
  push('');
  push('-- ---- 技能（保留 DEFAULT_NORMAL）----');
  push('DELETE FROM app_passive_combat_effect;');
  push('DELETE FROM app_passive_effect;');
  push('DELETE FROM app_passive_condition;');
  push('DELETE FROM app_passive_skill;');
  push(`DELETE FROM app_skill_charge WHERE skill_id <> '${DEFAULT_NORMAL}';`);
  push(`DELETE FROM app_skill_effect WHERE skill_id <> '${DEFAULT_NORMAL}';`);
  push(`DELETE FROM app_active_skill WHERE id <> '${DEFAULT_NORMAL}';`);
  push('');

  // ---- Skills ----
  const skills = [
    { id: 'ASK_MAIN_SMASH', name: '重击', type: 'SMALL', charge: 5, mul: 1.5, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_SWEEP', name: '横扫', type: 'SMALL', charge: 7, mul: 1.2, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_PIERCE', name: '穿刺', type: 'SMALL', charge: 6, mul: 2.0, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_BASH', name: '猛砸', type: 'SMALL', charge: 8, mul: 1.8, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ARROW', name: '连射', type: 'SMALL', charge: 6, mul: 1.4, target: 'RANDOM_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_HOWL', name: '战吼', type: 'SMALL', charge: 9, mul: 1.3, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_HEAL', name: '愈合', type: 'SMALL', charge: 8, mul: 1.2, target: 'SELF', effect: 'HEAL' },
    { id: 'ASK_MAIN_POISON', name: '毒袭', type: 'SMALL', charge: 7, mul: 1.6, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_SHADOW', name: '影斩', type: 'SMALL', charge: 8, mul: 2.1, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_FLAME', name: '炎击', type: 'SMALL', charge: 9, mul: 1.9, target: 'FRONT_ROW', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_BONE', name: '碎骨', type: 'SMALL', charge: 7, mul: 1.7, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_CURSE', name: '咒缚', type: 'SMALL', charge: 10, mul: 1.5, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_NUKE', name: '毁灭冲击', type: 'ULTIMATE', charge: 20, mul: 3.0, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_JUDGE', name: '审判之光', type: 'ULTIMATE', charge: 22, mul: 3.5, target: 'FIRST', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_STORM', name: '沙暴吞没', type: 'ULTIMATE', charge: 18, mul: 2.8, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_QUAKE', name: '地裂', type: 'ULTIMATE', charge: 20, mul: 3.2, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_RUIN', name: '遗迹崩塌', type: 'ULTIMATE', charge: 24, mul: 3.4, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_SHADOW', name: '暗影笼罩', type: 'ULTIMATE', charge: 22, mul: 3.3, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_ULT_ABYSS', name: '魔渊终焉', type: 'ULTIMATE', charge: 28, mul: 4.0, target: 'ALL_ENEMY', effect: 'DAMAGE' },
    { id: 'ASK_MAIN_WPN_SLASH', name: '利刃斩', type: 'NORMAL', charge: 0, mul: 1.1, target: 'FIRST', effect: 'DAMAGE', mode: 'SELF_BASE_ACTION' },
    { id: 'ASK_MAIN_WPN_STAB', name: '迅刺', type: 'NORMAL', charge: 0, mul: 1.15, target: 'FIRST', effect: 'DAMAGE', mode: 'SELF_BASE_ACTION' },
    { id: 'ASK_MAIN_WPN_CRUSH', name: '钝击', type: 'NORMAL', charge: 0, mul: 1.2, target: 'FIRST', effect: 'DAMAGE', mode: 'SELF_BASE_ACTION' },
  ];

  // Expand per-act skill variants to approach ~40
  for (let a = 1; a <= 6; a++) {
    skills.push({
      id: `ASK_MAIN_A${a}_STRIKE`,
      name: `幕${a}强击`,
      type: 'SMALL',
      charge: 5 + a,
      mul: 1.4 + a * 0.08,
      target: 'FIRST',
      effect: 'DAMAGE',
    });
    skills.push({
      id: `ASK_MAIN_A${a}_BURST`,
      name: `幕${a}爆发`,
      type: 'ULTIMATE',
      charge: 16 + a * 2,
      mul: 2.6 + a * 0.2,
      target: 'ALL_ENEMY',
      effect: 'DAMAGE',
    });
  }

  push('-- ---- 主动技能 ----');
  let sefSeq = 1;
  let schSeq = 1;
  for (const sk of skills) {
    const mode = sk.mode || 'MANUAL';
    const need = mode === 'MANUAL' ? sk.charge : 0;
    push(`INSERT INTO app_active_skill (id,name,skill_type,code,need_charge_mode,need_charge,max_cast_skill,max_cast_global,max_cast_all_means,max_cast_role,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${esc(sk.id)},${esc(sk.name)},${esc(sk.type)},${esc(sk.id)},${esc(mode)},${need},0,0,0,0,${sefSeq},${sqlBool(true)},NULL,${NOW},${NOW});`);
    const fid = `SEF_MAIN_${String(sefSeq).padStart(3, '0')}`;
    const formula = sk.effect === 'HEAL' ? healFormula(sk.mul) : dmgFormula(sk.mul);
    push(`INSERT INTO app_skill_effect (id,skill_id,name,target_type,effect_type,attr_key,attr_dir,formula_json,hit_segments,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc(fid)},${esc(sk.id)},${esc(sk.name)},${esc(sk.target)},${esc(sk.effect)},NULL,NULL,${esc(formula)},1,0,${NOW},${NOW});`);
    if (sk.type === 'NORMAL') {
      const cid = `SCH_MAIN_${String(schSeq).padStart(3, '0')}`;
      push(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,every_action_value,charge_gain,skill_charge_event,skill_charge_match,match_skill_type,match_skill_id,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc(cid)},${esc(sk.id)},'行动值充能','ACTION_VALUE','GLOBAL',1,1,NULL,NULL,NULL,NULL,0,${NOW},${NOW});`);
      schSeq++;
    } else {
      // 普攻施放充能
      const cid = `SCH_MAIN_${String(schSeq).padStart(3, '0')}`;
      push(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,every_action_value,charge_gain,skill_charge_event,skill_charge_match,match_skill_type,match_skill_id,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc(cid)},${esc(sk.id)},'技能充能','SKILL_CHARGE','GLOBAL',NULL,${sk.type === 'ULTIMATE' ? 1 : 1},'CAST','ANY_TYPE','NORMAL',NULL,0,${NOW},${NOW});`);
      schSeq++;
      if (sk.type === 'ULTIMATE') {
        const cid2 = `SCH_MAIN_${String(schSeq).padStart(3, '0')}`;
        push(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,every_action_value,charge_gain,skill_charge_event,skill_charge_match,match_skill_type,match_skill_id,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc(cid2)},${esc(sk.id)},'小技能充能','SKILL_CHARGE','GLOBAL',NULL,5,'CAST','ANY_TYPE','SMALL',NULL,1,${NOW},${NOW});`);
        schSeq++;
      }
    }
    sefSeq++;
  }
  push('');

  // ---- Passives (~20 OUT_BASIC) ----
  const passives = [];
  const passiveDefs = [
    ['ATK', 3], ['ATK', 5], ['ATK', 8], ['ATK', 12], ['ATK', 18],
    ['MAX_HP', 20], ['MAX_HP', 40], ['MAX_HP', 80], ['MAX_HP', 120], ['MAX_HP', 200],
    ['DEF', 2], ['DEF', 4], ['DEF', 6], ['DEF', 10], ['DEF', 15],
    ['ATK', 6], ['MAX_HP', 60], ['DEF', 8], ['ATK', 15], ['MAX_HP', 150],
  ];
  push('-- ---- 被动 ----');
  for (let i = 0; i < passiveDefs.length; i++) {
    const [key, val] = passiveDefs[i];
    const id = `PSK_MAIN_${String(i + 1).padStart(2, '0')}`;
    const name = key === 'ATK' ? `攻击+${val}` : key === 'MAX_HP' ? `生命+${val}` : `防御+${val}`;
    passives.push({ id, key, val, name });
    push(`INSERT INTO app_passive_skill (id,name,code,passive_type,condition_mode,sort,enable,CREATE_TIME,UPDATE_TIME) VALUES (${esc(id)},${esc(name)},${esc(id)},'OUT_BASIC','UNLIMITED',${i},${sqlBool(true)},${NOW},${NOW});`);
    push(`INSERT INTO app_passive_effect (id,skill_id,attr_key,attr_dir,value_num,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('PSE_MAIN_' + String(i + 1).padStart(2, '0'))},${esc(id)},${esc(key)},'INCREASE',${val},0,${NOW},${NOW});`);
  }
  push('');

  // ---- Items / Monsters / Recipes per act ----
  const items = []; // {id, code, name, type, ...}
  const monsters = []; // {id, name, rarity, act, chapterBoss?}
  const monsterByKey = {};
  const recipes = [];

  const actSmall = (a) => `ASK_MAIN_A${a}_STRIKE`;
  const actUlt = (a) => `ASK_MAIN_A${a}_BURST`;
  const ultByAct = {
    1: 'ASK_MAIN_ULT_NUKE',
    2: 'ASK_MAIN_ULT_STORM',
    3: 'ASK_MAIN_ULT_QUAKE',
    4: 'ASK_MAIN_ULT_RUIN',
    5: 'ASK_MAIN_ULT_SHADOW',
    6: 'ASK_MAIN_ULT_ABYSS',
  };
  const smallPool = ['ASK_MAIN_SMASH', 'ASK_MAIN_SWEEP', 'ASK_MAIN_PIERCE', 'ASK_MAIN_BASH', 'ASK_MAIN_ARROW', 'ASK_MAIN_HOWL', 'ASK_MAIN_POISON', 'ASK_MAIN_SHADOW', 'ASK_MAIN_FLAME', 'ASK_MAIN_BONE', 'ASK_MAIN_CURSE'];

  push('-- ---- 物品 / 怪物 / 配方 ----');

  let matExtSeq = 1;
  let wpnSeq = 1;
  let armSeq = 1;
  let helSeq = 1;
  let gloSeq = 1;
  let legSeq = 1;
  let accSeq = 1;
  let dropSeq = 1;
  let rcpSeq = 1;
  let rcmSeq = 1;
  let idpSeq = 1;
  let idsSeq = 1;

  function insertItemBase(it) {
    const maxStack = it.type === 'MATERIAL' ? 99 : 1;
    const slots = it.slots || {};
    push(`INSERT INTO app_item (id,code,name,icon,item_type,max_stack,weight,sort,enable,charge_skill_slot_count,player_default_edit_charge_skill_slot_count,player_max_edit_charge_skill_slot_count,basic_passive_slot_count,player_default_edit_basic_passive_slot_count,player_max_edit_basic_passive_slot_count,advanced_passive_slot_count,player_default_edit_advanced_passive_slot_count,player_max_edit_advanced_passive_slot_count,anchor_passive_slot_count,player_default_edit_anchor_passive_slot_count,player_max_edit_anchor_passive_slot_count,periodic_passive_slot_count,player_default_edit_periodic_passive_slot_count,player_max_edit_periodic_passive_slot_count,CREATE_TIME,UPDATE_TIME) VALUES (${esc(it.id)},${esc(it.code)},${esc(it.name)},${it.icon ? esc(it.icon) : 'NULL'},${esc(it.type)},${maxStack},0,${it.sort || 0},1,${slots.charge || 0},0,0,${slots.basic || 0},0,0,0,0,0,0,0,0,0,0,0,${NOW},${NOW});`);
  }

  for (const act of ACTS) {
    // materials
    const matIds = [];
    act.mats.forEach((m, mi) => {
      const id = `ITM_MAIN_A${act.id}_M${mi + 1}`;
      const it = { id, code: m.code, name: m.name, type: 'MATERIAL', icon: m.icon || null, sort: act.id * 100 + mi };
      items.push(it);
      matIds.push(id);
      insertItemBase(it);
      push(`INSERT INTO app_item_material (id,item_id,grade,CREATE_TIME,UPDATE_TIME) VALUES (${esc('MAT_MAIN_' + String(matExtSeq).padStart(3, '0'))},${esc(id)},${act.id},${NOW},${NOW});`);
      matExtSeq++;
    });

    // gear: weapon, armor, and mid/late extras
    const tier = act.id;
    const wpnAtk = 5 + tier * 8;
    const armHp = 40 + tier * 35;
    const armDef = tier * 3;

    const gearSpecs = [
      { kind: 'WEAPON', code: `t${tier}_weapon`, name: `${act.name}之刃`, atk: wpnAtk, wpnSkill: tier <= 2 ? 'ASK_MAIN_WPN_SLASH' : tier <= 4 ? 'ASK_MAIN_WPN_STAB' : 'ASK_MAIN_WPN_CRUSH', charge: tier >= 2 ? 1 : 0, basic: tier >= 2 ? 1 : 0 },
      { kind: 'ARMOR', code: `t${tier}_armor`, name: `${act.name}护甲`, hp: armHp, def: armDef, basic: 1 },
    ];
    if (tier >= 2) {
      gearSpecs.push({ kind: 'HELMET', code: `t${tier}_helmet`, name: `${act.name}头盔`, hp: Math.round(armHp * 0.4), def: Math.max(1, armDef - 1) });
      gearSpecs.push({ kind: 'GLOVES', code: `t${tier}_gloves`, name: `${act.name}手套`, hp: Math.round(armHp * 0.25), def: Math.max(1, Math.floor(armDef / 2)) });
    }
    if (tier >= 3) {
      gearSpecs.push({ kind: 'LEGS', code: `t${tier}_legs`, name: `${act.name}护腿`, hp: Math.round(armHp * 0.35), def: Math.max(1, armDef - 1) });
    }
    if (tier >= 4) {
      gearSpecs.push({ kind: 'ACCESSORY', code: `t${tier}_acc`, name: `${act.name}饰品`, basic: 1 });
    }
    // second weapon/armor for mid tiers
    if (tier >= 3) {
      gearSpecs.push({ kind: 'WEAPON', code: `t${tier}_weapon2`, name: `${act.name}重器`, atk: wpnAtk + 6, wpnSkill: 'ASK_MAIN_WPN_CRUSH', charge: 2, basic: 1 });
      gearSpecs.push({ kind: 'ARMOR', code: `t${tier}_armor2`, name: `${act.name}重甲`, hp: armHp + 40, def: armDef + 3, basic: 1 });
    }

    const gearIds = { WEAPON: [], ARMOR: [], HELMET: [], GLOVES: [], LEGS: [], ACCESSORY: [] };

    // act1 icons for starter
    const iconMap = {
      t1_weapon: '/art/item/wood_stick.png',
      t1_armor: '/art/item/wood_armor.png',
    };

    for (const g of gearSpecs) {
      const id = `ITM_MAIN_${g.code.toUpperCase()}`;
      const it = {
        id,
        code: g.code,
        name: g.name,
        type: g.kind,
        icon: iconMap[g.code] || null,
        sort: act.id * 100 + 20 + gearIds[g.kind].length,
        slots: { charge: g.charge || 0, basic: g.basic || 0 },
      };
      items.push(it);
      gearIds[g.kind].push(id);
      insertItemBase(it);
      if (g.kind === 'WEAPON') {
        push(`INSERT INTO app_item_weapon (id,item_id,base_atk,atk_speed_up_ratio,atk_speed_down_ratio,normal_skill_id,CREATE_TIME,UPDATE_TIME) VALUES (${esc('WPN_MAIN_' + String(wpnSeq).padStart(3, '0'))},${esc(id)},${g.atk},0,0,${esc(g.wpnSkill)},${NOW},${NOW});`);
        wpnSeq++;
        if (g.charge) {
          const sk = g.charge >= 2 ? 'ASK_MAIN_PIERCE' : 'ASK_MAIN_SMASH';
          push(`INSERT INTO app_item_default_skill (id,item_id,skill_id,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('IDS_MAIN_' + String(idsSeq).padStart(3, '0'))},${esc(id)},${esc(sk)},0,0,${NOW},${NOW});`);
          idsSeq++;
          if (g.charge >= 2) {
            push(`INSERT INTO app_item_default_skill (id,item_id,skill_id,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('IDS_MAIN_' + String(idsSeq).padStart(3, '0'))},${esc(id)},${esc(ultByAct[act.id])},1,1,${NOW},${NOW});`);
            idsSeq++;
          }
        }
        if (g.basic) {
          const p = passives[Math.min(passiveDefs.length - 1, (tier - 1) * 3)];
          push(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('IDP_MAIN_' + String(idpSeq).padStart(3, '0'))},${esc(id)},${esc(p.id)},'OUT_BASIC',0,0,${NOW},${NOW});`);
          idpSeq++;
        }
      } else if (g.kind === 'ARMOR') {
        push(`INSERT INTO app_item_armor (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES (${esc('ARM_MAIN_' + String(armSeq).padStart(3, '0'))},${esc(id)},${g.hp},${g.def},0,0,${NOW},${NOW});`);
        armSeq++;
        if (g.basic) {
          const p = passives[5 + Math.min(4, tier - 1)];
          push(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('IDP_MAIN_' + String(idpSeq).padStart(3, '0'))},${esc(id)},${esc(p.id)},'OUT_BASIC',0,0,${NOW},${NOW});`);
          idpSeq++;
        }
      } else if (g.kind === 'HELMET') {
        push(`INSERT INTO app_item_helmet (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES (${esc('HEL_MAIN_' + String(helSeq).padStart(3, '0'))},${esc(id)},${g.hp},${g.def},0,0,${NOW},${NOW});`);
        helSeq++;
      } else if (g.kind === 'GLOVES') {
        push(`INSERT INTO app_item_gloves (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES (${esc('GLO_MAIN_' + String(gloSeq).padStart(3, '0'))},${esc(id)},${g.hp},${g.def},0,0,${NOW},${NOW});`);
        gloSeq++;
      } else if (g.kind === 'LEGS') {
        push(`INSERT INTO app_item_legs (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES (${esc('LEG_MAIN_' + String(legSeq).padStart(3, '0'))},${esc(id)},${g.hp},${g.def},0,0,${NOW},${NOW});`);
        legSeq++;
      } else if (g.kind === 'ACCESSORY') {
        push(`INSERT INTO app_item_accessory (id,item_id,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES (${esc('ACC_MAIN_' + String(accSeq).padStart(3, '0'))},${esc(id)},${(tier * 2).toFixed(1)},0,${NOW},${NOW});`);
        accSeq++;
        if (g.basic) {
          const p = passives[10 + Math.min(4, tier - 1)];
          push(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('IDP_MAIN_' + String(idpSeq).padStart(3, '0'))},${esc(id)},${esc(p.id)},'OUT_BASIC',0,0,${NOW},${NOW});`);
          idpSeq++;
        }
      }
    }

    // recipes: mat -> gear
    const recipePairs = [
      { out: gearIds.WEAPON[0], mats: [[matIds[0], 2 + tier], [matIds[1], 1 + tier]] },
      { out: gearIds.ARMOR[0], mats: [[matIds[0], 3 + tier], [matIds[2], 1 + Math.floor(tier / 2)]] },
    ];
    if (gearIds.HELMET[0]) recipePairs.push({ out: gearIds.HELMET[0], mats: [[matIds[1], 2], [matIds[2], 2]] });
    if (gearIds.GLOVES[0]) recipePairs.push({ out: gearIds.GLOVES[0], mats: [[matIds[0], 2], [matIds[1], 2]] });
    if (gearIds.LEGS[0]) recipePairs.push({ out: gearIds.LEGS[0], mats: [[matIds[2], 2], [matIds[3], 1]] });
    if (gearIds.ACCESSORY[0]) recipePairs.push({ out: gearIds.ACCESSORY[0], mats: [[matIds[3], 2], [matIds[1], 2]] });
    if (gearIds.WEAPON[1]) recipePairs.push({ out: gearIds.WEAPON[1], mats: [[matIds[3], 2], [matIds[2], 3], [matIds[0], 2]] });
    if (gearIds.ARMOR[1]) recipePairs.push({ out: gearIds.ARMOR[1], mats: [[matIds[3], 2], [matIds[0], 4]] });

    for (const rp of recipePairs) {
      const outItem = items.find((x) => x.id === rp.out);
      const rid = `RCP_MAIN_${String(rcpSeq).padStart(3, '0')}`;
      recipes.push(rid);
      push(`INSERT INTO app_recipe (id,name,output_item_id,output_qty,sort,enable,CREATE_TIME,UPDATE_TIME) VALUES (${esc(rid)},${esc((outItem ? outItem.name : '装备') + '配方')},${esc(rp.out)},1,${rcpSeq},1,${NOW},${NOW});`);
      rp.mats.forEach((pair, i) => {
        push(`INSERT INTO app_recipe_material (id,recipe_id,item_id,quantity,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc('RCM_MAIN_' + String(rcmSeq).padStart(3, '0'))},${esc(rid)},${esc(pair[0])},${pair[1]},${i},${NOW},${NOW});`);
        rcmSeq++;
      });
      rcpSeq++;
    }

    // monsters
    const chapterBase = (act.id - 1) * 5;
    act.normals.forEach((name, i) => {
      const id = `MST_MAIN_A${act.id}_N${i + 1}`;
      const chapterHint = chapterBase + 1 + (i % 5);
      monsters.push({ id, name, rarity: 'NORMAL', act: act.id, chapterHint, mats: matIds, gear: gearIds });
      monsterByKey[`${act.id}:N:${i}`] = id;
    });
    act.rares.forEach((name, i) => {
      const id = `MST_MAIN_A${act.id}_R${i + 1}`;
      monsters.push({ id, name, rarity: 'RARE', act: act.id, chapterHint: chapterBase + 2 + i, mats: matIds, gear: gearIds, small: smallPool[(act.id + i) % smallPool.length] });
      monsterByKey[`${act.id}:R:${i}`] = id;
    });
    {
      const id = `MST_MAIN_A${act.id}_E1`;
      monsters.push({ id, name: act.epic, rarity: 'EPIC', act: act.id, chapterHint: chapterBase + 4, mats: matIds, gear: gearIds, small: actSmall(act.id), ult: ultByAct[act.id] });
      monsterByKey[`${act.id}:E:0`] = id;
    }
    act.bosses.forEach((name, i) => {
      const id = `MST_MAIN_A${act.id}_B${i + 1}`;
      const chapter = chapterBase + i + 1;
      monsters.push({ id, name, rarity: 'BOSS', act: act.id, chapter, mats: matIds, gear: gearIds, small: actSmall(act.id), ult: ultByAct[act.id], isChapterBoss: true });
      monsterByKey[`${act.id}:B:${i}`] = id;
    });
  }

  // Insert monsters + drops
  push('');
  push('-- ---- 怪物与掉落 ----');
  monsters.forEach((m, idx) => {
    const st = statsFor(m.chapter || m.chapterHint || m.act * 5, m.rarity);
    const [gh, gw] = raritySize(m.rarity);
    const small = m.small || null;
    const ult = m.ult || null;
    push(`INSERT INTO app_monster (id,name,rarity,grid_h,grid_w,base_atk,base_hp,base_def,base_action,sort,normal_skill_id,small_skill_id,ultimate_skill_id,CREATE_TIME,UPDATE_TIME) VALUES (${esc(m.id)},${esc(m.name)},${esc(m.rarity)},${gh},${gw},${st.atk},${st.hp},${st.def},${st.action},${idx + 1},NULL,${small ? esc(small) : 'NULL'},${ult ? esc(ult) : 'NULL'},${NOW},${NOW});`);

    // drops
    const drops = [];
    if (m.mats && m.mats[0]) drops.push({ item: m.mats[0], rate: m.rarity === 'BOSS' ? 100 : 80, min: 1, max: m.rarity === 'BOSS' ? 3 : 2 });
    if (m.mats && m.mats[1]) drops.push({ item: m.mats[1], rate: m.rarity === 'NORMAL' ? 40 : 70, min: 1, max: 2 });
    if (m.rarity === 'RARE' || m.rarity === 'EPIC') {
      if (m.mats[2]) drops.push({ item: m.mats[2], rate: 50, min: 1, max: 1 });
    }
    if (m.rarity === 'BOSS') {
      if (m.mats[3]) drops.push({ item: m.mats[3], rate: 100, min: 1, max: 2 });
      if (m.gear.WEAPON[0]) drops.push({ item: m.gear.WEAPON[0], rate: 15, min: 1, max: 1 });
      if (m.gear.ARMOR[0]) drops.push({ item: m.gear.ARMOR[0], rate: 15, min: 1, max: 1 });
    }
    if (m.rarity === 'EPIC' && m.gear.WEAPON[0]) {
      drops.push({ item: m.gear.WEAPON[0], rate: 8, min: 1, max: 1 });
    }
    drops.forEach((d, di) => {
      push(`INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,CREATE_TIME,UPDATE_TIME) VALUES (${esc('MDP_MAIN_' + String(dropSeq).padStart(4, '0'))},${esc(m.id)},${esc(d.item)},${d.rate},${d.min},${d.max},${di},1,${NOW},${NOW});`);
      dropSeq++;
    });
  });
  push('');

  // ---- Chapters / Levels / Placements ----
  push('-- ---- 章节 / 关卡 / 摆怪 ----');
  let slmSeq = 1;
  for (const act of ACTS) {
    for (let ci = 0; ci < 5; ci++) {
      const chapterNum = (act.id - 1) * 5 + ci + 1;
      const scp = `SCP_MAIN_${String(chapterNum).padStart(2, '0')}`;
      const cname = `第${String(chapterNum).padStart(2, '0')}章 · ${act.chapters[ci]}`;
      push(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${esc(scp)},${esc(TYPE_ID)},'CHAPTER',${esc(cname)},${esc(String(chapterNum).padStart(2, '0'))},${chapterNum},1,${esc(act.name)},${NOW},${NOW});`);

      const n0 = monsterByKey[`${act.id}:N:0`];
      const n1 = monsterByKey[`${act.id}:N:1`];
      const n2 = monsterByKey[`${act.id}:N:${(ci + 2) % 4}`];
      const n3 = monsterByKey[`${act.id}:N:${(ci + 3) % 4}`];
      const r0 = monsterByKey[`${act.id}:R:0`];
      const r1 = monsterByKey[`${act.id}:R:1`];
      const epic = monsterByKey[`${act.id}:E:0`];
      const boss = monsterByKey[`${act.id}:B:${ci}`];

      for (let lv = 1; lv <= 10; lv++) {
        const slv = `SLV_MAIN_${String(chapterNum).padStart(2, '0')}_${String(lv).padStart(2, '0')}`;
        const lname = `${chapterNum}-${lv}`;
        push(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,CREATE_TIME,UPDATE_TIME) VALUES (${esc(slv)},${esc(scp)},'LEVEL',${esc(lname)},${esc(lname)},${lv},1,${NOW},${NOW});`);

        let specs = [];
        if (lv <= 2) specs = [{ id: n0, rarity: 'NORMAL' }];
        else if (lv === 3) specs = [{ id: n0, rarity: 'NORMAL' }, { id: n1, rarity: 'NORMAL' }];
        else if (lv === 4) specs = [{ id: n2, rarity: 'NORMAL' }, { id: n0, rarity: 'NORMAL' }];
        else if (lv === 5) specs = [{ id: n1, rarity: 'NORMAL' }, { id: r0, rarity: 'RARE' }];
        else if (lv === 6) specs = [{ id: n3, rarity: 'NORMAL' }, { id: n2, rarity: 'NORMAL' }, { id: r1, rarity: 'RARE' }];
        else if (lv === 7) specs = [{ id: r0, rarity: 'RARE' }, { id: n0, rarity: 'NORMAL' }];
        else if (lv === 8) specs = [{ id: r1, rarity: 'RARE' }, { id: n1, rarity: 'NORMAL' }, { id: n2, rarity: 'NORMAL' }];
        else if (lv === 9) specs = [{ id: epic, rarity: 'EPIC' }, { id: r0, rarity: 'RARE' }];
        else specs = [{ id: boss, rarity: 'BOSS' }];

        // filter nulls
        specs = specs.filter((s) => s.id);
        const placed = placeUnits(specs);
        placed.forEach((p, pi) => {
          const id = `SLM_MAIN_${String(chapterNum).padStart(2, '0')}${String(lv).padStart(2, '0')}_${String(pi + 1).padStart(2, '0')}`;
          push(`INSERT INTO app_stage_level_monster (id,level_id,monster_id,pos_col,pos_row,sort,CREATE_TIME,UPDATE_TIME) VALUES (${esc(id)},${esc(slv)},${esc(p.monsterId)},${p.posCol},${p.posRow},${pi + 1},${NOW},${NOW});`);
          slmSeq++;
        });
      }
    }
  }

  push('');
  push('COMMIT;');
  push('SET FOREIGN_KEY_CHECKS=1;');
  push('');
  push('-- 校验提示:');
  push('-- SELECT COUNT(*) FROM app_stage WHERE kind = \'CHAPTER\'; -- 30');
  push('-- SELECT COUNT(*) FROM app_stage WHERE kind = \'LEVEL\'; -- 300');
  push('-- SELECT COUNT(*) FROM app_monster;');
  push('-- SELECT COUNT(*) FROM app_item;');
  push('-- SELECT COUNT(*) FROM app_recipe;');

  fs.mkdirSync(path.dirname(OUT), { recursive: true });
  fs.writeFileSync(OUT, lines.join('\n'), 'utf8');

  // stats
  const sql = lines.join('\n');
  const count = (re) => (sql.match(re) || []).length;
  console.log('Wrote', OUT);
  console.log('chapter inserts:', count(/kind,name,code,sort,enable,remark.*CHAPTER|'CHAPTER'/g));
  console.log('level inserts:', (sql.match(/,'LEVEL',/g) || []).length);
  console.log('placements:', count(/INSERT INTO app_stage_level_monster/g));
  console.log('monsters:', count(/INSERT INTO app_monster /g));
  console.log('items:', count(/INSERT INTO app_item /g));
  console.log('recipes:', count(/INSERT INTO app_recipe /g));
  console.log('active skills (new):', skills.length);
  console.log('passives:', passives.length);
  console.log('lines:', lines.length);
}

main();
