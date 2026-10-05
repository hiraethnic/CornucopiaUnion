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
        jPanel2 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
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

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBackground(new java.awt.Color(244, 246, 249));
        jPanel1.setPreferredSize(new java.awt.Dimension(786, 633));

        jPanel2.setBackground(new java.awt.Color(12, 35, 74));

        jLabel1.setBackground(new java.awt.Color(255, 255, 255));
        jLabel1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(251, 191, 36));
        jLabel1.setText("ADMIN PORTAL - TELLER MANAGEMENT");

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
                .addGap(16, 16, 16)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton1)
                .addGap(15, 15, 15))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(37, 37, 37)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jButton1))
                .addContainerGap(37, Short.MAX_VALUE))
        );

        jLabel2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel2.setText("Select Employee:");

        SelectEmployee.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel3.setBackground(new java.awt.Color(255, 255, 255));
        jLabel3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel3.setText("TELLER CREDENTIALS AND PROFILE");

        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 153), 2, true));
        jPanel3.setOpaque(false);

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Employee Name:");

        EmployeeName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        EmployeeName.addActionListener(this::EmployeeNameActionPerformed);

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Employee ID:");

        jTextField2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jTextField2.addActionListener(this::jTextField2ActionPerformed);

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Assigned Desk:");

        jLabel7.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel7.setText("Username:");

        UserName.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel8.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel8.setText("Password:");

        PassWord.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel9.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel9.setText("Account Status:");

        AccountStatus.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        AccountStatus.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "" }));
        AccountStatus.setToolTipText("");

        ResetPassword.setBackground(new java.awt.Color(30, 58, 138));
        ResetPassword.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        ResetPassword.setForeground(new java.awt.Color(255, 255, 255));
        ResetPassword.setText("EDIT PASSWORD");
        ResetPassword.addActionListener(this::ResetPasswordActionPerformed);

        SaveModifications.setBackground(new java.awt.Color(0, 204, 0));
        SaveModifications.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        SaveModifications.setForeground(new java.awt.Color(255, 255, 255));
        SaveModifications.setText("SAVE MODIFICATIONS");
        SaveModifications.addActionListener(this::SaveModificationsActionPerformed);

        jCheckBox3.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox3.setText("Billspayment");

        jCheckBox1.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox1.setText("Account creation");
        jCheckBox1.addActionListener(this::jCheckBox1ActionPerformed);

        jCheckBox2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox2.setText("Cash deposits");

        jCheckBox4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox4.setText("Fund transfers");

        jCheckBox5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox5.setText("Cash withdrawals");

        jLabel11.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel11.setText("Email Adress:");

        jTextField3.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jLabel12.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel12.setText("Contact Number:");

        contactNumber.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N

        jCheckBox6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jCheckBox6.setText("Foreignexchange");

        jButton2.setBackground(new java.awt.Color(220, 38, 38));
        jButton2.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton2.setForeground(new java.awt.Color(255, 255, 255));
        jButton2.setText("DELETE ACCOUNT");
        jButton2.addActionListener(this::jButton2ActionPerformed);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addGap(6, 6, 6)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jCheckBox5)
                            .addComponent(jCheckBox3)
                            .addComponent(jCheckBox6))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(18, 18, 18)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(EmployeeName, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(77, 77, 77)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel12)
                            .addComponent(jLabel11))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jTextField3, javax.swing.GroupLayout.DEFAULT_SIZE, 197, Short.MAX_VALUE)
                            .addComponent(contactNumber))
                        .addGap(26, 26, 26))
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel7)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(UserName, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel8)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(PassWord, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel3Layout.createSequentialGroup()
                                .addComponent(jLabel9)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(AccountStatus, javax.swing.GroupLayout.PREFERRED_SIZE, 208, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addGap(18, 18, 18)
                        .addComponent(ResetPassword)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jButton2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(SaveModifications, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(11, 11, 11)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(EmployeeName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11)
                    .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(15, 15, 15)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel12)
                    .addComponent(contactNumber, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jCheckBox1)
                            .addComponent(jCheckBox5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jCheckBox4)
                            .addComponent(jCheckBox3))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jCheckBox2)
                            .addComponent(jCheckBox6, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel7)
                            .addComponent(UserName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel8)
                            .addComponent(PassWord, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel9)
                            .addComponent(AccountStatus, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(23, 23, 23))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(SaveModifications)
                            .addComponent(ResetPassword))
                        .addGap(18, 18, 18)
                        .addComponent(jButton2)
                        .addContainerGap())))
        );

        jLabel10.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel10.setText("RECENT TELLER ACTIVITY");

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

        jButton6.setBackground(new java.awt.Color(30, 58, 138));
        jButton6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton6.setForeground(new java.awt.Color(255, 255, 255));
        jButton6.setText("BACK");
        jButton6.addActionListener(this::jButton6ActionPerformed);

        jToggleButton2.setFont(new java.awt.Font("Segoe UI", 0, 14)); // NOI18N
        jToggleButton2.addActionListener(this::jToggleButton2ActionPerformed);

        jButton4.setBackground(new java.awt.Color(30, 58, 138));
        jButton4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jButton4.setForeground(new java.awt.Color(255, 255, 255));
        jButton4.setText("REFRESH");
        jButton4.addActionListener(this::jButton4ActionPerformed);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton6)
                .addGap(30, 30, 30))
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel10)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(18, 18, 18)
                        .addComponent(SelectEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, 250, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 78, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton4)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToggleButton2, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(jLabel2)
                        .addComponent(SelectEmployee, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(34, 34, 34)
                        .addComponent(jLabel3))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton4)))
                .addGap(18, 18, 18)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addComponent(jLabel10)
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 140, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButton6)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, 784, Short.MAX_VALUE)
        );

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
         Admin adminnaframe = new Admin();
        
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
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JToggleButton jToggleButton2;
    // End of variables declaration//GEN-END:variables
}
