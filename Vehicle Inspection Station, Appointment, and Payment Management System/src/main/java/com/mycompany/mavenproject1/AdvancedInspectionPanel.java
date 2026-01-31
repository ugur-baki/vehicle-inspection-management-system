package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdvancedInspectionPanel extends JPanel {
    
    private static AdvancedInspectionPanel instance;
    
    // --- UI BİLEŞENLERİ ---
    
    // Arama Kısmı
    JComboBox<String> cmbSearchCriteria;
    JTextField txtSearch;
    JButton btnSearch, btnReset;

    // Filtre Kısmı
    JComboBox<String> cmbFilterBrand;
    JComboBox<String> cmbYearMin, cmbYearMax;

    // Tablolar
    JTable tblMaster, tblDetail;

    // Alt Butonlar
    JButton btnEdit, btnReport, btnDelete, btnPayment;
    
    // Singleton Pattern
    public static AdvancedInspectionPanel getInstance() {
        if (instance == null) {
            instance = new AdvancedInspectionPanel();
        }
        return instance;
    }

    private AdvancedInspectionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // 1. ÜST PANEL (ARAMA)
        add(createSearchPanel(), BorderLayout.NORTH);

        // 2. SOL PANEL (FİLTRELER)
        add(createFilterPanel(), BorderLayout.WEST);

        // 3. ORTA PANEL (TABLOLAR)
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        
        // Üst Tablo (Master)
        tblMaster = new JTable(); 
        DBHelper.styleTable(tblMaster);
        JPanel pnlMaster = new JPanel(new BorderLayout());
        pnlMaster.setBorder(new TitledBorder("SONUÇ LİSTESİ (Aktif İşlemler)"));
        pnlMaster.add(new JScrollPane(tblMaster));

        // Alt Tablo (Detay)
        tblDetail = new JTable(); 
        DBHelper.styleTable(tblDetail);
        JPanel pnlDetail = new JPanel(new BorderLayout());
        pnlDetail.setBorder(new TitledBorder("HIZLI GÖRÜNÜM (Salt Okunur)"));
        pnlDetail.add(new JScrollPane(tblDetail));

        // 4. ALT BUTONLAR
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        btnEdit = new JButton("Düzenle / Test Gir"); 
        btnReport = new JButton("Önizle"); 
        btnDelete = new JButton("Sil");
        btnPayment = new JButton("Ödeme Al"); 
        
        btnPayment.setEnabled(false); 
        setButtonsEnabled(false);
 
        pnlActions.add(btnEdit); 
        pnlActions.add(Box.createHorizontalStrut(10));
        pnlActions.add(btnPayment); 
        pnlActions.add(Box.createHorizontalStrut(5));
        pnlActions.add(btnReport); 
        pnlActions.add(Box.createHorizontalStrut(10));
        pnlActions.add(btnDelete);

        JPanel pnlBottomContainer = new JPanel(new BorderLayout());
        pnlBottomContainer.add(pnlDetail, BorderLayout.CENTER);
        pnlBottomContainer.add(pnlActions, BorderLayout.SOUTH);

        split.setTopComponent(pnlMaster);
        split.setBottomComponent(pnlBottomContainer);
        split.setDividerLocation(400); 
        add(split, BorderLayout.CENTER);

        // --- OLAYLAR (EVENTS) ---

        tblMaster.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean rowSelected = tblMaster.getSelectedRow() != -1;
                setButtonsEnabled(rowSelected); 
                
                if (rowSelected) {
                    int id = Integer.parseInt(tblMaster.getValueAt(tblMaster.getSelectedRow(), 0).toString());
                    loadDetailData(id);
                } else {
                    ((javax.swing.table.DefaultTableModel)tblDetail.getModel()).setRowCount(0);
                }
            }
        });

        tblMaster.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && tblMaster.getSelectedRow() != -1) {
                    openEditor();
                }
            }
        });

        btnSearch.addActionListener(e -> executeFullQuery());
        btnReset.addActionListener(e -> resetFilters());
        btnEdit.addActionListener(e -> openEditor());
        btnReport.addActionListener(e -> showReportPreview()); 
        btnDelete.addActionListener(e -> deleteSelectedRecord());
        btnPayment.addActionListener(e -> openPaymentDialog());
        txtSearch.addActionListener(e -> executeFullQuery());

        // Başlangıçta verileri yükle
        executeFullQuery();
        
        // Temayı uygula
        ThemeDecorator.getInstance().applyTheme(this);
    }
    
    // --- YARDIMCI METODLAR ---

    private void setButtonsEnabled(boolean enabled) {
        btnEdit.setEnabled(enabled);
        btnReport.setEnabled(enabled);
        btnDelete.setEnabled(enabled);
        btnPayment.setEnabled(enabled); 
    }

    private JPanel createSearchPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        p.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));

        JLabel lblIcon = new JLabel("Detaylı Arama:");
        lblIcon.setFont(AppConfig.FONT_HEADER);
        p.add(lblIcon);

        cmbSearchCriteria = new JComboBox<>(new String[]{"Müşteri Adı", "Plaka", "Şasi No", "Muayene No"});
        cmbSearchCriteria.setPreferredSize(new Dimension(150, 30));
        p.add(cmbSearchCriteria);

        txtSearch = new JTextField(25);
        txtSearch.setPreferredSize(new Dimension(200, 30));
        p.add(txtSearch);

        btnSearch = new JButton("BUL");
        btnSearch.setPreferredSize(new Dimension(80, 30));
        
        btnReset = new JButton("Temizle");
        btnReset.setPreferredSize(new Dimension(80, 30));

        p.add(btnSearch);
        p.add(btnReset);

        return p;
    }

    private JPanel createFilterPanel() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(new CompoundBorder(new MatteBorder(0, 0, 0, 1, Color.LIGHT_GRAY), new EmptyBorder(10, 10, 10, 10)));
        p.setBackground(AppConfig.COLOR_BG_FILTER);
        p.setPreferredSize(new Dimension(230, 0));

        JLabel lblTitle = new JLabel("FİLTRELER");
        lblTitle.setFont(AppConfig.FONT_TITLE);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(lblTitle);
        p.add(Box.createVerticalStrut(20));

        // Marka
        addFilterLabel(p, "Araç Markası:");
        cmbFilterBrand = new JComboBox<>();
        cmbFilterBrand.addItem("Tümü");
        loadBrandsToCombo();
        cmbFilterBrand.setMaximumSize(new Dimension(200, 30));
        cmbFilterBrand.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(cmbFilterBrand);
        p.add(Box.createVerticalStrut(20));

        // Yıl (GÜNCELLENDİ: ARALIK GENİŞLETİLDİ)
        addFilterLabel(p, "Model Yılı Aralığı:");
        JPanel pnlYear = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        pnlYear.setOpaque(false);
        
        cmbYearMin = new JComboBox<>();
        cmbYearMax = new JComboBox<>();
        
        // 1970 - 2030 arası (Daha geniş kapsam)
        for (int i = 1970; i <= 2030; i++) {
            cmbYearMin.addItem(String.valueOf(i));
            cmbYearMax.addItem(String.valueOf(i));
        }
        
        // Varsayılanları güvenli seç
        cmbYearMin.setSelectedItem("1990");
        cmbYearMax.setSelectedItem("2026");
        
        cmbYearMin.setPreferredSize(new Dimension(70, 25));
        cmbYearMax.setPreferredSize(new Dimension(70, 25));

        pnlYear.add(cmbYearMin); 
        pnlYear.add(new JLabel(" - ")); 
        pnlYear.add(cmbYearMax);
        pnlYear.setAlignmentX(Component.LEFT_ALIGNMENT);
        p.add(pnlYear);
        p.add(Box.createVerticalStrut(30));

        JButton btnApply = new JButton("Filtreleri Uygula");
        btnApply.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnApply.setMaximumSize(new Dimension(200, 40));
        btnApply.addActionListener(e -> executeFullQuery());
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

    private void resetFilters() {
        txtSearch.setText("");
        cmbSearchCriteria.setSelectedIndex(0);
        cmbFilterBrand.setSelectedIndex(0);
        cmbYearMin.setSelectedItem("1990");
        cmbYearMax.setSelectedItem("2026");
        executeFullQuery();
    }

    // --- VERİTABANI İŞLEMLERİ ---

    private void loadBrandsToCombo() {
        try (Connection conn = DBHelper.getConnection(); 
             Statement stmt = conn.createStatement(); 
             ResultSet rs = stmt.executeQuery("SELECT DISTINCT brand_name FROM brand ORDER BY brand_name")) {
            while (rs.next()) cmbFilterBrand.addItem(rs.getString(1));
        } catch (Exception e) {}
    }

    private void executeFullQuery() {
        String searchText = txtSearch.getText().trim();
        String searchCriteria = (String) cmbSearchCriteria.getSelectedItem();
        StringBuilder sql = new StringBuilder();
        
        //  1: Şirket İsimlerini de Destekle (COALESCE Kullanımı)
        sql.append("SELECT i.inspection_id AS \"Muayene No\", l.plate_number AS \"Plaka\", ");
        sql.append("COALESCE(ind.first_name || ' ' || ind.last_name, comp.company_name, 'Bilinmiyor') AS \"Müşteri\", ");
        sql.append("b.brand_name || ' ' || m.model_name AS \"Marka/Model\", v.production_year AS \"Yıl\", ");
        sql.append("v.chassis_number AS \"Şasi No\", i.result_status AS \"Sonuç\", ");
        sql.append("p.amount AS \"Ödeme\", i.start_time ");
        
        sql.append("FROM inspection i ");
        sql.append("JOIN appointment a ON i.appointment_id = a.appointment_id ");
        sql.append("JOIN vehicle v ON a.vehicle_id = v.vehicle_id ");
        sql.append("JOIN license_registration l ON v.vehicle_id = l.vehicle_id ");
        sql.append("JOIN model m ON v.model_id = m.model_id ");
        sql.append("JOIN brand b ON m.brand_id = b.brand_id ");
        sql.append("JOIN owner o ON l.owner_id = o.owner_id ");
        
        // Hem Bireysel Hem Kurumsal Tabloyu Bağla
        sql.append("LEFT JOIN individual_owner ind ON o.owner_id = ind.owner_id ");
        sql.append("LEFT JOIN company_owner comp ON o.owner_id = comp.owner_id "); // EKSİK OLAN BUYDU
        
        sql.append("LEFT JOIN payment p ON p.inspection_id = i.inspection_id ");
        sql.append("WHERE 1=1 ");
        
        // 1. Arama Kriteri (Şirket adı araması da eklendi)
        if (!searchText.isEmpty()) {
            if (searchCriteria.equals("Müşteri Adı"))
                sql.append("AND (ind.first_name ILIKE '%").append(searchText).append("%' OR ind.last_name ILIKE '%").append(searchText).append("%' OR comp.company_name ILIKE '%").append(searchText).append("%') ");
            else if (searchCriteria.equals("Plaka"))
                sql.append("AND l.plate_number ILIKE '%").append(searchText).append("%' ");
            else if (searchCriteria.equals("Şasi No"))
                sql.append("AND v.chassis_number ILIKE '%").append(searchText).append("%' ");
            else if (searchCriteria.equals("Muayene No")) {
                try {
                    int id = Integer.parseInt(searchText);
                    sql.append("AND i.inspection_id = ").append(id).append(" ");
                } catch(NumberFormatException e) {}
            }
        }

        // 2. Marka Filtresi
        String selectedBrand = (String) cmbFilterBrand.getSelectedItem();
        if (selectedBrand != null && !selectedBrand.equals("Tümü")) {
            sql.append("AND b.brand_name = '").append(selectedBrand).append("' ");
        }

        // 3. Yıl Filtresi
        String minYear = (String) cmbYearMin.getSelectedItem();
        String maxYear = (String) cmbYearMax.getSelectedItem();
        sql.append("AND v.production_year BETWEEN ").append(minYear).append(" AND ").append(maxYear).append(" ");

        //  2: Büyük/Küçük harf duyarlılığını kaldır (UPPER)
        // Böylece 'Active', 'active', 'ACTIVE' hepsi gelir.
        sql.append("AND UPPER(a.status) = 'ACTIVE' "); 

        sql.append("ORDER BY i.start_time DESC LIMIT 100");
        
        // Hata ayıklama için konsola yazdır
        System.out.println("DEBUG SQL: " + sql.toString());

        DBHelper.fillTable(tblMaster, sql.toString());
        setButtonsEnabled(false);
    }

    private void loadDetailData(int inspectionId) {
        DBHelper.fillTable(tblDetail, "SELECT tp.test_name AS \"Test Adı\", COALESCE(CAST(tr.measured_value AS VARCHAR), '-') AS \"Ölçülen Değer\", tp.unit AS \"Birim\", (tp.min_limit || ' - ' || tp.max_limit) AS \"Referans\", CASE WHEN tr.test_result_id IS NULL THEN 'YAPILMADI' WHEN tr.is_passed THEN 'GEÇTİ' ELSE 'KALDI' END AS \"Durum\" FROM test_parameter tp LEFT JOIN test_result tr ON tp.test_parameter_id = tr.test_parameter_id AND tr.inspection_id = " + inspectionId);
    }

    private void openEditor() {
        if(tblMaster.getSelectedRow() == -1) return;
        int id = Integer.parseInt(tblMaster.getValueAt(tblMaster.getSelectedRow(), 0).toString());
        
        InspectionEditorDialog editor = InspectionEditorDialog.getInstance(id);
        editor.setVisible(true);
        executeFullQuery(); 
    }

    private void openPaymentDialog() {
        int selectedRow = tblMaster.getSelectedRow();
        if (selectedRow == -1) return;

        int id = Integer.parseInt(tblMaster.getValueAt(selectedRow, 0).toString());
        String plate = tblMaster.getValueAt(selectedRow, 1).toString();
        String owner = tblMaster.getValueAt(selectedRow, 2).toString();
        
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        PaymentDialog dlg = new PaymentDialog(parentWindow, id, plate, owner);
        dlg.setVisible(true); 
        
        // Ödeme sonrası liste yenileme (ACTIVE -> COMPLETED olduğu için listeden gitmeli)
        executeFullQuery();
    }

    private void deleteSelectedRecord() {
         if (tblMaster.getSelectedRow() == -1) return;
         int id = Integer.parseInt(tblMaster.getValueAt(tblMaster.getSelectedRow(), 0).toString());
         
         if(JOptionPane.showConfirmDialog(this, "Bu kaydı silmek istediğine emin misin?\nBu işlem geri alınamaz!", "Silme Onayı", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
             try (Connection conn = DBHelper.getConnection(); Statement stmt = conn.createStatement()) {
                 stmt.executeUpdate("DELETE FROM test_result WHERE inspection_id=" + id);
                 stmt.executeUpdate("DELETE FROM inspection_defect WHERE inspection_id=" + id); 
                 stmt.executeUpdate("DELETE FROM payment WHERE inspection_id=" + id); 
                 stmt.executeUpdate("DELETE FROM inspection WHERE inspection_id=" + id);
                 
                 // Randevuyu da temizle (İsteğe bağlı)
                 // stmt.executeUpdate("DELETE FROM appointment WHERE appointment_id IN (SELECT appointment_id FROM inspection WHERE inspection_id=" + id + ")");
                 
                 JOptionPane.showMessageDialog(this, "Kayıt başarıyla silindi.");
                 executeFullQuery();
             } catch(Exception e) { 
                 e.printStackTrace();
                 JOptionPane.showMessageDialog(this, "Hata oluştu: " + e.getMessage());
             }
         }
    }
    
   private void showReportPreview() { 
        int selectedRow = tblMaster.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Lütfen önizlemek için bir kayıt seçin!", "Uyarı", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int inspectionId = Integer.parseInt(tblMaster.getValueAt(selectedRow, 0).toString());
        String plaka = tblMaster.getValueAt(selectedRow, 1).toString();
        String sahip = tblMaster.getValueAt(selectedRow, 2).toString();

        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        JFrame parentFrame = null;
        if (parentWindow instanceof JFrame) {
            parentFrame = (JFrame) parentWindow;
        }

        ReportPreviewDialog.getInstance(parentFrame, inspectionId, plaka, sahip).setVisible(true);
    }
}