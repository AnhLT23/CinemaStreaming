(() => {
    document.querySelectorAll("[data-password-target]").forEach((button) => {
        button.addEventListener("click", () => {
            const input = document.getElementById(button.dataset.passwordTarget);
            if (!input) return;
            const visible = input.type === "password";
            input.type = visible ? "text" : "password";
            const icon = button.querySelector(".material-symbols-outlined");
            if (icon) icon.textContent = visible ? "visibility_off" : "visibility";
            button.setAttribute("aria-label", visible ? "Ẩn mật khẩu" : "Hiện mật khẩu");
        });
    });
})();