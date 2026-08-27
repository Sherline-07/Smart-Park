let currentUser = null;
let allSlots = [];

document.addEventListener('DOMContentLoaded', () => {
    const userStr = localStorage.getItem('smartpark_user');
    if (!userStr) {
        window.location.href = 'user-login.html';
        return;
    }

    try {
        currentUser = JSON.parse(userStr);
        document.getElementById('headerUserName').textContent = currentUser.fullName || currentUser.username;
        if (currentUser.defaultVehicleNumber) {
            document.getElementById('bookVehicleNumber').value = currentUser.defaultVehicleNumber;
            document.getElementById('passVehicleNumber').value = currentUser.defaultVehicleNumber;
        }
        if (currentUser.defaultVehicleType) {
            document.getElementById('bookVehicleType').value = currentUser.defaultVehicleType;
            document.getElementById('passVehicleType').value = currentUser.defaultVehicleType;
        }
    } catch (e) {
        window.location.href = 'user-login.html';
        return;
    }

    // Set default entry time to current time
    const now = new Date();
    now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
    document.getElementById('bookEntryTime').value = now.toISOString().slice(0, 16);

    loadSlotsData();
    loadUserTickets();
});

function handleUserLogout() {
    localStorage.removeItem('smartpark_user');
    window.location.href = 'user-login.html';
}

function switchUserTab(tab) {
    const btnBooking = document.getElementById('btnTabBooking');
    const btnMonthly = document.getElementById('btnTabMonthly');
    const btnHistory = document.getElementById('btnTabHistory');

    const contentBooking = document.getElementById('tabBookingContent');
    const contentMonthly = document.getElementById('tabMonthlyContent');
    const contentHistory = document.getElementById('tabHistoryContent');

    const activeStyle = "flex: 1; padding: 14px; border: none; border-radius: 12px; font-weight: 700; background: var(--accent-cyan); color: #04111d; cursor: pointer;";
    const inactiveStyle = "flex: 1; padding: 14px; border: none; border-radius: 12px; font-weight: 700; color: var(--text-muted); background: transparent; cursor: pointer;";

    if (tab === 'booking') {
        contentBooking.style.display = 'block';
        contentMonthly.style.display = 'none';
        contentHistory.style.display = 'none';
        btnBooking.style = activeStyle;
        btnMonthly.style = inactiveStyle;
        btnHistory.style = inactiveStyle;
    } else if (tab === 'monthly') {
        contentBooking.style.display = 'none';
        contentMonthly.style.display = 'block';
        contentHistory.style.display = 'none';
        btnBooking.style = inactiveStyle;
        btnMonthly.style = activeStyle;
        btnHistory.style = inactiveStyle;
        updateMonthlyPassPrices();
    } else if (tab === 'history') {
        contentBooking.style.display = 'none';
        contentMonthly.style.display = 'none';
        contentHistory.style.display = 'block';
        btnBooking.style = inactiveStyle;
        btnMonthly.style = inactiveStyle;
        btnHistory.style = activeStyle;
        loadUserTickets();
    }
}

function showUserAlert(message, type) {
    const alertBox = document.getElementById('userAlertBox');
    alertBox.className = 'alert-box mb-4 ' + (type === 'success' ? 'alert-success' : 'alert-error');
    alertBox.innerHTML = (type === 'success' ? '<i class="fa-solid fa-circle-check mr-2"></i>' : '<i class="fa-solid fa-circle-exclamation mr-2"></i>') + message;
    alertBox.style.display = 'block';
    window.scrollTo({ top: 0, behavior: 'smooth' });
}

async function loadSlotsData() {
    try {
        const response = await fetch('/api/slots');
        const data = await response.json();
        if (data.success && data.slots) {
            allSlots = data.slots;
            loadAvailableSlotsForBooking();
            loadAvailableSlotsForMonthly();
        }
    } catch (e) {
        console.error('Failed to fetch slots:', e);
    }
}

function loadAvailableSlotsForBooking() {
    const vehicleType = document.getElementById('bookVehicleType').value;
    const grid = document.getElementById('bookingSlotGrid');
    grid.innerHTML = '';

    const availableSlots = allSlots.filter(s => s.vehicleType === vehicleType && s.status === 'AVAILABLE');

    if (availableSlots.length === 0) {
        grid.innerHTML = '<span style="color: #ff8080; font-size: 13px;">No available slots for ' + vehicleType + ' right now.</span>';
        return;
    }

    availableSlots.forEach(slot => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'slot-select-btn';
        btn.textContent = slot.slotNumber + ' (F' + slot.floorLevel + ')';
        btn.onclick = () => {
            document.querySelectorAll('#bookingSlotGrid .slot-select-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
            document.getElementById('selectedSlotId').value = slot.slotId;
            document.getElementById('summarySlot').textContent = slot.slotNumber;
        };
        grid.appendChild(btn);
    });

    updateBookingFareEstimate();
}

function updateBookingFareEstimate() {
    const vehicleType = document.getElementById('bookVehicleType').value;
    const duration = parseInt(document.getElementById('bookDuration').value) || 2;
    const hourlyRate = vehicleType === '4W' ? 40.0 : 20.0;
    const total = hourlyRate * duration;

    document.getElementById('summaryBaseRate').textContent = '₹' + hourlyRate.toFixed(2) + ' / hr';
    document.getElementById('summaryDuration').textContent = duration + (duration === 1 ? ' Hour' : ' Hours');
    document.getElementById('summaryTotalFee').textContent = '₹' + total.toFixed(2);
}

function loadAvailableSlotsForMonthly() {
    const vehicleType = document.getElementById('passVehicleType').value;
    const grid = document.getElementById('passSlotGrid');
    grid.innerHTML = '';

    const availableSlots = allSlots.filter(s => s.vehicleType === vehicleType && s.status === 'AVAILABLE');

    if (availableSlots.length === 0) {
        grid.innerHTML = '<span style="color: #ff8080; font-size: 13px;">No available slots for ' + vehicleType + ' monthly pass.</span>';
        return;
    }

    availableSlots.forEach(slot => {
        const btn = document.createElement('button');
        btn.type = 'button';
        btn.className = 'slot-select-btn';
        btn.textContent = slot.slotNumber + ' (F' + slot.floorLevel + ')';
        btn.onclick = () => {
            document.querySelectorAll('#passSlotGrid .slot-select-btn').forEach(b => b.classList.remove('selected'));
            btn.classList.add('selected');
            document.getElementById('passSelectedSlotId').value = slot.slotId;
        };
        grid.appendChild(btn);
    });
}

function selectPassPlan(months) {
    document.getElementById('selectedPassMonths').value = months;
    document.getElementById('planCard1').classList.toggle('selected', months === 1);
    document.getElementById('planCard3').classList.toggle('selected', months === 3);
}

function updateMonthlyPassPrices() {
    const vehicleType = document.getElementById('passVehicleType').value;
    const is4W = vehicleType === '4W';

    const p1 = is4W ? 2500 : 1200;
    const p3Mo = is4W ? 2250 : 1080;
    const p3Total = is4W ? 6750 : 3240;

    document.getElementById('planPrice1').innerHTML = '₹' + p1.toLocaleString() + ' <span>/ month</span>';
    document.getElementById('planPrice3').innerHTML = '₹' + p3Mo.toLocaleString() + ' <span>/ mo (₹' + p3Total.toLocaleString() + ' Total)</span>';

    loadAvailableSlotsForMonthly();
}

async function handlePreBookSubmit(event) {
    event.preventDefault();
    const vehicleNumber = document.getElementById('bookVehicleNumber').value.trim();
    const vehicleType = document.getElementById('bookVehicleType').value;
    const scheduledEntry = document.getElementById('bookEntryTime').value;
    const durationHours = document.getElementById('bookDuration').value;
    const slotId = document.getElementById('selectedSlotId').value;

    try {
        const response = await fetch('/api/user/reservations/create', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.userId,
                vehicleNumber, vehicleType,
                scheduledEntry, durationHours, slotId
            })
        });

        const data = await response.json();
        if (data.success) {
            showUserAlert(data.message, 'success');
            showTicketModal(data.passCode, data.vehicleNumber, data.slotNumber, scheduledEntry, data.estimatedFee);
            loadSlotsData();
            loadUserTickets();
        } else {
            showUserAlert(data.message || 'Failed to complete pre-booking.', 'error');
        }
    } catch (e) {
        showUserAlert('Unable to process pre-booking. Please check connection.', 'error');
    }
}

async function handleMonthlyPassSubmit(event) {
    event.preventDefault();
    const vehicleNumber = document.getElementById('passVehicleNumber').value.trim();
    const vehicleType = document.getElementById('passVehicleType').value;
    const monthsPaid = document.getElementById('selectedPassMonths').value;
    const slotId = document.getElementById('passSelectedSlotId').value;

    try {
        const response = await fetch('/api/user/passes/subscribe', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                userId: currentUser.userId,
                vehicleNumber, vehicleType,
                monthsPaid, slotId
            })
        });

        const data = await response.json();
        if (data.success) {
            showUserAlert(data.message, 'success');
            showTicketModal(data.passCode, data.vehicleNumber, data.slotNumber, data.startDate + ' to ' + data.endDate, data.amountPaid);
            loadSlotsData();
            loadUserTickets();
        } else {
            showUserAlert(data.message || 'Failed to issue monthly pass.', 'error');
        }
    } catch (e) {
        showUserAlert('Unable to process monthly pass. Please check connection.', 'error');
    }
}

async function loadUserTickets() {
    const container = document.getElementById('userTicketsList');
    if (!currentUser) return;

    try {
        const [resResp, passResp] = await Promise.all([
            fetch('/api/user/reservations/list?userId=' + currentUser.userId),
            fetch('/api/user/passes/list?userId=' + currentUser.userId)
        ]);

        const resData = await resResp.json();
        const passData = await passResp.json();

        let html = '';

        if (resData.reservations && resData.reservations.length > 0) {
            html += '<h4 style="font-size: 16px; font-weight: 700; color: var(--accent-cyan); margin-bottom: 15px;"><i class="fa-solid fa-calendar-check mr-2"></i> Pre-Booking Reservations</h4>';
            resData.reservations.forEach(r => {
                html += `
                <div class="pass-ticket-card">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                        <div>
                            <span class="pass-code-tag">${r.passCode}</span>
                            <span class="status-pill ${r.status.toLowerCase()} ml-2">${r.status}</span>
                        </div>
                        <div style="font-size: 20px; font-weight: 800; color: #fff;">Slot ${r.slotNumber}</div>
                    </div>
                    <div style="font-size: 14px; color: var(--text-muted); display: flex; gap: 20px; flex-wrap: wrap;">
                        <div><strong>Vehicle:</strong> ${r.vehicleNumber} (${r.vehicleType})</div>
                        <div><strong>Entry:</strong> ${r.scheduledEntry}</div>
                        <div><strong>Duration:</strong> ${r.durationHours} hrs</div>
                        <div><strong>Fee:</strong> ₹${parseFloat(r.estimatedFee).toFixed(2)}</div>
                    </div>
                    ${r.status === 'CONFIRMED' ? `
                    <div style="margin-top: 15px; text-align: right;">
                        <button type="button" class="btn btn-outline-red" style="padding: 6px 16px; font-size: 12px;" onclick="cancelReservation(${r.reservationId})">
                            <i class="fa-solid fa-xmark mr-1"></i> Cancel Reservation
                        </button>
                    </div>` : ''}
                </div>`;
            });
        }

        if (passData.passes && passData.passes.length > 0) {
            html += '<h4 style="font-size: 16px; font-weight: 700; color: #a855f7; margin-top: 25px; margin-bottom: 15px;"><i class="fa-solid fa-id-card-clip mr-2"></i> Daily Commuter Monthly Passes</h4>';
            passData.passes.forEach(p => {
                html += `
                <div class="pass-ticket-card" style="border-color: #a855f7; background: linear-gradient(135deg, rgba(4, 17, 29, 0.95), rgba(168, 85, 247, 0.15));">
                    <div style="display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px;">
                        <div>
                            <span class="pass-code-tag" style="color: #d8b4fe; background: rgba(168, 85, 247, 0.2);">${p.passCode}</span>
                            <span class="status-pill active ml-2">${p.status}</span>
                        </div>
                        <div style="font-size: 20px; font-weight: 800; color: #d8b4fe;">Slot ${p.slotNumber}</div>
                    </div>
                    <div style="font-size: 14px; color: var(--text-muted); display: flex; gap: 20px; flex-wrap: wrap;">
                        <div><strong>Vehicle:</strong> ${p.vehicleNumber} (${p.vehicleType})</div>
                        <div><strong>Validity:</strong> ${p.startDate} to ${p.endDate}</div>
                        <div><strong>Months:</strong> ${p.monthsPaid} Month(s)</div>
                        <div><strong>Paid:</strong> ₹${parseFloat(p.amountPaid).toFixed(2)}</div>
                    </div>
                </div>`;
            });
        }

        if (html === '') {
            html = '<p style="color: var(--text-muted); font-size: 14px;">You have no active pre-bookings or monthly passes yet.</p>';
        }

        container.innerHTML = html;
    } catch (e) {
        container.innerHTML = '<p style="color: #ff8080; font-size: 14px;">Error loading passes.</p>';
    }
}

async function cancelReservation(resId) {
    if (!confirm('Are you sure you want to cancel this pre-booking?')) return;

    try {
        const response = await fetch('/api/user/reservations/cancel', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ reservationId: resId, userId: currentUser.userId })
        });

        const data = await response.json();
        if (data.success) {
            showUserAlert(data.message, 'success');
            loadUserTickets();
            loadSlotsData();
        } else {
            showUserAlert(data.message || 'Failed to cancel reservation.', 'error');
        }
    } catch (e) {
        showUserAlert('Unable to cancel reservation.', 'error');
    }
}

function showTicketModal(code, vehicle, slot, dateTime, fee) {
    document.getElementById('modalPassCode').textContent = code;
    document.getElementById('modalVehicle').textContent = vehicle;
    document.getElementById('modalSlot').textContent = slot;
    document.getElementById('modalDateTime').textContent = dateTime || 'Immediate';
    document.getElementById('modalFee').textContent = '₹' + parseFloat(fee).toFixed(2);
    document.getElementById('ticketModal').style.display = 'flex';
}

function closeTicketModal() {
    document.getElementById('ticketModal').style.display = 'none';
}
