(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:updateController)(Block(VariableDeclarationStatement(SimpleType(SimpleName:RSController))(VariableDeclarationFragment(SimpleName:controller)(MethodInvocation(SimpleName:getCurrentController))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:controller))(SimpleName:getChannelCount)))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(ArrayAccess(SimpleName:joystickPanels)(SimpleName:i)))(SimpleName:setValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:controller))(SimpleName:getChannelValue)(METHOD_INVOCATION_ARGUMENTS(SimpleName:i)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:calibrate))(SimpleName:isSelected)))))))))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:HeliControlPanel))(SimpleName:heliPanel))(SimpleName:heliPanels)(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:heliPanel))(SimpleName:setValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:controller))(SimpleName:getControlValue)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:heliPanel))(SimpleName:getControl)))))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txView))(SimpleName:updateControls)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:txView))(SimpleName:repaint)))))))