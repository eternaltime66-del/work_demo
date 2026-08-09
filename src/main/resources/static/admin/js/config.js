const HOST = ""; // 后端地址，如 http://localhost:8081；空=同域/自动探测

/**
 * 后台管理配置：前后端分离。
 * - HOST 有值时优先用 HOST
 * - 否则：?api= / localStorage.apiBase / 自动探测
 */
(function (global) {
  function trimSlash(s) {
    return String(s || '').replace(/\/+$/, '');
  }

  function detectApiBase() {
    var host = trimSlash(typeof HOST !== 'undefined' ? HOST : '');
    if (host) return host;
    try {
      var qs = new URLSearchParams(location.search || '').get('api');
      if (qs) {
        var fromQs = trimSlash(qs);
        try { localStorage.setItem('apiBase', fromQs); } catch (e) { /* ignore */ }
        return fromQs;
      }
      var stored = localStorage.getItem('apiBase');
      if (stored) return trimSlash(stored);
      if (location.protocol === 'file:') return 'http://localhost:8081';
      if (location.port && location.port !== '8081' && location.hostname) {
        return location.protocol + '//' + location.hostname + ':8081';
      }
      return '';
    } catch (e) {
      return 'http://localhost:8081';
    }
  }

  function pageDir() {
    var path = location.pathname || '/';
    var i = path.lastIndexOf('/');
    return i >= 0 ? path.slice(0, i + 1) : './';
  }

  var dir = pageDir();
  var apiBase = detectApiBase();
  var artBase = dir + '../app/art/';

  global.APP_CONFIG = {
    apiBase: apiBase,
    artBase: artBase,
    resolveAssetUrl: function (url) {
      if (url == null || url === '') return url;
      var s = String(url);
      if (s.indexOf('http://') === 0 || s.indexOf('https://') === 0 || s.indexOf('data:') === 0) return s;
      if (s.indexOf('/art/') === 0) return artBase + s.slice('/art/'.length);
      if (s.indexOf('art/') === 0) return artBase + s.slice('art/'.length);
      if (s.charAt(0) === '/') return apiBase + s;
      return s;
    },
    tokenKey: 'token',
    adminTokenKey: 'adminToken',
    homePage: dir + '../app/index.html',
    loginPage: dir + 'index.html',
    adminPage: dir + 'index.html',
    battlePage: dir + '../app/battle.html',
    selectedLevelKey: 'selectedLevelId',
    codeType: 'AccountCheckForEmail',
    setApiBase: function (url) {
      var v = trimSlash(url);
      try { localStorage.setItem('apiBase', v); } catch (e) { /* ignore */ }
      global.APP_CONFIG.apiBase = v;
      apiBase = v;
      return v;
    }
  };
})(typeof window !== 'undefined' ? window : this);
