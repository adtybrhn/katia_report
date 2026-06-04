import com.kms.katalon.core.annotation.Keyword
import com.kms.katalon.core.configuration.RunConfiguration
import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import java.io.File
import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.awt.Color
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import org.openqa.selenium.TakesScreenshot
import org.openqa.selenium.OutputType
import org.openqa.selenium.WebDriver

public class KatiaReporter {

<<<<<<< HEAD
public class KatiaReporter {

	static List<Map<String, Object>> testResults = new ArrayList<>()

	// Memori sementara untuk menampung step-step yang berjalan di satu Test Case
	static List<Map<String, String>> currentSteps = new ArrayList<>()

	// FUNGSI AJAIB YANG ANDA MINTA:
=======
	static List<Map<String, String>> currentSteps = new ArrayList<>()

>>>>>>> 22d8e4ab639949522b383a6fdf81e7fc8ced015b
	public static StepRecord KatiaReporterScreenshot(String action, String data, String expected) {
		return new StepRecord(action, data, expected)
	}

	@Keyword
	def static addTestResult(String id, String name, String status, String apiData = null) {
		Map<String, Object> result = new HashMap<>()
		result.put("id", id.toString().trim())
		result.put("name", name.toString().trim())
		result.put("status", status)

		if (apiData != null) {
			Map<String, String> apiStep = new HashMap<>()
			apiStep.put("action", "Hit API Endpoint")
			apiStep.put("data", apiData)
			apiStep.put("expected", "")
			apiStep.put("status", status)
			apiStep.put("screenshot", "")
			currentSteps.add(apiStep)
		}

		result.put("steps", new ArrayList<>(currentSteps))
		currentSteps.clear()

		String projectDir = RunConfiguration.getProjectDir().replace("\\", "/")
		String folderPath = projectDir + "/katia_report"
		File reportDir = new File(folderPath)
		if (!reportDir.exists()) {
			reportDir.mkdirs()
		}

		File jsonFile = new File(folderPath + "/result.json")
		def reportData = [
			"projectName": "E2E Hybrid Testing (Web, API, Mobile)",
			"framework": "Katalon Studio",
			"platform": "Multi-Platform",
			"testDate": java.time.LocalDate.now().toString(),
			"summary": ["total": 0, "passed": 0, "failed": 0],
			"results": []
		]

		if (jsonFile.exists() && jsonFile.length() > 0) {
			try {
				def parsed = new JsonSlurper().parse(jsonFile)
				if (parsed.results != null) {
					reportData.results = parsed.results
				}
			} catch (Exception e) {}
		}

		// SMART OVERWRITE: Cegah duplikat jika TC yang sama di-run ulang (Retry)
		int existingIndex = reportData.results.findIndexOf { it.id == result.id }
		if (existingIndex != -1) {
			reportData.results[existingIndex] = result
		} else {
			reportData.results.add(result)
		}

		reportData.summary.total = reportData.results.size()
		reportData.summary.passed = reportData.results.count { it.status == 'PASSED' }
		reportData.summary.failed = reportData.results.count { it.status == 'FAILED' }

		jsonFile.write(JsonOutput.toJson(reportData))
	}

	@Keyword
	def static cleanUpOldReport() {
		String folderPath = RunConfiguration.getProjectDir().replace("\\", "/") + "/katia_report"

		// Hapus JSON lama
		File jsonFile = new File(folderPath + "/result.json")
		if(jsonFile.exists()) {
			jsonFile.delete()
		}

		// Hapus isi folder screenshot
		File screenshotDir = new File(folderPath + "/screenshots")
		if(screenshotDir.exists()) {
			screenshotDir.listFiles().each { if(it.isFile()) it.delete() }
		}

		currentSteps.clear()
	}

	@Keyword
	def static generatePDFReport() {
		println("[+] Memulai pembuatan PDF Master Report...")
		String projectDir = RunConfiguration.getProjectDir().replace("\\", "/")
		File jsonFile = new File(projectDir + "/katia_report/result.json")
		if (!jsonFile.exists()) {
			println("[!] Gagal: result.json tidak ditemukan!")
			return
		}

		String testId = "E2E"
		String baseName = "Execution"
		try {
			def parsed = new JsonSlurper().parse(jsonFile)
			if(parsed.results.size() > 0) {
				testId = parsed.results[0].id.toString().replaceAll("[^a-zA-Z0-9]", "_")
				baseName = parsed.results[0].name.toString().replaceAll("[^a-zA-Z0-9]", "_")
			}
		} catch(Exception e) {}

		java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy_HH-mm-ss")
		String timestamp = java.time.LocalDateTime.now().format(formatter)
		String dynamicFileName = "${testId}_${baseName}_${timestamp}.pdf"

		def pb = new ProcessBuilder(projectDir + "/katia_report/katia-report.exe", "-i", "result.json", "-o", dynamicFileName)
		pb.directory(new File(projectDir + "/katia_report"))
		pb.redirectErrorStream(true)
		def process = pb.start()
		process.waitFor()

		println("[V] Laporan Master PDF sukses dibuat: " + dynamicFileName)
	}
}

class StepRecord {
	String action, data, expected

	StepRecord(String action, String data, String expected) {
		this.action = action; this.data = data; this.expected = expected
	}

	StepRecord getPASSED() {
		recordStep("PASSED"); return this
	}
	StepRecord getFAILED() {
		recordStep("FAILED"); return this
	}

	private void recordStep(String status) {
		String projectDir = RunConfiguration.getProjectDir().replace("\\", "/")
		String baseFileName = action.replaceAll("[^a-zA-Z0-9]", "_") + "_" + System.currentTimeMillis().toString()
		String screenshotFolder = projectDir + "/katia_report/screenshots"
		File screenshotDir = new File(screenshotFolder)
		if (!screenshotDir.exists()) {
			screenshotDir.mkdirs()
		}

		String pngPath = screenshotFolder + "/" + baseFileName + ".png"
		String finalPath = ""

<<<<<<< HEAD
=======
		try {
			WebDriver nativeDriver = null
			try {
				nativeDriver = com.kms.katalon.core.webui.driver.DriverFactory.getWebDriver()
			} catch (Exception e) {}
			if (nativeDriver == null) {
				try {
					nativeDriver = com.kms.katalon.core.mobile.keyword.internal.MobileDriverFactory.getDriver()
				} catch (Exception e) {}
			}

			if (nativeDriver != null && nativeDriver instanceof TakesScreenshot) {
				File srcFile = ((TakesScreenshot) nativeDriver).getScreenshotAs(OutputType.FILE)
				Files.copy(srcFile.toPath(), new File(pngPath).toPath(), StandardCopyOption.REPLACE_EXISTING)
			}

			File pngFile = new File(pngPath)
			if (pngFile.exists() && pngFile.length() > 0) {
				String jpgPath = screenshotFolder + "/" + baseFileName + ".jpg"
				BufferedImage image = ImageIO.read(pngFile)

				if (image != null) {
					BufferedImage compressedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB)
					compressedImage.createGraphics().drawImage(image, 0, 0, Color.WHITE, null)
					ImageIO.write(compressedImage, "jpg", new File(jpgPath))

					pngFile.delete()
					finalPath = jpgPath
				} else {
					finalPath = pngPath
				}
			}
		} catch (Exception e) {}

		Map<String, String> step = new HashMap<>()
		step.put("action", action); step.put("data", data)
		step.put("expected", expected); step.put("status", status)
		step.put("screenshot", finalPath)
>>>>>>> 22d8e4ab639949522b383a6fdf81e7fc8ced015b
		KatiaReporter.currentSteps.add(step)
	}
}
