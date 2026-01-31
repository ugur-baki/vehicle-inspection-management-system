package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class OldRegistrationsPanel extends JPanel {
    private static OldRegistrationsPanel instance;

    private JTable tableHistory;
    private DefaultTableModel modelHistory;
    private JTextField txtSearchPlate;
    private JCheckBox chkPassed, chkFailed, chkPending;
    private JButton btnHTML;

    // Singleton Pattern
    public static OldRegistrationsPanel getInstance() {
        if (instance == null) {
            instance = new OldRegistrationsPanel();
        }
        return instance;
    }

    private OldRegistrationsPanel() {
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(15, 15, 15, 15));

        // Filtre Paneli
        add(createFilterPanel(), BorderLayout.WEST);

        // 1. ÜST PANEL (Arama ve Butonlar)
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBorder(new EmptyBorder(0, 0, 15, 0));

        JLabel lblTitle = new JLabel("Geçmiş Muayene Kayıtları");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(new Color(44, 62, 80));

        JPanel pnlSearch = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JLabel lblSearch = new JLabel("Plaka Ara:");
        txtSearchPlate = new JTextField(12);
        
        JButton btnRefresh = new JButton("Yenile");
        // Detay Butonu
        JButton btnDetails = new JButton("Seçili Kaydın Detaylarını Gör");
        btnDetails.setFocusPainted(false);
        // PDF Butonu
        btnHTML = new JButton("PDF Olarak Kaydet");

        // Arama Aksiyonu
        txtSearchPlate.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                loadHistoryData(txtSearchPlate.getText().trim());
            }
        });

        btnRefresh.addActionListener(e -> {
            txtSearchPlate.setText("");
            loadHistoryData("");
        });

        // ==========================================
        // BUTON AKSİYONU (DETAY PENCERESİ AÇMA)
        // ==========================================
        btnDetails.addActionListener(e -> {
            int selectedRow = tableHistory.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Lütfen detaylarını görmek istediğiniz kaydı tablodan seçiniz.", 
                    "Seçim Yapılmadı", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tablodan verileri al
            // Modeldeki sütun sırasına göre: 0->ID, 1->Plaka, 2->Araç, 3->SAHİP
            int inspectionId = (int) modelHistory.getValueAt(selectedRow, 0); 
            String plate = (String) modelHistory.getValueAt(selectedRow, 1);
            
            //  Artık 3. indeksteki 'Araç Sahibi' sütununu çekiyoruz
            String ownerInfo = (String) modelHistory.getValueAt(selectedRow, 3); 

            // ReportPreviewDialog'u aç
            JFrame parentFrame = (JFrame) SwingUtilities.getWindowAncestor(this);
            
            ReportPreviewDialog dialog = ReportPreviewDialog.getInstance(parentFrame, inspectionId, plate, ownerInfo);
            dialog.setVisible(true);
        });

        // ==========================================
        // PDF BUTONU AKSİYONU
        // ==========================================
        btnHTML.addActionListener(e -> {
            int selectedRow = tableHistory.getSelectedRow();
            
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, 
                    "Lütfen PDF olarak kaydetmek istediğiniz kaydı tablodan seçiniz.", 
                    "Seçim Yapılmadı", 
                    JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Tablodan verileri al
            int inspectionId = (int) modelHistory.getValueAt(selectedRow, 0); 
            String plate = (String) modelHistory.getValueAt(selectedRow, 1);
            String vehicleModel = (String) modelHistory.getValueAt(selectedRow, 2); 
            // PDF için de doğru sahip bilgisini alıyoruz
            String ownerInfo = (String) modelHistory.getValueAt(selectedRow, 3); 

            // PDF'yi kaydet
            PDFSaver.saveInspectionReport(inspectionId, plate, ownerInfo, vehicleModel);
        });

        pnlSearch.add(lblSearch);
        pnlSearch.add(txtSearchPlate);
        pnlSearch.add(btnRefresh);
        pnlSearch.add(btnDetails);
        pnlSearch.add(btnHTML); 

        pnlTop.add(lblTitle, BorderLayout.WEST);
        pnlTop.add(pnlSearch, BorderLayout.EAST);

        // 2. TABLO (Geçmiş Kayıtlar)
        //  "Araç Sahibi" sütunu eklendi
        String[] columns = {"ID", "Plaka", "Araç Bilgisi", "Araç Sahibi", "Muayene Tarihi", "Müfettiş", "Sonuç", "Notlar"};
        modelHistory = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Tablo düzenlenemesin
            }
            
            // ID sütunu integer dönsün
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Integer.class;
                return String.class;
            }
        };

        tableHistory = new JTable(modelHistory);
        styleTable(tableHistory);
        
        // Tekli seçim modu
        tableHistory.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(tableHistory);

        // Panellere ekle
        add(pnlTop, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        // İlk verileri yükle
        loadHistoryData("");
    }

    // ==========================================
    // VERİTABANI İŞLEMLERİ
    // ==========================================
    private void loadHistoryData(String plateSearch) {
        modelHistory.setRowCount(0); // Tabloyu temizle

        //  SQL Sorgusuna Owner tabloları JOIN edildi
        String sql = 
            "SELECT " +
            "  i.inspection_id, " +
            "  lr.plate_number, " +  
            "  b.brand_name || ' ' || m.model_name AS arac_bilgisi, " +
            // SAHİP BİLGİSİ SORGUSU (Bireysel veya Şirket)
            "  COALESCE(io.first_name || ' ' || io.last_name, co.company_name, 'Bilinmiyor') AS sahip_bilgisi, " +
            "  i.end_time, " +
            "  p.first_name || ' ' || p.last_name AS inspector_name, " +
            "  i.result_status, " +
            "  i.technician_notes " +
            "FROM inspection i " +
            "JOIN appointment a ON i.appointment_id = a.appointment_id " +
            "JOIN vehicle v ON a.vehicle_id = v.vehicle_id " +
            "JOIN license_registration lr ON v.vehicle_id = lr.vehicle_id " + 
            "JOIN model m ON v.model_id = m.model_id " +
            "JOIN brand b ON m.brand_id = b.brand_id " +
            //  vehicle_owner tablosu kaldırıldı.
            // İlişkiler doğrudan license_registration (lr) üzerinden kuruluyor.
            "LEFT JOIN individual_owner io ON lr.owner_id = io.owner_id " +
            "LEFT JOIN company_owner co ON lr.owner_id = co.owner_id " +
            "LEFT JOIN inspector ins ON i.inspector_id = ins.personnel_id " +
            "LEFT JOIN personnel p ON ins.personnel_id = p.personnel_id " +
            "WHERE i.result_status IS NOT NULL " +
            "  AND lr.is_active = TRUE "; 

        // Arama filtresi
        if (!plateSearch.isEmpty()) {
            sql += "AND lr.plate_number ILIKE ? "; 
        }

        // Durum Filtresi
        ArrayList<String> statuses = new ArrayList<>();
        if (chkPassed.isSelected()) {
            statuses.add("'GEÇTİ'"); statuses.add("'PASSED'");
        }
        if (chkFailed.isSelected()) {
            statuses.add("'KALDI'"); statuses.add("'FAILED'");
        }
        if (chkPending.isSelected()) {
            statuses.add("'DEVAM EDİYOR'"); statuses.add("'PENDING'"); statuses.add("'SÜRÜYOR'");
        }
        if (!statuses.isEmpty()) {
            sql += "AND i.result_status IN (" + String.join(",", statuses) + ") ";
        }

        sql += "ORDER BY i.end_time DESC";

        try (Connection conn = DBHelper.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (!plateSearch.isEmpty()) {
                ps.setString(1, "%" + plateSearch + "%");
            }

            ResultSet rs = ps.executeQuery();
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");

            while (rs.next()) {
                int id = rs.getInt("inspection_id");
                String plate = rs.getString("plate_number");
                String vehicle = rs.getString("arac_bilgisi");
                String owner = rs.getString("sahip_bilgisi"); // Yeni veri
                
                Timestamp ts = rs.getTimestamp("end_time");
                String date = (ts != null) ? sdf.format(ts) : "Tarih Yok";
                
                String inspector = rs.getString("inspector_name");
                String result = rs.getString("result_status"); 
                String notes = rs.getString("technician_notes");

                // Görüntüleme için Türkçeleştirme
                String displayResult = result;
                if ("PASSED".equalsIgnoreCase(result)) displayResult = "GEÇTİ";
                else if ("FAILED".equalsIgnoreCase(result)) displayResult = "KALDI";

                // Satıra ekle (Sıralama önemli)
                modelHistory.addRow(new Object[]{id, plate, vehicle, owner, date, inspector, displayResult, notes});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Veri çekilirken hata: " + e.getMessage());
        }
    }

    // ==========================================
    // TABLO GÖRSELLİĞİ
    // ==========================================
    private void styleTable(JTable table) {
        table.setRowHeight(30);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        table.setFillsViewportHeight(true);

        // Sütun Genişlikleri
        table.getColumnModel().getColumn(0).setPreferredWidth(40);  // ID
        table.getColumnModel().getColumn(1).setPreferredWidth(90);  // Plaka
        table.getColumnModel().getColumn(2).setPreferredWidth(150); // Araç
        table.getColumnModel().getColumn(3).setPreferredWidth(150); // Sahip (Yeni)
        table.getColumnModel().getColumn(6).setPreferredWidth(80);  // Sonuç (Index kaydı)

        // SONUÇ Sütunu Renklendirme (Index artık 6)
        table.getColumnModel().getColumn(6).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                String status = (String) value;
                setFont(new Font("Segoe UI", Font.BOLD, 12));
                
                if ("GEÇTİ".equals(status)) {
                    setForeground(new Color(39, 174, 96)); 
                } else if ("KALDI".equals(status)) {
                    setForeground(new Color(192, 57, 43)); 
                } else {
                    if (ThemeDecorator.getInstance().isDarkMode()) setForeground(ThemeDecorator.DARK_TEXT);
                      else setForeground(ThemeDecorator.LIGHT_TEXT);
                }
                return c;
            }
        });
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY), new EmptyBorder(10, 10, 10, 10)));
        p.setBackground(AppConfig.COLOR_BG_FILTER);
        p.setPreferredSize(new Dimension(230, 0));

        JLabel lblTitle = new JLabel("FİLTRELER");
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblTitle);
        p.add(Box.createVerticalStrut(20));

        // Durum
        addFilterLabel(p, "Muayene Durumu:");
        chkPassed = new JCheckBox("GEÇTİ (Başarılı)", true);
        chkFailed = new JCheckBox("KALDI (Hatalı)", true);
        chkPending = new JCheckBox("SÜRÜYOR (Bekleyen)", true);
        
        // Checkbox arkaplanlarını panel rengine uydur
        chkPassed.setOpaque(false); chkFailed.setOpaque(false); chkPending.setOpaque(false);

        p.add(chkPassed); p.add(chkFailed); p.add(chkPending);
        p.add(Box.createVerticalStrut(20));

        // Uygula Butonu
        JButton btnApply = new JButton("Filtreleri Uygula");
        btnApply.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnApply.setMaximumSize(new Dimension(200, 40));
        btnApply.addActionListener(e -> loadHistoryData(txtSearchPlate.getText().trim()));
        p.add(btnApply);

        return p;
    }

    private void addFilterLabel(JPanel p, String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(l);
        p.add(Box.createVerticalStrut(5));
    }
}