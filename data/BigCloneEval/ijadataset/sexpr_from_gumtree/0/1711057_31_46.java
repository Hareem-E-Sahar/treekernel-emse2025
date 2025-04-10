(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(Modifier:static)(PrimitiveType:int)(SimpleName:deleteRegistrationByEmail)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:email))(SimpleType(SimpleName:Exception))(Block(VariableDeclarationStatement(SimpleType(SimpleName:Session))(VariableDeclarationFragment(SimpleName:session)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:HibernateUtil))(SimpleName:getSessionFactory)))(SimpleName:getCurrentSession))))(VariableDeclarationStatement(SimpleType(SimpleName:Transaction))(VariableDeclarationFragment(SimpleName:tx)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:session))(SimpleName:beginTransaction))))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:Query))(VariableDeclarationFragment(SimpleName:query)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:session))(SimpleName:createQuery)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:query))(SimpleName:setString)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)(SimpleName:email))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:rowCount)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:query))(SimpleName:executeUpdate))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tx))(SimpleName:commit)))(ReturnStatement(SimpleName:rowCount)))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:ex))(Block(IfStatement(InfixExpression(SimpleName:tx)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tx))(SimpleName:rollback)))))(ThrowStatement(SimpleName:ex)))))))))