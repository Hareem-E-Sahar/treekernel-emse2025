(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:protected)(ArrayType(PrimitiveType:int)(Dimension))(SimpleName:getCompletionsBin)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:start))(SingleVariableDeclaration(ArrayType(SimpleType(SimpleName:AbstractEntry))(Dimension))(SimpleName:entries))(SingleVariableDeclaration(ArrayType(PrimitiveType:int)(Dimension))(SimpleName:initBounds))(Block(VariableDeclarationStatement(ArrayType(PrimitiveType:int)(Dimension))(VariableDeclarationFragment(SimpleName:bounds)(ArrayCreation(ArrayType(PrimitiveType:int)(Dimension))(ArrayInitializer(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:left)(ArrayAccess(SimpleName:initBounds)(NumberLiteral:0)))(VariableDeclarationFragment(SimpleName:right)(InfixExpression(ArrayAccess(SimpleName:initBounds)(NumberLiteral:1))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:middle)(InfixExpression(SimpleName:right)(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))(IfStatement(InfixExpression(SimpleName:left)(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:right))(ReturnStatement(SimpleName:bounds)))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ArrayAccess(SimpleName:entries)(SimpleName:left))(SimpleName:key)))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(SimpleName:start)))(ExpressionStatement(Assignment(SimpleName:right)(ASSIGNMENT_OPERATOR:=)(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(SimpleName:left)))))(WhileStatement(InfixExpression(SimpleName:left)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:middle))(Block(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ArrayAccess(SimpleName:entries)(SimpleName:middle))(SimpleName:key)))(SimpleName:compareTo)(METHOD_INVOCATION_ARGUMENTS(SimpleName:start)))(INFIX_EXPRESSION_OPERATOR:>=)(NumberLiteral:0))(Block(ExpressionStatement(Assignment(SimpleName:right)(ASSIGNMENT_OPERATOR:=)(SimpleName:middle)))(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:left)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:middle)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2)))))(Block(ExpressionStatement(Assignment(SimpleName:left)(ASSIGNMENT_OPERATOR:=)(SimpleName:middle)))(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:middle)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:right)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))))))(IfStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:!)(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ArrayAccess(SimpleName:entries)(SimpleName:right))(SimpleName:key)))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(SimpleName:start))))(ReturnStatement(SimpleName:bounds)))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:bounds)(NumberLiteral:0))(ASSIGNMENT_OPERATOR:=)(SimpleName:right)))(ExpressionStatement(Assignment(SimpleName:left)(ASSIGNMENT_OPERATOR:=)(SimpleName:right)))(ExpressionStatement(Assignment(SimpleName:right)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ArrayAccess(SimpleName:initBounds)(NumberLiteral:1))(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ArrayAccess(SimpleName:entries)(SimpleName:right))(SimpleName:key)))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(SimpleName:start)))(Block(ExpressionStatement(Assignment(ArrayAccess(SimpleName:bounds)(NumberLiteral:1))(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:right)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1))))(ReturnStatement(SimpleName:bounds))))(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:left)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:right)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))(WhileStatement(InfixExpression(SimpleName:left)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:middle))(Block(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(FieldAccess(ArrayAccess(SimpleName:entries)(SimpleName:middle))(SimpleName:key)))(SimpleName:startsWith)(METHOD_INVOCATION_ARGUMENTS(SimpleName:start)))(Block(ExpressionStatement(Assignment(SimpleName:left)(ASSIGNMENT_OPERATOR:=)(SimpleName:middle)))(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:right)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:middle)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2)))))(Block(ExpressionStatement(Assignment(SimpleName:right)(ASSIGNMENT_OPERATOR:=)(SimpleName:middle)))(ExpressionStatement(Assignment(SimpleName:middle)(ASSIGNMENT_OPERATOR:=)(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:middle)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:left)))(INFIX_EXPRESSION_OPERATOR:/)(NumberLiteral:2))))))))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:bounds)(NumberLiteral:1))(ASSIGNMENT_OPERATOR:=)(SimpleName:right)))(ReturnStatement(SimpleName:bounds))))))