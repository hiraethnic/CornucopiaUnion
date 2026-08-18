/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author Kijetsu
 */
public final class QueueDatabase {

    private static final Logger logger = Logger.getLogger(QueueDatabase.class.getName());
    private static final String DB_URL = "jdbc:sqlite:cornucopiabank.db";

    private static Connection connection;

    private QueueDatabase() {
        // utility class - no instances
    }

    /** Returns a single shared connection, opening one if needed. */
    public static synchronized Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (ClassNotFoundException | SQLException ex) {
            logger.log(Level.SEVERE, "Could not connect to SQLite database", ex);
        }
        return connection;
    }

    /**
     * Creates the queue_tickets table if it doesn't exist yet. Safe to call
     * from every frame's constructor - CREATE TABLE IF NOT EXISTS is a no-op
     * once the table is there.
     */
    public static synchronized void initialize() {
        String sql = "CREATE TABLE IF NOT EXISTS queue_tickets ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "ticket_no TEXT NOT NULL UNIQUE,"
                + "category TEXT NOT NULL,"
                + "customer_name TEXT,"
                + "priority INTEGER NOT NULL DEFAULT 0,"
                + "status TEXT NOT NULL DEFAULT 'WAITING',"
                + "counter TEXT,"
                + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))"
                + ")";
        Connection conn = getConnection();
        if (conn == null) {
            return;
        }
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create queue_tickets table", ex);
        }
    }

    /**
     * Issues a new ticket, saves it as WAITING and returns the generated
     * ticket number (e.g. "DP-004"). Numbering is sequential per prefix and
     * persisted, so it survives app restarts.
     *
     * @param prefix short code used in the ticket number, e.g. "DP", "WD", "BP", "FX", "AC"
     * @param category human-readable label shown on the live board, e.g. "Deposit"
     * @param customerName name tied to the transaction (may be null)
     * @param priority true if this customer gets priority lane treatment
     */
    public static synchronized String addTicket(String prefix, String category,
            String customerName, boolean priority) {
        initialize();
        String ticketNo = nextTicketNumber(prefix);
        String sql = "INSERT INTO queue_tickets (ticket_no, category, customer_name, priority, status) "
                + "VALUES (?, ?, ?, ?, 'WAITING')";
        Connection conn = getConnection();
        if (conn == null) {
            return ticketNo;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            ps.setString(2, category);
            ps.setString(3, customerName);
            ps.setInt(4, priority ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not insert ticket " + ticketNo, ex);
        }
        return ticketNo;
    }

    private static String nextTicketNumber(String prefix) {
        String sql = "SELECT COUNT(*) FROM queue_tickets WHERE ticket_no LIKE ?";
        Connection conn = getConnection();
        if (conn == null) {
            return ValidationUtils.generateTicketNumber(prefix);
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, prefix + "-%");
            try (ResultSet rs = ps.executeQuery()) {
                int count = rs.next() ? rs.getInt(1) : 0;
                return String.format("%s-%03d", prefix, count + 1);
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not compute next ticket number for " + prefix, ex);
            return ValidationUtils.generateTicketNumber(prefix);
        }
    }

    /** All tickets still waiting, priority customers first, then FIFO. Each row: {ticketNo, category}. */
    public static List<String[]> getWaitingTickets() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT ticket_no, category FROM queue_tickets WHERE status = 'WAITING' "
                + "ORDER BY priority DESC, id ASC";
        Connection conn = getConnection();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("ticket_no"), rs.getString("category")});
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch waiting tickets", ex);
        }
        return list;
    }

    /** Tickets currently being served. Each row: {ticketNo, counter}. */
    public static List<String[]> getNowServing() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT ticket_no, counter FROM queue_tickets WHERE status = 'SERVING' "
                + "ORDER BY id DESC";
        Connection conn = getConnection();
        if (conn == null) {
            return list;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{rs.getString("ticket_no"), rs.getString("counter")});
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch now-serving tickets", ex);
        }
        return list;
    }

    /** Most recently-called ticket, for the "last announcement" panel. {ticketNo, counter} or null. */
    public static String[] getLastAnnounced() {
        String sql = "SELECT ticket_no, counter FROM queue_tickets WHERE status = 'SERVING' "
                + "ORDER BY id DESC LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) {
            return null;
        }
        try (Statement st = conn.createStatement();
                ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return new String[]{rs.getString("ticket_no"), rs.getString("counter")};
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch last announced ticket", ex);
        }
        return null;
    }

    /**
     * Pulls the next WAITING ticket (priority first, then FIFO) into SERVING
     * status at the given counter. Not wired to any UI yet - handy once a
     * teller-side "Call Next" screen is built.
     *
     * @return true if a ticket was called, false if the queue was empty
     */
    public static synchronized boolean callNext(String counter) {
        String sql = "UPDATE queue_tickets SET status = 'SERVING', counter = ? "
                + "WHERE id = (SELECT id FROM queue_tickets WHERE status = 'WAITING' "
                + "ORDER BY priority DESC, id ASC LIMIT 1)";
        Connection conn = getConnection();
        if (conn == null) {
            return false;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counter);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not call next ticket", ex);
            return false;
        }
    }
}
