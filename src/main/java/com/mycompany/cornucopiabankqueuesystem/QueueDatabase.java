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
import java.sql.Types;
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
    }

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
        // Older databases: add the new permission columns and email column.
        addColumnIfMissing(conn, "can_bill_payments", "ALTER TABLE users ADD COLUMN can_bill_payments INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "can_foreign_exchange", "ALTER TABLE users ADD COLUMN can_foreign_exchange INTEGER NOT NULL DEFAULT 0");
        addColumnIfMissing(conn, "email", "ALTER TABLE users ADD COLUMN email TEXT");
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
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, password);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new TellerPermissions(
                            rs.getInt("can_account_creation") == 1,
                            rs.getInt("can_cash_deposits") == 1,
                            rs.getInt("can_bill_payments") == 1,
                            rs.getInt("can_fund_transfers") == 1,
                            rs.getInt("can_cash_withdrawals") == 1,
                            rs.getInt("can_foreign_exchange") == 1);
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Login query failed", ex);
        }
        return null;
    }

    // ---------------------------------------------------------------
    // STEP 2 ADDITIONS: TELLER PROFILE & PASSWORD MANAGEMENT
    // ---------------------------------------------------------------

    /** Fetches teller full name, username, and email for account info frame */
    public static String[] getTellerProfile(String username) {
        initializeUsersTable();
        String sql = "SELECT full_name, username, email FROM users WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return null;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new String[] {
                        rs.getString("full_name"),
                        rs.getString("username"),
                        rs.getString("email") != null ? rs.getString("email") : "N/A"
                    };
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching profile for " + username, ex);
        }
        return null;
    }

    /** Verifies if the entered current password matches what is stored in DB */
    public static boolean verifyCurrentPassword(String username, String currentPassword) {
        initializeUsersTable();
        String sql = "SELECT id FROM users WHERE username = ? AND password = ?";
        Connection conn = getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username.trim());
            ps.setString(2, currentPassword);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error verifying password for " + username, ex);
            return false;
        }
    }

    /** Updates the user's password in SQLite */
    public static boolean updatePassword(String username, String newPassword) {
        initializeUsersTable();
        String sql = "UPDATE users SET password = ? WHERE username = ?";
        Connection conn = getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, newPassword);
            ps.setString(2, username.trim());
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error updating password for " + username, ex);
            return false;
        }
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

    private static final String[] NO_ID_REQUIRED = {"deposit", "transfer", "bills payment", "billspayment"};

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

    public static List<String[]> getWaitingTickets() {
        return getWaitingTickets(Integer.MAX_VALUE);
    }

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

    public static synchronized Ticket callNext(String counter) {
        return callNext(counter, TellerPermissions.allGranted());
    }

    public static synchronized Ticket callNext(String counter, TellerPermissions perms) {
        if (getActiveTicket(counter) != null) {
            return null;
        }
        Connection conn = getConnection();
        if (conn == null) return null;

        int chosenId = -1;
        String pick = "SELECT id, category FROM queue_tickets WHERE status = 'WAITING' "
                + "ORDER BY priority DESC, id ASC";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(pick)) {
            while (rs.next()) {
                if (perms != null && perms.canHandle(rs.getString("category"))) {
                    chosenId = rs.getInt("id");
                    break;
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not look up next ticket", ex);
            return null;
        }
        if (chosenId < 0) return null;

        String sql = "UPDATE queue_tickets SET status = 'SERVING', counter = ?, "
                + "transaction_type = category, updated_at = datetime('now','localtime') WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counter);
            ps.setInt(2, chosenId);
            return ps.executeUpdate() > 0 ? getActiveTicket(counter) : null;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Could not call next ticket", ex);
            return null;
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

    public static synchronized boolean createBankAccount(String accountNo, String name, String accountType, double balance, String idPath) {
        String ticketNo = "AC-" + (1000 + (int)(Math.random() * 9000));
        String sql = "INSERT INTO queue_tickets (ticket_no, category, customer_name, amount, reference_no, valid_id_submitted, status) "
                   + "VALUES (?, ?, ?, ?, ?, 1, 'DONE')";

        Connection conn = getConnection();
        if (conn == null) return false;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, ticketNo);
            ps.setString(2, accountType);
            ps.setString(3, name);
            ps.setDouble(4, balance);
            ps.setString(5, accountNo);
            return ps.executeUpdate() > 0;
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error saving bank account record", ex);
            return false;
        }
    }
   
    public static synchronized double getAccountBalance(String accountNo, String name) {
        String sql = "SELECT amount FROM queue_tickets WHERE reference_no = ? AND LOWER(customer_name) = LOWER(?) AND status = 'DONE' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return -1.0;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            ps.setString(2, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("amount");
                }
            }
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, "Error fetching account balance for " + accountNo, ex);
        }
        return -1.0;
    }

    public static synchronized boolean updateAccountBalance(String accountNo, double newBalance) {
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
        String sql = "SELECT amount FROM queue_tickets WHERE reference_no = ? AND status = 'DONE' LIMIT 1";
        Connection conn = getConnection();
        if (conn == null) return -1.0;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("amount");
            }
        } catch (SQLException ex) {}
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
        
        double runningBalance = 0.0;
        String[] details = getAccountDetails(accountNo);
        if (details != null && details[3] != null) {
            try {
                runningBalance = Double.parseDouble(details[3]);
            } catch (NumberFormatException e) { }
        }

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
                    double rowBalance = runningBalance;

                    if (ticket != null) {
                        if (ticket.startsWith("AC-")) {
                            displayType = "Account Creation";
                            cashFlow = "+" + String.format("%.2f", runningBalance);
                            runningBalance = 0.0;
                        }
                        else if (ticket.startsWith("DP-")) {
                            displayType = "Deposit";
                            cashFlow = "+" + String.format("%.2f", amt);
                            runningBalance -= amt;
                        }
                        else if (ticket.startsWith("WD-")) {
                            displayType = "Withdrawal";
                            cashFlow = "-" + String.format("%.2f", amt);
                            runningBalance += amt;
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
}