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
Agar PDF dapat dicetak secara otomatis setelah semua pengujian selesai, **salin *file* `ReportingListener.groovy`** dari repositori ini dan letakkan di dalam folder `Test Listeners` di Katalon Anda.

*Listener* ini memiliki dua tugas utama:
1. Membersihkan memori hasil *test* usang sebelum *Test Suite* baru dimulai.
2. Men- *trigger* eksekusi `katia-report.exe` tepat satu kali di bagian akhir *Test Suite* untuk merender seluruh hasil JSON menjadi satu dokumen PDF lengkap.

### Tahap 4: Implementasi ke dalam Test Case
Tulis *script* pengujian Anda dengan rapi. Sistem ini dirancang untuk mendukung gaya penulisan yang bersih (*fluent api style*). Anda bisa langsung menyisipkan fungsi penangkap *screenshot* dan melampirkan statusnya (`.PASSED` / `.FAILED`) tepat setelah tindakan dilakukan di UI.

Berikut adalah contoh implementasinya pada fungsi `login`:

```Groovy
public static void login() {
    String username = 'standard_user'
    String password = 'secret_sauce'
    
    // 1. Input Username & Rekam Bukti
    WebUI.setText(textbox_username, username)
    KatiaReporterScreenshot("Input Username", "standard_user", "Text username terisi").PASSED

    // 2. Input Password & Rekam Bukti
    WebUI.setText(textbox_password, password)
    KatiaReporterScreenshot("Input Password", "secret_sauce", "Text password terisi").PASSED

    // 3. Eksekusi Login
    WebUI.click(btn_login)
    
    // 4. Verifikasi Akhir
    if (WebUI.verifyElementPresent(dashboard_logo, 5, FailureHandling.OPTIONAL)) {
        KatiaReporterScreenshot("Berhasil Login", "", "Masuk ke halaman dashboard").PASSED
    } else {
        KatiaReporterScreenshot("Gagal Login", "", "Gagal Masuk ke halaman dashboard").FAILED
    }
}
```
Lalu, tambahkan addTestResult di dalam blok eksekusi Test Case Anda untuk mengetahui jumlah hasil yang gagal maupun berhasil:
```Groovy
try {
    Login.openBrowser()
    Login.login()

    KatiaReporter.addTestResult("TC-001", "Verifikasi Login Valid", "PASSED")
} catch (Exception e) {
    KatiaReporter.addTestResult("TC-001", "Verifikasi Login Valid", "FAILED")
    KeywordUtil.markFailed("Test Gagal: " + e.getMessage())
}
```
### Selesai!
Sekarang, cukup jalankan pengujian Anda menggunakan fitur Test Suite di Katalon, dan rasakan keajaiban laporan PDF yang muncul secara otomatis di akhir proses.

## 🔧 Cara Kustomisasi & Build Ulang Engine (Advanced)
Jika Anda adalah seorang pengembang yang ingin memodifikasi tampilan PDF (seperti mengganti warna tema perusahaan, menyesuaikan margin, atau mengubah logika peletakan gambar), Anda dapat mengedit source code Node.js dan membungkusnya ulang (re-package) menjadi aplikasi mandiri yang baru.

Langkah-langkah:
Persiapan Lingkungan: Pastikan Anda telah menginstal Node.js (versi 18 atau lebih baru) di komputer Anda.

Unduh Source Code: Kloning atau unduh file index.js dan package.json dari repositori ini, letakkan di dalam folder kosong.

Instal Pustaka Pendukung: Buka terminal (CMD / Git Bash) di dalam folder tersebut, lalu jalankan perintah:

```Bash
npm install
```
Modifikasi Kode: Buka file index.js menggunakan code editor pilihan Anda (seperti VS Code). Anda bebas menyesuaikan array warna pada variabel primaryColor, mengatur logika auto-scaling dimensi gambar, mengubah font, dll.

Kompilasi Ulang (Package): Setelah Anda puas dengan perubahannya, jalankan perintah pkg berikut di terminal untuk menyatukan script dan Node.js ke dalam satu file .exe:

```Bash
npx pkg . --targets node18-win-x64 --output katia-report.exe
```
Implementasi: Ganti file katia-report.exe yang lama di folder proyek Katalon Anda dengan file .exe yang baru saja selesai di-build. Selesai!

## 🚫 Konfigurasi Tambahan (.gitignore)
Jika Anda menggabungkan engine ini ke dalam repositori GitHub proyek Katalon Anda, pastikan untuk menambahkan file .gitignore dengan format berikut agar file sampah hasil generate tidak ikut ter-upload berulang kali:

Plaintext
result.json
*.pdf
(Catatan: katia-report.exe tetap dibiarkan ter-upload agar rekan tim Anda yang melakukan clone proyek bisa langsung menggunakan engine tersebut).

## 📄 Lisensi
Proyek ini bersifat Open Source di bawah lisensi MIT. Anda bebas menggunakan dan memodifikasinya untuk kebutuhan personal maupun instansi.
