(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(MarkerAnnotation(SimpleName:Override))(Modifier:public)(PrimitiveType:void)(SimpleName:channelData)(SingleVariableDeclaration(SimpleType(SimpleName:Channel))(SimpleName:channel))(SingleVariableDeclaration(ArrayType(PrimitiveType:byte)(Dimension))(SimpleName:data))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:off))(VariableDeclarationFragment(SimpleName:w))(VariableDeclarationFragment(SimpleName:x))(VariableDeclarationFragment(SimpleName:y))(VariableDeclarationFragment(SimpleName:z)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:ciphertext)(ArrayCreation(ArrayType(PrimitiveType:byte)(Dimension(InfixExpression(QualifiedName:data.length)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1024)))))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:keystream)(ArrayCreation(ArrayType(PrimitiveType:byte)(Dimension(NumberLiteral:16))))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:block)(NumberLiteral:0)))(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:<)(InfixExpression(QualifiedName:data.length)(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:1024)))(PostfixExpression(SimpleName:block)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(Assignment(SimpleName:off)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))))(ExpressionStatement(Assignment(SimpleName:w)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(NumberLiteral:0)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:256)))))(ExpressionStatement(Assignment(SimpleName:x)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(NumberLiteral:1)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:256)))))(ExpressionStatement(Assignment(SimpleName:y)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(NumberLiteral:2)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:256)))))(ExpressionStatement(Assignment(SimpleName:z)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(NumberLiteral:3)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:256)))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(ParenthesizedExpression(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:i)))(INFIX_EXPRESSION_OPERATOR:<)(QualifiedName:data.length)))(Assignment(SimpleName:i)(ASSIGNMENT_OPERATOR:+=)(NumberLiteral:4))(Block(ExpressionStatement(Assignment(ArrayAccess(SimpleName:ciphertext)(PostfixExpression(SimpleName:off)(POSTFIX_EXPRESSION_OPERATOR:++)))(ASSIGNMENT_OPERATOR:=)(ArrayAccess(SimpleName:data)(PostfixExpression(SimpleName:w)(POSTFIX_EXPRESSION_OPERATOR:++)))))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:ciphertext)(PostfixExpression(SimpleName:off)(POSTFIX_EXPRESSION_OPERATOR:++)))(ASSIGNMENT_OPERATOR:=)(ArrayAccess(SimpleName:data)(PostfixExpression(SimpleName:x)(POSTFIX_EXPRESSION_OPERATOR:++)))))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:ciphertext)(PostfixExpression(SimpleName:off)(POSTFIX_EXPRESSION_OPERATOR:++)))(ASSIGNMENT_OPERATOR:=)(ArrayAccess(SimpleName:data)(PostfixExpression(SimpleName:y)(POSTFIX_EXPRESSION_OPERATOR:++)))))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:ciphertext)(PostfixExpression(SimpleName:off)(POSTFIX_EXPRESSION_OPERATOR:++)))(ASSIGNMENT_OPERATOR:=)(ArrayAccess(SimpleName:data)(PostfixExpression(SimpleName:z)(POSTFIX_EXPRESSION_OPERATOR:++)))))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(ParenthesizedExpression(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:i)))(INFIX_EXPRESSION_OPERATOR:<)(QualifiedName:data.length)))(Assignment(SimpleName:i)(ASSIGNMENT_OPERATOR:+=)(NumberLiteral:16))(Block(TryStatement(Block(ExpressionStatement(Assignment(SimpleName:keystream)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ThisExpression)(SimpleName:cipher)))(SimpleName:doFinal)(METHOD_INVOCATION_ARGUMENTS(FieldAccess(ThisExpression)(SimpleName:iv)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IllegalBlockSizeException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:BadPaddingException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:j)(NumberLiteral:0)))(InfixExpression(SimpleName:j)(INFIX_EXPRESSION_OPERATOR:<)(NumberLiteral:16))(PostfixExpression(SimpleName:j)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(Assignment(ArrayAccess(SimpleName:ciphertext)(InfixExpression(InfixExpression(InfixExpression(SimpleName:block)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024))(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:i))(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:j)))(ASSIGNMENT_OPERATOR:^=)(InfixExpression(ArrayAccess(SimpleName:keystream)(SimpleName:j))(INFIX_EXPRESSION_OPERATOR:^)(ArrayAccess(FieldAccess(ThisExpression)(SimpleName:iv))(SimpleName:j)))))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:j)(NumberLiteral:15)))(InfixExpression(SimpleName:j)(INFIX_EXPRESSION_OPERATOR:>=)(NumberLiteral:0))(PostfixExpression(SimpleName:j)(POSTFIX_EXPRESSION_OPERATOR:--))(Block(ExpressionStatement(Assignment(ArrayAccess(FieldAccess(ThisExpression)(SimpleName:iv))(SimpleName:j))(ASSIGNMENT_OPERATOR:+=)(NumberLiteral:1)))(IfStatement(InfixExpression(ParenthesizedExpression(InfixExpression(ArrayAccess(FieldAccess(ThisExpression)(SimpleName:iv))(SimpleName:j))(INFIX_EXPRESSION_OPERATOR:&)(NumberLiteral:0xFF)))(INFIX_EXPRESSION_OPERATOR:!=)(NumberLiteral:0))(Block(BreakStatement)))))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ThisExpression)(SimpleName:cipher)))(SimpleName:init)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Cipher.ENCRYPT_MODE)(FieldAccess(ThisExpression)(SimpleName:key))(ClassInstanceCreation(SimpleType(SimpleName:IvParameterSpec))(FieldAccess(ThisExpression)(SimpleName:iv)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:InvalidKeyException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:InvalidAlgorithmParameterException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))))))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ThisExpression)(SimpleName:output)))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ciphertext)(NumberLiteral:0)(InfixExpression(QualifiedName:ciphertext.length)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1024))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block)))))))