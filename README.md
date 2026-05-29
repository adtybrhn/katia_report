# Katia-Report

## 📖 Penjelasan Proyek
**Katia-Report** adalah sistem pelaporan otomatis (*automated reporting system*) independen yang dirancang untuk mengubah data hasil pengujian Katalon Studio menjadi dokumen PDF profesional berkelas *Enterprise*.

Proyek ini dibangun menggunakan **Node.js (jsPDF)** dan bertindak sebagai jembatan eksternal dari Katalon. Sistem ini sangat cocok untuk:
* **Pengujian Skala Besar**: Dirancang khusus untuk membaca hasil dari banyak *Test Case* sekaligus dalam satu *Test Suite*.
* **Pengujian Multi-Platform (Hybrid)**: Memiliki kecerdasan tata letak otomatis (*auto-scaling*) yang mendeteksi rasio gambar. Tangkapan layar web (landscape) akan dirender penuh, sementara tangkapan layar *mobile device* (portrait) akan dirender atas-bawah secara proporsional agar muat 2 gambar per halaman tanpa membuat gambar menjadi gepeng.
* **Standar Laporan Profesional**: Dilengkapi *Table of Contents* (Daftar Isi) dinamis anti-tabrak, *dashboard summary*, desain kop minimalis, serta penomoran halaman otomatis.

---

## 🚀 Fitur Utama
* **Plug-and-Play Executable (`.exe`)**: Engine telah dikompilasi menjadi *file* `.exe` yang siap pakai. Mesin *runner* (seperti komputer rekan tim atau server CI/CD) sama sekali tidak perlu menginstal Node.js atau melakukan konfigurasi *environment*.
* **Smart Layout Engine**: Secara dinamis menghitung tinggi teks dan gambar untuk mencegah baris yang terpotong ke halaman berikutnya.
* **Corporate Theme**: Menggunakan palet warna profesional (Navy Blue dan Charcoal) dengan garis batas yang rapi.

---

## 🛠️ Step-by-Step Cara Penggunaan

### Tahap 1: Setup Engine
1. Buat folder baru bernama `katia-report` di dalam direktori proyek Katalon Anda.
2. Salin *file* `katia-report.exe` yang ada di repositori ini, lalu letakkan ke dalam folder tersebut.

### Tahap 2: Gunakan Helper Class (KatiaReporter)
Untuk mempermudah integrasi, Anda tidak perlu menulis kode dari awal. Cukup **salin *file* `KatiaReporter.groovy`** dari repositori ini, lalu letakkan ke dalam folder `Include/scripts/groovy/(default package)` pada proyek Katalon Anda.

**Penjelasan Fungsi Utama:**
* **`addTestResult(id, name, status)`**: Fungsi ini bertugas untuk menangkap data hasil akhir dari sebuah *Test Case* (misal: ID Test, Nama Test, dan status `PASSED`/`FAILED`), lalu menyimpannya ke dalam memori antrean sebelum diekspor menjadi file JSON.
* **Fungsi Tangkapan Layar (Screenshot)**: Sistem ini mendukung perekaman langkah visual. Di dalam helper class, terdapat *method* khusus yang dapat diisi dengan nama tindakan (*action*), data input, harapan (*expected result*), dan statusnya (sukses/gagal). *Engine* akan membaca *path* *screenshot* tersebut secara otomatis dan merendernya ke dalam PDF dengan proporsi yang presisi.

### Tahap 3: Pasang Test Listener
Agar PDF dapat dicetak secara otomatis setelah semua pengujian selesai, **salin *file* `KatiaListener.groovy`** dari repositori ini dan letakkan di dalam folder `Test Listeners` di Katalon Anda.

*Listener* ini memiliki dua tugas utama:
1. Membersihkan memori hasil *test* usang sebelum *Test Suite* baru dimulai.
2. Men- *trigger* eksekusi `katia-report.exe` tepat satu kali di bagian akhir *Test Suite* untuk merender seluruh hasil JSON menjadi satu dokumen PDF lengkap.

### Tahap 4: Implementasi ke dalam Test Case
Tulis *script* pengujian Anda dengan rapi. Sistem ini dirancang untuk mendukung gaya penulisan yang bersih (*fluent api style*). Anda bisa langsung menyisipkan fungsi penangkap *screenshot* dan melampirkan statusnya (`.PASSED` / `.FAILED`) tepat setelah tindakan dilakukan di UI.

Berikut adalah contoh implementasinya`:

```Groovy
try {
    // 1. Eksekusi Langkah Pengujian & Ambil Screenshot
    WebUI.openBrowser('[https://katalon-demo-cura.herokuapp.com/](https://katalon-demo-cura.herokuapp.com/)')
    KatiaReporterScreenshot("Buka Web", "URL CURA", "Halaman web terbuka").PASSED
    
    // 2. Daftarkan Hasil Akhir Test Case
    KatiaReporter.addTestResult("TC-001", "Verifikasi Akses Web", "PASSED")
    
} catch (Exception e) {
    // Jika Error: Catat screenshot gagal & daftarkan hasil FAILED
    KatiaReporterScreenshot("Buka Web", "URL CURA", "Gagal membuka halaman").FAILED
    KatiaReporter.addTestResult("TC-001", "Verifikasi Akses Web", "FAILED")
    KeywordUtil.markFailed("Test Gagal: " + e.getMessage())
    
} finally {
    // 3. Langsung Generate PDF Report di akhir skrip!
    KatiaReporter.generatePDFReport()
    WebUI.closeBrowser()
}
```
### Selesai!
Sekarang, cukup jalankan pengujian Anda menggunakan fitur Test Suite di Katalon, dan rasakan keajaiban laporan PDF yang muncul secara otomatis di akhir proses.

### 🔧 Cara Kustomisasi & Build Ulang Engine (Advanced)
Jika Anda adalah seorang pengembang yang ingin memodifikasi tampilan PDF (seperti mengganti warna tema perusahaan atau mengubah logika peletakan gambar), Anda dapat mengedit source code Node.js dan membungkusnya ulang.

Kloning/unduh file index.js dan package.json dari repositori ini ke dalam sebuah folder kosong.

Buka terminal di folder tersebut, lalu jalankan npm install.

Buka file index.js menggunakan code editor dan modifikasi script sesuai keinginan.

Kompilasi ulang menjadi executable dengan perintah pkg sesuai OS Anda:

Untuk Windows:

```
Bash
    npx pkg . --targets node18-win-x64 --output katia-report.exe
```
   * **Untuk macOS (Intel):**
```Bash
    npx pkg . --targets node18-macos-x64 --output katia-report-mac
```
   * **Untuk macOS (Apple Silicon M1/M2/M3):**
```Bash
    npx pkg . --targets node18-macos-arm64 --output katia-report-mac-arm
```

---

### 🐞 Cara Debugging Report (Tanpa Katalon)
Karena Katia-Report bekerja dengan membaca *file* JSON, Anda bisa melakukan *debugging* tata letak (layout) secara instan melalui Terminal tanpa perlu menjalankan ulang *Test Case* UI di Katalon berulang kali.

1. Pastikan Anda memiliki *file* `result.json` (bisa didapatkan dari sisa eksekusi Katalon sebelumnya) dan letakkan di folder yang sama dengan *executable* Katia-Report.
2. Buka Terminal (macOS/Linux) atau Git Bash (Windows) di dalam folder tersebut.
3. **Jika Anda Menggunakan Windows (.exe):**
```Bash
    ./katia-report.exe -i result.json -o debug_report.pdf
```
Jika Anda Menggunakan macOS:
Di macOS, Anda harus memberikan izin eksekusi pada file tersebut terlebih dahulu sebelum menjalankannya:

```Bash
   chmod +x katia-report-mac
   ./katia-report-mac -i result.json -o debug_report.pdf
```
Jika Anda Developer Node.js (Tanpa Kompilasi):
Saat sedang mengedit kode index.js, Anda tidak perlu melakukan kompilasi (pkg) berulang kali. Cukup eksekusi script-nya langsung:

```Bash
   node index.js -i result.json -o debug_report.pdf
```
Buka debug_report.pdf untuk melihat apakah perbaikan desain Anda sudah pas!

### 📄 Lisensi
Proyek ini bersifat Open Source di bawah lisensi MIT. Anda bebas menggunakan dan memodifikasinya untuk kebutuhan personal maupun instansi.
