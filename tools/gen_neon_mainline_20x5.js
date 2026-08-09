#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'sql', 'seed_neon_mainline_20x5.sql');
const STORY = path.join(__dirname, '..', 'src', 'main', 'resources', '主线', '霓虹远征·第一篇章（1-20章）.md');
const TYPE = 'STY_10000001';
const NORMAL = 'ASK_71867187';
const VALID_DAMAGE_ELEMENTS = new Set(['PHYSICAL','POISON','IGNITE','FREEZE','SHOCK','BURN']);

const chapters = [
  ['灰港苏醒','灰港收容层','失忆的黑衣义体行者在停摆的收容舱醒来，只收到一段指向城外的陌生脉冲。',['舱门微光','清扫轨道','废弃登记处','失控安保线','灰港出口'],['巡线浮虫','锈壳清洁机','失序警戒体','封港执勤官'],['冷凝纤维','旧式芯片','灰港密钥'],'断频斩','灰港切割刃','收容层外骨骼'],
  ['雨幕旧街','雨幕居住环','行者追入终年降雨的旧街，发现脉冲会让废弃招牌短暂复述陌生人的记忆。',['雨棚下的影子','积水电网','无人便利站','旧街追迹','雨幕钟楼'],['导电雨蛭','广告残像','雨披追猎机','钟楼报时者'],['导电胶质','残像玻片','雨幕谐振片'],'回声穿刺','雨幕短枪','绝缘披甲'],
  ['断电车站','零线换乘站','所有列车都已停运，唯有一列无编号车厢仍按不存在的时刻表循环进站。',['熄灯站台','检票残机','空车厢','错位月台','零线终点'],['票据噬虫','轨道巡检机','空厢乘客影','零线列车长'],['磁轨碎片','过期票芯','零线时刻核'],'磁轨横扫','零线轨刃','站务防护服'],
  ['地下换流井','地下换流井','脉冲藏进城市供能主干，行者必须在过载前穿过层层换流阀阵。',['低压入口','电弧回廊','冷却管群','主阀平台','换流深井'],['漏电软体','阀门工偶','过载蓄能体','深井调度核'],['绝缘陶片','蓄能线圈','换流核心'],'电弧回旋','换流刺剑','深井绝缘甲'],
  ['第七码头','第七码头','第一段旅程止于封锁码头。脉冲并非求救，而是一份不断更新的航路邀请。',['货柜迷巷','吊机阴影','走私栈桥','封锁海堤','七码头主泊位'],['货签寄生体','吊臂守卫','走私猎犬机','泊位封锁者'],['货柜合金','走私编码器','七码头航标'],'航标爆裂','码头折刃','潮汐护甲'],
  ['锈潮工坊','锈潮制造带','越过海堤后，城市表皮下的旧制造带仍在为一场早已结束的战争生产零件。',['锈潮入口','冲压长廊','装配支线','废件熔池','总装工坊'],['铆钉爬机','冲压工偶','拼装猎手','锈潮总装师'],['锈钢片','伺服关节','总装权限核'],'伺服连击','锈潮链刃','装配工甲'],
  ['失控铸线','赤热铸造线','铸线将陌生信号误认作最高生产指令，开始制造从未被设计过的黑色躯壳。',['送料口','赤热传送带','模具阵列','淬火池','铸线母机'],['熔滴虫','搬运铸偶','黑模试作体','铸线母机'],['耐热线缆','黑模碎壳','母机印模'],'灼线贯击','赤热重刃','淬火隔热甲'],
  ['黑箱仓库','黑箱封存库','成排黑箱记录着城市主动删除的历史；其中一只黑箱知道主角义体的制造批次。',['外环货架','索引迷宫','封存冷室','销毁通道','零号黑箱'],['索引蜉蝣','封存搬运体','销毁执行机','零号保管者'],['索引片','封存胶囊','零号黑箱核'],'黑箱回响','索引短刃','封存层甲'],
  ['无人工厂','自治兵工厂','制造带的尽头是一座自行扩张的兵工厂，它把主角判定为遗失资产。',['识别闸门','无人机巢','弹药组装区','战术测试场','自治主控室'],['识别探针','蜂群战机','战术试验体','自治军械脑'],['微型翼片','战术模块','军械主控核'],'蜂群扫射','自治脉冲枪','战术复合甲'],
  ['炉心守门','炉心边界','关闭制造带需要穿过炉心。守门者没有敌意，却坚持执行一条比城市更古老的禁令。',['热井边缘','燃料栈道','压力环','守门长阶','炉心门扉'],['焰尘聚体','压力巡检机','炉心侍卫','古令守门者'],['焰尘结晶','压力阀芯','古令印章'],'炉压震荡','炉心断刃','古令耐热甲'],
  ['雾镜商业环','雾镜商业环','地表上层仍维持着繁华投影，居民残像重复消费，仿佛灾难从未发生。',['迎宾光廊','空店橱窗','积分广场','镜面天桥','商业环中枢'],['促销幻体','橱窗巡偶','积分收割者','雾镜经理'],['雾化晶片','消费凭证','商业环账本核'],'镜闪切割','雾镜细剑','橱窗幻甲'],
  ['空壳公寓','空壳居住塔','公寓保存着居民的生活习惯，却没有任何居民。主角听见与自己声纹一致的留言。',['门禁大厅','循环电梯','无人楼层','屋顶水箱','顶层样板间'],['门锁寄生体','家务工偶','声纹拟态者','空壳房东'],['门禁线圈','声纹薄膜','顶层住户核'],'声纹反冲','空壳腕刃','居住塔软甲'],
  ['记忆诊所','白噪诊疗区','诊所声称能修复记忆，代价是交出一段真实经历。系统却找不到主角可供交换的过去。',['候诊长廊','扫描室','记忆药库','手术灯阵','主诊疗舱'],['白噪菌群','诊疗机械臂','记忆缝合体','无面主诊师'],['白噪滤片','记忆凝胶','主诊权限针'],'白噪脉冲','诊疗光刃','记忆隔离衣'],
  ['静默广播塔','静默广播塔','脉冲通过广播塔覆盖全城，反复播送一句被截断的话：不要相信月亮的背面。',['发射塔基','维护梯井','静音层','天线阵列','广播主控台'],['频段蛾','维护爬机','静音执法体','无声播音员'],['频段鳞粉','静音线圈','广播母带'],'静默震波','频段长刃','广播屏蔽甲'],
  ['镜城中继','镜城中继站','塔顶中继将城市映成无数版本。每个版本里，黑衣行者都在走向不同出口。',['镜面入口','折射街区','逆行栈道','重影核心区','中继棱镜'],['折光游虫','重影巡兵','逆行复制体','镜城校准者'],['折光棱片','重影存储体','校准棱核'],'折镜连斩','镜城双刃','折射战甲'],
  ['天穹升降井','天穹升降井','唯一通向云层之上的升降井被封闭多年，脉冲却为主角保留了一枚单程权限。',['井底平台','配重通道','风压层','云门检修站','天穹闸口'],['配重螨','升降维护机','风压拦截者','天穹闸官'],['配重合金','风压叶片','天穹权限环'],'升空突袭','天穹枪刃','风压稳定甲'],
  ['云上墓园','云上数据墓园','云层上漂浮着停机义体的墓碑。部分墓碑使用了与主角相同的匿名编号。',['云阶入口','无名碑林','备份回廊','守墓平台','最高纪念碑'],['碑文萤体','备份守卫','匿名残躯','云上守墓人'],['碑文晶砂','备份芯核','匿名纪念章'],'墓碑回声','云墓长刀','备份护衣'],
  ['轨道残桥','轨道残桥','离开墓园的轨道桥通向近地空间，但桥面正被某种来自月面的潮汐逐段抹除。',['残桥起点','失重接缝','碎轨区','潮汐断面','轨道桥头堡'],['碎轨游体','接缝维修机','月潮蚀刻者','残桥镇守机'],['轨道陶钢','月潮粉尘','桥头堡密钥'],'失重穿行','轨道折枪','月潮抗蚀甲'],
  ['零号观测站','零号观测站','观测站从未观测星空，只监视地面每一个试图离开城市的人。主角是第七个抵达者。',['气闸前庭','观测档案层','追踪阵列','零号镜室','站长控制台'],['观测尘灵','追踪眼机','镜室抹除体','零号站长'],['观测镜屑','追踪算法核','站长记录盘'],'观测锁定','零号狙刃','镜室隐匿甲'],
  ['月背门槛','月背通讯门','观测站后的通讯门并不通向月球，而是通向一张覆盖诸城的未知网络。门后传来更多行者的心跳。',['通讯前室','引力锁','门槛回廊','未知握手','月背门扉'],['引力游丝','握手协议体','门槛纠错者','月背接引机'],['引力纤维','协议密钥','月背坐标核'],'月背共振','门槛黑刃','接引者外装']
];

// 每章技能石的机制模板：目标、效果、段数和充能事件均服务于章节叙事。
const stoneStyles = [
  ['FIRST','DAMAGE',2,'CAST','ANY_TYPE','NORMAL','苏醒后的短促双斩'],
  ['RANDOM_ENEMY','DAMAGE',3,'DEAL_DAMAGE','ANY',null,'雨滴般随机追击'],
  ['FRONT_ROW','DAMAGE',1,'CAST','ANY_TYPE','NORMAL','沿轨道横扫前排'],
  ['ALL_ENEMY','DAMAGE',1,'TAKE_DAMAGE','ANY',null,'受击蓄积后释放电弧'],
  ['BACK_ROW','DAMAGE',2,'KILL','ANY',null,'航标锁定敌方后排'],
  ['FIRST','DAMAGE',4,'CAST','ANY_TYPE','NORMAL','伺服结构连续冲压'],
  ['FRONT_ROW','DAMAGE',2,'DEAL_DAMAGE','ANY_ELEMENT',null,'赤热铸线灼烧前排'],
  ['RANDOM_ENEMY','DAMAGE',3,'TAKE_DAMAGE','ANY',null,'黑箱回放三段残响'],
  ['ALL_ENEMY','DAMAGE',3,'CAST','ANY_TYPE','SMALL','蜂群协议覆盖全场'],
  ['SELF','HEAL',1,'TAKE_DAMAGE','ANY',null,'炉压转化为自我修复'],
  ['ALL_ENEMY','DAMAGE',1,'DEAL_DAMAGE','ANY',null,'雾镜折射扩散'],
  ['SELF','HEAL',2,'CAST','ANY_TYPE','NORMAL','声纹回响修复躯体'],
  ['ALLY_MIN_HP','HEAL',1,'DEAL_DAMAGE','ANY',null,'诊疗协议修复最低生命友军'],
  ['ALL_ENEMY','DAMAGE',2,'CAST','ANY_TYPE','SMALL','静默频段震荡全场'],
  ['RANDOM_ENEMY','DAMAGE',4,'DEAL_DAMAGE','ANY',null,'镜像在随机目标间折返'],
  ['FIRST','DAMAGE',2,'KILL','ANY',null,'升空动能贯穿首位目标'],
  ['ALL_ALLY','HEAL',1,'TAKE_DAMAGE','ANY',null,'备份数据修复全体己方'],
  ['FRONT_ROW','DAMAGE',3,'CAST','ANY_TYPE','NORMAL','失重状态切开前排'],
  ['ENEMY_MAX_ATK','DAMAGE',1,'DEAL_DAMAGE','ANY',null,'观测阵列锁定最高攻击目标'],
  ['ALL_ENEMY','DAMAGE',2,'CAST','ANY_TYPE','SMALL','月背协议与全场共振']
];

const q = v => v == null ? 'NULL' : `'${String(v).replace(/'/g,"''")}'`;
const cleanNumber = n => String(Number(Number(n).toFixed(8)));
const formula = m => JSON.stringify([{kind:'PARAM',paramMode:'READ',readRole:'SELF',readCategory:'ATTR',readKey:'ATK'},{kind:'OP',op:'*'},{kind:'PARAM',paramMode:'LITERAL',value:cleanNumber(m)}]);
const literalFormula = n => JSON.stringify([{kind:'PARAM',paramMode:'LITERAL',value:cleanNumber(n)}]);
const lines = [];
const p = s => lines.push(s);
const now = 'NOW()';

function item(id, code, name, type, sort, remark, chargeSlots=0, basicSlots=0, advancedSlots=0, startSlots=0, combatSlots=0) {
  p(`INSERT INTO app_item (id,code,name,item_type,max_stack,sort,enable,remark,charge_skill_slot_count,basic_passive_slot_count,advanced_passive_slot_count,battle_start_passive_slot_count,battle_combat_passive_slot_count,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(code)},${q(name)},${q(type)},${type==='MATERIAL'?99:1},${sort},1,${q(remark)},${chargeSlots},${basicSlots},${advancedSlots},${startSlots},${combatSlots},${now},${now});`);
}
function skill(id,name,type,charge,mul,target,element,sort,style) {
  if (!VALID_DAMAGE_ELEMENTS.has(element)) throw new Error(`invalid DamageElement ${element} for ${id}`);
  const effect=style?.[1]||'DAMAGE', hits=style?.[2]||1, event=style?.[3]||'CAST';
  const match=style?.[4]||'ANY_TYPE', matchType=style?.[5]||(style?.[4]?'':'NORMAL'), flavor=style?.[6]||'章节战斗技能';
  p(`INSERT INTO app_active_skill (id,name,skill_type,skill_school,damage_element,code,need_charge_mode,need_charge,max_cast_skill,max_cast_global,max_cast_all_means,max_cast_role,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(type)},'脉冲',${q(element)},${q(id)},'MANUAL',${charge},0,0,0,0,${sort},1,${q(flavor)},${now},${now});`);
  p(`INSERT INTO app_skill_effect (id,skill_id,name,target_type,effect_type,formula_json,hit_segments,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SEF_'+id)},${q(id)},${q(name)},${q(target)},${q(effect)},${q(formula(mul))},${hits},0,${q(flavor)},${now},${now});`);
  if(event==='ACTION_VALUE') {
    p(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,every_action_value,charge_gain,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SCH_'+id)},${q(id)},'行动值充能','ACTION_VALUE','GLOBAL',${style?.[7]||100},${charge},0,${q(flavor)},${now},${now});`);
  } else {
    const chargeLabel={CAST:'释放充能',RECEIVE:'受技能充能',TAKE_DAMAGE:'受击充能',DEAL_DAMAGE:'伤害充能',KILL:'击杀充能'}[event]||'事件充能';
    p(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,charge_gain,skill_charge_event,skill_charge_match,match_skill_type,match_damage_element,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SCH_'+id)},${q(id)},${q(chargeLabel)},'SKILL_CHARGE','GLOBAL',1,${q(event)},${q(match)},${matchType?q(matchType):'NULL'},${match==='ANY_ELEMENT'?q(element):'NULL'},0,${q(flavor)},${now},${now});`);
  }
}
function passive(id,name,key,value,itemId,sort,remark) {
  p(`INSERT INTO app_passive_skill (id,name,code,passive_type,condition_mode,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(id)},'OUT_BASIC','UNLIMITED',${sort},1,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_passive_effect (id,skill_id,attr_key,attr_dir,value_num,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('PSE_'+id)},${q(id)},${q(key)},'INCREASE',${value},0,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q('IDP_'+id)},${q(itemId)},${q(id)},'OUT_BASIC',1,0,${now},${now});`);
}
function advancedPassive(id,name,key,dir,value,itemId,sort,remark) {
  p(`INSERT INTO app_passive_skill (id,name,code,passive_type,condition_mode,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(id)},'OUT_ADVANCED','UNLIMITED',${sort},1,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_passive_effect (id,skill_id,attr_key,attr_dir,value_num,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('PSE_'+id)},${q(id)},${q(key)},${q(dir)},${value},0,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q('IDP_'+id)},${q(itemId)},${q(id)},'OUT_ADVANCED',1,0,${now},${now});`);
}
function battlePassive(id,name,type,itemId,sort,remark,config,outputs) {
  if (config.refElement && !VALID_DAMAGE_ELEMENTS.has(config.refElement)) throw new Error(`invalid ref DamageElement ${config.refElement} for ${id}`);
  outputs.forEach(o=>{ if (o.element && !VALID_DAMAGE_ELEMENTS.has(o.element)) throw new Error(`invalid output DamageElement ${o.element} for ${id}`); });
  const event=config.event||null, rule=config.rule||null, elapsed=config.elapsed||0, max=config.max||0;
  const match=config.match||null, refType=config.refType||null, refElement=config.refElement||null;
  p(`INSERT INTO app_passive_skill (id,name,code,passive_type,condition_mode,skill_match_mode,ref_skill_type,ref_damage_element,max_trigger_per_battle,combat_event,start_apply_rule,start_elapsed_av,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(id)},${q(type)},'UNLIMITED',${match?q(match):'NULL'},${refType?q(refType):'NULL'},${refElement?q(refElement):'NULL'},${max},${event?q(event):'NULL'},${rule?q(rule):'NULL'},${elapsed},${sort},1,${q(remark)},${now},${now});`);
  outputs.forEach((o,i)=>p(`INSERT INTO app_skill_output (id,skill_id,passive_skill_id,name,output_kind,target_type,attr_key,attr_dir,effect_type,damage_element,formula_json,hit_segments,trigger_rate,duration_av,buff_def_id,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SOUT_'+id+'_'+(i+1))},NULL,${q(id)},${q(o.name)},${q(o.kind)},${q(o.target)},${o.attr?q(o.attr):'NULL'},${o.dir?q(o.dir):'NULL'},${o.effect?q(o.effect):'NULL'},${q(o.element||'PHYSICAL')},${o.literal!=null?q(literalFormula(o.literal)):o.mul!=null?q(formula(o.mul)):'NULL'},${o.hits||1},${o.rate??100},${o.duration||0},NULL,${i},${q(remark)},${now},${now});`));
  p(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q('IDP_'+id)},${q(itemId)},${q(id)},${q(type)},1,0,${now},${now});`);
}

function conditionedBattlePassive(id,name,type,itemId,sort,remark,config,outputs,condition) {
  battlePassive(id,name,type,itemId,sort,remark,config,outputs);
  p(`UPDATE app_passive_skill SET condition_mode='SELECT' WHERE id=${q(id)};`);
  const refItem=condition.itemId?q(condition.itemId):'NULL';
  const refSkill=condition.skillId?q(condition.skillId):'NULL';
  const kind=condition.itemId?'EQUIP_ITEM':'EQUIP_SKILL';
  p(`INSERT INTO app_passive_condition (id,skill_id,condition_type,ref_item_id,ref_skill_id,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('PCD_'+id)},${q(id)},${q(kind)},${refItem},${refSkill},0,${q(condition.remark)},${now},${now});`);
}

const coreBuilds = [
  ['雨幕过载枪','回响电弧','SHOCK','AFTER_DEAL_ACTIVE_DMG','ANY_ELEMENT','雨幕旧街信标','将电击伤害折返为连锁电弧，并回收少量生命'],
  ['零线磁轨刃','磁轨超导','SHOCK','AFTER_CAST_SKILL','ANY_TYPE','地下换流井信标','普通技能驱动磁轨追击，连续释放越快收益越高'],
  ['锈潮航标炮','锈潮猎杀','PHYSICAL','AFTER_KILL','ANY','锈潮工坊信标','击杀后回收装甲并向下一目标补射'],
  ['黑箱熔核刃','熔核灼印','BURN','AFTER_DEAL_ACTIVE_DMG','ANY_ELEMENT','黑箱仓库信标','火焰伤害引爆灼印，同时把余热转化为治疗'],
  ['炉心蜂群枪','蜂群热链','BURN','AFTER_CAST_SKILL','ANY_TYPE','炉心守门信标','小技能放出蜂群热链，对全体目标造成持续压迫'],
  ['白噪声纹杖','白噪回生','FREEZE','AFTER_TAKE_ACTIVE_DMG','ANY','记忆诊所信标','受击后生成白噪护持，兼顾恢复与减伤'],
  ['静默折镜刃','静默折返','FREEZE','AFTER_CAST_SKILL','ANY_TYPE','镜城中继信标','技能在镜面间折返，形成多段随机追击'],
  ['天穹云墓枪','云墓备份','PHYSICAL','AFTER_KILL','ANY','云上墓园信标','击杀写入备份，恢复生命并提升行动效率'],
  ['月潮观测炮','观测锁杀','SHOCK','AFTER_DEAL_ACTIVE_DMG','ANY_ELEMENT','零号观测站信标','电击命中后锁定高威胁目标并追加炮击'],
  ['月背共振刃','月背共振','BURN','AFTER_CAST_SKILL','ANY_TYPE','月背门槛信标','小技能唤起全场共振，兼具群伤与自我修复']
];

p('-- 霓虹远征第一篇章：20 大关 × 5 小关。由 tools/gen_neon_mainline_20x5.js 生成。');
p('SET NAMES utf8mb4; SET FOREIGN_KEY_CHECKS=0; START TRANSACTION;');
p(`DELETE FROM app_stage_first_reward WHERE stage_id IN (SELECT id FROM (SELECT s.id FROM app_stage s LEFT JOIN app_stage c ON s.parent_id=c.id WHERE s.parent_id=${q(TYPE)} OR c.parent_id=${q(TYPE)}) x);`);
p(`DELETE FROM app_stage_level_monster WHERE level_id IN (SELECT id FROM (SELECT s.id FROM app_stage s JOIN app_stage c ON s.parent_id=c.id WHERE c.parent_id=${q(TYPE)}) x);`);
p(`DELETE FROM app_player_stage_level WHERE level_id IN (SELECT id FROM (SELECT s.id FROM app_stage s JOIN app_stage c ON s.parent_id=c.id WHERE c.parent_id=${q(TYPE)}) x);`);
p(`DELETE FROM app_player_stage_chapter WHERE chapter_id IN (SELECT id FROM (SELECT id FROM app_stage WHERE parent_id=${q(TYPE)}) x);`);
p(`DELETE FROM app_stage WHERE parent_id IN (SELECT id FROM (SELECT id FROM app_stage WHERE parent_id=${q(TYPE)}) x);`);
p(`DELETE FROM app_stage WHERE parent_id=${q(TYPE)};`);
p("DELETE FROM app_stage_first_reward WHERE stage_id LIKE 'SLV_N20_%' OR stage_id LIKE 'SCP_N20_%';");
p("DELETE FROM app_stage_level_monster WHERE level_id LIKE 'SLV_N20_%';");
p("DELETE FROM app_player_stage_level WHERE level_id LIKE 'SLV_N20_%';");
p("DELETE FROM app_player_stage_chapter WHERE chapter_id LIKE 'SCP_N20_%';");
p("DELETE FROM app_stage WHERE id LIKE 'SLV_N20_%'; DELETE FROM app_stage WHERE id LIKE 'SCP_N20_%';");
p("DELETE FROM app_monster_drop WHERE id LIKE 'MDP_N20_%'; DELETE FROM app_recipe_material WHERE id LIKE 'RCM_N20_%'; DELETE FROM app_recipe WHERE id LIKE 'RCP_N20_%';");
p("DELETE FROM app_item_default_skill WHERE id LIKE 'IDS_N20_%'; DELETE FROM app_item_default_passive WHERE id LIKE 'IDP%N20_%'; DELETE FROM app_item_weapon WHERE id LIKE 'WPN_N20_%'; DELETE FROM app_item_armor WHERE id LIKE 'ARM_N20_%'; DELETE FROM app_item_gloves WHERE id LIKE 'GLO_N20_%'; DELETE FROM app_item_helmet WHERE id LIKE 'HEL_N20_%'; DELETE FROM app_item_legs WHERE id LIKE 'LEG_N20_%'; DELETE FROM app_item_accessory WHERE id LIKE 'ACC_N20_%'; DELETE FROM app_item_material WHERE id LIKE 'MAT_N20_%';");
p("DELETE FROM app_skill_charge WHERE skill_id LIKE 'ASK_N20_%'; DELETE FROM app_skill_effect WHERE skill_id LIKE 'ASK_N20_%'; DELETE FROM app_active_skill WHERE id LIKE 'ASK_N20_%';");
p("DELETE FROM app_skill_output WHERE passive_skill_id LIKE 'PSK_N20_%'; DELETE FROM app_passive_effect WHERE skill_id LIKE 'PSK_N20_%'; DELETE FROM app_passive_condition WHERE skill_id LIKE 'PSK_N20_%'; DELETE FROM app_passive_skill WHERE id LIKE 'PSK_N20_%';");
p("DELETE FROM app_monster WHERE id LIKE 'MST_N20_%'; DELETE FROM app_item WHERE id LIKE 'ITM_N20_%';");
p(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,CREATE_TIME,UPDATE_TIME) SELECT ${q(TYPE)},NULL,'TYPE','主线','MAIN',0,1,'霓虹远征',${now},${now} FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM app_stage WHERE id=${q(TYPE)});`);

const story=[];
story.push('# 霓虹远征·第一篇章（1—20章）','', '> 本篇是可继续扩展的第一段远征：只揭开“月背网络”的入口，不揭晓主角最终身世，也不解决世界核心矛盾。','');

chapters.forEach((c, ix) => {
  const no=ix+1, nn=String(no).padStart(2,'0');
  const [name,zone,intro,levels,mons,mats,skillName,weaponName,armorName]=c;
  const chapterId=`SCP_N20_${nn}`;
  const matIds=mats.map((m,i)=>`ITM_N20_${nn}_M${i+1}`);
  const weapon=`ITM_N20_${nn}_W`, armor=`ITM_N20_${nn}_A`, gloves=`ITM_N20_${nn}_G`;
  const helmet=`ITM_N20_${nn}_H`, legs=`ITM_N20_${nn}_L`, accessory=`ITM_N20_${nn}_R`, stone=`ITM_N20_${nn}_S`;
  const small=`ASK_N20_${nn}_S`, ult=`ASK_N20_${nn}_U`, player=`ASK_N20_${nn}_P`;
  const element=['PHYSICAL','SHOCK','BURN'][ix%3];
  const stoneStyle=stoneStyles[ix];
  const enemyEvents=['CAST','TAKE_DAMAGE','DEAL_DAMAGE','ACTION_VALUE','RECEIVE'];
  const smallEvent=enemyEvents[ix%enemyEvents.length], ultEvent=enemyEvents[(ix+2)%enemyEvents.length];
  const smallStyle=['FIRST','DAMAGE',1+(ix%3===0?1:0),smallEvent,smallEvent==='CAST'?'ANY_TYPE':smallEvent==='DEAL_DAMAGE'?'ANY_ELEMENT':'ANY',smallEvent==='CAST'?'NORMAL':null,`${zone}单位按本章战斗事件积蓄突击能量`,80+no*4];
  const ultStyle=['ALL_ENEMY','DAMAGE',1+(ix%4===0?1:0),ultEvent,ultEvent==='CAST'?'ANY_TYPE':ultEvent==='DEAL_DAMAGE'?'ANY_ELEMENT':'ANY',ultEvent==='CAST'?'SMALL':null,`${name}守关者按本章战斗事件积蓄过载能量`,150+no*6];
  skill(small,`${zone}突击`,'SMALL',Math.min(8+Math.floor(no/3),14),1.15+no*.025,'FIRST',element,no*3,smallStyle);
  skill(ult,`${name}过载`,'ULTIMATE',Math.min(18+Math.floor(no/2),28),1.7+no*.045,'ALL_ENEMY',element,no*3+1,ultStyle);
  skill(player,skillName,'SMALL',Math.min(5+Math.floor(no/4),10),stoneStyle[1]==='HEAL'?0.75+no*.025:1.25+no*.035,stoneStyle[0],element,no*3+2,stoneStyle);
  mats.forEach((m,i)=>{ item(matIds[i],`n20_${nn}_m${i+1}`,m,'MATERIAL',no*100+i,`${zone}材料`); p(`INSERT INTO app_item_material (id,item_id,grade,CREATE_TIME,UPDATE_TIME) VALUES ('MAT_N20_${nn}_${i+1}',${q(matIds[i])},${1+Math.floor(ix/4)},${now},${now});`); });
  const glovesName=`${zone}触控护手`, helmetName=`${zone}感应头盔`, legsName=`${zone}稳定护腿`;
  item(weapon,`n20_${nn}_weapon`,weaponName,'WEAPON',no*100+10,`附带充能技【${zone}突击】`,1);
  item(armor,`n20_${nn}_armor`,armorName,'ARMOR',no*100+11,`附带高级生存被动`,0,0,1);
  item(gloves,`n20_${nn}_gloves`,glovesName,'GLOVES',no*100+12,`附带高级输出被动`,0,0,1);
  item(helmet,`n20_${nn}_helmet`,helmetName,'HELMET',no*100+13,`受击或接收技能时触发防护效果`,0,0,0,0,1);
  item(legs,`n20_${nn}_legs`,legsName,'LEGS',no*100+14,`开战或经过指定行动值后启动`,0,0,0,1,0);
  item(accessory,`n20_${nn}_accessory`,`${name}信标`,'ACCESSORY',no*100+15,`释放、伤害或击杀时触发章节效果`,0,0,0,0,1);
  item(stone,`n20_${nn}_stone`,`${skillName}技能石`,'SKILL_STONE',no*100+16,`装入技能槽后获得【${skillName}】：${stoneStyle[6]}`,1);
  p(`INSERT INTO app_item_weapon (id,item_id,base_atk,atk_speed_up_ratio,atk_speed_down_ratio,normal_skill_id,CREATE_TIME,UPDATE_TIME) VALUES ('WPN_N20_${nn}',${q(weapon)},${4+no*3},${Math.min(2+no*.2,6).toFixed(1)},0,${q(NORMAL)},${now},${now});`);
  p(`INSERT INTO app_item_armor (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES ('ARM_N20_${nn}',${q(armor)},${25+no*18},${1+Math.floor(no*.8)},0,0,${now},${now});`);
  p(`INSERT INTO app_item_gloves (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('GLO_N20_${nn}',${q(gloves)},${8+no*5},${Math.floor(no*.25)},${Math.min(1+no*.15,4).toFixed(2)},0,'章节输出护手',${now},${now});`);
  p(`INSERT INTO app_item_helmet (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('HEL_N20_${nn}',${q(helmet)},${12+no*7},${1+Math.floor(no*.55)},0,0,'章节防护头盔',${now},${now});`);
  p(`INSERT INTO app_item_legs (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('LEG_N20_${nn}',${q(legs)},${16+no*9},${1+Math.floor(no*.4)},0,0,'章节稳定护腿',${now},${now});`);
  p(`INSERT INTO app_item_accessory (id,item_id,remark,CREATE_TIME,UPDATE_TIME) VALUES ('ACC_N20_${nn}_R',${q(accessory)},'章节信标',${now},${now}),('ACC_N20_${nn}_S',${q(stone)},'技能石扩展复用饰品结构',${now},${now});`);
  p(`INSERT INTO app_item_default_skill (id,item_id,skill_id,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES ('IDS_N20_${nn}_W',${q(weapon)},${q(small)},1,0,${now},${now}),('IDS_N20_${nn}_S',${q(stone)},${q(player)},1,0,${now},${now});`);
  const armorAdvanced=[['TAKEN_DMG_RATIO','DECREASE',2+Math.floor(no/5)],['FINAL_HP','INCREASE',3+Math.floor(no/4)],['TAKEN_ELEMENT_DMG_RATIO','DECREASE',2+Math.floor(no/6)]][ix%3];
  const gloveAdvanced=[['ATK_SPEED','INCREASE',2+Math.floor(no/4)],['DEAL_DMG_RATIO','INCREASE',2+Math.floor(no/5)],['LIFE_STEAL','INCREASE',1+Math.floor(no/5)],['DEAL_ELEMENT_DMG_RATIO','INCREASE',2+Math.floor(no/5)]][ix%4];
  advancedPassive(`PSK_N20_${nn}_A`,`${name}装甲协议`,armorAdvanced[0],armorAdvanced[1],armorAdvanced[2],armor,no*10,`${armorName}根据区域威胁调整高级防护参数`);
  advancedPassive(`PSK_N20_${nn}_G`,`${name}触控协议`,gloveAdvanced[0],gloveAdvanced[1],gloveAdvanced[2],gloves,no*10+1,`${glovesName}将章节信号转化为高级输出参数`);
  const helmetEvent=ix%2===0?'AFTER_TAKE_ACTIVE_DMG':'AFTER_RECEIVE_SKILL';
  const helmetOutput=ix%2===0
    ? [{name:`${name}应急修复`,kind:'EFFECT',target:'SELF',effect:'HEAL',mul:.12+no*.004,rate:35}]
    : [{name:`${name}防壁增压`,kind:'ATTR',target:'SELF',attr:'DEF',dir:'INCREASE',literal:2+Math.floor(no/4),duration:180,rate:45}];
  battlePassive(`PSK_N20_${nn}_H`,`${name}感应防壁`,'BATTLE_COMBAT',helmet,no*10+2,`${helmetName}解析来袭技能并触发防护`,{event:helmetEvent,match:'ANY',max:6},helmetOutput);
  const legRule=ix%3===0?'IMMEDIATE':ix%3===1?'AT_ELAPSED_ONCE':'EVERY_ELAPSED';
  battlePassive(`PSK_N20_${nn}_L`,`${name}步态程序`,'BATTLE_START',legs,no*10+3,`${legsName}按行动值启动机动程序`,{rule:legRule,elapsed:legRule==='IMMEDIATE'?0:120+no*5,max:legRule==='EVERY_ELAPSED'?4:1},[{name:`${name}步态增压`,kind:'ATTR',target:'SELF',attr:ix%2?'ATK_SPEED':'DEF',dir:'INCREASE',literal:ix%2?3+Math.floor(no/5):2+Math.floor(no/5),duration:legRule==='IMMEDIATE'?0:160,rate:100}]);
  const accessoryEvent=['AFTER_CAST_SKILL','AFTER_DEAL_ACTIVE_DMG','AFTER_KILL'][ix%3];
  const accessoryMatch=accessoryEvent==='AFTER_CAST_SKILL'?(ix%2?'ANY_TYPE':'ANY_ELEMENT'):'ANY';
  const accessoryConfig={event:accessoryEvent,match:accessoryMatch,refType:accessoryMatch==='ANY_TYPE'?(ix%4<2?'NORMAL':'SMALL'):null,refElement:accessoryMatch==='ANY_ELEMENT'?element:null,max:accessoryEvent==='AFTER_KILL'?3:8};
  const accessoryOutputs=accessoryEvent==='AFTER_KILL'
    ? [{name:`${name}回收修复`,kind:'EFFECT',target:'SELF',effect:'HEAL',mul:.22+no*.004,rate:100}]
    : [{name:`${name}信标追击`,kind:'EFFECT',target:'EVENT_HIT_TARGETS',effect:'DAMAGE',element,mul:.16+no*.005,hits:ix%4===0?2:1,rate:30+ix%3*10}];
  battlePassive(`PSK_N20_${nn}_R`,`${name}信标联动`,'BATTLE_COMBAT',accessory,no*10+4,`${name}信标在战斗事件后产生章节联动效果`,accessoryConfig,accessoryOutputs);
  const outs=[weapon,armor,gloves,helmet,legs,accessory,stone];
  const outNames=[weaponName,armorName,glovesName,helmetName,legsName,`${name}信标`,`${skillName}技能石`];
  outs.forEach((out,i)=>{ const rid=`RCP_N20_${nn}_${i+1}`; p(`INSERT INTO app_recipe (id,name,output_item_id,output_qty,unlock_chapter_id,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(rid)},${q(outNames[i]+'配方')},${q(out)},1,${q(chapterId)},${no*10+i},1,'随主线章节解锁',${now},${now});`); [[matIds[i%3],2+Math.floor(no/5)],[matIds[(i+1)%3],1+(i===6?1:0)]].forEach((x,j)=>p(`INSERT INTO app_recipe_material (id,recipe_id,item_id,quantity,sort,CREATE_TIME,UPDATE_TIME) VALUES ('RCM_N20_${nn}_${i+1}_${j+1}',${q(rid)},${q(x[0])},${x[1]},${j},${now},${now});`)); });
  const monsterIds=mons.map((m,i)=>`MST_N20_${nn}_${i+1}`);
  mons.forEach((m,i)=>{ const rarity=i===3?'BOSS':i===2?'RARE':'NORMAL'; const mult=i===3?4.2:i===2?1.7:1; const hp=Math.round((35+no*25)*mult), atk=Math.round((5+no*2.2)*mult), def=Math.round(no*.75*mult), action=i===0?75:i===1?85:i===2?95:110; const size=rarity==='BOSS'?[2,4]:rarity==='RARE'?[1,2]:[1,1]; p(`INSERT INTO app_monster (id,name,rarity,role_category,grid_h,grid_w,base_atk,base_hp,base_def,base_action,sort,normal_skill_id,small_skill_id,ultimate_skill_id,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(monsterIds[i])},${q(m)},${q(rarity)},'MONSTER',${size[0]},${size[1]},${atk},${hp},${def},${action},${no*10+i},NULL,${i>=2?q(small):'NULL'},${i===3?q(ult):'NULL'},${q(zone+'生态单位')},${now},${now});`); const drops=i===3?[[matIds[2],100,1,2],[stone,12,1,1]]:i===2?[[matIds[1],75,1,2],[matIds[2],35,1,1]]:[[matIds[0],85,1,2],[matIds[1],30,1,1]]; drops.forEach((d,j)=>p(`INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES ('MDP_N20_${nn}_${i+1}_${j+1}',${q(monsterIds[i])},${q(d[0])},${d[1]},${d[2]},${d[3]},${j},1,'章节生态掉落',${now},${now});`)); });
  p(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(chapterId)},${q(TYPE)},'CHAPTER',${q(name)},${q('N20-'+nn)},${no},1,${q(intro)},${now},${now});`);
  levels.forEach((ln,li)=>{ const lv=li+1, lid=`SLV_N20_${nn}_${lv}`; const stamina=Math.min(1+Math.floor(ix/4),5); p(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,stamina_cost,CREATE_TIME,UPDATE_TIME) VALUES (${q(lid)},${q(chapterId)},'LEVEL',${q(ln)},${q(`N20-${nn}-${lv}`)},${lv},1,${q(`${intro}｜节点：${ln}`)},${stamina},${now},${now});`); const specs=lv===1?[0]:lv===2?[0,1]:lv===3?[0,1,1]:lv===4?[1,2]:[3]; const spots=[[2,4],[0,4],[4,4],[1,3]]; specs.forEach((mi,si)=>{ const sp=spots[si]; p(`INSERT INTO app_stage_level_monster (id,level_id,monster_id,pos_col,pos_row,sort,CREATE_TIME,UPDATE_TIME) VALUES ('SLM_N20_${nn}_${lv}_${si+1}',${q(lid)},${q(monsterIds[mi])},${sp[0]},${sp[1]},${si},${now},${now});`); }); if(lv===5) p(`INSERT INTO app_stage_first_reward (id,stage_id,item_id,qty,sort,CREATE_TIME,UPDATE_TIME) VALUES ('SFR_N20_${nn}',${q(lid)},${q(matIds[2])},1,0,${now},${now});`); });
  story.push(`## 第${no}章　${name}`,'',intro,'',...levels.map((x,i)=>`- ${no}-${i+1} ${x}：${i===0?'进入并辨认区域规则':i===1?'遭遇基础生态与资源':i===2?'发现异常线索进一步扩大':i===3?'突破精英封锁并取得关键材料':'击败守关单位，获得前往下一章的坐标'}`),'',`怪物：${mons.join('、')}。材料：${mats.join('、')}。装备：${weaponName}、${armorName}、${glovesName}、${helmetName}、${legsName}、${name}信标。技能石【${skillName}】：${stoneStyle[6]}。`,'');
});

// 每两章解锁一件可免费合成的流派核心。高昂用量让它成为长期目标，而不是替代普通章节装备。
coreBuilds.forEach((cfg,ix)=>{
  const pair=ix+1, unlockNo=pair*2, nn=String(unlockNo).padStart(2,'0');
  const prev=String(unlockNo-1).padStart(2,'0');
  const itemId=`ITM_N20_CORE_${String(pair).padStart(2,'0')}`;
  const skillId=`ASK_N20_CORE_${String(pair).padStart(2,'0')}`;
  const passiveId=`PSK_N20_CORE_${String(pair).padStart(2,'0')}`;
  const advId=`PSK_N20_CORE_ADV_${String(pair).padStart(2,'0')}`;
  const recipeId=`RCP_N20_CORE_${String(pair).padStart(2,'0')}`;
  const linkedAccessory=`ITM_N20_${nn}_R`;
  const flavor=`双章流派核心·${cfg[1]}。${cfg[6]}；需同时装备第${unlockNo}章信标激活完整联动。`;
  item(itemId,`n20_core_${pair}`,cfg[0],'WEAPON',5000+pair,flavor,1,0,1,0,1);
  const style=['FIRST','DAMAGE',pair%3+2,pair%2?'DEAL_DAMAGE':'CAST',pair%2?'ANY_ELEMENT':'ANY_TYPE',pair%2?null:'SMALL',flavor];
  skill(skillId,`${cfg[1]}·核心释放`,pair%2?'SMALL':'NORMAL',8+pair,1.15+pair*.12,pair%3===0?'ALL_ENEMY':pair%3===1?'RANDOM_ENEMY':'FRONT_ROW',cfg[2],5000+pair,style);
  p(`INSERT INTO app_item_weapon (id,item_id,base_atk,atk_speed_up_ratio,atk_speed_down_ratio,normal_skill_id,CREATE_TIME,UPDATE_TIME) VALUES (${q('WPN_N20_CORE_'+pair)},${q(itemId)},${18+unlockNo*4},${cleanNumber(5+pair*.6)},0,${q(NORMAL)},${now},${now});`);
  p(`INSERT INTO app_item_default_skill (id,item_id,skill_id,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q('IDS_N20_CORE_'+pair)},${q(itemId)},${q(skillId)},1,0,${now},${now});`);
  advancedPassive(advId,`${cfg[1]}增幅`,'DEAL_ELEMENT_DMG_RATIO','INCREASE',8+pair*2,itemId,5000+pair,`${cfg[1]}流派的常驻高级增伤`);
  const outputs=cfg[3]==='AFTER_KILL'
    ? [{name:`${cfg[1]}回收`,kind:'EFFECT',target:'SELF',effect:'HEAL',mul:.5+pair*.03,rate:100},{name:`${cfg[1]}补射`,kind:'EFFECT',target:'RANDOM_ENEMY',effect:'DAMAGE',element:cfg[2],mul:.65+pair*.04,rate:100}]
    : [{name:`${cfg[1]}连携`,kind:'EFFECT',target:pair%3===0?'ALL_ENEMY':'EVENT_HIT_TARGETS',effect:'DAMAGE',element:cfg[2],mul:.38+pair*.045,hits:pair%3+1,rate:45+pair*3},{name:`${cfg[1]}回流`,kind:'EFFECT',target:'SELF',effect:'HEAL',mul:.18+pair*.018,rate:55+pair*3}];
  conditionedBattlePassive(passiveId,`${cfg[1]}完全联动`,'BATTLE_COMBAT',itemId,5100+pair,flavor,{event:cfg[3],match:cfg[4],refType:cfg[4]==='ANY_TYPE'?'SMALL':null,refElement:cfg[4]==='ANY_ELEMENT'?cfg[2]:null,max:8},outputs,{itemId:linkedAccessory,remark:`装备${cfg[5]}后激活完整联动`});
  p(`INSERT INTO app_recipe (id,name,output_item_id,output_qty,unlock_chapter_id,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(recipeId)},${q(cfg[0]+'核心配方')},${q(itemId)},1,${q('SCP_N20_'+nn)},${6000+pair},1,${q(`通关第${unlockNo}章后解锁的平民流派核心；材料需求约为同期普通装备的 8~16 倍`)},${now},${now});`);
  const amount=24+pair*7;
  [[`ITM_N20_${prev}_M3`,amount],[`ITM_N20_${nn}_M2`,amount+8],[`ITM_N20_${nn}_M3`,Math.ceil(amount*.65)]].forEach((x,j)=>p(`INSERT INTO app_recipe_material (id,recipe_id,item_id,quantity,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q(`RCM_N20_CORE_${pair}_${j+1}`)},${q(recipeId)},${q(x[0])},${x[1]},${j},${now},${now});`));
});

// 未来付费/活动核心：仅进入图鉴，不配置配方、掉落或首通奖励。
[
  ['日冕裁决器','BURN','将燃烧层数转化为爆发窗口'],
  ['深海零压枪','FREEZE','冻结受击节奏并延长防护链'],
  ['雷池神经刃','SHOCK','以高频充能驱动无限趋近的电弧'],
  ['虚空回收杖','PHYSICAL','把溢出治疗转写为追击伤害'],
  ['终端万象炮','BURN','混合元素队伍的终局共鸣核心']
].forEach((cfg,ix)=>{
  const n=ix+1, itemId=`ITM_N20_FUTURE_${String(n).padStart(2,'0')}`;
  item(itemId,`n20_future_${n}`,cfg[0],'WEAPON',7000+n,`未来限定核心｜暂无获取途径｜${cfg[2]}`,0,0,1,0,0);
  p(`INSERT INTO app_item_weapon (id,item_id,base_atk,atk_speed_up_ratio,atk_speed_down_ratio,normal_skill_id,CREATE_TIME,UPDATE_TIME) VALUES (${q('WPN_N20_FUTURE_'+n)},${q(itemId)},${110+n*18},${8+n},0,${q(NORMAL)},${now},${now});`);
  advancedPassive(`PSK_N20_FUTURE_${String(n).padStart(2,'0')}`,`${cfg[0]}·限定特性`,'DEAL_ELEMENT_DMG_RATIO','INCREASE',24+n*5,itemId,7000+n,`未来限定设计预留：${cfg[2]}`);
});

p('COMMIT; SET FOREIGN_KEY_CHECKS=1;');
p("-- 校验：CHAPTER=20，LEVEL=100，怪物=80，技能石=20。SELECT kind,COUNT(*) FROM app_stage WHERE id LIKE '%N20_%' GROUP BY kind;");
fs.writeFileSync(OUT,lines.join('\n'),'utf8');
fs.writeFileSync(STORY,story.join('\n'),'utf8');
console.log(`generated ${OUT}`); console.log(`generated ${STORY}`); console.log('chapters=20 levels=100 monsters=80 skillStones=20');
