(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(SimpleType(SimpleName:String))(SimpleName:generateDigest)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:password))(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:saltHex))(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:algorithm))(SimpleType(SimpleName:NoSuchAlgorithmException))(Block(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:algorithm))(SimpleName:equalsIgnoreCase)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(Block(ReturnStatement(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:UnixCrypt))(SimpleName:crypt)(METHOD_INVOCATION_ARGUMENTS(SimpleName:password))))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:algorithm))(SimpleName:equalsIgnoreCase)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(Block(ExpressionStatement(Assignment(SimpleName:algorithm)(ASSIGNMENT_OPERATOR:=)(StringLiteral:<STR>))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:algorithm))(SimpleName:equalsIgnoreCase)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(Block(ExpressionStatement(Assignment(SimpleName:algorithm)(ASSIGNMENT_OPERATOR:=)(StringLiteral:<STR>)))))))(VariableDeclarationStatement(SimpleType(SimpleName:MessageDigest))(VariableDeclarationFragment(SimpleName:msgDigest)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:MessageDigest))(SimpleName:getInstance)(METHOD_INVOCATION_ARGUMENTS(SimpleName:algorithm)))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:salt)(ArrayInitializer)))(IfStatement(InfixExpression(SimpleName:saltHex)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(ExpressionStatement(Assignment(SimpleName:salt)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(SimpleName:fromHex)(METHOD_INVOCATION_ARGUMENTS(SimpleName:saltHex)))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:label)(NullLiteral)))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:algorithm))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(Block(ExpressionStatement(Assignment(SimpleName:label)(ASSIGNMENT_OPERATOR:=)(ConditionalExpression(ParenthesizedExpression(InfixExpression(QualifiedName:salt.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(StringLiteral:<STR>)(StringLiteral:<STR>)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:algorithm))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(Block(ExpressionStatement(Assignment(SimpleName:label)(ASSIGNMENT_OPERATOR:=)(ConditionalExpression(ParenthesizedExpression(InfixExpression(QualifiedName:salt.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(StringLiteral:<STR>)(StringLiteral:<STR>)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:msgDigest))(SimpleName:reset)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:msgDigest))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:password))(SimpleName:getBytes)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:msgDigest))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(SimpleName:salt))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:pwhash)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:msgDigest))(SimpleName:digest))))(VariableDeclarationStatement(SimpleType(SimpleName:StringBuffer))(VariableDeclarationFragment(SimpleName:digest)(ClassInstanceCreation(SimpleType(SimpleName:StringBuffer))(SimpleName:label))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:digest))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Base64))(SimpleName:encode)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(SimpleName:concatenate)(METHOD_INVOCATION_ARGUMENTS(SimpleName:pwhash)(SimpleName:salt))))))))(ReturnStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:digest))(SimpleName:toString)))))))