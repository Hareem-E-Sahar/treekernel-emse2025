(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(Modifier:static)(ParameterizedType(SimpleType(SimpleName:List))(SimpleType(SimpleName:BitSet)))(SimpleName:makeRandomBitSetList)(SingleVariableDeclaration(Modifier:final)(PrimitiveType:long)(SimpleName:randomSeed))(SingleVariableDeclaration(Modifier:final)(PrimitiveType:int)(SimpleName:listSize))(SingleVariableDeclaration(Modifier:final)(PrimitiveType:int)(SimpleName:minBitsSize))(SingleVariableDeclaration(Modifier:final)(PrimitiveType:int)(SimpleName:maxBitsSize))(Block(VariableDeclarationStatement(SimpleType(SimpleName:Random))(VariableDeclarationFragment(SimpleName:r)(ClassInstanceCreation(SimpleType(SimpleName:Random))(SimpleName:randomSeed))))(VariableDeclarationStatement(ParameterizedType(SimpleType(SimpleName:List))(SimpleType(SimpleName:BitSet)))(VariableDeclarationFragment(SimpleName:resultList)(ClassInstanceCreation(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:BitSet)))(SimpleName:listSize))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:listSize))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:arraySize)(InfixExpression(SimpleName:minBitsSize)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:r))(SimpleName:nextInt)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(SimpleName:maxBitsSize)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:minBitsSize)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:resultList))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(SimpleName:makeRandomBitSet)(METHOD_INVOCATION_ARGUMENTS(SimpleName:r)(SimpleName:arraySize))))))))(ReturnStatement(SimpleName:resultList))))))