(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:capture)(Block(ExpressionStatement(Assignment(SimpleName:filePath)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:filePath)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(SimpleName:fileType))))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:Toolkit))(VariableDeclarationFragment(SimpleName:toolkit)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Toolkit))(SimpleName:getDefaultToolkit))))(VariableDeclarationStatement(SimpleType(SimpleName:Dimension))(VariableDeclarationFragment(SimpleName:screenSize)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:toolkit))(SimpleName:getScreenSize))))(VariableDeclarationStatement(SimpleType(SimpleName:Rectangle))(VariableDeclarationFragment(SimpleName:screenRect)(ClassInstanceCreation(SimpleType(SimpleName:Rectangle))(SimpleName:screenSize))))(VariableDeclarationStatement(SimpleType(SimpleName:Robot))(VariableDeclarationFragment(SimpleName:robot)(ClassInstanceCreation(SimpleType(SimpleName:Robot)))))(VariableDeclarationStatement(SimpleType(SimpleName:BufferedImage))(VariableDeclarationFragment(SimpleName:image)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:robot))(SimpleName:createScreenCapture)(METHOD_INVOCATION_ARGUMENTS(SimpleName:screenRect)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ImageIO))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:image)(SimpleName:fileType)(ClassInstanceCreation(SimpleType(SimpleName:File))(SimpleName:filePath))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(QualifiedName:System.out))(SimpleName:println)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:ex))))))))))))