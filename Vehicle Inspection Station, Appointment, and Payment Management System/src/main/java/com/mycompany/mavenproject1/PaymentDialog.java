package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;

public class PaymentDialog extends JDialog {

    private int inspectionId;
    private JTextField txtAmount, txtTransactionCode;
    private JComboBox<String> cmbMethod;

    public PaymentDialog(Window parent, int inspectionId, String plate, String owner) {
        super(parent, "Ödeme İşlemi", ModalityType.APPLICATION_MODAL);
        this.inspectionId = inspectionId;
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // BAŞLIK
        JLabel lblTitle = new JLabel("MUAYENE ÜCRETİ TAHSİLATI", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setBorder(new EmptyBorder(10, 0, 10, 0));
        lblTitle.setForeground(new Color(41, 128, 185));
        add(lblTitle, BorderLayout.NORTH);

        // ORTA PANEL (FORM)
        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlForm.setBorder(new EmptyBorder(10, 20, 10, 20));

        pnlForm.add(new JLabel("Plaka:"));
        pnlForm.add(new JLabel("<html><b>" + plate + "</b></html>"));

        pnlForm.add(new JLabel("Müşteri:"));
        pnlForm.add(new JLabel("<html>" + owner + "</html>"));

        pnlForm.add(new JLabel("Ödeme Tutarı (TL):"));
        txtAmount = new JTextField("1130.00"); // Varsayılan tutar
        txtAmount.setFont(new Font("Segoe UI", Font.BOLD, 14));
        pnlForm.add(txtAmount);

        pnlForm.add(new JLabel("Ödeme Yöntemi:"));
        cmbMethod = new JComboBox<>(new String[]{"NAKİT", "KREDİ KARTI", "HAVALE/EFT"});
        pnlForm.add(cmbMethod);

        pnlForm.add(new JLabel("İşlem Kodu (Opsiyonel):"));
        txtTransactionCode = new JTextField();
        pnlForm.add(txtTransactionCode);

        add(pnlForm, BorderLayout.CENTER);

        // ALT PANEL (BUTON)
        JPanel pnlBottom = new JPanel(new FlowLayout());
        JButton btnPay = new JButton("ÖDEMEYİ ONAYLA");
        btnPay.setPreferredSize(new Dimension(200, 40));
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        btnPay.addActionListener(e -> processPayment());
        
        pnlBottom.add(btnPay);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void processPayment() {
        // Validasyon
        try {
            double amount = Double.parseDouble(txtAmount.getText().trim());
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Lütfen geçerli bir tutar giriniz!", "Hata", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DBHelper.getConnection()) {
            // Ödeme No Oluşturma (Benzersiz olmalı)
            String paymentNo = "PAY-" + inspectionId + "-" + System.currentTimeMillis();
            
            String sql = DBHelper.getInsertPaymentQuery();
            
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, inspectionId);
            ps.setString(2, paymentNo);
            ps.setBigDecimal(3, new java.math.BigDecimal(txtAmount.getText().trim()));
            ps.setString(4, (String) cmbMethod.getSelectedItem());
            ps.setString(5, txtTransactionCode.getText());
            
            ps.executeUpdate();
            
            JOptionPane.showMessageDialog(this, "Ödeme başarıyla alındı!\nMakbuz No: " + paymentNo);
            dispose(); // Pencereyi kapat

        } catch (SQLException ex) {
            // Trigger'dan gelen hatayı yakala (Başarısız muayene ödemesi engeli)
            if (ex.getMessage().contains("Başarısız muayene")) {
                JOptionPane.showMessageDialog(this, "HATA: Veritabanı kuralı gereği başarısız (KALDI) muayenelerden ödeme alınamaz!\n[Trigger: prevent_payment_if_failed]", "İşlem Engellendi", JOptionPane.ERROR_MESSAGE);
            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Veritabanı Hatası: " + ex.getMessage(), "Hata", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}