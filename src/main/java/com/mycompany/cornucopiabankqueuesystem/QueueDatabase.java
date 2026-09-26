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
    public static synchronized Ticket callNext(String counter) {
        if (getActiveTicket(counter) != null) {
            return null; 
        }
        String sql = "UPDATE queue_tickets SET status = 'SERVING', counter = ?, "
                + "transaction_type = category, updated_at = datetime('now','localtime') "
                + "WHERE id = (SELECT id FROM queue_tickets WHERE status = 'WAITING' "
                + "ORDER BY priority DESC, id ASC LIMIT 1)";
        Connection conn = getConnection();
        if (conn == null) return null;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, counter);
            int updated = ps.executeUpdate();
            return updated > 0 ? getActiveTicket(counter) : null;
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

    /** Inserts a new bank account with an initial balance and uploaded ID file path */
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

 /**
  * Updates the balance of an existing bank account.
  * 
  * @param accountNo Account number stored in reference_no
  * @param newBalance Updated balance to save in amount column
  * @return true if row updated successfully
  */
 public static synchronized boolean updateAccountBalance(String accountNo, double newBalance) {
     String sql = "UPDATE queue_tickets SET amount = ?, updated_at = datetime('now','localtime') WHERE reference_no = ? AND status = 'DONE'";
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
        java.sql.Connection conn = getConnection();
        if (conn == null) return -1.0;
        
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, accountNo);
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getDouble("amount");
            }
        } catch (java.sql.SQLException ex) {}
        return -1.0; // Returns -1 if account doesn't exist
    }

    


}
