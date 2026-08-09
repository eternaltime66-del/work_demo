(function (window) {
  function cfg() {
    return window.APP_CONFIG || {};
  }

  function apiBase() {
    return cfg().apiBase || '';
  }

  function getToken() {
    return localStorage.getItem(cfg().tokenKey || 'token') || '';
  }

  function saveToken(token) {
    if (token) localStorage.setItem(cfg().tokenKey || 'token', token);
  }

  function clearToken() {
    localStorage.removeItem(cfg().tokenKey || 'token');
  }

  function request(path, params) {
    var body = new URLSearchParams();
    Object.keys(params || {}).forEach(function (key) {
      if (params[key] !== undefined && params[key] !== null) {
        body.append(key, params[key]);
      }
    });

    return fetch(apiBase() + path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
        token: getToken()
      },
      body: body.toString()
    }).then(function (res) {
      return res.json();
    });
  }

  function requestJson(path, data) {
    return fetch(apiBase() + path, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        token: getToken()
      },
      body: JSON.stringify(data == null ? [] : data)
    }).then(function (res) {
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

  window.GameApi = {
    getToken: getToken,
    saveToken: saveToken,
    clearToken: clearToken,
    sendEmailCode: function (email) {
      return request('/api/unit/send/code', {
        account: email,
        type: cfg().codeType
      }).then(ensureSuccess);
    },
    emailRegister: function (email, emsCode, psd, psdAgain) {
      return request('/api/account/email/register', {
        email: email,
        emsCode: emsCode,
        psd: psd,
        psdAgain: psdAgain
      }).then(ensureSuccess);
    },
    emailLogin: function (email, password) {
      return request('/api/account/email/login', {
        email: email,
        password: password
      }).then(ensureSuccess);
    },
    emailLoginByCode: function (email, emsCode) {
      return request('/api/account/email/login/code', {
        email: email,
        emsCode: emsCode
      }).then(ensureSuccess);
    },
    changePassword: function (oldPassword, newPassword, newPasswordAgain) {
      return request('/api/account/password/change', {
        oldPassword: oldPassword,
        newPassword: newPassword,
        newPasswordAgain: newPasswordAgain
      }).then(ensureSuccess);
    },
    setPasswordByCode: function (emsCode, newPassword, newPasswordAgain) {
      return request('/api/account/password/set-by-code', {
        emsCode: emsCode,
        newPassword: newPassword,
        newPasswordAgain: newPasswordAgain
      }).then(ensureSuccess);
    },
    userInfo: function () {
      return request('/api/user/info', {}).then(ensureSuccess);
    },
    playerRoleList: function () {
      return request('/api/player/role/list', {}).then(ensureSuccess);
    },
    playerRoleInfo: function (id) {
      return request('/api/player/role/info', { id: id }).then(ensureSuccess);
    },
    stageTree: function () {
      return request('/api/stage/tree', {}).then(ensureSuccess);
    },
    stageStamina: function () {
      return request('/api/stage/stamina', {}).then(ensureSuccess);
    },
    stageMainProgress: function () {
      return request('/api/stage/main/progress', {}).then(ensureSuccess);
    },
    stageLevelPreview: function (levelId) {
      return request('/api/stage/level/preview', { levelId: levelId }).then(ensureSuccess);
    },
    stageTowerStatus: function () {
      return request('/api/stage/tower/status', {}).then(ensureSuccess);
    },
    stageTowerEnter: function () {
      return request('/api/stage/tower/enter', {}).then(ensureSuccess);
    },
    stageTowerFight: function () {
      return request('/api/stage/tower/fight', {}).then(ensureSuccess);
    },
    stageTypeList: function () {
      return request('/api/stage/type/list', {}).then(ensureSuccess);
    },
    stageChapterList: function (typeId) {
      return request('/api/stage/chapter/list', { typeId: typeId }).then(ensureSuccess);
    },
    stageLevelList: function (chapterId) {
      return request('/api/stage/level/list', { chapterId: chapterId }).then(ensureSuccess);
    },
    stageLevelMonsterList: function (levelId) {
      return request('/api/stage/level/monster/list', { levelId: levelId }).then(ensureSuccess);
    },
    playerLayoutList: function () {
      return request('/api/player/layout/list', {}).then(ensureSuccess);
    },
    playerLayoutSave: function (items) {
      return requestJson('/api/player/layout/save', items || []).then(ensureSuccess);
    },

    prepSummary: function () {
      return request('/api/prep/summary', {}).then(ensureSuccess);
    },
    prepTransfer: function (fromType, fromKey, toType, toKey, quantity) {
      var body = {
        fromType: fromType,
        fromKey: fromKey,
        toType: toType,
        toKey: toKey == null ? '' : toKey
      };
      if (quantity != null && quantity !== '') {
        body.quantity = quantity;
      }
      return request('/api/prep/transfer', body).then(ensureSuccess);
    },
    prepWarehouseToBag: function (slotNos) {
      return request('/api/prep/warehouse/to-bag', {
        slotNos: Array.isArray(slotNos) ? slotNos.join(',') : slotNos
      }).then(ensureSuccess);
    },
    prepBagToWarehouse: function (bagIds) {
      return request('/api/prep/bag/to-warehouse', {
        bagIds: Array.isArray(bagIds) ? bagIds.join(',') : bagIds
      }).then(ensureSuccess);
    },
    prepEquip: function (slot, itemId) {
      return request('/api/prep/equip', { slot: slot, itemId: itemId }).then(ensureSuccess);
    },
    prepUnequip: function (slot) {
      return request('/api/prep/unequip', { slot: slot }).then(ensureSuccess);
    },

    /** 怪物掉落试算（不入仓），monsterIds 可为逗号分隔 */
    monsterDropRoll: function (monsterIds) {
      return request('/api/monster/drop/roll', {
        monsterIds: Array.isArray(monsterIds) ? monsterIds.join(',') : monsterIds
      }).then(ensureSuccess);
    },
    /** 战斗演算（行动值）；胜利掉落入仓库；无尽塔请用 stageTowerFight */
    battleFight: function (levelId) {
      return request('/api/battle/fight', { levelId: levelId }).then(ensureSuccess);
    },

    craftList: function () {
      return request('/api/craft/list', {}).then(ensureSuccess);
    },
    craftDetail: function (recipeId) {
      return request('/api/craft/detail', { recipeId: recipeId }).then(ensureSuccess);
    },
    craftExecute: function (recipeId) {
      return request('/api/craft/execute', { recipeId: recipeId }).then(ensureSuccess);
    },
    craftDropSources: function (itemId) {
      return request('/api/craft/drop-sources', { itemId: itemId }).then(ensureSuccess);
    },
    itemDetail: function (itemId) {
      return request('/api/item/detail', { itemId: itemId }).then(ensureSuccess);
    },

    activeSkillDetail: function (id) {
      return request('/api/active/skill/detail', { id: id }).then(ensureSuccess);
    },
    passiveSkillDetail: function (id) {
      return request('/api/passive/skill/detail', { id: id }).then(ensureSuccess);
    },
    buffDefDetail: function (id) {
      return request('/api/buff/def/detail', { id: id }).then(ensureSuccess);
    }
  };
})(window);
