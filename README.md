# Katalon Studio Custom PDF Report Engine

Engine pelapor kustom (*custom report engine*) berbasis **Node.js (jsPDF)** dan **Katalon Studio (Groovy)** untuk menghasilkan laporan eksekusi pengujian otomatis (*automated testing report*) berkelas *Enterprise*. 

Engine ini dirancang khusus untuk mendukung pengujian skala besar (*multiple test cases* melalui Test Suite) serta memiliki kecerdasan **Hybrid Layout** yang mampu mengatur tata letak tangkapan layar perangkat Web (Landscape) maupun Mobile (Portrait) secara otomatis tanpa merusak rasio gambar (*anti-gepeng*).

---

## 🚀 Fitur Utama

* **Zero-Dependency Executive Executable (`.exe`)**: Dikompilasi menggunakan `pkg` sehingga dapat dijalankan langsung di mesin mana pun (misal: Windows Runner / CI-CD pipeline) tanpa perlu menginstal Node.js secara lokal.
* **Corporate Color Palette Theme**: Desain profesional minimalis menggunakan tema warna biru *navy* elegan, lengkap dengan *global header* dan *footer* (penomoran halaman dinamis otomatis di luar halaman Cover).
* **Table of Contents (TOC) Dinamis Multi-Halaman**: Otomatis menghitung kedalaman halaman laporan, menggunakan penanda titik-titik klasik (*dot leaders*) menuju nomor halaman, serta aman dari *bug text-overlapping* saat daftar bab meluber lebih dari 1 halaman.
* **Smart Dashboard Execution Card**: Dilengkapi metrik visual modern berbentuk *rounded cards* untuk merangkum jumlah status pengujian (**TOTAL**, **PASSED**, dan **FAILED**).
* **Smart Screenshot Vertical Auto-Scaling**: Otomatis mendeteksi dimensi asli gambar. Untuk tangkapan layar *mobile device* (portrait), engine akan menyusun **2 gambar secara vertikal (atas-bawah) per halaman** dan memposisikannya tepat di tengah halaman (*center aligned*).
* **Clean Layout Extension**: Kepala tabel (*table head*) detail eksekusi otomatis hanya muncul di halaman pertama saja (*show head on first page*) untuk mencegah visual yang terlalu rapat dan tumpang tindih dengan garis pembatas atas.

---

## 🛠️ Alur Arsitektur Sistem

```text
[Katalon Test Suite] 
       │
       ▼
[addTestResult() / Capture Screenshot] ──► Disimpan ke Memori Sementara (List<Map>)
       │
       ▼ (Setelah seluruh Test Case selesai)
[ReportingListener (AfterTestSuite)]  ──► Menghasilkan 'result.json'
       │
       ▼
[portable_report.exe (CLI Trigger)]   ──► Membaca JSON & Menghasilkan Dokumen PDF Premium
