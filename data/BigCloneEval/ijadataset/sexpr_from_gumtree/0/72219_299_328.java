(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(SimpleType(QualifiedName:java.util.Vector))(SimpleName:testLookUpFromClassNames)(Block(VariableDeclarationStatement(SimpleType(QualifiedName:java.util.Vector))(VariableDeclarationFragment(SimpleName:mgedOntClassNames)(ClassInstanceCreation(SimpleType(QualifiedName:java.util.Vector)))))(TryStatement(Block(VariableDeclarationStatement(SimpleType(QualifiedName:java.util.Iterator))(VariableDeclarationFragment(SimpleName:it)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:entryClassSet))(SimpleName:iterator))))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:counter)(NumberLiteral:0)))(WhileStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:it))(SimpleName:hasNext))(Block(VariableDeclarationStatement(SimpleType(SimpleName:EntryClassPair))(VariableDeclarationFragment(SimpleName:pair)(CastExpression(SimpleType(SimpleName:EntryClassPair))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:it))(SimpleName:next)))))(ExpressionStatement(PostfixExpression(SimpleName:counter)(POSTFIX_EXPRESSION_OPERATOR:++)))(VariableDeclarationStatement(SimpleType(QualifiedName:java.lang.Class))(VariableDeclarationFragment(SimpleName:MAGEclass)(MethodInvocation(METHOD_INVOCATION_RECEIVER(QualifiedName:java.lang.Class))(SimpleName:forName)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:pair.className)))))(VariableDeclarationStatement(SimpleType(QualifiedName:java.lang.reflect.Constructor))(VariableDeclarationFragment(SimpleName:constructor)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:MAGEclass))(SimpleName:getConstructor)(METHOD_INVOCATION_ARGUMENTS(NullLiteral)))))(VariableDeclarationStatement(SimpleType(SimpleName:Extendable))(VariableDeclarationFragment(SimpleName:MAGEobj)(NullLiteral)))(TryStatement(Block(ExpressionStatement(Assignment(SimpleName:MAGEobj)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:Extendable))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:constructor))(SimpleName:newInstance)(METHOD_INVOCATION_ARGUMENTS(NullLiteral))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:mgedOntologyClassName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:oh))(SimpleName:resolveOntologyClassNameFromModel)(METHOD_INVOCATION_ARGUMENTS(SimpleName:MAGEobj)(QualifiedName:pair.entryName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:StringOutputHelpers))(SimpleName:writeOutput)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(QualifiedName:pair.className)(StringLiteral:<STR>)(QualifiedName:pair.entryName)(StringLiteral:<STR>))(NumberLiteral:3))))(IfStatement(InfixExpression(SimpleName:mgedOntologyClassName)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:StringOutputHelpers))(SimpleName:writeOutput)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:mgedOntologyClassName)(StringLiteral:<STR>))(NumberLiteral:3))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:mgedOntClassNames))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:mgedOntologyClassName)))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:StringOutputHelpers))(SimpleName:writeOutput)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>))(NumberLiteral:3)))))))(CatchClause(SingleVariableDeclaration(SimpleType(QualifiedName:java.lang.InstantiationException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:StringOutputHelpers))(SimpleName:writeOutput)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:MAGEclass))(SimpleName:getName))(StringLiteral:<STR>))(NumberLiteral:3))))))))))(CatchClause(SingleVariableDeclaration(SimpleType(QualifiedName:java.lang.Exception))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))(ReturnStatement(SimpleName:mgedOntClassNames))))))