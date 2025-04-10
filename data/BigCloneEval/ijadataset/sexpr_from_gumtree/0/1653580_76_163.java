(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(Modifier:static)(PrimitiveType:void)(SimpleName:serialize)(SingleVariableDeclaration(SimpleType(SimpleName:IAlgorithm))(SimpleName:alg))(SingleVariableDeclaration(SimpleType(SimpleName:IGraph))(SimpleName:graph))(SingleVariableDeclaration(SimpleType(SimpleName:IParameterInventory))(SimpleName:inventory))(SingleVariableDeclaration(SimpleType(SimpleName:OutputStream))(SimpleName:os))(SimpleType(SimpleName:IOException))(Block(VariableDeclarationStatement(SimpleType(SimpleName:ZipOutputStream))(VariableDeclarationFragment(SimpleName:zos)(ClassInstanceCreation(SimpleType(SimpleName:ZipOutputStream))(SimpleName:os))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:putNextEntry)(METHOD_INVOCATION_ARGUMENTS(ClassInstanceCreation(SimpleType(SimpleName:ZipEntry))(StringLiteral:<STR>)))))(VariableDeclarationStatement(SimpleType(SimpleName:OutputStreamWriter))(VariableDeclarationFragment(SimpleName:w)(ClassInstanceCreation(SimpleType(SimpleName:OutputStreamWriter))(SimpleName:zos))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:alg))(SimpleName:getClass)))(SimpleName:getName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:graph))(SimpleName:getGraphName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:graph))(SimpleName:getAbscissaName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:graph))(SimpleName:getOrdinateName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:graph))(SimpleName:getDescription)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:Entry))(SimpleType(SimpleName:String))(SimpleType(SimpleName:Object)))(SimpleName:ntr))(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:graph))(SimpleName:getCurveFamily)))(SimpleName:entrySet))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getKey)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:alg))(SimpleName:getParameter)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getKey)))))(SimpleName:serialize)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getValue)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:String))(SimpleName:valueOf)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inventory))(SimpleName:getRunsPerSet)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:String))(SimpleName:s))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inventory))(SimpleName:getChangingParamNames))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:s))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:AdditionalHandler))(SimpleName:ah))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inventory))(SimpleName:getAdditionalHandlers))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ah))(SimpleName:getHandlerName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ah))(SimpleName:getActualValueName)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ah))(SimpleName:getHandler)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:Map))(SimpleType(SimpleName:String))(SimpleType(SimpleName:IValue)))(SimpleName:inputMap))(SimpleName:inventory)(Block(VariableDeclarationStatement(ParameterizedType(SimpleType(SimpleName:Map))(SimpleType(SimpleName:String))(SimpleType(SimpleName:IValue)))(VariableDeclarationFragment(SimpleName:retMap)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inventory))(SimpleName:getReturnValues)(METHOD_INVOCATION_ARGUMENTS(SimpleName:inputMap)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:Entry))(SimpleType(SimpleName:String))(SimpleType(SimpleName:IValue)))(SimpleName:ntr))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:inputMap))(SimpleName:entrySet))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getKey)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getValue)))(SimpleName:parameter)))(SimpleName:serialize)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getValue)))(SimpleName:value)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(EnhancedForStatement(SingleVariableDeclaration(ParameterizedType(SimpleType(SimpleName:Entry))(SimpleType(SimpleName:String))(SimpleType(SimpleName:IValue)))(SimpleName:ntr))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:retMap))(SimpleName:entrySet))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getKey)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getValue)))(SimpleName:parameter)))(SimpleName:serialize)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ntr))(SimpleName:getValue)))(SimpleName:value)))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:w))(SimpleName:flush)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:closeEntry)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:zos))(SimpleName:close)))))))