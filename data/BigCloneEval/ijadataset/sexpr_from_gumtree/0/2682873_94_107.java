(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:float)(SimpleName:makeConvolutionDyy)(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:x))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:y))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:filtersize))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:x1))(VariableDeclarationFragment(SimpleName:x2))(VariableDeclarationFragment(SimpleName:y1))(VariableDeclarationFragment(SimpleName:y2))(VariableDeclarationFragment(SimpleName:y3))(VariableDeclarationFragment(SimpleName:y4)))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:center))(VariableDeclarationFragment(SimpleName:lobeSize_1))(VariableDeclarationFragment(SimpleName:lobeSize_2)))(ExpressionStatement(Assignment(SimpleName:center)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:filtersize)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))(ExpressionStatement(Assignment(SimpleName:lobeSize_1)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:filtersize)(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:3))))(ExpressionStatement(Assignment(SimpleName:lobeSize_2)(ASSIGNMENT_OPERATOR:=)(InfixExpression(NumberLiteral:5)(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(InfixExpression(NumberLiteral:4)(INFIX_EXPRESSION_OPERATOR:*)(ParenthesizedExpression(InfixExpression(SimpleName:filtersize)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:9))))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:6)))))(ExpressionStatement(Assignment(SimpleName:y1)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:y)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:center))))(ExpressionStatement(Assignment(SimpleName:y4)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:y)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:center))))(ExpressionStatement(Assignment(SimpleName:y2)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:y)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:center))(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:lobeSize_1))))(ExpressionStatement(Assignment(SimpleName:y3)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:y)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:center))(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:lobeSize_1))))(ExpressionStatement(Assignment(SimpleName:x1)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:x)(INFIX_EXPRESSION_OPERATOR:-)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:lobeSize_2)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2)))))(ExpressionStatement(Assignment(SimpleName:x2)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:x)(INFIX_EXPRESSION_OPERATOR:+)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:lobeSize_2)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2)))))(ReturnStatement(ParenthesizedExpression(InfixExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:integralImage))(SimpleName:getIntegralSquare)(METHOD_INVOCATION_ARGUMENTS(SimpleName:x1)(SimpleName:y2)(SimpleName:x2)(SimpleName:y3)))(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:3))(INFIX_EXPRESSION_OPERATOR:-)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:integralImage))(SimpleName:getIntegralSquare)(METHOD_INVOCATION_ARGUMENTS(SimpleName:x1)(SimpleName:y1)(SimpleName:x2)(SimpleName:y4))))))))))