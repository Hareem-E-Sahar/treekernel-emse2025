(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:private)(SimpleType(SimpleName:NackFrag))(SimpleName:decodeNackFrag)(SimpleType(SimpleName:MalformedSubmessageException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:EntityId))(VariableDeclarationFragment(SimpleName:readerId)(ClassInstanceCreation(SimpleType(SimpleName:EntityId))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:EntityId_tHelper))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:_packet))))))(VariableDeclarationStatement(SimpleType(SimpleName:EntityId))(VariableDeclarationFragment(SimpleName:writerId)(ClassInstanceCreation(SimpleType(SimpleName:EntityId))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:EntityId_tHelper))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:_packet))))))(VariableDeclarationStatement(SimpleType(SimpleName:SequenceNumber))(VariableDeclarationFragment(SimpleName:writerSN)(ClassInstanceCreation(SimpleType(SimpleName:SequenceNumber))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:SequenceNumber_tHelper))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:_packet))))))(VariableDeclarationStatement(SimpleType(SimpleName:FragmentNumberSet))(VariableDeclarationFragment(SimpleName:fragmentNumberState)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:FragmentNumberSet))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:_packet)))))(VariableDeclarationStatement(SimpleType(SimpleName:Count))(VariableDeclarationFragment(SimpleName:count)(ClassInstanceCreation(SimpleType(SimpleName:Count))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:Count_tHelper))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:_packet))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(QualifiedName:GlobalProperties.logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Logger.INFO)(TypeLiteral(SimpleType(SimpleName:CDRMessageProcessorDEBUG)))(StringLiteral:<STR>)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:writerSN))(SimpleName:getLongValue))))))(ReturnStatement(ClassInstanceCreation(SimpleType(SimpleName:NackFrag))(SimpleName:readerId)(SimpleName:writerId)(SimpleName:writerSN)(SimpleName:fragmentNumberState)(SimpleName:count)))))))