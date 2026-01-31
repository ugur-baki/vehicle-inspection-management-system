package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class CustomersInfoPanel extends JPanel {

    private static CustomersInfoPanel instance;

    private JTable tblCustomers;
    private DefaultTableModel modelCustomers;

    private JTextField txtSearch;
    private JComboBox<String> cmbTypeFilter;
    private JButton btnSearch, btnRefresh;

    public static CustomersInfoPanel getInstance() {
        if (instance == null) {
            instance = new CustomersInfoPanel();
        }
        return instance;
    }

    private CustomersInfoPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // =========================
        // TOP PANEL
        // =========================
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlTop.setBorder(createStyledBorder("Müşteri Sorgulama"));

        pnlTop.add(new JLabel("🔍 Ara (İsim / No):"));
        txtSearch = new JTextField(20);
        pnlTop.add(txtSearch);

        pnlTop.add(new JLabel("Müşteri Tipi:"));
        cmbTypeFilter = new JComboBox<>(new String[]{"Tümü", "Bireysel", "Kurumsal"});
        pnlTop.add(cmbTypeFilter);

        btnSearch = new JButton("Bul");
        btnRefresh = new JButton("Yenile");

        pnlTop.add(btnSearch);
        pnlTop.add(btnRefresh);

        add(pnlTop, BorderLayout.NORTH);

        // =========================
        // TABLE
        // =========================
        String[] columns = {
                "ID", "Tip", "Müşteri",
                "TC / Vergi No",
                "Telefon",
                "Email",
                "Şehir",
                "İlçe",
                "Adres",
                "Kayıt Tarihi"
        };

        modelCustomers = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tblCustomers = new JTable(modelCustomers);
        DBHelper.styleTable(tblCustomers);

        tblCustomers.getColumnModel().getColumn(0).setMaxWidth(60);
        tblCustomers.getColumnModel().getColumn(1).setPreferredWidth(90);
        tblCustomers.getColumnModel().getColumn(2).setPreferredWidth(220);
        tblCustomers.getColumnModel().getColumn(8).setPreferredWidth(300);

        add(new JScrollPane(tblCustomers), BorderLayout.CENTER);

        // =========================
        // EVENTS
        // =========================
        btnSearch.addActionListener(e -> loadData());

        btnRefresh.addActionListener(e -> {
            txtSearch.setText("");
            cmbTypeFilter.setSelectedIndex(0);
            loadData();
        });

        txtSearch.addActionListener(e -> loadData());

        loadData();
    }

    // =========================
    // DATA LOAD
    // =========================
    private void loadData() {

        modelCustomers.setRowCount(0);

        String search = txtSearch.getText().trim();
        String typeFilter = (String) cmbTypeFilter.getSelectedItem();

        StringBuilder sql = new StringBuilder();

        //  Ara tablo (owner_address) kaldırıldı.
        // Adres tablosu artık doğrudan owner_id'ye bağlı.
        sql.append("""
            SELECT
                o.owner_id,
                o.owner_type,
                o.created_date,

                CASE
                    WHEN o.owner_type = 'INDIVIDUAL'
                    THEN i.first_name || ' ' || i.last_name
                    ELSE c.company_name
                END AS customer_name,

                MAX(
                    CASE
                        WHEN o.owner_type = 'INDIVIDUAL'
                        THEN i.national_id
                        ELSE c.tax_number
                    END
                ) AS identifier,

                -- Contact tablosu row-based olduğu için MAX ile pivotluyoruz
                MAX(CASE WHEN con.contact_type = 'Telefon' THEN con.contact_value END) AS phone,
                MAX(CASE WHEN con.contact_type = 'Email' THEN con.contact_value END) AS email,

                a.city,
                a.district,
                a.street || ' No:' || a.building_no || '/' || a.door_no AS full_address

            FROM owner o
            LEFT JOIN individual_owner i ON o.owner_id = i.owner_id
            LEFT JOIN company_owner c ON o.owner_id = c.owner_id
            LEFT JOIN owner_contact con ON con.owner_id = o.owner_id
            -- owner_address tablosu SİLİNDİ, doğrudan address tablosuna join atıyoruz
            LEFT JOIN address a ON a.owner_id = o.owner_id
            WHERE 1=1
        """);

        if (!search.isEmpty()) {
            sql.append("""
                AND (
                    i.first_name ILIKE '%""" + search + """
                    %' OR
                    i.last_name  ILIKE '%""" + search + """
                    %' OR
                    i.national_id LIKE '%""" + search + """
                    %' OR
                    c.company_name ILIKE '%""" + search + """
                    %' OR
                    c.tax_number LIKE '%""" + search + """
                    %'
                )
            """);
        }

        if ("Bireysel".equals(typeFilter)) {
            sql.append(" AND o.owner_type = 'INDIVIDUAL' ");
        } else if ("Kurumsal".equals(typeFilter)) {
            sql.append(" AND o.owner_type = 'COMPANY' ");
        }

        sql.append("""
            GROUP BY
                o.owner_id, o.owner_type, o.created_date,
                i.first_name, i.last_name,
                c.company_name,
                a.city, a.district, a.street, a.building_no, a.door_no
            ORDER BY o.owner_id DESC
        """);

        try (Connection conn = DBHelper.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql.toString())) {

            while (rs.next()) {
                modelCustomers.addRow(new Object[]{
                        rs.getInt("owner_id"),
                        rs.getString("owner_type").equals("INDIVIDUAL") ? "Bireysel" : "Kurumsal",
                        rs.getString("customer_name"),
                        rs.getString("identifier"),
                        rs.getString("phone"),
                        rs.getString("email"),
                        rs.getString("city"),
                        rs.getString("district"),
                        rs.getString("full_address"),
                        rs.getTimestamp("created_date")
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Veri yüklenirken hata:\n" + e.getMessage());
        }
    }

    private TitledBorder createStyledBorder(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(Color.GRAY),
                title,
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Segoe UI", Font.BOLD, 14)
        );
    }
}