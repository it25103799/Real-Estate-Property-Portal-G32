/**
 * NESTIQ Real Estate - Professional Page Transition System
 * Implements smooth fade/slide animations for all page navigations
 * Does NOT interfere with CRUD operations or form submissions
 */

(function() {
    'use strict';

    // Configuration
    const TRANSITION_DURATION = 300; // milliseconds
    const TRANSITION_EASING = 'cubic-bezier(0.4, 0, 0.2, 1)';

    // State tracking
    let isTransitioning = false;
    let pendingNavigation = null;

    /**
     * Initialize the transition system
     */
    function init() {
        console.log('🎬 Page Transition System initialized');

        // Add transition overlay to body
        createTransitionOverlay();

        // Intercept navigation methods
        interceptNavigation();

        // Listen for page load completion
        window.addEventListener('load', handlePageLoad);

        // Handle browser back/forward
        window.addEventListener('popstate', handlePopState);
    }

    /**
     * Create the transition overlay element
     */
    function createTransitionOverlay() {
        const overlay = document.createElement('div');
        overlay.id = 'page-transition-overlay';
        overlay.style.cssText = `
            position: fixed;
            top: 0;
            left: 0;
            width: 100%;
            height: 100%;
            background: linear-gradient(135deg, #ffffff 0%, #f7f8fa 100%);
            z-index: 99999;
            pointer-events: none;
            opacity: 0;
            visibility: hidden;
            transition: opacity ${TRANSITION_DURATION}ms ${TRANSITION_EASING},
                        visibility ${TRANSITION_DURATION}ms ${TRANSITION_EASING};
        `;

        // Dark mode support
        if (document.documentElement.getAttribute('data-theme') === 'dark') {
            overlay.style.background = 'linear-gradient(135deg, #0f1117 0%, #1a1d27 100%)';
        }

        // Add loading indicator
        const loader = document.createElement('div');
        loader.className = 'transition-loader';
        loader.style.cssText = `
            position: absolute;
            top: 50%;
            left: 50%;
            transform: translate(-50%, -50%);
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 20px;
        `;

        // Spinner
        const spinner = document.createElement('div');
        spinner.style.cssText = `
            width: 50px;
            height: 50px;
            border: 3px solid rgba(26, 86, 219, 0.2);
            border-top-color: #1a56db;
            border-radius: 50%;
            animation: spin 0.8s linear infinite;
        `;

        // Loading text
        const text = document.createElement('div');
        text.textContent = 'Loading...';
        text.style.cssText = `
            font-family: 'Outfit', sans-serif;
            font-size: 14px;
            font-weight: 500;
            color: #5a5f70;
            letter-spacing: 0.5px;
        `;

        // Add keyframes for spinner
        const style = document.createElement('style');
        style.textContent = `
            @keyframes spin {
                to { transform: rotate(360deg); }
            }
            @keyframes fadeIn {
                from { opacity: 0; }
                to { opacity: 1; }
            }
            @keyframes fadeOut {
                from { opacity: 1; }
                to { opacity: 0; }
            }
        `;
        document.head.appendChild(style);

        loader.appendChild(spinner);
        loader.appendChild(text);
        overlay.appendChild(loader);
        document.body.appendChild(overlay);
    }

    /**
     * Intercept all navigation methods
     */
    function interceptNavigation() {
        // Store original functions
        const originalLocationAssign = window.location.assign;
        const originalLocationReplace = window.location.replace;

        // Override location.assign
        window.location.assign = function(url) {
            if (!isFormSubmission() && !isCRUDOperation(url)) {
                performTransition(() => {
                    originalLocationAssign.call(window.location, url);
                });
            } else {
                originalLocationAssign.call(window.location, url);
            }
        };

        // Override location.replace
        window.location.replace = function(url) {
            if (!isFormSubmission() && !isCRUDOperation(url)) {
                performTransition(() => {
                    originalLocationReplace.call(window.location, url);
                });
            } else {
                originalLocationReplace.call(window.location, url);
            }
        };

        // Intercept anchor clicks
        document.addEventListener('click', function(e) {
            const link = e.target.closest('a[href]');
            if (!link) return;

            const href = link.getAttribute('href');
            
            // Skip if it's a form submission, CRUD operation, or special link
            if (isFormSubmission() || 
                isCRUDOperation(href) ||
                href.startsWith('#') ||
                href.startsWith('javascript:') ||
                href.startsWith('mailto:') ||
                href.startsWith('tel:') ||
                link.hasAttribute('download') ||
                link.getAttribute('target') === '_blank') {
                return;
            }

            // Only intercept same-origin links
            const linkUrl = new URL(href, window.location.origin);
            if (linkUrl.origin !== window.location.origin) {
                return;
            }

            e.preventDefault();
            performTransition(() => {
                window.location.href = href;
            });
        }, true);

        // Intercept form submissions that redirect (not AJAX/CRUD)
        document.addEventListener('submit', function(e) {
            const form = e.target;
            if (!form || !form.action) return;

            // Let all form submissions proceed normally
            // This ensures CRUD operations are not affected
        }, true);
    }

    /**
     * Check if current action is a form submission
     */
    function isFormSubmission() {
        return document.activeElement && 
               (document.activeElement.tagName === 'BUTTON' || 
                document.activeElement.tagName === 'INPUT') &&
               document.activeElement.type === 'submit';
    }

    /**
     * Check if URL is a CRUD operation
     */
    function isCRUDOperation(url) {
        if (!url) return false;
        
        const crudPatterns = [
            /\/(add|create|insert|new)/i,
            /\/(update|edit|modify|change)/i,
            /\/(delete|remove|destroy)/i,
            /\/(save|store|submit)/i,
            /Servlet/i,
            /\?action=/i,
            /method=post/i
        ];

        return crudPatterns.some(pattern => pattern.test(url));
    }

    /**
     * Perform the page transition animation
     */
    function performTransition(callback) {
        if (isTransitioning) {
            pendingNavigation = callback;
            return;
        }

        isTransitioning = true;
        const overlay = document.getElementById('page-transition-overlay');

        if (!overlay) {
            callback();
            return;
        }

        // Show overlay with fade in
        overlay.style.visibility = 'visible';
        overlay.style.opacity = '1';
        overlay.style.pointerEvents = 'auto';

        // Wait for fade in, then navigate
        setTimeout(() => {
            callback();
        }, TRANSITION_DURATION * 0.6);
    }

    /**
     * Handle page load completion
     */
    function handlePageLoad() {
        const overlay = document.getElementById('page-transition-overlay');
        if (!overlay) return;

        // Fade out overlay
        setTimeout(() => {
            overlay.style.opacity = '0';
            overlay.style.pointerEvents = 'none';

            setTimeout(() => {
                overlay.style.visibility = 'hidden';
                isTransitioning = false;

                // Handle pending navigation
                if (pendingNavigation) {
                    const pending = pendingNavigation;
                    pendingNavigation = null;
                    pending();
                }
            }, TRANSITION_DURATION);
        }, 100);
    }

    /**
     * Handle browser back/forward buttons
     */
    function handlePopState() {
        const overlay = document.getElementById('page-transition-overlay');
        if (overlay && !isTransitioning) {
            overlay.style.visibility = 'visible';
            overlay.style.opacity = '1';
            overlay.style.pointerEvents = 'auto';

            setTimeout(() => {
                overlay.style.opacity = '0';
                overlay.style.pointerEvents = 'none';
                setTimeout(() => {
                    overlay.style.visibility = 'hidden';
                }, TRANSITION_DURATION);
            }, 50);
        }
    }

    /**
     * Public API: Manually trigger transition for programmatic navigation
     */
    window.navigateWithTransition = function(url) {
        if (isCRUDOperation(url)) {
            window.location.href = url;
        } else {
            performTransition(() => {
                window.location.href = url;
            });
        }
    };

    /**
     * Public API: Check if transition system is active
     */
    window.isTransitionActive = function() {
        return isTransitioning;
    };

    // Initialize when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Export for module systems
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = {
            navigateWithTransition: window.navigateWithTransition,
            isTransitionActive: window.isTransitionActive
        };
    }

})();
