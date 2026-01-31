package com.mycompany.mavenproject1;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class SplashFrame extends JWindow {
    private static SplashFrame instance;

    public static SplashFrame getInstance() {
        if (instance == null) {
            instance = new SplashFrame();
        }
        return instance;
    }

    private SplashFrame() {
        JPanel content = (JPanel) getContentPane();
        content.setBackground(new Color(44, 62, 80));
        content.setLayout(new BorderLayout());
        ((JComponent) getContentPane()).setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));

        JLabel lblTitle = new JLabel("PRO INSPECTION SYSTEM v5.0", JLabel.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setBorder(new EmptyBorder(30, 10, 30, 10));

        JProgressBar bar = new JProgressBar();
        bar.setForeground(AppConfig.COLOR_SUCCESS); // Eğer AppConfig de hata veriyorsa onun da paketine bak
        bar.setStringPainted(true);
        bar.setString("Sistem Başlatılıyor...");

        content.add(lblTitle, BorderLayout.CENTER);
        content.add(bar, BorderLayout.SOUTH);

        setSize(450, 200);
        setLocationRelativeTo(null);
        setVisible(true);

        new Thread(() -> {
            try {
                for (int i = 0; i <= 100; i++) {
                    Thread.sleep(10);
                    bar.setValue(i);
                }
                SwingUtilities.invokeLater(() -> {
                    dispose();
                    MainDashboard.getInstance().setVisible(true); // MainDashboard'un da paketine dikkat et
                });
            } catch (Exception e) {}
        }).start();
    }
}