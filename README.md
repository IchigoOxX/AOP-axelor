# Axelor Open Platform (AOP) & Enterprise Suite (AOS)
### Customized Edition with Native Arabic Localization, Industry Modules & Regional Tax Compliance

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Gradle](https://img.shields.io/badge/Gradle-8.x-blue.svg)](https://gradle.org/)
[![Platform](https://img.shields.io/badge/Axelor%20Platform-v8.0-green.svg)](https://axelor.com/)
[![Database](https://img.shields.io/badge/PostgreSQL-14%2B-blue.svg)](https://www.postgresql.org/)
[![Localization](https://img.shields.io/badge/Localization-Native%20Arabic%20(RTL)-emerald.svg)](#native-arabic-erp-localization)
[![E-Invoicing](https://img.shields.io/badge/E--Invoicing-Egypt%20(ETA)%20%7C%20Saudi%20Arabia%20(ZATCA)-brightgreen.svg)](#regional-tax-compliance--e-invoicing-egypt--saudi-arabia)

---

## 📌 Overview & Customizations

This repository represents an **extended, production-hardened distribution of the Axelor Open Platform (AOP v8.0) and Axelor Open Suite (AOS)**. Built on top of the robust Axelor framework, this edition features custom business logic, architecture enhancements, proprietary vertical industry solutions, full native Arabic localization, and official government e-invoicing compliance for the Middle East.

### Key Custom Additions & Modifications
* **Native Embedded Arabic Localization:** All business, operational, and financial terminology translated into professional Arabic directly inside module resource bundles (`messages_ar.csv`), conforming to IFRS and regional accounting standards without any external translation scripts.
* **Vertical Industry Modules:** Custom-built enterprise modules for **Poultry Farming & Hatchery Lifecycle**, **Livestock Herd & Dairy Operations**, and **Cloud Retail Point of Sale (POS)**.
* **Official Regional Tax Compliance:** Complete electronic invoicing integration modules for both **Egypt (Egyptian Tax Authority - ETA)** and **Saudi Arabia (ZATCA Phase 2 FATOORA)**.
* **Architecture & UI Optimizations:** Null-safe XML view templates, resolved Groovy expressions, optimized Hibernate query filters eliminating in-memory fetch overhead, and enhanced RTL layout responsiveness.

---

## 🏛️ Regional Tax Compliance & E-Invoicing (Egypt & Saudi Arabia)

The platform includes end-to-end electronic invoicing engines designed to meet official statutory requirements for tax authorities in Egypt and Saudi Arabia:

### 1. Egypt — Egyptian Tax Authority (ETA / مصلحة الضرائب المصرية)
* **Module:** `axelor-egypt-einvoice`
* **Direct Integration:** Seamless connection to the official **Egyptian Tax Authority (ETA)** e-Invoicing and e-Receipt API portal.
* **Document Mapping:** Automatically serializes standard Axelor sales invoices, credit notes, and debit notes into ETA canonical JSON / XML schemas (v1.0 & v0.9).
* **Digital Signatures:** Supports digital signature signing with USB hardware tokens (ePass2003 / Egypt Trust / Misr clearing) and HSM servers using CAdES-BES standard.
* **Submission Lifecycle:** Real-time invoice submission, status polling (Valid, Invalid, Rejected), UUID retrieval, cancellation management, and comprehensive audit tracking via `EtaSubmissionLog`.

### 2. Saudi Arabia — Zakat, Tax and Customs Authority (ZATCA / هيئة الزكاة والضريبة والجمارك)
* **Module:** `axelor-zatca-einvoice`
* **ZATCA Phase 2 (FATOORA Integration):** Full compliance with Saudi Arabia's mandatory integration phase for Standard Tax Invoices (B2B) and Simplified Tax Invoices (B2C).
* **Cryptographic Standards:** Cryptographic invoice hash generation (SHA-256), cryptographic stamp (ECDSA with secp256k1 curve), and embedded X.509 digital certificates.
* **UBL 2.1 XML Engine:** Produces fully compliant UBL 2.1 XML documents validated against official ZATCA Schematron business rules.
* **Clearance & Reporting:** Direct API connection for real-time B2B invoice clearance and B2C reporting within mandated statutory timelines.
* **ZATCA QR Code:** Generates TLV-encoded Base64 QR codes strictly adhering to ZATCA technical specifications.

---

## 🌟 Custom Industry Enterprise Modules

### 1. `gl-poultry-farming` — Poultry Farm & Hatchery Management
* **Flock & Center Management:** Multi-center tracking for brooding/starter flocks (تحضين), commercial broilers (بداري تسمين), and parent stock / reproduction (قطيع الأمهات وإنتاج بيض المفرخ).
* **Daily Technical Sheets:** Daily recording of flock mortality, feed consumption, water intake, average bird weights, Feed Conversion Ratio (FCR), ambient temperatures, humidity, and darkness/lighting cycles.
* **Incubators & Hatchery Cycles:** Complete operational cycle tracking including egg collection, classification/sorting (OAC, OD, ODJ, RE), setter trolleys loading, hatcher transfers, and chick dispatch.
* **Veterinary Programs:** Automated vaccination schedules and medical treatment logs mapped to flock daily and weekly age.
* **Live KPI Dashboards:** Interactive dashboard cards providing real-time visibility into shed occupancy, active flocks, mortality rates, and feed inventory.

### 2. `gl-livestock-farming` — Herd, Dairy & Feedlot Management
* **Herd & Animals Register:** Individual animal tracking with ear tag identification, pedigree records, breed categorizations, and life-cycle status.
* **Daily Milk Production:** Session-by-session milking logs (morning, afternoon, evening), milk volume recording, fat/protein quality tests, and tank refrigeration tracking.
* **Fattening & Weight Monitoring:** Automated weight logs, average daily gain (ADG), and nutrition efficiency monitoring for feedlot cattle.
* **Breeding & Reproduction:** Heat cycle detection, artificial insemination (AI) logs, ultrasound pregnancy diagnosis, and expected calving calendars.
* **Veterinary Care & Treatments:** Preventive vaccinations, clinical visits, quarantine management, and drug withdrawal tracking.

### 3. `gl-pos` — Cloud Point of Sale (POS)
* Fast, touchscreen-friendly cashier interface supporting barcode scanning and instant item search.
* Session and shift management with cash opening balances, cash drops, and end-of-day reconciliation.
* Customer loyalty program engine with reward points, customer tiers, and physical/digital loyalty cards.

### 4. Enterprise Integrations
* **`axelor-teamsapp`:** Deep integration with Microsoft Teams for workflow notifications, task assignments, and executive approval flows.
* **`axelor-docusign`:** Electronic document signature workflows for sales agreements, purchase orders, and vendor contracts.

---

## 🌐 Native Arabic ERP Localization (التعريب الكامل المدمج)

* **100% Native Resource Bundles:** Every module (both the 28+ core Axelor Open Suite modules and custom modules) contains its complete Arabic translation natively packaged under `src/main/resources/i18n/messages_ar.csv`.
* **Zero External Dependencies:** No external scripts, database patches, or manual intervention required. Translations are compiled and served natively by the Axelor platform engine.
* **Standardized ERP Terminology:** Formatted with standardized Arabic accounting and business terms conforming to International Financial Reporting Standards (IFRS).
* **RTL Ready:** Seamless Right-to-Left (RTL) interface presentation with optimized styling for Arabic typography.

---

## 🛠️ Technology Stack

* **Backend Engine:** Java 21 LTS, Google Guice (Dependency Injection), Hibernate 6 / JPA.
* **Web Layer:** JAX-RS (Jakarta REST), JSON-RPC, Embedded Apache Tomcat.
* **Frontend:** React-based declarative UI generated from XML object views.
* **Build System:** Gradle 8.x + `com.axelor.app` plugin.
* **Database:** PostgreSQL 14+ (Full support for JSONB, Sequences, and Full-Text Search).

---

## 🚀 Quick Start & Setup

### Prerequisites
1. **Java JDK 21 LTS** installed and configured (`JAVA_HOME` pointing to JDK 21).
2. **PostgreSQL 14+** running locally or remotely with a database named `my_custom_system`.

### 1. Database Configuration
Configure database connection parameters in `src/main/resources/axelor-config.properties`:
```properties
db.default.driver = org.postgresql.Driver
db.default.url = jdbc:postgresql://localhost:5432/my_custom_system
db.default.user = postgres
db.default.password = YOUR_PASSWORD
db.default.ddl = update
```

### 2. Build the Project
```powershell
# Windows
.\gradlew.bat build -x test

# Linux / macOS
./gradlew build -x test
```

### 3. Run the Application
```powershell
# Windows
.\gradlew.bat run

# Linux / macOS
./gradlew run
```
Access the web portal at: `http://localhost:8080`
* **Default Username:** `admin`
* **Default Password:** `admin`

---

## 📁 Media & Demo Assets

The repository includes standard enterprise demonstration assets under the `upload/` directory:
* Company logos, document headers, and report templates.
* Sample product images and barcodes for POS and inventory testing.
* Preconfigured sample data packaged in standard Axelor CSV module formats.

---

## 📄 License

Axelor Open Suite is distributed under the GNU Affero General Public License (AGPLv3) / Enterprise License.
Custom vertical modules (`gl-poultry-farming`, `gl-livestock-farming`, `gl-pos`, `axelor-egypt-einvoice`, `axelor-zatca-einvoice`) are developed for specialized industry workflows.
