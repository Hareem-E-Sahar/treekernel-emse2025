(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:protected)(ArrayType(PrimitiveType:byte)(Dimension))(SimpleName:generateKeyBytes)(SingleVariableDeclaration(SimpleType(SimpleName:PBEKey))(SimpleName:pbeKey))(SingleVariableDeclaration(ArrayType(PrimitiveType:byte)(Dimension))(SimpleName:salt))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:iterationCount))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:keyLength))(Block(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:out)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:outCut)(ArrayCreation(ArrayType(PrimitiveType:byte)(Dimension(SimpleName:keyLength))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:messageDigest_))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:pbeKey))(SimpleName:getEncoded)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:messageDigest_))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(SimpleName:salt))))(ExpressionStatement(Assignment(SimpleName:out)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:messageDigest_))(SimpleName:digest))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:1)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:iterationCount))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:messageDigest_))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(SimpleName:out))))(ExpressionStatement(Assignment(SimpleName:out)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:messageDigest_))(SimpleName:digest))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:System))(SimpleName:arraycopy)(METHOD_INVOCATION_ARGUMENTS(SimpleName:out)(NumberLiteral:0)(SimpleName:outCut)(NumberLiteral:0)(SimpleName:keyLength))))(ReturnStatement(SimpleName:outCut))))))