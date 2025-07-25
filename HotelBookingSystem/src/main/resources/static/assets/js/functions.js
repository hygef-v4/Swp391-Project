/**
 * Booking - Multipurpose Online Booking Theme
 *
 * @author StackBros (#)
 * @version 1.2.1
 **/


/* ===================
Table Of Content
======================
01 PRELOADER
02 MEGA MENU
03 STICKY HEADER
04 TINY SLIDER
05 STICKY BAR
06 TOOLTIP
07 POPOVER
08 BACK TO TOP
09 GLIGHTBOX
10 CHOICES
11 AOS ANIMATION
12 QUILL EDITOR
13 STEPPER
14 PRICING
15 STICKY ELEMENT
16 FLATPICKER
17 SPLIDE SLIDER
18 NOUISLIDER
19 DROPZONE
20 FAKE PASSWORD
21 AUTO TAB
22 GUEST SELECTOR
23 PARALLAX BACKGROUND
24 OVERLAY SCROLLBAR
25 TRAFFIC CHART
26 TRAFFIC CHART 2
27 TRAFFIC CHART 3
28 TRAFFIC CHART 4
====================== */

"use strict";
!function () {

    window.Element.prototype.removeClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.remove(className);
        }
        return this;
    }, window.Element.prototype.addClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.add(className);
        }
        return this;
    }, window.Element.prototype.toggleClass = function () {
        let className = arguments.length > 0 && void 0 !== arguments[0] ? arguments[0] : "",
            selectors = this;
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (this.isVariableDefined(selectors) && className) {
            selectors.classList.toggle(className);
        }
        return this;
    }, window.Element.prototype.isVariableDefined = function () {
        return !!this && typeof (this) != 'undefined' && this != null;
    }
}();

// Get CSS var value
var ThemeColor = function () {
    return {
        getCssVariableValue: function (e) {
            var t = getComputedStyle(document.documentElement).getPropertyValue(e);
            return t && t.length > 0 && (t = t.trim()), t;
        }
    };
}();

var e = {
    init: function () {
        e.preLoader(),
            e.megaMenu(),
            e.stickyHeader(),
            e.tinySlider(),
            e.stickyBar(),
            e.toolTipFunc(),
            e.popOverFunc(),
            e.backTotop(),
            e.lightBox(),
            e.choicesSelect(),
            e.aosFunc(),
            e.quill(),
            e.stepper(),
            e.pricing(),
            e.stickyElement(),
            e.flatPicker(),
            e.splideSlider(),
            e.rangeSlider(),
            e.dropZone(),
            e.fakePwd(),
            e.autoTabinput(),
            e.trafficstatsChart(),
            e.trafficChart(),
            e.guestSelector(),
            e.parallaxBG(),
            e.overlayScrollbars(),
            e.trafficsplineChart(),
            e.trafficroomChart();

    },
    isVariableDefined: function (el) {
        return typeof !!el && (el) != 'undefined' && el != null;
    },
    getParents: function (el, selector, filter) {
        const result = [];
        const matchesSelector = el.matches || el.webkitMatchesSelector || el.mozMatchesSelector || el.msMatchesSelector;

        // match start from parent
        el = el.parentElement;
        while (el && !matchesSelector.call(el, selector)) {
            if (!filter) {
                if (selector) {
                    if (matchesSelector.call(el, selector)) {
                        return result.push(el);
                    }
                } else {
                    result.push(el);
                }
            } else {
                if (matchesSelector.call(el, filter)) {
                    result.push(el);
                }
            }
            el = el.parentElement;
            if (e.isVariableDefined(el)) {
                if (matchesSelector.call(el, selector)) {
                    return el;
                }
            }

        }
        return result;
    },
    getNextSiblings: function (el, selector, filter) {
        let sibs = [];
        let nextElem = el.parentNode.firstChild;
        const matchesSelector = el.matches || el.webkitMatchesSelector || el.mozMatchesSelector || el.msMatchesSelector;
        do {
            if (nextElem.nodeType === 3) continue; // ignore text nodes
            if (nextElem === el) continue; // ignore elem of target
            if (nextElem === el.nextElementSibling) {
                if ((!filter || filter(el))) {
                    if (selector) {
                        if (matchesSelector.call(nextElem, selector)) {
                            return nextElem;
                        }
                    } else {
                        sibs.push(nextElem);
                    }
                    el = nextElem;

                }
            }
        } while (nextElem = nextElem.nextSibling)
        return sibs;
    },
    on: function (selectors, type, listener) {
        document.addEventListener("DOMContentLoaded", () => {
            if (!(selectors instanceof HTMLElement) && selectors !== null) {
                selectors = document.querySelector(selectors);
            }
            selectors.addEventListener(type, listener);
        });
    },
    onAll: function (selectors, type, listener) {
        document.addEventListener("DOMContentLoaded", () => {
            document.querySelectorAll(selectors).forEach((element) => {
                if (type.indexOf(',') > -1) {
                    let types = type.split(',');
                    types.forEach((type) => {
                        element.addEventListener(type, listener);
                    });
                } else {
                    element.addEventListener(type, listener);
                }


            });
        });
    },
    removeClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.removeClass(className);
        }
    },
    removeAllClass: function (selectors, className) {
        if (e.isVariableDefined(selectors) && (selectors instanceof HTMLElement)) {
            document.querySelectorAll(selectors).forEach((element) => {
                element.removeClass(className);
            });
        }

    },
    toggleClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.toggleClass(className);
        }
    },
    toggleAllClass: function (selectors, className) {
        if (e.isVariableDefined(selectors) && (selectors instanceof HTMLElement)) {
            document.querySelectorAll(selectors).forEach((element) => {
                element.toggleClass(className);
            });
        }
    },
    addClass: function (selectors, className) {
        if (!(selectors instanceof HTMLElement) && selectors !== null) {
            selectors = document.querySelector(selectors);
        }
        if (e.isVariableDefined(selectors)) {
            selectors.addClass(className);
        }
    },
    select: function (selectors) {
        return document.querySelector(selectors);
    },
    selectAll: function (selectors) {
        return document.querySelectorAll(selectors);
    },


    // START: 01 Preloader
    preLoader: function () {
        window.onload = function () {
            var preloader = e.select('.preloader');
            if (e.isVariableDefined(preloader)) {
                preloader.className += ' animate__animated animate__fadeOut';
                setTimeout(function () {
                    preloader.style.display = 'none';
                }, 200);
            }
        };
    },
    // END: Preloader

    // START: 02 Mega Menu
    megaMenu: function () {
        e.onAll('.dropdown-menu a.dropdown-item.dropdown-toggle', 'click', function (event) {
            var element = this;
            event.preventDefault();
            event.stopImmediatePropagation();
            if (e.isVariableDefined(element.nextElementSibling) && !element.nextElementSibling.classList.contains("show")) {
                const parents = e.getParents(element, '.dropdown-menu');
                e.removeClass(parents.querySelector('.show'), "show");
                if (e.isVariableDefined(parents.querySelector('.dropdown-opened'))) {
                    e.removeClass(parents.querySelector('.dropdown-opened'), "dropdown-opened");
                }

            }
            var $subMenu = e.getNextSiblings(element, ".dropdown-menu");
            e.toggleClass($subMenu, "show");
            $subMenu.previousElementSibling.toggleClass('dropdown-opened');
            var parents = e.getParents(element, 'li.nav-item.dropdown.show');
            if (e.isVariableDefined(parents) && parents.length > 0) {
                e.on(parents, 'hidden.bs.dropdown', function (event) {
                    e.removeAllClass('.dropdown-submenu .show');
                });
            }
        });
    },
    // END: Mega Menu

    // START: 03 Sticky Header
    stickyHeader: function () {
        var stickyNav = e.select('.header-sticky');
        if (e.isVariableDefined(stickyNav)) {
            var stickyHeight = stickyNav.offsetHeight;
            stickyNav.insertAdjacentHTML('afterend', '<div id="sticky-space"></div>');
            var stickySpace = e.select('#sticky-space');
            if (e.isVariableDefined(stickySpace)) {
                document.addEventListener('scroll', function (event) {
                    var scTop = window.pageYOffset || document.documentElement.scrollTop;
                    if (scTop >= 400) {
                        stickySpace.addClass('active');
                        e.select("#sticky-space.active").style.height = stickyHeight + 'px';
                        stickyNav.addClass('header-sticky-on');
                    } else {
                        stickySpace.removeClass('active');
                        stickySpace.style.height = '0px';
                        stickyNav.removeClass("header-sticky-on");
                    }
                });
            }
        }
    },
    // END: Sticky Header

    // START: 04 Tiny Slider
    tinySlider: function () {
        const $carousel = e.select('.tiny-slider-inner');
        if (e.isVariableDefined($carousel)) {
            const tnsCarousel = e.selectAll('.tiny-slider-inner');
            tnsCarousel.forEach(slider => {
                const slider1 = slider;
                const sliderMode = slider1.getAttribute('data-mode') ? slider1.getAttribute('data-mode') : 'carousel';
                const sliderAxis = slider1.getAttribute('data-axis') ? slider1.getAttribute('data-axis') : 'horizontal';
                const sliderSpace = slider1.getAttribute('data-gutter') ? slider1.getAttribute('data-gutter') : 30;
                const sliderEdge = slider1.getAttribute('data-edge') ? slider1.getAttribute('data-edge') : 0;

                const sliderItems = slider1.getAttribute('data-items') ? slider1.getAttribute('data-items') : 4; //option: number (items in all device)
                const sliderItemsXl = slider1.getAttribute('data-items-xl') ? slider1.getAttribute('data-items-xl') : Number(sliderItems); //option: number (items in 1200 to end )
                const sliderItemsLg = slider1.getAttribute('data-items-lg') ? slider1.getAttribute('data-items-lg') : Number(sliderItemsXl); //option: number (items in 992 to 1199 )
                const sliderItemsMd = slider1.getAttribute('data-items-md') ? slider1.getAttribute('data-items-md') : Number(sliderItemsLg); //option: number (items in 768 to 991 )
                const sliderItemsSm = slider1.getAttribute('data-items-sm') ? slider1.getAttribute('data-items-sm') : Number(sliderItemsMd); //option: number (items in 576 to 767 )
                const sliderItemsXs = slider1.getAttribute('data-items-xs') ? slider1.getAttribute('data-items-xs') : Number(sliderItemsSm); //option: number (items in start to 575 )

                const sliderSpeed = slider1.getAttribute('data-speed') ? slider1.getAttribute('data-speed') : 500;
                const sliderautoWidth = slider1.getAttribute('data-autowidth') === 'true'; //option: true or false
                const sliderArrow = slider1.getAttribute('data-arrow') !== 'false'; //option: true or false
                const sliderDots = slider1.getAttribute('data-dots') !== 'false'; //option: true or false

                const sliderAutoPlay = slider1.getAttribute('data-autoplay') !== 'false'; //option: true or false
                const sliderAutoPlayTime = slider1.getAttribute('data-autoplaytime') ? slider1.getAttribute('data-autoplaytime') : 4000;
                const sliderHoverPause = slider1.getAttribute('data-hoverpause') === 'true'; //option: true or false
                if (e.isVariableDefined(e.select('.custom-thumb'))) {
                    var sliderNavContainer = e.select('.custom-thumb');
                }
                const sliderLoop = slider1.getAttribute('data-loop') !== 'false'; //option: true or false
                const sliderRewind = slider1.getAttribute('data-rewind') === 'true'; //option: true or false
                const sliderAutoHeight = slider1.getAttribute('data-autoheight') === 'true'; //option: true or false
                const sliderAutoWidth = slider1.getAttribute('data-autowidth') === 'true'; //option: true or false
                const sliderfixedWidth = slider1.getAttribute('data-fixedwidth') === 'true'; //option: true or false
                const sliderTouch = slider1.getAttribute('data-touch') !== 'false'; //option: true or false
                const sliderDrag = slider1.getAttribute('data-drag') !== 'false'; //option: true or false
                // Check if document DIR is RTL
                const ifRtl = document.getElementsByTagName("html")[0].getAttribute("dir");
                let sliderDirection;
                if (ifRtl === 'rtl') {
                    sliderDirection = 'rtl';
                }

                const tnsSlider = tns({
                    container: slider,
                    mode: sliderMode,
                    axis: sliderAxis,
                    gutter: sliderSpace,
                    edgePadding: sliderEdge,
                    speed: sliderSpeed,
                    autoWidth: sliderautoWidth,
                    controls: sliderArrow,
                    nav: sliderDots,
                    autoplay: sliderAutoPlay,
                    autoplayTimeout: sliderAutoPlayTime,
                    autoplayHoverPause: sliderHoverPause,
                    autoplayButton: false,
                    autoplayButtonOutput: false,
                    controlsPosition: top,
                    navContainer: sliderNavContainer,
                    navPosition: top,
                    autoplayPosition: top,
                    controlsText: [
                        '<i class="bi bi-arrow-left"></i>',
                        '<i class="bi bi-arrow-right"></i>'
                    ],
                    loop: sliderLoop,
                    rewind: sliderRewind,
                    autoHeight: sliderAutoHeight,

                    fixedWidth: sliderfixedWidth,
                    touch: sliderTouch,
                    mouseDrag: sliderDrag,
                    arrowKeys: true,
                    items: sliderItems,
                    textDirection: sliderDirection,
                    responsive: {
                        0: {
                            items: Number(sliderItemsXs)
                        },
                        576: {
                            items: Number(sliderItemsSm)
                        },
                        768: {
                            items: Number(sliderItemsMd)
                        },
                        992: {
                            items: Number(sliderItemsLg)
                        },
                        1200: {
                            items: Number(sliderItemsXl)
                        }
                    }
                });
            });
        }
    },
    // END: Tiny Slider

    // START: 05 Sticky Bar
    stickyBar: function () {
        var sb = e.select('[data-sticky]');
        if (e.isVariableDefined(sb)) {
            var sticky = new Sticky('[data-sticky]');
        }
    },
    // END: Sticky Bar

    // START: 06 Tooltip
    // Enable tooltips everywhere via data-toggle attribute
    toolTipFunc: function () {
        var tooltipTriggerList = [].slice.call(e.selectAll('[data-bs-toggle="tooltip"]'))
        var tooltipList = tooltipTriggerList.map(function (tooltipTriggerEl) {
            return new bootstrap.Tooltip(tooltipTriggerEl)
        })
    },
    // END: Tooltip

    // START: 07 Popover
    // Enable popover everywhere via data-toggle attribute
    popOverFunc: function () {
        var popoverTriggerList = [].slice.call(e.selectAll('[data-bs-toggle="popover"]'))
        var popoverList = popoverTriggerList.map(function (popoverTriggerEl) {
            return new bootstrap.Popover(popoverTriggerEl)
        })
    },
    // END: Popover

    // START: 08 Back to Top
    backTotop: function () {
        var scrollpos = window.scrollY;
        var backBtn = e.select('.back-top');
        if (e.isVariableDefined(backBtn)) {
            var add_class_on_scroll = () => backBtn.addClass("back-top-show");
            var remove_class_on_scroll = () => backBtn.removeClass("back-top-show");

            window.addEventListener('scroll', function () {
                scrollpos = window.scrollY;
                if (scrollpos >= 800) {
                    add_class_on_scroll()
                } else {
                    remove_class_on_scroll()
                }
            });

            backBtn.addEventListener('click', () => window.scrollTo({
                top: 0,
                behavior: 'smooth',
            }));
        }
    },
    // END: Back to Top

    // START: 09 GLightbox
    lightBox: function () {
        var light = e.select('[data-glightbox]');
        if (e.isVariableDefined(light)) {
            var lb = GLightbox({
                selector: '*[data-glightbox]',
                openEffect: 'fade',
                closeEffect: 'fade'
            });
        }
    },
    // END: GLightbox

    // START: 10 Choices
    choicesSelect: function () {
        var choice = e.select('.js-choice');

        if (e.isVariableDefined(choice)) {
            var element = document.querySelectorAll('.js-choice');

            element.forEach(function (item) {
                var removeItemBtn = item.getAttribute('data-remove-item-button') == 'true' ? true : false;
                var placeHolder = item.getAttribute('data-placeholder') == 'false' ? false : true;
                var placeHolderVal = item.getAttribute('data-placeholder-val') ? item.getAttribute('data-placeholder-val') : 'Type and hit enter';
                var maxItemCount = item.getAttribute('data-max-item-count') ? item.getAttribute('data-max-item-count') : 3;
                var searchEnabled = item.getAttribute('data-search-enabled') == 'true' ? true : false;

                var choices = new Choices(item, {
                    removeItemButton: removeItemBtn,
                    placeholder: placeHolder,
                    placeholderValue: placeHolderVal,
                    maxItemCount: maxItemCount,
                    searchEnabled: searchEnabled,
                    shouldSort: false
                });

            });
        }
    },
    // END: Choices

    // START: 11 AOS Animation
    aosFunc: function () {
        var aos = e.select('.aos');
        if (e.isVariableDefined(aos)) {
            AOS.init({
                duration: 500,
                easing: 'ease-out-quart',
                once: true
            });
        }
    },
    // END: AOS Animation

    // START: 12 Quill Editor
    quill: function () {
        var ql = e.select('.quilleditor');
        if (e.isVariableDefined(ql)) {
            var element = e.selectAll('.quilleditor');
            element.forEach(function (item) {
                var t = item.previousElementSibling;
                var edt = new Quill(item, {
                    modules: {toolbar: t},
                    theme: 'snow'
                });
            });
        }
    },
    // END: Quill Editor

    // START: 13 Stepper
    stepper: function () {
        var stp = e.select('#stepper');
        if (e.isVariableDefined(stp)) {
            var nxtBtn = document.querySelectorAll('.next-btn');
            var prvBtn = document.querySelectorAll('.prev-btn');

            var stepper = new Stepper(document.querySelector('#stepper'), {
                linear: false,
                animation: true
            });

            nxtBtn.forEach(function (button) {
                button.addEventListener("click", () => {
                    stepper.next()
                })
            });

            prvBtn.forEach(function (button) {
                button.addEventListener("click", () => {
                    stepper.previous()
                })
            });
        }
    },
    // END: Stepper

    // START: 14 Pricing
    pricing: function () {
        var p = e.select('.price-wrap');
        if (e.isVariableDefined(p)) {
            var pWrap = e.selectAll(".price-wrap");
            pWrap.forEach(item => {

                var priceSwitch = item.querySelector('.price-toggle'),
                    priceElement = item.querySelectorAll('.plan-price');

                priceSwitch.addEventListener('change', function () {
                    if (priceSwitch.checked) {
                        priceElement.forEach(pItem => {
                            var dd = pItem.getAttribute('data-annual-price');
                            pItem.innerHTML = dd;
                        });
                    } else {
                        priceElement.forEach(pItem => {
                            var ee = pItem.getAttribute('data-monthly-price');
                            pItem.innerHTML = ee;
                        });
                    }
                });
            });
        }
    },
    // END: Pricing

    // START: 15 Sticky element
    stickyElement: function () {
        var scrollpos = window.scrollY;
        var sp = e.select('.sticky-element');
        if (e.isVariableDefined(sp)) {
            var add_class_on_scroll = () => sp.addClass("sticky-element-sticked");
            var remove_class_on_scroll = () => sp.removeClass("sticky-element-sticked");

            window.addEventListener('scroll', function () {
                scrollpos = window.scrollY;
                if (scrollpos >= 800) {
                    add_class_on_scroll()
                } else {
                    remove_class_on_scroll()
                }
            });
        }
    },
    // END: Sticky element

    // START: 16 Flatpicker
    flatPicker: function () {

        // Định nghĩa tiếng Việt cho Flatpickr
        flatpickr.localize({
            weekdays: {
                shorthand: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
                longhand: ['Chủ nhật', 'Thứ hai', 'Thứ ba', 'Thứ tư', 'Thứ năm', 'Thứ sáu', 'Thứ bảy']
            },
            months: {
                shorthand: ['Th1', 'Th2', 'Th3', 'Th4', 'Th5', 'Th6', 'Th7', 'Th8', 'Th9', 'Th10', 'Th11', 'Th12'],
                longhand: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6', 'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12']
            },
            firstDayOfWeek: 1,
            rangeSeparator: " => ",
            weekAbbreviation: "Tuần",
            time_24hr: true
        });

        var picker = e.select('.flatpickr');
        if (e.isVariableDefined(picker)) {
            var element = e.selectAll('.flatpickr');
            element.forEach(function (item) {
                var mode = item.getAttribute('data-mode') == 'multiple' ? 'multiple' : item.getAttribute('data-mode') == 'range' ? 'range' : 'single';
                var enableTime = item.getAttribute('data-enableTime') == 'true' ? true : false;
                var noCalendar = item.getAttribute('data-noCalendar') == 'true' ? true : false;
                var inline = item.getAttribute('data-inline') == 'true' ? true : false;
                var dateFormat = item.getAttribute('data-date-format') ? item.getAttribute('data-date-format') : item.getAttribute('data-enableTime') == 'true' ? "h:i K" : "Y-m-d";

                flatpickr(item, {
                    locale: "vn",
                    mode: mode,
                    enableTime: enableTime,
                    noCalendar: noCalendar,
                    inline: inline,
                    animate: "false",
                    position: "top",
                    dateFormat: dateFormat,
                    altInput: true,
                    altFormat: "d/m/Y",
                    disableMobile: "true",
                    minDate: "today",
                    onValueUpdate: function (selectedDates, dateStr, instance) {
                        if (instance.config.mode === "range" && selectedDates.length === 2) {
                            const diff = (selectedDates[1] - selectedDates[0]) / (1000 * 60 * 60 * 24);
                            if (diff < 1) {
                                instance.clear(); // Không hợp lệ thì xoá chọn
                            }
                        }
                    },
                });

            });
        }
    },
    // END: Flatpicker

    // START: 17 Splide slider
    splideSlider: function () {
        var splide1 = e.select('.splide-main');
        if (e.isVariableDefined(splide1)) {
            var secondarySlider = new Splide('.splide-thumb', {
                rewind: true,
                fixedWidth: 200,
                fixedHeight: 120,
                isNavigation: true,
                gap: 10,
                focus: 'center',
                pagination: false,
                cover: true,
                breakpoints: {
                    '600': {
                        fixedWidth: 150,
                        fixedHeight: 100,
                    }
                }
            }).mount();

            // Create the main slider.
            var primarySlider = new Splide('.splide-main', {
                type: 'fade',
                heightRatio: 0.5,
                pagination: false,
                arrows: false,
                autoplay: true,
                cover: true,
            });

            // Set the thumbnails slider as a sync target and then call mount.
            primarySlider.sync(secondarySlider).mount();

        }
    },
    // END: Splide slider

    // START: 18 noUislider
    rangeSlider: function () {
        var rangeSlider = e.select('.noui-slider-range');
        if (e.isVariableDefined(rangeSlider)) {
            var rangeSliders = e.selectAll('.noui-slider-range');
            rangeSliders.forEach(slider => {
                var nouiMin = parseInt(slider.getAttribute('data-range-min'));
                var nouiMax = parseInt(slider.getAttribute('data-range-max'));
                var nouiSelectedMin = parseInt(slider.getAttribute('data-range-selected-min'));
                var nouiSelectedMax = parseInt(slider.getAttribute('data-range-selected-max'));

                var rangeText = slider.previousElementSibling;
                var imin = rangeText.firstElementChild;
                var imax = rangeText.lastElementChild;
                var inputs = [imin, imax];

                noUiSlider.create(slider, {
                    start: [nouiSelectedMin, nouiSelectedMax],
                    connect: true,
                    step: 100,
                    range: {
                        min: [nouiMin],
                        max: [nouiMax]
                    },
                    format: {
                        to: function (value) {
                            return value.toLocaleString('en-US') + ',000';
                        },
                        from: function (value) {
                            return Number(value.replace(',00', ''));
                        }
                    }
                });

                slider.noUiSlider.on("update", function (values, handle) {
                    inputs[handle].value = values[handle];
                });

                // Ép giá trị nhập vào không vượt quá min/max
                imin.addEventListener('change', function () {
                    let value = parseInt(this.value.replace(/,/g, '')) / 1000;
                    slider.noUiSlider.set([value, null]);
                });

                imax.addEventListener('change', function () {
                    let value = parseInt(this.value.replace(/,/g, '')) / 1000;
                    slider.noUiSlider.set([null, value]);
                });
            });
        }
    },
    // END: noUislider

    // START: 19 dropzone
    dropZone: function () {
        if (e.isVariableDefined(e.select("[data-dropzone]"))) {
            window.Dropzone.autoDiscover = false;

            // 1. Default Dropzone Initialization
            if (e.isVariableDefined(e.select(".dropzone-default"))) {
                e.selectAll(".dropzone-default").forEach((e => {
                    const a = e.dataset.dropzone ? JSON.parse(e.dataset.dropzone) : {},
                        b = {
                            url: '/upload', // Change this URL to your actual image upload code
                            // Fake the file upload, since GitHub does not handle file uploads
                            // and returns a 404
                            // https://docs.dropzone.dev/getting-started/setup/server-side-implementation
                            init: function () {
                                this.on('error', function (file, errorMessage) {
                                    if (file.accepted) {
                                        var mypreview = document.getElementsByClassName('dz-error');
                                        mypreview = mypreview[mypreview.length - 1];
                                        mypreview.classList.toggle('dz-error');
                                        mypreview.classList.toggle('dz-success');
                                    }
                                });
                            }
                        },
                        c = {
                            ...b,
                            ...a
                        };
                    new Dropzone(e, c);
                }));
            }

            // 2. Custom cover and list previews Dropzone Initialization
            if (e.isVariableDefined(e.select(".dropzone-custom"))) {
                e.selectAll(".dropzone-custom").forEach((d => {
                    const j = d.dataset.dropzone ? JSON.parse(d.dataset.dropzone) : {},
                        o = {
                            addRemoveLinks: true,
                            previewsContainer: d.querySelector(".dz-preview"),
                            previewTemplate: d.querySelector(".dz-preview").innerHTML,
                            url: '/upload', // Change this URL to your actual image upload code
                            // Now fake the file upload, since GitHub does not handle file uploads
                            // and returns a 404
                            // https://docs.dropzone.dev/getting-started/setup/server-side-implementation
                            init: function () {
                                this.on('error', function (file, errorMessage) {
                                    if (file.accepted) {
                                        var mypreview = document.getElementsByClassName('dz-error');
                                        mypreview = mypreview[mypreview.length - 1];
                                        mypreview.classList.toggle('dz-error');
                                        mypreview.classList.toggle('dz-success');
                                    }
                                });
                            }
                        },
                        x = {
                            ...o,
                            ...j
                        };
                    d.querySelector(".dz-preview").innerHTML = '';
                    new Dropzone(d, x);
                }));
            }
        }
    },
    // END: dropzone

    // START: 20 Fake Password
    fakePwd: function () {
        if (e.isVariableDefined(e.select('.fakepassword'))) {
            var password = document.querySelector('.fakepassword');
            var toggler = document.querySelector('.fakepasswordicon');

            var showHidePassword = () => {
                if (password.type == 'password') {
                    password.setAttribute('type', 'text');
                    toggler.classList.add('fa-eye');
                } else {
                    toggler.classList.remove('fa-eye');
                    password.setAttribute('type', 'password');
                }
            };

            toggler.addEventListener('click', showHidePassword);
        }
    },
    // END: Fake Password

    // START: 21 Auto tab
    autoTabinput: function () {
        var autb = document.getElementsByClassName("autotab")[0];
        if (e.isVariableDefined(autb)) {
            autb.onkeyup = function (e) {
                var target = e.srcElement;
                var maxLength = parseInt(target.attributes["maxlength"].value, 10);
                var myLength = target.value.length;
                if (myLength >= maxLength) {
                    var next = target;
                    while (next = next.nextElementSibling) {
                        if (next == null)
                            break;
                        if (next.tagName.toLowerCase() == "input") {
                            next.focus();
                            break;
                        }
                    }
                }
            }
        }
    },
    // END: Auto tab input

    // START: 22 Guest Selector
    guestSelector: function () {
        if (e.isVariableDefined(e.select('.guest-selector'))) {

            let guests = parseInt(document.getElementById('guestsInput').value);
            let rooms = parseInt(document.getElementById('roomsInput').value);

            let selectionResult = document.querySelector('.selection-result');

            let guestValue = document.querySelector('.guests');
            let guestAdd = document.querySelector('.guest-add');
            let guestRemove = document.querySelector('.guest-remove');

            let roomValue = document.querySelector('.rooms');
            let roomAdd = document.querySelector('.room-add');
            let roomRemove = document.querySelector('.room-remove');

            function addElement(type) {
                if (type == 'guest') {
                    if(guests == 1) guestRemove.classList.remove("disabled");
                    guests = guests < 50 ? guests + 1 : guests;
                    if(guests == 50) guestAdd.classList.add("disabled");

                    showElements();
                }  else if (type == 'room') {
                    if(rooms == 1) roomRemove.classList.remove("disabled");
                    rooms = rooms < 20 ? rooms + 1 : rooms;
                    if(rooms == 20) roomAdd.classList.add("disabled");

                    showElements();
                }
            }

            function showElements() {
                guestValue.innerText = guests;
                roomValue.innerText = rooms;

                let guestInput = document.getElementById('guestsInput')
                if(guestInput != null) guestInput.value = guests;
                let roomsInput = document.getElementById('roomsInput')
                if(roomsInput != null) roomsInput.value = rooms;

                let roomText = 'Phòng';
                let guestText = 'Khách';
                let resultText = guests + ' ' + guestText + ' ' + rooms + ' ' + roomText;
                selectionResult.setAttribute('value', resultText);
            }


            function removeElement(type) {
                if (type == 'guest') {
                    if(guests == 50) guestAdd.classList.remove("disabled");
                    guests = guests > 1 ? guests - 1 : guests;
                    if(guests == 1) guestRemove.classList.add("disabled");

                    showElements();
                } else if (type == 'room') {
                    if(rooms == 20) roomAdd.classList.remove("disabled");
                    rooms = rooms > 1 ? rooms - 1 : rooms;
                    if(rooms == 1) roomRemove.classList.add("disabled");

                    showElements();
                }
            }

            guestAdd.addEventListener('click', function () {
                addElement('guest');
            });

            guestRemove.addEventListener('click', function () {
                removeElement('guest');
            });

            roomAdd.addEventListener('click', function () {
                addElement('room');
            });

            roomRemove.addEventListener('click', function () {
                removeElement('room');
            });
        }
    },
    // END: Guest Selector

    // START: 23 Parallax Background
    parallaxBG: function () {
        var parBG = e.select('.bg-parallax');
        if (e.isVariableDefined(parBG)) {
            jarallax(e.selectAll('.bg-parallax'), {
                speed: 0.6
            });
        }
    },
    // END: Parallax Background

    // START: 24 Overlay scrollbar
    overlayScrollbars: function () {
        var os = e.select('.custom-scrollbar');
        if (os) {
            document.addEventListener("DOMContentLoaded", function () {
                var cs = document.querySelectorAll('.custom-scrollbar');
                cs.forEach(c => {
                    OverlayScrollbars(c, {
                        scrollbars: {
                            autoHide: 'leave',
                            autoHideDelay: 200
                        },
                        overflowBehavior: {
                            x: "visible-hidden",
                            y: "scroll"
                        }
                    });
                });
            });
        }
    },
    // END: Overlay scrollbars

    // START: 25 Traffic Chart
    trafficstatsChart: function () {
        var cpv = e.select('#apexChartTrafficStats');
        if (e.isVariableDefined(cpv)) {
            var options = {
                colors: [
                    '#2163e8',
                ],
                series: [{
                    name: 'Users',
                    data: [100, 401, 305, 501, 409, 602, 609, 901, 848, 602, 809, 901]
                }],
                chart: {
                    height: 320,
                    type: 'area',
                    toolbar: {
                        show: false
                    }
                },
                grid: {
                    strokeDashArray: 4,
                    position: 'back'
                },
                dataLabels: {
                    enabled: false
                },
                stroke: {
                    curve: 'smooth'
                },
                legend: {
                    show: true,
                    horizontalAlign: 'right',
                    position: 'top',
                    labels: {},
                    markers: {
                        width: 8,
                        height: 8
                    }
                },
                xaxis: {
                    labels: {
                        show: true
                    },
                    axisBorder: {
                        show: false
                    },
                    categories: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec']
                },
                tooltip: {
                    x: {
                        format: 'dd/MM/yy HH:mm'
                    },
                },
            };
            var chart = new ApexCharts(document.querySelector("#apexChartTrafficStats"), options);
            chart.render();
        }
    },
    // END: Traffic Chart 

    // START: 26 Traffic Chart 2
    trafficChart: function () {
        var cpv = e.select('#ChartTrafficViews');
        if (e.isVariableDefined(cpv)) {
            // CHART: Page Views
            var options = {
                series: [70, 15, 10, 5],
                labels: ['Organic', 'Google', 'Social media', 'Referral'],
                chart: {
                    height: 200,
                    width: 200,
                    offsetX: 0,
                    type: 'donut',
                    sparkline: {
                        enabled: !0
                    }
                },
                colors: [
                    ThemeColor.getCssVariableValue('--bs-primary'),
                    ThemeColor.getCssVariableValue('--bs-success'),
                    ThemeColor.getCssVariableValue('--bs-warning'),
                    ThemeColor.getCssVariableValue('--bs-danger')
                ],
                tooltip: {
                    theme: "dark"
                },
                responsive: [{
                    breakpoint: 480,
                    options: {
                        chart: {
                            width: 200,
                            height: 200,
                        },
                        legend: {
                            position: 'bottom'
                        }
                    }
                }]
            };
            var chart = new ApexCharts(document.querySelector("#ChartTrafficViews"), options);
            chart.render();
        }
    },
    // END: Traffic Chart 2


    // START: 27 Traffic Chart 3
    trafficsplineChart: function () {
        var chartEl = document.querySelector('#ChartGuesttraffic');
        if (!chartEl) return;

        // Parse data from data attributes
        var labels = JSON.parse(chartEl.dataset.labels);
        var checkInData = JSON.parse(chartEl.dataset.checkin);
        var checkOutData = JSON.parse(chartEl.dataset.checkout);

        var options = {
            series: [
                {
                    name: 'Check-in',
                    data: checkInData
                },
                {
                    name: 'Check-out',
                    data: checkOutData
                }
            ],
            chart: {
                height: 350,
                type: 'area'
            },
            colors: [
                ThemeColor.getCssVariableValue('--bs-primary'),
                ThemeColor.getCssVariableValue('--bs-info')
            ],
            dataLabels: {
                enabled: false
            },
            stroke: {
                curve: 'smooth'
            },
            xaxis: {
                type: 'category',
                categories: labels
            }
        };

        var chart = new ApexCharts(chartEl, options);
        chart.render();
    },


    // END: Traffic Chart 3

    // START: 28 Traffic Chart 4
    trafficroomChart: function () {
        var cpv = e.select('#ChartTrafficRooms');
        if (e.isVariableDefined(cpv)) {
            // CHART: Page Views
            var options = {
                series: [70, 30],
                labels: ['Sold Out', 'Available'],
                chart: {
                    height: 300,
                    width: 300,
                    offsetX: 0,
                    type: 'donut',
                    sparkline: {
                        enabled: !0
                    }
                },
                colors: [
                    ThemeColor.getCssVariableValue('--bs-danger'),
                    ThemeColor.getCssVariableValue('--bs-success')
                ],
                tooltip: {
                    theme: "dark"
                },
                responsive: [{
                    breakpoint: 480,
                    options: {
                        chart: {
                            width: 200,
                            height: 200,
                        },
                        legend: {
                            position: 'bottom'
                        }
                    }
                }]
            }
            var chart = new ApexCharts(document.querySelector("#ChartTrafficRooms"), options);
            chart.render();
        }
    },
    // END: Traffic Chart 4

};
e.init();

function scrollToId(id){
    const e = document.getElementById(id);
    if(e){
        const y = e.getBoundingClientRect().top + window.scrollY - 100;
        window.scrollTo({top: y, behavior: 'smooth'});
    }
}

document.addEventListener('DOMContentLoaded', function () {
    const hash = window.location.hash;
    if(hash){
        const id = hash.substring(1);
        console.log(id);
        scrollToId(id);
    }
});