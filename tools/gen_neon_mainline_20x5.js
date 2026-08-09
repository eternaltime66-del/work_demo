#!/usr/bin/env node
'use strict';

const fs = require('fs');
const path = require('path');
const OUT = path.join(__dirname, '..', 'src', 'main', 'resources', 'sql', 'seed_neon_mainline_20x5.sql');
const STORY = path.join(__dirname, '..', 'src', 'main', 'resources', '主线', '霓虹远征·第一篇章（1-20章）.md');
const TYPE = 'STY_10000001';
const NORMAL = 'ASK_71867187';

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
const lines = [];
const p = s => lines.push(s);
const now = 'NOW()';

function item(id, code, name, type, sort, remark, chargeSlots=0, basicSlots=0) {
  p(`INSERT INTO app_item (id,code,name,item_type,max_stack,sort,enable,remark,charge_skill_slot_count,basic_passive_slot_count,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(code)},${q(name)},${q(type)},${type==='MATERIAL'?99:1},${sort},1,${q(remark)},${chargeSlots},${basicSlots},${now},${now});`);
}
function skill(id,name,type,charge,mul,target,element,sort,style) {
  const effect=style?.[1]||'DAMAGE', hits=style?.[2]||1, event=style?.[3]||'CAST';
  const match=style?.[4]||'ANY_TYPE', matchType=style?.[5]||(style?.[4]?'':'NORMAL'), flavor=style?.[6]||'章节战斗技能';
  p(`INSERT INTO app_active_skill (id,name,skill_type,skill_school,damage_element,code,need_charge_mode,need_charge,max_cast_skill,max_cast_global,max_cast_all_means,max_cast_role,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(type)},'脉冲',${q(element)},${q(id)},'MANUAL',${charge},0,0,0,0,${sort},1,${q(flavor)},${now},${now});`);
  p(`INSERT INTO app_skill_effect (id,skill_id,name,target_type,effect_type,formula_json,hit_segments,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SEF_'+id)},${q(id)},${q(name)},${q(target)},${q(effect)},${q(formula(mul))},${hits},0,${q(flavor)},${now},${now});`);
  p(`INSERT INTO app_skill_charge (id,skill_id,name,condition_type,scope,charge_gain,skill_charge_event,skill_charge_match,match_skill_type,match_damage_element,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('SCH_'+id)},${q(id)},${q(event==='CAST'?'释放充能':event==='TAKE_DAMAGE'?'受击充能':event==='DEAL_DAMAGE'?'伤害充能':'击杀充能')},'SKILL_CHARGE','GLOBAL',1,${q(event)},${q(match)},${matchType?q(matchType):'NULL'},${match==='ANY_ELEMENT'?q(element):'NULL'},0,${q(flavor)},${now},${now});`);
}
function passive(id,name,key,value,itemId,sort,remark) {
  p(`INSERT INTO app_passive_skill (id,name,code,passive_type,condition_mode,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(id)},${q(name)},${q(id)},'OUT_BASIC','UNLIMITED',${sort},1,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_passive_effect (id,skill_id,attr_key,attr_dir,value_num,sort,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q('PSE_'+id)},${q(id)},${q(key)},'INCREASE',${value},0,${q(remark)},${now},${now});`);
  p(`INSERT INTO app_item_default_passive (id,item_id,passive_skill_id,passive_type,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES (${q('IDP_'+id)},${q(itemId)},${q(id)},'OUT_BASIC',1,0,${now},${now});`);
}

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
p("DELETE FROM app_item_default_skill WHERE id LIKE 'IDS_N20_%'; DELETE FROM app_item_default_passive WHERE id LIKE 'IDP_N20_%'; DELETE FROM app_item_weapon WHERE id LIKE 'WPN_N20_%'; DELETE FROM app_item_armor WHERE id LIKE 'ARM_N20_%'; DELETE FROM app_item_gloves WHERE id LIKE 'GLO_N20_%'; DELETE FROM app_item_helmet WHERE id LIKE 'HEL_N20_%'; DELETE FROM app_item_legs WHERE id LIKE 'LEG_N20_%'; DELETE FROM app_item_accessory WHERE id LIKE 'ACC_N20_%'; DELETE FROM app_item_material WHERE id LIKE 'MAT_N20_%';");
p("DELETE FROM app_skill_charge WHERE skill_id LIKE 'ASK_N20_%'; DELETE FROM app_skill_effect WHERE skill_id LIKE 'ASK_N20_%'; DELETE FROM app_active_skill WHERE id LIKE 'ASK_N20_%';");
p("DELETE FROM app_passive_effect WHERE skill_id LIKE 'PSK_N20_%'; DELETE FROM app_passive_skill WHERE id LIKE 'PSK_N20_%';");
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
  skill(small,`${zone}突击`,'SMALL',Math.min(8+Math.floor(no/3),14),1.15+no*.025,'FIRST',element,no*3);
  skill(ult,`${name}过载`,'ULTIMATE',Math.min(18+Math.floor(no/2),28),1.7+no*.045,'ALL_ENEMY',element,no*3+1);
  skill(player,skillName,'SMALL',Math.min(5+Math.floor(no/4),10),stoneStyle[1]==='HEAL'?0.75+no*.025:1.25+no*.035,stoneStyle[0],element,no*3+2,stoneStyle);
  mats.forEach((m,i)=>{ item(matIds[i],`n20_${nn}_m${i+1}`,m,'MATERIAL',no*100+i,`${zone}材料`); p(`INSERT INTO app_item_material (id,item_id,grade,CREATE_TIME,UPDATE_TIME) VALUES ('MAT_N20_${nn}_${i+1}',${q(matIds[i])},${1+Math.floor(ix/4)},${now},${now});`); });
  const glovesName=`${zone}触控护手`, helmetName=`${zone}感应头盔`, legsName=`${zone}稳定护腿`;
  item(weapon,`n20_${nn}_weapon`,weaponName,'WEAPON',no*100+10,`附带充能技【${zone}突击】`,1);
  item(armor,`n20_${nn}_armor`,armorName,'ARMOR',no*100+11,`附带生存被动`,0,1);
  item(gloves,`n20_${nn}_gloves`,glovesName,'GLOVES',no*100+12,`附带输出被动`,0,1);
  item(helmet,`n20_${nn}_helmet`,helmetName,'HELMET',no*100+13,`附带防护被动`,0,1);
  item(legs,`n20_${nn}_legs`,legsName,'LEGS',no*100+14,`附带稳定被动`,0,1);
  item(accessory,`n20_${nn}_accessory`,`${name}信标`,'ACCESSORY',no*100+15,`附带章节信标被动`,0,1);
  item(stone,`n20_${nn}_stone`,`${skillName}技能石`,'SKILL_STONE',no*100+16,`装入技能槽后获得【${skillName}】：${stoneStyle[6]}`,1);
  p(`INSERT INTO app_item_weapon (id,item_id,base_atk,atk_speed_up_ratio,atk_speed_down_ratio,normal_skill_id,CREATE_TIME,UPDATE_TIME) VALUES ('WPN_N20_${nn}',${q(weapon)},${4+no*3},${Math.min(2+no*.2,6).toFixed(1)},0,${q(NORMAL)},${now},${now});`);
  p(`INSERT INTO app_item_armor (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,CREATE_TIME,UPDATE_TIME) VALUES ('ARM_N20_${nn}',${q(armor)},${25+no*18},${1+Math.floor(no*.8)},0,0,${now},${now});`);
  p(`INSERT INTO app_item_gloves (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('GLO_N20_${nn}',${q(gloves)},${8+no*5},${Math.floor(no*.25)},${Math.min(1+no*.15,4).toFixed(2)},0,'章节输出护手',${now},${now});`);
  p(`INSERT INTO app_item_helmet (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('HEL_N20_${nn}',${q(helmet)},${12+no*7},${1+Math.floor(no*.55)},0,0,'章节防护头盔',${now},${now});`);
  p(`INSERT INTO app_item_legs (id,item_id,hp,defense,atk_speed_up_ratio,atk_speed_down_ratio,remark,CREATE_TIME,UPDATE_TIME) VALUES ('LEG_N20_${nn}',${q(legs)},${16+no*9},${1+Math.floor(no*.4)},0,0,'章节稳定护腿',${now},${now});`);
  p(`INSERT INTO app_item_accessory (id,item_id,remark,CREATE_TIME,UPDATE_TIME) VALUES ('ACC_N20_${nn}_R',${q(accessory)},'章节信标',${now},${now}),('ACC_N20_${nn}_S',${q(stone)},'技能石扩展复用饰品结构',${now},${now});`);
  p(`INSERT INTO app_item_default_skill (id,item_id,skill_id,slot_no,sort,CREATE_TIME,UPDATE_TIME) VALUES ('IDS_N20_${nn}_W',${q(weapon)},${q(small)},1,0,${now},${now}),('IDS_N20_${nn}_S',${q(stone)},${q(player)},1,0,${now},${now});`);
  passive(`PSK_N20_${nn}_A`,`${name}装甲共生`,'MAX_HP',12+no*5,armor,no*10,`${armorName}将章节材料转化为生命容量`);
  passive(`PSK_N20_${nn}_G`,`${name}触控增幅`,'ATK',2+Math.floor(no*.7),gloves,no*10+1,`${glovesName}强化义体输出`);
  passive(`PSK_N20_${nn}_H`,`${name}感应防壁`,'DEF',1+Math.floor(no*.45),helmet,no*10+2,`${helmetName}提前解析来袭信号`);
  passive(`PSK_N20_${nn}_L`,`${name}步态稳定`,'MAX_HP',8+no*4,legs,no*10+3,`${legsName}维持战斗姿态`);
  passive(`PSK_N20_${nn}_R`,`${name}信标同步`,no%2?'ATK':'DEF',1+Math.floor(no*.55),accessory,no*10+4,`${name}信标与主角义体同步`);
  const outs=[weapon,armor,gloves,helmet,legs,accessory,stone];
  const outNames=[weaponName,armorName,glovesName,helmetName,legsName,`${name}信标`,`${skillName}技能石`];
  outs.forEach((out,i)=>{ const rid=`RCP_N20_${nn}_${i+1}`; p(`INSERT INTO app_recipe (id,name,output_item_id,output_qty,unlock_chapter_id,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(rid)},${q(outNames[i]+'配方')},${q(out)},1,${q(chapterId)},${no*10+i},1,'随主线章节解锁',${now},${now});`); [[matIds[i%3],2+Math.floor(no/5)],[matIds[(i+1)%3],1+(i===6?1:0)]].forEach((x,j)=>p(`INSERT INTO app_recipe_material (id,recipe_id,item_id,quantity,sort,CREATE_TIME,UPDATE_TIME) VALUES ('RCM_N20_${nn}_${i+1}_${j+1}',${q(rid)},${q(x[0])},${x[1]},${j},${now},${now});`)); });
  const monsterIds=mons.map((m,i)=>`MST_N20_${nn}_${i+1}`);
  mons.forEach((m,i)=>{ const rarity=i===3?'BOSS':i===2?'RARE':'NORMAL'; const mult=i===3?4.2:i===2?1.7:1; const hp=Math.round((35+no*25)*mult), atk=Math.round((5+no*2.2)*mult), def=Math.round(no*.75*mult), action=i===0?75:i===1?85:i===2?95:110; const size=rarity==='BOSS'?[2,4]:rarity==='RARE'?[1,2]:[1,1]; p(`INSERT INTO app_monster (id,name,rarity,role_category,grid_h,grid_w,base_atk,base_hp,base_def,base_action,sort,normal_skill_id,small_skill_id,ultimate_skill_id,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(monsterIds[i])},${q(m)},${q(rarity)},'MONSTER',${size[0]},${size[1]},${atk},${hp},${def},${action},${no*10+i},NULL,${i>=2?q(small):'NULL'},${i===3?q(ult):'NULL'},${q(zone+'生态单位')},${now},${now});`); const drops=i===3?[[matIds[2],100,1,2],[stone,12,1,1]]:i===2?[[matIds[1],75,1,2],[matIds[2],35,1,1]]:[[matIds[0],85,1,2],[matIds[1],30,1,1]]; drops.forEach((d,j)=>p(`INSERT INTO app_monster_drop (id,monster_id,item_id,drop_rate,min_qty,max_qty,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES ('MDP_N20_${nn}_${i+1}_${j+1}',${q(monsterIds[i])},${q(d[0])},${d[1]},${d[2]},${d[3]},${j},1,'章节生态掉落',${now},${now});`)); });
  p(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,CREATE_TIME,UPDATE_TIME) VALUES (${q(chapterId)},${q(TYPE)},'CHAPTER',${q(name)},${q('N20-'+nn)},${no},1,${q(intro)},${now},${now});`);
  levels.forEach((ln,li)=>{ const lv=li+1, lid=`SLV_N20_${nn}_${lv}`; const stamina=Math.min(1+Math.floor(ix/4),5); p(`INSERT INTO app_stage (id,parent_id,kind,name,code,sort,enable,remark,stamina_cost,CREATE_TIME,UPDATE_TIME) VALUES (${q(lid)},${q(chapterId)},'LEVEL',${q(ln)},${q(`N20-${nn}-${lv}`)},${lv},1,${q(`${intro}｜节点：${ln}`)},${stamina},${now},${now});`); const specs=lv===1?[0]:lv===2?[0,1]:lv===3?[0,1,1]:lv===4?[1,2]:[3]; const spots=[[2,4],[0,4],[4,4],[1,3]]; specs.forEach((mi,si)=>{ const sp=spots[si]; p(`INSERT INTO app_stage_level_monster (id,level_id,monster_id,pos_col,pos_row,sort,CREATE_TIME,UPDATE_TIME) VALUES ('SLM_N20_${nn}_${lv}_${si+1}',${q(lid)},${q(monsterIds[mi])},${sp[0]},${sp[1]},${si},${now},${now});`); }); if(lv===5) p(`INSERT INTO app_stage_first_reward (id,stage_id,item_id,qty,sort,CREATE_TIME,UPDATE_TIME) VALUES ('SFR_N20_${nn}',${q(lid)},${q(matIds[2])},1,0,${now},${now});`); });
  story.push(`## 第${no}章　${name}`,'',intro,'',...levels.map((x,i)=>`- ${no}-${i+1} ${x}：${i===0?'进入并辨认区域规则':i===1?'遭遇基础生态与资源':i===2?'发现异常线索进一步扩大':i===3?'突破精英封锁并取得关键材料':'击败守关单位，获得前往下一章的坐标'}`),'',`怪物：${mons.join('、')}。材料：${mats.join('、')}。装备：${weaponName}、${armorName}、${glovesName}、${helmetName}、${legsName}、${name}信标。技能石【${skillName}】：${stoneStyle[6]}。`,'');
});

p('COMMIT; SET FOREIGN_KEY_CHECKS=1;');
p("-- 校验：CHAPTER=20，LEVEL=100，怪物=80，技能石=20。SELECT kind,COUNT(*) FROM app_stage WHERE id LIKE '%N20_%' GROUP BY kind;");
fs.writeFileSync(OUT,lines.join('\n'),'utf8');
fs.writeFileSync(STORY,story.join('\n'),'utf8');
console.log(`generated ${OUT}`); console.log(`generated ${STORY}`); console.log('chapters=20 levels=100 monsters=80 skillStones=20');
