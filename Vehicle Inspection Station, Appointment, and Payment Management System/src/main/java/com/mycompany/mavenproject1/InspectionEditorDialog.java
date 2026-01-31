package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;

public class InspectionEditorDialog extends JDialog {
    public static InspectionEditorDialog instance;
    private int inspectionId;

    // TESTLER
    private JTable tblTests;
    private DefaultTableModel modelTests;

    // KUSURLAR
    private JTable tblDefects;
    private DefaultTableModel modelDefects;

    private JComboBox<ComboItem> cmbCategory;
    private JComboBox<ComboItem> cmbDefect;
    private JLabel lblSeverityDisplay;

    private JTextArea txtNotes;

    public static InspectionEditorDialog getInstance(int id) {
        if (instance == null || !instance.isDisplayable()) {
            instance = new InspectionEditorDialog(id);
        }
        return instance;
    }

    private InspectionEditorDialog(int id) {
        this.inspectionId = id;
        setTitle("Muayene İşlem Merkezi - ID: " + id);
        setSize(1000, 750);
        setLocationRelativeTo(null);
        setModal(true);
        setLayout(new BorderLayout(10, 10));

        // =================== TOP ===================
        JPanel pnlInfo = new JPanel(new GridLayout(2, 4, 10, 10));
        pnlInfo.setBorder(
                new CompoundBorder(
                        new EmptyBorder(10, 10, 0, 10),
                        new TitledBorder("Araç ve Ruhsat Bilgileri")
                )
        );
        loadInfo(pnlInfo);
        add(pnlInfo, BorderLayout.NORTH);

        // =================== CENTER ===================
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(350);

        // -------- TESTLER --------
        modelTests = new DefaultTableModel(
                new Object[]{"ID", "Test Parametresi", "Min", "Max", "Birim", "Ölçülen Değer", "GEÇTİ Mİ"},
                0
        ) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 5; // Sadece ölçülen değer editlenebilir
            }

            @Override
            public Class<?> getColumnClass(int c) {
                return c == 6 ? Boolean.class : Object.class; // 6. sütun Checkbox olsun
            }

            // --- OTOMATİK HESAPLAMA ALGORİTMASI ---
            @Override
            public void setValueAt(Object aValue, int row, int column) {
                super.setValueAt(aValue, row, column); // Önce girilen değeri hücreye yaz

                // Eğer değiştirilen sütun "Ölçülen Değer" (5. indeks) ise hesapla
                if (column == 5) {
                    try {
                        // Eğer değer boşaltıldıysa tiki kaldır
                        if (aValue == null || aValue.toString().trim().isEmpty()) {
                            super.setValueAt(false, row, 6);
                            return;
                        }

                        // Girilen değeri sayıya çevir (Virgül girilirse noktaya çeviriyoruz hata almamak için)
                        String valStr = aValue.toString().replace(",", ".");
                        java.math.BigDecimal measured = new java.math.BigDecimal(valStr);

                        // Min ve Max değerlerini tablodan al
                        java.math.BigDecimal min = (java.math.BigDecimal) getValueAt(row, 2);
                        java.math.BigDecimal max = (java.math.BigDecimal) getValueAt(row, 3);

                        boolean isPassed = true;

                        // Min kontrolü (measured < min ise başarısız)
                        if (min != null && measured.compareTo(min) < 0) {
                            isPassed = false;
                        }
                        // Max kontrolü (measured > max ise başarısız)
                        if (max != null && measured.compareTo(max) > 0) {
                            isPassed = false;
                        }

                        // Sonucu 6. sütuna (Checkbox) yaz
                        super.setValueAt(isPassed, row, 6);

                    } catch (NumberFormatException e) {
                        // Eğer kullanıcı "abc" gibi sayı olmayan bir şey girerse tiki kaldır
                        super.setValueAt(false, row, 6);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        tblTests = new JTable(modelTests);
        tblTests.setRowHeight(25);
        tblTests.getColumnModel().getColumn(0).setMaxWidth(50);

        loadTestParams();

        JPanel pnlTests = new JPanel(new BorderLayout());
        pnlTests.setBorder(new TitledBorder("1. ADIM: TEST ÖLÇÜMLERİ"));
        pnlTests.add(new JScrollPane(tblTests), BorderLayout.CENTER);

        split.setTopComponent(pnlTests);

        // -------- KUSURLAR --------
        JPanel pnlDefects = new JPanel(new BorderLayout(10, 10));
        pnlDefects.setBorder(new TitledBorder("2. ADIM: TESPİT EDİLEN KUSURLAR"));

        JPanel pnlSelect = new JPanel(new GridLayout(2, 3, 10, 5));
        pnlSelect.add(new JLabel("Kategori"));
        pnlSelect.add(new JLabel("Kusur"));
        pnlSelect.add(new JLabel("Derece"));

        cmbCategory = new JComboBox<>();
        loadCategoriesManually();
        pnlSelect.add(cmbCategory);

        cmbDefect = new JComboBox<>();
        pnlSelect.add(cmbDefect);

        lblSeverityDisplay = new JLabel("-");
        lblSeverityDisplay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlSelect.add(lblSeverityDisplay);

        cmbCategory.addActionListener(e -> {
            ComboItem cat = (ComboItem) cmbCategory.getSelectedItem();
            if (cat != null) loadDefectsByCodePrefix(String.valueOf(cat.getId()));
        });

        cmbDefect.addActionListener(e -> updateSeverityLabel());

        JPanel pnlDefectBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnAddDefect = new JButton("+ Listeye Ekle");
        JButton btnRemoveDefect = new JButton("- Seçileni Çıkar");

        btnAddDefect.addActionListener(e -> addDefectToTable());
        btnRemoveDefect.addActionListener(e -> removeDefectFromTable());

        pnlDefectBtns.add(btnAddDefect);
        pnlDefectBtns.add(btnRemoveDefect);

        JPanel pnlNorth = new JPanel(new BorderLayout());
        pnlNorth.add(pnlSelect, BorderLayout.CENTER);
        pnlNorth.add(pnlDefectBtns, BorderLayout.SOUTH);

        pnlDefects.add(pnlNorth, BorderLayout.NORTH);

        modelDefects = new DefaultTableModel(
                new Object[]{"Defect ID", "Kod", "Açıklama", "Seviye", "Not"},
                0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return c == 4; }
        };

        tblDefects = new JTable(modelDefects);
        loadExistingDefects();

        pnlDefects.add(new JScrollPane(tblDefects), BorderLayout.CENTER);

        split.setBottomComponent(pnlDefects);
        add(split, BorderLayout.CENTER);

        // =================== BOTTOM ===================
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 10));
        pnlBottom.setBorder(new EmptyBorder(10, 10, 10, 10));

        txtNotes = new JTextArea(3, 50);
        pnlBottom.add(new JScrollPane(txtNotes), BorderLayout.CENTER);

        // --- BUTON PANELİ ---
        JPanel pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));

        // 1. Buton: Sadece Kaydet (İşlemi kapatmaz)
        JButton btnSaveOnly = new JButton("Kaydet (Devam Et)");
        btnSaveOnly.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSaveOnly.addActionListener(e -> saveFullInspectionData(false)); // FALSE gönderiyoruz

        // 2. Buton: Bitir (İşlemi kapatır)
        JButton btnFinish = new JButton("MUAYENEYİ BİTİR");
        btnFinish.setBackground(new Color(46, 204, 113)); // Yeşil renk
        btnFinish.setForeground(Color.WHITE);
        btnFinish.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnFinish.addActionListener(e -> saveFullInspectionData(true)); // TRUE gönderiyoruz

        pnlButtons.add(btnSaveOnly);
        pnlButtons.add(btnFinish);

        pnlBottom.add(pnlButtons, BorderLayout.EAST);

        add(pnlBottom, BorderLayout.SOUTH);
        ThemeDecorator.getInstance().applyTheme(this);
    }

    // =================== LOADERS ===================
    private void loadInfo(JPanel p) {
        // Önce paneli temizle (tekrarlayan eklemeler olmasın)
        p.removeAll();
        
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {

            // SQL DÜZELTİLDİ:
            // 1. INNER JOIN yerine LEFT JOIN kullanıldı (Biri eksikse diğerleri gelsin diye).
            // 2. Owner bağlantısı 'license_registration' üzerinden yapıldı (Senin ilk attığın çalışan SQL'deki gibi).
            String sql =
                "SELECT " +
                "  l.plate_number, " +
                "  v.chassis_number, " +
                "  b.brand_name, " +
                "  m.model_name, " +
                "  COALESCE(ind.first_name || ' ' || ind.last_name, co.company_name) AS owner_name, " +
                "  i.technician_notes " +
                "FROM inspection i " +
                "JOIN appointment a ON i.appointment_id = a.appointment_id " +
                "JOIN vehicle v ON a.vehicle_id = v.vehicle_id " +
                "LEFT JOIN license_registration l ON v.vehicle_id = l.vehicle_id " +  // LEFT JOIN
                "LEFT JOIN model m ON v.model_id = m.model_id " +
                "LEFT JOIN brand b ON m.brand_id = b.brand_id " +
                // Owner bağlantısını vehicle_owner yerine license_registration üzerinden deniyoruz (Daha yaygın yapı)
                "LEFT JOIN owner o ON l.owner_id = o.owner_id " + 
                "LEFT JOIN individual_owner ind ON o.owner_id = ind.owner_id " +
                "LEFT JOIN company_owner co ON o.owner_id = co.owner_id " +
                "WHERE i.inspection_id = " + inspectionId;

            ResultSet rs = stmt.executeQuery(sql);
            
            if (rs.next()) {
                // Verileri güvenli şekilde al (Null gelirse "Yok" yazsın)
                String plaka = rs.getString("plate_number");
                String marka = rs.getString("brand_name");
                String model = rs.getString("model_name");
                String sasi = rs.getString("chassis_number");
                String sahip = rs.getString("owner_name");
                String notlar = rs.getString("technician_notes");

                // Label'ları ekle
                JLabel lblPlaka = new JLabel("Plaka: " + (plaka != null ? plaka : "-"));
                lblPlaka.setFont(new Font("Segoe UI", Font.BOLD, 12));
                p.add(lblPlaka);

                JLabel lblArac = new JLabel("Araç: " + (marka != null ? marka : "") + " " + (model != null ? model : ""));
                lblArac.setFont(new Font("Segoe UI", Font.BOLD, 12));
                p.add(lblArac);

                p.add(new JLabel("Şasi No: " + (sasi != null ? sasi : "-")));
                p.add(new JLabel("Sahip: " + (sahip != null ? sahip : "-")));

                // Notları text alanına doldur
                if (notlar != null) {
                    txtNotes.setText(notlar);
                }
            } else {
                // Sorgu çalıştı ama kayıt bulunamadıysa
                p.add(new JLabel("HATA: Bu ID'ye ait veri bulunamadı!"));
            }
            
            // Görünümü yenile (Swing bazen sonradan eklenenleri çizmez)
            p.revalidate();
            p.repaint();

        } catch (Exception e) {
            e.printStackTrace();
            p.add(new JLabel("Veritabanı Hatası!"));
        }
    }

    private void loadTestParams() {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql =
                "SELECT tp.test_parameter_id, tp.test_name, tp.min_limit, tp.max_limit, tp.unit, " +
                "tr.measured_value, tr.is_passed " +
                "FROM test_parameter tp " +
                "LEFT JOIN test_result tr ON tp.test_parameter_id=tr.test_parameter_id " +
                "AND tr.inspection_id=" + inspectionId +
                " ORDER BY tp.test_parameter_id";

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                modelTests.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getBigDecimal(3),
                        rs.getBigDecimal(4),
                        rs.getString(5),
                        rs.getObject(6),
                        rs.getObject(7) != null && rs.getBoolean(7)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadExistingDefects() {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement()) {

            String sql =
                "SELECT d.defect_id, d.defect_code, d.description, d.severity, id.location_note " +
                "FROM inspection_defect id " +
                "JOIN defect_catalog d ON id.defect_id=d.defect_id " +
                "WHERE id.inspection_id=" + inspectionId;

            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                modelDefects.addRow(new Object[]{
                        rs.getInt(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4),
                        rs.getString(5)
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =================== KUSUR YÖNETİMİ ===================
    private void loadCategoriesManually() {
        cmbCategory.removeAllItems();
        for (int i = 1; i <= 8; i++) {
            cmbCategory.addItem(new ComboItem(i, i + ". Kategori"));
        }
    }

    private void loadDefectsByCodePrefix(String prefix) {
        cmbDefect.removeAllItems();
        String sql =
            "SELECT defect_id, defect_code, description, severity " +
            "FROM defect_catalog WHERE defect_code LIKE ? ORDER BY defect_code";
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, prefix + ".%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = "[" + rs.getString(2) + "] " +
                               rs.getString(3) + " (" + rs.getString(4) + ")";
                cmbDefect.addItem(new ComboItem(rs.getInt(1), label));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSeverityLabel() {
        ComboItem selected = (ComboItem) cmbDefect.getSelectedItem();
        if (selected == null) {
            lblSeverityDisplay.setText("-");
            return;
        }
        String t = selected.getName();
        if (t.contains("AĞIR")) {
            lblSeverityDisplay.setText("AĞIR KUSUR");
            lblSeverityDisplay.setForeground(Color.RED);
        } else if (t.contains("EMNİYETSİZ")) {
            lblSeverityDisplay.setText("EMNİYETSİZ");
            lblSeverityDisplay.setForeground(new Color(139, 0, 0));
        } else {
            lblSeverityDisplay.setText("HAFİF KUSUR");
            lblSeverityDisplay.setForeground(new Color(39, 174, 96));
        }
    }

    private void addDefectToTable() {
        ComboItem d = (ComboItem) cmbDefect.getSelectedItem();
        if (d == null) return;

        for (int i = 0; i < modelDefects.getRowCount(); i++) {
            if ((int) modelDefects.getValueAt(i, 0) == d.getId()) {
                JOptionPane.showMessageDialog(this, "Bu kusur zaten ekli.");
                return;
            }
        }

        String raw = d.getName();
        String code = raw.substring(raw.indexOf("[") + 1, raw.indexOf("]"));
        String desc = raw.substring(raw.indexOf("]") + 2, raw.lastIndexOf("(")).trim();
        String sev = raw.substring(raw.lastIndexOf("(") + 1, raw.lastIndexOf(")"));

        modelDefects.addRow(new Object[]{d.getId(), code, desc, sev, ""});
    }

    private void removeDefectFromTable() {
        int r = tblDefects.getSelectedRow();
        if (r != -1) modelDefects.removeRow(r);
    }

    // =================== SAVE ===================
    private void saveFullInspectionData(boolean isFinished) {
        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. TESTLERİ GÜNCELLE
                conn.createStatement().executeUpdate(
                        "DELETE FROM test_result WHERE inspection_id=" + inspectionId);

                PreparedStatement psTest = conn.prepareStatement(
                        "INSERT INTO test_result (inspection_id,test_parameter_id,measured_value) VALUES (?,?,?)");

                for (int i = 0; i < modelTests.getRowCount(); i++) {
                    Object val = modelTests.getValueAt(i, 5);
                    if (val != null && !val.toString().isBlank()) {
                        psTest.setInt(1, inspectionId);
                        psTest.setInt(2, (int) modelTests.getValueAt(i, 0));
                        psTest.setBigDecimal(3, new java.math.BigDecimal(val.toString().replace(",", ".")));
                        psTest.addBatch();
                    }
                }
                psTest.executeBatch();

                // 2. KUSURLARI GÜNCELLE
                conn.createStatement().executeUpdate(
                        "DELETE FROM inspection_defect WHERE inspection_id=" + inspectionId);

                PreparedStatement psDef = conn.prepareStatement(
                        "INSERT INTO inspection_defect (inspection_id,defect_id,location_note) VALUES (?,?,?)");

                for (int i = 0; i < modelDefects.getRowCount(); i++) {
                    psDef.setInt(1, inspectionId);
                    psDef.setInt(2, (int) modelDefects.getValueAt(i, 0));
                    psDef.setString(3, (String) modelDefects.getValueAt(i, 4));
                    psDef.addBatch();
                }
                psDef.executeBatch();

                // 3. MUAYENE TABLOSUNU GÜNCELLE (KRİTİK KISIM)
                String sqlUpdate;
                if (isFinished) {
                    // Eğer bitiriliyorsa: Bitiş zamanını (end_time) ŞU AN yap.
                    sqlUpdate = "UPDATE inspection SET technician_notes=?, end_time=NOW() WHERE inspection_id=?";
                } else {
                    // Sadece kaydediliyorsa: Bitiş zamanına dokunma (NULL kalsın veya eskisini korusun)
                    sqlUpdate = "UPDATE inspection SET technician_notes=? WHERE inspection_id=?";
                }

                PreparedStatement psUpd = conn.prepareStatement(sqlUpdate);
                psUpd.setString(1, txtNotes.getText());
                psUpd.setInt(2, inspectionId);
                psUpd.executeUpdate();

                conn.commit(); // Veritabanına işle

                // 4. KULLANICIYA GERİ BİLDİRİM
                if (isFinished) {
                    // Bitirme işlemi ise sonucu göster ve pencereyi kapat
                    String finalStatus = "BİLİNMİYOR";
                    PreparedStatement psCheck = conn.prepareStatement("SELECT result_status FROM inspection WHERE inspection_id=?");
                    psCheck.setInt(1, inspectionId);
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next()) finalStatus = rsCheck.getString(1);

                    String msg = "Muayene tamamlandı ve kapatıldı!\nSistem Kararı: " +
                            ("PASSED".equals(finalStatus) ? "GEÇTİ (Başarılı)" : "KALDI (Başarısız)");

                    JOptionPane.showMessageDialog(this, msg, "İşlem Tamam", JOptionPane.INFORMATION_MESSAGE);
                    dispose(); // Pencereyi kapat

                } else {
                    // Sadece kaydetme işlemi ise bilgi ver ama pencereyi kapatma
                    JOptionPane.showMessageDialog(this, "Değişiklikler taslak olarak kaydedildi.\nİşleme devam edebilirsiniz.", "Kaydedildi", JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (Exception ex) {
                conn.rollback();
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Hata oluştu: " + ex.getMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + e.getMessage());
        }
    }
}
