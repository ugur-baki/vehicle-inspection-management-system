package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector; 

public class DetailedRegistrationPanel extends JPanel {
    private static DetailedRegistrationPanel instance;
    
    // Ortak Araç Alanları
    JTextField txtPlate, txtChassis, txtEngine, txtYear, txtColor;
    
    // Bireysel Alanlar
    JLabel lblTCWarning;
    JTextField txtName, txtSurname, txtTC;
    
    // Kurumsal Alanlar
    JTextField txtTaxNo, txtCompanyName, txtTaxOffice, txtContactPerson;
    
    // Ortak İletişim (Owner_Contact tablosu için)
    JTextField txtPhone, txtEmail;
    
    // Adres Alanları (Address tablosu için)
    JTextField txtCity, txtDistrict, txtStreet, txtBuildingNo, txtDoorNo, txtZipCode;
    
    // Araç ComboBox Yapıları
    JComboBox<ComboItem> cmbBrand; 
    JComboBox<ComboItem> cmbModel;
    JComboBox<ComboItem> cmbType; 
    
    // Randevu/Muayene Seçim ComboBox'ları
    JComboBox<ComboItem> cmbStation;
    JComboBox<ComboItem> cmbInspector;
    
    // Müşteri Tipi Seçimi
    JRadioButton rdoIndividual, rdoCompany;
    JPanel pnlOwnerFields; 

    public static DetailedRegistrationPanel getInstance() {
        if (instance == null) {
            instance = new DetailedRegistrationPanel();
        }
        return instance;
    }

    private DetailedRegistrationPanel() {
        setLayout(new BorderLayout());
        
        // Ana Grid Panel
        JPanel pnlMain = new JPanel(new GridBagLayout());
        pnlMain.setBorder(new EmptyBorder(20, 40, 20, 40));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.NORTH;

        // ============================================================
        // 1. SOL TARAF: ARAÇ BİLGİLERİ
        // ============================================================
        JPanel pnlVehicle = new JPanel(new GridLayout(0, 2, 5, 10));
        pnlVehicle.setBorder(new TitledBorder(new LineBorder(AppConfig.COLOR_BTN_ACTION), " ARAÇ BİLGİLERİ ", TitledBorder.CENTER, TitledBorder.TOP, AppConfig.FONT_HEADER));
        
        // Marka (DB'den)
        pnlVehicle.add(new JLabel("Marka:")); 
        cmbBrand = new JComboBox<>(DBHelper.getBrandsModel()); 
        pnlVehicle.add(cmbBrand);

        // Model (DB'den)
        pnlVehicle.add(new JLabel("Model:")); 
        cmbModel = new JComboBox<>(); 
        pnlVehicle.add(cmbModel);

        // Marka seçildiğinde Modelleri getir
        cmbBrand.addActionListener(e -> {
            ComboItem selectedBrand = (ComboItem) cmbBrand.getSelectedItem();
            if (selectedBrand != null) {
                DefaultComboBoxModel<ComboItem> newModel = DBHelper.getModelsByBrandId(selectedBrand.getId());
                cmbModel.setModel(newModel);
                if (cmbModel.getItemCount() > 0) cmbModel.setSelectedIndex(0);
            }
        });
        
        if (cmbBrand.getItemCount() > 0) cmbBrand.setSelectedIndex(0);

        // Kasa Tipi (DB'den)
        pnlVehicle.add(new JLabel("Kasa Tipi:"));
        cmbType = new JComboBox<>(DBHelper.getVehicleTypesModel());
        pnlVehicle.add(cmbType);

        pnlVehicle.add(new JLabel("Plaka:")); txtPlate = new JTextField(); pnlVehicle.add(txtPlate);
        pnlVehicle.add(new JLabel("Şasi No:")); txtChassis = new JTextField(); pnlVehicle.add(txtChassis);
        pnlVehicle.add(new JLabel("Motor No:")); txtEngine = new JTextField(); pnlVehicle.add(txtEngine);
        pnlVehicle.add(new JLabel("Renk:")); txtColor = new JTextField(); pnlVehicle.add(txtColor);
        pnlVehicle.add(new JLabel("Yıl:")); txtYear = new JTextField(); pnlVehicle.add(txtYear);
        
        txtYear.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });


        // ============================================================
        // 2. SAĞ TARAF: MÜŞTERİ ve ADRES BİLGİLERİ
        // ============================================================
        JPanel pnlRightSide = new JPanel();
        pnlRightSide.setLayout(new BoxLayout(pnlRightSide, BoxLayout.Y_AXIS));

        // 2A. MÜŞTERİ BİLGİLERİ
        JPanel pnlOwner = new JPanel(new BorderLayout(5, 5));
        pnlOwner.setBorder(new TitledBorder(new LineBorder(new Color(155, 89, 182)), " RUHSAT SAHİBİ & İLETİŞİM ", TitledBorder.CENTER, TitledBorder.TOP, AppConfig.FONT_HEADER));
        
        // Tip Seçimi (Radio Buttonlar)
        JPanel pnlType = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup grpType = new ButtonGroup();
        rdoIndividual = new JRadioButton("Bireysel Müşteri", true);
        rdoCompany = new JRadioButton("Kurumsal (Şirket)");
        grpType.add(rdoIndividual); grpType.add(rdoCompany);
        pnlType.add(rdoIndividual); pnlType.add(rdoCompany);
        pnlOwner.add(pnlType, BorderLayout.NORTH);

        // Değişken Alanlar Paneli
        pnlOwnerFields = new JPanel(new GridLayout(0, 2, 5, 10));
        
        // Alanları Tanımla
        txtTC = new JTextField(); txtName = new JTextField(); txtSurname = new JTextField();
        txtTaxNo = new JTextField(); txtCompanyName = new JTextField(); txtTaxOffice = new JTextField(); txtContactPerson = new JTextField();
        txtPhone = new JTextField(); txtEmail = new JTextField();

        txtPhone.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });

        txtTC = new JTextField();
        lblTCWarning = new JLabel("*");
        lblTCWarning.setForeground(Color.RED);
        lblTCWarning.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTCWarning.setVisible(false); // Başlangıçta gizli

        // 1. KISITLAMA: Sadece Rakam ve Maksimum 11 Hane
        txtTC.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                // Rakam değilse VEYA uzunluk 11 olmuşsa engelle
                if (!Character.isDigit(c) || txtTC.getText().length() >= 12) {
                    evt.consume(); 
                }
            }
        });

        // 2. DOĞRULAMA: Yazı değiştikçe Algoritmayı Kontrol Et
        txtTC.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { validateTC(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { validateTC(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { validateTC(); }
        });

        

        // Varsayılan Bireysel
        updateOwnerFields(true);

        // Seçim Değişince Alanları Güncelle
        rdoIndividual.addActionListener(e -> updateOwnerFields(true));
        rdoCompany.addActionListener(e -> updateOwnerFields(false));

        pnlOwner.add(pnlOwnerFields, BorderLayout.CENTER);

        // 2B. ADRES BİLGİLERİ (YENİ EKLENDİ)
        JPanel pnlAddress = new JPanel(new GridLayout(0, 4, 5, 10)); // 4 Sütunlu ızgara
        pnlAddress.setBorder(new TitledBorder(new LineBorder(new Color(230, 126, 34)), " ADRES BİLGİLERİ ", TitledBorder.CENTER, TitledBorder.TOP, AppConfig.FONT_HEADER));

        // Adres Alanları
        txtCity = new JTextField(); 
        txtDistrict = new JTextField();
        txtStreet = new JTextField();
        txtBuildingNo = new JTextField();
        txtDoorNo = new JTextField();
        txtZipCode = new JTextField();

        pnlAddress.add(new JLabel("İl:")); pnlAddress.add(txtCity);
        pnlAddress.add(new JLabel("İlçe:")); pnlAddress.add(txtDistrict);
        pnlAddress.add(new JLabel("Sokak/Cadde:")); pnlAddress.add(txtStreet);
        pnlAddress.add(new JLabel("Posta Kodu:")); pnlAddress.add(txtZipCode);
        pnlAddress.add(new JLabel("Bina No:")); pnlAddress.add(txtBuildingNo);
        pnlAddress.add(new JLabel("Kapı No:")); pnlAddress.add(txtDoorNo);

        txtZipCode.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });

        txtBuildingNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });

        txtDoorNo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                char c = evt.getKeyChar();
                if (!Character.isDigit(c)) {
                    evt.consume();  // Sayı değilse yazma işlemini iptal et
                }
            }
        });

        // Sağ taraftaki panelleri birleştir
        pnlRightSide.add(pnlOwner);
        pnlRightSide.add(Box.createVerticalStrut(10)); // Boşluk
        pnlRightSide.add(pnlAddress);

        // ============================================================
        // 3. ALT TARAF: RANDEVU VE İSTASYON SEÇİMİ
        // ============================================================
        JPanel pnlAppointment = new JPanel(new GridLayout(0, 4, 10, 10)); // 4 Sütunlu yapı
        pnlAppointment.setBorder(new TitledBorder(new LineBorder(new Color(46, 204, 113)), " RANDEVU DETAYLARI ", TitledBorder.CENTER, TitledBorder.TOP, AppConfig.FONT_HEADER));

        pnlAppointment.add(new JLabel("İstasyon Seçiniz:", SwingConstants.RIGHT));
        cmbStation = new JComboBox<>();
        pnlAppointment.add(cmbStation);

        pnlAppointment.add(new JLabel("Muayene Uzmanı:", SwingConstants.RIGHT));
        cmbInspector = new JComboBox<>();
        pnlAppointment.add(cmbInspector);
        
        // İstasyonları Yükle
        loadStations();
        
        // İSTASYON SEÇİLİNCE FİLTRELEME YAP
        cmbStation.addActionListener(e -> {
            ComboItem selectedStation = (ComboItem) cmbStation.getSelectedItem();
            if (selectedStation != null) {
                loadInspectors(selectedStation.getId());
            } else {
                cmbInspector.removeAllItems();
            }
        });
        
        if (cmbStation.getItemCount() > 0) {
            cmbStation.setSelectedIndex(0);
            loadInspectors(((ComboItem) cmbStation.getSelectedItem()).getId());
        }

        // ============================================================
        // 4. LAYOUT YERLEŞİMİ
        // ============================================================
        
        // Sol Panel (Araç)
        gbc.gridx = 0; gbc.gridy = 0; 
        gbc.weightx = 0.4; 
        pnlMain.add(pnlVehicle, gbc);

        // Sağ Panel (Müşteri + Adres)
        gbc.gridx = 1; 
        gbc.weightx = 0.6;
        pnlMain.add(pnlRightSide, gbc);

        // Alt Panel (Randevu) - İki sütuna yayılacak
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.gridwidth = 2; 
        pnlMain.add(pnlAppointment, gbc);

        // Kaydet Butonu
        JButton btnSave = new JButton("KAYDET VE RANDEVU OLUŞTUR");
        btnSave.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnSave.setPreferredSize(new Dimension(300, 50));
        btnSave.addActionListener(e -> saveFullData());

        add(new JScrollPane(pnlMain), BorderLayout.CENTER);
        JPanel pnlBtn = new JPanel(); pnlBtn.add(btnSave);
        add(pnlBtn, BorderLayout.SOUTH);
    }

   // --- DİNAMİK ALAN GÜNCELLEME ---
    private void updateOwnerFields(boolean isIndividual) {
        pnlOwnerFields.removeAll(); 
        
        if (isIndividual) {
            // TC Kutusunu ve Uyarı Yıldızını Yanyana Koymak İçin Küçük Panel
            JPanel pnlTCWrapper = new JPanel(new BorderLayout());
            pnlTCWrapper.setOpaque(false); // Arka plan rengini bozmasın
            pnlTCWrapper.add(txtTC, BorderLayout.CENTER);
            pnlTCWrapper.add(lblTCWarning, BorderLayout.EAST); // Sağa yaslı yıldız

            pnlOwnerFields.add(new JLabel("TC Kimlik:")); 
            pnlOwnerFields.add(pnlTCWrapper); // Textfield yerine Wrapper paneli ekliyoruz
            
            pnlOwnerFields.add(new JLabel("Ad:")); pnlOwnerFields.add(txtName);
            pnlOwnerFields.add(new JLabel("Soyad:")); pnlOwnerFields.add(txtSurname);
        } else {
            pnlOwnerFields.add(new JLabel("Vergi No:")); pnlOwnerFields.add(txtTaxNo);
            pnlOwnerFields.add(new JLabel("Şirket Adı:")); pnlOwnerFields.add(txtCompanyName);
            pnlOwnerFields.add(new JLabel("Vergi Dairesi:")); pnlOwnerFields.add(txtTaxOffice);
            pnlOwnerFields.add(new JLabel("İlgili Kişi:")); pnlOwnerFields.add(txtContactPerson);
        }
        
        pnlOwnerFields.add(new JLabel("Telefon:")); pnlOwnerFields.add(txtPhone);
        pnlOwnerFields.add(new JLabel("E-Posta:")); pnlOwnerFields.add(txtEmail);
        
        ThemeDecorator.getInstance().applyTheme(pnlOwnerFields); 
        
        pnlOwnerFields.revalidate();
        pnlOwnerFields.repaint();
    }

    // --- VERİTABANI YARDIMCI METODLARI ---
    
    private void loadStations() {
        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT station_id, station_name FROM inspection_station ORDER BY station_name")) {
            
            Vector<ComboItem> items = new Vector<>();
            while (rs.next()) {
                items.add(new ComboItem(rs.getInt("station_id"), rs.getString("station_name")));
            }
            cmbStation.setModel(new DefaultComboBoxModel<>(items));
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadInspectors(int stationId) {
        cmbInspector.removeAllItems(); 
        
        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(DBHelper.getInspectorsByStationQuery())) {
            
            ps.setInt(1, stationId);
            ResultSet rs = ps.executeQuery();
            Vector<ComboItem> items = new Vector<>();
            while (rs.next()) {
                String fullName = rs.getString("first_name") + " " + rs.getString("last_name") + " (" + rs.getString("stamp_number") + ")";
                items.add(new ComboItem(rs.getInt("personnel_id"), fullName));
            }
            cmbInspector.setModel(new DefaultComboBoxModel<>(items));
            
        } catch (Exception e) { 
            System.err.println("Personel listesi yüklenemedi: " + e.getMessage());
        }
    }

    // --- KAYDETME İŞLEMİ ---
    private void saveFullData() {
        // 1. Validasyonlar
        ComboItem selectedModel = (ComboItem) cmbModel.getSelectedItem();
        ComboItem selectedType = (ComboItem) cmbType.getSelectedItem();
        ComboItem selectedStation = (ComboItem) cmbStation.getSelectedItem();
        ComboItem selectedInspector = (ComboItem) cmbInspector.getSelectedItem();

        if (selectedModel == null || selectedType == null) {
            JOptionPane.showMessageDialog(this, "Lütfen araç modelini ve kasa tipini seçiniz!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (selectedStation == null || selectedInspector == null) {
            JOptionPane.showMessageDialog(this, "Lütfen İstasyon ve Muayene Uzmanı seçiniz!", "Eksik Bilgi", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            conn.setAutoCommit(false); // Transaction Başlat
            try {
                int ownerId;

                // A: MÜŞTERİ OLUŞTURMA / BULMA
                if (rdoIndividual.isSelected()) {
                    ownerId = getOrCreateOwnerId(conn, txtTC.getText(), txtName.getText(), txtSurname.getText());
                } else {
                    ownerId = getOrCreateCompanyId(conn, txtTaxNo.getText(), txtCompanyName.getText(), txtTaxOffice.getText(), txtContactPerson.getText());
                }

                // --- B: İLETİŞİM BİLGİLERİ (Owner_Contact) ---
                saveContactInfo(conn, ownerId, "Telefon", txtPhone.getText());
                saveContactInfo(conn, ownerId, "Email", txtEmail.getText());
                
                // --- C: ADRES BİLGİLERİ (GÜNCELLENDİ) ---
                if (!txtCity.getText().isEmpty()) {
                    // createAddress artık ownerId parametresi de alıyor
                    createAddress(conn, ownerId, txtStreet.getText(), txtBuildingNo.getText(), txtDoorNo.getText(), 
                                  txtDistrict.getText(), txtCity.getText(), txtZipCode.getText());
                }

                // D: ARAÇ OLUŞTURMA / BULMA
                int modelId = selectedModel.getId();
                int vId = getOrCreateVehicleId(conn, modelId, txtChassis.getText(), txtEngine.getText(), txtColor.getText(), txtYear.getText());

                // E: ARAÇ-SAHİP İLİŞKİSİ
                // DÜZELTME: vehicle_owner tablosu artık YOK. İlişki ruhsat üzerinden kurulur.
                if (!checkVehicleOwnerExists(conn, vId, ownerId)) {
                    conn.createStatement().executeUpdate("DO $$ BEGIN END $$;"); 
                }
                
                // F: RUHSAT (Bu metod güncellendi, ownerId alıyor)
                upsertLicense(conn, vId, txtPlate.getText(), ownerId);

                // G: RANDEVU VE MUAYENE
                
                // 1. Appointment Ekle
                // =========================================================================================
                // HATA BURADAYDI: 'Active' yerine 'ACTIVE' yazılması gerekiyor.
                // =========================================================================================
                PreparedStatement psApp = conn.prepareStatement(
                    "INSERT INTO appointment(vehicle_id, station_id, appointment_date, status) VALUES(?, ?, NOW(), 'ACTIVE') RETURNING appointment_id");
                
                psApp.setInt(1, vId);
                psApp.setInt(2, selectedStation.getId()); 
                ResultSet rsApp = psApp.executeQuery();
                rsApp.next();
                int appointmentId = rsApp.getInt(1);

                // 2. Inspection Ekle
                PreparedStatement psInsp = conn.prepareStatement(
                "INSERT INTO inspection(appointment_id, inspector_id, start_time, result_status) VALUES(?, ?, NOW(), 'DEVAM EDİYOR')");
                psInsp.setInt(1, appointmentId);
                psInsp.setInt(2, selectedInspector.getId()); 
                psInsp.executeUpdate();

                conn.commit(); 
                JOptionPane.showMessageDialog(this, "Kayıt Başarılı! \nAraç, Müşteri, Adres ve Randevu kayıtları oluşturuldu.");
                
            } catch (SQLException ex) { 
                conn.rollback(); 
                
                String userMessage = DBHelper.translateSQLException(ex);
                
                // Eğer "zaten aktif randevu" hatasıysa, Zombie Fix önerisi sunabiliriz
                if (ex.getMessage().contains("zaten aktif bir randevu var")) {
                     int choice = JOptionPane.showConfirmDialog(this, 
                        userMessage + "\n\nAskıda kalmış bu randevuyu temizleyip tekrar denemek ister misiniz?", 
                        "Çakışma Tespit Edildi", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                     
                    if (choice == JOptionPane.YES_OPTION) {
                        fixZombieAppointment(txtChassis.getText());
                    }
                } else {
                    // Diğer trigger hataları (Kapasite vb.)
                    JOptionPane.showMessageDialog(this, userMessage, "İşlem Başarısız", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    // --- YARDIMCI METODLAR ---

    private void fixZombieAppointment(String chassis) {
        try (Connection conn = DBHelper.getConnection()) {
            PreparedStatement ps = conn.prepareStatement(DBHelper.getDeleteZombieAppointmentQuery());
            ps.setString(1, chassis);
            int count = ps.executeUpdate();
            
            if (count > 0) JOptionPane.showMessageDialog(this, "Eski randevu temizlendi! \nLütfen 'KAYDET' butonuna tekrar basınız.", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            else JOptionPane.showMessageDialog(this, "Temizlenecek kayıt bulunamadı.", "Uyarı", JOptionPane.WARNING_MESSAGE);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Temizleme hatası: " + e.getMessage());
        }
    }

    private void saveContactInfo(Connection conn, int ownerId, String type, String value) throws SQLException {
        if (value == null || value.trim().isEmpty()) return;
        String checkSql = "SELECT 1 FROM owner_contact WHERE owner_id = ? AND contact_type = ? AND contact_value = ?";
        try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
            psCheck.setInt(1, ownerId); psCheck.setString(2, type); psCheck.setString(3, value);
            if (psCheck.executeQuery().next()) return; 
        }
        String insertSql = "INSERT INTO owner_contact (owner_id, contact_type, contact_value, is_primary) VALUES (?, ?, ?, TRUE)";
        try (PreparedStatement psInsert = conn.prepareStatement(insertSql)) {
            psInsert.setInt(1, ownerId); psInsert.setString(2, type); psInsert.setString(3, value); psInsert.executeUpdate();
        }
    }

   // GÜNCELLENMİŞ ADRES KAYDI (Smart ID + Owner ID Fix)
    private int createAddress(Connection conn, int ownerId, String street, String bNo, String dNo, String district, String city, String zip) throws SQLException {
        
        // 1. Akıllı ID'yi Hesapla
        int smartId = DBHelper.generateSmartId(conn, "address", "address_id");
        
        // 2. SQL Sorgusu (ID'yi biz veriyoruz, owner_id'yi de ekliyoruz)
        String sql = "INSERT INTO address (address_id, owner_id, street, building_no, door_no, district, city, zip_code) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, smartId); 
            ps.setInt(2, ownerId); 
            ps.setString(3, street);
            ps.setString(4, bNo);
            ps.setString(5, dNo);
            ps.setString(6, district);
            ps.setString(7, city);
            ps.setString(8, zip);
            
            ps.executeUpdate();
            return smartId;
        }
    }

    private int getOrCreateOwnerId(Connection conn, String tc, String name, String surname) throws SQLException {
        PreparedStatement psCheck = conn.prepareStatement("SELECT owner_id FROM individual_owner WHERE national_id = ?");
        psCheck.setString(1, tc);
        ResultSet rs = psCheck.executeQuery();
        if (rs.next()) return rs.getInt(1);

        PreparedStatement psO = conn.prepareStatement("INSERT INTO owner(owner_type) VALUES('INDIVIDUAL') RETURNING owner_id");
        ResultSet rsO = psO.executeQuery(); rsO.next(); int oId = rsO.getInt(1);
        
        PreparedStatement psI = conn.prepareStatement("INSERT INTO individual_owner(owner_id, national_id, first_name, last_name) VALUES(?,?,?,?)");
        psI.setInt(1, oId); psI.setString(2, tc); psI.setString(3, name); psI.setString(4, surname); 
        psI.executeUpdate();
        return oId;
    }

    private int getOrCreateCompanyId(Connection conn, String taxNo, String compName, String taxOffice, String contactPerson) throws SQLException {
        PreparedStatement psCheck = conn.prepareStatement("SELECT owner_id FROM company_owner WHERE tax_number = ?");
        psCheck.setString(1, taxNo);
        ResultSet rs = psCheck.executeQuery();
        if (rs.next()) return rs.getInt(1);

        PreparedStatement psO = conn.prepareStatement("INSERT INTO owner(owner_type) VALUES('COMPANY') RETURNING owner_id");
        ResultSet rsO = psO.executeQuery(); rsO.next(); int oId = rsO.getInt(1);
        
        PreparedStatement psC = conn.prepareStatement("INSERT INTO company_owner(owner_id, company_name, tax_number, tax_office, contact_person) VALUES(?,?,?,?,?)");
        psC.setInt(1, oId); psC.setString(2, compName); psC.setString(3, taxNo); psC.setString(4, taxOffice); psC.setString(5, contactPerson);
        psC.executeUpdate();
        return oId;
    }

    private int getOrCreateVehicleId(Connection conn, int modelId, String chassis, String engine, String color, String yearStr) throws SQLException {
        PreparedStatement psCheck = conn.prepareStatement("SELECT vehicle_id FROM vehicle WHERE chassis_number = ?");
        psCheck.setString(1, chassis);
        ResultSet rs = psCheck.executeQuery();
        if (rs.next()) return rs.getInt(1);

        PreparedStatement psV = conn.prepareStatement("INSERT INTO vehicle(model_id, chassis_number, engine_number, color, production_year) VALUES(?,?,?,?,?) RETURNING vehicle_id");
        psV.setInt(1, modelId); psV.setString(2, chassis); psV.setString(3, engine); psV.setString(4, color); psV.setInt(5, Integer.parseInt(yearStr));
        ResultSet rsV = psV.executeQuery(); rsV.next(); 
        return rsV.getInt(1);
    }

    private boolean checkVehicleOwnerExists(Connection conn, int vId, int oId) throws SQLException {
        ResultSet rs = conn.createStatement().executeQuery("SELECT 1 FROM license_registration WHERE vehicle_id="+vId+" AND owner_id="+oId);
        return rs.next();
    }

    private void upsertLicense(Connection conn, int vId, String plate, int ownerId) throws SQLException {
        PreparedStatement psCheck = conn.prepareStatement("SELECT registration_id FROM license_registration WHERE vehicle_id = ?");
        psCheck.setInt(1, vId);
        if (!psCheck.executeQuery().next()) {
            PreparedStatement psL = conn.prepareStatement("INSERT INTO license_registration(vehicle_id, plate_number, license_serial_code, registration_date, owner_id) VALUES(?,?,?,CURRENT_DATE,?)");
            psL.setInt(1, vId); psL.setString(2, plate); psL.setString(3, "SERI-" + vId); 
            psL.setInt(4, ownerId); 
            psL.executeUpdate();
        }
    }

    private void validateTC() {
        String tc = txtTC.getText();
        
        if (tc.length() != 12) {
            lblTCWarning.setVisible(true); 
            lblTCWarning.setToolTipText("TC Kimlik No 12 haneli olmalıdır.");
            return;
        }

        try {
            int totalFirst10 = 0;
            for (int i = 0; i < 10; i++) {
                totalFirst10 += Character.getNumericValue(tc.charAt(i));
            }

            int lastDigit = Character.getNumericValue(tc.charAt(10));
            int remainder = totalFirst10 % 10; 

            if (remainder == lastDigit) {
                lblTCWarning.setVisible(false); 
            } else {
                lblTCWarning.setVisible(true); 
                lblTCWarning.setToolTipText("Geçersiz TC Kimlik Algoritması!");
            }

        } catch (NumberFormatException e) {
            lblTCWarning.setVisible(true);
        }
    }
}