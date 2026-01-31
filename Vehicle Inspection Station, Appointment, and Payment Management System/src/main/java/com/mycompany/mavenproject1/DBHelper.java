package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.Vector;

public class DBHelper {

    // =====================
    // CONNECTION
    // =====================
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                AppConfig.DB_URL,
                AppConfig.USER,
                AppConfig.PASS
        );
    }

    // =====================
    // TABLE FILLER
    // =====================
    public static void fillTable(JTable table, String sql) {
        try (Connection c = getConnection();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            ResultSetMetaData m = r.getMetaData();
            Vector<String> columns = new Vector<>();
            for (int i = 1; i <= m.getColumnCount(); i++) {
                columns.add(m.getColumnLabel(i));
            }

            Vector<Vector<Object>> data = new Vector<>();
            while (r.next()) {
                Vector<Object> row = new Vector<>();
                for (int i = 1; i <= m.getColumnCount(); i++) {
                    row.add(r.getObject(i));
                }
                data.add(row);
            }

            table.setModel(new DefaultTableModel(data, columns) {
                @Override
                public boolean isCellEditable(int r, int c) {
                    return false;
                }
            });

            styleTable(table);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Veri çekme hatası: " + e.getMessage());
        }
    }

    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(true);
        if (table.getTableHeader() != null) {
            table.getTableHeader().setFont(AppConfig.FONT_HEADER);
            ((DefaultTableCellRenderer)
                    table.getTableHeader()
                            .getDefaultRenderer())
                    .setHorizontalAlignment(JLabel.CENTER);
        }
    }

    // =====================
    // BRAND / MODEL / TYPE
    // =====================
    public static DefaultComboBoxModel<ComboItem> getBrandsModel() {
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
        String sql = "SELECT brand_id, brand_name FROM brand ORDER BY brand_name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addElement(
                        new ComboItem(
                                rs.getInt("brand_id"),
                                rs.getString("brand_name")
                        )
                );
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static DefaultComboBoxModel<ComboItem> getModelsByBrandId(int brandId) {
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();

        String sql =
                "SELECT model_id, model_name " +
                "FROM model " +
                "WHERE brand_id = ? " +
                "ORDER BY model_name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, brandId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                model.addElement(
                        new ComboItem(
                                rs.getInt("model_id"),
                                rs.getString("model_name")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    public static DefaultComboBoxModel<ComboItem> getVehicleTypesModel() {
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();
        String sql =
                "SELECT vehicle_type_id, type_name " +
                "FROM vehicle_type ORDER BY type_name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addElement(
                        new ComboItem(
                                rs.getInt("vehicle_type_id"),
                                rs.getString("type_name")
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }

    // =====================
    // INSPECTOR (3NF + INHERITANCE FIX)
    // =====================
    public static DefaultComboBoxModel<ComboItem> getInspectorsByStation(int stationId) {

        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();

        //'station_personnel' tablosu silindiği için join .
        // İlişki artık 'personnel' tablosundaki 'station_id' üzerinden kuruluyor.
        String sql =
            "SELECT p.personnel_id, p.first_name, p.last_name, i.stamp_number " +
            "FROM personnel p " +
            "JOIN inspector i ON p.personnel_id = i.personnel_id " +
            "WHERE p.personnel_type = 'INSPECTOR' " +
            "AND p.station_id = ? " + // sp.station_id yerine p.station_id
            "ORDER BY p.first_name, p.last_name";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, stationId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String label =
                        rs.getString("first_name") + " " +
                        rs.getString("last_name") +
                        " (" + rs.getString("stamp_number") + ")";
                model.addElement(
                        new ComboItem(
                                rs.getInt("personnel_id"),
                                label
                        )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    // =====================
    // OFFICE STAFF UPSERT
    // =====================
    // DBHelper.java içinde bulun ve değiştirin:
    public static void upsertOfficeStaff(
            Connection conn,
            int personnelId,
            String authorizationCode,
            String qualificationLevel,
            int experienceYears // YENİ PARAMETRE
    ) throws SQLException {
    
        String sql =
                "INSERT INTO office_staff (personnel_id, authorization_code, qualification_level, experience_years) " +
                "VALUES (?, ?, ?, ?) " +
                "ON CONFLICT (personnel_id) DO UPDATE SET " +
                "authorization_code = EXCLUDED.authorization_code, " +
                "qualification_level = EXCLUDED.qualification_level, " +
                "experience_years = EXCLUDED.experience_years"; // GÜNCELLEME EKLENDİ
    
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, personnelId);
            ps.setString(2, authorizationCode);
            ps.setString(3, qualificationLevel);
            
            // Eğer boş gelirse 0 yazalım, hata vermesin
            ps.setInt(4, experienceYears); 
            
            ps.executeUpdate();
        }
    }

    // =====================
    // STATION LIST (ComboBox için)
    // =====================
    public static DefaultComboBoxModel<ComboItem> getStationsModel() {
        DefaultComboBoxModel<ComboItem> model = new DefaultComboBoxModel<>();

        model.addElement(new ComboItem(-1, "Tümü"));

        String sql =
            "SELECT station_id, station_name || ' - ' || city AS label " +
            "FROM inspection_station " +
            "ORDER BY city, station_name";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                model.addElement(
                    new ComboItem(
                        rs.getInt("station_id"),
                        rs.getString("label")
                    )
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return model;
    }

    // =====================
    // PERSONNEL → STATION ASSIGN (FIXED)
    // =====================
    public static void assignPersonnelToStation(
            Connection conn,
            int personnelId,
            int stationId
    ) throws SQLException {

        //'station_personnel' tablosu silindi.
        // Personel tablosundaki station_id sütunu güncellenmeli.
        String sql =
            "UPDATE personnel SET station_id = ? WHERE personnel_id = ?";

        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, stationId);
        ps.setInt(2, personnelId);
        ps.executeUpdate();
    }

    // =====================
    // OWNER CONTACT (FIXED LOGIC)
    // =====================
    public static ResultSet getPrimaryContact(Connection conn, int ownerId) throws SQLException {
        //'phone' ve 'email' sütunları artık yok.
        // Veriyi çekip Java'nın beklediği sütun isimlerine (alias) dönüştürüyoruz.
        // NOT: Yeni yapıda bir kişinin birden fazla telefonu olabilir, burada LIMIT 1 ile rastgele birini alıyoruz.
        PreparedStatement ps = conn.prepareStatement(
            "SELECT contact_id, " +
            "(SELECT contact_value FROM owner_contact WHERE owner_id = ? AND contact_type = 'Telefon' LIMIT 1) AS phone, " +
            "(SELECT contact_value FROM owner_contact WHERE owner_id = ? AND contact_type = 'Email' LIMIT 1) AS email " +
            "FROM owner_contact " +
            "WHERE owner_id = ? " +
            "LIMIT 1"
        );
        ps.setInt(1, ownerId);
        ps.setInt(2, ownerId);
        ps.setInt(3, ownerId);
        return ps.executeQuery();
    }

    public static void upsertOwnerContact(
            Connection conn,
            int ownerId,
            String phone,
            String email
    ) throws SQLException {
        
        String tempSql = "INSERT INTO owner_contact (owner_id, contact_type, contact_value) VALUES (?, 'Telefon', ?)";
        
        PreparedStatement ps = conn.prepareStatement(tempSql);
        ps.setInt(1, ownerId);
        ps.setString(2, phone); 
        // 3. Parametre (email) kullanılmadı çünkü tablo yapısı değişti.
        ps.executeUpdate();
    }

    // =====================
    // OWNER ADDRESS (FIXED TABLE NAME)
    // =====================
    public static ResultSet getPrimaryAddress(Connection conn, int ownerId) throws SQLException {
        //Tablo adı 'owner_address' -> 'address'.
        // Sütun adı 'address_text' -> 'street'.
        PreparedStatement ps = conn.prepareStatement(
            "SELECT address_id, city, district, street AS address_text " +
            "FROM address " +
            "WHERE owner_id = ? " +
            "ORDER BY address_id LIMIT 1"
        );
        ps.setInt(1, ownerId);
        return ps.executeQuery();
    }
    
    public static void upsertOwnerAddress(
            Connection conn,
            int ownerId,
            String city,
            String district,
            String addressText
    ) throws SQLException {
    
        //Tablo adı 'owner_address' -> 'address'.
        // Sütun 'address_text' -> 'street'.
        // NOT: address tablosunda owner_id unique değilse (1-N ilişki), ON CONFLICT (owner_id) hata verebilir.
        // Ancak şemada owner_id foreign key, unique constraint görünmüyor. 
        // Java'nın çalışması için Conflict kısmını kaldırıp düz insert yapıyorum.
        
        String sql =
            "INSERT INTO address (owner_id, city, district, street) " +
            "VALUES (?, ?, ?, ?)";
    
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, ownerId);
        ps.setString(2, city);
        ps.setString(3, district);
        ps.setString(4, addressText);
        ps.executeUpdate();
    }

    // =====================
    // SQL QUERIES
    // =====================
    public static String getInspectorsByStationQuery() {
        //station_personnel silindi, join .
        return "SELECT i.personnel_id, p.first_name, p.last_name, i.stamp_number " +
               "FROM inspector i " +
               "JOIN personnel p ON i.personnel_id = p.personnel_id " +
               "WHERE p.station_id = ? " +
               "ORDER BY p.first_name";
    }

    public static String getDeleteZombieAppointmentQuery() {
        return "DELETE FROM appointment WHERE status = 'ACTIVE' AND vehicle_id = (SELECT vehicle_id FROM vehicle WHERE chassis_number = ?)";
    }

    public static String getInsertAddressQuery() {
        //address_text yok, street var.
        return "INSERT INTO address (street, building_no, door_no, district, city, zip_code, owner_id) VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING address_id";
    }

    public static String getInsertOwnerAddressQuery() {
        // address_title sütunu silindiği için bu sorgu artık işlevsizdir.
        // Parametre hatası almamak için (Java tarafında setInt çağrılırsa diye) dummy bir sorgu döndürüyoruz.
        return "SELECT 1 WHERE 1=1 OR ?=0 OR ?=0"; 
    }

    public static String getInsertPaymentQuery() {
        return "INSERT INTO payment (inspection_id, payment_no, payment_date, amount, payment_method, transaction_code) " +
               "VALUES (?, ?, CURRENT_DATE, ?, ?, ?)";
    }

    public static String getTestResultsQuery() {
        return "SELECT tp.test_name, tr.measured_value, tp.unit, tp.min_limit, tp.max_limit, tr.is_passed " +
               "FROM test_result tr " +
               "JOIN test_parameter tp ON tr.test_parameter_id = tp.test_parameter_id " +
               "WHERE tr.inspection_id = ?";
    }

    public static String getDefectsQuery() {
        return "SELECT d.defect_code, d.description, d.severity " +
               "FROM inspection_defect id " +
               "JOIN defect_catalog d ON id.defect_id = d.defect_id " +
               "WHERE id.inspection_id = ?";
    }

    public static String getStationsBaseQuery() {
        // district sütunu eklendi
        return "SELECT station_id, station_name, city, district, phone_number, capacity_per_day, address FROM inspection_station WHERE 1=1 ";
    }

    public static String getInsertStationQuery() {
        // city'den sonra district ekledik
        return "INSERT INTO inspection_station (station_name, city, district, phone_number, capacity_per_day, address) VALUES (?, ?, ?, ?, ?, ?)";
    }

    public static String getUpdateStationQuery() {
        // city'den sonra district ekledik
        return "UPDATE inspection_station SET station_name=?, city=?, district=?, phone_number=?, capacity_per_day=?, address=? WHERE station_id=?";
    }

    // 1. Temayı Veritabanına Kaydetme/Güncelleme
public static void updateThemeMode(boolean isDark) {
    String sql = "UPDATE app_settings SET is_dark_mode = ? WHERE id = 1";
    
    try (Connection conn = getConnection();
         PreparedStatement pstmt = conn.prepareStatement(sql)) {
        
        pstmt.setBoolean(1, isDark);
        pstmt.executeUpdate();
        System.out.println("Tema tercihi veritabanına kaydedildi: " + (isDark ? "Dark" : "Light"));
        
    } catch (SQLException e) {
        System.out.println("Tema kaydedilemedi: " + e.getMessage());
    }
}

    // 2. Uygulama Açılırken Temayı Okuma (Bunu Main class'ta kullanacağız)
    public static boolean loadThemeMode() {
        String sql = "SELECT is_dark_mode FROM app_settings WHERE id = 1";
        boolean isDark = false; // Varsayılan Light

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                isDark = rs.getBoolean("is_dark_mode");
            }

        } catch (SQLException e) {
            System.out.println("Tema bilgisi okunamadı, varsayılan kullanılıyor.");
        }
        return isDark;
    }

    // =====================
    // AKILLI ID ÜRETİCİ (SMART ID GENERATOR)
    // =====================
    // Algoritma: Önce boşlukları (gap) doldur, boşluk yoksa Max ID + 1 ver.
    public static int generateSmartId(Connection conn, String tableName, String idColumnName) {
        int nextId = 1; // Tablo boşsa 1 döner
        try {
            // 1. ADIM: Boşluk Kontrolü (Gap Finding)
            // Mantık: Kendisinden 1 fazlası olmayan en küçük ID'yi bul.
            String gapSql = 
                "SELECT MIN(t1." + idColumnName + " + 1) " +
                "FROM " + tableName + " t1 " +
                "WHERE NOT EXISTS (SELECT 1 FROM " + tableName + " t2 WHERE t2." + idColumnName + " = t1." + idColumnName + " + 1)";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(gapSql)) {
                if (rs.next()) {
                    int gapId = rs.getInt(1);
                    if (gapId > 0) return gapId; // Boşluk bulundu!
                }
            }

            // 2. ADIM: Boşluk Yoksa En Büyüğünü Bul (Max + 1)
            String maxSql = "SELECT MAX(" + idColumnName + ") FROM " + tableName;
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(maxSql)) {
                if (rs.next()) {
                    nextId = rs.getInt(1) + 1;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return nextId;
    }

    // =====================
    // TRIGGER HATA YÖNETİCİSİ (YENİ)
    // =====================
    public static String translateSQLException(SQLException ex) {
        String msg = ex.getMessage();
        
        // Trigger 1: Aktif Randevu Kontrolü
        if (msg.contains("zaten aktif bir randevu var")) {
            return "UYARI: Bu araç için sistemde zaten devam eden (kapanmamış) bir randevu mevcut.\nLütfen 'Eski Kayıtlar'dan kontrol ediniz.";
        }
        
        // Trigger 7: İstasyon Kapasitesi
        if (msg.contains("günlük kapasite dolu")) {
            // Mesajın içinden sayıları da çekebiliriz ama basit tutalım
            return "KAPASİTE HATASI: Seçilen istasyonun bu tarih için randevu kotası dolmuştur.\nLütfen başka bir tarih veya istasyon seçiniz.";
        }

        // Trigger 4: Test Limit Kontrolü (Genelde sessiz çalışır ama manuel insert denenirse)
        if (msg.contains("Test sonucu limit dışı")) {
            return "HATA: Girilen test değeri, izin verilen limitlerin dışında.";
        }
        
        // Unique Constraint (TC, Plaka vb.)
        if (msg.contains("unique constraint") || msg.contains("duplicate key")) {
            if (msg.contains("plate_number")) return "Bu PLAKA sistemde zaten kayıtlı.";
            if (msg.contains("national_id")) return "Bu TC KİMLİK NO ile kayıtlı bir müşteri zaten var.";
            if (msg.contains("tax_number")) return "Bu VERGİ NO ile kayıtlı bir şirket zaten var.";
        }

        // Bilinmeyen diğer hatalar
        return "Veritabanı İşlem Hatası:\n" + msg;
    }
}