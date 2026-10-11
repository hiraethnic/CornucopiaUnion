/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;
import javax.swing.JOptionPane;
/**
 *
 * @author Rara
 */
public class AdminRecord extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AdminRecord.class.getName());

    /**
     * Creates new form AdminRecord
     */
    public AdminRecord() {
        initComponents();
        QueueDatabase.initialize(); 
        setupTellerColumns();
        loadTransactionLogs();
        setLocationRelativeTo(null);
    }
    
    
    
        private void setupTellerColumns() {
        javax.swing.table.DefaultTableModel model = new javax.swing.table.DefaultTableModel(
            new Object[][]{},
            new String[]{"TICKET NO.", "DATE", "TIME", "TRANSACTION TYPE", "NAME", "ACCOUNT NO.",
                "AMOUNT", "REFERENCE NO.", "TELLER USER"}) {
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };
        AccountList.setModel(model);
    }
    
    
    
    private void loadTransactionLogs() {
    loadTransactionLogs(""); // tawagin ang may filter kung wala pang search
    }

   private void loadTransactionLogs(String searchKeyword) {
        try {
            javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) AccountList.getModel();
            model.setRowCount(0); 

            java.sql.Connection conn = QueueDatabase.getConnection();
            String query = "SELECT * FROM queue_tickets";
            
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                 query += " WHERE ticket_no LIKE ? OR customer_name LIKE ? OR reference_no LIKE ? OR sys_ref_no LIKE ? OR transaction_type LIKE ? OR counter LIKE ? OR confirmed_by LIKE ? OR released_by LIKE ?";
            }
             query += " ORDER BY id DESC";

            java.sql.PreparedStatement pst = conn.prepareStatement(query);
            
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String keyword = "%" + searchKeyword.trim() + "%";
                for (int i = 1; i <= 8; i++) {
                    pst.setString(i, keyword);
                }
            }

            java.sql.ResultSet rs = pst.executeQuery();

            while (rs.next()) {
                String createdAt = rs.getString("created_at"); 
                String datePart = createdAt != null && createdAt.contains(" ") ? createdAt.split(" ")[0] : createdAt;
                String timePart = createdAt != null && createdAt.contains(" ") ? createdAt.split(" ")[1] : "";
                
                String ticketNo = rs.getString("ticket_no");
                String transType = rs.getString("transaction_type");
                String sysRefNo = rs.getString("sys_ref_no");
                String dbRefNo = rs.getString("reference_no"); 
                
                // --- FIX 1: TELLER USER ---
                // Try confirmed_by first. If empty, grab it from the counter name.
                String tellerUser = rs.getString("confirmed_by");
                if (tellerUser == null || tellerUser.trim().isEmpty()) {
                    String counterVal = rs.getString("counter");
                    if (counterVal != null && counterVal.startsWith("Counter - ")) {
                        tellerUser = counterVal.substring(10); // Removes "Counter - " to just show "ace landry"
                    } else if (counterVal != null) {
                        tellerUser = counterVal;
                    } else {
                        tellerUser = rs.getString("released_by");
                    }
                }
                if (tellerUser == null || tellerUser.trim().isEmpty()) tellerUser = "Not recorded";

                if (transType == null || transType.isEmpty()) transType = rs.getString("category");
                if (dbRefNo == null) dbRefNo = "N/A";

                String displayAccountNo = dbRefNo; 
                
                // --- FIX 2: REFERENCE NUMBER ---
                // Force it to ONLY show the generated system reference (e.g. BP-REF-123456)
                String displayRefNo = (sysRefNo != null && !sysRefNo.trim().isEmpty()) ? sysRefNo : "N/A";

                // --- FIX 3: ACCOUNT NO & BILLS PAYMENT CATEGORY ---
                if (ticketNo != null && ticketNo.startsWith("BP-")) {
                    transType = "Bills Payment"; 
                    if (dbRefNo.contains(" / Bank Acc: ")) {
                        String[] parts = dbRefNo.split(" / Bank Acc: ");
                        displayAccountNo = parts[1]; // Shows Unified Card Account Number
                    } else {
                        displayAccountNo = "N/A (Cash)"; // Shows they paid with Cash
                    }
                } 
                else if (ticketNo != null && ticketNo.startsWith("AC-")) {
                    transType = "Account Creation";
                }

                // Format Amount
                String amountStr = rs.getString("amount");
                if (amountStr != null && !amountStr.trim().isEmpty()) {
                    try {
                        double amt = Double.parseDouble(amountStr);
                        amountStr = String.format("₱ %,.2f", amt);
                    } catch (Exception e) {}
                } else {
                    amountStr = "₱ 0.00";
                }

                model.addRow(new Object[]{
                    ticketNo,
                    datePart,
                    timePart,
                    transType,
                    rs.getString("customer_name"),
                    displayAccountNo,
                    amountStr,
                    displayRefNo, // Placed exactly in the Reference No Column
                    tellerUser    // Placed exactly in the Teller User Column
                });
            }
            
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage());
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollBar1 = new javax.swing.JScrollBar();
        ACCOUNTPANEL = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton6 = new javax.swing.JButton();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        AccountList = new javax.swing.JTable();
        jButton5 = new javax.swing.JButton();
        adminRecords = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        ACCOUNTPANEL.setBackground(new java.awt.Color(234, 243, 255));
        ACCOUNTPANEL.setRequestFocusEnabled(false);
        ACCOUNTPANEL.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField1.addActionListener(this::jTextField1ActionPerformed);
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyReleased(java.awt.event.KeyEvent evt) {
                jTextField1KeyReleased(evt);
            }
        });
        ACCOUNTPANEL.add(jTextField1, new org.netbeans.lib.awtextra.AbsoluteConstraints(130, 150, 330, 30));

        jButton6.setBackground(new java.awt.Color(30, 58, 138));
        jButton6.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("SEARCH");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        ACCOUNTPANEL.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(30, 150, -1, 30));

        jButton4.setBackground(new java.awt.Color(30, 58, 138));
        jButton4.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("REFRESH");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        ACCOUNTPANEL.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 270, 120, 40));

        jButton1.setBackground(new java.awt.Color(12, 35, 74));
        jButton1.setFont(new java.awt.Font("Lucida Bright", 0, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Logout");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        ACCOUNTPANEL.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(970, 30, 90, 40));

        AccountList.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        AccountList.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "TICKET NO.", "DATE", "TIME", "TRANSACTION TYPE", "NAME", "ACCOUNT NO.", "AMOUNT", "REFERENCE NO.", "TELLER USER"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(AccountList);

        ACCOUNTPANEL.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 190, 920, 480));

        jButton5.setBackground(new java.awt.Color(30, 58, 138));
        jButton5.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton5.setForeground(new java.awt.Color(255, 255, 255));
        jButton5.setText("BACK");
        jButton5.addActionListener(this::jButton5ActionPerformed);
        ACCOUNTPANEL.add(jButton5, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 220, 120, 40));

        adminRecords.setIcon(new javax.swing.ImageIcon("C:\\Users\\Rara\\Documents\\CornucopiaUnion\\IMAGEADMIN&TELLER\\adminrecord (1).png")); // NOI18N
        adminRecords.setText("jLabel1");
        ACCOUNTPANEL.add(adminRecords, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -20, 1100, 960));

        getContentPane().add(ACCOUNTPANEL, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 700));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        loadTransactionLogs();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton6ActionPerformed

    private void jButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton5ActionPerformed
        // TODO add your handling code here:
         Admindashboard adminnaframe = new Admindashboard();
        
        adminnaframe.setVisible(true);
        
        this.dispose();
    }//GEN-LAST:event_jButton5ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        new Loginadminteller().setVisible(true);
        this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1KeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTextField1KeyReleased
        loadTransactionLogs(jTextField1.getText());

    }//GEN-LAST:event_jTextField1KeyReleased

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed

    }//GEN-LAST:event_jTextField1ActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new AdminRecord().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel ACCOUNTPANEL;
    private javax.swing.JTable AccountList;
    private javax.swing.JLabel adminRecords;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton5;
    private javax.swing.JButton jButton6;
    private javax.swing.JScrollBar jScrollBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
