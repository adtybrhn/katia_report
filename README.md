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
   ```Bash
   npx pkg . --targets node18-win-x64 --output portable_report.exe
   ```
### Tahap 2: Buat Helper Class di Scripts Groovy
Buat folder dan file Groovy baru di direktori Include/scripts/groovy/com/report/PortableReporter.groovy. Class murni ini bertugas menangkap data dari Katalon dan menyimpannya menjadi file result.json agar bisa dibaca oleh engine.
   ```
   import com.kms.katalon.core.annotation.Keyword
   import com.kms.katalon.core.configuration.RunConfiguration
   import groovy.json.JsonOutput
   
   
   public class PortableReporter {
   
   	static List<Map<String, Object>> testResults = new ArrayList<>()
   
   	// Memori sementara untuk menampung step-step yang berjalan di satu Test Case
   	static List<Map<String, String>> currentSteps = new ArrayList<>()
   
   	// FUNGSI AJAIB YANG ANDA MINTA:
   	public static StepRecord PortableReporterScreenshot(String action, String data, String expected) {
   		return new StepRecord(action, data, expected)
   	}
   
   	@Keyword
   	def static addTestResult(String id, String name, String status) {
   		Map<String, Object> result = new HashMap<>()
   		result.put("id", id)
   		result.put("name", name)
   		result.put("status", status)
   
   		// Memasukkan riwayat langkah (steps) ke dalam Test Case ini
   		result.put("steps", new ArrayList<>(currentSteps))
   
   		// Kosongkan memori steps untuk Test Case berikutnya
   		currentSteps.clear()
   
   		testResults.add(result)
   	}
   
   	@Keyword
   	def static generatePDFReport() {
   		println("[+] Memulai pembuatan JSON dan PDF Report...")
   		String projectDir = RunConfiguration.getProjectDir()
   
   		// --- NAMA FILE DINAMIS DENGAN ID TEST CASE ---
   		// Mengambil ID Test Case (TC-001) dan Nama Test Case (Verifikasi Login Valid)
   		String testId = testResults.isEmpty() ? "TC" : testResults.get(0).id.toString().replaceAll("[^a-zA-Z0-9]", "_")
   		String baseName = testResults.isEmpty() ? "Laporan_Test" : testResults.get(0).name.toString().replaceAll("[^a-zA-Z0-9]", "_")
   
   		// Membuat format Timestamp
   		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
   		String timestamp = java.time.LocalDateTime.now().format(formatter)
   
   		// Hasil akhir: TC_001_Verifikasi_Login_Valid_26-05-2026_12-45-02.pdf
   		String dynamicFileName = "${testId}_${baseName}_${timestamp}.pdf"
   		// ----------------------------------------------
   
   		def reportData = [
   			"projectName": "Sauce Demo Web Application ",
   			"framework": "Katalon Studio",
   			"platform": "Web UI",
   			"testDate": java.time.LocalDate.now().toString(),
   			"summary": [
   				"total": testResults.size(),
   				"passed": testResults.count { it.status == 'PASSED' },
   				"failed": testResults.count {
   					it.status == 'FAILED'
   				}
   			],
   			"results": testResults
   		]
   
   		String jsonString = JsonOutput.toJson(reportData)
   		File jsonFile = new File(projectDir + "/portable_report/result.json")
   		jsonFile.write(jsonString)
   
   		def pb = new ProcessBuilder(
   				projectDir + "/portable_report/portable_report.exe",
   				"-i", "result.json",
   				"-o", dynamicFileName
   				)
   		pb.directory(new File(projectDir + "/portable_report"))
   		pb.redirectErrorStream(true)
   
   		def process = pb.start()
   		process.waitFor()
   
   		println("[+] Hasil Eksekusi CLI:")
   		println(process.text)
   		println("[V] Laporan tersimpan dengan nama: " + dynamicFileName)
   	}
   }
   
   // KELAS PEMBANTU UNTUK MENGELOLA .PASSED / .FAILED
   class StepRecord {
   	String action, data, expected
   
   	StepRecord(String action, String data, String expected) {
   		this.action = action
   		this.data = data
   		this.expected = expected
   	}
   
   	// Mengembalikan objek agar editor Katalon tidak protes
   	StepRecord getPASSED() {
   		recordStep("PASSED")
   		return this
   	}
   
   	StepRecord getFAILED() {
   		recordStep("FAILED")
   		return this
   	}
   
   	private void recordStep(String status) {
   		String projectDir = RunConfiguration.getProjectDir()
   		String timestamp = System.currentTimeMillis().toString()
   		// Buat nama file unik agar tidak saling menimpa
   		String fileName = action.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp + ".png"
   		String screenshotPath = projectDir + "/portable_report/screenshots/" + fileName
   
   		try {
   			// Ambil screenshot secara otomatis
   			WebUI.takeScreenshot(screenshotPath)
   		} catch (Exception e) {
   			println("Gagal mengambil screenshot: " + e.getMessage())
   		}
   
   		// Simpan data langkah ke memori
   		Map<String, String> step = new HashMap<>()
   		step.put("action", action)
   		step.put("data", data)
   		step.put("expected", expected)
   		step.put("status", status)
   		step.put("screenshot", screenshotPath)
   
   		PortableReporter.currentSteps.add(step)
   	}
   }
   ```
### Tahap 3: Pasang Test Listener (Otomatisasi Laporan)
Agar laporan dirender otomatis setelah semua pengujian di dalam Test Suite selesai, buat file ReportingListener.groovy di folder Test Listeners.
```
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
```

### Tahap 4: Implementasi ke dalam Test Case
Tulis script pengujian Anda seperti biasa. Gunakan blok try-catch untuk menandai akhir dari pengujian Anda, lalu panggil metode dari helper class Groovy yang sudah dibuat.

```
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
```

Selesai! Sekarang, cukup jalankan pengujian Anda menggunakan fitur Test Suite di Katalon, dan rasakan keajaiban laporan PDF yang muncul secara otomatis di akhir proses.

🚫 Konfigurasi Tambahan (.gitignore)
Jika Anda melakukan push proyek ini ke repositori, pastikan untuk menambahkan file .gitignore agar pustaka Node.js yang berat tidak ikut ter-upload:

Plaintext
node_modules/
portable_report.exe
result.json
*.pdf
📄 Lisensi
Proyek ini bersifat Open Source di bawah lisensi MIT. Anda bebas menggunakan dan memodifikasinya untuk kebutuhan personal maupun instansi.
