/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;

import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
/**
 *
 * @author Lenovo
 */
public class Adminacciuntmanagement extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Adminacciuntmanagement.class.getName());

    /**
     * Creates new form Adminacciuntmanagement
     */
    public Adminacciuntmanagement() {
        initComponents();
        setLocationRelativeTo(null);
        
        jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar()) || jTextField1.getText().length() >= 11) {
                    evt.consume(); // Ignores the keystroke
                }
            }
        });
        
        AccNo.setEditable(false);
        AccType.setEditable(false);
        BALance.setEditable(false);
        AccName.setEditable(false);
        
        jButton2.addActionListener(this::searchAccount);
        LOCK.addActionListener(this::editName);
        
           // ID photo panel: starts empty, click the photo to open it bigger
        uploadphoto.setIcon(null);
        uploadphoto.setText("No account selected");
        uploadphoto.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        uploadphoto.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                openIdPhoto();
            }
        });
        
        
        
        
        
        
        
        
        
        
        
        
        
        
        // --- ADD THIS TO AUTO-REFRESH THE TABLE & BALANCE EVERY 3 SECONDS ---
        new javax.swing.Timer(3000, e -> {
            String currentAcc = AccNo.getText().trim();
            if (!currentAcc.isEmpty()) {
                loadHistory(currentAcc); // Pulls new transaction rows automatically
                
                // Updates the live balance text automatically
                double currentBal = QueueDatabase.getBalanceByNumber(currentAcc);
                if (currentBal >= 0) {
                    BALance.setText("PHP " + String.format("%.2f", currentBal));
                }
            }
        }).start();
        
    }
    
    private void searchAccount(java.awt.event.ActionEvent evt) {
        String searchTerm = jTextField1.getText().trim();
        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter an account number or name to search.");
            return;
        }

        String[] details = QueueDatabase.getAccountDetails(searchTerm);
        
        if (details == null) {
            JOptionPane.showMessageDialog(this, "Account not found in the database.");
            return;
        }

        AccNo.setText(details[0]);
        AccName.setText(details[1]);
        AccType.setText(details[2]);
        BALance.setText("PHP " + details[3]);
        
        if ("LOCKED".equals(details[4])) {
            jButton3.setText("UNLOCK");
        } else {
            jButton3.setText("LOCK");
        }

        AccName.setEditable(false);
        LOCK.setText("EDIT");
        loadHistory(details[0]);
        showIdPhoto(details[0]);

        loadHistory(details[0]);
    }
    
   // Stores the BLOB binary data while the account is open
    private byte[] currentIdBlob = null;
 
    private void showIdPhoto(String accNo) {
        currentIdBlob = null;
        uploadphoto.setIcon(null);
        uploadphoto.setText("Loading ID...");
 
        // 1. Fetch the Binary Large Object (BLOB) from the database
        byte[] blobData = QueueDatabase.getAccountIdBlob(accNo);
        
        if (blobData == null || blobData.length == 0) {
            uploadphoto.setText("No ID uploaded");
            return;
        }
        
        currentIdBlob = blobData;
 
        // 2. Convert the BLOB back into an image
        try {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(currentIdBlob);
            if (icon.getIconWidth() > 0) {
                // Resize the image to fit the label nicely so it doesn't break your UI
                java.awt.Image img = icon.getImage().getScaledInstance(200, 150, java.awt.Image.SCALE_SMOOTH);
                uploadphoto.setIcon(new javax.swing.ImageIcon(img));
                uploadphoto.setText("");
            } else {
                uploadphoto.setText("ID is a PDF (Click to open)");
            }
        } catch (Exception e) {
            uploadphoto.setText("Cannot display this ID file");
        }
    }
    
    private void openIdPhoto() {
        if (currentIdBlob == null) return;
        try {
            javax.swing.ImageIcon icon = new javax.swing.ImageIcon(currentIdBlob);
            if (icon.getIconWidth() > 0) {
                // Show a larger pop-up of the BLOB image
                javax.swing.JScrollPane sp = new javax.swing.JScrollPane(new javax.swing.JLabel(icon));
                sp.setPreferredSize(new java.awt.Dimension(
                        Math.min(icon.getIconWidth() + 20, 800), Math.min(icon.getIconHeight() + 20, 600)));
                JOptionPane.showMessageDialog(this, sp, "Valid ID - " + AccName.getText(), JOptionPane.PLAIN_MESSAGE);
            } else {
                // If the BLOB is a PDF document, extract it to a temp file and open it
                java.io.File tempPdf = java.io.File.createTempFile("ID_Document_", ".pdf");
                java.nio.file.Files.write(tempPdf.toPath(), currentIdBlob);
                java.awt.Desktop.getDesktop().open(tempPdf);
                tempPdf.deleteOnExit(); // Automatically cleans up the temp file
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not open the ID file.");
        }
    }    
    

    private void loadHistory(String accNo) {
        DefaultTableModel model = (DefaultTableModel) jTable1.getModel();
        model.setRowCount(0);
        
        java.util.List<String[]> history = QueueDatabase.getAccountHistory(accNo);
        for (String[] row : history) {
            model.addRow(row);
        }
    }

    private void editName(java.awt.event.ActionEvent evt) {
        String accNo = AccNo.getText().trim();
        if (accNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search for an account first.");
            return;
        }

        if (LOCK.getText().equals("EDIT")) {
            AccName.setEditable(true);
            AccName.requestFocus();
            LOCK.setText("SAVE");
        } else {
            String newName = AccName.getText().trim();
            if (newName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account name cannot be empty.");
                return;
            }
            
            if (QueueDatabase.updateAccountName(accNo, newName)) {
                JOptionPane.showMessageDialog(this, "Account name updated successfully.");
                AccName.setEditable(false);
                LOCK.setText("EDIT");
            } else {
                JOptionPane.showMessageDialog(this, "Failed to update account name.");
            }
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

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jLabel3 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        AccName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        AccNo = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        AccType = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        BALance = new javax.swing.JTextField();
        jButton3 = new javax.swing.JButton();
        LOCK = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jLabel8 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel6 = new javax.swing.JPanel();
        uploadphoto = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(255, 218, 106));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel2.setBackground(new java.awt.Color(12, 35, 74));

        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(251, 191, 36));
        jLabel1.setText("ADMIN PORTAL - ACCOUNT MANAGEMENT");

        jButton1.setBackground(new java.awt.Color(12, 35, 74));
        jButton1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Logout");
        jButton1.addActionListener(this::jButton1ActionPerformed);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(14, 14, 14)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 387, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 499, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(16, 16, 16))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(31, 31, 31)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addContainerGap(43, Short.MAX_VALUE))
        );

        jPanel1.add(jPanel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 994, -1));

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 153), 2, true));
        jPanel3.setOpaque(false);

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(12, 35, 74));
        jLabel2.setText("Find Account  Number:");

        jTextField1.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton2.setBackground(new java.awt.Color(30, 58, 138));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("SEARCH");

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 366, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 305, Short.MAX_VALUE)
                .addComponent(jButton2)
                .addGap(50, 50, 50))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap(17, Short.MAX_VALUE)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton2))
                .addGap(21, 21, 21))
        );

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 119, 980, -1));

        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(12, 35, 74));
        jLabel3.setText("ACCOUNT DETAILS");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(180, 200, -1, -1));

        jPanel4.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 153), 2, true));
        jPanel4.setOpaque(false);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(12, 35, 74));
        jLabel4.setText("Account Name:");

        AccName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(12, 35, 74));
        jLabel5.setText("Account NO:");

        AccNo.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(12, 35, 74));
        jLabel6.setText("Account Type:");

        AccType.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(12, 35, 74));
        jLabel7.setText("Total Balance:");

        BALance.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jButton3.setBackground(new java.awt.Color(220, 38, 38));
        jButton3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton3.setForeground(new java.awt.Color(255, 255, 255));
        jButton3.setText("LOCK");
        jButton3.addActionListener(this::jButton3ActionPerformed);

        LOCK.setBackground(new java.awt.Color(30, 58, 138));
        LOCK.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        LOCK.setForeground(new java.awt.Color(255, 255, 255));
        LOCK.setText("EDIT");

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jLabel6)
                    .addComponent(jLabel7))
                .addGap(18, 18, 18)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(AccName)
                    .addComponent(AccNo)
                    .addComponent(AccType)
                    .addComponent(BALance, javax.swing.GroupLayout.DEFAULT_SIZE, 265, Short.MAX_VALUE))
                .addGap(32, 32, 32)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(LOCK, javax.swing.GroupLayout.DEFAULT_SIZE, 94, Short.MAX_VALUE)
                    .addComponent(jButton3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(16, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(AccName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(AccNo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(AccType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(LOCK, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 94, Short.MAX_VALUE)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(BALance, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButton3, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(28, 28, 28))
        );

        jPanel1.add(jPanel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 231, 540, 250));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Date", "Transaction Type", "Cash Flow", "Balance", "Ref No."
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        jScrollPane1.setViewportView(jTable1);

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(6, 521, 982, 140));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(12, 35, 74));
        jLabel8.setText("TRANSACTION HISTORY");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 490, -1, -1));

        jButton4.setBackground(new java.awt.Color(30, 58, 138));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("BACK");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(900, 670, -1, -1));

        jPanel6.setRequestFocusEnabled(false);
        jPanel6.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());
        jPanel6.add(uploadphoto, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 0, -1, -1));

        jPanel1.add(jPanel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(560, 200, 420, 300));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, 710));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        // TODO add your handling code here:
        String accNo = AccNo.getText().trim();
        if (accNo.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Search for an account first.");
            return;
        }
     
        boolean isCurrentlyLocked = jButton3.getText().equals("UNLOCK");
        boolean success = QueueDatabase.toggleAccountLock(accNo, !isCurrentlyLocked);
        
        if (success) {
            if (isCurrentlyLocked) {
                jButton3.setText("LOCK");
                JOptionPane.showMessageDialog(this, "Account Unlocked. Transactions are now allowed.");
            } else {
                jButton3.setText("UNLOCK");
                JOptionPane.showMessageDialog(this, "Account Locked. All future Teller transactions will be blocked.");
            }
        } else {
            JOptionPane.showMessageDialog(this, "Failed to change account lock status.");
        }
        
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       new Loginadminteller().setVisible(true);
       this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        // TODO add your handling code here:
        Admin adminnaframe = new Admin();
        
        adminnaframe.setVisible(true);
        
        this.dispose();
    }//GEN-LAST:event_jButton4ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Adminacciuntmanagement().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField AccName;
    private javax.swing.JTextField AccNo;
    private javax.swing.JTextField AccType;
    private javax.swing.JTextField BALance;
    private javax.swing.JButton LOCK;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JLabel uploadphoto;
    // End of variables declaration//GEN-END:variables
}
