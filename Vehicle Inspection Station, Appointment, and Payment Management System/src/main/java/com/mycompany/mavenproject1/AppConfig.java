package com.mycompany.mavenproject1;

import java.awt.Color;
import java.awt.Font;

public class AppConfig {
    // Veritabanı
    public static final String DB_URL = "jdbc:postgresql://localhost:5432/databaseProject";
    public static final String USER = "postgres";
    public static final String PASS = "1453"; 

    // Fontlar
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_NORMAL = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_TITLE = new Font("Segoe UI", Font.BOLD, 16);

    // Renkler
    public static final Color COLOR_BG_FILTER = new Color(240, 242, 245);
    public static final Color COLOR_BTN_ACTION = new Color(52, 152, 219);
    public static final Color COLOR_DANGER = new Color(231, 76, 60);
    public static final Color COLOR_SUCCESS = new Color(46, 204, 113);
    public static final Color COLOR_WARNING = new Color(241, 196, 15);
}