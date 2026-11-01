document.addEventListener('DOMContentLoaded', () => {
    // 1. Menu mobile
    const hamburger = document.getElementById('hamburger');
    const navMenu = document.getElementById('nav-menu');
    const navLinks = document.querySelectorAll('.nav-link');

    const closeMenu = () => {
        if (!navMenu || !hamburger) return;
        navMenu.classList.remove('active');
        hamburger.classList.remove('active');
        hamburger.setAttribute('aria-expanded', 'false');
        document.body.classList.remove('no-scroll');
    };

    if (hamburger && navMenu) {
        hamburger.addEventListener('click', () => {
            const isOpen = navMenu.classList.toggle('active');
            hamburger.classList.toggle('active', isOpen);
            hamburger.setAttribute('aria-expanded', String(isOpen));
            document.body.classList.toggle('no-scroll', isOpen);
        });
        navLinks.forEach((l) => l.addEventListener('click', closeMenu));
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape') closeMenu();
        });
    }

    // 2. Cuộn: header, thanh tiến trình, back-to-top, nav active
    const header = document.getElementById('header');
    const progressBar = document.getElementById('scroll-progress');
    const backToTop = document.getElementById('back-to-top');
    const sections = document.querySelectorAll('section[id]');

    const onScroll = () => {
        const y = window.scrollY;
        if (header) header.classList.toggle('scrolled', y > 50);
        if (progressBar) {
            const h = document.documentElement.scrollHeight - window.innerHeight;
            const pct = h > 0 ? Math.min((y / h) * 100, 100) : 0;
            progressBar.style.width = pct + '%';
        }
        if (backToTop) backToTop.classList.toggle('visible', y > 400);
        let current = '';
        sections.forEach((s) => {
            const top = s.offsetTop - 120;
            if (y >= top && y < top + s.offsetHeight) current = s.id;
        });
        navLinks.forEach((l) => {
            l.classList.toggle('active', l.getAttribute('href') === '#' + current);
        });
    };
    window.addEventListener('scroll', onScroll, { passive: true });
    onScroll();

    if (backToTop) {
        backToTop.addEventListener('click', () => {
            window.scrollTo({ top: 0, behavior: 'smooth' });
        });
    }

    // 3. Hiệu ứng xuất hiện khi cuộn
    const revealItems = document.querySelectorAll('.reveal');
    if ('IntersectionObserver' in window && revealItems.length) {
        const obs = new IntersectionObserver((entries) => {
            entries.forEach((en) => {
                if (en.isIntersecting) {
                    en.target.classList.add('active');
                    obs.unobserve(en.target);
                }
            });
        }, { threshold: 0.12, rootMargin: '0px 0px -50px 0px' });
        revealItems.forEach((item) => obs.observe(item));
    } else {
        revealItems.forEach((item) => item.classList.add('active'));
    }

    // 4. Đếm số liệu khi cuộn tới
    const counters = document.querySelectorAll('.counter');
    let countersStarted = false;

    const animateCounter = (counter) => {
        const target = Number(counter.getAttribute('data-target')) || 0;
        const duration = 1600;
        const startTime = performance.now();
        const update = (now) => {
            const p = Math.min((now - startTime) / duration, 1);
            const eased = 1 - Math.pow(1 - p, 2);
            counter.textContent = p < 1 ? Math.round(target * eased) : target;
            if (p < 1) requestAnimationFrame(update);
        };
        requestAnimationFrame(update);
    };

    const startCounters = () => {
        if (countersStarted) return;
        countersStarted = true;
        counters.forEach(animateCounter);
    };

    const statsSection = document.getElementById('stats');
    if (statsSection && 'IntersectionObserver' in window) {
        const statsObs = new IntersectionObserver((entries) => {
            entries.forEach((en) => {
                if (en.isIntersecting) {
                    startCounters();
                    statsObs.disconnect();
                }
            });
        }, { threshold: 0.3 });
        statsObs.observe(statsSection);
    } else {
        startCounters();
    }

    // 5. Form liên hệ
    const contactForm = document.getElementById('contact-form');
    const formNote = document.getElementById('form-note');
    const isValidEmail = (v) => /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);

    if (contactForm) {
        contactForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const name = contactForm.querySelector('#cf-name');
            const email = contactForm.querySelector('#cf-email');
            const message = contactForm.querySelector('#cf-message');
            [name, email, message].forEach((f) => f && f.classList.remove('invalid'));

            let valid = true;
            if (name && !name.value.trim()) { name.classList.add('invalid'); valid = false; }
            if (email && !isValidEmail(email.value.trim())) { email.classList.add('invalid'); valid = false; }
            if (message && !message.value.trim()) { message.classList.add('invalid'); valid = false; }

            if (!valid) {
                if (formNote) {
                    formNote.textContent = 'Vui lòng kiểm tra lại thông tin đã nhập.';
                    formNote.className = 'form-note error';
                }
                return;
            }

            const btn = contactForm.querySelector('button[type="submit"]');
            if (btn) { btn.disabled = true; btn.textContent = 'Đang gửi...'; }
            setTimeout(() => {
                if (formNote) {
                    formNote.textContent = 'Cảm ơn bạn! Chúng tôi sẽ liên hệ trong vòng 24 giờ.';
                    formNote.className = 'form-note success';
                }
                contactForm.reset();
                if (btn) { btn.disabled = false; btn.innerHTML = 'Gửi Yêu Cầu <i class="fa-solid fa-paper-plane"></i>'; }
            }, 900);
        });
    }

    // 6. Form đăng ký nhận tin
    const newsletterForm = document.getElementById('newsletter-form');
    if (newsletterForm) {
        newsletterForm.addEventListener('submit', (e) => {
            e.preventDefault();
            const input = newsletterForm.querySelector('input[type="email"]');
            if (!input || !isValidEmail(input.value.trim())) {
                if (input) input.style.borderColor = '#ef4444';
                return;
            }
            input.style.borderColor = '';
            const oldPlaceholder = input.placeholder;
            input.value = '';
            input.placeholder = 'Đã đăng ký thành công!';
            setTimeout(() => { input.placeholder = oldPlaceholder; }, 2500);
        });
    }
});
