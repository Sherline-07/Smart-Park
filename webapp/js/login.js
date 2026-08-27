// Worker/Admin Login Logic
document.addEventListener("DOMContentLoaded", () => {
    // If already logged in, redirect to dashboard
    const savedWorker = localStorage.getItem("smartpark_worker");
    if (savedWorker) {
        window.location.href = "index.html";
        return;
    }

    const form = document.getElementById("loginForm");
    const errorAlert = document.getElementById("errorAlert");
    const errorMessage = document.getElementById("errorMessage");
    const loginBtn = document.getElementById("loginBtn");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        errorAlert.classList.remove("show");

        const username = document.getElementById("username").value.trim();
        const password = document.getElementById("password").value.trim();

        if (!username || !password) {
            showError("Please enter both username and password.");
            return;
        }

        loginBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Authenticating...`;
        loginBtn.disabled = true;

        try {
            const response = await fetch("/api/auth/login", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ username, password })
            });

            const data = await response.json();

            if (response.ok && data.success) {
                // Save session in localStorage
                localStorage.setItem("smartpark_worker", JSON.stringify(data.worker));
                loginBtn.innerHTML = `<i class="fa-solid fa-circle-check"></i> Redirecting...`;
                setTimeout(() => {
                    window.location.href = "index.html";
                }, 400);
            } else {
                showError(data.message || "Invalid username or password.");
                loginBtn.innerHTML = `<i class="fa-solid fa-right-to-bracket"></i> Login to System`;
                loginBtn.disabled = false;
            }
        } catch (err) {
            console.error("Login connection error:", err);
            showError("Unable to connect to Java backend. Ensure server is running.");
            loginBtn.innerHTML = `<i class="fa-solid fa-right-to-bracket"></i> Login to System`;
            loginBtn.disabled = false;
        }
    });

    function showError(msg) {
        errorMessage.innerText = msg;
        errorAlert.classList.add("show");
    }
});

// Helper to fill demo credentials
function fillDemo(user, pass) {
    document.getElementById("username").value = user;
    document.getElementById("password").value = pass;
    document.getElementById("errorAlert").classList.remove("show");
}
