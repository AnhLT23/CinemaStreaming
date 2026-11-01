(function () {
    const STORAGE_KEY = "tvhay_theme";

    function getSavedTheme() {
        const fromStorage = localStorage.getItem(STORAGE_KEY) || localStorage.getItem("theme");
        if (fromStorage === "light" || fromStorage === "dark") {
            return fromStorage;
        }
        const fromAttr = document.documentElement.getAttribute("data-theme");
        if (fromAttr === "light" || fromAttr === "dark") {
            return fromAttr;
        }
        return "dark";
    }

    function applyTheme(theme) {
        const normalized = theme === "light" ? "light" : "dark";
        const root = document.documentElement;

        root.setAttribute("data-theme", normalized);
        root.setAttribute("data-bs-theme", normalized);
        root.classList.toggle("dark", normalized === "dark");
        root.classList.toggle("light", normalized === "light");

        if (document.body) {
            document.body.setAttribute("data-theme", normalized);
            document.body.classList.toggle("theme-light", normalized === "light");
            document.body.classList.toggle("theme-dark", normalized === "dark");
        }

        localStorage.setItem(STORAGE_KEY, normalized);
        localStorage.setItem("theme", normalized);

        // Cập nhật biểu tượng trên nút bấm (☾ hoặc ☀)
        document.querySelectorAll(".theme-toggle .theme-icon").forEach(function (icon) {
            icon.textContent = normalized === "light" ? "☀" : "☾";
        });
    }

    // Áp dụng ngay lập tức khi nạp script
    applyTheme(getSavedTheme());

    document.addEventListener("DOMContentLoaded", function () {
        applyTheme(getSavedTheme());

        document.querySelectorAll(".theme-toggle").forEach(function (btn) {
            btn.addEventListener("click", function (e) {
                e.preventDefault();
                const current = document.documentElement.getAttribute("data-theme") || "dark";
                const nextTheme = current === "dark" ? "light" : "dark";

                applyTheme(nextTheme);

                // Đồng bộ ngầm lên Server (Session + Cookie + Database)
                const apiUrl = btn.getAttribute("data-theme-url") || "api/theme";
                fetch(apiUrl, {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8"
                    },
                    body: "theme=" + encodeURIComponent(nextTheme)
                }).catch(function () {
                    // Nếu server chưa bật API thì localStorage vẫn giữ nguyên trạng thái
                });
            });
        });
    });
    const root = document.documentElement;
    const previous = localStorage.getItem('cinema_theme');
    if (root.dataset.theme !== 'dark' && root.dataset.theme !== 'light') {
        if (previous === 'dark' || previous === 'light')
            root.dataset.theme = previous;
    }
    const buttons = document.querySelectorAll('[data-theme-toggle]');
    if (!buttons.length)
        return;
    const csrf = document.querySelector('meta[name="csrf-token"]')?.content;
    const ownScript = [...document.scripts].find(s => s.src.includes('/assets/js/theme.js'));
    const prefix = ownScript ? new URL(ownScript.src).pathname.split('/assets/js/theme.js')[0] : '';
    buttons.forEach(button => button.addEventListener('click', async () => {
            const next = root.dataset.theme === 'dark' ? 'light' : 'dark';
            if (!csrf)
                return;
            try {
                const result = await fetch(prefix + '/api/theme', {
                    method: 'POST', credentials: 'same-origin',
                    headers: {'Content-Type': 'application/x-www-form-urlencoded;charset=UTF-8'},
                    body: new URLSearchParams({theme: next, csrfToken: csrf})
                });
                if (!result.ok)
                    throw new Error('Không thể lưu giao diện');
                root.dataset.theme = next;
                localStorage.setItem('cinema_theme', next);
            } catch (e) {
                window.alert('Không lưu được giao diện. Hãy thử lại.');
            }
        }));
})();