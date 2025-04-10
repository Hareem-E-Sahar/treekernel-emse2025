(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(MarkerAnnotation(SimpleName:Override))(Modifier:public)(PrimitiveType:void)(SimpleName:onEvent)(SingleVariableDeclaration(SimpleType(SimpleName:EventHeader))(SimpleName:header))(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:Event))(SimpleName:event))(Block(SwitchStatement(QualifiedName:header.type)(SwitchCase(QualifiedName:HTTPEventContants.HTTP_REQUEST_EVENT_TYPE))(Block(VariableDeclarationStatement(Modifier:final)(SimpleType(SimpleName:HTTPRequestEvent))(VariableDeclarationFragment(SimpleName:req)(CastExpression(SimpleType(SimpleName:HTTPRequestEvent))(SimpleName:event))))(VariableDeclarationStatement(SimpleType(SimpleName:ChannelFuture))(VariableDeclarationFragment(SimpleName:future)(MethodInvocation(SimpleName:getChannelFuture)(METHOD_INVOCATION_ARGUMENTS(SimpleName:req)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:future))(SimpleName:getChannel)))(SimpleName:isConnected))(Block(ExpressionStatement(MethodInvocation(SimpleName:onRemoteConnected)(METHOD_INVOCATION_ARGUMENTS(SimpleName:future)(SimpleName:req)))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:future))(SimpleName:addListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ChannelFutureListener))(AnonymousClassDeclaration(MethodDeclaration(MarkerAnnotation(SimpleName:Override))(Modifier:public)(PrimitiveType:void)(SimpleName:operationComplete)(SingleVariableDeclaration(SimpleType(SimpleName:ChannelFuture))(SimpleName:cf))(SimpleType(SimpleName:Exception))(Block(ExpressionStatement(MethodInvocation(SimpleName:onRemoteConnected)(METHOD_INVOCATION_ARGUMENTS(SimpleName:cf)(SimpleName:req)))))))))))))(BreakStatement))(SwitchCase(QualifiedName:HTTPEventContants.HTTP_CHUNK_EVENT_TYPE))(Block(VariableDeclarationStatement(SimpleType(SimpleName:HTTPChunkEvent))(VariableDeclarationFragment(SimpleName:chunk)(CastExpression(SimpleType(SimpleName:HTTPChunkEvent))(SimpleName:event))))(VariableDeclarationStatement(Modifier:final)(SimpleType(SimpleName:ChannelBuffer))(VariableDeclarationFragment(SimpleName:buf)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ChannelBuffers))(SimpleName:wrappedBuffer)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:chunk.content)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:getChannel)))(SimpleName:isConnected))(Block(ExpressionStatement(Assignment(SimpleName:currentChannelFuture)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:getChannel)))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))))(Block(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:isSuccess))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(SimpleName:getID))(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:readableBytes))))))(ExpressionStatement(MethodInvocation(SimpleName:closeLocalChannel))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:addListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ChannelFutureListener))(AnonymousClassDeclaration(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:operationComplete)(SingleVariableDeclaration(Modifier:final)(SimpleType(SimpleName:ChannelFuture))(SimpleName:future))(SimpleType(SimpleName:Exception))(Block(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:future))(SimpleName:isSuccess))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:future))(SimpleName:getChannel)))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf)))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(SimpleName:closeLocalChannel))))))))))))))))(BreakStatement))(SwitchCase(QualifiedName:HTTPEventContants.HTTP_CONNECTION_EVENT_TYPE))(Block(VariableDeclarationStatement(SimpleType(SimpleName:HTTPConnectionEvent))(VariableDeclarationFragment(SimpleName:ev)(CastExpression(SimpleType(SimpleName:HTTPConnectionEvent))(SimpleName:event))))(IfStatement(InfixExpression(QualifiedName:ev.status)(INFIX_EXPRESSION_OPERATOR:==)(QualifiedName:HTTPConnectionEvent.CLOSED))(Block(IfStatement(InfixExpression(InfixExpression(NullLiteral)(INFIX_EXPRESSION_OPERATOR:!=)(SimpleName:currentChannelFuture))(INFIX_EXPRESSION_OPERATOR:&&)(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:!)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:isDone))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:currentChannelFuture))(SimpleName:addListener)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ChannelFutureListener))(AnonymousClassDeclaration(MethodDeclaration(MarkerAnnotation(SimpleName:Override))(Modifier:public)(PrimitiveType:void)(SimpleName:operationComplete)(SingleVariableDeclaration(SimpleType(SimpleName:ChannelFuture))(SimpleName:future))(SimpleType(SimpleName:Exception))(Block(ExpressionStatement(MethodInvocation(SimpleName:closeRemote)))))))))))(Block(ExpressionStatement(MethodInvocation(SimpleName:closeRemote)))))))(BreakStatement))(SwitchCase)(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:error)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(QualifiedName:header.type)))))(BreakStatement)))))))