package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StationInfoPanel extends JPanel {
    private static StationInfoPanel instance;    

    private JTable tblStations;
    private DefaultTableModel modelStations;
    private JTextField txtName, txtCity, txtDistrict, txtPhone, txtSearch;
    private JTextArea txtAddress; // Adres biraz uzun olabilir
    private JComboBox<Integer> cmbCapacity;
    private JButton btnAdd, btnUpdate, btnDelete, btnClear, btnSearch;
    
    // Seçili İstasyon ID
    private int selectedStationId = -1;

    public static StationInfoPanel getInstance() {
        if (instance == null) {
            instance = new StationInfoPanel();
        }
        return instance;
    }

    private StationInfoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. FORM ALANI (ÜST PANEL)
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBorder(createStyledBorder("İstasyon Yönetimi"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // --- Satır 1 ---
        gbc.gridx = 0; gbc.gridy = 0; pnlForm.add(new JLabel("İstasyon Adı:"), gbc);
        gbc.gridx = 1; txtName = new JTextField(15); pnlForm.add(txtName, gbc);
        
        gbc.gridx = 2; pnlForm.add(new JLabel("Şehir:"), gbc);
        gbc.gridx = 3; txtCity = new JTextField(15); pnlForm.add(txtCity, gbc);
        
        // --- Satır 2 ---
        gbc.gridx = 0; gbc.gridy = 1; pnlForm.add(new JLabel("İlçe:"), gbc);
        gbc.gridx = 1; txtDistrict = new JTextField(15); pnlForm.add(txtDistrict, gbc);
        
        gbc.gridx = 2; pnlForm.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 3; txtPhone = new JTextField(15);
        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });
        pnlForm.add(txtPhone, gbc);
        
        // --- Satır 3 ---
        gbc.gridx = 0; gbc.gridy = 2; pnlForm.add(new JLabel("Günlük Kapasite:"), gbc);
        gbc.gridx = 1; 
        cmbCapacity = new JComboBox<>();
        // 1'den 200'e kadar sayıları ekle
        for (int i = 1; i <= 350; i++) {
            cmbCapacity.addItem(i);
        }
        cmbCapacity.setSelectedItem(50); // Varsayılan 50
        
        pnlForm.add(cmbCapacity, gbc);

        // --- Satır 4 (Adres - Geniş Alan) ---
        gbc.gridx = 0; gbc.gridy = 3; pnlForm.add(new JLabel("Adres:"), gbc);
        gbc.gridx = 1; gbc.gridwidth = 3; // 3 sütunluk yer kaplasın
        txtAddress = new JTextArea(2, 40);
        txtAddress.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        pnlForm.add(txtAddress, gbc);
        
        // Form Butonları
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBtns.setOpaque(false);
        
        btnAdd = new JButton("Ekle"); btnAdd.setBackground(AppConfig.COLOR_SUCCESS); btnAdd.setForeground(Color.WHITE);
        btnUpdate = new JButton("Güncelle"); btnUpdate.setBackground(AppConfig.COLOR_WARNING); btnUpdate.setForeground(Color.WHITE);
        btnDelete = new JButton("Sil"); btnDelete.setBackground(AppConfig.COLOR_DANGER); btnDelete.setForeground(Color.WHITE);
        btnClear = new JButton("Temizle");
        
        // Başlangıç durumu
        btnUpdate.setEnabled(false); btnDelete.setEnabled(false);
        
        pnlBtns.add(btnAdd); pnlBtns.add(btnUpdate); pnlBtns.add(btnDelete); pnlBtns.add(btnClear);
        
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 4;
        pnlForm.add(pnlBtns, gbc);
        
        add(pnlForm, BorderLayout.NORTH);

        // 2. TABLO VE ARAMA (ORTA PANEL)
        JPanel pnlCenter = new JPanel(new BorderLayout(5, 5));
        
        // Arama
        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlSearch.add(new JLabel("🔍 İstasyon Ara:"));
        txtSearch = new JTextField(20);
        btnSearch = new JButton("Bul");
        pnlSearch.add(txtSearch); pnlSearch.add(btnSearch);
        pnlCenter.add(pnlSearch, BorderLayout.NORTH);
        
        // Tablo
        modelStations = new DefaultTableModel(new Object[]{"ID", "İstasyon Adı", "Şehir", "İlçe", "Telefon", "Kapasite", "Adres"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        tblStations = new JTable(modelStations);
        DBHelper.styleTable(tblStations);
        
        // Sütun genişlik ayarları (Adres geniş olsun)
        tblStations.getColumnModel().getColumn(0).setMaxWidth(50); // ID
        tblStations.getColumnModel().getColumn(3).setMaxWidth(80); // İlçe
        tblStations.getColumnModel().getColumn(5).setMaxWidth(80); // Kapasite
        
        pnlCenter.add(new JScrollPane(tblStations), BorderLayout.CENTER);
        add(pnlCenter, BorderLayout.CENTER);

        // 3. EVENTLER
        loadData("");
        
        // Tablo seçimi
        tblStations.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tblStations.getSelectedRow() != -1) {
                fillFormFromSelection();
            }
        });

        // Buton aksiyonları
        btnAdd.addActionListener(e -> addStation());
        btnUpdate.addActionListener(e -> updateStation());
        btnDelete.addActionListener(e -> deleteStation());
        btnClear.addActionListener(e -> clearForm());
        btnSearch.addActionListener(e -> loadData(txtSearch.getText()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText())); // Enter
    }

    // --- METODLAR ---

    private void loadData(String searchText) {
        modelStations.setRowCount(0);
        String sql = DBHelper.getStationsBaseQuery();
        
        if (!searchText.isEmpty()) {
            sql += "AND (station_name ILIKE '%" + searchText + "%' OR city ILIKE '%" + searchText + "%') ";
        }
        sql += "ORDER BY station_name";

        try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                modelStations.addRow(new Object[]{
                    rs.getInt("station_id"),
                    rs.getString("station_name"),
                    rs.getString("city"),
                    rs.getString("district"), // ARTIK VERİTABANINDAN GELİYOR (Eskiden "-" idi)
                    rs.getString("phone_number"),
                    rs.getInt("capacity_per_day"),
                    rs.getString("address")
                });
            }
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veri yüklenirken hata: " + e.getMessage());
        }
    }

    private void addStation() {
        if (txtName.getText().isEmpty() || txtCity.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "İstasyon Adı ve Şehir zorunludur!");
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            
            PreparedStatement ps = conn.prepareStatement(DBHelper.getInsertStationQuery());
            ps.setString(1, txtName.getText());
            ps.setString(2, txtCity.getText());
            ps.setString(3, txtDistrict.getText()); // YENİ EKLENDİ
            ps.setString(4, txtPhone.getText());    // Sıra kaydı (3 -> 4 oldu)
            ps.setInt(5, (Integer) cmbCapacity.getSelectedItem()); // (4 -> 5 oldu)
            ps.setString(6, txtAddress.getText());  // (5 -> 6 oldu)
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "İstasyon başarıyla eklendi.");
            clearForm();
            loadData("");
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Ekleme Hatası: " + e.getMessage());
        }
    }

    private void updateStation() {
        if (selectedStationId == -1) return;

        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(DBHelper.getUpdateStationQuery());
            ps.setString(1, txtName.getText());
            ps.setString(2, txtCity.getText());
            ps.setString(3, txtDistrict.getText()); // YENİ EKLENDİ
            ps.setString(4, txtPhone.getText());    // Sıra kaydı
            ps.setInt(5, (Integer) cmbCapacity.getSelectedItem());
            ps.setString(6, txtAddress.getText());
            ps.setInt(7, selectedStationId);
            
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "İstasyon güncellendi.");
            clearForm();
            loadData("");
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Güncelleme Hatası: " + e.getMessage());
        }
    }

    private void deleteStation() {
        if (selectedStationId == -1) return;
        
        if (JOptionPane.showConfirmDialog(this, "Bu istasyonu silmek istiyor musunuz?\n(Bağlı randevular varsa silinemeyebilir)", "Silme Onayı", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            conn.createStatement().executeUpdate("UPDATE personnel SET station_id = NULL WHERE station_id=" + selectedStationId);
            
            // Şimdi ana istasyonu sil
            conn.createStatement().executeUpdate("DELETE FROM inspection_station WHERE station_id=" + selectedStationId);
            
            JOptionPane.showMessageDialog(this, "İstasyon silindi.");
            clearForm();
            loadData("");
        } catch (Exception e) { 
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Silinemedi! Bu istasyona bağlı randevular olabilir.\n" + e.getMessage());
        }
    }

    private void fillFormFromSelection() {
        int row = tblStations.getSelectedRow();
        selectedStationId = Integer.parseInt(tblStations.getValueAt(row, 0).toString());
        txtName.setText(tblStations.getValueAt(row, 1).toString());
        txtCity.setText(tblStations.getValueAt(row, 2).toString());
        txtDistrict.setText(tblStations.getValueAt(row, 3) != null ? tblStations.getValueAt(row, 3).toString() : "");
        txtPhone.setText(tblStations.getValueAt(row, 4) != null ? tblStations.getValueAt(row, 4).toString() : "");
        int capacity = Integer.parseInt(tblStations.getValueAt(row, 5).toString());
        cmbCapacity.setSelectedItem(capacity);
        txtAddress.setText(tblStations.getValueAt(row, 6) != null ? tblStations.getValueAt(row, 6).toString() : "");
        
        btnAdd.setEnabled(false);
        btnUpdate.setEnabled(true);
        btnDelete.setEnabled(true);
    }

    private void clearForm() {
        txtName.setText(""); txtCity.setText(""); txtDistrict.setText(""); txtPhone.setText(""); txtAddress.setText("");
        cmbCapacity.setSelectedItem(50);
        selectedStationId = -1;
        tblStations.clearSelection();
        
        btnAdd.setEnabled(true);
        btnUpdate.setEnabled(false);
        btnDelete.setEnabled(false);
    }
    
    private TitledBorder createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY), 
                title, 
                TitledBorder.CENTER, 
                TitledBorder.TOP, 
                new Font("Segoe UI", Font.BOLD, 14));
    }
}