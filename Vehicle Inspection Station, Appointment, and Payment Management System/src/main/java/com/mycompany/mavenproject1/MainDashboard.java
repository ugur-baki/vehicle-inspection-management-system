package com.mycompany.mavenproject1;

import javax.swing.*;
import java.awt.*;

public class MainDashboard extends JFrame {
    private static MainDashboard instance;

    public static MainDashboard getInstance() {
        if (instance == null) {
            instance = new MainDashboard();
        }
        return instance;
    }

    private MainDashboard() {
        setTitle("ProInspection - İstasyon Yönetimi v5.0");
        setSize(1200, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        tabbedPane.addTab("Muayene İşlemleri", AdvancedInspectionPanel.getInstance());
        tabbedPane.addTab("Eski Kayıtlar", OldRegistrationsPanel.getInstance());
        tabbedPane.addTab("Yeni Kayıt", DetailedRegistrationPanel.getInstance());
        tabbedPane.addTab("İstasyon Bilgileri", StationInfoPanel.getInstance());
        tabbedPane.addTab("Personel Bilgileri", PersonelInfoPanel.getInstance());
        tabbedPane.addTab("Müşteriler", CustomersInfoPanel.getInstance());
        tabbedPane.addTab("KayıtlıAraçlar", PinnedVehicles.getInstance());
        tabbedPane.addTab("Ayarlar", SettingsPanel.getInstance());
        

        add(tabbedPane);
        if (ThemeDecorator.getInstance().isDarkMode()) {
        ThemeDecorator.getInstance().applyTheme(this);
    }
    }
}