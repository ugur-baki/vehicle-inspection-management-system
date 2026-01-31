package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class PinnedVehicles extends JPanel {
    
    private static PinnedVehicles instance;
    ThemeDecorator theme = ThemeDecorator.getInstance();

    // UI Bileşenleri
    private JTable tblVehicles;
    private DefaultTableModel modelVehicles;
    private JTextField txtSearch;
    private JButton btnSearch, btnRefresh;

    // Singleton Erişim
    public static PinnedVehicles getInstance() {
        if (instance == null) {
            instance = new PinnedVehicles();
        }
        return instance;
    }

    private PinnedVehicles() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. ÜST PANEL (ARAMA ÇUBUĞU)
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTop.setBorder(createStyledBorder("Araç Sorgulama"));

        pnlTop.add(new JLabel("🔍 Ara (Plaka / Şasi / Müşteri):"));
        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(200, 30));
        pnlTop.add(txtSearch);

        btnSearch = new JButton("Bul");
        pnlTop.add(btnSearch);

        btnRefresh = new JButton("Yenile");
        pnlTop.add(btnRefresh);

        add(pnlTop, BorderLayout.NORTH);

        // 2. ORTA PANEL (TABLO)
        // Sütunlar: ID, Plaka, Marka/Model, Yıl, Şasi No, Ruhsat Sahibi
        String[] columns = {"Araç ID", "Plaka", "Marka - Model", "Yıl", "Şasi No", "Ruhsat Sahibi"};
        
        modelVehicles = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false; // Tablo üzerinde düzenleme yapılamasın (Sadece izleme)
            }
        };

        tblVehicles = new JTable(modelVehicles);
        DBHelper.styleTable(tblVehicles); // Mevcut stil sınıfını kullanıyoruz
        
        // Sütun genişlik ayarları
        tblVehicles.getColumnModel().getColumn(0).setMaxWidth(60); // ID
        tblVehicles.getColumnModel().getColumn(1).setPreferredWidth(100); // Plaka
        tblVehicles.getColumnModel().getColumn(2).setPreferredWidth(200); // Marka Model
        
        add(new JScrollPane(tblVehicles), BorderLayout.CENTER);

        // 3. EVENTLER (OLAYLAR)
        btnSearch.addActionListener(e -> loadData(txtSearch.getText().trim()));
        txtSearch.addActionListener(e -> loadData(txtSearch.getText().trim())); // Enter tuşu
        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            loadData("");
        });
    }

    // --- VERİTABANINDAN VERİ ÇEKME ---
    private void loadData(String searchText) {
        modelVehicles.setRowCount(0); // Tabloyu temizle

        StringBuilder sql = new StringBuilder();
        
        //  vehicle_owner tablosu artık YOK.
        // Araç sahibi bilgisi license_registration tablosundaki owner_id'den gelir.
        
        sql.append("SELECT v.vehicle_id, l.plate_number, ");
        sql.append("b.brand_name || ' - ' || m.model_name AS vehicle_full_name, ");
        sql.append("v.production_year, v.chassis_number, ");
        
        // Sahip Adı: Bireysel ise Ad Soyad, Kurumsal ise Şirket Adı gelsin
        sql.append("CASE WHEN o.owner_type = 'INDIVIDUAL' THEN (i.first_name || ' ' || i.last_name) ");
        sql.append("     ELSE c.company_name END AS owner_name ");
        
        sql.append("FROM vehicle v ");
        sql.append("JOIN license_registration l ON v.vehicle_id = l.vehicle_id ");
        sql.append("JOIN model m ON v.model_id = m.model_id ");
        sql.append("JOIN brand b ON m.brand_id = b.brand_id ");
        
        // DEĞİŞİKLİK BURADA: vehicle_owner yerine license_registration üzerinden owner'a gidiyoruz
        sql.append("JOIN owner o ON l.owner_id = o.owner_id ");
        
        sql.append("LEFT JOIN individual_owner i ON o.owner_id = i.owner_id ");
        sql.append("LEFT JOIN company_owner c ON o.owner_id = c.owner_id ");
        sql.append("WHERE l.is_active = TRUE "); // Sadece aktif ruhsatları getir

        // Arama Filtresi
        if (!searchText.isEmpty()) {
            sql.append("AND (");
            sql.append("l.plate_number ILIKE '%").append(searchText).append("%' OR ");
            sql.append("v.chassis_number ILIKE '%").append(searchText).append("%' OR ");
            sql.append("i.first_name ILIKE '%").append(searchText).append("%' OR ");
            sql.append("i.last_name ILIKE '%").append(searchText).append("%' OR ");
            sql.append("c.company_name ILIKE '%").append(searchText).append("%'");
            sql.append(") ");
        }

        sql.append("ORDER BY v.vehicle_id DESC"); // En son eklenen en üstte

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                modelVehicles.addRow(new Object[]{
                    rs.getInt("vehicle_id"),
                    rs.getString("plate_number"),
                    rs.getString("vehicle_full_name"),
                    rs.getInt("production_year"),
                    rs.getString("chassis_number"),
                    rs.getString("owner_name")
                });
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veri çekme hatası: " + e.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
        }
    }

    private TitledBorder createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(title);
    }
    
}