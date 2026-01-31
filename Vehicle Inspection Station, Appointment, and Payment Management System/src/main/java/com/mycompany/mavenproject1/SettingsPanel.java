package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class SettingsPanel extends JPanel {
    private static SettingsPanel instance;

    JToggleButton tglDarkMode;
    JButton btnTestConnection;
    JButton btnClearData;
    JLabel lblStatus;

    public static SettingsPanel getInstance() {
        if (instance == null) {
            instance = new SettingsPanel();
        }
        return instance;
    }

    private SettingsPanel() {
        setLayout(new BorderLayout(20, 20));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);

        // --- BAŞLIK ---
        JLabel lblTitle = new JLabel("SİSTEM AYARLARI");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(new Color(52, 73, 94));
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        add(lblTitle, BorderLayout.NORTH);

        // --- ORTA PANEL (KUTULAR) ---
        JPanel pnlCenter = new JPanel(new GridLayout(1, 2, 20, 0)); // Yan yana iki kutu
        pnlCenter.setOpaque(false);

        // 1. KUTU: GÖRÜNÜM
        JPanel pnlAppearance = new JPanel(new GridBagLayout());
        pnlAppearance.setBorder(createStyledBorder("Görünüm Ayarları"));
        pnlAppearance.setBackground(new Color(245, 245, 250));
        
        tglDarkMode = new JToggleButton("Karanlık Mod Kapalı");
        tglDarkMode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        tglDarkMode.setPreferredSize(new Dimension(200, 40));
        tglDarkMode.addActionListener(e -> toggleTheme());
        
        pnlAppearance.add(tglDarkMode);


        // 2. KUTU: VERİTABANI YÖNETİMİ
        JPanel pnlDatabase = new JPanel(new GridLayout(3, 1, 10, 10));
        pnlDatabase.setBorder(createStyledBorder("Veritabanı İşlemleri"));
        pnlDatabase.setBackground(new Color(245, 245, 250));

        btnTestConnection = new JButton("Bağlantıyı Test Et");
        btnTestConnection.setBackground(new Color(52, 152, 219));
        btnTestConnection.setForeground(Color.WHITE);
        btnTestConnection.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTestConnection.addActionListener(e -> testDB());

        btnClearData = new JButton("Tüm Muayene Geçmişini Sil");
        btnClearData.setBackground(new Color(231, 76, 60)); // Kırmızı
        btnClearData.setForeground(Color.WHITE);
        btnClearData.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnClearData.addActionListener(e -> clearInspectionHistory());
        
        lblStatus = new JLabel("Durum: Bekleniyor...", JLabel.CENTER);
        lblStatus.setFont(new Font("Segoe UI", Font.ITALIC, 12));

        pnlDatabase.add(btnTestConnection);
        pnlDatabase.add(btnClearData);
        pnlDatabase.add(lblStatus);

        pnlCenter.add(pnlAppearance);
        pnlCenter.add(pnlDatabase);

        add(pnlCenter, BorderLayout.CENTER);

        // --- ALT BİLGİ ---
        JLabel lblFooter = new JLabel("ProInspection v5.0 | Geliştirici: mycompany | 2026 © Tüm Hakları Saklıdır.", JLabel.CENTER);
        lblFooter.setForeground(Color.GRAY);
        lblFooter.setBorder(new EmptyBorder(20, 0, 0, 0));
        add(lblFooter, BorderLayout.SOUTH);

        // --- BAŞLANGIÇ DURUMU KONTROLÜ ---
        // Eğer uygulama açıldığında Dark Mode aktifse, butonu ona göre ayarla
        if (ThemeDecorator.getInstance().isDarkMode()) {
            tglDarkMode.setSelected(true);
            tglDarkMode.setText("Aydınlık Moda Geç");
            
            // SettingsPanel.instance yerine 'this' kullanmalısın.
            // Çünkü bu satır çalışırken 'instance' henüz null olabilir.
            ThemeDecorator.getInstance().applyTheme(this); 
        } else {
            tglDarkMode.setSelected(false);
            tglDarkMode.setText("Karanlık Mod Kapalı");
        }
    }

    // --- METODLAR ---

    private TitledBorder createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                " " + title + " ",
                TitledBorder.CENTER,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 16),
                new Color(100, 100, 100)
        );
    }

    private void toggleTheme() {
        boolean isDarkNow;

        if (tglDarkMode.isSelected()) {
            tglDarkMode.setText("Aydınlık Moda Geç");
            isDarkNow = true;
        } else {
            tglDarkMode.setText("Karanlık Mod Kapalı");
            isDarkNow = false;
        }

        // 1. Singleton üzerinden bellekte değiştir
        ThemeDecorator.getInstance().setDarkMode(isDarkNow);
        
        // 2. EKRANI YENİLE (Mevcut paneli boya)
        ThemeDecorator.getInstance().applyTheme(SettingsPanel.instance); // Kendini boya
        ThemeDecorator.getInstance().applyTheme(MainDashboard.getInstance()); // Ana ekranı boya
        MainDashboard.getInstance().repaint();

        // 3. VERİTABANINA KAYDET (Arka planda thread ile)
        new Thread(() -> {
            DBHelper.updateThemeMode(isDarkNow);
        }).start();
    }

    private void testDB() {
        try (Connection conn = DBHelper.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                lblStatus.setText("Durum:Bağlantı Başarılı!");
                lblStatus.setForeground(new Color(46, 204, 113));
                JOptionPane.showMessageDialog(this, "Veritabanı bağlantısı sorunsuz çalışıyor!", "Başarılı", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (Exception e) {
            lblStatus.setText("Durum: Bağlantı Hatası!");
            lblStatus.setForeground(Color.RED);
            JOptionPane.showMessageDialog(this, "Hata: " + e.getMessage(), "Bağlantı Hatası", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearInspectionHistory() {
        // Kullanıcıya neyin silineceğini net söyleyelim
        int confirm = JOptionPane.showConfirmDialog(this, 
                "DİKKAT! Bu işlem şunları silecektir:\n" +
                "- Tüm Test Sonuçları\n" +
                "- Girilen Kusur Kayıtları\n" +
                "- Tamamlanmış ve Bekleyen Tüm Muayeneler\n" +
                "- Tüm Randevu Kayıtları\n\n" +
                "Müşteri ve Araç kayıtları SAKLANACAKTIR.\n" +
                "Devam etmek istiyor musun?", 
                "Sistem Veri Temizliği", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        
        if (confirm == JOptionPane.YES_OPTION) {
            try (Connection conn = DBHelper.getConnection()) {
                conn.setAutoCommit(false); // Transaction (İşlem bütünlüğü) başlat
                
                try (Statement stmt = conn.createStatement()) {
                    // --- SİLME SIRASI ÇOK ÖNEMLİ (Foreign Key Hata Vermemesi İçin) ---
                    
                    // 1. Adım: Muayeneye bağlı alt tabloları temizle
                    stmt.executeUpdate("DELETE FROM test_result");        // Test ölçümleri
                    stmt.executeUpdate("DELETE FROM inspection_defect");  // Girilen kusurlar
                    stmt.executeUpdate("DELETE FROM payment");            // Ödemeler (Eğer kullanıyorsan)
                    
                    // 2. Adım: Muayene tablosunu temizle
                    stmt.executeUpdate("DELETE FROM inspection");         // Ana muayene kaydı
                    
                    // 3. Adım: Randevuları temizle (Artık muayene bağı kalmadığı için silinebilir)
                    stmt.executeUpdate("DELETE FROM appointment");        // Randevular
                    
                    conn.commit(); // Her şey başarılıysa onayla
                    JOptionPane.showMessageDialog(this, "Temizlik Başarılı!\nTüm muayene ve randevu geçmişi silindi.");
                    
                } catch (SQLException ex) {
                    conn.rollback(); // Bir hata olursa hiçbir şeyi silme, geri al
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Silme işlemi sırasında hata oluştu ve geri alındı:\n" + ex.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Bağlantı hatası: " + e.getMessage());
            }
        }
    }
}