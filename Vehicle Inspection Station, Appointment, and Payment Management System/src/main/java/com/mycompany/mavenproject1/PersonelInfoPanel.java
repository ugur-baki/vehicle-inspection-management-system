package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PersonelInfoPanel extends JPanel {

    private static PersonelInfoPanel instance;

    private JTable tblPersonel;
    private DefaultTableModel modelPersonel;

    // Form Alanları
    private JTextField txtName, txtSurname, txtDepartment;
    private JTextField txtHireDate, txtWorkShift, txtSalary;
    
    // Değişken Alanlar
    private JTextField txtDetail;           // Damga No / Uzmanlık
    private JTextField txtExperience;       // Deneyim Yılı (Ortak)
    private JTextField txtCertification;    // Sertifika (Sadece Teknisyen)
    private JTextField txtOfficeAuthCode;   // Ofis Yetki Kodu
    private JTextField txtOfficeQualLevel;  // Ofis Yetki Seviyesi

    private JComboBox<String> cmbType;
    private JComboBox<ComboItem> cmbStation;

    // Dinamik Etiketler
    private JLabel lblDetail;        
    private JLabel lblExperience;    
    private JLabel lblCertification; 

    private JButton btnAdd, btnUpdate, btnDelete, btnClear;

    private int selectedPersonelId = -1;

    public static PersonelInfoPanel getInstance() {
        if (instance == null) {
            instance = new PersonelInfoPanel();
        }
        return instance;
    }

    private PersonelInfoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ==========================================
        // 1. FORM ALANI
        // ==========================================
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(createStyledBorder("Personel Yönetimi"));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // ---- SATIR 1: Ad - Soyad
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ad:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtName = new JTextField(15);
        pnlForm.add(txtName, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Soyad:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtSurname = new JTextField(15);
        pnlForm.add(txtSurname, gbc);

        // ---- SATIR 2: Departman - Tip
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        pnlForm.add(new JLabel("Departman:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtDepartment = new JTextField(15);
        pnlForm.add(txtDepartment, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Personel Tipi:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        cmbType = new JComboBox<>(new String[]{"INSPECTOR", "TECHNICIAN", "OFFICE"});
        pnlForm.add(cmbType, gbc);

        // ---- SATIR 3: Tarih - Vardiya
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("İşe Giriş (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtHireDate = new JTextField(15);
        pnlForm.add(txtHireDate, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Vardiya:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtWorkShift = new JTextField(15);
        pnlForm.add(txtWorkShift, gbc);

        // ---- SATIR 4: Maaş
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        pnlForm.add(new JLabel("Maaş:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtSalary = new JTextField(15);
        txtSalary.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar()) && evt.getKeyChar() != '.') {
                    evt.consume();
                }
            }
        });
        pnlForm.add(txtSalary, gbc);

        // ---- SATIR 5: Detay (Sol) - Deneyim (Sağ)
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        lblDetail = new JLabel("Damga No:"); 
        pnlForm.add(lblDetail, gbc);

        gbc.gridx = 1; gbc.weightx = 1;
        txtDetail = new JTextField(15);
        pnlForm.add(txtDetail, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        lblExperience = new JLabel("Deneyim (Yıl):"); 
        pnlForm.add(lblExperience, gbc);
        
        gbc.gridx = 3; gbc.weightx = 1;
        txtExperience = new JTextField(15);
        txtExperience.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar())) evt.consume();
            }
        });
        pnlForm.add(txtExperience, gbc);

        // ---- SATIR 6: Sertifika (Sol)
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0;
        lblCertification = new JLabel("Sertifika/Seviye:");
        pnlForm.add(lblCertification, gbc);
        
        gbc.gridx = 1; gbc.weightx = 1;
        txtCertification = new JTextField(15);
        pnlForm.add(txtCertification, gbc);
        
        // ---- SATIR 7: Ofis Yetki Kodları
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ofis Yetki Kodu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        txtOfficeAuthCode = new JTextField(15);
        pnlForm.add(txtOfficeAuthCode, gbc);

        gbc.gridx = 2; gbc.weightx = 0;
        pnlForm.add(new JLabel("Ofis Yetki Seviyesi:"), gbc);
        gbc.gridx = 3; gbc.weightx = 1;
        txtOfficeQualLevel = new JTextField(15);
        pnlForm.add(txtOfficeQualLevel, gbc);

        // ---- SATIR 8: İstasyon Seçimi
        gbc.gridx = 0; gbc.gridy = 7; gbc.weightx = 0;
        pnlForm.add(new JLabel("İstasyon:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; gbc.weightx = 1;
        cmbStation = new JComboBox<>();
        cmbStation.setModel(DBHelper.getStationsModel());
        pnlForm.add(cmbStation, gbc);
        gbc.gridwidth = 1;

        // ---- BUTONLAR
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnAdd = new JButton("Ekle");
        btnUpdate = new JButton("Güncelle");
        btnDelete = new JButton("Sil");
        btnClear = new JButton("Temizle");

        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);

        pnlBtns.add(btnAdd);
        pnlBtns.add(btnUpdate);
        pnlBtns.add(btnDelete);
        pnlBtns.add(btnClear);

        gbc.gridx = 0; gbc.gridy = 8; gbc.gridwidth = 4; gbc.weightx = 1;
        pnlForm.add(pnlBtns, gbc);

        add(pnlForm, BorderLayout.NORTH);

        // ==========================================
        // 2. TABLO ALANI ( Deneyim ve Sertifika sütunları kaldırıldı)
        // ==========================================
        String[] columns = {"ID", "Ad", "Soyad", "Departman", "Tip", "Detay", "İşe Giriş", "Vardiya", "Maaş", "İstasyon"};
        
        modelPersonel = new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        tblPersonel = new JTable(modelPersonel);
        DBHelper.styleTable(tblPersonel);
        add(new JScrollPane(tblPersonel), BorderLayout.CENTER);

        // ==========================================
        // 3. OLAYLAR (EVENTS)
        // ==========================================
        
        cmbType.addActionListener(e -> updateDetailLabel());
        cmbStation.addActionListener(e -> loadData());

        tblPersonel.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblPersonel.getSelectedRow() != -1) {
                fillFormFromSelection();
            }
        });

        btnAdd.addActionListener(e -> addRecord());
        btnUpdate.addActionListener(e -> updateRecord());
        btnDelete.addActionListener(e -> deleteRecord());
        btnClear.addActionListener(e -> clearForm());

        // Başlangıç yüklemeleri
        loadData();
        updateDetailLabel();
    }

    // --- ARAYÜZ METODLARI ---

    private void updateDetailLabel() {
        String type = (String) cmbType.getSelectedItem();

        txtDetail.setEnabled(false);
        txtOfficeAuthCode.setEnabled(false);
        txtOfficeQualLevel.setEnabled(false);
        txtExperience.setEnabled(false);
        txtCertification.setEnabled(false);
        
        lblDetail.setText("-");
        lblExperience.setText(""); 
        lblCertification.setText("");

        if ("INSPECTOR".equals(type)) { 
            lblDetail.setText("Damga No:");
            txtDetail.setEnabled(true);
            
        } else if ("TECHNICIAN".equals(type)) { 
            lblDetail.setText("Uzmanlık Alanı:");
            txtDetail.setEnabled(true);
            
            lblExperience.setText("Deneyim (Yıl):");
            txtExperience.setEnabled(true);
            
            lblCertification.setText("Sertifika Seviyesi:");
            txtCertification.setEnabled(true);
            
        } else { // OFFICE
            lblDetail.setText("-"); 
            txtDetail.setText("");
            
            lblExperience.setText("Deneyim (Yıl):");
            txtExperience.setEnabled(true);
            
            lblCertification.setText(""); 
            
            txtOfficeAuthCode.setEnabled(true);
            txtOfficeQualLevel.setEnabled(true);
        }
    }

    private void loadData() {
        modelPersonel.setRowCount(0);
        ComboItem selectedStation = (ComboItem) cmbStation.getSelectedItem();
        boolean filterByStation = selectedStation != null && selectedStation.getId() > 0;

        String sql =
            "SELECT p.personnel_id, p.first_name, p.last_name, p.department, p.personnel_type, " +
            "COALESCE(i.stamp_number, t.expertise_area, os.authorization_code, '-'), " +
            "p.hire_date, p.work_shift, p.salary, ist.station_name " +
            "FROM personnel p " +
            "LEFT JOIN inspector i ON p.personnel_id = i.personnel_id " +
            "LEFT JOIN technician t ON p.personnel_id = t.personnel_id " +
            "LEFT JOIN office_staff os ON p.personnel_id = os.personnel_id " +
            "LEFT JOIN inspection_station ist ON p.station_id = ist.station_id ";

        if (filterByStation) {
            sql += "WHERE p.station_id = ? ";
        }
        sql += "ORDER BY p.first_name";

        try (Connection c = DBHelper.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            if (filterByStation) ps.setInt(1, selectedStation.getId());

            ResultSet r = ps.executeQuery();
            while (r.next()) {
                modelPersonel.addRow(new Object[]{
                        r.getInt(1),
                        r.getString(2),
                        r.getString(3),
                        r.getString(4),
                        r.getString(5),
                        r.getString(6), 
                        r.getDate(7),
                        r.getString(8),
                        r.getDouble(9),
                        r.getString(10)
                        // Deneyim ve Sertifika sütunları kaldırıldı
                });
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addRecord() {
        ComboItem station = (ComboItem) cmbStation.getSelectedItem();
        
        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false); 

            String type = (String) cmbType.getSelectedItem();

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO personnel (first_name,last_name,department,personnel_type,hire_date,work_shift,salary,station_id) " +
                "VALUES (?,?,?,?,?,?,?,?) RETURNING personnel_id"
            );

            ps.setString(1, txtName.getText());
            ps.setString(2, txtSurname.getText());
            ps.setString(3, txtDepartment.getText());
            ps.setString(4, type);
            try { ps.setDate(5, Date.valueOf(txtHireDate.getText())); } catch (Exception d) { ps.setDate(5, null); }
            ps.setString(6, txtWorkShift.getText());
            try { ps.setDouble(7, Double.parseDouble(txtSalary.getText())); } catch (Exception n) { ps.setDouble(7, 0.0); }
            
            if (station != null && station.getId() > 0) ps.setInt(8, station.getId());
            else ps.setNull(8, Types.INTEGER);

            ResultSet rs = ps.executeQuery();
            rs.next();
            int pid = rs.getInt(1);

            int expYears = 0;
            try { expYears = Integer.parseInt(txtExperience.getText()); } catch (Exception e) {}

            if ("INSPECTOR".equals(type)) {
                if (txtDetail.getText().isEmpty()) throw new SQLException("Müfettiş için Damga No zorunludur!");
                PreparedStatement p = conn.prepareStatement("INSERT INTO inspector (personnel_id, stamp_number) VALUES (?,?)");
                p.setInt(1, pid);
                p.setString(2, txtDetail.getText());
                p.executeUpdate();
            } else if ("TECHNICIAN".equals(type)) {
                PreparedStatement p = conn.prepareStatement("INSERT INTO technician (personnel_id, expertise_area, experience_years, certification_level) VALUES (?,?,?,?)");
                p.setInt(1, pid);
                p.setString(2, txtDetail.getText());
                p.setInt(3, expYears);
                p.setString(4, txtCertification.getText());
                p.executeUpdate();
            } else { // OFFICE
                DBHelper.upsertOfficeStaff(conn, pid, txtOfficeAuthCode.getText(), txtOfficeQualLevel.getText(), expYears);
            }

            conn.commit();
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Personel eklendi.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    private void updateRecord() {
        if (selectedPersonelId == -1) return;

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement ps = conn.prepareStatement(
                "UPDATE personnel SET first_name=?,last_name=?,department=?,hire_date=?,work_shift=?,salary=?,station_id=? WHERE personnel_id=?"
            );

            ps.setString(1, txtName.getText());
            ps.setString(2, txtSurname.getText());
            ps.setString(3, txtDepartment.getText());
            try { ps.setDate(4, Date.valueOf(txtHireDate.getText())); } catch (Exception d) { ps.setDate(4, null); }
            ps.setString(5, txtWorkShift.getText());
            try { ps.setDouble(6, Double.parseDouble(txtSalary.getText())); } catch (Exception n) { ps.setDouble(6, 0.0); }
            
            ComboItem station = (ComboItem) cmbStation.getSelectedItem();
            if (station != null && station.getId() > 0) ps.setInt(7, station.getId());
            else ps.setNull(7, Types.INTEGER);
            
            ps.setInt(8, selectedPersonelId);
            ps.executeUpdate();

            // Alt tablo güncelleme
            String type = (String) cmbType.getSelectedItem();
            int expYears = 0;
            try { expYears = Integer.parseInt(txtExperience.getText()); } catch (Exception e) {}

            if ("OFFICE".equals(type)) {
                DBHelper.upsertOfficeStaff(conn, selectedPersonelId, txtOfficeAuthCode.getText(), txtOfficeQualLevel.getText(), expYears);
            } else if ("INSPECTOR".equals(type)) {
                PreparedStatement p = conn.prepareStatement("INSERT INTO inspector (personnel_id, stamp_number) VALUES (?,?) ON CONFLICT (personnel_id) DO UPDATE SET stamp_number=EXCLUDED.stamp_number");
                p.setInt(1, selectedPersonelId);
                p.setString(2, txtDetail.getText());
                p.executeUpdate();
            } else if ("TECHNICIAN".equals(type)) {
                PreparedStatement p = conn.prepareStatement("INSERT INTO technician (personnel_id, expertise_area, experience_years, certification_level) VALUES (?,?,?,?) ON CONFLICT (personnel_id) DO UPDATE SET expertise_area=EXCLUDED.expertise_area, experience_years=EXCLUDED.experience_years, certification_level=EXCLUDED.certification_level");
                p.setInt(1, selectedPersonelId);
                p.setString(2, txtDetail.getText());
                p.setInt(3, expYears);
                p.setString(4, txtCertification.getText());
                p.executeUpdate();
            }

            conn.commit();
            loadData();
            clearForm();
            JOptionPane.showMessageDialog(this, "Personel güncellendi.");

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage());
        }
    }

    //  SİLME METODU (Önce alt tablo, sonra üst tablo)
    private void deleteRecord() {
        if (selectedPersonelId == -1) return;
        if(JOptionPane.showConfirmDialog(this, "Bu personeli silmek istediğine emin misin?\nNot: Personel tamamen silinecektir.", "Onay", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Önce alt tablolardan sil (Sırayla hepsini dene, hata verirse pas geç veya hepsini sil)
                conn.createStatement().executeUpdate("DELETE FROM inspector WHERE personnel_id=" + selectedPersonelId);
                conn.createStatement().executeUpdate("DELETE FROM technician WHERE personnel_id=" + selectedPersonelId);
                conn.createStatement().executeUpdate("DELETE FROM office_staff WHERE personnel_id=" + selectedPersonelId);
                
                // Sonra ana tablodan sil
                conn.createStatement().executeUpdate("DELETE FROM personnel WHERE personnel_id=" + selectedPersonelId);
                
                conn.commit();
                loadData();
                clearForm();
                JOptionPane.showMessageDialog(this, "Personel başarıyla silindi.");
            } catch (SQLException ex) {
                conn.rollback();
                if (ex.getMessage().contains("violation")) {
                    JOptionPane.showMessageDialog(this, "Bu personel geçmişte işlem yaptığı için silinemiyor!\nSistem bütünlüğü için kayıt korunmalıdır.", "Silinemedi", JOptionPane.WARNING_MESSAGE);
                } else {
                    throw ex;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Silme hatası: " + e.getMessage());
        }
    }

    //  SEÇİM METODU (Veritabanından eksik detayları çeker)
    private void fillFormFromSelection() {
        int r = tblPersonel.getSelectedRow();
        selectedPersonelId = (int) tblPersonel.getValueAt(r, 0);
        
        txtName.setText(tblPersonel.getValueAt(r, 1).toString());
        txtSurname.setText(tblPersonel.getValueAt(r, 2).toString());
        txtDepartment.setText(tblPersonel.getValueAt(r, 3).toString());
        
        String type = tblPersonel.getValueAt(r, 4).toString();
        cmbType.setSelectedItem(type);
        updateDetailLabel(); 

        // Ortak Alanlar
        txtHireDate.setText(tblPersonel.getValueAt(r, 6) != null ? tblPersonel.getValueAt(r, 6).toString() : "");
        txtWorkShift.setText(tblPersonel.getValueAt(r, 7).toString());
        txtSalary.setText(tblPersonel.getValueAt(r, 8).toString());

        // Tabloda görünen Detay (Damga / Uzmanlık)
        String detailVal = tblPersonel.getValueAt(r, 5).toString();

        // EKSİK ALANLARI VERİTABANINDAN ÇEKELİM
        String expVal = "";
        String certVal = "";
        String authCode = "";
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                "SELECT " +
                "COALESCE(t.experience_years, os.experience_years, 0), " +
                "COALESCE(t.certification_level, os.qualification_level, ''), " +
                "COALESCE(os.authorization_code, '') " +
                "FROM personnel p " +
                "LEFT JOIN technician t ON p.personnel_id = t.personnel_id " +
                "LEFT JOIN office_staff os ON p.personnel_id = os.personnel_id " +
                "WHERE p.personnel_id = ?")) {
            
            ps.setInt(1, selectedPersonelId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                expVal = String.valueOf(rs.getInt(1));
                certVal = rs.getString(2);
                authCode = rs.getString(3);
            }
        } catch (Exception e) { e.printStackTrace(); }

        // Kutuları doldur
        if ("INSPECTOR".equals(type)) {
            txtDetail.setText(detailVal);
        } else if ("TECHNICIAN".equals(type)) {
            txtDetail.setText(detailVal);
            txtExperience.setText(expVal);
            txtCertification.setText(certVal);
        } else { // OFFICE
            txtOfficeAuthCode.setText(authCode.isEmpty() ? detailVal : authCode); 
            txtOfficeQualLevel.setText(certVal);
            txtExperience.setText(expVal);
        }

        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        txtName.setText(""); txtSurname.setText(""); txtDepartment.setText("");
        txtHireDate.setText(""); txtWorkShift.setText(""); txtSalary.setText("");
        txtDetail.setText(""); txtExperience.setText(""); txtCertification.setText("");
        txtOfficeAuthCode.setText(""); txtOfficeQualLevel.setText("");

        selectedPersonelId = -1;
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
        cmbType.setEnabled(true);
        updateDetailLabel();
    }

    private TitledBorder createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        );
    }
}