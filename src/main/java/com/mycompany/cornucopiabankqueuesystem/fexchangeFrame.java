/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package com.mycompany.cornucopiabankqueuesystem;

/**
 *
 * @author admin
 */
        public class fexchangeFrame extends javax.swing.JFrame {
            
            private static final java.util.Map<String, Double> EXCHANGE_RATES = new java.util.LinkedHashMap<>();
            private static long lastFetchTime = 0;
            private static final long REFRESH_INTERVAL_MS = 24 * 60 * 60 * 1000L; // ito ung para sa 24hr cache
            private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(fexchangeFrame.class.getName());

            // basically ito lng mangyayari if wlng internet
            private static void loadFallbackRates()
            { 
              EXCHANGE_RATES.put("SAR", 15.10);
              EXCHANGE_RATES.put("AED", 15.40);
              EXCHANGE_RATES.put("KWD", 184.50);
              EXCHANGE_RATES.put("QAR", 15.50);
              EXCHANGE_RATES.put("SGD", 42.50);
              EXCHANGE_RATES.put("HKD", 7.30);
              EXCHANGE_RATES.put("JPY", 0.39);
              EXCHANGE_RATES.put("TWD", 1.80);
              EXCHANGE_RATES.put("USD", 57.00);
              EXCHANGE_RATES.put("CAD", 42.00);
            }
            
           private void updateRatesusingAPI() {
    long currentTime = System.currentTimeMillis();
    
    if (!EXCHANGE_RATES.isEmpty() && (currentTime - lastFetchTime < REFRESH_INTERVAL_MS)) {
        return;
    }
    
    new Thread(() -> {
        java.util.Map<String, Double> fetchedRates = new java.util.HashMap<>();
        boolean success = false;

        try {
            java.net.URL url = new java.net.URL("https://api.frankfurter.app/latest?from=PHP");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            
            if (conn.getResponseCode() == 200) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(conn.getInputStream()))) {
                    
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    
                    String json = response.toString();
                    String[] targetCurrencies = {"SAR", "AED", "KWD", "QAR", "SGD", "HKD", "JPY", "TWD", "USD", "CAD"};
                    
                    for (String code : targetCurrencies) {
                        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("\"" + code + "\"\\s*:\\s*([0-9.]+)");
                        java.util.regex.Matcher matcher = pattern.matcher(json);
                        
                        if (matcher.find()) {
                            double rateFromPhp = Double.parseDouble(matcher.group(1));
                            // Foreign Unit to PHP  1 USD = ~57 PHP
                            if (rateFromPhp > 0) {
                                fetchedRates.put(code, 1.0 / rateFromPhp);
                            }
                        }
                    }
                    success = !fetchedRates.isEmpty();
                }
            }
        } catch (Exception e) {
            logger.warning("API connection failed. Defaulting to offline rates: " + e.getMessage());
        }

        final boolean apiSuccess = success;
        javax.swing.SwingUtilities.invokeLater(() -> {
            if (apiSuccess) {
                EXCHANGE_RATES.putAll(fetchedRates);
                lastFetchTime = System.currentTimeMillis();
            } else if (EXCHANGE_RATES.isEmpty()) {
                loadFallbackRates();
            }
            populateRateTable();
            updateEstimatedTotal();
        });
    }).start(); 
}
        public fexchangeFrame() {
            initComponents();
            setLocationRelativeTo(null);
            
            // --- ADD THIS TO MAKE TABLE TRANSPARENT ---
            
            // 2. Make the table and all the rows invisible
            jTable1.setOpaque(false);
            jTable1.setBackground(new java.awt.Color(0, 0, 0, 0));
            ((javax.swing.table.DefaultTableCellRenderer) jTable1.getDefaultRenderer(Object.class)).setOpaque(false);
            ((javax.swing.table.DefaultTableCellRenderer) jTable1.getDefaultRenderer(Object.class)).setBackground(new java.awt.Color(0, 0, 0, 0));

            // 3. Make the top header row invisible
            jTable1.getTableHeader().setOpaque(false);
            jTable1.getTableHeader().setBackground(new java.awt.Color(0, 0, 0, 0));
            ((javax.swing.table.DefaultTableCellRenderer) jTable1.getTableHeader().getDefaultRenderer()).setOpaque(false);
            ((javax.swing.table.DefaultTableCellRenderer) jTable1.getTableHeader().getDefaultRenderer()).setBackground(new java.awt.Color(0, 0, 0, 0));
            // ------------------------------------------

            QueueDatabase.initialize();
            applyRealTimeValidation();
            
            // loads lng if offline
            if (EXCHANGE_RATES.isEmpty()) {
                loadFallbackRates();
            }
            populateRateTable();
            
            updateRatesusingAPI();
            
            txtForeignAmount.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { updateEstimatedTotal(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { updateEstimatedTotal(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { updateEstimatedTotal(); }
            });

            jComboBox1.addActionListener(evt -> updateEstimatedTotal());
            
        }

        /** Fills the reference exchange-rate table shown on the left panel. */
        private void populateRateTable() {
            javax.swing.table.DefaultTableModel model =
                    (javax.swing.table.DefaultTableModel) jTable1.getModel();
            model.setRowCount(0);
            
            for (int i = 0; i < jComboBox1.getItemCount(); i++) {
                String entry = jComboBox1.getItemAt(i);
                
                // Grabs the short code (e.g., "SAR", "JPY")
                String code = extractCurrencyCode(entry); 
                if (code == null) continue;
                
                Double rate = EXCHANGE_RATES.get(code);
                if (rate == null) continue;
                
                String country = entry;
                
                // Grabs just the country name from the combo box text
                if (entry.contains(" - ") && entry.contains("(")) {
                    country = entry.substring(entry.indexOf(" - ") + 3, entry.indexOf(" (")).trim();
                }
                
                // --- QUICK FIXES FOR TEXT DISPLAY ---
                if (country.equals("Saudi Arabia")) {
                    country = "Saudi";
                } else if (country.equals("United Arab Emirates")) {
                    country = "UAE";
                }
                
                if (code.equals("JPY")) {
                    code = "YEN";
                }
                // ------------------------------------
                
                // Adds the Cleaned Country, Code, and Rate to the table
                model.addRow(new Object[]{country, code, String.format("%,.2f", rate)});
            }
        }
        
            private void updateEstimatedTotal() {
            Object currencySelection = jComboBox1.getSelectedItem();
            String amountText = txtForeignAmount.getText().trim();

            String code = currencySelection == null ? null : extractCurrencyCode(currencySelection.toString());
            Double rate = code == null ? null : EXCHANGE_RATES.get(code);

            if (rate == null || !amountText.matches("\\d{1,12}(\\.\\d{1,2})?")) {
                lblPhpTotal.setText("₱ 0.00 PHP");
                return;
            }

            double amount = Double.parseDouble(amountText);
            double phpTotal = amount * rate;
            lblPhpTotal.setText( String.format("%,.2f", phpTotal) + " PHP");
        }

        /** Pulls the 3-letter currency code out of a combo box entry like "USD - United States (US Dollar)". */
        private String extractCurrencyCode(String comboEntry) {
            if (comboEntry == null || comboEntry.trim().isEmpty()) {
                return null;
            }
            String trimmed = comboEntry.trim();
            int dashIndex = trimmed.indexOf(" - ");
            String code = dashIndex > 0 ? trimmed.substring(0, dashIndex) : trimmed;
            return code.trim();
        }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jComboBox2 = new javax.swing.JComboBox<>();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        label = new javax.swing.JLabel();
        clearbtn = new javax.swing.JButton();
        btnGetTicket = new javax.swing.JButton();
        lblPhpTotal = new javax.swing.JLabel();
        jButton1 = new javax.swing.JButton();
        chkPriority = new javax.swing.JCheckBox();
        jLabel5 = new javax.swing.JLabel();
        jLabel6 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox<>();
        txtCustomerName = new javax.swing.JTextField();
        txtForeignAmount = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Cornucopia Union - Foreign Exchange Kiosk");
        getContentPane().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jPanel3.setBackground(new java.awt.Color(244, 246, 249));
        jPanel3.setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        jLabel2.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel2.setText("EXCHANGE RATES (TO PHP)");
        jPanel3.add(jLabel2, new org.netbeans.lib.awtextra.AbsoluteConstraints(70, 110, -1, -1));

        jTable1.setFont(new java.awt.Font("Segoe UI", 1, 18)); // NOI18N
        jTable1.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Country ", "Currency", "Rate (PHP)"
            }
        ));
        jTable1.setOpaque(false);
        jTable1.setRowHeight(28);
        jScrollPane1.setViewportView(jTable1);

        jPanel3.add(jScrollPane1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 180, 380, 400));

        label.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        label.setText("Customer Name:");
        jPanel3.add(label, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 170, -1, -1));

        clearbtn.setBackground(new java.awt.Color(220, 38, 38));
        clearbtn.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        clearbtn.setForeground(new java.awt.Color(255, 255, 255));
        clearbtn.setText("CLEAR");
        clearbtn.addActionListener(this::clearbtnActionPerformed);
        jPanel3.add(clearbtn, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 510, 120, 40));

        btnGetTicket.setBackground(new java.awt.Color(12, 35, 74));
        btnGetTicket.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        btnGetTicket.setForeground(new java.awt.Color(255, 255, 255));
        btnGetTicket.setText("CONFIRM");
        btnGetTicket.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(253, 220, 92), 3));
        btnGetTicket.addActionListener(this::btnGetTicketActionPerformed);
        jPanel3.add(btnGetTicket, new org.netbeans.lib.awtextra.AbsoluteConstraints(420, 510, 120, 40));

        lblPhpTotal.setFont(new java.awt.Font("Segoe UI", 1, 20)); // NOI18N
        lblPhpTotal.setText(" 0.00 PHP");
        jPanel3.add(lblPhpTotal, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 400, -1, -1));

        jButton1.setBackground(new java.awt.Color(0, 0, 204));
        jButton1.setFont(new java.awt.Font("Lucida Bright", 1, 14)); // NOI18N
        jButton1.setForeground(new java.awt.Color(255, 255, 255));
        jButton1.setText("BACK");
        jButton1.addActionListener(this::jButton1ActionPerformed);
        jPanel3.add(jButton1, new org.netbeans.lib.awtextra.AbsoluteConstraints(610, 560, 120, 40));

        chkPriority.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        chkPriority.setText("Priority Line (Senior / PWD / Pregnant)");
        jPanel3.add(chkPriority, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 430, 450, 80));

        jLabel5.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel5.setText("Amount in Foreign Currency:");
        jPanel3.add(jLabel5, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 300, -1, -1));

        jLabel6.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel6.setText("Estimated Amount in Philippine Peso (PHP):");
        jPanel3.add(jLabel6, new org.netbeans.lib.awtextra.AbsoluteConstraints(400, 370, -1, 20));

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "SAR - Saudi Arabia (Saudi Riyal)", "AED - United Arab Emirates (UAE Dirham)", "KWD - Kuwait (Kuwaiti Dinar)", "QAR - Qatar (Qatari Riyal)", "SGD - Singapore (Singapore Dollar)", "HKD - Hong Kong (Hong Kong Dollar)", "JPY - Japan (Japanese Yen)", "TWD - Taiwan (New Taiwan Dollar)", "USD - United States (US Dollar)", "CAD - Canada (Canadian Dollar)" }));
        jComboBox1.addActionListener(this::jComboBox1ActionPerformed);
        jPanel3.add(jComboBox1, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 260, 220, 30));
        jPanel3.add(txtCustomerName, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 195, 220, 30));

        txtForeignAmount.addActionListener(this::txtForeignAmountActionPerformed);
        jPanel3.add(txtForeignAmount, new org.netbeans.lib.awtextra.AbsoluteConstraints(440, 330, 220, 30));

        jLabel4.setFont(new java.awt.Font("Segoe UI", 1, 14)); // NOI18N
        jLabel4.setText("Select Foreign Currency:");
        jPanel3.add(jLabel4, new org.netbeans.lib.awtextra.AbsoluteConstraints(410, 230, -1, -1));

        jLabel1.setIcon(new javax.swing.ImageIcon("C:\\Users\\Kijetsu\\Documents\\NetBeansProjects\\CornucopiaUnion\\src\\main\\java\\Images\\Foreign Exchange.png")); // NOI18N
        jLabel1.setText("jLabel1");
        jPanel3.add(jLabel1, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, 780, -1));

        getContentPane().add(jPanel3, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 1, 1000, 600));

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
         KioskFrame kkkiosk = new  KioskFrame();
         
         kkkiosk.setVisible(true);
         
         this.dispose();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void clearbtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearbtnActionPerformed
        // TODO add your handling code here:
       
    txtCustomerName.setText("");
    txtForeignAmount.setText("");

    
    if (jComboBox1.getItemCount() > 0) {
        jComboBox1.setSelectedIndex(0);
    }

    
    lblPhpTotal.setText("₱ 0.00 PHP");

    
    chkPriority.setSelected(false);
    }//GEN-LAST:event_clearbtnActionPerformed

    private void btnGetTicketActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnGetTicketActionPerformed
        // TODO add your handling code here:
        String customerName = txtCustomerName.getText().trim();
        Object currencySelection = jComboBox1.getSelectedItem();
        String amountText = txtForeignAmount.getText().trim();

        if (!ValidationUtils.isValidName(customerName)) {
            ValidationUtils.showError(this,
                    "Please enter a valid customer name (letters only).");
            txtCustomerName.requestFocusInWindow();
            return;
        }

        if (!ValidationUtils.isValidComboSelection(currencySelection)) {
            ValidationUtils.showError(this, "Please select a foreign currency.");
            jComboBox1.requestFocusInWindow();
            return;
        }

        String code = extractCurrencyCode(currencySelection.toString());
        Double rate = code == null ? null : EXCHANGE_RATES.get(code);
        if (rate == null) {
            ValidationUtils.showError(this,
                    "Exchange rate unavailable for the selected currency. Please choose another.");
            jComboBox1.requestFocusInWindow();
            return;
        }

        if (!ValidationUtils.isValidAmountFormat(amountText)) {
            ValidationUtils.showError(this,
                    "Please enter a valid foreign currency amount (numbers only, e.g. 100 or 100.50).");
            txtForeignAmount.requestFocusInWindow();
            return;
        }

        Double amount = ValidationUtils.parseAmount(amountText);
        if (amount == null || amount <= 0) {
            ValidationUtils.showError(this, "Amount must be greater than zero.");
            txtForeignAmount.requestFocusInWindow();
            return;
        }

        double phpTotal = amount * rate;
        lblPhpTotal.setText("₱ " + String.format("%,.2f", phpTotal) + " PHP");

        String ticket = QueueDatabase.addTicket("FX", "Foreign Exchange", customerName, chkPriority.isSelected());
        QueueDatabase.saveKioskData(ticket, code, amount);
        ValidationUtils.showSuccess(this,
                "Queue ticket issued!\n"
                + "Queue ticket: " + ticket + "\n"
                + "Customer: " + customerName + "\n"
                + "Converting: " + String.format("%,.2f", amount) + " " + code + "\n"
                + "Estimated PHP: " + lblPhpTotal.getText() + "\n"
                + "Priority: " + (chkPriority.isSelected() ? "Yes" : "No"));
    }//GEN-LAST:event_btnGetTicketActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void txtForeignAmountActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_txtForeignAmountActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_txtForeignAmountActionPerformed
    
    private void applyRealTimeValidation() {
        // Foreign Amount: Max 6 digits, whole numbers only (no decimals)
        txtForeignAmount.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                if (!Character.isDigit(evt.getKeyChar()) || txtForeignAmount.getText().length() >= 6) evt.consume();
            }
        });
    }
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
        java.awt.EventQueue.invokeLater(() -> new fexchangeFrame().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnGetTicket;
    private javax.swing.JCheckBox chkPriority;
    private javax.swing.JButton clearbtn;
    private javax.swing.JButton jButton1;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTable jTable1;
    private javax.swing.JLabel label;
    private javax.swing.JLabel lblPhpTotal;
    private javax.swing.JTextField txtCustomerName;
    private javax.swing.JTextField txtForeignAmount;
    // End of variables declaration//GEN-END:variables
}
