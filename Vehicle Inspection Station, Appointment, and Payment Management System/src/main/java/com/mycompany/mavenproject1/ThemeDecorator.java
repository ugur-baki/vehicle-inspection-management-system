package com.mycompany.mavenproject1;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;

public class ThemeDecorator {
    private static ThemeDecorator instance;
    private boolean darkMode = false;

    // Renk Tanımlamaları
    public static final Color DARK_BG = new Color(30, 30, 30);
    public static final Color DARK_FG = Color.WHITE;
    public static final Color DARK_PANEL = new Color(45, 45, 48);
    public static final Color DARK_TEXT = Color.WHITE;
    public static final Color DARK_BORDER = new Color(63, 63, 70);
    public static final Color DARK_SELECTION = new Color(38, 79, 120);

    public static final Color LIGHT_BG = Color.WHITE;
    public static final Color LIGHT_FG = Color.BLACK;
    public static final Color LIGHT_PANEL = new Color(245, 245, 250);
    public static final Color LIGHT_TEXT = Color.BLACK;
    public static final Color LIGHT_BORDER = new Color(200, 200, 200);
    public static final Color LIGHT_SELECTION = new Color(184, 207, 229);

    public static ThemeDecorator getInstance() {
        if (instance == null) instance = new ThemeDecorator();
        return instance;
    }

    private ThemeDecorator() {}

    public void setDarkMode(boolean enabled) {
        this.darkMode = enabled;
    }

    public boolean isDarkMode() { return darkMode; }

    // --- KRİTİK METOD: TÜM ARAYÜZÜ BOYAR ---
    public void applyTheme(Container container) {
        Color bg = darkMode ? DARK_PANEL : LIGHT_PANEL;
        Color text = darkMode ? DARK_TEXT : LIGHT_TEXT;
        Color border = darkMode ? DARK_BORDER : LIGHT_BORDER;
        Color inputBg = darkMode ? DARK_BG : LIGHT_BG;
        Color inputFg = darkMode ?  DARK_BG: LIGHT_BG;
        Color scrollBg = darkMode ? DARK_BG : LIGHT_BG;

        for (Component c : container.getComponents()) {
            // 1. Panelleri Boya
            if (c instanceof JPanel) {
                c.setBackground(bg);
                // Panel başlığı (TitledBorder) varsa onu da boya
                if (((JPanel) c).getBorder() instanceof TitledBorder) {
                    TitledBorder tb = (TitledBorder) ((JPanel) c).getBorder();
                    tb.setTitleColor(text);
                    tb.setBorder(BorderFactory.createLineBorder(border));
                }
            } 
            // 2. Yazıları Boya
            else if ( c instanceof JTextArea|| c instanceof JPanel || c instanceof JButton|| c instanceof JTextField || c instanceof JLabel || c instanceof JCheckBox || c instanceof JRadioButton) {
                c.setForeground(text);
                c.setBackground(scrollBg);
            }
            // 3. Tabloları Boya
            else if (c instanceof JTable) {
                JTable table = (JTable) c;
                table.setBackground(bg);
                table.setForeground(text);
                table.setSelectionBackground(darkMode ? DARK_SELECTION : LIGHT_SELECTION);
                table.setGridColor(darkMode ? DARK_BORDER : LIGHT_BORDER);
                table.setSelectionForeground(darkMode ? DARK_FG : LIGHT_FG);
                table.getTableHeader().setForeground(LIGHT_FG);
                // Header (Başlık) kısmını boya
              
            }
            // 4. Input Alanlarını Boya
            else if (c instanceof JTextField || c instanceof JTextArea || c instanceof JComboBox || c instanceof JSpinner) {
                c.setBackground(inputBg);
                c.setForeground(inputFg);
            }
            // 5. ScrollPane (Tablo çerçevesi) Boya
            else if (c instanceof JScrollPane) {
                c.setBackground(scrollBg);
                ((JScrollPane) c).getViewport().setBackground(scrollBg);
            }

            // Alt bileşenler varsa içeri gir (Recursive)
            if (c instanceof Container) {
                applyTheme((Container) c);
            }
            if (c instanceof JComboBox) {
                ((JComboBox<?>) c).setRenderer(new DefaultListCellRenderer() {
                    @Override
                    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                        Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                        c.setBackground(bg);
                        c.setForeground(text);
                        return c;
                    }
                });
            }
        }
    }
}