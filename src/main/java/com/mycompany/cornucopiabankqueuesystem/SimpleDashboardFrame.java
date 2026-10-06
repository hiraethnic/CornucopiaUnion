package com.mycompany.cornucopiabankqueuesystem;

import javax.swing.*;
import java.awt.*;

/**
 * Simple admin dashboard - standard Swing components only.
 * Layout: nav buttons (top), 4 stat cards, teller table, then
 * "Transactions by type" bars + "Recent activity" list.
 */
public class SimpleDashboardFrame extends JFrame {

    private static final Color BG   = new Color(24, 24, 24);
    private static final Color CARD = new Color(38, 38, 38);
    private static final Color BLUE = new Color(47, 125, 225);
    private static final Color TEXT = Color.WHITE;
    private static final Color MUTED = new Color(160, 160, 160);

    public SimpleDashboardFrame() {
        setTitle("Cornucopia Union - Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(BG);
        ((JComponent) getContentPane()).setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        add(buildNav(), BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 10));
        center.setOpaque(false);
        center.add(buildStats());
        center.add(buildTellerTable());
        center.add(buildBottom());
        add(center, BorderLayout.CENTER);

        setSize(760, 620);
        setLocationRelativeTo(null);
    }

    // ---- Top buttons ----
    private JPanel buildNav() {
        JPanel p = new JPanel(new GridLayout(1, 4, 8, 0));
        p.setOpaque(false);
        for (String name : new String[]{"Teller management", "Account management", "Teller creation", "Logs"}) {
            JButton b = new JButton(name);
            b.setBackground(CARD);
            b.setForeground(TEXT);
            b.setFocusPainted(false);
            p.add(b);
        }
        return p;
    }

    // ---- Four number cards ----
    private JPanel buildStats() {
        JPanel p = new JPanel(new GridLayout(1, 4, 10, 0));
        p.setOpaque(false);
        p.add(statCard("Cash in", "\u20B1 842,500"));
        p.add(statCard("Cash out", "\u20B1 531,000"));
        p.add(statCard("Net cash flow", "+\u20B1 311,500"));
        p.add(statCard("Transactions", "187"));
        return p;
    }

    private JPanel statCard(String title, String value) {
        JPanel card = new JPanel(new GridLayout(2, 1));
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        JLabel t = new JLabel(title);
        t.setForeground(MUTED);
        JLabel v = new JLabel(value);
        v.setForeground(TEXT);
        v.setFont(new Font("Segoe UI", Font.BOLD, 20));
        card.add(t);
        card.add(v);
        return card;
    }

    // ---- Teller table ----
    private JScrollPane buildTellerTable() {
        String[] cols = {"Teller", "Now serving", "Status", "Served", "Cash in", "Cash out"};
        Object[][] rows = {
            {"Juan Dela Cruz", "DP-4821 Deposit",   "Serving",    42, "\u20B1 310,000", "\u20B1 0"},
            {"Maria Santos",   "WD-1307 Withdraw",  "Serving",    38, "\u20B1 0",       "\u20B1 245,000"},
            {"Ana Reyes",      "No ticket",         "Idle 6 min", 29, "\u20B1 128,500", "\u20B1 86,000"},
            {"Pedro Lim",      "FX-2250 Foreign",   "Serving",    21, "\u20B1 404,000", "\u20B1 200,000"}
        };
        JTable table = new JTable(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table.setRowHeight(26);
        table.setBackground(CARD);
        table.setForeground(TEXT);
        table.setGridColor(new Color(60, 60, 60));
        table.getTableHeader().setBackground(BG);
        table.getTableHeader().setForeground(MUTED);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)), "Teller activity (live)",
                0, 0, null, TEXT));
        sp.getViewport().setBackground(CARD);
        sp.setOpaque(false);
        return sp;
    }

    // ---- Bottom row: bars + recent activity ----
    private JPanel buildBottom() {
        JPanel p = new JPanel(new GridLayout(1, 2, 10, 0));
        p.setOpaque(false);

        // Transactions by type
        JPanel bars = new JPanel(new GridLayout(6, 1, 0, 4));
        bars.setBackground(CARD);
        bars.setBorder(titled("Transactions by type"));
        addBar(bars, "Deposit", 58);
        addBar(bars, "Withdraw", 47);
        addBar(bars, "Bills payment", 35);
        addBar(bars, "Fund transfer", 22);
        addBar(bars, "Foreign exch.", 12);
        addBar(bars, "New account", 13);
        p.add(bars);

        // Recent activity
        DefaultListModel<String> model = new DefaultListModel<>();
        model.addElement("Deposit \u20B1 25,000 - Juan Dela Cruz (1m ago)");
        model.addElement("Withdraw \u20B1 150,000 - Maria Santos (3m ago)");
        model.addElement("New account opened - Ana Reyes (7m ago)");
        model.addElement("Teller logged in - Pedro Lim (12m ago)");
        JList<String> list = new JList<>(model);
        list.setBackground(CARD);
        list.setForeground(TEXT);

        JPanel recent = new JPanel(new BorderLayout(0, 6));
        recent.setBackground(CARD);
        recent.setBorder(titled("Recent activity"));
        recent.add(list, BorderLayout.CENTER);
        JButton all = new JButton("View all in logs");
        recent.add(all, BorderLayout.SOUTH);
        p.add(recent);

        return p;
    }

    private void addBar(JPanel parent, String name, int value) {
        JProgressBar bar = new JProgressBar(0, 60);
        bar.setValue(value);
        bar.setStringPainted(true);
        bar.setString(name + "  -  " + value);
        bar.setForeground(BLUE);
        bar.setBackground(new Color(55, 55, 55));
        parent.add(bar);
    }

    private javax.swing.border.Border titled(String title) {
        return BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 70)), title, 0, 0, null, TEXT);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SimpleDashboardFrame().setVisible(true));
    }
}
