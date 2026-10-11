/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;

/**
 *
 * @author Lenovo
 */
public class Admintellermanagement extends javax.swing.JFrame {
    
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(Admintellermanagement.class.getName());

    /**
     * Creates new form Admintellermanagement
     * 
     */
    
    private boolean loading = false;   // true while the dropdown is being refilled
    
      public Admintellermanagement() {
         initComponents();
    QueueDatabase.initializeUsersTable();
    setLocationRelativeTo(null);
 
    jCheckBox3.setText("Bills payment");
    AccountStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[]{"Active", "Inactive", "Terminated"}));
 
    // Online (green) / Offline (red) button. Clicking it changes the selected teller's status.
    jToggleButton2.addActionListener(e -> {
        String user = selectedUsername();
        if (user == null) {
            jToggleButton2.setSelected(false);
        } else {
            QueueDatabase.setTellerOnline(user, jToggleButton2.isSelected());
        }
        showOnlineState();
    });
    showOnlineState();
    UserName.setEditable(false);   // username is the login ID
 
    SelectEmployee.addActionListener(e -> {
        if (!loading) loadSelectedTeller();
    });
    jButton6.addActionListener(e -> {   // BACK
        new Admin().setVisible(true);
        this.dispose();
    });
 
    loadTellerList(null);
    }
    
    
    /** Dropdown text is "Full Name (username)"; this pulls the username back out. */
private String selectedUsername() {
    Object item = SelectEmployee.getSelectedItem();
    if (item == null) return null;
    String text = item.toString();
    int open = text.lastIndexOf('(');
    int close = text.lastIndexOf(')');
    if (open < 0 || close < open) return null;
    return text.substring(open + 1, close);
}
 
private void loadTellerList(String selectUsername) {
    loading = true;
    SelectEmployee.removeAllItems();
    for (String[] t : QueueDatabase.getAllTellers()) {
        SelectEmployee.addItem(t[0] + " (" + t[1] + ")");
    }
    if (selectUsername != null) {
        for (int i = 0; i < SelectEmployee.getItemCount(); i++) {
            if (SelectEmployee.getItemAt(i).endsWith("(" + selectUsername + ")")) {
                SelectEmployee.setSelectedIndex(i);
                break;
            }
        }
    }
    
    loading = false;
    loadSelectedTeller();
}
 
private void clearFields() {
    EmployeeName.setText("");
    jTextField2.setText("");
    jTextField3.setText("");
    contactNumber.setText("");
    UserName.setText("");
    PassWord.setText("");
    jCheckBox1.setSelected(false);
    jCheckBox2.setSelected(false);
    jCheckBox3.setSelected(false);
    jCheckBox4.setSelected(false);
    jCheckBox5.setSelected(false);
    jCheckBox6.setSelected(false);
    AccountStatus.setSelectedIndex(0);
    jToggleButton2.setSelected(false);
    showOnlineState();
    ((javax.swing.table.DefaultTableModel) jTable1.getModel()).setRowCount(0);
}
 
private void loadSelectedTeller() {
    String username = selectedUsername();
    if (username == null) { clearFields(); return; }
    String[] profile = QueueDatabase.getTellerProfile(username);
    TellerPermissions perms = QueueDatabase.getTellerPermissions(username);
    if (profile == null || perms == null) { clearFields(); return; }

    // Added bounds checking and null safety to prevent exceptions from hiding the data
    EmployeeName.setText(profile.length > 0 && profile[0] != null ? profile[0] : "");
    jTextField2.setText(profile.length > 1 && profile[1] != null ? profile[1] : "");
    jTextField3.setText(profile.length > 2 && profile[2] != null ? profile[2] : "");
    contactNumber.setText(profile.length > 3 && profile[3] != null ? profile[3] : "");
    
    UserName.setText(username);
    
    // Safety checks for Status, Password, and Online status
    String st = (profile.length > 4 && profile[4] != null) ? profile[4].toUpperCase() : "ACTIVE";
    AccountStatus.setSelectedItem(st.equals("TERMINATED") ? "Terminated" : st.equals("INACTIVE") ? "Inactive" : "Active");
    
    PassWord.setText(profile.length > 5 && profile[5] != null ? profile[5] : "");
    jToggleButton2.setSelected(profile.length > 6 && "1".equals(profile[6]));
    showOnlineState();
    
    jCheckBox1.setSelected(perms.canAccountCreation());
    jCheckBox2.setSelected(perms.canCashDeposits());
    jCheckBox3.setSelected(perms.canBillPayments());
    jCheckBox4.setSelected(perms.canFundTransfers());
    jCheckBox5.setSelected(perms.canCashWithdrawals());
    jCheckBox6.setSelected(perms.canForeignExchange());
    
    loadActivity(username);
}
 
/** Green + "ONLINE" when selected, red + "OFFLINE" when not. */
private void showOnlineState() {
    boolean on = jToggleButton2.isSelected();
    jToggleButton2.setText(on ? "ONLINE" : "OFFLINE");
    jToggleButton2.setFont(new java.awt.Font("Segoe UI", 1, 11));
    jToggleButton2.setBackground(on ? new java.awt.Color(0, 153, 0) : new java.awt.Color(204, 0, 0));
    jToggleButton2.setForeground(java.awt.Color.WHITE);
    jToggleButton2.setOpaque(true);
}
 
private void loadActivity(String username) {
    javax.swing.table.DefaultTableModel model = (javax.swing.table.DefaultTableModel) jTable1.getModel();
    model.setRowCount(0);
    for (String[] row : QueueDatabase.getTellerActivity(username)) {
        model.addRow(row);
    }
}
    /**ss
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        SelectEmployee = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        EmployeeName = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        UserName = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        PassWord = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        AccountStatus = new javax.swing.JComboBox<>();
        ResetPassword = new javax.swing.JButton();
        SaveModifications = new javax.swing.JButton();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jLabel11 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        contactNumber = new javax.swing.JTextField();
        jCheckBox6 = new javax.swing.JCheckBox();
        jButton2 = new javax.swing.JButton();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jButton4 = new javax.swing.JButton();
        jButton1 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(234, 243, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(786, 633));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(12, 35, 74));
        jLabel2.setText("Select Employee:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 110, -1, 20));

        SelectEmployee.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel1.add(SelectEmployee, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 250, -1));

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(12, 35, 74));
        jLabel3.setText("TELLER CREDENTIALS AND PROFILE");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 140, -1, -1));

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 153), 2, true));
        jPanel3.setOpaque(false);
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(12, 35, 74));
        jLabel4.setText("Employee Name:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 16, -1, -1));

        EmployeeName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EmployeeName.addActionListener(this::EmployeeNameActionPerformed);
        jPanel3.add(EmployeeName, new org.netbeans.lib.awtextra.AbsoluteConstraints(142, 13, 208, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(12, 35, 74));
        jLabel5.setText("Employee ID:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 57, 114, -1));

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField2.addActionListener(this::jTextField2ActionPerformed);
        jPanel3.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(142, 54, 208, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(12, 35, 74));
        jLabel6.setText("Assigned Desk:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(8, 94, -1, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(12, 35, 74));
        jLabel7.setText("Username:");
        jPanel3.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 190, -1, 30));

        UserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel3.add(UserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 190, 208, 30));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(12, 35, 74));
        jLabel8.setText("Password:");
        jPanel3.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 230, -1, -1));

        PassWord.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel3.add(PassWord, new org.netbeans.lib.awtextra.AbsoluteConstraints(110, 230, 208, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(12, 35, 74));
        jLabel9.setText("Account Status:");
        jPanel3.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 270, -1, 20));

        AccountStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        AccountStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        AccountStatus.setToolTipText("");
        jPanel3.add(AccountStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(120, 266, 208, 30));

        ResetPassword.setBackground(new java.awt.Color(30, 58, 138));
        ResetPassword.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        ResetPassword.setForeground(new java.awt.Color(255, 255, 255));
        ResetPassword.setText("EDIT PASSWORD");
        ResetPassword.addActionListener(this::ResetPasswordActionPerformed);
        jPanel3.add(ResetPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(350, 230, -1, 30));

        SaveModifications.setBackground(new java.awt.Color(0, 153, 0));
        SaveModifications.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        SaveModifications.setForeground(new java.awt.Color(255, 255, 255));
        SaveModifications.setText("SAVE MODIFICATIONS");
        SaveModifications.addActionListener(this::SaveModificationsActionPerformed);
        jPanel3.add(SaveModifications, new org.netbeans.lib.awtextra.AbsoluteConstraints(550, 230, -1, 30));

        jCheckBox3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox3.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox3.setText("Billspayment");
        jPanel3.add(jCheckBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(256, 122, -1, -1));

        jCheckBox1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox1.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox1.setText("Account creation");
        jCheckBox1.addActionListener(this::jCheckBox1ActionPerformed);
        jPanel3.add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(114, 92, -1, -1));

        jCheckBox2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox2.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox2.setText("Cash deposits");
        jPanel3.add(jCheckBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(114, 158, -1, -1));

        jCheckBox4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox4.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox4.setText("Fund transfers");
        jPanel3.add(jCheckBox4, new org.netbeans.lib.awtextra.AbsoluteConstraints(114, 122, -1, -1));

        jCheckBox5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox5.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox5.setText("Cash withdrawals");
        jPanel3.add(jCheckBox5, new org.netbeans.lib.awtextra.AbsoluteConstraints(256, 92, -1, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(12, 35, 74));
        jLabel11.setText("Email Adress:");
        jPanel3.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(427, 16, -1, -1));

        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel3.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 13, 197, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(12, 35, 74));
        jLabel12.setText("Contact Number:");
        jPanel3.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(427, 57, -1, -1));

        contactNumber.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jPanel3.add(contactNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(547, 54, 197, -1));

        jCheckBox6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox6.setForeground(new java.awt.Color(12, 35, 74));
        jCheckBox6.setText("Foreignexchange");
        jPanel3.add(jCheckBox6, new org.netbeans.lib.awtextra.AbsoluteConstraints(256, 158, -1, -1));

        jButton2.setBackground(new java.awt.Color(220, 38, 38));
        jButton2.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("DELETE ACCOUNT");
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel3.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(559, 270, 190, 30));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 170, 760, 310));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(12, 35, 74));
        jLabel10.setText("RECENT TELLER ACTIVITY");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 480, -1, -1));

        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Time", "Ticket NO.", "Transaction Type", "Status ", "Handled"
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

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 500, 760, 130));

        jButton6.setBackground(new java.awt.Color(30, 58, 138));
        jButton6.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("BACK");
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(690, 640, -1, -1));

        jToggleButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jToggleButton2.addActionListener(this::jToggleButton2ActionPerformed);
        jPanel1.add(jToggleButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(640, 110, 130, 30));

        jButton4.setBackground(new java.awt.Color(30, 58, 138));
        jButton4.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("REFRESH");
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(520, 110, -1, 30));

        jButton1.setBackground(new java.awt.Color(12, 35, 74));
        jButton1.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Logout");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel1.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(670, 40, -1, -1));

        jLabel13.setIcon(new javax.swing.ImageIcon("C:\\Users\\Lenovo\\OneDrive\\Documents\\NetBeansProjects\\CornucopiaUnion\\src\\main\\java\\Images\\tellermanagement.png")); // NOI18N
        jLabel13.setText("jLabel13");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -60, 1160, 790));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 784, 761));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
       new Loginadminteller().setVisible(true);
       this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void SaveModificationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveModificationsActionPerformed
         String username = selectedUsername();
    if (username == null) {
        ValidationUtils.showError(this, "Select a teller first.");
        return;
    }
    String name = EmployeeName.getText();
    String employeeId = jTextField2.getText();
    String email = jTextField3.getText();
    String contact = contactNumber.getText();
 
    if (!ValidationUtils.isValidName(name)) {
        ValidationUtils.showError(this, "Enter a valid employee full name (letters only).");
        return;
    }
    if (!ValidationUtils.isValidReferenceNumber(employeeId)) {
        ValidationUtils.showError(this, "Employee ID must be 4-30 characters (letters, numbers, hyphen).");
        return;
    }
    if (QueueDatabase.employeeIdExists(employeeId, username)) {
        ValidationUtils.showError(this, "That employee ID is already used by another teller.");
        return;
    }
    if (!ValidationUtils.isValidEmail(email)) {
        ValidationUtils.showError(this, "Enter a valid email address (e.g. name@example.com).");
        return;
    }
    if (!ValidationUtils.isValidContactNumber(contact)) {
        ValidationUtils.showError(this, "Contact number must be 09XXXXXXXXX or +639XXXXXXXXX.");
        return;
    }
    if (!ValidationUtils.isValidComboSelection(AccountStatus.getSelectedItem())) {
        ValidationUtils.showError(this, "Select an account status.");
        return;
    }
    boolean active = "Active".equals(AccountStatus.getSelectedItem().toString());
    if (active && !(jCheckBox1.isSelected() || jCheckBox2.isSelected() || jCheckBox3.isSelected()
            || jCheckBox4.isSelected() || jCheckBox5.isSelected() || jCheckBox6.isSelected())) {
        ValidationUtils.showError(this, "An active teller needs at least one permission.");
        return;
    }
 
    TellerPermissions permissions = new TellerPermissions(
            jCheckBox1.isSelected(),   // Account creation
            jCheckBox2.isSelected(),   // Cash deposits
            jCheckBox3.isSelected(),   // Bills payment
            jCheckBox4.isSelected(),   // Fund transfers
            jCheckBox5.isSelected(),   // Cash withdrawals
            jCheckBox6.isSelected());   // Foreign exchange
 
    if (QueueDatabase.updateTeller(username, name, employeeId, email, contact,
            permissions, AccountStatus.getSelectedItem().toString().toUpperCase())) {
        ValidationUtils.showSuccess(this, "Changes saved for " + name.trim() + ".");
        loadTellerList(username);   // name may have changed
    } else {
        ValidationUtils.showError(this, "Could not save the changes. Please try again.");
    }   
  
 
   
    }//GEN-LAST:event_SaveModificationsActionPerformed

    private void ResetPasswordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ResetPasswordActionPerformed
   String username = selectedUsername();
        if (username == null) {
            ValidationUtils.showError(this, "Select a teller first.");
            return;
        }
        String newPassword = PassWord.getText();
        if (!ValidationUtils.isValidPassword(newPassword)) {
            ValidationUtils.showError(this, "Type the new password (at least 6 characters) in the Password box first.");
            return;
        }
        int choice = javax.swing.JOptionPane.showConfirmDialog(this,
                "Change the password of " + username + "?", "Confirm", javax.swing.JOptionPane.YES_NO_OPTION);
        if (choice != javax.swing.JOptionPane.YES_OPTION) return;

        if (QueueDatabase.updateTellerPassword(username, newPassword)) {
            ValidationUtils.showSuccess(this, "Password updated for " + username + ".");
        } else {
            ValidationUtils.showError(this, "Could not update the password. Please try again.");
        }
    }//GEN-LAST:event_ResetPasswordActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jToggleButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jToggleButton2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jToggleButton2ActionPerformed

    private void jTextField2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField2ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField2ActionPerformed

    private void EmployeeNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_EmployeeNameActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_EmployeeNameActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // DELETE ACCOUNT
    String username = selectedUsername();
    if (username == null) {
        ValidationUtils.showError(this, "Select a teller first.");
        return;
    }
    int choice = javax.swing.JOptionPane.showConfirmDialog(this,
            "Permanently delete the teller account \"" + username + "\"?\n"
            + "This cannot be undone. Past transaction records are kept.",
            "Confirm Delete", javax.swing.JOptionPane.YES_NO_OPTION,
            javax.swing.JOptionPane.WARNING_MESSAGE);
    if (choice != javax.swing.JOptionPane.YES_OPTION) return;

    if (QueueDatabase.deleteTeller(username)) {
        ValidationUtils.showSuccess(this, "Teller " + username + " deleted.");
        loadTellerList(null);
    } else {
        ValidationUtils.showError(this, "Could not delete the account. Please try again.");
    }
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
     // REFRESH: reload the teller list, profile, online state and activity, keeping the same teller selected.
    loadTellerList(selectedUsername()); 
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton6ActionPerformed
        // TODO add your handling code here:
         Admindashboard adminnaframe = new Admindashboard();
        
        adminnaframe.setVisible(true);
        
        this.dispose();
    }//GEN-LAST:event_jButton6ActionPerformed

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
        java.awt.EventQueue.invokeLater(() -> new Admintellermanagement().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox<String> AccountStatus;
    private javax.swing.JTextField EmployeeName;
    private javax.swing.JTextField PassWord;
    private javax.swing.JButton ResetPassword;
    private javax.swing.JButton SaveModifications;
    private javax.swing.JComboBox<String> SelectEmployee;
    private javax.swing.JTextField UserName;
    private javax.swing.JTextField contactNumber;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButton6;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JToggleButton jToggleButton2;
    // End of variables declaration//GEN-END:variables
}
