(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(Modifier:static)(ArrayType(PrimitiveType:byte)(Dimension))(SimpleName:getMD5HashDigest)(SingleVariableDeclaration(ArrayType(PrimitiveType:byte)(Dimension))(SimpleName:input))(SimpleType(SimpleName:ApplicationException))(Block(TryStatement(Block(ReturnStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:MessageDigest))(SimpleName:getInstance)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(SimpleName:digest)(METHOD_INVOCATION_ARGUMENTS(SimpleName:input)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:Exception))(SimpleName:e))(Block(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:ApplicationException))(SimpleName:e)(StringLiteral:<STR>))))))))))