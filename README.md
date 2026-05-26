# Katalon Custom PDF Report Engine

## 📖 Penjelasan Proyek
**Katalon Custom PDF Report Engine** adalah sistem pelaporan otomatis (*automated reporting system*) independen yang dirancang untuk mengubah data hasil pengujian Katalon Studio menjadi dokumen PDF profesional berkelas *Enterprise*.

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
1. Buat folder baru bernama `portable_report` di dalam direktori proyek Katalon Anda.
2. Salin *file* `portable_report.exe` yang ada di repositori ini, lalu letakkan ke dalam folder tersebut.

### Tahap 2: Gunakan Helper Class (PortableReporter)
Untuk mempermudah integrasi, Anda tidak perlu menulis kode dari awal. Cukup **salin *file* `PortableReporter.groovy`** dari repositori ini, lalu letakkan ke dalam folder `Include/scripts/groovy/(default package)` pada proyek Katalon Anda.

**Penjelasan Fungsi Utama:**
* **`addTestResult(id, name, status)`**: Fungsi ini bertugas untuk menangkap data hasil akhir dari sebuah *Test Case* (misal: ID Test, Nama Test, dan status `PASSED`/`FAILED`), lalu menyimpannya ke dalam memori antrean sebelum diekspor menjadi file JSON.
* **Fungsi Tangkapan Layar (Screenshot)**: Sistem ini mendukung perekaman langkah visual. Di dalam helper class, terdapat *method* khusus yang dapat diisi dengan nama tindakan (*action*), data input, harapan (*expected result*), dan statusnya (sukses/gagal). *Engine* akan membaca *path* *screenshot* tersebut secara otomatis dan merendernya ke dalam PDF dengan proporsi yang presisi.

### Tahap 3: Pasang Test Listener
Agar PDF dapat dicetak secara otomatis setelah semua pengujian selesai, **salin *file* `ReportingListener.groovy`** dari repositori ini dan letakkan di dalam folder `Test Listeners` di Katalon Anda.

*Listener* ini memiliki dua tugas utama:
1. Membersihkan memori hasil *test* usang sebelum *Test Suite* baru dimulai.
2. Men- *trigger* eksekusi `portable_report.exe` tepat satu kali di bagian akhir *Test Suite* untuk merender seluruh hasil JSON menjadi satu dokumen PDF lengkap.

### Tahap 4: Implementasi ke dalam Test Case
Tulis *script* pengujian Anda dengan rapi. Sistem ini dirancang untuk mendukung gaya penulisan yang bersih (*fluent api style*). Anda bisa langsung menyisipkan fungsi penangkap *screenshot* dan melampirkan statusnya (`.PASSED` / `.FAILED`) tepat setelah tindakan dilakukan di UI.

Berikut adalah contoh implementasinya pada fungsi `login`:

```groovy
public static void login() {
    String username = 'standard_user'
    String password = 'secret_sauce'
    
    // 1. Input Username & Rekam Bukti
    WebUI.setText(textbox_username, username)
    PortableReporterScreenshot("Input Username", "standard_user", "Text username terisi").PASSED

    // 2. Input Password & Rekam Bukti
    WebUI.setText(textbox_password, password)
    PortableReporterScreenshot("Input Password", "secret_sauce", "Text password terisi").PASSED

    // 3. Eksekusi Login
    WebUI.click(btn_login)
    
    // 4. Verifikasi Akhir
    if (WebUI.verifyElementPresent(dashboard_logo, 5, FailureHandling.OPTIONAL)) {
        PortableReporterScreenshot("Berhasil Login", "", "Masuk ke halaman dashboard").PASSED
    } else {
        PortableReporterScreenshot("Gagal Login", "", "Gagal Masuk ke halaman dashboard").FAILED
```
### Selesai! 
Sekarang, cukup jalankan pengujian Anda menggunakan fitur Test Suite di Katalon, dan rasakan keajaiban laporan PDF yang muncul secara otomatis di akhir proses. 
    }
}
