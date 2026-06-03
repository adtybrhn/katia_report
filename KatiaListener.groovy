import com.kms.katalon.core.annotation.BeforeTestSuite
import com.kms.katalon.core.annotation.AfterTestSuite
import com.kms.katalon.core.context.TestSuiteContext

class KatiaListener {
	
    // Dijalankan SEBELUM Test Suite dimulai
    @BeforeTestSuite
    def clearOldData(TestSuiteContext testSuiteContext) {
        // Hapus sisa memori dari pengujian sebelumnya agar PDF benar-benar bersih
        KatiaReporter.testResults.clear()
        KatiaReporter.currentSteps.clear()
        println("[+] Memori dibersihkan. Memulai Test Suite...")
    }

    // Dijalankan SETELAH seluruh Test Case di dalam Test Suite selesai
    @AfterTestSuite
    def generateFinalReport(TestSuiteContext testSuiteContext) {
        // Cetak 1 PDF raksasa yang berisi seluruh hasil Test Case
        KatiaReporter.generatePDFReport()
    }
}
