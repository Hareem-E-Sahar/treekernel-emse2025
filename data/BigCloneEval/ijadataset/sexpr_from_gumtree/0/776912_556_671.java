(CompilationUnit(TypeDeclaration(Modifier:public)(TYPE_DECLARATION_KIND:class)(SimpleName:Test)(MethodDeclaration(Modifier:public)(PrimitiveType:void)(SimpleName:save)(SimpleType(SimpleName:IOException))(SimpleType(SimpleName:StaleException))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:finest)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>))))(VariableDeclarationStatement(SimpleType(SimpleName:FileChannel))(VariableDeclarationFragment(SimpleName:c)(NullLiteral)))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:ByteBuffer))(VariableDeclarationFragment(SimpleName:buf)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ByteBuffer))(SimpleName:allocate)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:16384)))))(ExpressionStatement(Assignment(SimpleName:c)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(ClassInstanceCreation(SimpleType(SimpleName:RandomAccessFile))(SimpleName:file)(StringLiteral:<STR>)))(SimpleName:getChannel))))(IfStatement(InfixExpression(SimpleName:version)(INFIX_EXPRESSION_OPERATOR:<)(SimpleName:DATABASE_VERSION))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:info)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:version)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:info)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(SimpleName:DATABASE_VERSION)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:verification)(MethodInvocation(SimpleName:encrypt)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:VERIFICATION_STRING))(SimpleName:clone))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:VERIFICATION_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(CastExpression(PrimitiveType:byte)(QualifiedName:verification.length)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:verification))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putInt)(METHOD_INVOCATION_ARGUMENTS(SimpleName:DATABASE_VERSION))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(ExpressionStatement(Assignment(SimpleName:version)(ASSIGNMENT_OPERATOR:=)(SimpleName:DATABASE_VERSION)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:SERIAL_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:limit)(METHOD_INVOCATION_ARGUMENTS(NumberLiteral:8))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:read)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:dbSerial)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:getLong))))(IfStatement(InfixExpression(SimpleName:dbSerial)(INFIX_EXPRESSION_OPERATOR:!=)(SimpleName:serialNumber))(ThrowStatement(ClassInstanceCreation(SimpleType(SimpleName:StaleException))(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putLong)(METHOD_INVOCATION_ARGUMENTS(PrefixExpression(PREFIX_EXPRESSION_OPERATOR:++)(SimpleName:serialNumber)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:SERIAL_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:DATA_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:finest)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:keyPairs))(SimpleName:size))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putInt)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:keyPairs))(SimpleName:size)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:finest)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:folders))(SimpleName:size))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putInt)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:folders))(SimpleName:size)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:finest)(METHOD_INVOCATION_ARGUMENTS(InfixExpression(StringLiteral:<STR>)(INFIX_EXPRESSION_OPERATOR:+)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:entries))(SimpleName:size))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putInt)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:entries))(SimpleName:size)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(VariableDeclarationStatement(PrimitiveType:long)(VariableDeclarationFragment(SimpleName:pos)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:iv)))(TryStatement(Block(VariableDeclarationStatement(SimpleType(SimpleName:Cipher))(VariableDeclarationFragment(SimpleName:symCipher)(MethodInvocation(SimpleName:getCBCSymmetricCipher)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(SimpleName:getKey))))))(VariableDeclarationStatement(SimpleType(SimpleName:AlgorithmParameters))(VariableDeclarationFragment(SimpleName:params)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:getParameters))))(ExpressionStatement(Assignment(SimpleName:iv)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:params))(SimpleName:getEncoded))))(EnhancedForStatement(SingleVariableDeclaration(ParameterizedType(SimpleType(QualifiedName:Map.Entry))(SimpleType(SimpleName:X509Certificate))(SimpleType(SimpleName:PrivateKey)))(SimpleName:pair))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:keyPairs))(SimpleName:entrySet))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:encrypted)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:encoded)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:pair))(SimpleName:getKey)))(SimpleName:getEncoded))))(VariableDeclarationStatement(PrimitiveType:short)(VariableDeclarationFragment(SimpleName:len)(CastExpression(PrimitiveType:short)(QualifiedName:encoded.length))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:putShort)(METHOD_INVOCATION_ARGUMENTS(SimpleName:len))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encoded))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:flip)))(ExpressionStatement(Assignment(SimpleName:encrypted)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:array))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:position))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:remaining))))))(IfStatement(InfixExpression(InfixExpression(SimpleName:encrypted)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(QualifiedName:encrypted.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encrypted)))))(ExpressionStatement(Assignment(SimpleName:encoded)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:pair))(SimpleName:getValue)))(SimpleName:getEncoded))))(ExpressionStatement(Assignment(SimpleName:len)(ASSIGNMENT_OPERATOR:=)(CastExpression(PrimitiveType:short)(QualifiedName:encoded.length))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:putShort)(METHOD_INVOCATION_ARGUMENTS(SimpleName:len))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encoded))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:flip)))(ExpressionStatement(Assignment(SimpleName:encrypted)(ASSIGNMENT_OPERATOR:=)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:array))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:position))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:tmp))(SimpleName:remaining))))))(IfStatement(InfixExpression(InfixExpression(SimpleName:encrypted)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(QualifiedName:encrypted.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encrypted)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:Folder))(SimpleName:f))(SimpleName:folders)(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(SimpleType(SimpleName:ByteBuffer))(VariableDeclarationFragment(SimpleName:b)(MethodInvocation(SimpleName:serialize)(METHOD_INVOCATION_ARGUMENTS(SimpleName:f)))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:encrypted)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:array))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:position))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:remaining))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encrypted))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))))(EnhancedForStatement(SingleVariableDeclaration(SimpleType(SimpleName:PasswordEntry))(SimpleName:e))(SimpleName:entries)(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(SimpleType(SimpleName:ByteBuffer))(VariableDeclarationFragment(SimpleName:b)(MethodInvocation(SimpleName:serialize)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e)))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:encrypted)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:update)(METHOD_INVOCATION_ARGUMENTS(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:array))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:position))(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:b))(SimpleName:remaining))))))(IfStatement(InfixExpression(InfixExpression(SimpleName:encrypted)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(QualifiedName:encrypted.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encrypted)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:finalBlock)(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:symCipher))(SimpleName:doFinal))))(IfStatement(InfixExpression(InfixExpression(SimpleName:finalBlock)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(INFIX_EXPRESSION_OPERATOR:&&)(InfixExpression(QualifiedName:finalBlock.length)(INFIX_EXPRESSION_OPERATOR:>)(NumberLiteral:0)))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:finalBlock))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf)))))))(CatchClause(SingleVariableDeclaration(SimpleType(SimpleName:GeneralSecurityException))(SimpleName:e))(Block(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:log)(METHOD_INVOCATION_ARGUMENTS(QualifiedName:Level.WARNING)(StringLiteral:<STR>)(SimpleName:e))))(VariableDeclarationStatement(SimpleType(SimpleName:IOException))(VariableDeclarationFragment(SimpleName:ioe)(ClassInstanceCreation(SimpleType(SimpleName:IOException))(StringLiteral:<STR>))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:ioe))(SimpleName:initCause)(METHOD_INVOCATION_ARGUMENTS(SimpleName:e))))(ThrowStatement(SimpleName:ioe)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(PrimitiveType:int)(VariableDeclarationFragment(SimpleName:len)(CastExpression(PrimitiveType:int)(ParenthesizedExpression(InfixExpression(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position))(INFIX_EXPRESSION_OPERATOR:-)(SimpleName:pos))))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:putInt)(METHOD_INVOCATION_ARGUMENTS(SimpleName:len))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:DATALEN_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:clear)))(VariableDeclarationStatement(ArrayType(PrimitiveType:byte)(Dimension))(VariableDeclarationFragment(SimpleName:encIv)(MethodInvocation(SimpleName:encrypt)(METHOD_INVOCATION_ARGUMENTS(SimpleName:iv)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(CastExpression(PrimitiveType:byte)(QualifiedName:encIv.length)))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:put)(METHOD_INVOCATION_ARGUMENTS(SimpleName:encIv))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:buf))(SimpleName:flip)))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:position)(METHOD_INVOCATION_ARGUMENTS(SimpleName:IV_OFFSET))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:write)(METHOD_INVOCATION_ARGUMENTS(SimpleName:buf))))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:log))(SimpleName:info)(METHOD_INVOCATION_ARGUMENTS(StringLiteral:<STR>)))))(Block(IfStatement(InfixExpression(SimpleName:c)(INFIX_EXPRESSION_OPERATOR:!=)(NullLiteral))(ExpressionStatement(MethodInvocation(METHOD_INVOCATION_RECEIVER(SimpleName:c))(SimpleName:close))))))))))