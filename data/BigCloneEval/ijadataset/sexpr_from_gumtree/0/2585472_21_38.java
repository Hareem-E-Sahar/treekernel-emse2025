(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:protected)(Modifier:static)(SimpleType(SimpleName:String))(SimpleName:getDocumentAt)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:urlString))(Block(VariableDeclarationStatement(SimpleType(SimpleName:StringBuffer))(VariableDeclarationFragment(SimpleName:html_text)(ClassInstanceCreation(SimpleType(SimpleName:StringBuffer)))))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:URL))(VariableDeclarationFragment(SimpleName:url)(ClassInstanceCreation(SimpleType(SimpleName:URL))(SimpleName:urlString))))(VariableDeclarationStatement(SimpleType(SimpleName:URLConnection))(VariableDeclarationFragment(SimpleName:conn)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:url))(SimpleName:openConnection))))(VariableDeclarationStatement(SimpleType(SimpleName:BufferedReader))(VariableDeclarationFragment(SimpleName:reader)(ClassInstanceCreation(SimpleType(SimpleName:BufferedReader))(ClassInstanceCreation(SimpleType(SimpleName:InputStreamReader))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:conn))(SimpleName:getInputStream))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:line)(NullLiteral)))(WhileStatement(InfixExpression(ParenthesizedExpression(Assignment(SimpleName:line)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:reader))(SimpleName:readLine))))(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:html_text))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(SimpleName:line)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:reader))(SimpleName:close)))(ExpressionStatement(Assignment(SimpleName:url)(ASSIGNMENT_OPERATOR:=)(NullLiteral)))(ExpressionStatement(Assignment(SimpleName:conn)(ASSIGNMENT_OPERATOR:=)(NullLiteral))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:MalformedURLException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(QualifiedName:System.out))(SimpleName:println)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:urlString)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))(ReturnStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:html_text))(SimpleName:toString)))))))