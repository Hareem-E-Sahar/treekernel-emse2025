(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(Modifier:static)(Modifier:final)(PrimitiveType:void)(SimpleName:load)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:filename))(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:name))(SimpleType(SimpleName:LoadException))(Block(Block(VariableDeclarationStatement(SimpleType(SimpleName:Object))(VariableDeclarationFragment(SimpleName:o)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:resources))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(SimpleName:name)))))(IfStatement(InfixExpression(SimpleName:o)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(ReturnStatement)))(VariableDeclarationStatement(SimpleType(SimpleName:ReadableByteChannel))(VariableDeclarationFragment(SimpleName:channel)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ResourcePathManager))(SimpleName:getChannel)(METHOD_INVOCATION_ARGUMENTS(SimpleName:filename)))))(IfStatement(InfixExpression(SimpleName:channel)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:LoadException))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:filename)))))(VariableDeclarationStatement(SimpleType(SimpleName:Image2D))(VariableDeclarationFragment(SimpleName:image)(NullLiteral)))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:filename))(SimpleName:endsWith)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))(ExpressionStatement(Assignment(SimpleName:image)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:TGAFile))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:channel)))))(ExpressionStatement(Assignment(SimpleName:image)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:BuiltinFile))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:channel))))))(IfStatement(InfixExpression(SimpleName:image)(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:LoadException))(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:filename)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:resources))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:name)(SimpleName:image))))))))