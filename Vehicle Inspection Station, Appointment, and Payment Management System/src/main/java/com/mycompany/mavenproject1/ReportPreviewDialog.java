package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class ReportPreviewDialog extends JDialog {
    private static ReportPreviewDialog instance;

    // Test Tablosu Bileşenleri
    private JTable tableTests;
    private DefaultTableModel modelTests;

    // Kusur Tablosu Bileşenleri (Artık TextArea değil)
    private JTable tableDefects;
    private DefaultTableModel modelDefects;

    public static ReportPreviewDialog getInstance(JFrame parent, int inspectionId, String plate, String ownerName) {
        if (instance == null) {
            instance = new ReportPreviewDialog(parent, inspectionId, plate, ownerName);
        } else {
            // Var olan instance'ı güncelle ve göster
            instance.dispose(); 
            instance = new ReportPreviewDialog(parent, inspectionId, plate, ownerName);
        }
        return instance;
    }

    private ReportPreviewDialog(JFrame parent, int inspectionId, String plate, String ownerName) {
        
        super(parent, "Rapor Önizleme", true);
        ThemeDecorator.getInstance().applyTheme(this);

        setSize(950, 800); // Tablolar sığsın diye biraz genişlettik
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- ANA PANEL ---
        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setBorder(new EmptyBorder(20, 20, 20, 20));

        // 1. BAŞLIK
        JLabel lblHeader = new JLabel("ARAÇ MUAYENE DETAYLI RAPORU");
        lblHeader.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblHeader.setForeground(new Color(41, 128, 185));
        lblHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JSeparator sep = new JSeparator();
        sep.setMaximumSize(new Dimension(1200, 2));
        sep.setForeground(new Color(41, 128, 185));

        // 2. BİLGİ KUTULARI
        JPanel pnlInfo = createInfoPanel("Muayene Bilgileri", 
                "<html><b>Rapor No:</b> " + inspectionId + "<br><b>Tarih:</b> " + new java.util.Date().toString() + "</html>");

        JPanel pnlVehicle = createInfoPanel("Araç & Müşteri", 
                "<html><b>Plaka:</b> " + plate + "<br><b>Sahip:</b> " + ownerName + "</html>");

        // 3. TEST SONUÇLARI TABLOSU
        JLabel lblTableTitle = new JLabel("1. BÖLÜM: Test Parametreleri ve Ölçümler");
        lblTableTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTableTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblTableTitle.setBorder(new EmptyBorder(10, 0, 5, 0));

        // Test Tablo Modeli
        String[] columnsTests = {"Test Adı", "Ölçülen Değer", "Referans Aralığı", "Durum"};
        modelTests = new DefaultTableModel(columnsTests, 0);
        tableTests = new JTable(modelTests);
        styleTestTable(tableTests);
        
        JScrollPane scrollPaneTests = new JScrollPane(tableTests);
        scrollPaneTests.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollPaneTests.setPreferredSize(new Dimension(900, 200));
        scrollPaneTests.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // 4. KUSURLAR BÖLÜMÜ (YENİ TABLO YAPISI)
        JLabel lblDefectsTitle = new JLabel("2. BÖLÜM: Tespit Edilen Kusurlar (Defect List)");
        lblDefectsTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDefectsTitle.setForeground(new Color(192, 57, 43)); // Kırmızı başlık
        lblDefectsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDefectsTitle.setBorder(new EmptyBorder(20, 0, 5, 0));

        // Kusur Tablo Modeli
        String[] columnsDefects = {"Kusur Kodu", "Kusur Açıklaması", "Kusur Derecesi"};
        modelDefects = new DefaultTableModel(columnsDefects, 0);
        tableDefects = new JTable(modelDefects);
        styleDefectTable(tableDefects); // Kusur tablosuna özel stil

        JScrollPane scrollDefects = new JScrollPane(tableDefects);
        scrollDefects.setAlignmentX(Component.LEFT_ALIGNMENT);
        scrollDefects.setPreferredSize(new Dimension(900, 150));
        scrollDefects.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));

        // 5. GENEL SONUÇ
        JPanel pnlResult = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlResult.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlResult.setMaximumSize(new Dimension(1200, 70));
        pnlResult.setBorder(new EmptyBorder(20, 0, 0, 0));
        
        JLabel lblResult = new JLabel("GENEL SONUÇ HESAPLANIYOR...");
        lblResult.setFont(new Font("Segoe UI", Font.BOLD, 26));
        pnlResult.add(lblResult);

        // --- BİLEŞENLERİ EKLE ---
        pnlContent.add(lblHeader);
        pnlContent.add(Box.createVerticalStrut(5));
        pnlContent.add(sep);
        pnlContent.add(Box.createVerticalStrut(15));
        pnlContent.add(pnlInfo);
        pnlContent.add(Box.createVerticalStrut(5));
        pnlContent.add(pnlVehicle);
        pnlContent.add(Box.createVerticalStrut(15));
        
        pnlContent.add(lblTableTitle);
        pnlContent.add(scrollPaneTests);
        
        pnlContent.add(lblDefectsTitle);     
        pnlContent.add(scrollDefects); // Artık tablo ekliyoruz
        
        pnlContent.add(Box.createVerticalStrut(10));
        pnlContent.add(pnlResult);

        // --- VERİLERİ ÇEK ---
        loadRealData(inspectionId, lblResult);

        // --- ALT BUTON ---
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnOk = new JButton("Kapat");
        btnOk.setPreferredSize(new Dimension(100, 35));
        btnOk.addActionListener(e -> dispose());
        pnlBottom.add(btnOk);

        add(new JScrollPane(pnlContent), BorderLayout.CENTER);
        add(pnlBottom, BorderLayout.SOUTH);
        ThemeDecorator.getInstance().applyTheme(this);
    }

    private void loadRealData(int inspectionId, JLabel lblResultLabel) {
        
        try (Connection conn = DBHelper.getConnection()) {
            
            // ---------------------------------------------------------
            // 1. ADIM: RESMİ SONUCU (STATUS) ÇEK
            // Kararı Trigger verdiği için doğrudan sonucu okuyoruz.
            // ---------------------------------------------------------
            String sqlStatus = "SELECT result_status FROM inspection WHERE inspection_id = ?";
            PreparedStatement psStatus = conn.prepareStatement(sqlStatus);
            psStatus.setInt(1, inspectionId);
            ResultSet rsStatus = psStatus.executeQuery();
            
            String dbResult = "BİLİNMİYOR";
            if (rsStatus.next()) {
                dbResult = rsStatus.getString("result_status");
            }

            // Etiketi veritabanındaki duruma göre ayarla
            if ("PASSED".equalsIgnoreCase(dbResult) || "GEÇTİ".equalsIgnoreCase(dbResult)) {
                lblResultLabel.setText("GENEL SONUÇ: MUAYENEDEN GEÇTİ");
                lblResultLabel.setForeground(new Color(39, 174, 96)); // Yeşil
            } else if ("FAILED".equalsIgnoreCase(dbResult) || "KALDI".equalsIgnoreCase(dbResult)) {
                lblResultLabel.setText("GENEL SONUÇ: MUAYENEDEN KALDI");
                lblResultLabel.setForeground(new Color(192, 57, 43)); // Kırmızı
            } else {
                lblResultLabel.setText("GENEL SONUÇ: " + dbResult);
            }

            // ---------------------------------------------------------
            // 2. ADIM: TEST DETAYLARINI TABLOYA DOLDUR
            // ---------------------------------------------------------
            String sqlTests = 
                "SELECT tp.test_name, tr.measured_value, tp.unit, tp.min_limit, tp.max_limit, tr.is_passed " +
                "FROM test_result tr " +
                "JOIN test_parameter tp ON tr.test_parameter_id = tp.test_parameter_id " +
                "WHERE tr.inspection_id = ?";
            
            PreparedStatement psTest = conn.prepareStatement(sqlTests);
            psTest.setInt(1, inspectionId);
            ResultSet rsTest = psTest.executeQuery();

            while (rsTest.next()) {
                String name = rsTest.getString("test_name");
                String val = rsTest.getString("measured_value") + " " + rsTest.getString("unit");
                
                String min = rsTest.getString("min_limit");
                String max = rsTest.getString("max_limit");
                String ref = (min != null ? min : "0") + " - " + (max != null ? max : "∞");
                
                boolean passed = rsTest.getBoolean("is_passed");
                modelTests.addRow(new Object[]{ name, val, ref, passed ? "GEÇTİ" : "KALDI" });
            }

            // ---------------------------------------------------------
            // 3. ADIM: KUSURLARI YENİ TABLOYA DOLDUR
            // ---------------------------------------------------------
            String sqlDefects = 
                "SELECT d.defect_code, d.description, d.severity " +
                "FROM inspection_defect id " +
                "JOIN defect_catalog d ON id.defect_id = d.defect_id " +
                "WHERE id.inspection_id = ?";
                
            PreparedStatement psDefect = conn.prepareStatement(sqlDefects);
            psDefect.setInt(1, inspectionId);
            ResultSet rsDefect = psDefect.executeQuery();
            
            boolean hasDefects = false;

            while(rsDefect.next()) {
                hasDefects = true;
                String code = rsDefect.getString("defect_code");
                String desc = rsDefect.getString("description");
                String severity = rsDefect.getString("severity");

                // Tabloya satır ekle
                modelDefects.addRow(new Object[]{ code, desc, severity });
            }
            
            if (!hasDefects) {
                // Eğer kusur yoksa tabloya boş bilgilendirme satırı atabiliriz
                modelDefects.addRow(new Object[]{"-", "Kusur kaydı bulunmamaktadır.", "-"});
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Rapor verisi çekilirken hata: " + e.getMessage());
        }
    }

    // --- YARDIMCI METODLAR ---
    private JPanel createInfoPanel(String title, String htmlContent) {
        JPanel pnl = new JPanel(new BorderLayout());
        pnl.setBackground(new Color(250, 250, 250));
        pnl.setBorder(new CompoundBorder(
                new LineBorder(Color.LIGHT_GRAY, 1),
                new EmptyBorder(10, 15, 10, 15)
        ));
        pnl.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnl.setMaximumSize(new Dimension(1000, 80));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        JLabel lblContent = new JLabel(htmlContent);
        lblContent.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblContent.setBorder(new EmptyBorder(5, 0, 0, 0));

        pnl.add(lblTitle, BorderLayout.NORTH);
        pnl.add(lblContent, BorderLayout.CENTER);
        return pnl;
    }

    // Test Tablosu Stili
    private void styleTestTable(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
        
        table.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String status = (String) value;
                if ("GEÇTİ".equals(status)) {
                    c.setForeground(new Color(39, 174, 96));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                }
                return c;
            }
        });
    }

    // Kusur Tablosu Stili (Özel Renklendirme)
    private void styleDefectTable(JTable table) {
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(192, 57, 43)); // Kırmızı Başlık
        table.getTableHeader().setForeground(Color.WHITE);
        table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);

        // Sütun genişlikleri
        table.getColumnModel().getColumn(0).setPreferredWidth(100); // Kod
        table.getColumnModel().getColumn(1).setPreferredWidth(500); // Açıklama
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Derece

        // Kusur Derecesine Göre Renklendirme
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                // Derece sütunu (2. index) değerini al
                String severity = (String) table.getValueAt(row, 2);
                
                if (severity != null && (severity.contains("AĞIR") || severity.contains("Major") || severity.contains("EMNİYETSİZ") || severity.contains("Unsafe"))) {
                    c.setForeground(Color.RED);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if (severity != null && (severity.contains("HAFİF") || severity.contains("Minor"))) {
                    c.setForeground(new Color(211, 84, 0)); // Turuncu
                } else {
                    c.setForeground(Color.BLACK);
                }
                return c;
            }
        });
    }
}