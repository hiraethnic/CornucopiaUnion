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
import java.sql.Types;
/**
 *
 * @author Kijetsu
 */
public final class QueueDatabase {

    private static final Logger logger = Logger.getLogger(QueueDatabase.class.getName());
    private static final String DB_URL = "jdbc:sqlite:cornucopiabank.db";

    private static Connection connection;

    private QueueDatabase() {
    }

    /** Returns a single shared connection, opening one if needed. */
      /** Creates the users table if it doesn't exist. Permission flags: 1 = allowed, 0 = not allowed. */
    public static synchronized void initializeUsersTable() {
        String sql = "CREATE TABLE IF NOT EXISTS users ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "full_name TEXT NOT NULL,"
                + "can_account_creation INTEGER NOT NULL DEFAULT 0,"
                + "can_cash_deposits INTEGER NOT NULL DEFAULT 0,"
                + "can_bill_payments INTEGER NOT NULL DEFAULT 0,"
                + "can_fund_transfers INTEGER NOT NULL DEFAULT 0,"
                + "can_cash_withdrawals INTEGER NOT NULL DEFAULT 0,"
                + "can_foreign_exchange INTEGER NOT NULL DEFAULT 0,"
                + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))"
                + ")";
        Connection conn = getConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create users table", ex);
        }
        // Older databases: add the new permission columns.
        addColumnIfMissing(conn, "can_bill_payments", "ALTER TABLE users ADD COLUMN can_bill_payments INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "can_foreign_exchange", "ALTER TABLE users ADD COLUMN can_foreign_exchange INTEGER NOT NULL DEFAULT 0");
        // Teller profile columns (used by Admin + Teller Management).
        addColumnIfMissing(conn, "employee_id", "ALTER TABLE users ADD COLUMN employee_id TEXT");
        addColumnIfMissing(conn, "email", "ALTER TABLE users ADD COLUMN email TEXT");
        addColumnIfMissing(conn, "contact_number", "ALTER TABLE users ADD COLUMN contact_number TEXT");
        addColumnIfMissing(conn, "status", "ALTER TABLE users ADD COLUMN status TEXT NOT NULL DEFAULT 'ACTIVE'");
        addColumnIfMissing(conn, "online", "ALTER TABLE users ADD COLUMN online INTEGER NOT NULL DEFAULT 0");
    }

    public static boolean usernameExists(String username) {
        initializeUsersTable();
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(username) = LOWER(?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking username", ex);
            return false;
        }
    }

    /** Saves a new teller with their permissions. Returns true if saved. */
    public static synchronized boolean createTeller(String username, String password,
            String fullName, TellerPermissions p) {
        initializeUsersTable();
        String sql = "INSERT INTO users (username, password, full_name, can_account_creation, "
                + "can_cash_deposits, can_bill_payments, can_fund_transfers, can_cash_withdrawals, can_foreign_exchange) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            // NOTE: password is saved as plain text. Should be replaced with a hashed password
            // (e.g. SHA-256 with a salt) in both createTeller() and authenticate().
            ps.setString(2, password);
            ps.setString(3, fullName.trim());
            ps.setInt(4, p.canAccountCreation() ? 1 : 0);
            ps.setInt(5, p.canCashDeposits() ? 1 : 0);
            ps.setInt(6, p.canBillPayments() ? 1 : 0);
            ps.setInt(7, p.canFundTransfers() ? 1 : 0);
            ps.setInt(8, p.canCashWithdrawals() ? 1 : 0);
            ps.setInt(9, p.canForeignExchange() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create teller " + username, ex);
            return false;
        }
    }

    /**
     * Checks username + password. Returns the teller's permissions,
     * or null if the login is wrong.
     */
    public static TellerPermissions authenticate(String username, String password) {
        initializeUsersTable();
        String sql = "SELECT * FROM users WHERE username = ? AND password = ? AND status = 'ACTIVE'";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    TellerPermissions perms = new TellerPermissions(
                            rs.getInt("can_account_creation") == 1,
                            rs.getInt("can_cash_deposits") == 1,
                            rs.getInt("can_bill_payments") == 1,
                            rs.getInt("can_fund_transfers") == 1,
                            rs.getInt("can_cash_withdrawals") == 1,
                            rs.getInt("can_foreign_exchange") == 1)
                            .withIdentity(rs.getString("username"), rs.getString("full_name"));
                    setTellerOnline(perms.getUsername(), true);   // shows green in Teller Management
                    return perms;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Login query failed", ex);
        }
        return null;
    }
    
    
    
    // ---------------------------------------------------------------
    // TELLER MANAGEMENT (used by the Admintellermanagement form)
    // ---------------------------------------------------------------

    /** Saves a new teller with full profile (employee ID, email, contact) and permissions. */
    public static synchronized boolean createTeller(String username, String password, String fullName,
            String employeeId, String email, String contact, TellerPermissions p) {
        initializeUsersTable();
        String sql = "INSERT INTO users (username, password, full_name, employee_id, email, contact_number, "
                + "can_account_creation, can_cash_deposits, can_bill_payments, can_fund_transfers, "
                + "can_cash_withdrawals, can_foreign_exchange) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password); // plain text, same as createTeller() above
            ps.setString(3, fullName.trim());
            ps.setString(4, employeeId.trim());
            ps.setString(5, email.trim());
            ps.setString(6, contact.trim());
            ps.setInt(7, p.canAccountCreation() ? 1 : 0);
            ps.setInt(8, p.canCashDeposits() ? 1 : 0);
            ps.setInt(9, p.canBillPayments() ? 1 : 0);
            ps.setInt(10, p.canFundTransfers() ? 1 : 0);
            ps.setInt(11, p.canCashWithdrawals() ? 1 : 0);
            ps.setInt(12, p.canForeignExchange() ? 1 : 0);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create teller " + username, ex);
            return false;
        }
    }

    /** True if another teller (not excludeUsername) already uses this employee ID. */
    public static boolean employeeIdExists(String employeeId, String excludeUsername) {
        initializeUsersTable();
        String sql = "SELECT COUNT(*) FROM users WHERE LOWER(employee_id) = LOWER(?) AND LOWER(username) <> LOWER(?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, employeeId.trim());
            ps.setString(2, excludeUsername == null ? "" : excludeUsername.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking employee ID", ex);
            return false;
        }
    }

    /** Profile of one teller: {fullName, employeeId, email, contact, status, password, online("1"/"0")}, or null. */
    public static String[] getTellerProfile(String username) {
        initializeUsersTable();
        String sql = "SELECT full_name, employee_id, email, contact_number, status, password, online FROM users WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                        rs.getString("full_name"),
                        rs.getString("employee_id") == null ? "" : rs.getString("employee_id"),
                        rs.getString("email") == null ? "" : rs.getString("email"),
                        rs.getString("contact_number") == null ? "" : rs.getString("contact_number"),
                        rs.getString("status"),
                        rs.getString("password"),
                        String.valueOf(rs.getInt("online"))};
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not load teller " + username, ex);
        }
        return null;
    }

    /** Permissions of one teller, or null if not found. */
    public static TellerPermissions getTellerPermissions(String username) {
        initializeUsersTable();
        String sql = "SELECT * FROM users WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TellerPermissions(
                            rs.getInt("can_account_creation") == 1,
                            rs.getInt("can_cash_deposits") == 1,
                            rs.getInt("can_bill_payments") == 1,
                            rs.getInt("can_fund_transfers") == 1,
                            rs.getInt("can_cash_withdrawals") == 1,
                            rs.getInt("can_foreign_exchange") == 1)
                            .withIdentity(rs.getString("username"), rs.getString("full_name"));
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not load permissions for " + username, ex);
        }
        return null;
    }

    /** Updates a teller's profile, permissions and status ("ACTIVE" / "INACTIVE"). Username never changes. */
    public static synchronized boolean updateTeller(String username, String fullName, String employeeId,
            String email, String contact, TellerPermissions p, String status) {
        String sql = "UPDATE users SET full_name = ?, employee_id = ?, email = ?, contact_number = ?, "
                + "can_account_creation = ?, can_cash_deposits = ?, can_bill_payments = ?, "
                + "can_fund_transfers = ?, can_cash_withdrawals = ?, can_foreign_exchange = ?, status = ? "
                + "WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, fullName.trim());
            ps.setString(2, employeeId.trim());
            ps.setString(3, email.trim());
            ps.setString(4, contact.trim());
            ps.setInt(5, p.canAccountCreation() ? 1 : 0);
            ps.setInt(6, p.canCashDeposits() ? 1 : 0);
            ps.setInt(7, p.canBillPayments() ? 1 : 0);
            ps.setInt(8, p.canFundTransfers() ? 1 : 0);
            ps.setInt(9, p.canCashWithdrawals() ? 1 : 0);
            ps.setInt(10, p.canForeignExchange() ? 1 : 0);
            ps.setString(11, status);
            ps.setString(12, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not update teller " + username, ex);
            return false;
        }
    }

    /** Marks a teller online (true) or offline (false). */
    public static synchronized boolean setTellerOnline(String username, boolean online) {
        String sql = "UPDATE users SET online = ? WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, online ? 1 : 0);
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not update online status for " + username, ex);
            return false;
        }
    }

    /** Permanently removes a teller account. Past transaction records keep the teller's name. */
    /** Permanently removes a teller account. Past transaction records keep the teller's name. */
    public static synchronized boolean deleteTeller(String username) {
    String sql = "DELETE FROM users WHERE username = ?";
    Connection conn = getConnection();
    if (conn == null) return false;
    try (PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, username);
        return ps.executeUpdate() > 0;
    } catch (SQLException ex) {
        logger.log(Level.SEVERE, "Could not delete teller " + username, ex);
        return false;
    }
}

    public static synchronized boolean updateTellerPassword(String username, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword); // plain text, same as createTeller()
            ps.setString(2, username);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not update password for " + username, ex);
            return false;
        }
    }

    /**
     * Records handled by one teller (newest first, max 50), including account
     * creations. Each row: {time, ticketNo, transactionType, status, handledBy}.
     * Matches on the "(username)" part of confirmed_by so old records still
     * show up even if the teller's full name is changed later.
     */
    public static List<String[]> getTellerActivity(String username) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT COALESCE(updated_at, created_at) AS t, ticket_no, category, transaction_type, "
                + "status, confirmed_by FROM queue_tickets WHERE confirmed_by LIKE ? ESCAPE '\\' "
                + "ORDER BY id DESC LIMIT 50";
        Connection conn = getConnection();
        if (conn == null) return list;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            String safe = username.replace("\\", "\\\\").replace("_", "\\_").replace("%", "\\%");
            ps.setString(1, "%(" + safe + ")");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticket = rs.getString("ticket_no");
                    String type = rs.getString("transaction_type");
                    if (type == null || type.isEmpty()) type = rs.getString("category");
                    if (ticket != null && ticket.startsWith("AC-")) {
                        type = "Account Creation - " + rs.getString("category");
                    }
                    list.add(new String[]{rs.getString("t"), ticket, type,
                        rs.getString("status"), rs.getString("confirmed_by")});
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not load activity for " + username, ex);
        }
        return list;
    }

    // ---------------------------------------------------------------
    // ADMINS (used by the creationAdmin form)
    // ---------------------------------------------------------------

    /** Creates the admins table if it doesn't exist. */
    public static synchronized void initializeAdminsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS admins ("
                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "admin_id TEXT NOT NULL UNIQUE,"
                + "full_name TEXT NOT NULL,"
                + "email TEXT NOT NULL,"
                + "contact_number TEXT NOT NULL,"
                + "username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))"
                + ")";
        Connection conn = getConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create admins table", ex);
        }
    }

    /** True if an admin already has this Admin ID or username. */
    public static boolean adminExists(String adminId, String username) {
        initializeAdminsTable();
        String sql = "SELECT COUNT(*) FROM admins WHERE LOWER(admin_id) = LOWER(?) OR LOWER(username) = LOWER(?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminId.trim());
            ps.setString(2, username.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking admin", ex);
            return false;
        }
    }

    /** Saves a new admin. Returns true if saved. */
    public static synchronized boolean createAdmin(String adminId, String fullName, String email,
            String contactNumber, String username, String password) {
        initializeAdminsTable();
        String sql = "INSERT INTO admins (admin_id, full_name, email, contact_number, username, password) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, adminId.trim());
            ps.setString(2, fullName.trim());
            ps.setString(3, email.trim());
            ps.setString(4, contactNumber.trim());
            ps.setString(5, username.trim());
            // NOTE: password is saved as plain text. Should be replaced with a hashed password.
            ps.setString(6, password);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create admin " + username, ex);
            return false;
        }
    }
    
    public static boolean authenticateAdmin(String username, String password) {
        initializeAdminsTable();
        String sql = "SELECT * FROM admins WHERE username = ? AND password = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Admin login query failed", ex);
            return false;
        }
    }
   
    /** All saved teller accounts. Each row: {fullName, username, permissions, createdAt}. */
    public static List<String[]> getAllTellers() {
        initializeUsersTable();
        List<String[]> list = new ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) return list;
        String sql = "SELECT * FROM users ORDER BY id DESC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                List<String> perms = new ArrayList<>();
                if (rs.getInt("can_account_creation") == 1) perms.add("Account Creation");
                if (rs.getInt("can_cash_deposits") == 1) perms.add("Deposit");
                if (rs.getInt("can_bill_payments") == 1) perms.add("Bills Payment");
                if (rs.getInt("can_fund_transfers") == 1) perms.add("Transfer");
                if (rs.getInt("can_cash_withdrawals") == 1) perms.add("Withdrawal");
                if (rs.getInt("can_foreign_exchange") == 1) perms.add("Foreign Exchange");
                list.add(new String[]{rs.getString("full_name"), rs.getString("username"),
                    String.join(", ", perms), rs.getString("created_at")});
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not load teller accounts", ex);
        }
        return list;
    }

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
    
    public static final class Ticket {
        public final int id;
        public final String ticketNo;
        public final String category;
        public final String customerName;
        public final boolean priority;
        public final String status;
        public final String counter;
        public final String createdAt;
        public final String transactionType;
        public final boolean validIdSubmitted;
        public final Double amount;
        public final String referenceNo;

        Ticket(int id, String ticketNo, String category, String customerName, boolean priority,
                String status, String counter, String createdAt, String transactionType,
                boolean validIdSubmitted, Double amount, String referenceNo) {
            this.id = id;
            this.ticketNo = ticketNo;
            this.category = category;
            this.customerName = customerName;
            this.priority = priority;
            this.status = status;
            this.counter = counter;
            this.createdAt = createdAt;
            this.transactionType = transactionType;
            this.validIdSubmitted = validIdSubmitted;
            this.amount = amount;
            this.referenceNo = referenceNo;
        }
    }

    /** Transaction categories that do NOT require a valid ID before confirming. */
    private static final String[] NO_ID_REQUIRED = {"deposit", "transfer", "bills payment", "billspayment"};

    /** True if the given category/transaction type requires a valid ID before it can be confirmed. */
    public static boolean requiresValidId(String categoryOrType) {
        if (categoryOrType == null) {
            return true;
        }
        String normalized = categoryOrType.trim().toLowerCase();
        for (String exempt : NO_ID_REQUIRED) {
            if (normalized.contains(exempt)) {
                return false;
            }
        }
        return true;
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
         migrateSchema(conn);
    }
    
    private static void migrateSchema(Connection conn) {
        addColumnIfMissing(conn, "transaction_type", "ALTER TABLE queue_tickets ADD COLUMN transaction_type TEXT");
        addColumnIfMissing(conn, "valid_id_submitted", "ALTER TABLE queue_tickets ADD COLUMN valid_id_submitted INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "amount", "ALTER TABLE queue_tickets ADD COLUMN amount REAL");
        addColumnIfMissing(conn, "reference_no", "ALTER TABLE queue_tickets ADD COLUMN reference_no TEXT");
        addColumnIfMissing(conn, "updated_at", "ALTER TABLE queue_tickets ADD COLUMN updated_at TEXT");
        addColumnIfMissing(conn, "confirmed_by", "ALTER TABLE queue_tickets ADD COLUMN confirmed_by TEXT");
        addColumnIfMissing(conn, "released_by", "ALTER TABLE queue_tickets ADD COLUMN released_by TEXT");
        addColumnIfMissing(conn, "id_document_path", "ALTER TABLE queue_tickets ADD COLUMN id_document_path TEXT");
        addColumnIfMissing(conn, "id_blob", "ALTER TABLE queue_tickets ADD COLUMN id_blob BLOB"); 
    
    }

    private static void addColumnIfMissing(Connection conn, String columnName, String alterSql) {
        try (Statement st = conn.createStatement()) {
            st.execute(alterSql);
        } catch (SQLException ex) {
            String msg = ex.getMessage();
            if (msg == null || !msg.toLowerCase().contains("duplicate column")) {
                logger.log(Level.WARNING, "Could not add column " + columnName, ex);
            }
        }
    }

    private static Ticket mapRow(ResultSet rs) throws SQLException {
        return new Ticket(
                rs.getInt("id"),
                rs.getString("ticket_no"),
                rs.getString("category"),
                rs.getString("customer_name"),
                rs.getInt("priority") == 1,
                rs.getString("status"),
                rs.getString("counter"),
                rs.getString("created_at"),
                rs.getString("transaction_type"),
                rs.getInt("valid_id_submitted") == 1,
                rs.getObject("amount") == null ? null : rs.getDouble("amount"),
                rs.getString("reference_no")
        );
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
        return getWaitingTickets(Integer.MAX_VALUE);
    }

    /** Same as getWaitingTickets() but capped to the given number of rows (e.g. the next 5 in line). */
    public static List<String[]> getWaitingTickets(int limit) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT ticket_no, category FROM queue_tickets WHERE status = 'WAITING' "
                + "ORDER BY priority DESC, id ASC LIMIT ?";
        Connection conn = getConnection();
        if (conn == null) {
            return list;
        }
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{rs.getString("ticket_no"), rs.getString("category")});
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch waiting tickets", ex);
        }
        return list;
    }

    /** Tickets currently being served. Each row: {ticketNo, counter}. */
    /** Tickets currently called or ongoing (SERVING or HELD). Each row: {ticketNo, counter}. */
        public static List<String[]> getNowServing() {
            List<String[]> list = new ArrayList<>();
            String sql = "SELECT ticket_no, counter FROM queue_tickets WHERE status IN ('SERVING','HELD') "
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
            String sql = "SELECT ticket_no, counter FROM queue_tickets WHERE status IN ('SERVING','HELD') "
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
    public static Ticket getActiveTicket(String counter) {
        String sql = "SELECT * FROM queue_tickets WHERE status IN ('SERVING','HELD') AND counter = ? "
                + "ORDER BY id DESC LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counter);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch active ticket for counter " + counter, ex);
            return null;
        }
    }

    public static Ticket getTicketByNumber(String ticketNo) {
        String sql = "SELECT * FROM queue_tickets WHERE ticket_no = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? mapRow(rs) : null;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not fetch ticket " + ticketNo, ex);
            return null;
        }
    }

    public static synchronized boolean holdTicket(String ticketNo) {
        String sql = "UPDATE queue_tickets SET status = 'HELD', updated_at = datetime('now','localtime') "
                + "WHERE ticket_no = ? AND status = 'SERVING'";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not hold ticket " + ticketNo, ex);
            return false;
        }
    }

    public static synchronized boolean cancelTicket(String ticketNo) {
        String sql = "UPDATE queue_tickets SET status = 'CANCELLED', updated_at = datetime('now','localtime') "
                + "WHERE ticket_no = ? AND status IN ('SERVING','HELD')";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not cancel ticket " + ticketNo, ex);
            return false;
        }
    }
    
    /** Updates a ticket's core details when a teller edits them */
    public static synchronized boolean updateTicketDetails(String ticketNo, String customerName, String referenceNo, Double amount) {
        String sql = "UPDATE queue_tickets SET customer_name = ?, reference_no = ?, amount = ?, updated_at = datetime('now','localtime') WHERE ticket_no = ?";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, customerName);
            ps.setString(2, referenceNo);
            if (amount == null) {
                ps.setNull(3, java.sql.Types.REAL);
            } else {
                ps.setDouble(3, amount);
            }
            ps.setString(4, ticketNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not update ticket details for " + ticketNo, ex);
            return false;
        }
    }

    public static synchronized boolean confirmTransaction(String ticketNo, String transactionType,
            boolean validIdSubmitted, Double amount, String referenceNo) {
        String sql = "UPDATE queue_tickets SET status = 'DONE', transaction_type = ?, "
                + "valid_id_submitted = ?, amount = ?, reference_no = ?, "
                + "updated_at = datetime('now','localtime') "
                + "WHERE ticket_no = ? AND status = 'HELD'";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, transactionType);
            ps.setInt(2, validIdSubmitted ? 1 : 0);
            if (amount == null) {
                ps.setNull(3, Types.REAL);
            } else {
                ps.setDouble(3, amount);
            }
            ps.setString(4, referenceNo);
            ps.setString(5, ticketNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not confirm transaction for ticket " + ticketNo, ex);
            return false;
        }
    }

    /**
     * Pulls the next WAITING ticket (priority first, then FIFO) into SERVING
     * status at the given counter. Not wired to any UI yet - handy once a
     * teller-side "Call Next" screen is built.
     *
     * @return true if a ticket was called, false if the queue was empty
     */
    /** Calls the next waiting ticket of ANY type (no permission limit). */
    public static synchronized Ticket callNext(String counter) {
        return callNext(counter, TellerPermissions.allGranted());
    }

    /**
     * Calls the next waiting ticket that this teller is allowed to serve.
     * Tickets for functions the teller doesn't have are skipped and stay waiting
     * for another teller.
     */
    public static synchronized Ticket callNext(String counter, TellerPermissions perms) {
        if (getActiveTicket(counter) != null) {
            return null;
        }
        Connection conn = getConnection();
        if (conn == null) return null;

        // Loop to try picking a ticket until successful or the queue is empty
        while (true) {
            int chosenId = -1;
            String pick = "SELECT id, ticket_no, category FROM queue_tickets WHERE status = 'WAITING' "
                    + "ORDER BY priority DESC, id ASC";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(pick)) {
                while (rs.next()) {
                    if (perms != null && perms.canHandleTicket(rs.getString("ticket_no"), rs.getString("category"))) {
                        chosenId = rs.getInt("id");
                        break;
                    }
                }
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Could not look up next ticket", ex);
                return null;
            }
            
            if (chosenId < 0) return null; // No available tickets for this teller

            // Added AND status = 'WAITING' to ensure it hasn't been taken by another teller
            String sql = "UPDATE queue_tickets SET status = 'SERVING', counter = ?, "
                    + "transaction_type = category, updated_at = datetime('now','localtime') "
                    + "WHERE id = ? AND status = 'WAITING'";
                    
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, counter);
                ps.setInt(2, chosenId);
                
                // If it successfully updates 1 row, we secured the ticket!
                if (ps.executeUpdate() > 0) {
                    return getActiveTicket(counter);
                }
                
                // If 0 rows updated, someone else took it first. 
                // The loop restarts automatically to fetch the next person in line.
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, "Could not call next ticket", ex);
                return null;
            }
        }
    }

    public static synchronized void saveKioskData(String ticketNo, String referenceNo, double amount) {
        String sql = "UPDATE queue_tickets SET reference_no = ?, amount = ? WHERE ticket_no = ?";
        Connection conn = getConnection();
        if (conn == null) return;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, referenceNo);
            ps.setDouble(2, amount);
            ps.setString(3, ticketNo);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not save kiosk details for " + ticketNo, ex);
        }
}    

/** Creates the accounts table if it doesn't exist */
    public static synchronized void initializeAccountsTable() {
        String sql = "CREATE TABLE IF NOT EXISTS bank_accounts ("
                + "account_number TEXT PRIMARY KEY,"
                + "account_name TEXT NOT NULL,"
                + "account_type TEXT NOT NULL,"
                + "balance REAL NOT NULL DEFAULT 0.0,"
                + "id_document_path TEXT,"
                + "created_at TEXT NOT NULL DEFAULT (datetime('now','localtime'))"
                + ")";
        Connection conn = getConnection();
        if (conn == null) return;
        try (Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not create bank_accounts table", ex);
        }
    }


    public static boolean doesAccountExist(String accountName) {
        initializeAccountsTable();
        String sql = "SELECT COUNT(*) FROM bank_accounts WHERE LOWER(account_name) = LOWER(?)";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountName.trim());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error checking account existence", ex);
            return false;
        }
    }

  

   /** Same as above, but also records which teller account confirmed it. */
   public static synchronized boolean createBankAccount(String accountNo, String name, String accountType, double balance, String idPath, String confirmedBy) {
        initialize(); 

        String ticketNo = "AC-" + (1000 + (int)(Math.random() * 9000));
        
        String sql = "INSERT INTO queue_tickets (ticket_no, category, customer_name, amount, reference_no, valid_id_submitted, status, confirmed_by, id_document_path, id_blob) "
                   + "VALUES (?, ?, ?, ?, ?, 1, 'DONE', ?, ?, ?)";

        Connection conn = getConnection();
        if (conn == null) return false;

        // --- SAFELY CONVERT THE IMAGE FILE INTO A BLOB ---
        byte[] fileBytes = null;
        if (idPath != null && !idPath.trim().isEmpty()) {
            java.io.File imgFile = new java.io.File(idPath);
            
            // This safely checks if the file exists before attempting to read it, preventing the Windows Error
            if (imgFile.exists() && imgFile.isFile()) { 
                try {
                    fileBytes = java.nio.file.Files.readAllBytes(imgFile.toPath());
                } catch (Exception e) {
                    System.out.println("Notice: Could not convert ID to BLOB - " + e.getMessage());
                }
            }
        }

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            ps.setString(2, accountType);
            ps.setString(3, name);
            ps.setDouble(4, balance);
            ps.setString(5, accountNo);
            ps.setString(6, confirmedBy);
            ps.setString(7, idPath);
            ps.setBytes(8, fileBytes); // Saves the BLOB bytes into the database
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            return false;
        }
    }

    /** Copies the uploaded ID photo into an "id_uploads" folder (named after the account number) so it stays available. */
    private static String copyIdPhoto(String idPath, String accountNo) {
        if (idPath == null || idPath.isEmpty()) return null;
        try {
            java.nio.file.Path src = java.nio.file.Paths.get(idPath);
            String fileName = src.getFileName().toString();
            String ext = fileName.contains(".") ? fileName.substring(fileName.lastIndexOf('.')) : "";
            java.nio.file.Path folder = java.nio.file.Paths.get("id_uploads");
            java.nio.file.Files.createDirectories(folder);
            java.nio.file.Path dest = folder.resolve(accountNo + ext);
            java.nio.file.Files.copy(src, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            return dest.toAbsolutePath().toString();
        } catch (Exception ex) {
            logger.log(Level.WARNING, "Could not copy ID photo, keeping original path", ex);
            return idPath;
        }
    }

    /** Path of the ID photo uploaded when this account was created, or null if none. */
    public static String getAccountIdPath(String accountNo) {
        initialize();
        String sql = "SELECT id_document_path FROM queue_tickets WHERE reference_no = ? AND ticket_no LIKE 'AC-%' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("id_document_path");
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching ID photo path", ex);
        }
        return null;
    }
    
    public static byte[] getAccountIdBlob(String accountNo) {
        String sql = "SELECT id_blob FROM queue_tickets WHERE reference_no = ? AND ticket_no LIKE 'AC-%' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBytes("id_blob");
            }
        } catch (SQLException ex) { }
        return null;
    }
   
   public static synchronized double getAccountBalance(String accountNo, String name) {
        // Updated to target the main AC- record and check the lock status
        String sql = "SELECT amount, status FROM queue_tickets WHERE reference_no = ? AND LOWER(customer_name) = LOWER(?) AND ticket_no LIKE 'AC-%' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return -1.0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if ("LOCKED".equals(rs.getString("status"))) return -2.0; // -2.0 means locked
                    return rs.getDouble("amount");
                }
            }
        } catch (SQLException ex) { }
        return -1.0; // -1.0 means not found
    }
 /**
  * Updates the balance of an existing bank account.
  * 
  * @param accountNo Account number stored in reference_no
  * @param newBalance Updated balance to save in amount column
  * @return true if row updated successfully
  */
 public static synchronized boolean updateAccountBalance(String accountNo, double newBalance) {
        // FIX: Added "AND ticket_no LIKE 'AC-%'" so it only updates the main account balance, 
        // leaving your older transaction history amounts completely untouched.
        String sql = "UPDATE queue_tickets SET amount = ?, updated_at = datetime('now','localtime') "
                   + "WHERE reference_no = ? AND ticket_no LIKE 'AC-%'";
                   
        Connection conn = getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, newBalance);
            ps.setString(2, accountNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating balance for account " + accountNo, ex);
            return false;
        }
    }
 
 public static void saveKioskData(String ticket, String sourceAccount, String destinationAccount, long amount) {
        String sql = "UPDATE queue_tickets SET reference_no = ?, amount = ? WHERE ticket_no = ?";

        Connection conn = getConnection();
        if (conn == null) return;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, sourceAccount + "," + destinationAccount);
            pstmt.setDouble(2, amount);
            pstmt.setString(3, ticket);
            pstmt.executeUpdate();
            
        } catch (SQLException e) {
            System.out.println("Error saving transfer data: " + e.getMessage());
        }
    }

    public static String getDestinationAccount(String ticket) {
        String sql = "SELECT reference_no FROM queue_tickets WHERE ticket_no = ?";
        Connection conn = getConnection();
        if (conn == null) return "";

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, ticket);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    String ref = rs.getString("reference_no");
                    return ref != null ? ref : "";
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving destination account: " + e.getMessage());
        }
        return "";
    }
    
    public static synchronized double getBalanceByNumber(String accountNo) {
        String sql = "SELECT amount, status FROM queue_tickets WHERE reference_no = ? AND ticket_no LIKE 'AC-%' LIMIT 1";
        java.sql.Connection conn = getConnection();
        if (conn == null) return -1.0;
        
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    if ("LOCKED".equals(rs.getString("status"))) return -2.0; 
                    return rs.getDouble("amount");
                }
            }
        } catch (java.sql.SQLException ex) {}
        return -1.0; 
    }
    
    public static String[] getAccountDetails(String searchTerm) {
        String sql = "SELECT reference_no, customer_name, category, amount, status FROM queue_tickets "
                   + "WHERE (reference_no = ? OR LOWER(customer_name) = LOWER(?)) AND ticket_no LIKE 'AC-%' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, searchTerm.trim());
            ps.setString(2, searchTerm.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("reference_no"),
                        rs.getString("customer_name"),
                        rs.getString("category"),
                        String.valueOf(rs.getDouble("amount")),
                        rs.getString("status")
                    };
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching account details", ex);
        }
        return null;
    }

    public static List<String[]> getAccountHistory(String accountNo) {
        List<String[]> history = new ArrayList<>();
        
        // 1. Grab the EXACT balance used by the Account Details panel so they never mismatch
        double runningBalance = 0.0;
        String[] details = getAccountDetails(accountNo);
        if (details != null && details[3] != null) {
            try {
                runningBalance = Double.parseDouble(details[3]);
            } catch (NumberFormatException e) { }
        }

        // 2. Read newest to oldest (ORDER BY id DESC) to calculate backward from the current balance
        String sql = "SELECT created_at, ticket_no, amount, reference_no FROM queue_tickets "
                   + "WHERE reference_no LIKE ? AND status IN ('DONE', 'LOCKED') ORDER BY id DESC";
        
        Connection conn = getConnection();
        if (conn == null) return history;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + accountNo + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String ticket = rs.getString("ticket_no");
                    String refNo = rs.getString("reference_no");
                    double amt = rs.getDouble("amount");
                    
                    String displayType = "Transaction";
                    String cashFlow = "0.00";
                    double rowBalance = runningBalance; // The exact balance after this transaction

                    if (ticket != null) {
                        if (ticket.startsWith("AC-")) {
                            displayType = "Account Creation";
                            cashFlow = "+" + String.format("%.2f", runningBalance);
                            runningBalance = 0.0;
                        }
                        else if (ticket.startsWith("DP-")) {
                            displayType = "Deposit";
                            cashFlow = "+" + String.format("%.2f", amt);
                            runningBalance -= amt; // Undo deposit to find previous balance
                        }
                        else if (ticket.startsWith("WD-")) {
                            displayType = "Withdrawal";
                            cashFlow = "-" + String.format("%.2f", amt);
                            runningBalance += amt; // Undo withdrawal to find previous balance
                        }
                        else if (ticket.startsWith("TR-")) {
                            displayType = "Transfer Funds";
                            if (refNo != null && refNo.startsWith(accountNo + ",")) {
                                cashFlow = "-" + String.format("%.2f", amt);
                                runningBalance += amt; 
                            } else { 
                                cashFlow = "+" + String.format("%.2f", amt);
                                runningBalance -= amt; 
                            }
                        }
                        else if (ticket.startsWith("BP-")) {
                            displayType = "Bills Payment";
                            cashFlow = "-" + String.format("%.2f", amt);
                            runningBalance += amt; 
                        }
                    }
                    
                    // Since we queried DESC, adding normally puts the newest transaction at the top
                    history.add(new String[] { 
                        rs.getString("created_at"),
                        displayType,
                        cashFlow,
                        String.format("%.2f", rowBalance),
                        ticket
                    });
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching account history", ex);
        }
        return history;
    }

    public static boolean updateAccountName(String accountNo, String newName) {
        String sql = "UPDATE queue_tickets SET customer_name = ? WHERE reference_no = ? AND ticket_no LIKE 'AC-%'";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newName);
            ps.setString(2, accountNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating account name", ex);
            return false;
        }
    }

    public static boolean toggleAccountLock(String accountNo, boolean lock) {
        String newStatus = lock ? "LOCKED" : "DONE";
        String sql = "UPDATE queue_tickets SET status = ? WHERE reference_no = ? AND ticket_no LIKE 'AC-%'";
        Connection conn = getConnection();
        if (conn == null) return false;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newStatus);
            ps.setString(2, accountNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error toggling account lock", ex);
            return false;
        }
    }
    
    // --- ADMIN DASHBOARD FUNCTIONS ---

    public static double[] getTodayCashFlow() {
        double cashIn = 0.0, cashOut = 0.0;
        String sql = "SELECT category, amount FROM queue_tickets WHERE status = 'DONE' AND date(updated_at) = date('now', 'localtime')";
        Connection conn = getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    String cat = rs.getString("category").toLowerCase();
                    double amt = rs.getDouble("amount");
                    if (cat.contains("deposit") || cat.contains("bill") || cat.contains("account")) {
                        cashIn += amt;
                    } else if (cat.contains("withdraw")) {
                        cashOut += amt;
                    }
                }
            } catch (SQLException ex) { }
        }
        return new double[]{cashIn, cashOut, cashIn - cashOut};
    }

    public static int[] getTodayTransactionCounts() {
        int done = 0, pending = 0;
        String sql = "SELECT status FROM queue_tickets WHERE date(created_at) = date('now', 'localtime') OR date(updated_at) = date('now', 'localtime')";
        Connection conn = getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    if ("DONE".equals(rs.getString("status"))) done++;
                    else pending++;
                }
            } catch (SQLException ex) { }
        }
        return new int[]{done, pending};
    }

    public static java.util.List<Object[]> getTellerDashboardActivity() {
        java.util.List<Object[]> rows = new java.util.ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) return rows;

        // Fetch all users
        String userSql = "SELECT username, online FROM users";
        try (Statement userSt = conn.createStatement(); ResultSet userRs = userSt.executeQuery(userSql)) {
            while (userRs.next()) {
                String username = userRs.getString("username");
                boolean isOnline = userRs.getInt("online") == 1;

                // Current serving
                String currentServing = "None";
                String activeSql = "SELECT ticket_no FROM queue_tickets WHERE status IN ('SERVING', 'HELD') AND counter LIKE '%" + username + "%' LIMIT 1";
                try(Statement actSt = conn.createStatement(); ResultSet actRs = actSt.executeQuery(activeSql)){
                    if(actRs.next()) currentServing = actRs.getString("ticket_no");
                }

                // Stats today (FIXED: Uses 'counter' to correctly track who did the transaction)
                int served = 0;
                double cashIn = 0, cashOut = 0;
                String statSql = "SELECT category, amount FROM queue_tickets WHERE status = 'DONE' AND counter LIKE '%" + username + "%' AND date(updated_at) = date('now', 'localtime')";
                try(Statement statSt = conn.createStatement(); ResultSet statRs = statSt.executeQuery(statSql)){
                    while(statRs.next()) {
                        served++;
                        String cat = statRs.getString("category").toLowerCase();
                        double amt = statRs.getDouble("amount");
                        if (cat.contains("deposit") || cat.contains("bill") || cat.contains("account")) cashIn += amt;
                        else if (cat.contains("withdraw")) cashOut += amt;
                    }
                }
                
                String status = !isOnline ? "Offline" : (currentServing.equals("None") ? "Idle" : "Serving");
                
                // FIXED: Removed complex formatting so it shows as normal numbers without the "..."
                rows.add(new Object[]{
                    username, currentServing, status, served, 
                    cashIn, cashOut
                });
            }
        } catch (SQLException ex) { }
        return rows;
    }

    public static java.util.List<String> getDashboardAlerts() {
        java.util.List<String> alerts = new java.util.ArrayList<>();
        Connection conn = getConnection();
        if (conn == null) return alerts;
        
        try (Statement st = conn.createStatement()) {
            // Large withdrawals
            ResultSet rs1 = st.executeQuery("SELECT ticket_no, amount FROM queue_tickets WHERE status = 'DONE' AND category LIKE '%withdraw%' AND amount >= 50000 AND date(updated_at) = date('now', 'localtime')");
            while(rs1.next()) alerts.add("⚠️ Large Withdrawal: " + rs1.getString("ticket_no") + " (₱ " + rs1.getDouble("amount") + ")");
            
            // Long waits
            ResultSet rs2 = st.executeQuery("SELECT ticket_no FROM queue_tickets WHERE status = 'WAITING' AND (julianday('now', 'localtime') - julianday(created_at)) * 24 * 60 > 30");
            while(rs2.next()) alerts.add("⚠️ Long Wait: Ticket " + rs2.getString("ticket_no") + " waiting > 30 mins");
            
            // Idle tellers
            ResultSet rs3 = st.executeQuery("SELECT username FROM users WHERE status = 'ACTIVE' AND online = 1 AND username NOT IN (SELECT counter FROM queue_tickets WHERE status IN ('SERVING', 'HELD') AND counter IS NOT NULL)");
            while(rs3.next()) alerts.add("⚠️ Idle Teller: " + rs3.getString("username") + " has no active ticket");
        } catch (SQLException ex) { }
        return alerts;
    }

    public static java.util.List<String> getRecentActivity() {
        java.util.List<String> list = new java.util.ArrayList<>();
        String sql = "SELECT category, confirmed_by, updated_at FROM queue_tickets WHERE status = 'DONE' ORDER BY updated_at DESC LIMIT 4";
        Connection conn = getConnection();
        if (conn != null) {
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(rs.getString("category") + " by " + rs.getString("confirmed_by") + " at " + rs.getString("updated_at").substring(11));
                }
            } catch (SQLException ex) { }
        }
        return list;
    }

    


}