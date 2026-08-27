function switchAuthTab(tab) {
    const loginForm = document.getElementById('loginForm');
    const registerForm = document.getElementById('registerForm');
    const tabLoginBtn = document.getElementById('tabLoginBtn');
    const tabRegisterBtn = document.getElementById('tabRegisterBtn');
    const alertBox = document.getElementById('alertBox');
    
    alertBox.style.display = 'none';

    if (tab === 'login') {
        loginForm.style.display = 'block';
        registerForm.style.display = 'none';
        tabLoginBtn.classList.add('active');
        tabRegisterBtn.classList.remove('active');
    } else {
        loginForm.style.display = 'none';
        registerForm.style.display = 'block';
        tabLoginBtn.classList.remove('active');
        tabRegisterBtn.classList.add('active');
    }
}

function showAlert(message, type) {
    const alertBox = document.getElementById('alertBox');
    alertBox.className = 'alert-box ' + (type === 'success' ? 'alert-success' : 'alert-error');
    alertBox.innerHTML = (type === 'success' ? '<i class="fa-solid fa-circle-check mr-2"></i>' : '<i class="fa-solid fa-circle-exclamation mr-2"></i>') + message;
    alertBox.style.display = 'block';
}

async function handleUserLogin(event) {
    event.preventDefault();
    const username = document.getElementById('loginUsername').value.trim();
    const password = document.getElementById('loginPassword').value.trim();

    try {
        const response = await fetch('/api/user/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username, password })
        });

        const data = await response.json();
        if (data.success) {
            showAlert('Login successful! Redirecting to Customer Dashboard...', 'success');
            localStorage.setItem('smartpark_user', JSON.stringify(data.user));
            setTimeout(() => {
                window.location.href = 'user-dashboard.html';
            }, 1000);
        } else {
            showAlert(data.message || 'Invalid login credentials.', 'error');
        }
    } catch (err) {
        showAlert('Unable to connect to server. Ensure server is running.', 'error');
    }
}

async function handleUserRegister(event) {
    event.preventDefault();
    const fullName = document.getElementById('regFullName').value.trim();
    const username = document.getElementById('regUsername').value.trim();
    const password = document.getElementById('regPassword').value.trim();
    const phone = document.getElementById('regPhone').value.trim();
    const defaultVehicleNumber = document.getElementById('regVehicle').value.trim();
    const defaultVehicleType = document.getElementById('regVehicleType').value;

    try {
        const response = await fetch('/api/user/auth/register', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                fullName, username, password, phone,
                defaultVehicleNumber, defaultVehicleType
            })
        });

        const data = await response.json();
        if (data.success) {
            showAlert(data.message, 'success');
            setTimeout(() => {
                switchAuthTab('login');
                document.getElementById('loginUsername').value = username;
            }, 1500);
        } else {
            showAlert(data.message || 'Registration failed.', 'error');
        }
    } catch (err) {
        showAlert('Unable to connect to server. Ensure server is running.', 'error');
    }
}
