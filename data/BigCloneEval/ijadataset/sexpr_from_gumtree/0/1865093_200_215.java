(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(MarkerAnnotation(SimpleName:Override))(Modifier:public)(TypeParameter(SimpleName:T))(ArrayType(SimpleType(SimpleName:T))(Dimension))(SimpleName:toArray)(SingleVariableDeclaration(ArrayType(SimpleType(SimpleName:T))(Dimension))(SimpleName:array))(Block(VariableDeclarationStatement(ArrayType(SimpleType(SimpleName:Object))(Dimension))(VariableDeclarationFragment(SimpleName:data)(MethodInvocation(SimpleName:data))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:size)(ConditionalExpression(InfixExpression(SimpleName:data)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(NumberLiteral:0)(QualifiedName:data.length))))(IfStatement(InfixExpression(SimpleName:size)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0))(Block(IfStatement(InfixExpression(QualifiedName:array.length)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:size))(Block(VariableDeclarationStatement(SingleMemberAnnotation(SimpleName:SuppressWarnings)(StringLiteral:<STR>))(ArrayType(SimpleType(SimpleName:T))(Dimension))(VariableDeclarationFragment(SimpleName:newArray)(CastExpression(ArrayType(SimpleType(SimpleName:T))(Dimension))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Array))(SimpleName:newInstance)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:array))(SimpleName:getClass)))(SimpleName:getComponentType))(SimpleName:size))))))(ExpressionStatement(Assignment(SimpleName:array)(ASSIGNMENT_OPERATOR:=)(SimpleName:newArray)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:System))(SimpleName:arraycopy)(METHOD_INVOCATION_ARGUMENTS(SimpleName:data)(NumberLiteral:0)(SimpleName:array)(NumberLiteral:0)(SimpleName:size))))))(IfStatement(InfixExpression(QualifiedName:array.length)(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:size))(Block(ExpressionStatement(Assignment(ArrayAccess(SimpleName:array)(SimpleName:size))(ASSIGNMENT_OPERATOR:=)(NullLiteral)))))(ReturnStatement(SimpleName:array))))))