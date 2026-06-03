// --- KODE PATCH UNTUK PKG (WAJIB DI BARIS PALING ATAS) ---
const util = require('util');
const OriginalTextDecoder = util.TextDecoder;
global.TextDecoder = class extends OriginalTextDecoder {
    constructor(encoding, options) {
        if (encoding === 'latin1') encoding = 'utf-8';
        super(encoding, options);
    }
};
// ---------------------------------------------------------

const { program } = require('commander');
const fs = require('fs');
const path = require('path');
const { jsPDF } = require('jspdf');

const autoTableLib = require('jspdf-autotable');
const autoTable = autoTableLib.default || autoTableLib;

program.name('qa-reporter').requiredOption('-i, --input <path>').requiredOption('-o, --output <path>');
program.parse();
const options = program.opts();
const inputPath = path.resolve(options.input);
const outputPath = path.resolve(options.output);

try {
    if (!fs.existsSync(inputPath)) throw new Error('File JSON tidak ditemukan!');
    const data = JSON.parse(fs.readFileSync(inputPath, 'utf-8'));
    
    const doc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    const hasSteps = data.results.some(t => t.steps && t.steps.length > 0);

    const primaryColor = [34, 45, 82];    
    const textColor = [50, 50, 50];       
    const textLight = [120, 120, 120];    
    const passColor = [40, 167, 69];      
    const failColor = [220, 53, 69];      

    // ==========================================
    // TAHAP 1: PRE-CALCULATE TABLE OF CONTENTS
    // ==========================================
    const tocList = [];
    const summaryToc = { title: '1. Ringkasan Eksekusi', targetPage: 0, level: 0 };
    const detailToc = { title: '2. Detail Eksekusi', targetPage: 0, level: 0 };
    tocList.push(summaryToc, detailToc);
    
    let lampiranToc = null;
    if (hasSteps) {
        lampiranToc = { title: '3. Lampiran Step & Bukti Data', targetPage: 0, level: 0 };
        tocList.push(lampiranToc);
        
        data.results.forEach(test => {
            if (test.steps && test.steps.length > 0) {
                test.tocRef = { title: `      • [${test.id}] ${test.name}`, targetPage: 0, level: 1 };
                tocList.push(test.tocRef);
                test.steps.forEach((step, index) => {
                    step.tocRef = { title: `            - Langkah ${index + 1}: ${step.action}`, targetPage: 0, level: 2 };
                    tocList.push(step.tocRef);
                });
            }
        });
    }

    const tempDoc = new jsPDF({ orientation: 'portrait', unit: 'mm', format: 'a4' });
    autoTable(tempDoc, {
        startY: 35,
        margin: { top: 22, bottom: 20 }, 
        body: tocList.map(item => [item.title, '999']),
        theme: 'plain',
        styles: { cellPadding: 2.5, fontSize: 10 }
    });
    const tocPageCount = tempDoc.internal.getNumberOfPages();

    // ==========================================
    // TAHAP 2: HALAMAN 1 (COVER)
    // ==========================================
    doc.setFillColor(...primaryColor);
    doc.rect(0, 0, 210, 6, 'F');

    const centerX = 105; 
    let coverY = 85; 
    
    doc.setFontSize(32); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor);
    doc.text("AUTOMATION TEST REPORT", centerX, coverY, { align: "center" });
    coverY += 15;
    
    doc.setFontSize(16); doc.setFont("helvetica", "normal"); doc.setTextColor(...textColor); 
    doc.text(`Project: ${data.projectName}`, centerX, coverY, { align: "center" });
    coverY += 18;

    doc.setFontSize(12); doc.setFont("helvetica", "normal"); doc.setTextColor(...textLight);
    const maxDisplay = 12; 
    
    for (let i = 0; i < data.results.length; i++) {
        if (i >= maxDisplay) {
            doc.setFont("helvetica", "italic");
            doc.text(`... dan ${data.results.length - maxDisplay} test case lainnya.`, centerX, coverY, { align: "center" });
            coverY += 7;
            break; 
        }
        const tc = data.results[i];
        doc.text(`[${tc.id}] ${tc.name}`, centerX, coverY, { align: "center" });
        coverY += 7; 
    }

    coverY += 10; 

    doc.setFontSize(11); doc.setTextColor(150, 150, 150); doc.setFont("helvetica", "normal");
    doc.text(`Environment: ${data.platform ? `${data.platform} via ${data.framework}` : data.framework}`, centerX, coverY, { align: "center" });
    coverY += 6;
    doc.text(`Generated on: ${data.testDate}`, centerX, coverY, { align: "center" });

    // ==========================================
    // TAHAP 3: SUMMARY & DETAIL EKSEKUSI
    // ==========================================
    doc.addPage(); 
    
    summaryToc.targetPage = doc.internal.getNumberOfPages() + tocPageCount;
    detailToc.targetPage = doc.internal.getNumberOfPages() + tocPageCount;
    
    doc.setFontSize(16); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor); 
    doc.text(`1. Ringkasan Eksekusi`, 14, 25);
    
    doc.setFillColor(248, 249, 250); doc.roundedRect(14, 30, 58, 24, 2, 2, 'F'); 
    doc.setDrawColor(100, 100, 100); doc.setLineWidth(1.2); doc.line(16, 30, 70, 30); 
    doc.setFontSize(10); doc.setTextColor(120); doc.setFont("helvetica", "bold");
    doc.text("TOTAL TEST", 43, 38, { align: "center" });
    doc.setFontSize(22); doc.setTextColor(...textColor); 
    doc.text(`${data.summary.total}`, 43, 49, { align: "center" });

    doc.setFillColor(248, 249, 250); doc.roundedRect(76, 30, 58, 24, 2, 2, 'F'); 
    doc.setDrawColor(...passColor); doc.setLineWidth(1.2); doc.line(78, 30, 132, 30); 
    doc.setFontSize(10); doc.setTextColor(120); 
    doc.text("PASSED", 105, 38, { align: "center" });
    doc.setFontSize(22); doc.setTextColor(...passColor); 
    doc.text(`${data.summary.passed}`, 105, 49, { align: "center" });

    doc.setFillColor(248, 249, 250); doc.roundedRect(138, 30, 58, 24, 2, 2, 'F'); 
    doc.setDrawColor(...failColor); doc.setLineWidth(1.2); doc.line(140, 30, 194, 30); 
    doc.setFontSize(10); doc.setTextColor(120); 
    doc.text("FAILED", 167, 38, { align: "center" });
    doc.setFontSize(22); doc.setTextColor(...failColor); 
    doc.text(`${data.summary.failed}`, 167, 49, { align: "center" });

    doc.setTextColor(...primaryColor); doc.setFontSize(16); doc.setFont("helvetica", "bold");
    doc.text(`2. Detail Eksekusi`, 14, 68); 
    
    const tableBody = data.results.map(test => [test.id, test.name, test.status]);
    autoTable(doc, {
        startY: 72, 
        head: [['ID', 'Nama Test Case', 'Status']], 
        body: tableBody, 
        theme: 'striped',
        headStyles: { fillColor: primaryColor, textColor: 255, fontStyle: 'bold' }, 
        alternateRowStyles: { fillColor: [248, 249, 250] },
        styles: { fontSize: 10, cellPadding: 4, textColor: textColor },
        margin: { top: 25, bottom: 20 }, 
        showHead: 'firstPage',           
        didParseCell: function(d) {
            if (d.section === 'body' && d.column.index === 2) {
                d.cell.styles.textColor = d.cell.raw === 'PASSED' ? passColor : failColor;
                d.cell.styles.fontStyle = 'bold';
            }
        }
    });

    // ==========================================
    // TAHAP 4: DETAIL LANGKAH (SMART WRAP & AUTO PAGINATION)
    // ==========================================
    if (hasSteps) {
        doc.addPage(); 
        lampiranToc.targetPage = doc.internal.getNumberOfPages() + tocPageCount;
        
        doc.setFontSize(16); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor); 
        doc.text(`3. Lampiran Step & Bukti Data`, 14, 25);

        let currentY = 35; 

        data.results.forEach(test => {
            if (test.steps && test.steps.length > 0) {
                if (currentY > 255) { doc.addPage(); currentY = 25; }
                test.tocRef.targetPage = doc.internal.getNumberOfPages() + tocPageCount;

                doc.setFillColor(240, 244, 248); doc.rect(14, currentY - 5, 182, 10, 'F');
                doc.setFontSize(12); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor);
                doc.text(`Test Case: ${test.id} - ${test.name}`, 16, currentY + 2);
                currentY += 12;

                test.steps.forEach((step, index) => {
                    let base64Data = null;
                    let format = 'PNG';
                    let finalW = 0, finalH = 0, posX = 15;
                    let hasScreenshotPath = step.screenshot && step.screenshot.trim() !== "";
                    let imageExists = false;

                    if (hasScreenshotPath && fs.existsSync(step.screenshot)) {
                        imageExists = true;
                        const ext = path.extname(step.screenshot).toLowerCase();
                        format = (ext === '.jpg' || ext === '.jpeg') ? 'JPEG' : 'PNG';
                        base64Data = fs.readFileSync(step.screenshot, 'base64');
                        const imgProps = doc.getImageProperties(base64Data);
                        
                        const maxBoxW = 180;
                        const maxBoxH = (imgProps.height > imgProps.width) ? 95 : 85; 
                        
                        const scale = Math.min(maxBoxW / imgProps.width, maxBoxH / imgProps.height);
                        finalW = imgProps.width * scale;
                        finalH = imgProps.height * scale;
                        posX = 14 + (maxBoxW - finalW) / 2;
                    }

                    if (currentY > 265) { doc.addPage(); currentY = 25; }
                    step.tocRef.targetPage = doc.internal.getNumberOfPages() + tocPageCount;

                    doc.setFontSize(11); doc.setFont("helvetica", "bold"); doc.setTextColor(...textColor);
                    doc.text(`Langkah ${index + 1}: ${step.action}`, 15, currentY);
                    
                    doc.setTextColor(step.status === 'PASSED' ? passColor[0] : failColor[0], step.status === 'PASSED' ? passColor[1] : failColor[1], step.status === 'PASSED' ? passColor[2] : failColor[2]);
                    doc.text(`[${step.status}]`, 180, currentY);
                    currentY += 6;

                    // JIKA UI TEST (Punya Gambar Valid)
                    if (hasScreenshotPath && imageExists) {
                        doc.setFontSize(10); doc.setFont("helvetica", "normal"); doc.setTextColor(...textColor);
                        
                        doc.text(`Data: ${step.data || "-"}`, 15, currentY);
                        currentY += 5;

                        const splitExpectedText = doc.splitTextToSize(`Expected: ${step.expected || "-"}`, 180);
                        splitExpectedText.forEach(line => {
                            if (currentY > 275) { doc.addPage(); currentY = 25; }
                            doc.text(line, 15, currentY);
                            currentY += 5;
                        });
                        currentY += 3; 

                        if (currentY + finalH > 275) { doc.addPage(); currentY = 25; }
                        doc.setDrawColor(220, 220, 220); doc.setLineWidth(0.3);
                        doc.rect(posX, currentY, finalW, finalH); 
                        doc.addImage(base64Data, format, posX, currentY, finalW, finalH);
                        currentY += finalH + 10; 
                    } 
                    
                    // JIKA API TEST (Format Data Menggunakan Tabel atau Teks)
                    else {
                        let isTableData = false;
                        let tableRows = [];
                        
                        // Coba mendeteksi apakah data dari Katalon berformat Array (Tabel)
                        try {
                            if (step.data) {
                                tableRows = JSON.parse(step.data);
                                if (Array.isArray(tableRows)) {
                                    isTableData = true;
                                }
                            }
                        } catch (e) {
                            // Jika gagal di-parse, berarti ini teks biasa
                        }

                        if (isTableData) {
                            // Render Tabel Asli
                            let bodyData = Array.isArray(tableRows[0]) ? tableRows : tableRows.map(r => [r]);
                            
                            autoTable(doc, {
                                startY: currentY + 2,
                                body: bodyData,
                                theme: 'grid',
                                styles: { fontSize: 9, cellPadding: 4, textColor: [60, 60, 60], font: "helvetica", overflow: 'linebreak' },
                                // Jika array punya 2 nilai per baris, buat kolom kiri lebih sempit & tebal
                                columnStyles: bodyData[0].length === 2 
                                    ? { 0: { cellWidth: 42, fontStyle: 'bold', fillColor: [244, 246, 249] }, 1: { cellWidth: 140 } } 
                                    : { 0: { cellWidth: 182 } },
                                margin: { left: 14, right: 14 },
                                tableWidth: 182
                            });
                            
                            // Lanjutkan ke posisi Y terakhir setelah tabel selesai
                            currentY = doc.lastAutoTable.finalY + 8;
                        } else {
                            // Fallback jika ternyata teks biasa (non-tabel)
                            doc.setFontSize(10); doc.setFont("helvetica", "normal"); doc.setTextColor(...textLight);
                            const splitDataText = doc.splitTextToSize(step.data || "-", 180);
                            splitDataText.forEach(line => {
                                if (currentY > 275) { doc.addPage(); currentY = 25; }
                                doc.text(line, 15, currentY);
                                currentY += 5; 
                            });
                            currentY += 4;
                        }

                        if (hasScreenshotPath && !imageExists) {
                            if (currentY > 275) { doc.addPage(); currentY = 25; }
                            doc.setTextColor(...failColor); doc.setFont("helvetica", "italic");
                            doc.text(`(Gambar tidak ditemukan pada path: ${step.screenshot})`, 15, currentY);
                            currentY += 10;
                        }
                    }
                });
                
                currentY += 2; doc.setDrawColor(230, 230, 230); doc.line(14, currentY, 196, currentY); currentY += 10;
            }
        });
    }

    // ==========================================
    // TAHAP 5: MENGGAMBAR TOC DI AKHIR DOKUMEN
    // ==========================================
    const contentEndPage = doc.internal.getNumberOfPages();
    
    doc.addPage(); 
    doc.setFontSize(22); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor); 
    doc.text("Table of Contents", 14, 25);
    doc.setDrawColor(220, 220, 220); doc.setLineWidth(0.5); doc.line(14, 30, 196, 30); 
    
    autoTable(doc, {
        startY: 35, 
        margin: { top: 22, bottom: 20 }, 
        body: tocList.map(item => [item.title, (item.targetPage - 1).toString()]), 
        theme: 'plain', 
        styles: { cellPadding: 2.5, fontSize: 10 }, 
        columnStyles: { 0: { halign: 'left', cellWidth: 165 }, 1: { halign: 'right', cellWidth: 15 } },
        didParseCell: function(d) {
            if (d.section === 'body') {
                const level = tocList[d.row.index].level;
                if (d.column.index === 0) {
                    if (level === 2) { d.cell.styles.textColor = textLight; d.cell.styles.fontStyle = 'normal'; } 
                    else if (level === 1) { d.cell.styles.textColor = textColor; d.cell.styles.fontStyle = 'normal'; } 
                    else { d.cell.styles.textColor = primaryColor; d.cell.styles.fontStyle = 'bold'; }
                } else if (d.column.index === 1) {
                    d.cell.styles.textColor = level === 2 ? textLight : textColor;
                    d.cell.styles.fontStyle = level === 0 ? 'bold' : 'normal';
                }
            }
        }
    });

    // ==========================================
    // TAHAP 6: MEMINDAHKAN TOC KE HALAMAN 2
    // ==========================================
    const finalTotalPages = doc.internal.getNumberOfPages();
    const actualTocPageCount = finalTotalPages - contentEndPage;
    
    for (let i = 0; i < actualTocPageCount; i++) {
        doc.movePage(contentEndPage + 1 + i, 2 + i);
    }

    // ==========================================
    // TAHAP 7: INJEKSI GLOBAL HEADER & FOOTER
    // ==========================================
    const displayTotalPages = finalTotalPages - 1; 
    
    for (let i = 2; i <= finalTotalPages; i++) {
        doc.setPage(i);
        
        doc.setFontSize(8); doc.setFont("helvetica", "bold"); doc.setTextColor(...primaryColor);
        doc.text("QA AUTOMATION REPORT", 14, 12);
        doc.setFont("helvetica", "normal"); doc.setTextColor(...textLight);
        doc.text(data.projectName, 196, 12, { align: 'right' });
        doc.setDrawColor(230, 230, 230); doc.setLineWidth(0.3); doc.line(14, 15, 196, 15);

        doc.setFontSize(8); doc.setFont("helvetica", "normal"); doc.setTextColor(...textLight);
        doc.text(`Generated: ${data.testDate}`, 14, 287);
        const pageText = `Halaman ${i - 1} dari ${displayTotalPages}`;
        doc.text(pageText, 196, 287, { align: 'right' });
        doc.line(14, 283, 196, 283);
    }

    const pdfOutput = doc.output('arraybuffer');
    fs.writeFileSync(outputPath, Buffer.from(pdfOutput));
} catch (error) {
    console.error(`[X] GAGAL: ${error.message}`);
}