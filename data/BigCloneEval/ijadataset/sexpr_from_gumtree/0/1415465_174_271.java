(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(SingleMemberAnnotation(SimpleName:SuppressWarnings)(StringLiteral:<STR>))(Modifier:private)(PrimitiveType:void)(SimpleName:addToOpsList)(SingleVariableDeclaration(SimpleType(SimpleName:ServiceProxy))(SimpleName:serviceToAdd))(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:HashMap))(SimpleType(SimpleName:String))(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:ServiceProxy))))(SimpleName:opsList2))(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:HashMap))(SimpleType(SimpleName:String))(SimpleType(SimpleName:String)))(SimpleName:methodNameToOperation))(Block(VariableDeclarationStatement(SimpleType(SimpleName:MetadataSection))(VariableDeclarationFragment(SimpleName:meta)(NullLiteral)))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:meta)(ASSIGNMENT_OPERATOR:=)(CastExpression(SimpleType(SimpleName:MetadataSection))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceToAdd))(SimpleName:getServiceMetadata)))(SimpleName:getWsdls)))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:DPWSException))(SimpleName:e1))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e1))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e1))(SimpleName:printStackTrace))))))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:URL))(VariableDeclarationFragment(SimpleName:url)(ClassInstanceCreation(SimpleType(SimpleName:URL))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:meta))(SimpleName:getLocation)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:meta))(SimpleName:getLocation))))))(VariableDeclarationStatement(SimpleType(SimpleName:StaxBuilder))(VariableDeclarationFragment(SimpleName:t)(ClassInstanceCreation(SimpleType(SimpleName:StaxBuilder)))))(VariableDeclarationStatement(SimpleType(SimpleName:InputFactory))(VariableDeclarationFragment(SimpleName:n)(ClassInstanceCreation(SimpleType(SimpleName:InputFactory)))))(VariableDeclarationStatement(SimpleType(SimpleName:XMLStreamReader))(VariableDeclarationFragment(SimpleName:reader)(NullLiteral)))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:reader)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:n))(SimpleName:createXMLStreamReader)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:url))(SimpleName:openStream)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:XMLStreamException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))(VariableDeclarationStatement(SimpleType(SimpleName:Document))(VariableDeclarationFragment(SimpleName:testDoc)(NullLiteral)))(TryStatement(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:testDoc)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:t))(SimpleName:build)(METHOD_INVOCATION_ARGUMENTS(SimpleName:reader))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:XMLStreamException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:e))(SimpleName:printStackTrace))))))(VariableDeclarationStatement(SimpleType(SimpleName:List))(VariableDeclarationFragment(SimpleName:contentList1)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:testDoc))(SimpleName:getContent))))(VariableDeclarationStatement(SimpleType(SimpleName:Element))(VariableDeclarationFragment(SimpleName:content1)(CastExpression(SimpleType(SimpleName:Element))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:contentList1))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:ElementFilter))(VariableDeclarationFragment(SimpleName:elFilter)(ClassInstanceCreation(SimpleType(SimpleName:ElementFilter))(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:List))(VariableDeclarationFragment(SimpleName:filteredContent)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:content1))(SimpleName:getContent)(METHOD_INVOCATION_ARGUMENTS(SimpleName:elFilter)))))(VariableDeclarationStatement(SimpleType(SimpleName:Element))(VariableDeclarationFragment(SimpleName:portTypeElement)(CastExpression(SimpleType(SimpleName:Element))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:filteredContent))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:portTypeName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:portTypeElement))(SimpleName:getAttributeValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:ElementFilter))(VariableDeclarationFragment(SimpleName:opsFilter)(ClassInstanceCreation(SimpleType(SimpleName:ElementFilter))(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:List))(VariableDeclarationFragment(SimpleName:filteredOps)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:portTypeElement))(SimpleName:getContent)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opsFilter)))))(ForStatement(VariableDeclarationExpression(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:i)(NumberLiteral:0)))(InfixExpression(SimpleName:i)(INFIX_EXPRESSION_OPERATOR:<)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:filteredOps))(SimpleName:size)))(PostfixExpression(SimpleName:i)(POSTFIX_EXPRESSION_OPERATOR:++))(Block(VariableDeclarationStatement(SimpleType(SimpleName:Element))(VariableDeclarationFragment(SimpleName:opsElement)(CastExpression(SimpleType(SimpleName:Element))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:filteredOps))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(SimpleName:i))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:opName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsElement))(SimpleName:getAttributeValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:ElementFilter))(VariableDeclarationFragment(SimpleName:inputFilter)(ClassInstanceCreation(SimpleType(SimpleName:ElementFilter))(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:List))(VariableDeclarationFragment(SimpleName:inputActionList)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsElement))(SimpleName:getContent)(METHOD_INVOCATION_ARGUMENTS(SimpleName:inputFilter)))))(IfStatement(InfixExpression(InfixExpression(SimpleName:inputActionList)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inputActionList))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:totalRequestString)(NullLiteral)))(VariableDeclarationStatement(SimpleType(SimpleName:Element))(VariableDeclarationFragment(SimpleName:requestElement)(CastExpression(SimpleType(SimpleName:Element))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inputActionList))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:0))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:inputName)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:requestElement))(SimpleName:getAttributeValue)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(VariableDeclarationStatement(SimpleType(SimpleName:StringTokenizer))(VariableDeclarationFragment(SimpleName:tokenizer)(ClassInstanceCreation(SimpleType(SimpleName:StringTokenizer))(SimpleName:inputName)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:prefix)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tokenizer))(SimpleName:nextToken))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:uri)(NullLiteral)))(VariableDeclarationStatement(SimpleType(SimpleName:Iterator))(VariableDeclarationFragment(SimpleName:it)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:requestElement))(SimpleName:getAdditionalNamespaces)))(SimpleName:iterator))))(WhileStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:it))(SimpleName:hasNext))(Block(VariableDeclarationStatement(SimpleType(SimpleName:Namespace))(VariableDeclarationFragment(SimpleName:namespaceAttribute)(CastExpression(SimpleType(SimpleName:Namespace))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:it))(SimpleName:next)))))(IfStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:prefix))(SimpleName:equals)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:namespaceAttribute))(SimpleName:getPrefix))))(Block(ExpressionStatement(Assignment(SimpleName:uri)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:namespaceAttribute))(SimpleName:getURI))))(BreakStatement)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:ElementFilter))(VariableDeclarationFragment(SimpleName:outputFilter)(ClassInstanceCreation(SimpleType(SimpleName:ElementFilter))(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:List))(VariableDeclarationFragment(SimpleName:outputActionList)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsElement))(SimpleName:getContent)(METHOD_INVOCATION_ARGUMENTS(SimpleName:outputFilter)))))(IfStatement(InfixExpression(InfixExpression(SimpleName:outputActionList)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:outputActionList))(SimpleName:size))(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:totalRequestString)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:uri)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:portTypeName)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tokenizer))(SimpleName:nextToken))))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.INFO)(StringLiteral:<STR>))))(ExpressionStatement(Assignment(SimpleName:totalRequestString)(ASSIGNMENT_OPERATOR:=)(InfixExpression(SimpleName:uri)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:portTypeName)(StringLiteral:<STR>)(SimpleName:opName))))))(VariableDeclarationStatement(SimpleType(SimpleName:String))(VariableDeclarationFragment(SimpleName:uniqueOpName)(InfixExpression(SimpleName:opName)(INFIX_EXPRESSION_OPERATOR:+)(StringLiteral:<STR>)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceToAdd))(SimpleName:getId)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:methodNameToOperation))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:uniqueOpName)(SimpleName:totalRequestString))))(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsList2))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opName)))(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsList2))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opName))))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:serviceToAdd)))))(Block(VariableDeclarationStatement(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:ServiceProxy)))(VariableDeclarationFragment(SimpleName:opsList)(ClassInstanceCreation(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:ServiceProxy))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsList))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:serviceToAdd))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsList2))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opName)(SimpleName:opsList))))(IfStatement(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsListForService))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceToAdd))(SimpleName:getId))))(INFIX_EXPRESSION_OPERATOR:==)(NullLiteral))(Block(VariableDeclarationStatement(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:String)))(VariableDeclarationFragment(SimpleName:serviceList)(ClassInstanceCreation(ParameterizedType(SimpleType(SimpleName:ArrayList))(SimpleType(SimpleName:String))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceList))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opName))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsListForService))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceToAdd))(SimpleName:getId))(SimpleName:serviceList)))))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:opsListForService))(SimpleName:get)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:serviceToAdd))(SimpleName:getId)))))(SimpleName:add)(METHOD_INVOCATION_ARGUMENTS(SimpleName:opName)))))))))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:MalformedURLException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:IOException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:logger))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e)))))))))))