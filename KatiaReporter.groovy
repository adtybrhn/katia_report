import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.json.JsonOutput


public class KatiaReporter {

	static List<Map<String, Object>> testResults = new ArrayList<>()

	// Memori sementara untuk menampung step-step yang berjalan di satu Test Case
	static List<Map<String, String>> currentSteps = new ArrayList<>()

	// FUNGSI AJAIB YANG ANDA MINTA:
	public static StepRecord KatiaReporterScreenshot(String action, String data, String expected) {
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

		KatiaReporter.currentSteps.add(step)
	}
}