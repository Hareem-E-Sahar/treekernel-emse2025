(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(SimpleType(SimpleName:InputStream))(SimpleName:open)(SimpleType(SimpleName:IOException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:URLConnection))(VariableDeclarationFragment(SimpleName:uc)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:url))(SimpleName:openConnection))))(ExpressionStatement(Assignment(SimpleName:lastModified)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:uc))(SimpleName:getLastModified))))(ReturnStatement(ClassInstanceCreation(SimpleType(SimpleName:InputStreamImpl))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:uc))(SimpleName:getInputStream))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:uc))(SimpleName:getContentLength))))))))