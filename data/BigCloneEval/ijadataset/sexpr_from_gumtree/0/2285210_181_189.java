(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(SimpleType(SimpleName:HttpResponse))(SimpleName:execute)(SingleVariableDeclaration(SimpleType(SimpleName:HttpRequestBase))(SimpleName:requestBase))(SimpleType(SimpleName:IOException))(SimpleType(SimpleName:ClientProtocolException))(SimpleType(SimpleName:ProcessException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:HttpResponse))(VariableDeclarationFragment(SimpleName:res)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:client))(SimpleName:execute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:requestBase)))))(VariableDeclarationStatement(SimpleType(SimpleName:StatusLine))(VariableDeclarationFragment(SimpleName:statusLine)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:res))(SimpleName:getStatusLine))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:code)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:statusLine))(SimpleName:getStatusCode))))(IfStatement(InfixExpression(SimpleName:code)(INFIX_EXPRESSION_OPERATOR:>=)(QualifiedName:HttpStatus.SC_BAD_REQUEST))(Block(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:ProcessException))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:statusLine)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:requestBase))(SimpleName:getURI)))))))(ReturnStatement(SimpleName:res))))))