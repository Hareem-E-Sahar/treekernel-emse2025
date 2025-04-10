(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:boolean)(SimpleName:download)(Block(VariableDeclarationStatement(SimpleType(SimpleName:BufferedInputStream))(VariableDeclarationFragment(SimpleName:bis)(NullLiteral)))(VariableDeclarationStatement(SimpleType(SimpleName:FileOutputStream))(VariableDeclarationFragment(SimpleName:fos)(NullLiteral)))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:URL))(VariableDeclarationFragment(SimpleName:url)(ClassInstanceCreation(SimpleType(SimpleName:URL))(SimpleName:location))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:chunks)(InfixExpression(ParenthesizedExpression(InfixExpression(MethodInvocation(SimpleName:getSize))(INFIX_EXPRESSION_OPERATOR:/)(SimpleName:chunkSize)))(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1))))(ExpressionStatement(Assignment(SimpleName:fos)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:FileOutputStream))(ClassInstanceCreation(SimpleType(SimpleName:File))(SimpleName:target)))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:chunk)(NumberLiteral:0)))(InfixExpression(SimpleName:chunk)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:chunks))(PostfixExpression(SimpleName:chunk)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:downloaded)(NumberLiteral:0)))(VariableDeclarationStatement(SimpleType(SimpleName:URLConnection))(VariableDeclarationFragment(SimpleName:connection)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:url))(SimpleName:openConnection))))(IfStatement(InfixExpression(SimpleName:chunk)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:connection))(SimpleName:addRequestProperty)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(ParenthesizedExpression(InfixExpression(SimpleName:chunk)(INFIX_EXPRESSION_OPERATOR:*)(SimpleName:chunkSize)))(StringLiteral:<STR>)))))))(ExpressionStatement(Assignment(SimpleName:bis)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:BufferedInputStream))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:connection))(SimpleName:getInputStream)))))(VariableDeclarationStatement(SimpleType(SimpleName:StringBuffer))(VariableDeclarationFragment(SimpleName:sb)(ClassInstanceCreation(SimpleType(SimpleName:StringBuffer)))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:buffer)(ArrayCreation(ArrayType(PrimitiveType:byte)(Dimension(SimpleName:bufferSize))))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:r)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:bis))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buffer)))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:total)(SimpleName:r)))(WhileStatement(InfixExpression(SimpleName:r)(INFIX_EXPRESSION_OPERATOR:!=)(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(Block(VariableDeclarationStatement(PrimitiveType:boolean)(VariableDeclarationFragment(SimpleName:shouldBreak)(BooleanLiteral:false)))(IfStatement(InfixExpression(ParenthesizedExpression(InfixExpression(SimpleName:downloaded)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:r)))(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:chunkSize))(Block(ExpressionStatement(Assignment(SimpleName:r)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:chunkSize)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:downloaded))))(ExpressionStatement(Assignment(SimpleName:shouldBreak)(ASSIGNMENT_OPERATOR:=)(BooleanLiteral:true)))))(IfStatement(InfixExpression(SimpleName:r)(INFIX_EXPRESSION_OPERATOR:==)(SimpleName:bufferSize))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fos))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buffer)))))(Block(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:r))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fos))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(ArrayAccess(SimpleName:buffer)(SimpleName:i)))))))))(IfStatement(MethodInvocation(SimpleName:isCanceled))(Block(ReturnStatement(BooleanLiteral:true)))(IfStatement(InfixExpression(SimpleName:r)(INFIX_EXPRESSION_OPERATOR:!=)(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(Block(ExpressionStatement(Assignment(SimpleName:downloaded)(ASSIGNMENT_OPERATOR:+=)(SimpleName:r)))(ExpressionStatement(Assignment(SimpleName:total)(ASSIGNMENT_OPERATOR:+=)(SimpleName:r)))(ExpressionStatement(MethodInvocation(SimpleName:downloaded)(METHOD_INVOCATION_ARGUMENTS(SimpleName:total)))))))(IfStatement(SimpleName:shouldBreak)(Block(BreakStatement)))(ExpressionStatement(Assignment(SimpleName:r)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:bis))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buffer)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:bis))(SimpleName:close)))))(ExpressionStatement(MethodInvocation(SimpleName:done)))(ReturnStatement(BooleanLiteral:true)))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ex))(SimpleName:printStackTrace)))(ReturnStatement(BooleanLiteral:false))))(Block(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fos))(SimpleName:flush)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fos))(SimpleName:close))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ex))(SimpleName:printStackTrace))))))))))))