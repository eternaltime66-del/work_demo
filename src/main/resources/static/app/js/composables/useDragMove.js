/**
 * Pointer-based drag (mobile + desktop). Replaces HTML5 DnD for touch screens.
 *
 * useDragMove({
 *   onPick(payload, e),
 *   onHover(payload, dropEl, e),
 *   onDrop(payload, dropEl, e),
 *   onClick(payload, e),  // fired when not dragged
 *   dropSelector,         // default [data-drop]
 *   threshold,            // px, default 6
 *   holdMs                // ms, default 200
 * })
 */
(function (global) {
  function useDragMove(opts) {
    opts = opts || {};
    var threshold = opts.threshold != null ? opts.threshold : 6;
    var holdMs = opts.holdMs != null ? opts.holdMs : 200;
    var dropSelector = opts.dropSelector || '[data-drop]';
    var session = null;

    function findDrop(x, y) {
      var el = document.elementFromPoint(x, y);
      return el ? el.closest(dropSelector) : null;
    }

    function cleanup() {
      if (!session) return;
      document.removeEventListener('pointermove', onMove);
      document.removeEventListener('pointerup', onUp);
      document.removeEventListener('pointercancel', onUp);
      session = null;
    }

    function onMove(e) {
      if (!session || e.pointerId !== session.pointerId) return;
      var dx = e.clientX - session.x0;
      var dy = e.clientY - session.y0;
      var dist = Math.sqrt(dx * dx + dy * dy);
      var held = Date.now() - session.t0 >= holdMs;
      if (!session.dragging && (dist > threshold || held)) {
        session.dragging = true;
        if (typeof opts.onPick === 'function') opts.onPick(session.payload, e);
      }
      if (!session.dragging) return;
      e.preventDefault();
      var dropEl = findDrop(e.clientX, e.clientY);
      if (typeof opts.onHover === 'function') opts.onHover(session.payload, dropEl, e);
    }

    function onUp(e) {
      if (!session || e.pointerId !== session.pointerId) return;
      var wasDragging = session.dragging;
      var payload = session.payload;
      var dropEl = wasDragging ? findDrop(e.clientX, e.clientY) : null;
      cleanup();
      if (wasDragging) {
        if (typeof opts.onDrop === 'function') opts.onDrop(payload, dropEl, e);
      } else if (typeof opts.onClick === 'function') {
        opts.onClick(payload, e);
      }
    }

    function onPointerDown(e, payload) {
      if (e.button != null && e.button !== 0) return;
      if (session) cleanup();
      session = {
        pointerId: e.pointerId,
        x0: e.clientX,
        y0: e.clientY,
        t0: Date.now(),
        dragging: false,
        payload: payload
      };
      try {
        if (e.currentTarget && e.currentTarget.setPointerCapture) {
          e.currentTarget.setPointerCapture(e.pointerId);
        }
      } catch (err) { /* ignore */ }
      document.addEventListener('pointermove', onMove, { passive: false });
      document.addEventListener('pointerup', onUp);
      document.addEventListener('pointercancel', onUp);
    }

    return {
      onPointerDown: onPointerDown,
      isDragging: function () { return !!(session && session.dragging); },
      cancel: cleanup
    };
  }

  global.useDragMove = useDragMove;
})(typeof window !== 'undefined' ? window : this);
