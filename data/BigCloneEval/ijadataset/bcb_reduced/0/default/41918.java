import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;

public class IDE extends javax.swing.JFrame {

    /** Creates new form IDE */
    public IDE() {
        initIDE();
        initComponents();
    }

    private void initComponents() {
        jScrollPane1 = new javax.swing.JScrollPane();
        display_code_textarea = new javax.swing.JTextArea();
        edit_code_panel = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        edit_code_textarea = new javax.swing.JTextArea();
        jButton1 = new javax.swing.JButton();
        Append = new javax.swing.JButton();
        fontPlus = new javax.swing.JButton();
        jButton6 = new javax.swing.JButton();
        jLabel5 = new javax.swing.JLabel();
        jScrollPane5 = new javax.swing.JScrollPane();
        jPanel2 = new javax.swing.JPanel();
        rolesPanel = new javax.swing.JPanel();
        messagesPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        component_description_pane = new javax.swing.JTextPane();
        jLabel6 = new javax.swing.JLabel();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        clausesPanel = new javax.swing.JPanel();
        variablesPanel = new javax.swing.JPanel();
        project_control_pane = new javax.swing.JPanel();
        save_project_button = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        project_description_pane = new javax.swing.JTextPane();
        jLabel10 = new javax.swing.JLabel();
        run_project_button = new javax.swing.JButton();
        new_Project_button = new javax.swing.JButton();
        load_project_button = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        new_role_name_txt = new javax.swing.JTextField();
        new_role_id_txt = new javax.swing.JTextField();
        create_new_role_button = new javax.swing.JButton();
        new_role_comment = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        create_new_clause_button = new javax.swing.JButton();
        new_clause_name_txt = new javax.swing.JTextField();
        new_clause_comment_txt = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        create_new_variable_button = new javax.swing.JButton();
        new_variable_name_txt = new javax.swing.JTextField();
        new_variable_comment_txt = new javax.swing.JTextField();
        new_variable_type_txt = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        new_message_name_txt = new javax.swing.JTextField();
        create_new_message_button = new javax.swing.JButton();
        new_message_comment_txt = new javax.swing.JTextField();
        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("IDE");
        addWindowListener(new java.awt.event.WindowAdapter() {

            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        display_code_textarea.setColumns(20);
        display_code_textarea.setRows(5);
        display_code_textarea.setDoubleBuffered(true);
        jScrollPane1.setViewportView(display_code_textarea);
        edit_code_panel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        edit_code_textarea.setColumns(20);
        edit_code_textarea.setFont(new java.awt.Font("Courier", 0, 18));
        edit_code_textarea.setLineWrap(true);
        edit_code_textarea.setRows(5);
        edit_code_textarea.addKeyListener(new java.awt.event.KeyAdapter() {

            public void keyPressed(java.awt.event.KeyEvent evt) {
                edit_code_textareaKeyPressed(evt);
            }

            public void keyTyped(java.awt.event.KeyEvent evt) {
                edit_code_textareaKeyTyped(evt);
            }
        });
        jScrollPane4.setViewportView(edit_code_textarea);
        jButton1.setText("Execute");
        jButton1.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        Append.setText("Append");
        Append.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AppendActionPerformed(evt);
            }
        });
        fontPlus.setText("font +");
        fontPlus.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fontPlusActionPerformed(evt);
            }
        });
        jButton6.setText("font -");
        org.jdesktop.layout.GroupLayout edit_code_panelLayout = new org.jdesktop.layout.GroupLayout(edit_code_panel);
        edit_code_panel.setLayout(edit_code_panelLayout);
        edit_code_panelLayout.setHorizontalGroup(edit_code_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(edit_code_panelLayout.createSequentialGroup().addContainerGap().add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 1032, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(edit_code_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(Append, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE).add(jButton1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE).add(edit_code_panelLayout.createSequentialGroup().add(fontPlus, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jButton6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 137, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        edit_code_panelLayout.setVerticalGroup(edit_code_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(edit_code_panelLayout.createSequentialGroup().addContainerGap().add(edit_code_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jScrollPane4, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 86, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(edit_code_panelLayout.createSequentialGroup().add(jButton1).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(Append).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(edit_code_panelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(fontPlus).add(jButton6)))).addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)));
        jLabel5.setFont(new java.awt.Font("Tahoma", 0, 36));
        jLabel5.setText("Unreal LCC IDE v1.0");
        jPanel2.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        rolesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        rolesPanel.setAutoscrolls(true);
        rolesPanel.setFont(new java.awt.Font("Tahoma", 0, 8));
        messagesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        messagesPanel.setAutoscrolls(true);
        messagesPanel.setFont(new java.awt.Font("Tahoma", 0, 8));
        jScrollPane2.setViewportView(component_description_pane);
        jLabel6.setText("ROLES");
        jLabel6.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel7.setText("VARIABLES");
        jLabel7.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel8.setText("MESSAGES");
        jLabel8.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jLabel9.setText("CLAUSES");
        jLabel9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        clausesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        clausesPanel.setAutoscrolls(true);
        clausesPanel.setFont(new java.awt.Font("Tahoma", 0, 8));
        variablesPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        variablesPanel.setAutoscrolls(true);
        variablesPanel.setFont(new java.awt.Font("Tahoma", 0, 8));
        project_control_pane.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        save_project_button.setText("Save");
        save_project_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                save_project_buttonActionPerformed(evt);
            }
        });
        jScrollPane3.setViewportView(project_description_pane);
        jLabel10.setFont(new java.awt.Font("Tahoma", 0, 14));
        jLabel10.setText("PROJECT");
        run_project_button.setText("Run");
        run_project_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                run_project_buttonActionPerformed(evt);
            }
        });
        new_Project_button.setText("New");
        new_Project_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                new_Project_buttonActionPerformed(evt);
            }
        });
        load_project_button.setText("Load");
        load_project_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                load_project_buttonActionPerformed(evt);
            }
        });
        org.jdesktop.layout.GroupLayout project_control_paneLayout = new org.jdesktop.layout.GroupLayout(project_control_pane);
        project_control_pane.setLayout(project_control_paneLayout);
        project_control_paneLayout.setHorizontalGroup(project_control_paneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(project_control_paneLayout.createSequentialGroup().addContainerGap().add(project_control_paneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(project_control_paneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane3).add(org.jdesktop.layout.GroupLayout.LEADING, project_control_paneLayout.createSequentialGroup().add(run_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(load_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 72, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(save_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 74, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(new_Project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).add(jLabel10)).addContainerGap(1222, Short.MAX_VALUE)));
        project_control_paneLayout.setVerticalGroup(project_control_paneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(project_control_paneLayout.createSequentialGroup().addContainerGap().add(jLabel10).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(project_control_paneLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(run_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(load_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(save_project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 47, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(new_Project_button, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 48, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jScrollPane3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addContainerGap(50, Short.MAX_VALUE)));
        jPanel3.setBorder(new javax.swing.border.LineBorder(new java.awt.Color(0, 0, 0), 1, true));
        jPanel4.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel1.setText("CREATE NEW ROLE");
        new_role_name_txt.setText("Name");
        new_role_name_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_role_name_txtMouseClicked(evt);
            }
        });
        new_role_id_txt.setText("Id");
        new_role_id_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_role_id_txtMouseClicked(evt);
            }
        });
        create_new_role_button.setText("OK");
        create_new_role_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_new_role_buttonActionPerformed(evt);
            }
        });
        new_role_comment.setText("Comment");
        new_role_comment.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_role_commentMouseClicked(evt);
            }
        });
        org.jdesktop.layout.GroupLayout jPanel4Layout = new org.jdesktop.layout.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap().add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel1).add(jPanel4Layout.createSequentialGroup().add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(org.jdesktop.layout.GroupLayout.LEADING, new_role_id_txt).add(org.jdesktop.layout.GroupLayout.LEADING, new_role_comment).add(org.jdesktop.layout.GroupLayout.LEADING, new_role_name_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 154, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(17, 17, 17).add(create_new_role_button)))));
        jPanel4Layout.setVerticalGroup(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel4Layout.createSequentialGroup().addContainerGap().add(jLabel1).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel4Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(new_role_name_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(org.jdesktop.layout.GroupLayout.TRAILING, new_role_comment, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(org.jdesktop.layout.GroupLayout.BASELINE, new_role_id_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(org.jdesktop.layout.GroupLayout.BASELINE, create_new_role_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 445, Short.MAX_VALUE)).addContainerGap()));
        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        create_new_clause_button.setText("OK");
        create_new_clause_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_new_clause_buttonActionPerformed(evt);
            }
        });
        new_clause_name_txt.setText("Name");
        new_clause_name_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_clause_name_txtMouseClicked(evt);
            }
        });
        new_clause_comment_txt.setText("Comment");
        new_clause_comment_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_clause_comment_txtMouseClicked(evt);
            }
        });
        jLabel4.setText("CREATE NEW CLAUSE");
        org.jdesktop.layout.GroupLayout jPanel5Layout = new org.jdesktop.layout.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createSequentialGroup().addContainerGap().add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel4).add(jPanel5Layout.createSequentialGroup().add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(new_clause_comment_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.TRAILING, new_clause_name_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1030, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(create_new_clause_button))).addContainerGap()));
        jPanel5Layout.setVerticalGroup(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5Layout.createSequentialGroup().addContainerGap().add(jLabel4).add(17, 17, 17).add(jPanel5Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(org.jdesktop.layout.GroupLayout.TRAILING, create_new_clause_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel5Layout.createSequentialGroup().add(new_clause_name_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(new_clause_comment_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jPanel6.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        create_new_variable_button.setText("OK");
        create_new_variable_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_new_variable_buttonActionPerformed(evt);
            }
        });
        new_variable_name_txt.setText("Name");
        new_variable_name_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_variable_name_txtMouseClicked(evt);
            }
        });
        new_variable_comment_txt.setText("Comment");
        new_variable_comment_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_variable_comment_txtMouseClicked(evt);
            }
        });
        new_variable_type_txt.setText("Type");
        new_variable_type_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_variable_type_txtMouseClicked(evt);
            }
        });
        jLabel2.setText("CREATE NEW VARIABLE");
        org.jdesktop.layout.GroupLayout jPanel6Layout = new org.jdesktop.layout.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel6Layout.createSequentialGroup().addContainerGap().add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel2).add(new_variable_name_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE).add(new_variable_type_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE).add(new_variable_comment_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 116, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(create_new_variable_button).addContainerGap()));
        jPanel6Layout.setVerticalGroup(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel6Layout.createSequentialGroup().addContainerGap().add(jLabel2).add(14, 14, 14).add(jPanel6Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING).add(org.jdesktop.layout.GroupLayout.LEADING, create_new_variable_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 77, Short.MAX_VALUE).add(org.jdesktop.layout.GroupLayout.LEADING, jPanel6Layout.createSequentialGroup().add(new_variable_name_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(new_variable_type_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(9, 9, 9).add(new_variable_comment_txt, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 22, Short.MAX_VALUE))).addContainerGap()));
        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jLabel3.setText("CREATE NEW MESSAGE");
        new_message_name_txt.setText("Name");
        new_message_name_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_message_name_txtMouseClicked(evt);
            }
        });
        create_new_message_button.setText("OK");
        create_new_message_button.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                create_new_message_buttonActionPerformed(evt);
            }
        });
        new_message_comment_txt.setText("Comment");
        new_message_comment_txt.addMouseListener(new java.awt.event.MouseAdapter() {

            public void mouseClicked(java.awt.event.MouseEvent evt) {
                new_message_comment_txtMouseClicked(evt);
            }
        });
        org.jdesktop.layout.GroupLayout jPanel7Layout = new org.jdesktop.layout.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel7Layout.createSequentialGroup().addContainerGap().add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7Layout.createSequentialGroup().add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(new_message_comment_txt).add(new_message_name_txt)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 1002, Short.MAX_VALUE).add(create_new_message_button)).add(jLabel3)).addContainerGap()));
        jPanel7Layout.setVerticalGroup(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel7Layout.createSequentialGroup().addContainerGap().add(jLabel3).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel7Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(create_new_message_button, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 85, Short.MAX_VALUE).add(jPanel7Layout.createSequentialGroup().add(new_message_name_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 39, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(new_message_comment_txt, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel3Layout.createSequentialGroup().addContainerGap().add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 1126, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel5, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel6, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).addContainerGap()));
        jPanel3Layout.setVerticalGroup(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel3Layout.createSequentialGroup().addContainerGap().add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(jPanel5, 0, 491, Short.MAX_VALUE).add(jPanel4, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(jPanel6, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(jPanel7, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addContainerGap()));
        org.jdesktop.layout.GroupLayout jPanel2Layout = new org.jdesktop.layout.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().add(10, 10, 10).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 886, Short.MAX_VALUE).add(jPanel2Layout.createSequentialGroup().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jLabel6).add(messagesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 364, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jPanel2Layout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jLabel8)).add(jPanel2Layout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(rolesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 364, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(clausesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 516, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(variablesPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 516, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(jLabel9).add(jLabel7)))).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().add(724, 724, 724).add(project_control_pane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2Layout.createSequentialGroup().addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel3, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))).addContainerGap()));
        jPanel2Layout.setVerticalGroup(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(jPanel2Layout.createSequentialGroup().addContainerGap().add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel6).add(jLabel9)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false).add(clausesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(rolesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE).add(jLabel7).add(jLabel8)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jPanel2Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false).add(messagesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(variablesPanel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 791, Short.MAX_VALUE)).add(jPanel2Layout.createSequentialGroup().add(project_control_pane, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE).add(50, 50, 50).add(jPanel3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 631, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        jScrollPane5.setViewportView(jPanel2);
        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(1978, 1978, 1978).add(edit_code_panel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)).add(layout.createSequentialGroup().add(37, 37, 37).add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 3105, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED).add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(jLabel5).add(489, 489, 489)).add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup().add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 2263, Short.MAX_VALUE).addContainerGap()))));
        layout.setVerticalGroup(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING).add(layout.createSequentialGroup().add(jLabel5).add(138, 138, 138).add(jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 339, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)).add(layout.createSequentialGroup().add(jScrollPane5, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 583, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE).add(60, 60, 60).add(edit_code_panel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))).addContainerGap()));
        pack();
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {
        JOptionPane exit = new JOptionPane();
        exit.setOptionType(JOptionPane.YES_NO_OPTION);
        int returnVal = exit.showConfirmDialog(this, "Have you really had enough?", "Exit?", JOptionPane.YES_NO_OPTION);
        if (returnVal == JOptionPane.OK_OPTION) {
            System.exit(0);
        }
    }

    private void edit_code_textareaKeyPressed(java.awt.event.KeyEvent evt) {
        if (evt.equals(evt.VK_DELETE) || evt.equals(evt.VK_BACK_SPACE)) editCursorPos--;
    }

    private void edit_code_textareaKeyTyped(java.awt.event.KeyEvent evt) {
        editCursorPos++;
    }

    private void AppendActionPerformed(java.awt.event.ActionEvent evt) {
        appendCode();
    }

    private void fontPlusActionPerformed(java.awt.event.ActionEvent evt) {
        Font font = edit_code_panel.getFont();
        String text = edit_code_textarea.getText();
        edit_code_textarea.setText(null);
        float size = font.getSize();
        size += 2;
        java.awt.Font font2 = font.deriveFont(size);
        edit_code_panel.setFont(font2);
        System.out.println("Size" + edit_code_panel.getFont().getSize());
        edit_code_textarea.setText(text);
    }

    private void new_message_comment_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_clause_comment_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_role_commentMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void run_project_buttonActionPerformed(java.awt.event.ActionEvent evt) {
    }

    private void new_clause_name_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_variable_comment_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_variable_type_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_variable_name_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_message_name_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_role_id_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void new_role_name_txtMouseClicked(java.awt.event.MouseEvent evt) {
        textBoxSelected((JTextField) evt.getSource());
    }

    private void textBoxSelected(JTextField text) {
        text.selectAll();
    }

    private void create_new_clause_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        createNewClause();
    }

    private void create_new_variable_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        createNewVariable();
    }

    private void create_new_message_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        createNewMessage();
    }

    private void create_new_role_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        createNewRole();
    }

    private void save_project_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        saveProject();
    }

    private void new_Project_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        JOptionPane exit = new JOptionPane();
        exit.setOptionType(JOptionPane.YES_NO_OPTION);
        int returnVal = exit.showConfirmDialog(this, "Are you sure you want to discard the old project?", "New Project?", JOptionPane.YES_NO_OPTION);
        if (returnVal == JOptionPane.OK_OPTION) {
            newProject();
        }
    }

    private void load_project_buttonActionPerformed(java.awt.event.ActionEvent evt) {
        loadProject();
    }

    /**
   * @param args the command line arguments
   */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {
                new IDE().setVisible(true);
            }
        });
    }

    private javax.swing.JButton Append;

    private javax.swing.JPanel clausesPanel;

    private javax.swing.JTextPane component_description_pane;

    private javax.swing.JButton create_new_clause_button;

    private javax.swing.JButton create_new_message_button;

    private javax.swing.JButton create_new_role_button;

    private javax.swing.JButton create_new_variable_button;

    private javax.swing.JTextArea display_code_textarea;

    private javax.swing.JPanel edit_code_panel;

    private javax.swing.JTextArea edit_code_textarea;

    private javax.swing.JButton fontPlus;

    private javax.swing.JButton jButton1;

    private javax.swing.JButton jButton6;

    private javax.swing.JLabel jLabel1;

    private javax.swing.JLabel jLabel10;

    private javax.swing.JLabel jLabel2;

    private javax.swing.JLabel jLabel3;

    private javax.swing.JLabel jLabel4;

    private javax.swing.JLabel jLabel5;

    private javax.swing.JLabel jLabel6;

    private javax.swing.JLabel jLabel7;

    private javax.swing.JLabel jLabel8;

    private javax.swing.JLabel jLabel9;

    private javax.swing.JPanel jPanel2;

    private javax.swing.JPanel jPanel3;

    private javax.swing.JPanel jPanel4;

    private javax.swing.JPanel jPanel5;

    private javax.swing.JPanel jPanel6;

    private javax.swing.JPanel jPanel7;

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JScrollPane jScrollPane4;

    private javax.swing.JScrollPane jScrollPane5;

    private javax.swing.JButton load_project_button;

    private javax.swing.JPanel messagesPanel;

    private javax.swing.JButton new_Project_button;

    private javax.swing.JTextField new_clause_comment_txt;

    private javax.swing.JTextField new_clause_name_txt;

    private javax.swing.JTextField new_message_comment_txt;

    private javax.swing.JTextField new_message_name_txt;

    private javax.swing.JTextField new_role_comment;

    private javax.swing.JTextField new_role_id_txt;

    private javax.swing.JTextField new_role_name_txt;

    private javax.swing.JTextField new_variable_comment_txt;

    private javax.swing.JTextField new_variable_name_txt;

    private javax.swing.JTextField new_variable_type_txt;

    private javax.swing.JPanel project_control_pane;

    private javax.swing.JTextPane project_description_pane;

    private javax.swing.JPanel rolesPanel;

    private javax.swing.JButton run_project_button;

    private javax.swing.JButton save_project_button;

    private javax.swing.JPanel variablesPanel;

    public void updateDisplay() {
    }

    public void appendCode() {
        display_code_textarea.append(edit_code_textarea.getText());
    }

    public void insertText(String text, int pos) {
        display_code_textarea.insert(text, pos);
    }

    public void loadProject() {
        JFileChooser chooser = new JFileChooser();
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            newProject();
            if (debug > 4) System.out.println("You chose to open this file: " + chooser.getSelectedFile().getName());
            String filePath = chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName();
            BufferedReader rd;
            java.util.List lines = new LinkedList();
            File file;
            String line;
            try {
                rd = new BufferedReader(new FileReader(filePath));
                while ((line = rd.readLine()) != null) {
                    lines.add(line);
                }
                Iterator it = lines.iterator();
                while (it.hasNext()) {
                    line = (String) it.next();
                    parse(line);
                    display_code_textarea.append(line + "\n");
                    if (debug > 4) System.out.println(line);
                }
                rd.close();
            } catch (FileNotFoundException fnfe) {
                System.err.println("File not found: ");
                return;
            } catch (IOException ioe) {
                System.err.println("IO Exception reading file: ");
                System.exit(1);
            }
        }
    }

    public void saveProject() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save");
        chooser.setApproveButtonText("Save");
        int returnVal = chooser.showOpenDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                String filePath = chooser.getCurrentDirectory() + "/" + chooser.getSelectedFile().getName();
                File f = new File(filePath);
                PrintWriter out = new PrintWriter(new FileWriter(f));
                out.print(display_code_textarea.getText());
                out.close();
            } catch (IOException e) {
                System.err.println("IO Exception reading file: ");
                System.exit(1);
            }
        }
    }

    public void newProject() {
        editCursorPos = 0;
        display_code_textarea.setText(null);
        edit_code_textarea.setText(null);
        project_description_pane.setText(null);
        roleTable = new Hashtable();
        messageTable = new Hashtable();
        variableTable = new Hashtable();
        clauseTable = new Hashtable();
        roleCommentTable = new Hashtable();
        clauseCommentTable = new Hashtable();
        messageCommentTable = new Hashtable();
        variableCommentTable = new Hashtable();
        rolesPanel.removeAll();
        rolesPanel.revalidate();
        messagesPanel.removeAll();
        variablesPanel.removeAll();
        clausesPanel.removeAll();
    }

    public void createNewRole() {
        String role = new_role_name_txt.getText();
        String comment = new_role_comment.getText();
        addRole(role);
        addRoleComment(role, comment);
        display_code_textarea.append("%%%<role:" + role + ">" + comment + "\n");
        new_role_name_txt.setText("Name");
        new_role_comment.setText("Comment");
    }

    public void createNewMessage() {
        String message = new_message_name_txt.getText();
        String comment = new_message_comment_txt.getText();
        addMessage(message);
        addMessageComment(message, comment);
        display_code_textarea.append("%%%<message:" + message + ">" + comment + "\n");
        new_message_name_txt.setText("Name");
        new_message_comment_txt.setText("Comment");
    }

    public void createNewVariable() {
        String variable = new_variable_name_txt.getText();
        String comment = new_variable_comment_txt.getText();
        addVariable(variable);
        addVariableComment(variable, comment);
        display_code_textarea.append("%%%<vaiable:" + variable + ">" + comment + "\n");
        new_variable_name_txt.setText("Name");
        new_variable_comment_txt.setText("Comment");
    }

    public void createNewClause() {
        String clause = new_clause_name_txt.getText();
        String comment = new_clause_comment_txt.getText();
        addClause(clause);
        addClauseComment(clause, comment);
        display_code_textarea.append("%%%<clause:" + clause + ">" + comment + "\n");
        new_clause_name_txt.setText("Name");
        new_clause_comment_txt.setText("Comment");
    }

    public void parse(String line) {
        String[] temp;
        if (line.regionMatches(0, "%%%", 0, 3)) {
            temp = line.split(">");
            String[] tags = temp[0].split("<")[1].split(":");
            String comment = temp[1];
            if (debug > 3) System.out.println("Tag0:" + tags[0] + " Tag1:" + tags[1] + " Comment:" + comment);
            if (tags[0].equalsIgnoreCase("project")) {
                project_description_pane.setText(comment);
                setTitle(tags[1]);
            } else if (tags[0].equalsIgnoreCase("role")) addRoleComment(tags[1], comment);
        } else if (line.regionMatches(0, "%%", 0, 2)) {
            if (debug > 3) System.out.println("Comment found");
        } else if (line.contains("::")) {
            temp = line.split("::", 2);
            addRole(temp[0]);
            parse(temp[1]);
        } else if (line.contains("then")) {
            temp = line.split("then", 2);
            addClause(temp[0]);
            addClause(temp[1]);
            parse(temp[0]);
            parse(temp[1]);
        } else if (line.contains("<=")) {
            temp = line.split("<=", 2);
            addMessage(temp[0]);
            if (!temp[1].contains("<--")) addRole(temp[1]); else {
                String[] tmp = temp[1].split("<--", 2);
                addRole(tmp[0]);
                parse(tmp[1]);
            }
        } else if (line.contains("=>")) {
            temp = line.split("=>", 2);
            addMessage(temp[0]);
            if (!temp[1].contains("<--")) addRole(temp[1]); else {
                String[] tmp = temp[1].split("<--", 2);
                addRole(tmp[0]);
                parse(tmp[1]);
            }
        } else if (line.contains("and")) {
            temp = line.split("and", 2);
            parse(temp[0]);
            parse(temp[1]);
        } else if (!line.equalsIgnoreCase("")) addVariable(line);
    }

    public void addRole(String nR) {
        String newRole = nR.trim();
        JButton newButton = new javax.swing.JButton();
        if (!roleTable.containsKey(newRole)) {
            if (debug > 3) System.out.println("Creating new button for :" + newRole);
            newButton.setText(newRole);
            newButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    if (editCursorPos == 0) text += "::";
                    text += " ";
                    edit_code_textarea.insert(text, editCursorPos);
                    editCursorPos += text.length();
                }
            });
            newButton.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    component_description_pane.setText((String) roleCommentTable.get(text));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    component_description_pane.setText(null);
                }
            });
            roleTable.put(newRole, newButton);
            rolesPanel.add(newButton);
            rolesPanel.revalidate();
        } else if (debug > 4) System.out.println("Role already there");
    }

    public void addClause(String nC) {
        String newClause = nC.trim() + " ";
        JButton newButton = new javax.swing.JButton();
        if (!clauseTable.containsKey(newClause)) {
            if (debug > 3) System.out.println("Creating new button for :" + newClause);
            newButton.setText(newClause);
            newButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    edit_code_textarea.insert(text, editCursorPos);
                    editCursorPos += text.length();
                }
            });
            newButton.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    component_description_pane.setText((String) roleCommentTable.get(text));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    component_description_pane.setText(null);
                }
            });
            clauseTable.put(newClause, newButton);
            clausesPanel.add(newButton);
            clausesPanel.revalidate();
        } else if (debug > 4) System.out.println("Message already there");
    }

    public void addMessage(String nM) {
        String newMessage = nM.trim() + " ";
        JButton newButton = new javax.swing.JButton();
        if (!messageTable.containsKey(newMessage)) {
            if (debug > 3) System.out.println("Creating new button for :" + newMessage);
            newButton.setText(newMessage);
            newButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    edit_code_textarea.insert(text, editCursorPos);
                    editCursorPos += text.length();
                }
            });
            newButton.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    component_description_pane.setText((String) messageCommentTable.get(text));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    component_description_pane.setText(null);
                }
            });
            messageTable.put(newMessage, newButton);
            messagesPanel.add(newButton);
            messagesPanel.revalidate();
        } else if (debug > 4) System.out.println("Message already there");
    }

    public void addVariable(String nV) {
        String newVariable = nV.trim() + " ";
        JButton newButton = new javax.swing.JButton();
        if (!variableTable.containsKey(newVariable)) {
            if (debug > 3) System.out.println("Creating new button for :" + newVariable);
            newButton.setText(newVariable);
            newButton.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    edit_code_textarea.insert(text, editCursorPos);
                    editCursorPos += text.length();
                }
            });
            newButton.addMouseListener(new java.awt.event.MouseAdapter() {

                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    String text = ((JButton) evt.getSource()).getText();
                    component_description_pane.setText((String) variableCommentTable.get(text));
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    component_description_pane.setText(null);
                }
            });
            variableTable.put(newVariable, newButton);
            variablesPanel.add(newButton);
            variablesPanel.revalidate();
        } else if (debug > 4) System.out.println("Role already there");
    }

    public void addRoleComment(String role, String comment) {
        roleCommentTable.put(role, comment);
    }

    public void addMessageComment(String msg, String comment) {
        messageCommentTable.put(msg, comment);
    }

    public void addClauseComment(String clause, String comment) {
        clauseCommentTable.put(clause, comment);
    }

    public void addVariableComment(String var, String comment) {
        variableCommentTable.put(var, comment);
    }

    /**
   * BUG List:
   * 1) New project action does not remove buttons correctly or refresh the boxes
   * 2) Font +/- buttons does not cause a screen refresh
   * TODO list:
   * 3) Implement an Undo function
   * 4) Interface with the Unreal Launcher
   * 5) Add colours to the parsed code
   */
    public Hashtable roleTable;

    public Hashtable roleCommentTable;

    public Hashtable clauseTable;

    public Hashtable clauseCommentTable;

    public Hashtable messageTable;

    public Hashtable messageCommentTable;

    public Hashtable variableTable;

    public Hashtable variableCommentTable;

    public int debug = 0;

    public int editCursorPos = 0;

    public void initIDE() {
        roleTable = new Hashtable();
        roleCommentTable = new Hashtable();
        clauseCommentTable = new Hashtable();
        clauseTable = new Hashtable();
        messageCommentTable = new Hashtable();
        messageTable = new Hashtable();
        variableCommentTable = new Hashtable();
        variableTable = new Hashtable();
    }
}
