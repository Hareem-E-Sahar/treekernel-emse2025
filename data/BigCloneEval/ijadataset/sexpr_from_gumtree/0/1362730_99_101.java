(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(Modifier:static)(SimpleType(SimpleName:InputStream))(SimpleName:open)(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:url))(SimpleType(SimpleName:MalformedURLException))(SimpleType(SimpleName:IOException))(Block(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:FileUtils))(SimpleName:isURI)(METHOD_INVOCATION_ARGUMENTS(SimpleName:url)))(ReturnStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:URL))(SimpleName:url)))(SimpleName:openStream)))(ReturnStatement(ClassInstanceCreation(SimpleType(SimpleName:FileInputStream))(SimpleName:url))))))))