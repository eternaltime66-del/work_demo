(function (window) {
  var cfg = window.APP_CONFIG || {};
  var ADMIN_TOKEN_KEY = cfg.adminTokenKey || 'adminToken';

  function getAdminToken() {
    return localStorage.getItem(ADMIN_TOKEN_KEY) || '';
  }

  function saveAdminToken(token) {
    if (token) localStorage.setItem(ADMIN_TOKEN_KEY, token);
  }

  function clearAdminToken() {
    localStorage.removeItem(ADMIN_TOKEN_KEY);
  }

  function request(path, options) {
    options = options || {};
    var headers = Object.assign({
      token: getAdminToken()
    }, options.headers || {});

    var init = { method: options.method || 'POST', headers: headers };
    if (options.json) {
      headers['Content-Type'] = 'application/json';
      init.body = JSON.stringify(options.body || {});
    } else {
      headers['Content-Type'] = 'application/x-www-form-urlencoded';
      init.body = new URLSearchParams(options.body || {}).toString();
    }

    return fetch((cfg.apiBase || '') + path, init).then(function (res) {
      return res.json();
    });
  }

  function ensureSuccess(result) {
    if (!result || result.success === false) {
      var err = new Error((result && result.msg) || '请求失败');
      err.result = result;
      throw err;
    }
    return result;
  }

  window.AdminApi = {
    getAdminToken: getAdminToken,
    saveAdminToken: saveAdminToken,
    clearAdminToken: clearAdminToken,
    sendEmailCode: function (email) {
      return request('/api/unit/send/code', {
        body: { account: email, type: cfg.codeType }
      }).then(ensureSuccess);
    },
    login: function (account, password) {
      return request('/back/login', {
        body: { account: account, password: password }
      }).then(ensureSuccess);
    },
    register: function (email, emsCode, psd, psdAgain) {
      return request('/back/register', {
        body: { email: email, emsCode: emsCode, psd: psd, psdAgain: psdAgain }
      }).then(ensureSuccess);
    },

    memberList: function (body) {
      return request('/back/member/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    memberGift: function (uid, itemId, quantity) {
      return request('/back/member/gift', {
        body: { uid: uid, itemId: itemId, quantity: quantity }
      }).then(ensureSuccess);
    },

    roleBaseStatList: function (body) {
      return request('/back/role/base/stat/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    roleBaseStatUpdate: function (body) {
      return request('/back/role/base/stat/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    roleBaseStatRemove: function (id) {
      return request('/back/role/base/stat/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    playerRoleList: function (body) {
      return request('/back/player/role/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    playerRoleGrant: function (uid, baseStatId) {
      return request('/back/player/role/grant', { body: { uid: uid, baseStatId: baseStatId } }).then(ensureSuccess);
    },
    playerRoleUpdate: function (body) {
      return request('/back/player/role/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    playerRoleRemove: function (id) {
      return request('/back/player/role/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    monsterList: function (body) {
      return request('/back/monster/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    monsterUpdate: function (body) {
      return request('/back/monster/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    monsterRemove: function (id) {
      return request('/back/monster/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    stageTree: function () {
      return request('/back/stage/tree', { body: {} }).then(ensureSuccess);
    },
    stageTypeUpdate: function (body) {
      return request('/back/stage/type/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    stageTypeRemove: function (id) {
      return request('/back/stage/type/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    stageChapterUpdate: function (body) {
      return request('/back/stage/chapter/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    stageChapterRemove: function (id) {
      return request('/back/stage/chapter/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    stageLevelUpdate: function (body) {
      return request('/back/stage/level/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    stageLevelRemove: function (id) {
      return request('/back/stage/level/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    stageLevelMonsterList: function (levelId) {
      return request('/back/stage/level/monster/list', { body: { levelId: levelId } }).then(ensureSuccess);
    },
    stageLevelMonsterUpdate: function (body) {
      return request('/back/stage/level/monster/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    stageLevelMonsterAdd: function (levelId, monsterId) {
      return request('/back/stage/level/monster/add', {
        body: { levelId: levelId, monsterId: monsterId }
      }).then(ensureSuccess);
    },
    stageLevelMonsterRemove: function (id) {
      return request('/back/stage/level/monster/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    itemList: function (body) {
      return request('/back/item/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    itemUpdate: function (body) {
      return request('/back/item/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    itemRemove: function (id) {
      return request('/back/item/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    itemExtList: function (type, body) {
      var pathMap = {
        MATERIAL: '/back/item/material/list',
        WEAPON: '/back/item/weapon/list',
        ARMOR: '/back/item/armor/list',
        GLOVES: '/back/item/gloves/list',
        HELMET: '/back/item/helmet/list',
        ACCESSORY: '/back/item/accessory/list',
        LEGS: '/back/item/legs/list'
      };
      return request(pathMap[type], { json: true, body: body || {} }).then(ensureSuccess);
    },
    itemExtUpdate: function (type, body) {
      var pathMap = {
        MATERIAL: '/back/item/material/update',
        WEAPON: '/back/item/weapon/update',
        ARMOR: '/back/item/armor/update',
        GLOVES: '/back/item/gloves/update',
        HELMET: '/back/item/helmet/update',
        ACCESSORY: '/back/item/accessory/update',
        LEGS: '/back/item/legs/update'
      };
      return request(pathMap[type], { json: true, body: body || {} }).then(ensureSuccess);
    },
    itemDetail: function (id) {
      return request('/back/item/detail', { body: { id: id } }).then(ensureSuccess);
    },
    itemDefaultSkillList: function (itemId) {
      return request('/back/item/default/skill/list', { body: { itemId: itemId } }).then(ensureSuccess);
    },
    itemDefaultSkillSave: function (itemId, skills) {
      return request('/back/item/default/skill/save', {
        json: true,
        body: { itemId: itemId, skills: skills || [] }
      }).then(ensureSuccess);
    },
    itemDefaultPassiveList: function (itemId, passiveType) {
      return request('/back/item/default/passive/list', {
        json: true,
        body: { itemId: itemId, passiveType: passiveType }
      }).then(ensureSuccess);
    },
    itemDefaultPassiveSave: function (itemId, passiveType, passives) {
      return request('/back/item/default/passive/save', {
        json: true,
        body: { itemId: itemId, passiveType: passiveType, passives: passives || [] }
      }).then(ensureSuccess);
    },

    recipeList: function (body) {
      return request('/back/recipe/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    recipeDetail: function (id) {
      return request('/back/recipe/detail', { body: { id: id } }).then(ensureSuccess);
    },
    recipeUpdate: function (body) {
      return request('/back/recipe/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    recipeRemove: function (id) {
      return request('/back/recipe/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    recipeMaterialList: function (recipeId) {
      return request('/back/recipe/material/list', { body: { recipeId: recipeId } }).then(ensureSuccess);
    },
    recipeMaterialUpdate: function (body) {
      return request('/back/recipe/material/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    recipeMaterialRemove: function (id) {
      return request('/back/recipe/material/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    monsterDropList: function (body) {
      return request('/back/monster/drop/list', { json: true, body: body || {} }).then(ensureSuccess);
    },
    monsterDropListByMonster: function (monsterId) {
      return request('/back/monster/drop/listByMonster', { body: { monsterId: monsterId } }).then(ensureSuccess);
    },
    monsterDropUpdate: function (body) {
      return request('/back/monster/drop/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    monsterDropRemove: function (id) {
      return request('/back/monster/drop/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    activeSkillList: function (body, page) {
      var qs = '';
      if (page && (page.size || page.current)) {
        qs = '?current=' + (page.current || 1) + '&size=' + (page.size || 20);
      }
      return request('/back/active/skill/list' + qs, { json: true, body: body || {} }).then(ensureSuccess);
    },
    activeSkillUpdate: function (body) {
      return request('/back/active/skill/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    activeSkillRemove: function (id) {
      return request('/back/active/skill/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    activeSkillDetail: function (id) {
      return request('/back/active/skill/detail', { body: { id: id } }).then(ensureSuccess);
    },
    skillChargeList: function (skillId) {
      return request('/back/active/skill/charge/list', { body: { skillId: skillId } }).then(ensureSuccess);
    },
    skillChargeUpdate: function (body) {
      return request('/back/active/skill/charge/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    skillChargeRemove: function (id) {
      return request('/back/active/skill/charge/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },
    skillEffectList: function (skillId) {
      return request('/back/active/skill/effect/list', { body: { skillId: skillId } }).then(ensureSuccess);
    },
    skillEffectUpdate: function (body) {
      return request('/back/active/skill/effect/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    skillEffectRemove: function (id) {
      return request('/back/active/skill/effect/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    },

    passiveSkillList: function (body, page) {
      var qs = '';
      if (page && (page.size || page.current)) {
        qs = '?current=' + (page.current || 1) + '&size=' + (page.size || 20);
      }
      return request('/back/passive/skill/list' + qs, { json: true, body: body || {} }).then(ensureSuccess);
    },
    passiveSkillDetail: function (id) {
      return request('/back/passive/skill/detail', { body: { id: id } }).then(ensureSuccess);
    },
    passiveSkillUpdate: function (body) {
      return request('/back/passive/skill/update', { json: true, body: body || {} }).then(ensureSuccess);
    },
    passiveSkillRemove: function (id) {
      return request('/back/passive/skill/remove', { json: true, body: { id: id } }).then(ensureSuccess);
    }
  };
})(window);
