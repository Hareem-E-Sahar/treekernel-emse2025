(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:protected)(PrimitiveType:void)(SimpleName:processRequest)(SingleVariableDeclaration(SimpleType(SimpleName:HttpServletRequest))(SimpleName:request))(SingleVariableDeclaration(SimpleType(SimpleName:HttpServletResponse))(SimpleName:response))(SimpleType(SimpleName:ServletException))(SimpleType(QualifiedName:java.io.IOException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:File))(VariableDeclarationFragment(SimpleName:file)))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:fileName)))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:contentType)))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:UUID)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getParameter)(METHOD_INVOCATION_ARGUMENTS(SimpleName:PARAM_UUID)))))(IfStatement(InfixExpression(SimpleName:UUID)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:sessionAttribute)(InfixExpression(SimpleName:SESSION_ATTR_FILE_PREFIX)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:UUID))))(ExpressionStatement(Assignment(SimpleName:file)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:File))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getSession)))(SimpleName:getAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:sessionAttribute))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getSession)))(SimpleName:removeAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:sessionAttribute))))(IfStatement(InfixExpression(InfixExpression(SimpleName:file)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:||)(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:!)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:exists))))(Block(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:s)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(ParenthesizedExpression(ConditionalExpression(InfixExpression(SimpleName:file)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath))))(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s))))(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:ServletException))(SimpleName:s)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:isDebugEnabled))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:debug)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath)))))))(ExpressionStatement(Assignment(SimpleName:fileName)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getName))))(ExpressionStatement(Assignment(SimpleName:contentType)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(SimpleName:determineContentType)(METHOD_INVOCATION_ARGUMENTS(SimpleName:fileName))))))(Block(ExpressionStatement(Assignment(SimpleName:file)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:File))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_FILE))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:removeAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_FILE))))(IfStatement(InfixExpression(InfixExpression(SimpleName:file)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:||)(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:!)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:exists))))(Block(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:s)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(ParenthesizedExpression(ConditionalExpression(InfixExpression(SimpleName:file)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath))))(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s))))(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:ServletException))(SimpleName:s)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:isDebugEnabled))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:debug)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath)))))))(ExpressionStatement(Assignment(SimpleName:fileName)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:String))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_FILE_NAME))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:removeAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_FILE_NAME))))(IfStatement(InfixExpression(SimpleName:fileName)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:pos)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fileName))(SimpleName:lastIndexOf)(METHOD_INVOCATION_ARGUMENTS(CharacterLiteral:<STR>)))))(IfStatement(InfixExpression(SimpleName:pos)(INFIX_EXPRESSION_OPERATOR:>=)(NumberLiteral:0))(ExpressionStatement(Assignment(SimpleName:fileName)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fileName))(SimpleName:substring)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(SimpleName:pos)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1))))))(Block(ExpressionStatement(Assignment(SimpleName:pos)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fileName))(SimpleName:lastIndexOf)(METHOD_INVOCATION_ARGUMENTS(CharacterLiteral:<STR>)))))(IfStatement(InfixExpression(SimpleName:pos)(INFIX_EXPRESSION_OPERATOR:>=)(NumberLiteral:0))(ExpressionStatement(Assignment(SimpleName:fileName)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fileName))(SimpleName:substring)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(SimpleName:pos)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1))))))))))(ExpressionStatement(Assignment(SimpleName:fileName)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getName)))))(ExpressionStatement(Assignment(SimpleName:contentType)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:String))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:getAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_CONTENT_TYPE))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:request))(SimpleName:removeAttribute)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ATTR_CONTENT_TYPE))))(IfStatement(InfixExpression(SimpleName:contentType)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(ExpressionStatement(MethodInvocation(SimpleName:determineContentType)(METHOD_INVOCATION_ARGUMENTS(SimpleName:fileName)))))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:isDebugEnabled))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:debug)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:fileName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:debug)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:contentType)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:response))(SimpleName:setContentType)(METHOD_INVOCATION_ARGUMENTS(SimpleName:contentType))))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:response))(SimpleName:setBufferSize)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:2048)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IllegalStateException))(SimpleName:e))(Block)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:response))(SimpleName:setHeader)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:fileName)(StringLiteral:<STR>)))))(VariableDeclarationStatement(SimpleType(SimpleName:InputStream))(VariableDeclarationFragment(SimpleName:in)(ClassInstanceCreation(SimpleType(SimpleName:FileInputStream))(SimpleName:file))))(VariableDeclarationStatement(SimpleType(SimpleName:ServletOutputStream))(VariableDeclarationFragment(SimpleName:out)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:response))(SimpleName:getOutputStream))))(TryStatement(Block(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:buffer)(ArrayCreation(ArrayType(PrimitiveType:byte)(Dimension(NumberLiteral:1000))))))(WhileStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:in))(SimpleName:available))(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:out))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buffer)(NumberLiteral:0)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:in))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buffer)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:out))(SimpleName:flush))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath)))(SimpleName:e))))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:out))(SimpleName:close)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:in))(SimpleName:close)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:isDebugEnabled))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:debug)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath)))))))))))