(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(PrimitiveType:boolean)(SimpleName:splitDataFile)(SingleVariableDeclaration(SimpleType(SimpleName:File))(SimpleName:file))(SingleVariableDeclaration(PrimitiveType:int)(SimpleName:blockSize))(Block(VariableDeclarationStatement(SimpleType(SimpleName:RandomAccessFile))(VariableDeclarationFragment(SimpleName:parentFile)(NullLiteral)))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:filenum)(NumberLiteral:0)))(VariableDeclarationStatement(PrimitiveType:boolean)(VariableDeclarationFragment(SimpleName:isSuccess)(BooleanLiteral:true)))(TryStatement(Block(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:index)(InfixExpression(CastExpression(PrimitiveType:long)(SimpleName:blockSize))(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024)(NumberLiteral:1024))))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:fileSize)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:length))))(VariableDeclarationStatement(ArrayType(SimpleType(SimpleName:File))(Dimension))(VariableDeclarationFragment(SimpleName:subFiles)(NullLiteral)))(IfStatement(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:<=)(ParenthesizedExpression(InfixExpression(SimpleName:index)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1024))))(Block(ReturnStatement(SimpleName:isSuccess))))(IfStatement(InfixExpression(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:%)(SimpleName:index))(INFIX_EXPRESSION_OPERATOR:==)(NumberLiteral:0))(ExpressionStatement(Assignment(SimpleName:filenum)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:/)(SimpleName:index))))(ExpressionStatement(Assignment(SimpleName:filenum)(ASSIGNMENT_OPERATOR:=)(InfixExpression(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:/)(SimpleName:index))(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1)))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:sourceFile)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath))))(ExpressionStatement(Assignment(SimpleName:parentFile)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:RandomAccessFile))(SimpleName:sourceFile)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:fileName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:sourceFile))(SimpleName:substring)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:sourceFile))(SimpleName:lastIndexOf)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))))(ExpressionStatement(Assignment(SimpleName:subFiles)(ASSIGNMENT_OPERATOR:=)(ArrayCreation(ArrayType(SimpleType(SimpleName:File))(Dimension(CastExpression(PrimitiveType:int)(SimpleName:filenum)))))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:filenum))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:_tempFileName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:StringBuilder))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(SimpleName:fileName))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(SimpleName:i))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(SimpleName:toString))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:File))(SimpleName:_tempFileName)))(SimpleName:createNewFile)))(ExpressionStatement(Assignment(ArrayAccess(SimpleName:subFiles)(SimpleName:i))(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:File))(SimpleName:_tempFileName))))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:beg)(NumberLiteral:0)))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:filenum))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(VariableDeclarationStatement(SimpleType(SimpleName:FileOutputStream))(VariableDeclarationFragment(SimpleName:outputStream)(NullLiteral)))(TryStatement(Block(ExpressionStatement(Assignment(SimpleName:outputStream)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:FileOutputStream))(ArrayAccess(SimpleName:subFiles)(SimpleName:i)))))(VariableDeclarationStatement(SimpleType(SimpleName:FileChannel))(VariableDeclarationFragment(SimpleName:inChannel)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:parentFile))(SimpleName:getChannel))))(VariableDeclarationStatement(SimpleType(SimpleName:FileChannel))(VariableDeclarationFragment(SimpleName:outChannel)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:outputStream))(SimpleName:getChannel))))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:remain)))(IfStatement(InfixExpression(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:beg))(INFIX_EXPRESSION_OPERATOR:>)(SimpleName:index))(ExpressionStatement(Assignment(SimpleName:remain)(ASSIGNMENT_OPERATOR:=)(SimpleName:index)))(ExpressionStatement(Assignment(SimpleName:remain)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:fileSize)(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:beg)))))(WhileStatement(InfixExpression(SimpleName:remain)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0))(Block(IfStatement(InfixExpression(SimpleName:remain)(INFIX_EXPRESSION_OPERATOR:>)(InfixExpression(NumberLiteral:5)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024)(NumberLiteral:1024)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inChannel))(SimpleName:transferTo)(METHOD_INVOCATION_ARGUMENTS(SimpleName:beg)(InfixExpression(NumberLiteral:5)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024)(NumberLiteral:1024))(SimpleName:outChannel))))(ExpressionStatement(Assignment(SimpleName:remain)(ASSIGNMENT_OPERATOR:-=)(InfixExpression(NumberLiteral:5)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024)(NumberLiteral:1024))))(ExpressionStatement(Assignment(SimpleName:beg)(ASSIGNMENT_OPERATOR:+=)(InfixExpression(NumberLiteral:5)(INFIX_EXPRESSION_OPERATOR:*)(NumberLiteral:1024)(NumberLiteral:1024)))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inChannel))(SimpleName:transferTo)(METHOD_INVOCATION_ARGUMENTS(SimpleName:beg)(SimpleName:remain)(SimpleName:outChannel))))(ExpressionStatement(Assignment(SimpleName:beg)(ASSIGNMENT_OPERATOR:+=)(SimpleName:remain)))(BreakStatement)))))(IfStatement(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(InfixExpression(SimpleName:filenum)(INFIX_EXPRESSION_OPERATOR:-)(NumberLiteral:1)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:parentFile))(SimpleName:seek)(METHOD_INVOCATION_ARGUMENTS(SimpleName:beg))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:tail)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:parentFile))(SimpleName:readLine))))(IfStatement(InfixExpression(SimpleName:tail)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(Block(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:j)(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:1))))(InfixExpression(SimpleName:j)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:filenum))(PostfixExpression(SimpleName:j)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ArrayAccess(SimpleName:subFiles)(SimpleName:j)))(SimpleName:delete)))))(BreakStatement)))(ExpressionStatement(Assignment(SimpleName:beg)(ASSIGNMENT_OPERATOR:+=)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tail))(SimpleName:length))(INFIX_EXPRESSION_OPERATOR:+)(NumberLiteral:2))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:outputStream))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tail))(SimpleName:getBytes))))))))(Block(TryStatement(Block(IfStatement(InfixExpression(SimpleName:outputStream)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:outputStream))(SimpleName:close)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e)(SimpleName:e))))))))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ex)(SimpleName:ex))))(ExpressionStatement(Assignment(SimpleName:isSuccess)(ASSIGNMENT_OPERATOR:=)(BooleanLiteral:false)))))(Block(IfStatement(InfixExpression(SimpleName:parentFile)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:parentFile))(SimpleName:close))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e)(SimpleName:e))))))))(IfStatement(InfixExpression(SimpleName:isSuccess)(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(SimpleName:filenum)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:renameTo)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:File))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:StringBuilder))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:file))(SimpleName:getAbsolutePath)))))(SimpleName:append)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(SimpleName:toString)))))))))(ReturnStatement(SimpleName:isSuccess))))))