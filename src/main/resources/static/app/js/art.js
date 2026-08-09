/**
 * 素材位降级：探测 art/{path}，存在则挂到 .art-slot，否则保留 emoji/CSS 占位。
 */
(function (global) {
  function resolveRoot() {
    if (global.APP_CONFIG && global.APP_CONFIG.artBase) {
      return global.APP_CONFIG.artBase;
    }
    var path = location.pathname || '/';
    var i = path.lastIndexOf('/');
    var dir = i >= 0 ? path.slice(0, i + 1) : './';
    return dir + 'art/';
  }

  var ROOT = resolveRoot();
  var cache = Object.create(null);

  function probe(path) {
    if (!path) return Promise.resolve(null);
    if (Object.prototype.hasOwnProperty.call(cache, path)) {
      return Promise.resolve(cache[path]);
    }
    return new Promise(function (resolve) {
      var img = new Image();
      img.onload = function () {
        cache[path] = ROOT + path;
        resolve(cache[path]);
      };
      img.onerror = function () {
        cache[path] = null;
        resolve(null);
      };
      img.src = ROOT + path;
    });
  }

  function apply(root) {
    ROOT = resolveRoot();
    var scope = root && root.querySelectorAll ? root : document;
    var nodes = scope.querySelectorAll('[data-art]');
    for (var i = 0; i < nodes.length; i++) {
      (function (el) {
        var path = el.getAttribute('data-art');
        if (!path || el.getAttribute('data-art-applied') === path) return;
        probe(path).then(function (url) {
          if (el.getAttribute('data-art') !== path) return;
          el.setAttribute('data-art-applied', path);
          if (url) {
            el.classList.add('art-ready');
            el.classList.remove('art-fallback');
            if (el.tagName === 'IMG') {
              el.src = url;
            } else {
              el.style.backgroundImage = 'url("' + url + '")';
            }
          } else {
            el.classList.add('art-fallback');
            el.classList.remove('art-ready');
          }
        });
      })(nodes[i]);
    }
  }

  global.ArtSlots = { root: ROOT, probe: probe, apply: apply };
})(typeof window !== 'undefined' ? window : this);
