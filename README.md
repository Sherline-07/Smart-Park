# 🅿️ Smart Park — Smart Parking and Billing System
### Worker & Admin Management System (Full-Stack Java + JDBC + MySQL)

Welcome to **Smart Park** — an automated smart parking and dynamic billing management system designed for parking operators and facility administrators.

---

## 🏗️ 1. Project Architecture

The application follows a clean, modular **MVC (Model-View-Controller) / DAO architecture** tailored for 2nd-year CSE college projects:

```
[ Frontend: HTML5 / CSS3 / JavaScript (Cyberpunk Dark Neon) ]
                        ↓ (HTTP REST JSON)
[ Controllers: AuthController, DashboardController, EntryController, ExitController, SlotController, HistoryController ]
                        ↓
[ Service Layer: ParkingService (Validation, Atomic Slot Allocation) & BillingService (Surge Pricing Engine) ]
                        ↓
[ DAO Layer: WorkerDAO, ParkingSlotDAO, ParkingRecordDAO (PreparedStatements) ]
                        ↓ (JDBC Connection)
[ MySQL Database: smart_parking (Tables: workers, parking_slots, parking_records) ]
```

---

## 📋 2. System Requirements

- **Java Development Kit (JDK)**: Java 17 or Java 21+ (Java 21 LTS pre-configured)
- **Database**: MySQL Server 8.0+ / MariaDB / XAMPP (or built-in standalone fallback mode)
- **JDBC Driver**: `mysql-connector-j-8.3.0.jar` (included in `lib/`)
- **Web Browser**: Chrome, Edge, Firefox, or Brave

---

## 🗄️ 3. Database Setup (MySQL)

### Step 1: Open MySQL Command Line or phpMyAdmin
Log into your MySQL instance:
```bash
mysql -u root -p
```

### Step 2: Execute the Setup Script
Run the provided SQL script located in `database/smart_parking.sql`:
```sql
SOURCE C:/path/to/smart-parking/database/smart_parking.sql;
```
*Or copy and paste the contents of `database/smart_parking.sql` into MySQL Workbench / phpMyAdmin SQL tab.*

### Database Tables Created:
1. **`workers`**: Stores worker/admin accounts (`worker_id`, `username`, `password`, `full_name`, `role`).
2. **`parking_slots`**: 30 facility slots (15 Four-Wheeler `A-01` to `A-15`, 15 Two-Wheeler `B-01` to `B-15`).
3. **`parking_records`**: Parking transactions (`record_id`, `vehicle_number`, `vehicle_type`, `slot_id`, `entry_time`, `exit_time`, `duration_minutes`, `billable_hours`, `hourly_rate`, `dynamic_pricing_applied`, `total_amount`, `status`).

### Default Worker Credentials:
| Username | Password | Role | Full Name |
|---|---|---|---|
| `admin` | `admin123` | Administrator | System Administrator |
| `worker1` | `worker123` | Operator | Ramesh Kumar (Operator) |
| `worker2` | `worker123` | Operator | Suresh Raina (Operator) |

---

## ⚙️ 4. Backend Configuration (`config/db.properties`)

Edit `config/db.properties` if your MySQL username or password differs from default (`root`/`root`):

```properties
# MySQL Connection
db.driver=com.mysql.cj.jdbc.Driver
db.url=jdbc:mysql://localhost:3306/smart_parking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
db.user=root
db.password=root

# Base Hourly Rates (INR)
pricing.2w.base=20.0
pricing.4w.base=40.0

# Dynamic Surge Pricing (When Occupancy > 80%)
pricing.occupancy.threshold=80.0
pricing.2w.surge=30.0
pricing.4w.surge=60.0

# Web Server Port
server.port=8080
```

> **Note**: If MySQL is not running yet during an offline demonstration, the system automatically activates its embedded fallback database so you can demo the system without interruption!

---

## 🚀 5. How to Run the Application

### Option A: One-Click Execution (Windows)
1. Double-click `compile.bat` (compiles Java classes into `bin/`).
2. Double-click `run.bat` (starts the server on `http://localhost:8080`).

### Option B: Command Line (PowerShell / CMD)
```powershell
# 1. Compile
javac -encoding UTF-8 -cp "lib/*" -d "bin" (Get-ChildItem -Path "src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName)

# 2. Run
java -cp "bin;lib/*" com.smartpark.Main
```

### Option C: Open in Browser
Visit:
- **Login Portal**: [http://localhost:8080/login.html](http://localhost:8080/login.html)
- **Worker Dashboard**: [http://localhost:8080/index.html](http://localhost:8080/index.html)

---

## 🧪 6. Step-by-Step Live Demonstration Guide

### Demo 1 — Worker Login
1. Open `http://localhost:8080/login.html`.
2. Try an invalid password to demonstrate error handling.
3. Enter `admin` and `admin123`.
4. Click **Login** $\rightarrow$ Successfully redirects to the Worker Dashboard.

### Demo 2 — Live Real-Time Dashboard
1. Notice the real metrics fetched from the database:
   - **Total Slots**: 30
   - **Available Slots**: 30
   - **Occupied Slots**: 0
   - **Occupancy Rate**: 0.0%
   - **Dynamic Pricing**: Inactive (Standard Rates: 4W = ₹40/hr, 2W = ₹20/hr)

### Demo 3 — Vehicle Entry & Automated Slot Allocation
1. Navigate to **Vehicle Entry** tab.
2. Enter Vehicle Plate: `TN01AB1234`.
3. Select **Four Wheeler (4W)**.
4. Click **Allocate Slot & Confirm Entry**.
5. **Observation**:
   - System automatically allocates slot `A-01`.
   - Entry timestamp is stored in MySQL.
   - Dashboard instantly updates: Available drops to 29, Occupied increases to 1.
   - Slot `A-01` in the **Parking Slots** grid turns red (`OCCUPIED`).
6. **Duplicate Check**: Try entering `TN01AB1234` again $\rightarrow$ System warns: *"Vehicle TN01AB1234 is already parked in Slot A-01"*.

### Demo 4 — Dynamic Surge Pricing (>80% Occupancy)
1. Check in vehicles until occupied slots exceed 24 (80% of 30 slots).
2. **Observation**:
   - Occupancy exceeds 80%.
   - **High Occupancy Surge Banner** glows on top of dashboard.
   - 4W rate changes to **₹60/hr** and 2W rate changes to **₹30/hr**.

### Demo 5 — Vehicle Exit & Automated Billing Slip
1. Navigate to **Vehicle Exit & Bill** tab.
2. Select or type `TN01AB1234`.
3. Click **Calculate Duration & Generate Bill**.
4. **Observation**:
   - System calculates exact duration in minutes.
   - Applies the **rounding rule**: $\le 1\text{ hr} = 1\text{ hr}$; any additional fraction rounds up to next full hour.
   - Generates an itemized **Invoice Slip Modal** showing Bill ID, Vehicle Plate, Category, Slot, Entry Time, Exit Time, Duration, Billable Hours, Rate, and Total Amount.
   - Slot `A-01` is released and immediately turns green (`AVAILABLE`).
   - Transaction is saved and appears in the **History** tab.

---

## 💡 7. Viva & Project Presentation Q&A

### Q1: Why did you use `PreparedStatement` instead of `Statement`?
> **Answer**: `PreparedStatement` pre-compiles SQL queries on the database server, drastically improving query performance and completely eliminating SQL Injection attacks because user parameters are sent separately as typed values rather than concatenated strings.

### Q2: How does the Automatic Slot Allocation work?
> **Answer**: When a vehicle checks in, `ParkingSlotDAO.getAvailableSlot(vehicleType)` performs `SELECT slot_id, slot_number FROM parking_slots WHERE vehicle_type = ? AND status = 'AVAILABLE' ORDER BY slot_id ASC LIMIT 1`. This atomically grabs the lowest available slot compatible with the vehicle type without human bias.

### Q3: How is Dynamic Pricing calculated?
> **Answer**: The system computes $\text{Occupancy \%} = \frac{\text{Occupied Slots}}{\text{Total Slots}} \times 100$. When this value $> 80\%$, the surge multiplier activates (4W: ₹60/hr, 2W: ₹30/hr). The flag `dynamic_pricing_applied` is permanently stored with the completed invoice for auditing.

### Q4: How are fractional parking durations billed?
> **Answer**: The system applies standard municipal parking ceil rounding:
> $$\text{billable\_hours} = \max\left(1, \left\lceil \frac{\text{duration\_minutes}}{60.0} \right\rceil\right)$$
> For instance, 1 hour 15 minutes counts as 2 billable hours.

---

## 📁 8. Project Directory Structure

```
smart-parking/
├── bin/                                # Compiled Java bytecode (.class files)
├── config/
│   └── db.properties                  # Database credentials & pricing rates
├── database/
│   └── smart_parking.sql              # MySQL DDL, DML, slots seed & initial accounts
├── lib/
│   ├── mysql-connector-j-8.3.0.jar     # MySQL JDBC Driver
│   └── h2-2.2.224.jar                 # Standalone fallback driver
├── src/
│   └── com/smartpark/
│       ├── Main.java                  # Main application startup
│       ├── model/                     # Entity models (Worker, ParkingSlot, ParkingRecord, BillingReceipt, DashboardStats)
│       ├── dao/                       # JDBC Data Access Objects (DBConnection, WorkerDAO, ParkingSlotDAO, ParkingRecordDAO)
│       ├── service/                   # Business logic (ParkingService, BillingService)
│       ├── controller/                # REST API handlers (Auth, Dashboard, Entry, Exit, Slots, History)
│       ├── server/                    # AppServer HTTP static & API router
│       ├── util/                      # JsonUtil, DateTimeUtil, DBConfig
│       └── test/                      # SystemIntegrationTest (automated test suite)
├── webapp/
│   ├── index.html                     # Full Worker/Admin Control Dashboard
│   ├── login.html                     # Worker Login Page
│   ├── css/
│   │   ├── parking.css                # Base Dark-Neon Theme
│   │   └── admin.css                  # Dashboard, slots visualizer, invoice slip styles
│   └── js/
│       ├── login.js                   # Authentication & Session script
│       └── app.js                     # Real-time state, forms, modals & REST API integration
├── compile.bat / compile.ps1          # One-click build script
├── run.bat / run.ps1                  # One-click run script
└── README.md                          # Full documentation
```
