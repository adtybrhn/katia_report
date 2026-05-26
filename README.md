Markdown
# Katalon Custom PDF Report Engine

## 📖 Penjelasan Proyek
**Katalon Custom PDF Report Engine** adalah sistem pelaporan otomatis (*automated reporting system*) independen yang dirancang untuk mengubah data hasil pengujian Katalon Studio menjadi dokumen PDF profesional berkelas *Enterprise*.

Proyek ini dibangun menggunakan **Node.js (jsPDF)** dan bertindak sebagai jembatan eksternal dari Katalon. Sistem ini sangat cocok untuk:
* **Pengujian Skala Besar**: Dirancang khusus untuk membaca hasil dari banyak *Test Case* sekaligus dalam satu *Test Suite*.
* **Pengujian Multi-Platform (Hybrid)**: Memiliki kecerdasan tata letak otomatis (*auto-scaling*) yang mendeteksi rasio gambar. Tangkapan layar web (landscape) akan dirender penuh, sementara tangkapan layar *mobile device* (portrait) akan dirender atas-bawah secara proporsional agar muat 2 gambar per halaman tanpa membuat gambar menjadi gepeng.
* **Standar Laporan Profesional**: Dilengkapi *Table of Contents* (Daftar Isi) dinamis anti-tabrak, *dashboard summary*, desain kop minimalis, serta penomoran halaman otomatis.

---

## 🚀 Fitur Utama
* **Stand-Alone Executable (`.exe`)**: Engine Node.js dapat dikompilasi menjadi *file* `.exe`, sehingga mesin *runner* (seperti server CI/CD) tidak perlu menginstal Node.js sama sekali.
* **Smart Layout Engine**: Secara dinamis menghitung tinggi teks dan gambar untuk mencegah baris yang terpotong ke halaman berikutnya.
* **Corporate Theme**: Menggunakan palet warna profesional (Navy Blue dan Charcoal) dengan garis batas yang rapi.

---

## 🛠️ Step-by-Step Cara Penggunaan

### Tahap 1: Setup & Kompilasi Engine (Node.js)
Langkah ini hanya perlu dilakukan sekali untuk mengubah *script* JS menjadi aplikasi mandiri.
1. Pastikan **Node.js** terinstal di komputer Anda.
2. Buat folder baru bernama `portable_report` di dalam direktori proyek Katalon Anda.
3. Masukkan *file* `index.js` dan `package.json` Anda ke folder tersebut.
4. Buka terminal (Git Bash / CMD) di dalam folder `portable_report` dan jalankan instalasi *library*:
   ```bash
   npm install jspdf jspdf-autotable commander
Build aplikasi menjadi executable (.exe):

Bash
npx pkg . --targets node18-win-x64 --output portable_report.exe

### Tahap 2: Buat Custom Keyword di Katalon
Buat file Groovy baru di direktori Keywords/com/report/PortableReporter.groovy. Keyword ini bertugas menangkap data dari Katalon dan menyimpannya menjadi file result.json agar bisa dibaca oleh engine.

Groovy
package com.report
import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.json.JsonOutput

class PortableReporter {
    static List<Map<String, Object>> testResults = []

    @Keyword
    def static addTestResult(String id, String name, String status) {
        testResults.add([
            "id": id,
            "name": name,
            "status": status,
            "steps": [] // Opsional: Tambahkan logika untuk menangkap log langkah & screenshot
        ])
    }

    @Keyword
    def static generatePDFReport() {
        String projectDir = RunConfiguration.getProjectDir()
        String testId = testResults.isEmpty() ? "TC" : testResults.get(0).id.toString().replaceAll("[^a-zA-Z0-9]", "_")
        String baseName = testResults.isEmpty() ? "Report" : testResults.get(0).name.toString().replaceAll("[^a-zA-Z0-9]", "_")
        String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss"))
        String dynamicFileName = "${testId}_${baseName}_${timestamp}.pdf"

        def reportData = [
            "projectName": "Automation QA System",
            "framework": "Katalon Studio",
            "platform": "Hybrid (Web & Mobile)",
            "testDate": java.time.LocalDate.now().toString(),
            "summary": [
                "total": testResults.size(),
                "passed": testResults.count { it.status == 'PASSED' },
                "failed": testResults.count { it.status == 'FAILED' }
            ],
            "results": testResults
        ]

        File jsonFile = new File(projectDir + "/portable_report/result.json")
        jsonFile.write(JsonOutput.toJson(reportData))

        def pb = new ProcessBuilder(projectDir + "/portable_report/portable_report.exe", "-i", "result.json", "-o", dynamicFileName)
        pb.directory(new File(projectDir + "/portable_report"))
        pb.start().waitFor()
        println("[V] Laporan PDF Dibuat: " + dynamicFileName)
    }
}

### Tahap 3: Pasang Test Listener (Otomatisasi Laporan)
Agar laporan dirender otomatis setelah semua pengujian di dalam Test Suite selesai, buat file ReportingListener.groovy di folder Test Listeners.

Groovy
import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.annotation.AfterTestSuite
import com.kms.katalon.core.context.TestSuiteContext
import com.report.PortableReporter

class ReportingListener {
    @BeforeTestSuite
    def clearOldData(TestSuiteContext testSuiteContext) {
        PortableReporter.testResults.clear() // Bersihkan sisa tes sebelumnya
    }

    @AfterTestSuite
    def generateFinalReport(TestSuiteContext testSuiteContext) {
        PortableReporter.generatePDFReport() // Cetak PDF gabungan
    }
}

### Tahap 4: Implementasi ke dalam Test Case
Tulis script pengujian Anda seperti biasa. Gunakan blok try-catch untuk menandai akhir dari pengujian Anda dan mengirimkan hasilnya ke memori.

Groovy
import com.kms.katalon.core.util.KeywordUtil
import com.report.PortableReporter

try {
    // ... Logika otomasi klik, input, swipe, dll ...
    
    // Jika semua kode di atas sukses:
    PortableReporter.addTestResult("TC-001", "Verifikasi Login Valid", "PASSED")
} catch (Exception e) {
    // Jika ada error di tengah jalan:
    PortableReporter.addTestResult("TC-001", "Verifikasi Login Valid", "FAILED")
    KeywordUtil.markFailed("Test Gagal: " + e.getMessage())
}

Selesai! Sekarang, cukup jalankan pengujian Anda menggunakan fitur Test Suite di Katalon, dan rasakan keajaiban laporan PDF yang muncul secara otomatis di akhir proses.

🚫 Konfigurasi Tambahan (.gitignore)
Jika Anda melakukan push proyek ini ke repositori, pastikan untuk menambahkan file .gitignore agar pustaka Node.js yang berat tidak ikut ter- upload:

Plaintext
node_modules/
portable_report.exe
result.json
*.pdf

📄 Lisensi
Proyek ini bersifat Open Source di bawah lisensi MIT. Anda bebas menggunakan dan memodifikasinya untuk kebutuhan personal maupun instansi.
