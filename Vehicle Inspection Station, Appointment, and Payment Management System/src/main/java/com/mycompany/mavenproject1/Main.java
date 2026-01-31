package com.mycompany.mavenproject1;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // 1. ÖNCE VERİTABANINDAN TEMA TERCİHİNİ ÇEK
        // Bu işlem arayüz çizilmeden önce yapılmalı ki renkler doğru gelsin.
        boolean savedTheme = DBHelper.loadThemeMode();
        ThemeDecorator.getInstance().setDarkMode(savedTheme);

        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
            UIManager.put("Table.font", AppConfig.FONT_NORMAL);
            UIManager.put("TableHeader.font", AppConfig.FONT_HEADER);
            UIManager.put("Label.font", AppConfig.FONT_HEADER);
        } catch (Exception e) {}

        // Splash ekranını aç, o da işi bitince MainDashboard'ı açacak.
        SwingUtilities.invokeLater(() -> SplashFrame.getInstance());
    }
}