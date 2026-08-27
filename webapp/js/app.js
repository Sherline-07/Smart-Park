/**
 * Smart Park - Main Frontend Application Logic
 * Full-Stack integration with Java Backend & MySQL Database
 */

// Global State
let allSlots = [];
let allHistory = [];
let activeVehicles = [];
let currentSlotFilter = 'ALL';

document.addEventListener("DOMContentLoaded", () => {
    // 1. Check Authentication Guard
    const workerJson = localStorage.getItem("smartpark_worker");
    if (!workerJson) {
        window.location.href = "login.html";
        return;
    }

    try {
        const worker = JSON.parse(workerJson);
        const nameDisplay = document.getElementById("workerNameDisplay");
        if (nameDisplay && worker.fullName) {
            nameDisplay.innerText = worker.fullName;
        }
    } catch (e) {
        console.error("Worker session parse error:", e);
    }

    // 2. Initial Data Load
    refreshDashboard();
    fetchSlots();

    // 3. Setup Form Event Listeners
    setupEntryForm();
    setupExitForm();

    // 4. Periodic Background Refresh (every 10 seconds)
    setInterval(() => {
        const activeTab = document.querySelector(".nav-link.active")?.dataset.tab;
        if (activeTab === "dashboard") {
            refreshDashboard();
        } else if (activeTab === "slots") {
            fetchSlots();
        }
    }, 10000);
});

/* =========================================================
   TAB SWITCHING
   ========================================================= */
function switchTab(tabId) {
    document.querySelectorAll(".nav-link").forEach(btn => {
        btn.classList.toggle("active", btn.dataset.tab === tabId);
    });

    document.querySelectorAll(".tab-content").forEach(tab => {
        tab.classList.toggle("active", tab.id === `tab-${tabId}`);
    });

    if (tabId === "dashboard") {
        refreshDashboard();
    } else if (tabId === "slots") {
        fetchSlots();
    } else if (tabId === "history") {
        fetchHistory();
    } else if (tabId === "exit") {
        fetchActiveVehicles();
    }
}

/* =========================================================
   DASHBOARD METRICS & LIVE DATA
   ========================================================= */
async function refreshDashboard() {
    await Promise.all([
        fetchDashboardStats(),
        fetchActiveVehicles()
    ]);
}

async function fetchDashboardStats() {
    try {
        const res = await fetch("/api/dashboard/stats");
        const result = await res.json();

        if (res.ok && result.success) {
            const data = result.data;
            
            // Update Top Counters
            setText("statTotalSlots", data.totalSlots);
            setText("statAvailableSlots", data.availableSlots);
            setText("statOccupiedSlots", data.occupiedSlots);
            setText("statOccupancyRate", `${data.occupancyPercentage}%`);
            setText("meterPercentageText", `${data.occupancyPercentage}% (${data.occupiedSlots}/${data.totalSlots} Slots)`);

            // Category Breakdown
            setText("stat4WTotal", `${data.total4W} Total`);
            setText("stat4WAvail", data.available4W);
            setText("stat4WOcc", data.occupied4W);

            setText("stat2WTotal", `${data.total2W} Total`);
            setText("stat2WAvail", data.available2W);
            setText("stat2WOcc", data.occupied2W);

            // Progress Meter
            const meterFill = document.getElementById("occupancyProgressBar");
            if (meterFill) {
                meterFill.style.width = `${Math.min(100, data.occupancyPercentage)}%`;
                meterFill.classList.toggle("warning", data.occupancyPercentage > 80);
            }

            // Dynamic Surge Banner
            const surgeBanner = document.getElementById("dynamicPricingBanner");
            if (surgeBanner) {
                surgeBanner.classList.toggle("active", data.dynamicPricingActive);
            }
        }
    } catch (err) {
        console.error("Failed to fetch dashboard stats:", err);
    }
}

async function fetchActiveVehicles() {
    try {
        const res = await fetch("/api/records/active");
        const result = await res.json();

        if (res.ok && result.success) {
            activeVehicles = result.records || [];
            renderActiveVehiclesTable(activeVehicles);
            populateActiveDatalist(activeVehicles);
        }
    } catch (err) {
        console.error("Failed to fetch active vehicles:", err);
    }
}

function renderActiveVehiclesTable(records) {
    const tbody = document.getElementById("activeVehiclesTableBody");
    const countBadge = document.getElementById("activeVehicleCountBadge");
    if (countBadge) countBadge.innerText = `${records.length} Vehicles Active`;

    if (!tbody) return;

    if (records.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="5" style="text-align:center; color:var(--text-muted); padding:35px;">
                    <i class="fa-solid fa-square-parking" style="font-size:24px; margin-bottom:8px; display:block; opacity:0.5;"></i>
                    No vehicles currently parked. All slots ready for entry.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = records.map(r => `
        <tr>
            <td><span class="plate-badge">${escapeHtml(r.vehicleNumber)}</span></td>
            <td>
                <span class="type-pill ${r.vehicleType === '4W' ? 'type-4w' : 'type-2w'}">
                    <i class="fa-solid ${r.vehicleType === '4W' ? 'fa-car' : 'fa-motorcycle'}"></i> ${r.vehicleType}
                </span>
            </td>
            <td><strong style="color:var(--accent-cyan); font-size:15px;">${escapeHtml(r.slotNumber)}</strong></td>
            <td><span style="color:var(--text-muted); font-size:13px;">${escapeHtml(r.entryTime)}</span></td>
            <td>
                <button onclick="quickExit('${escapeHtml(r.vehicleNumber)}')" class="btn-logout" style="padding:4px 12px; font-size:12px; border-radius:15px;">
                    <i class="fa-solid fa-receipt"></i> Exit & Bill
                </button>
            </td>
        </tr>
    `).join("");
}

function populateActiveDatalist(records) {
    const datalist = document.getElementById("activeVehiclesDatalist");
    if (!datalist) return;
    datalist.innerHTML = records.map(r => `<option value="${r.vehicleNumber}">Slot: ${r.slotNumber} (${r.vehicleType})</option>`).join("");
}

/* =========================================================
   TAB 2: VEHICLE ENTRY
   ========================================================= */
function selectEntryCategory(type) {
    document.getElementById("entryVehicleType").value = type;
    document.getElementById("catCard4W").classList.toggle("selected", type === "4W");
    document.getElementById("catCard2W").classList.toggle("selected", type === "2W");
}

function setupEntryForm() {
    const form = document.getElementById("vehicleEntryForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const vehicleNumber = document.getElementById("entryVehicleNo").value.trim().toUpperCase();
        const vehicleType = document.getElementById("entryVehicleType").value;

        if (!vehicleNumber) {
            showToast("Please enter a vehicle registration number.", "error");
            return;
        }

        const submitBtn = document.getElementById("btnSubmitEntry");
        submitBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Allocating Slot...`;
        submitBtn.disabled = true;

        try {
            const res = await fetch("/api/vehicle/entry", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ vehicleNumber, vehicleType })
            });

            const data = await res.json();

            if (res.ok && data.success) {
                showToast(data.message || "Vehicle parked successfully!", "success");
                form.reset();
                selectEntryCategory("4W"); // reset to default

                // Show confirmation modal
                showEntrySuccessModal(data.data);

                // Refresh dashboard & slot visualizer
                refreshDashboard();
                fetchSlots();
            } else {
                showToast(data.message || "Failed to check-in vehicle.", "error");
            }
        } catch (err) {
            console.error("Entry error:", err);
            showToast("Server communication failure.", "error");
        } finally {
            submitBtn.innerHTML = `<i class="fa-solid fa-key"></i> Allocate Slot & Confirm Entry`;
            submitBtn.disabled = false;
        }
    });
}

function showEntrySuccessModal(data) {
    setText("modalEntryPlate", data.vehicleNumber);
    setText("modalEntryType", data.vehicleType === "4W" ? "Four Wheeler (Car)" : "Two Wheeler (Bike)");
    setText("modalEntrySlot", data.slotNumber);
    setText("modalEntryTime", data.entryTime);
    openModal("entrySuccessModal");
}

/* =========================================================
   TAB 3: VEHICLE EXIT & BILLING
   ========================================================= */
function setupExitForm() {
    const form = document.getElementById("vehicleExitForm");
    if (!form) return;

    form.addEventListener("submit", async (e) => {
        e.preventDefault();
        const vehicleNumber = document.getElementById("exitVehicleNo").value.trim().toUpperCase();

        if (!vehicleNumber) {
            showToast("Please enter or select a vehicle plate number.", "error");
            return;
        }

        await processExit(vehicleNumber);
    });
}

function quickExit(plate) {
    switchTab("exit");
    document.getElementById("exitVehicleNo").value = plate;
    processExit(plate);
}

async function processExit(vehicleNumber) {
    const submitBtn = document.getElementById("btnSubmitExit");
    if (submitBtn) {
        submitBtn.innerHTML = `<i class="fa-solid fa-spinner fa-spin"></i> Calculating Bill...`;
        submitBtn.disabled = true;
    }

    try {
        const res = await fetch("/api/vehicle/exit", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ vehicleNumber })
        });

        const data = await res.json();

        if (res.ok && data.success) {
            showToast(data.message || "Bill calculated successfully!", "success");
            document.getElementById("vehicleExitForm")?.reset();

            // Render Invoice Slip in Modal
            showExitReceiptModal(data.receipt);

            // Refresh state
            refreshDashboard();
            fetchSlots();
            fetchHistory();
        } else {
            showToast(data.message || "Failed to process exit.", "error");
        }
    } catch (err) {
        console.error("Exit processing error:", err);
        showToast("Error processing checkout with database.", "error");
    } finally {
        if (submitBtn) {
            submitBtn.innerHTML = `<i class="fa-solid fa-calculator"></i> Calculate Duration & Generate Bill`;
            submitBtn.disabled = false;
        }
    }
}

function showExitReceiptModal(receipt) {
    setText("receiptBillId", `Invoice #${receipt.billId}`);
    setText("receiptPlate", receipt.vehicleNumber);
    setText("receiptCategory", receipt.vehicleType === "4W" ? "Four Wheeler (4W)" : "Two Wheeler (2W)");
    setText("receiptSlot", receipt.slotNumber);
    setText("receiptEntryTime", receipt.entryTime);
    setText("receiptExitTime", receipt.exitTime);
    setText("receiptDuration", `${receipt.durationFormatted} (${receipt.durationMinutes} min)`);
    setText("receiptBillableHours", `${receipt.billableHours} Hour${receipt.billableHours > 1 ? 's' : ''}`);
    setText("receiptRate", `₹${receipt.hourlyRate}/hr`);
    setText("receiptTotalAmount", `₹${receipt.totalAmount.toFixed(2)}`);

    const surgeEl = document.getElementById("receiptSurgeNotice");
    if (surgeEl) {
        surgeEl.style.display = receipt.dynamicPricingApplied ? "block" : "none";
    }

    openModal("exitReceiptModal");
}

/* =========================================================
   TAB 4: PARKING SLOTS VISUAL GRID
   ========================================================= */
async function fetchSlots() {
    try {
        const res = await fetch("/api/slots");
        const result = await res.json();

        if (res.ok && result.success) {
            allSlots = result.slots || [];
            updateSlotCounters(allSlots);
            renderSlotsGrid();
        }
    } catch (err) {
        console.error("Failed to fetch slots:", err);
    }
}

function updateSlotCounters(slots) {
    setText("countFilterAll", slots.length);
    setText("countFilter4W", slots.filter(s => s.vehicleType === "4W").length);
    setText("countFilter2W", slots.filter(s => s.vehicleType === "2W").length);
}

function filterSlots(category) {
    currentSlotFilter = category;
    document.querySelectorAll(".slot-filters .filter-btn").forEach(btn => {
        btn.classList.remove("active");
        if (btn.innerText.includes(category) || (category === "ALL" && btn.innerText.includes("All"))) {
            btn.classList.add("active");
        }
    });
    renderSlotsGrid();
}

function renderSlotsGrid() {
    const container = document.getElementById("slotsGridContainer");
    if (!container) return;

    let filtered = allSlots;
    if (currentSlotFilter === "4W" || currentSlotFilter === "2W") {
        filtered = allSlots.filter(s => s.vehicleType === currentSlotFilter);
    } else if (currentSlotFilter === "AVAILABLE" || currentSlotFilter === "OCCUPIED" || currentSlotFilter === "RESERVED" || currentSlotFilter === "MONTHLY_PASS") {
        filtered = allSlots.filter(s => s.status === currentSlotFilter);
    }

    if (filtered.length === 0) {
        container.innerHTML = `<div style="grid-column:1/-1; text-align:center; padding:30px; color:var(--text-muted);">No slots found matching the selected filter.</div>`;
        return;
    }

    container.innerHTML = filtered.map(slot => `
        <div class="slot-box ${slot.status}">
            <div class="slot-cat-badge">
                <i class="fa-solid ${slot.vehicleType === '4W' ? 'fa-car' : 'fa-motorcycle'}"></i> ${slot.vehicleType} • Floor ${slot.floorLevel}
            </div>
            <div class="slot-number">${escapeHtml(slot.slotNumber)}</div>
            <span class="slot-status-pill">${slot.status}</span>
        </div>
    `).join("");
}

/* =========================================================
   TAB 5: PARKING HISTORY
   ========================================================= */
async function fetchHistory() {
    try {
        const res = await fetch("/api/records/history");
        const result = await res.json();

        if (res.ok && result.success) {
            allHistory = result.records || [];
            renderHistoryTable(allHistory);
        }
    } catch (err) {
        console.error("Failed to fetch history:", err);
    }
}

function filterHistoryTable() {
    const query = (document.getElementById("historySearchInput")?.value || "").trim().toUpperCase();
    const filtered = allHistory.filter(h => h.vehicleNumber.includes(query) || h.slotNumber.includes(query));
    renderHistoryTable(filtered);
}

function renderHistoryTable(records) {
    const tbody = document.getElementById("historyTableBody");
    if (!tbody) return;

    if (records.length === 0) {
        tbody.innerHTML = `
            <tr>
                <td colspan="9" style="text-align:center; color:var(--text-muted); padding:35px;">
                    <i class="fa-solid fa-inbox" style="font-size:24px; margin-bottom:8px; display:block; opacity:0.5;"></i>
                    No completed parking transactions recorded yet.
                </td>
            </tr>
        `;
        return;
    }

    tbody.innerHTML = records.map(r => `
        <tr>
            <td><strong style="color:var(--text-muted);">#${r.recordId}</strong></td>
            <td><span class="plate-badge">${escapeHtml(r.vehicleNumber)}</span></td>
            <td><span class="type-pill ${r.vehicleType === '4W' ? 'type-4w' : 'type-2w'}">${r.vehicleType}</span></td>
            <td><strong style="color:var(--accent-cyan);">${escapeHtml(r.slotNumber)}</strong></td>
            <td style="font-size:12px; color:var(--text-muted);">${escapeHtml(r.entryTime)}</td>
            <td style="font-size:12px; color:var(--text-muted);">${escapeHtml(r.exitTime)}</td>
            <td>${r.durationMinutes} min (${r.billableHours} hr)</td>
            <td>
                ₹${r.hourlyRate}/hr
                ${r.dynamicPricingApplied ? '<span class="surge-tag" style="margin-left:4px;">Surge</span>' : ''}
            </td>
            <td><strong class="price-text">₹${r.totalAmount.toFixed(2)}</strong></td>
        </tr>
    `).join("");
}

/* =========================================================
   UI HELPERS & MODALS
   ========================================================= */
function openModal(id) {
    document.getElementById(id)?.classList.add("active");
}

function closeModal(id) {
    document.getElementById(id)?.classList.remove("active");
}

function showToast(message, type = "success") {
    const container = document.getElementById("toastContainer");
    if (!container) return;

    const toast = document.createElement("div");
    toast.className = `toast ${type}`;
    toast.innerHTML = `
        <i class="fa-solid ${type === 'success' ? 'fa-circle-check' : 'fa-circle-exclamation'}"></i>
        <span>${escapeHtml(message)}</span>
    `;

    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = "0";
        toast.style.transform = "translateY(10px)";
        toast.style.transition = "all 0.3s ease";
        setTimeout(() => toast.remove(), 300);
    }, 4000);
}

function setText(id, text) {
    const el = document.getElementById(id);
    if (el) el.innerText = text;
}

function escapeHtml(str) {
    if (!str) return "";
    return String(str)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

function logoutWorker() {
    localStorage.removeItem("smartpark_worker");
    window.location.href = "login.html";
}
