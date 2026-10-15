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
    // Give your checkboxes clean, uniform sizing so they never clip text
java.awt.Dimension checkboxSize = new java.awt.Dimension(160, 30);
jCheckBox1.setPreferredSize(checkboxSize);
jCheckBox2.setPreferredSize(checkboxSize);
jCheckBox3.setPreferredSize(checkboxSize);
jCheckBox5.setPreferredSize(checkboxSize);
jCheckBox6.setPreferredSize(checkboxSize);
PWDline.setPreferredSize(checkboxSize);
 
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
        PWDline.setSelected(false); // NOTE: Clears PWD
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

        EmployeeName.setText(profile.length > 0 && profile[0] != null ? profile[0] : "");
        jTextField2.setText(profile.length > 1 && profile[1] != null ? profile[1] : "");
        jTextField3.setText(profile.length > 2 && profile[2] != null ? profile[2] : "");
        contactNumber.setText(profile.length > 3 && profile[3] != null ? profile[3] : "");
        
        UserName.setText(username);
        
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
        PWDline.setSelected(perms.isPwdLine()); // NOTE: Maps DB to checkbox
        
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
        SaveModifications = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        AccountStatus = new javax.swing.JComboBox<>();
        jLabel9 = new javax.swing.JLabel();
        ResetPassword = new javax.swing.JButton();
        PassWord = new javax.swing.JTextField();
        jLabel8 = new javax.swing.JLabel();
        UserName = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        contactNumber = new javax.swing.JTextField();
        jTextField3 = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        EmployeeName = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        SelectEmployee = new javax.swing.JComboBox<>();
        jLabel3 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jCheckBox2 = new javax.swing.JCheckBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jCheckBox5 = new javax.swing.JCheckBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        PWDline = new javax.swing.JCheckBox();
        jCheckBox1 = new javax.swing.JCheckBox();
        jLabel6 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jButton6 = new javax.swing.JButton();
        jToggleButton2 = new javax.swing.JToggleButton();
        jButton4 = new javax.swing.JButton();
        jLabel13 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel1.setBackground(new java.awt.Color(234, 243, 255));
        jPanel1.setPreferredSize(new java.awt.Dimension(786, 633));
        jPanel1.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        SaveModifications.setBackground(new java.awt.Color(197, 160, 89));
        SaveModifications.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        SaveModifications.setForeground(new java.awt.Color(12, 35, 74));
        SaveModifications.setText("SAVE MODIFICATIONS");
        SaveModifications.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        SaveModifications.addActionListener(this::SaveModificationsActionPerformed);
        jPanel1.add(SaveModifications, new org.netbeans.lib.awtextra.AbsoluteConstraints(830, 110, 190, 30));

        jButton2.setBackground(new java.awt.Color(197, 160, 89));
        jButton2.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(12, 35, 74));
        jButton2.setText("DELETE ACCOUNT");
        jButton2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton2.addActionListener(this::jButton2ActionPerformed);
        jPanel1.add(jButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(660, 110, -1, 30));

        AccountStatus.setBackground(new java.awt.Color(12, 35, 74));
        AccountStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        AccountStatus.setForeground(new java.awt.Color(255, 255, 255));
        AccountStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        AccountStatus.setToolTipText("");
        AccountStatus.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jPanel1.add(AccountStatus, new org.netbeans.lib.awtextra.AbsoluteConstraints(710, 210, 260, -1));

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setForeground(new java.awt.Color(255, 255, 255));
        jLabel9.setText("Account Status:");
        jPanel1.add(jLabel9, new org.netbeans.lib.awtextra.AbsoluteConstraints(600, 200, -1, 40));

        ResetPassword.setBackground(new java.awt.Color(197, 160, 89));
        ResetPassword.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        ResetPassword.setForeground(new java.awt.Color(12, 35, 74));
        ResetPassword.setText("EDIT PASSWORD");
        ResetPassword.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        ResetPassword.addActionListener(this::ResetPasswordActionPerformed);
        jPanel1.add(ResetPassword, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 360, 140, 30));

        PassWord.setBackground(new java.awt.Color(12, 35, 74));
        PassWord.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        PassWord.setForeground(new java.awt.Color(255, 255, 255));
        PassWord.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        PassWord.addActionListener(this::PassWordActionPerformed);
        jPanel1.add(PassWord, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 360, 210, -1));

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setForeground(new java.awt.Color(255, 255, 255));
        jLabel8.setText("Password:");
        jPanel1.add(jLabel8, new org.netbeans.lib.awtextra.AbsoluteConstraints(100, 360, -1, -1));

        UserName.setBackground(new java.awt.Color(12, 35, 74));
        UserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        UserName.setForeground(new java.awt.Color(255, 255, 255));
        UserName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jPanel1.add(UserName, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 330, 210, -1));

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setForeground(new java.awt.Color(255, 255, 255));
        jLabel7.setText("  Username:");
        jPanel1.add(jLabel7, new org.netbeans.lib.awtextra.AbsoluteConstraints(90, 330, 80, -1));

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setForeground(new java.awt.Color(255, 255, 255));
        jLabel12.setText(" Contact Number:");
        jPanel1.add(jLabel12, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 300, 120, 20));

        contactNumber.setBackground(new java.awt.Color(12, 35, 74));
        contactNumber.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        contactNumber.setForeground(new java.awt.Color(255, 255, 255));
        contactNumber.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jPanel1.add(contactNumber, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 300, 210, -1));

        jTextField3.setEditable(false);
        jTextField3.setBackground(new java.awt.Color(12, 35, 74));
        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField3.setForeground(new java.awt.Color(255, 255, 255));
        jTextField3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(197, 160, 89), 1, true));
        jTextField3.addActionListener(this::jTextField3ActionPerformed);
        jPanel1.add(jTextField3, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 270, 210, -1));

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setForeground(new java.awt.Color(255, 255, 255));
        jLabel11.setText("Email Adress:");
        jPanel1.add(jLabel11, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 270, -1, -1));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setForeground(new java.awt.Color(255, 255, 255));
        jLabel5.setText("Employee ID:");
        jPanel1.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(80, 240, 90, 20));

        jTextField2.setBackground(new java.awt.Color(12, 35, 74));
        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField2.setForeground(new java.awt.Color(255, 255, 255));
        jTextField2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jTextField2.addActionListener(this::jTextField2ActionPerformed);
        jPanel1.add(jTextField2, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 240, 210, -1));

        EmployeeName.setEditable(false);
        EmployeeName.setBackground(new java.awt.Color(12, 35, 74));
        EmployeeName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EmployeeName.setForeground(new java.awt.Color(255, 255, 255));
        EmployeeName.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        EmployeeName.addActionListener(this::EmployeeNameActionPerformed);
        jPanel1.add(EmployeeName, new org.netbeans.lib.awtextra.AbsoluteConstraints(170, 210, 210, -1));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setForeground(new java.awt.Color(255, 255, 255));
        jLabel4.setText("Employee Name:");
        jPanel1.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(54, 210, 120, -1));

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(255, 255, 255));
        jLabel2.setText("Select Employee:");
        jPanel1.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(20, 110, -1, 30));

        SelectEmployee.setBackground(new java.awt.Color(12, 35, 74));
        SelectEmployee.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        SelectEmployee.setForeground(new java.awt.Color(255, 255, 255));
        SelectEmployee.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jPanel1.add(SelectEmployee, new org.netbeans.lib.awtextra.AbsoluteConstraints(140, 110, 250, 30));

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setForeground(new java.awt.Color(255, 255, 255));
        jLabel3.setText("TELLER CREDENTIALS AND PROFILE");
        jPanel1.add(jLabel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 170, -1, -1));

        jPanel3.setOpaque(false);
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jCheckBox3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox3.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox3.setText("Billspayment");
        jPanel3.add(jCheckBox3, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 70, -1, -1));

        jCheckBox2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox2.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox2.setText("Cash deposits");
        jPanel3.add(jCheckBox2, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 90, -1, -1));

        jCheckBox4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox4.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox4.setText("Fund transfers");
        jPanel3.add(jCheckBox4, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 70, -1, -1));

        jCheckBox5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox5.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox5.setText("Cash withdrawals");
        jPanel3.add(jCheckBox5, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 50, -1, -1));

        jCheckBox6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox6.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox6.setText("Foreignexchange");
        jPanel3.add(jCheckBox6, new org.netbeans.lib.awtextra.AbsoluteConstraints(150, 90, -1, -1));

        PWDline.setFont(new java.awt.Font("Segoe UI", 1, 13)); // NOI18N
        PWDline.setForeground(new java.awt.Color(255, 255, 255));
        PWDline.setText("PWD LINE");
        PWDline.addActionListener(this::PWDlineActionPerformed);
        jPanel3.add(PWDline, new org.netbeans.lib.awtextra.AbsoluteConstraints(290, 50, -1, -1));

        jCheckBox1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox1.setForeground(new java.awt.Color(255, 255, 255));
        jCheckBox1.setText("Account creation");
        jCheckBox1.addActionListener(this::jCheckBox1ActionPerformed);
        jPanel3.add(jCheckBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 50, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setForeground(new java.awt.Color(255, 255, 255));
        jLabel6.setText("Assigned Desk:");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(10, 10, -1, -1));

        jPanel1.add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 260, 480, 150));

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setForeground(new java.awt.Color(255, 255, 255));
        jLabel10.setText("RECENT TELLER ACTIVITY");
        jPanel1.add(jLabel10, new org.netbeans.lib.awtextra.AbsoluteConstraints(60, 460, -1, -1));

        jTable1.setBackground(new java.awt.Color(12, 35, 74));
        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jTable1.setForeground(new java.awt.Color(255, 255, 255));
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

        jPanel1.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(50, 490, 990, 100));

        jButton6.setBackground(new java.awt.Color(12, 35, 74));
        jButton6.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("BACK");
        jButton6.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jButton6.addActionListener(this::jButton6ActionPerformed);
        jPanel1.add(jButton6, new org.netbeans.lib.awtextra.AbsoluteConstraints(540, 110, 110, 30));

        jToggleButton2.setBackground(new java.awt.Color(12, 35, 74));
        jToggleButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jToggleButton2.setForeground(new java.awt.Color(255, 255, 255));
        jToggleButton2.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(197, 160, 89)));
        jToggleButton2.addActionListener(this::jToggleButton2ActionPerformed);
        jPanel1.add(jToggleButton2, new org.netbeans.lib.awtextra.AbsoluteConstraints(950, 30, 140, 40));

        jButton4.setBackground(new java.awt.Color(197, 160, 89));
        jButton4.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(12, 35, 74));
        jButton4.setText("REFRESH");
        jButton4.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jButton4.addActionListener(this::jButton4ActionPerformed);
        jPanel1.add(jButton4, new org.netbeans.lib.awtextra.AbsoluteConstraints(390, 110, 80, 30));

        jLabel13.setIcon(new javax.swing.ImageIcon("C:\\Users\\Rara\\Documents\\CornucopiaUnion\\src\\main\\java\\addminimages\\TellerManagementt (2).png")); // NOI18N
        jLabel13.setText("jLabel13");
        jPanel1.add(jLabel13, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, -40, 1120, 720));

        getContentPane().add(jPanel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 1100, 630));

        pack();
    }// </editor-fold>//GEN-END:initComponents

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
                jCheckBox1.isSelected(),   
                jCheckBox2.isSelected(),   
                jCheckBox3.isSelected(),   
                jCheckBox4.isSelected(),   
                jCheckBox5.isSelected(),   
                jCheckBox6.isSelected(),   
                PWDline.isSelected()); // NOTE: Saves new PWD Checkbox status
     
        if (QueueDatabase.updateTeller(username, name, employeeId, email, contact,
                permissions, AccountStatus.getSelectedItem().toString().toUpperCase())) {
            ValidationUtils.showSuccess(this, "Changes saved for " + name.trim() + ".");
            loadTellerList(username);   
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

    private void PWDlineActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PWDlineActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PWDlineActionPerformed

    private void jTextField3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField3ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField3ActionPerformed

    private void PassWordActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_PassWordActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_PassWordActionPerformed

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
    private javax.swing.JCheckBox PWDline;
    private javax.swing.JTextField PassWord;
    private javax.swing.JButton ResetPassword;
    private javax.swing.JButton SaveModifications;
    private javax.swing.JComboBox<String> SelectEmployee;
    private javax.swing.JTextField UserName;
    private javax.swing.JTextField contactNumber;
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
