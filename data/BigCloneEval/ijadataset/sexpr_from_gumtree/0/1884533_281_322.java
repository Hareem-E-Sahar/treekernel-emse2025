(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(SimpleType(SimpleName:HttpURLConnection))(SimpleName:getConnection)(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:String))(SimpleName:contentType))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:trace)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(IfStatement(InfixExpression(SimpleName:serverURL)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(Block(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:IllegalStateException))(StringLiteral:<STR>)))))(VariableDeclarationStatement(SimpleType(SimpleName:HttpURLConnection))(VariableDeclarationFragment(SimpleName:connection)(NullLiteral)))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:URL))(VariableDeclarationFragment(SimpleName:url)(ClassInstanceCreation(SimpleType(SimpleName:URL))(SimpleName:serverURL))))(ExpressionStatement(Assignment(SimpleName:connection)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:HttpURLConnection))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:url))(SimpleName:openConnection)))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:encoding)(ClassInstanceCreation(SimpleType(SimpleName:String))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Base64))(SimpleName:encodeBase64)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(ParenthesizedExpression(InfixExpression(SimpleName:merchantId)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(SimpleName:merchantKey))))(SimpleName:getBytes)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:connection))(SimpleName:setRequestProperty)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:encoding)))))(IfStatement(SimpleName:acceptAllCertificates)(Block(IfStatement(InstanceofExpression(SimpleName:connection)(SimpleType(SimpleName:HttpsURLConnection)))(Block(VariableDeclarationStatement(SimpleType(SimpleName:HttpsURLConnection))(VariableDeclarationFragment(SimpleName:sslConnection)(CastExpression(SimpleType(SimpleName:HttpsURLConnection))(SimpleName:connection))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:sslConnection))(SimpleName:setHostnameVerifier)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:HostnameVerifier))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:boolean)(SimpleName:verify)(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:String))(SimpleName:hostName))(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:SSLSession))(SimpleName:session))(Block(ReturnStatement(BooleanLiteral:true))))))))))))(Block(IfStatement(InfixExpression(SimpleName:clientSocketFactory)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(IfStatement(InstanceofExpression(SimpleName:connection)(SimpleType(SimpleName:HttpsURLConnection)))(Block(VariableDeclarationStatement(SimpleType(SimpleName:HttpsURLConnection))(VariableDeclarationFragment(SimpleName:sslConnection)(CastExpression(SimpleType(SimpleName:HttpsURLConnection))(SimpleName:connection))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:sslConnection))(SimpleName:setSSLSocketFactory)(METHOD_INVOCATION_ARGUMENTS(SimpleName:clientSocketFactory))))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:connection))(SimpleName:setDoOutput)(METHOD_INVOCATION_ARGUMENTS(BooleanLiteral:true))))(IfStatement(InfixExpression(SimpleName:contentType)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:connection))(SimpleName:setRequestProperty)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(SimpleName:contentType)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:RuntimeException))(StringLiteral:<STR>)(SimpleName:e))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:trace)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(IfStatement(InfixExpression(SimpleName:connection)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(Block(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:RuntimeException))(StringLiteral:<STR>)))))(ReturnStatement(SimpleName:connection))))))