(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(Modifier:static)(PrimitiveType:int)(SimpleName:binarySearch)(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:Vector))(SimpleType(SimpleName:Integer)))(SimpleName:list))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:value))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:middle)))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:valueTest)))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:low)(NumberLiteral:0)))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:high)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))(WhileStatement(InfixExpression(SimpleName:low)(INFIX_EXPRESSION_OPERATOR:<=)(SimpleName:high))(Block(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:low)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:high)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))(ExpressionStatement(Assignment(SimpleName:valueTest)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(SimpleName:middle)))))(IfStatement(InfixExpression(SimpleName:valueTest)(INFIX_EXPRESSION_OPERATOR:==)(SimpleName:value))(Block(ReturnStatement(SimpleName:middle)))(IfStatement(InfixExpression(SimpleName:valueTest)(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:value))(Block(ExpressionStatement(Assignment(SimpleName:high)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:middle)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))))(Block(ExpressionStatement(Assignment(SimpleName:low)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:middle)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1)))))))))(IfStatement(InfixExpression(SimpleName:low)(INFIX_EXPRESSION_OPERATOR:>=)(ParenthesizedExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))(Block(ExpressionStatement(Assignment(SimpleName:valueTest)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))))(IfStatement(InfixExpression(SimpleName:valueTest)(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:value))(Block(ReturnStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(ParenthesizedExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))))(Block(ReturnStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:size)))))))(IfStatement(InfixExpression(SimpleName:high)(INFIX_EXPRESSION_OPERATOR:<=)(NumberLiteral:0))(Block(ExpressionStatement(Assignment(SimpleName:valueTest)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:list))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0)))))(IfStatement(InfixExpression(SimpleName:valueTest)(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:value))(Block(ReturnStatement(NumberLiteral:0)))(Block(ReturnStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))))(Block(ReturnStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(SimpleName:low))))))))))