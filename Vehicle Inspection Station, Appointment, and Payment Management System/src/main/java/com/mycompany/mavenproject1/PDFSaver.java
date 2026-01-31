package com.mycompany.mavenproject1;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;
import javax.swing.*;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PDFSaver {

    // PDF Fontları
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.DARK_GRAY);
    private static final Font FONT_SUBHEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.BLACK);
    private static final Font FONT_NORMAL = FontFactory.getFont(FontFactory.HELVETICA, 10, BaseColor.BLACK);
    private static final Font FONT_BOLD = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
    private static final Font FONT_GREEN = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, new BaseColor(39, 174, 96));
    private static final Font FONT_RED = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.RED);

    public static void saveInspectionReport(int inspectionId, String plate, String ownerInfo, String vehicleModel) {
        // 1. Dosya Seçici Aç
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Raporu Kaydet");
        fileChooser.setSelectedFile(new File("Muayene_Raporu_" + plate + ".pdf"));
        
        int userSelection = fileChooser.showSaveDialog(null);

        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            // Uzantı kontrolü (.pdf yoksa ekle)
            if (!fileToSave.getAbsolutePath().endsWith(".pdf")) {
                fileToSave = new File(fileToSave.getAbsolutePath() + ".pdf");
            }

            try {
                // 2. PDF Dokümanını Oluştur
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(fileToSave));
                document.open();

                // --- BAŞLIK ---
                Paragraph title = new Paragraph("ARAÇ MUAYENE DETAYLI RAPORU", FONT_HEADER);
                title.setAlignment(Element.ALIGN_CENTER);
                document.add(title);
                
                document.add(new Paragraph(" ")); // Boşluk
                document.add(new LineSeparator()); // Çizgi
                document.add(new Paragraph(" "));

                // --- BİLGİLER ---
                PdfPTable infoTable = new PdfPTable(2);
                infoTable.setWidthPercentage(100);
                
                // Sol Taraf (Rapor Bilgisi)
                PdfPCell cellInfo = new PdfPCell();
                cellInfo.setBorder(Rectangle.NO_BORDER);
                cellInfo.addElement(new Paragraph("Rapor No: " + inspectionId, FONT_NORMAL));
                cellInfo.addElement(new Paragraph("Tarih: " + new SimpleDateFormat("dd.MM.yyyy HH:mm").format(new Date()), FONT_NORMAL));
                infoTable.addCell(cellInfo);

                // Sağ Taraf (Araç Bilgisi)
                PdfPCell cellVehicle = new PdfPCell();
                cellVehicle.setBorder(Rectangle.NO_BORDER);
                cellVehicle.addElement(new Paragraph("Plaka: " + plate, FONT_BOLD));
                cellVehicle.addElement(new Paragraph("Araç: " + vehicleModel, FONT_NORMAL));
                cellVehicle.addElement(new Paragraph("Sahip: " + ownerInfo, FONT_NORMAL));
                infoTable.addCell(cellVehicle);

                document.add(infoTable);
                document.add(new Paragraph(" "));
                document.add(new Paragraph(" "));

                // --- 1. BÖLÜM: TEST SONUÇLARI ---
                document.add(new Paragraph("1. BÖLÜM: Test Parametreleri ve Ölçümler", FONT_SUBHEADER));
                document.add(new Paragraph(" "));

                PdfPTable tableTests = new PdfPTable(4); // 4 Kolon
                tableTests.setWidthPercentage(100);
                tableTests.setWidths(new float[]{3, 2, 2, 1.5f}); // Kolon genişlik oranları

                // Tablo Başlıkları
                addTableHeader(tableTests, "Test Adı");
                addTableHeader(tableTests, "Ölçülen Değer");
                addTableHeader(tableTests, "Referans");
                addTableHeader(tableTests, "Durum");

                // Veritabanından Testleri Çek ve Ekle
                fillTestTable(inspectionId, tableTests);
                document.add(tableTests);

                document.add(new Paragraph(" "));

                // --- 2. BÖLÜM: KUSURLAR ---
                document.add(new Paragraph("2. BÖLÜM: Tespit Edilen Kusurlar (Defect List)", FONT_SUBHEADER));
                document.add(new Paragraph(" "));

                PdfPTable tableDefects = new PdfPTable(3);
                tableDefects.setWidthPercentage(100);
                tableDefects.setWidths(new float[]{1.5f, 4, 2});

                addTableHeader(tableDefects, "Kusur Kodu");
                addTableHeader(tableDefects, "Açıklama");
                addTableHeader(tableDefects, "Derece");

                // Veritabanından Kusurları Çek ve Ekle
                fillDefectTable(inspectionId, tableDefects);
                document.add(tableDefects);
                
                document.add(new Paragraph(" "));
                document.add(new LineSeparator());

                // --- GENEL SONUÇ ---
                String generalResult = getGeneralResult(inspectionId);
                Paragraph pResult = new Paragraph("GENEL SONUÇ: " + generalResult, FONT_HEADER);
                pResult.setAlignment(Element.ALIGN_CENTER);
                
                if (generalResult.contains("GEÇTİ")) {
                    pResult.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, new BaseColor(39, 174, 96)));
                } else {
                    pResult.setFont(FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, BaseColor.RED));
                }
                
                document.add(pResult);

                document.close();

                JOptionPane.showMessageDialog(null, "PDF başarıyla kaydedildi:\n" + fileToSave.getAbsolutePath());
                
                // Dosyayı otomatik aç (Opsiyonel)
                try {
                    Desktop.getDesktop().open(fileToSave);
                } catch (Exception ex) {
                    // Otomatik açılamazsa hata vermesin
                }

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "PDF oluşturulurken hata: " + e.getMessage());
            }
        }
    }

    // --- YARDIMCI METODLAR ---

    private static void addTableHeader(PdfPTable table, String title) {
        PdfPCell header = new PdfPCell();
        header.setBackgroundColor(new BaseColor(44, 62, 80));
        header.setBorderWidth(1);
        header.setPhrase(new Phrase(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, new BaseColor(255, 255, 255))));
        header.setPadding(5);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(header);
    }

    private static void fillTestTable(int inspectionId, PdfPTable table) throws SQLException {
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(DBHelper.getTestResultsQuery());
            ps.setInt(1, inspectionId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                table.addCell(new Phrase(rs.getString("test_name"), FONT_NORMAL));
                
                String val = rs.getString("measured_value") + " " + rs.getString("unit");
                PdfPCell cellVal = new PdfPCell(new Phrase(val, FONT_NORMAL));
                cellVal.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellVal);

                String min = rs.getString("min_limit");
                String max = rs.getString("max_limit");
                String ref = (min != null ? min : "0") + " - " + (max != null ? max : "∞");
                PdfPCell cellRef = new PdfPCell(new Phrase(ref, FONT_NORMAL));
                cellRef.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellRef);

                boolean passed = rs.getBoolean("is_passed");
                PdfPCell cellStatus = new PdfPCell(new Phrase(passed ? "GEÇTİ" : "KALDI", passed ? FONT_GREEN : FONT_RED));
                cellStatus.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellStatus);
            }
        }
    }

    private static void fillDefectTable(int inspectionId, PdfPTable table) throws SQLException {
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(DBHelper.getDefectsQuery());
            ps.setInt(1, inspectionId);
            ResultSet rs = ps.executeQuery();

            boolean hasDefect = false;
            while (rs.next()) {
                hasDefect = true;
                table.addCell(new Phrase(rs.getString("defect_code"), FONT_NORMAL));
                table.addCell(new Phrase(rs.getString("description"), FONT_NORMAL));
                
                String sev = rs.getString("severity");
                Font sevFont = FONT_NORMAL;
                if (sev.contains("AĞIR") || sev.contains("EMNİYETSİZ")) sevFont = FONT_RED;
                
                table.addCell(new Phrase(sev, sevFont));
            }
            
            if (!hasDefect) {
                PdfPCell emptyCell = new PdfPCell(new Phrase("Kusur Kaydı Yoktur", FONT_GREEN));
                emptyCell.setColspan(3);
                emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                emptyCell.setPadding(10);
                table.addCell(emptyCell);
            }
        }
    }

    private static String getGeneralResult(int inspectionId) {
        String res = "BİLİNMİYOR";
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT result_status FROM inspection WHERE inspection_id = ?");
            ps.setInt(1, inspectionId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String dbRes = rs.getString("result_status");
                if ("PASSED".equalsIgnoreCase(dbRes)) res = "MUAYENEDEN GEÇTİ";
                else if ("FAILED".equalsIgnoreCase(dbRes)) res = "MUAYENEDEN KALDI";
                else res = dbRes;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return res;
    }
}