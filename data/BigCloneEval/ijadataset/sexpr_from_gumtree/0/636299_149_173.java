(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:saveSongbookAsZip)(SingleVariableDeclaration(SimpleType(SimpleName:Songbook))(SimpleName:songbook))(SingleVariableDeclaration(SimpleType(SimpleName:File))(SimpleName:songbookfile))(SimpleType(SimpleName:IOException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:FileOutputStream))(VariableDeclarationFragment(SimpleName:fos)(ClassInstanceCreation(SimpleType(SimpleName:FileOutputStream))(SimpleName:songbookfile))))(VariableDeclarationStatement(SimpleType(SimpleName:ZipOutputStream))(VariableDeclarationFragment(SimpleName:zos)(ClassInstanceCreation(SimpleType(SimpleName:ZipOutputStream))(SimpleName:fos))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:setComment)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:Date))))(SimpleName:toString))(StringLiteral:<STR>)))))(VariableDeclarationStatement(SimpleType(SimpleName:ZipEntry))(VariableDeclarationFragment(SimpleName:e)(ClassInstanceCreation(SimpleType(SimpleName:ZipEntry))(InfixExpression(QualifiedName:IOConstants.METADATA_DIRECTORY)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(QualifiedName:IOConstants.INDEX_FILE_NAME)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:putNextEntry)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e))))(ExpressionStatement(MethodInvocation(SimpleName:writeIndex)(METHOD_INVOCATION_ARGUMENTS(SimpleName:songbook)(SimpleName:zos))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:closeEntry)))(VariableDeclarationStatement(SimpleType(SimpleName:ZipEntry))(VariableDeclarationFragment(SimpleName:ple)(ClassInstanceCreation(SimpleType(SimpleName:ZipEntry))(InfixExpression(QualifiedName:IOConstants.METADATA_DIRECTORY)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(QualifiedName:IOConstants.PLAYLISTS_FILE_NAME)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:putNextEntry)(METHOD_INVOCATION_ARGUMENTS(SimpleName:ple))))(ExpressionStatement(MethodInvocation(SimpleName:writePlaylists)(METHOD_INVOCATION_ARGUMENTS(SimpleName:songbook)(SimpleName:zos))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:closeEntry)))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:Song))(SimpleName:s))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:songbook))(SimpleName:getSongs))(Block(ExpressionStatement(Assignment(SimpleName:e)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:ZipEntry))(MethodInvocation(SimpleName:convertToFileName)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s))))))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:putNextEntry)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e)))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:ZipException))(SimpleName:e1))(Block(ExpressionStatement(Assignment(SimpleName:e)(ASSIGNMENT_OPERATOR:=)(ClassInstanceCreation(SimpleType(SimpleName:ZipEntry))(InfixExpression(MethodInvocation(SimpleName:convertToFileName)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s)))(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:System))(SimpleName:currentTimeMillis))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:putNextEntry)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e)))))))(ExpressionStatement(MethodInvocation(SimpleName:saveSong)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s)(SimpleName:zos)(BooleanLiteral:false))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:closeEntry)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:IOUtils))(SimpleName:closeQuietly)(METHOD_INVOCATION_ARGUMENTS(SimpleName:zos))))))))