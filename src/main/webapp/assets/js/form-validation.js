(() => {
    document.querySelectorAll("form[novalidate]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            if (!form.checkValidity()) {
                event.preventDefault();
                event.stopPropagation();
            }
            form.classList.add("was-validated");
        });
    });

    const form = document.getElementById("register-form");
    if (!form) return;

    const password = form.querySelector('[name="password"]');
    const confirmation = form.querySelector('[name="confirmPassword"]');
    const message = document.getElementById("form-message");
    if (!password || !confirmation || !message) return;

    const clearMismatch = () => {
        confirmation.setCustomValidity("");
        message.hidden = true;
        message.textContent = "";
    };

    const handlePasswordInput = () => {
        if (password.value === confirmation.value) clearMismatch();
    };

    password.addEventListener("input", handlePasswordInput);
    confirmation.addEventListener("input", handlePasswordInput);

    form.addEventListener("submit", (event) => {
        if (password.value === confirmation.value) {
            clearMismatch();
            return;
        }

        event.preventDefault();
        confirmation.setCustomValidity("Hai mật khẩu không khớp.");
        message.textContent = "Hai mật khẩu không khớp.";
        message.hidden = false;
        confirmation.reportValidity();
    });
})();
document.addEventListener('DOMContentLoaded', () => {
  const form = document.getElementById('register-form');
  if (!form) return;
  form.addEventListener('submit', (event) => {
    const one = form.querySelector('[name="password"]').value;
    const two = form.querySelector('[name="confirmPassword"]').value;
    const message = document.getElementById('form-message');
    if (one !== two) {
      event.preventDefault();
      message.textContent = 'Hai mật khẩu không khớp.';
      message.hidden = false;
    } else message.hidden = true;
  });
});
