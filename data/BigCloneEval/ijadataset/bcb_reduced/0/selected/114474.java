package widgets;

/**
 *
 * @author razi
 */
public class help extends javax.swing.JFrame {

    /** Creates new form help */
    public help() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    private void initComponents() {
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextPane2 = new javax.swing.JTextPane();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane3 = new javax.swing.JTextPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        jTextPane4 = new javax.swing.JTextPane();
        setTitle("Help");
        jTextPane1.setBackground(new java.awt.Color(255, 255, 255));
        jTextPane1.setContentType("text/html");
        jTextPane1.setEditable(false);
        jTextPane1.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n      <h1>Basics</h1><hr/>\n\n<h2>Making blocks</h2>\n<p>You can create bloks from \"Block panel\" on the left (when there's no selected blocks), or from Pop-up menu (right click in canvas where are no blocks)</p>\n<p>In flowchart can be only one \"Start\" block.</p>\n<hr/>\n\n<h2>Connecting blocks</h2>\n<p>To connect blocks, select first block, then click next while holding CTRL. If there was other out-connection from first block, it will be removed.</p>\n<p>The exception is Decision block, which has 2 out-connections: \"true\" and \"false\". In this case will be removed oldest connection, where you try to connect third block. To replace \"true\" with \"false\" (and vice versa) select Decision block and press \"r\" (reverse)</p>\n<hr/>\n\n<h2>Group blocks</h2>\n<p>You can group blocks, that mean draw a rounded rectangle under blocks. To do this, select blocks, click right mouse button on one selected and click \"Group\"</p>\n<p>To select the rectangle, do double-click.</p>\n<p>To add or remove blocks from group, select the group and click block while holding CTRL</p>\n<hr/>\n\n<h2>\"var\" keyword</h2>\n<p>\"var\" is used to declare variables in JavaScript, but not in Python. In case of Python JavaBlock removes all \"var\".</p>\n<p>If variable is declared by using \"var\" it will be added to tracked variables in Simulation Panel (right).</p>\n<hr>\n\n<h3>DO NOT USE \";\"!!</h3>\n    </p>\n  </body>\n</html>\n");
        jScrollPane1.setViewportView(jTextPane1);
        jTabbedPane1.addTab("Basic", jScrollPane1);
        jTextPane2.setBackground(new java.awt.Color(255, 255, 255));
        jTextPane2.setContentType("text/html");
        jTextPane2.setEditable(false);
        jTextPane2.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n\n<h2>non-code based blocks</h2>\n<p>You can read/write data using non-code based blocks. You don't must know IO syntax, just use simple editor.</p>\n<p>To make non-code based IO block, unselect \"code based blocks\" from \"Block panel\" and/or pop-up menu.</p>\n<p>In editor you choose type of operation (input or output), variables names (separate by \", \")</p>\n<p>If Output you can write also expresions like <i>a*b</i></p>\n<p>If Input, you should choose type of data (float, integer, string, char array, logic)</p>\n<hr/>\n\n<h2>code based</h2>\n<ul>You must use these functions:\n<li><b>Read(\"message\")</b> - returns string</li>\n<li><b>ReadNumber(\"message\")</b> - returns float</li>\n<li><b>ReadInteger(\"message\")</b> - returns integer</li>\n<li><b>Write(\"message\")</b> - Writes a string</li>\n<li><b>Writeln(\"message\")</b> - Writes a string and goes to next line</li>\n</ul>\nex.:\n<pre>var a=ReadInteger(\"Type a number\")\nWriteln(\"The square of \"+a+\" is: \"+(a*a)+\".\")</pre>\n<p>In Python to make strings you must cast all variables to string:\n<pre>var a=ReadInteger(\"Type a number\")\nWriteln(\"The square of \"+<b>str(a)</b>+\" is: \"+<b>str(a*a)</b>+\".\")</pre>\n</p>\n<hr>\n\n<h2>\"Flowchart commands\"</h2>\n<p>Works only in IO block</p>\n<pre>read a\nwriteln (a*a)</pre>\n\n\n\n    </p>\n  </body>\n</html>\n");
        jScrollPane2.setViewportView(jTextPane2);
        jTabbedPane1.addTab("Input/Output", jScrollPane2);
        jTextPane3.setBackground(new java.awt.Color(255, 255, 255));
        jTextPane3.setContentType("text/html");
        jTextPane3.setEditable(false);
        jTextPane3.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n     \n<h2>Comparing</h2>\n<ul><p>In JavaScript you can compary only 2 values. You can compare using operators:\n<li> == - is equal</li>\n<li> != - is not equal</li>\n<li> &lt; - is lesser than</li>\n<li> &gt; - is bigger than</li>\n<li> &lt;= - is lesser or equal</li>\n<li> &gt;= - is bigger or equal</li></p>\n<p>To connect comparison use Logic Operators:\n<li>and (&&) - both sides must be true</li>\n<li>or (||) - at least one must be true</li></p>\n<p>In JavaScript engine you must use operators in (), in Python- english words. JavaBlocks replaces \"and\" to && and && to \"and\" depending on the selected ScriptEngine.\n</ul>\n<pre>a&gt;0 && a&lt;5</pre>\n\n<p>In Python you can use multiple comparisons:\n<pre>0&lt;a&lt;5</pre></p>\n    </p>\n  </body>\n</html>\n");
        jScrollPane3.setViewportView(jTextPane3);
        jTabbedPane1.addTab("Decision block", jScrollPane3);
        jTextPane4.setBackground(new java.awt.Color(255, 255, 255));
        jTextPane4.setContentType("text/html");
        jTextPane4.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n \n<h1>Structure block</h1>\n<p>RMP > Structure block</p>\n<h2>Making object:</h2>\n<p>To create object from structure:\n<pre>var obj=StructureName()</pre></p>\n<h2>Access to fields:</h2>\n<pre>obj.fieldName=4</pre>\n<pre>obj.setFieldName(4) //type-safe \"set\"</pre>\n<pre>obj.getFieldName(4) //type-safe \"get\"</pre>\n    </p>\n  </body>\n</html>\n");
        jScrollPane4.setViewportView(jTextPane4);
        jTabbedPane1.addTab("Structure block", jScrollPane4);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 444, Short.MAX_VALUE));
        layout.setVerticalGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING).addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE));
        jTabbedPane1.getAccessibleContext().setAccessibleName("Basic");
        pack();
    }

    /**
    * @param args the command line arguments
    */
    public static void showHelp() {
        new help().setVisible(true);
    }

    private javax.swing.JScrollPane jScrollPane1;

    private javax.swing.JScrollPane jScrollPane2;

    private javax.swing.JScrollPane jScrollPane3;

    private javax.swing.JScrollPane jScrollPane4;

    private javax.swing.JTabbedPane jTabbedPane1;

    private javax.swing.JTextPane jTextPane1;

    private javax.swing.JTextPane jTextPane2;

    private javax.swing.JTextPane jTextPane3;

    private javax.swing.JTextPane jTextPane4;
}
