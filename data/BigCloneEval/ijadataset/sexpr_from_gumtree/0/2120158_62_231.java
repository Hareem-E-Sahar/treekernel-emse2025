(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(SimpleName:viewUserDetail)(SingleVariableDeclaration(SimpleType(SimpleName:Component))(SimpleName:parent))(SingleVariableDeclaration(Modifier:final)(PrimitiveType:char)(SimpleName:type))(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:String))(SimpleName:lauid))(Block(SuperConstructorInvocation(CastExpression(SimpleType(SimpleName:JDialog))(SimpleName:parent)))(ExpressionStatement(Assignment(SimpleName:viewuserdetail)(ASSIGNMENT_OPERATOR:=)(ThisExpression)))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:str)(StringLiteral:<STR>)))(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(ExpressionStatement(Assignment(SimpleName:str)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))))(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(ExpressionStatement(Assignment(SimpleName:str)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))))(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(ExpressionStatement(Assignment(SimpleName:str)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlLayout))(SimpleName:getDialogLayout)(METHOD_INVOCATION_ARGUMENTS(ThisExpression)(SimpleName:WIDTH)(SimpleName:HEIGHT)(SimpleName:str))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:setLayout)(METHOD_INVOCATION_ARGUMENTS(NullLiteral))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:setResizable)(METHOD_INVOCATION_ARGUMENTS(BooleanLiteral:false))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:setDefaultCloseOperation)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:JDialog.DO_NOTHING_ON_CLOSE))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:addWindowListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:WindowListener))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowClosing)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block(TryStatement(Block(IfStatement(InfixExpression(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(INFIX_EXPRESSION_OPERATOR:||)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showYesNoQuestionMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))))(INFIX_EXPRESSION_OPERATOR:==)(QualifiedName:JOptionPane.YES_OPTION)))(Block(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlDatabase))(SimpleName:executeQuery)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getConnection))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:lauid)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:dispose))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showErrorMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(SimpleName:ex)))))))))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowActivated)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowClosed)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowDeactivated)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowDeiconified)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowIconified)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:windowOpened)(SingleVariableDeclaration(SimpleType(SimpleName:WindowEvent))(SimpleName:e))(Block)))))))(VariableDeclarationStatement(SimpleType(SimpleName:JLabel))(VariableDeclarationFragment(SimpleName:lblID)(ClassInstanceCreation(SimpleType(SimpleName:JLabel))(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblID))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:10)(NumberLiteral:100)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblID))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:lblID))))(ExpressionStatement(Assignment(SimpleName:txtID)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JTextField)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:10)(NumberLiteral:100)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:setDocument)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:getInstance)))(SimpleName:getDefDoc)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:16))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:txtID))))(VariableDeclarationStatement(SimpleType(SimpleName:JLabel))(VariableDeclarationFragment(SimpleName:lblName)(ClassInstanceCreation(SimpleType(SimpleName:JLabel))(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblName))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:40)(NumberLiteral:100)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblName))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:lblName))))(ExpressionStatement(Assignment(SimpleName:txtName)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JTextField)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:40)(NumberLiteral:250)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:setDocument)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:getInstance)))(SimpleName:getDefDoc)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:50))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:txtName))))(VariableDeclarationStatement(SimpleType(SimpleName:JLabel))(VariableDeclarationFragment(SimpleName:lblVName)(ClassInstanceCreation(SimpleType(SimpleName:JLabel))(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblVName))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:70)(NumberLiteral:100)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblVName))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:lblVName))))(ExpressionStatement(Assignment(SimpleName:txtVName)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JTextField)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:70)(NumberLiteral:250)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:setDocument)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:getInstance)))(SimpleName:getDefDoc)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:30))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:txtVName))))(VariableDeclarationStatement(SimpleType(SimpleName:JLabel))(VariableDeclarationFragment(SimpleName:lblPWD)(ClassInstanceCreation(SimpleType(SimpleName:JLabel))(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblPWD))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:100)(NumberLiteral:100)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblPWD))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:lblPWD))))(ExpressionStatement(Assignment(SimpleName:txtPWD)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JPasswordField)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:100)(NumberLiteral:250)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:setDocument)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:getInstance)))(SimpleName:getDefDoc)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:32))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:setEchoChar)(METHOD_INVOCATION_ARGUMENTS(CharacterLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:addFocusListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:FocusListener))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:focusGained)(SingleVariableDeclaration(SimpleType(SimpleName:FocusEvent))(SimpleName:e))(Block(ExpressionStatement(Assignment(SimpleName:isPasswordChanged)(ASSIGNMENT_OPERATOR:=)(BooleanLiteral:true)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:setText)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:focusLost)(SingleVariableDeclaration(SimpleType(SimpleName:FocusEvent))(SimpleName:e))(Block)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:txtPWD))))(VariableDeclarationStatement(SimpleType(SimpleName:JLabel))(VariableDeclarationFragment(SimpleName:lblRight)(ClassInstanceCreation(SimpleType(SimpleName:JLabel))(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblRight))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:130)(NumberLiteral:130)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:lblRight))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:lblRight))))(ExpressionStatement(Assignment(SimpleName:cboRight)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JComboBox)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:cboRight))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:130)(NumberLiteral:250)(NumberLiteral:20))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:cboRight))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<=)(NumberLiteral:3))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:cboRight))(SimpleName:addItem)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:getClass)))(SimpleName:getSimpleName))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:i)(StringLiteral:<STR>)))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:cboRight))))(VariableDeclarationStatement(SimpleType(SimpleName:JButton))(VariableDeclarationFragment(SimpleName:btnOK)(ClassInstanceCreation(SimpleType(SimpleName:JButton))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnOK))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnOK))(SimpleName:addActionListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ActionListener))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:actionPerformed)(SingleVariableDeclaration(SimpleType(SimpleName:ActionEvent))(SimpleName:e))(Block(TryStatement(Block(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:dispose)))(ReturnStatement)))(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showYesNoQuestionMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))))(INFIX_EXPRESSION_OPERATOR:==)(QualifiedName:JOptionPane.YES_OPTION))(Block(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:getText)))(SimpleName:trim)))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showInformationMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:requestFocus)))(ReturnStatement)))(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:getText)))(SimpleName:trim)))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showInformationMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:requestFocus)))(ReturnStatement)))(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:String))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:getPassword))))(SimpleName:trim)))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showInformationMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:getClass)))(SimpleName:getSimpleName))(StringLiteral:<STR>)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:requestFocus)))(ReturnStatement)))(VariableDeclarationStatement(SimpleType(SimpleName:MessageDigest))(VariableDeclarationFragment(SimpleName:md)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:MessageDigest))(SimpleName:getInstance)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:digest)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:md))(SimpleName:digest)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:String))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtPWD))(SimpleName:getPassword))))(SimpleName:getBytes))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:pwd)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Base64))(SimpleName:encode)(METHOD_INVOCATION_ARGUMENTS(SimpleName:digest)))))(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(Block(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlDatabase))(SimpleName:dcount)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getConnection))(StringLiteral:<STR>)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:getText)))(SimpleName:trim)))(SimpleName:toUpperCase))(StringLiteral:<STR>))))(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showInformationMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:requestFocus)))(ReturnStatement)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlDatabase))(SimpleName:executeQuery)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getConnection))(InfixExpression(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(StringLiteral:<STR>))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:getText))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:getText)))(SimpleName:trim))(StringLiteral:<STR>)(ParenthesizedExpression(ConditionalExpression(ParenthesizedExpression(InfixExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText))(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:||)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText)))(SimpleName:trim)))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText))))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:cboRight))(SimpleName:getSelectedItem)))(SimpleName:toString)))(SimpleName:substring)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0)(NumberLiteral:1)))(StringLiteral:<STR>)(SimpleName:pwd)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getUser))(StringLiteral:<STR>)))))))(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlDatabase))(SimpleName:executeQuery)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getConnection))(InfixExpression(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtName))(SimpleName:getText))(StringLiteral:<STR>)(ParenthesizedExpression(ConditionalExpression(ParenthesizedExpression(InfixExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText))(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:||)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText)))(SimpleName:trim)))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtVName))(SimpleName:getText))))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:cboRight))(SimpleName:getSelectedItem)))(SimpleName:toString)))(SimpleName:substring)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0)(NumberLiteral:1)))(ParenthesizedExpression(ConditionalExpression(SimpleName:isPasswordChanged)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:pwd)(StringLiteral:<STR>))(StringLiteral:<STR>)))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getUser))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txtID))(SimpleName:getText)))(SimpleName:trim))(StringLiteral:<STR>)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:dispose))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showErrorMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(SimpleName:ex))))))))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnOK))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:10)(NumberLiteral:170)(NumberLiteral:110)(NumberLiteral:25))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:btnOK))))(ExpressionStatement(Assignment(SimpleName:btnAbbr)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:JButton))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnAbbr))(SimpleName:setFont)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getFont)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnAbbr))(SimpleName:addActionListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ActionListener))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:actionPerformed)(SingleVariableDeclaration(SimpleType(SimpleName:ActionEvent))(SimpleName:e))(Block(TryStatement(Block(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showYesNoQuestionMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlXML))(SimpleName:getInstance)))(SimpleName:getLanguageDataValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(StringLiteral:<STR>)))))(INFIX_EXPRESSION_OPERATOR:==)(QualifiedName:JOptionPane.YES_OPTION))(Block(IfStatement(InfixExpression(SimpleName:type)(INFIX_EXPRESSION_OPERATOR:==)(CharacterLiteral:<STR>))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlDatabase))(SimpleName:executeQuery)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlMain))(SimpleName:getConnection))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:lauid)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:viewuserdetail))(SimpleName:dispose))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showErrorMessage)(METHOD_INVOCATION_ARGUMENTS(SimpleName:viewuserdetail)(SimpleName:ex))))))))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:btnAbbr))(SimpleName:setBounds)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:130)(NumberLiteral:170)(NumberLiteral:110)(NumberLiteral:25))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:btnAbbr))))(ExpressionStatement(MethodInvocation(SimpleName:setDefaults)(METHOD_INVOCATION_ARGUMENTS(SimpleName:type)(SimpleName:lauid))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:setModal)(METHOD_INVOCATION_ARGUMENTS(BooleanLiteral:true))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ThisExpression))(SimpleName:setVisible)(METHOD_INVOCATION_ARGUMENTS(BooleanLiteral:true)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ctrlTools))(SimpleName:showErrorMessage)(METHOD_INVOCATION_ARGUMENTS(ThisExpression)(SimpleName:ex)))))))))))