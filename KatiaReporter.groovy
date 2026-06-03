import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import com.kms.katalon.core.webui.keyword.WebUiBuiltInKeywords as WebUI
import groovy.json.JsonOutput

import java.nio.file.Files
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.io.File

public class KatiaReporter {

	// Memori utama untuk menampung seluruh hasil Test Case
	static List<Map<String, Object>> testResults = new ArrayList<>()

	// Memori sementara untuk menampung step-step (UI/API) yang berjalan di satu Test Case
	static List<Map<String, String>> currentSteps = new ArrayList<>()

	// =========================================================
	// FUNGSI UNTUK WEB UI TESTING (SCREENSHOT)
	// =========================================================
	public static StepRecord PortableReporterScreenshot(String action, String data, String expected) {
		return new StepRecord(action, data, expected)
	}

	// =========================================================
	// FUNGSI PENCATAT TEST CASE (BISA UNTUK UI MAUPUN API)
	// =========================================================
	@Keyword
	def static addTestResult(String id, String name, String status, String apiPayload = "") {
		
		// Jika ini adalah test API (memiliki payload JSON/XML), jadikan sebagai satu step khusus
		if (apiPayload != null && apiPayload.trim() != "") {
			Map<String, String> apiStep = [
				"action": "Hit API Endpoint",
				"expected": "Response Status Sesuai Ekspektasi",
				"status": status,
				"data": apiPayload
			]
			currentSteps.add(apiStep)
		}

		// Bungkus seluruh step yang sudah terkumpul ke dalam format Test Case
		Map<String, Object> result = new HashMap<>()
		result.put("id", id)
		result.put("name", name)
		result.put("status", status)
		result.put("steps", new ArrayList<>(currentSteps))

		// Kosongkan memori steps untuk Test Case berikutnya
		currentSteps.clear()

		// Masukkan ke memori utama
		testResults.add(result)
		println("=> [KatiaReporter] Berhasil mencatat TC: " + id)
	}

	// =========================================================
	// FUNGSI PEMBUAT PDF & JSON (DIPANGGIL OLEH TEST LISTENER)
	// =========================================================
	@Keyword
	def static generatePDFReport() {
		println("[+] Memulai pembuatan JSON dan PDF Report...")
		String projectDir = RunConfiguration.getProjectDir()

		// --- NAMA FILE DINAMIS ---
		String testId = testResults.isEmpty() ? "TC" : testResults.get(0).id.toString().replaceAll("[^a-zA-Z0-9]", "_")
		String baseName = testResults.isEmpty() ? "Laporan_Test" : testResults.get(0).name.toString().replaceAll("[^a-zA-Z0-9]", "_")

		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
		String timestamp = java.time.LocalDateTime.now().format(formatter)
		String dynamicFileName = "${testId}_${baseName}_${timestamp}.pdf"

		// Rangkai data akhir
		def reportData = [
			"projectName": "Katalon Unified Testing (Web & API)",
			"framework": "Katalon Studio",
			"platform": "Hybrid (Web UI / REST API)",
			"testDate": java.time.LocalDate.now().toString(),
			"summary": [
				"total": testResults.size(),
				"passed": testResults.count { it.status == 'PASSED' },
				"failed": testResults.count { it.status == 'FAILED' }
			],
			"results": testResults
		]

		// 1. Simpan jadi result.json
		File reportFolder = new File(projectDir + "/katia_report")
		if (!reportFolder.exists()) {
			reportFolder.mkdirs() // Buat folder otomatis jika tidak ada
		}

		String jsonString = JsonOutput.toJson(reportData)
		File jsonFile = new File(reportFolder, "result.json")
		jsonFile.write(jsonString)
		println("[+] File result.json sukses diperbarui.")

		// 2. Eksekusi Engine Katia-Report (Menggunakan CMD agar aman dari isu path spasi di Windows)
		String exeName = "katia-report.exe" // Pastikan nama ini sesuai dengan file Anda!
		File exeFile = new File(reportFolder, exeName)
		
		if (!exeFile.exists()) {
			println("[-] ERROR KRITIKAL: Sistem gagal menemukan file engine pelaporan.")
			println("[-] Harap pastikan file " + exeName + " benar-benar ada di dalam folder: " + reportFolder.getAbsolutePath())
			return // Hentikan proses jika exe tidak ada
		}

		try {
			// Perintah dipanggil via cmd.exe /c agar jalur eksekusi lebih stabil di Windows
			def pb = new ProcessBuilder("cmd.exe", "/c", exeName, "-i", "result.json", "-o", dynamicFileName)
			pb.directory(reportFolder)
			pb.redirectErrorStream(true)

			def process = pb.start()
			process.waitFor()

			println("[+] Hasil Eksekusi CLI:")
			println(process.text)
			println("[V] Laporan PDF sukses tercetak: " + dynamicFileName)
		} catch (Exception e) {
			println("[-] GAGAL mengeksekusi engine katia-report: " + e.getMessage())
		}
	}
}

// =========================================================
// KELAS PEMBANTU UNTUK MENGELOLA SCREENSHOT (.PASSED / .FAILED)
// =========================================================
class StepRecord {
	String action, data, expected

	StepRecord(String action, String data, String expected) {
		this.action = action
		this.data = data
		this.expected = expected
	}

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
		
		// 1. Tentukan nama file sementara (PNG) dan nama file final (JPG)
		String baseFileName = action.replaceAll("[^a-zA-Z0-9]", "_") + "_" + timestamp
		String pngPath = projectDir + "/katia_report/screenshots/" + baseFileName + ".png"
		String jpgPath = projectDir + "/katia_report/screenshots/" + baseFileName + ".jpg"
		
		File screenshotDir = new File(projectDir + "/katia_report/screenshots/")
		if (!screenshotDir.exists()) {
			screenshotDir.mkdirs()
		}

		try {
			// 2. Minta Katalon mengambil screenshot PNG mentah
			WebUI.takeScreenshot(pngPath)

			// 3. PROSES KOMPRESI (Konversi PNG mentah ke JPG ringan)
			File pngFile = new File(pngPath)
			BufferedImage image = ImageIO.read(pngFile)
			
			// Buat kanvas baru khusus JPG (menghapus latar belakang transparan)
			BufferedImage compressedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB)
			compressedImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
			
			// Simpan sebagai file JPG
			File jpgFile = new File(jpgPath)
			ImageIO.write(compressedImage, "jpg", jpgFile)
			
			// Hapus file PNG yang berat agar tidak memenuhi hardisk Anda
			if (pngFile.exists()) {
				pngFile.delete()
			}

		} catch (Exception e) {
			println("Gagal memproses screenshot: " + e.getMessage())
			jpgPath = "" // Kosongkan jika gagal agar Node.js tidak error
		}

		// 4. Masukkan data ke memori
		Map<String, String> step = new HashMap<>()
		step.put("action", action)
		step.put("data", data)
		step.put("expected", expected)
		step.put("status", status)
		step.put("screenshot", jpgPath) // Katia-Report sekarang akan menerima path JPG

		KatiaReporter.currentSteps.add(step)
	}
}
