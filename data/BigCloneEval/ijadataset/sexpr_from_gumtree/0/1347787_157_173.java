(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:boolean)(SimpleName:transferFromChannelToFile)(SingleVariableDeclaration(SimpleType(SimpleName:ReadableByteChannel))(SimpleName:channel))(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:fileName))(SimpleType(SimpleName:IOException))(Block(IfStatement(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:!)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:_location))(SimpleName:exists)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:_location))(SimpleName:mkdirs)))))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:dataLen)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ChannelUtil))(SimpleName:readLong)(METHOD_INVOCATION_ARGUMENTS(SimpleName:channel)))))(IfStatement(InfixExpression(SimpleName:dataLen)(INFIX_EXPRESSION_OPERATOR:<)(NumberLiteral:0))(ReturnStatement(BooleanLiteral:false)))(VariableDeclarationStatement(SimpleType(SimpleName:File))(VariableDeclarationFragment(SimpleName:file)(ClassInstanceCreation(SimpleType(SimpleName:File))(SimpleName:_location)(SimpleName:fileName))))(VariableDeclarationStatement(SimpleType(SimpleName:RandomAccessFile))(VariableDeclarationFragment(SimpleName:raf)(NullLiteral)))(VariableDeclarationStatement(SimpleType(SimpleName:FileChannel))(VariableDeclarationFragment(SimpleName:fc)(NullLiteral)))(TryStatement(Block(ExpressionStatement(Assignment(SimpleName:raf)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:RandomAccessFile))(SimpleName:file)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:fc)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:raf))(SimpleName:getChannel))))(ReturnStatement(ParenthesizedExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:fc))(SimpleName:transferFrom)(METHOD_INVOCATION_ARGUMENTS(SimpleName:channel)(NumberLiteral:0)(SimpleName:dataLen)))(INFIX_EXPRESSION_OPERATOR:==)(SimpleName:dataLen)))))(Block(IfStatement(InfixExpression(SimpleName:raf)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:raf))(SimpleName:close))))))))))