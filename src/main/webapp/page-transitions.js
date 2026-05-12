/**
 * NESTIQ Real Estate — Page Transition System v2
 * ─────────────────────────────────────────────
 * Features:
 *   • Slim top progress bar (NProgress-style) on every navigation
 *   • Smooth fade + slide-up entrance animation on every page load
 *   • Zero interference with CRUD operations / form submissions
 */

(function () {
    'use strict';

    /* ── Config ─────────────────────────────────────── */
    const BAR_COLOR      = '#1a56db';   // accent blue matching the theme
    const BAR_HEIGHT     = '3px';
    const ANIM_DURATION  = 380;         // ms — entrance animation length
    const BAR_SPEED      = 260;         // ms — bar fill speed
    const SLIDE_OFFSET   = '22px';      // how far up the content slides in

    /* ── Inject CSS once ────────────────────────────── */
    (function injectStyles() {
        const style = document.createElement('style');
        style.textContent = `
      /* ── Top progress bar ── */
      #pt-bar-wrap {
        position: fixed;
        top: 0; left: 0;
        width: 100%; height: ${BAR_HEIGHT};
        z-index: 999999;
        pointer-events: none;
        overflow: hidden;
      }
      #pt-bar {
        height: 100%;
        width: 0%;
        background: ${BAR_COLOR};
        border-radius: 0 2px 2px 0;
        box-shadow: 0 0 8px ${BAR_COLOR}80;
        transition: width ${BAR_SPEED}ms cubic-bezier(0.4, 0, 0.2, 1),
                    opacity 200ms ease;
        opacity: 0;
      }

      /* ── Page entrance animation ── */
      @keyframes pt-enter {
        from {
          opacity: 0;
          transform: translateY(${SLIDE_OFFSET});
        }
        to {
          opacity: 1;
          transform: translateY(0);
        }
      }

      body.pt-entering {
        animation: pt-enter ${ANIM_DURATION}ms cubic-bezier(0.25, 0.46, 0.45, 0.94) both;
      }

      /* ── Exit fade (quick, before browser navigates) ── */
      @keyframes pt-exit {
        from { opacity: 1; }
        to   { opacity: 0; }
      }
      body.pt-exiting {
        animation: pt-exit 180ms ease forwards;
      }

      /* ── Spinner for slow navigations ── */
      #pt-spinner {
        position: fixed;
        bottom: 24px; right: 24px;
        width: 20px; height: 20px;
        border: 2px solid ${BAR_COLOR}30;
        border-top-color: ${BAR_COLOR};
        border-radius: 50%;
        z-index: 999998;
        opacity: 0;
        pointer-events: none;
        transition: opacity 200ms ease;
        animation: pt-spin 0.7s linear infinite;
      }
      #pt-spinner.visible { opacity: 1; }
      @keyframes pt-spin { to { transform: rotate(360deg); } }
    `;
        document.head.appendChild(style);
    })();

    /* ── DOM elements ───────────────────────────────── */
    let barWrap, bar, spinner;

    function createElements() {
        barWrap = document.createElement('div');
        barWrap.id = 'pt-bar-wrap';

        bar = document.createElement('div');
        bar.id = 'pt-bar';

        barWrap.appendChild(bar);
        document.body.appendChild(barWrap);

        spinner = document.createElement('div');
        spinner.id = 'pt-spinner';
        document.body.appendChild(spinner);
    }

    /* ── Progress bar helpers ───────────────────────── */
    let barTimer = null;

    function barStart() {
        bar.style.opacity = '1';
        bar.style.width = '0%';
        requestAnimationFrame(() => {
            bar.style.transitionDuration = '120ms';
            bar.style.width = '30%';
            setTimeout(() => {
                bar.style.transitionDuration = BAR_SPEED + 'ms';
                bar.style.width = '75%';
                barTimer = setTimeout(() => {
                    spinner.classList.add('visible');
                }, 400);
            }, 130);
        });
    }

    function barDone() {
        clearTimeout(barTimer);
        spinner.classList.remove('visible');
        bar.style.transitionDuration = '150ms';
        bar.style.width = '100%';
        setTimeout(() => {
            bar.style.opacity = '0';
            setTimeout(() => { bar.style.width = '0%'; }, 200);
        }, 160);
    }

    /* ── CRUD / form-submission guard ───────────────── */
    const CRUD_PATTERNS = [
        /Servlet(\?|$)/i,
        /[?&]action=/i,
        /\/(add|create|insert)\b/i,
        /\/(update|edit|modify)\b/i,
        /\/(delete|remove|destroy)\b/i,
        /\/(save|store|submit)\b/i,
        /\/(login|logout|register)\b/i,
        /\/(book|cancel|complete)\b/i,
        /\/(mark|reply|change|replace)\b/i,
    ];

    function isCRUD(url) {
        if (!url) return false;
        return CRUD_PATTERNS.some(function(p) { return p.test(url); });
    }

    function isSubmitClick(el) {
        if (!el) return false;
        var tag = el.tagName.toUpperCase();
        return (tag === 'BUTTON' || tag === 'INPUT') && el.type === 'submit';
    }

    /* ── Navigation with transition ─────────────────── */
    var navigating = false;

    function go(url) {
        if (navigating) return;
        navigating = true;

        barStart();
        document.body.classList.add('pt-exiting');

        setTimeout(function() {
            window.location.href = url;
        }, 160);
    }

    /* ── Intercept anchor clicks ────────────────────── */
    document.addEventListener('click', function (e) {
        var link = e.target.closest('a[href]');
        if (!link) return;

        var href = link.getAttribute('href');
        if (!href) return;

        if (
            href.indexOf('#') === 0 ||
            href.indexOf('javascript:') === 0 ||
            href.indexOf('mailto:') === 0 ||
            href.indexOf('tel:') === 0 ||
            link.hasAttribute('download') ||
            link.getAttribute('target') === '_blank' ||
            isCRUD(href) ||
            isSubmitClick(document.activeElement)
        ) return;

        try {
            var dest = new URL(href, window.location.href);
            if (dest.origin !== window.location.origin) return;
        } catch (_) { return; }

        e.preventDefault();
        go(href);
    }, true);

    /* ── Public helper for programmatic navigation ───── */
    window.navigateWithTransition = function (url) {
        if (isCRUD(url)) { window.location.href = url; } else { go(url); }
    };

    /* ── Page entrance on load ──────────────────────── */
    function onLoad() {
        createElements();
        barDone();

        document.body.classList.remove('pt-exiting');
        void document.body.offsetWidth; // force reflow
        document.body.classList.add('pt-entering');

        setTimeout(function() {
            document.body.classList.remove('pt-entering');
            navigating = false;
        }, ANIM_DURATION);
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', onLoad);
    } else {
        onLoad();
    }

})();
