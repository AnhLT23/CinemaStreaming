document.querySelectorAll("[data-demo-username]").forEach((button) => {
    button.addEventListener("click", () => {
        const identity = document.getElementById("login-identity");
        const password = document.getElementById("login-password");
        const status = document.getElementById("demo-account-status");
        if (!identity || !password || !status) return;

        identity.value = button.dataset.demoUsername;
        password.value = button.dataset.demoPassword;
        status.textContent = `Đã điền tài khoản ${button.dataset.demoUsername}. Nhấn “Đăng nhập” để tiếp tục.`;
        identity.focus();
        document.querySelectorAll("[data-demo-username]").forEach((option) => {
            option.setAttribute("aria-pressed", String(option === button));
        });
    });
});
