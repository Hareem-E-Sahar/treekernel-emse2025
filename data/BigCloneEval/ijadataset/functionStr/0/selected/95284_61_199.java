public class Test {    public static void main(String[] args) {
        if (args.length != 2) {
            printUsage();
            System.exit(1);
        }
        try {
            Security.addProvider(new IAIK());
            Module pkcs11Module = Module.getInstance(args[0]);
            pkcs11Module.initialize(null);
            Token token = Util.selectToken(pkcs11Module, output_, input_);
            TokenInfo tokenInfo = token.getTokenInfo();
            output_.println("################################################################################");
            output_.println("Information of Token:");
            output_.println(tokenInfo);
            output_.println("################################################################################");
            Session session = Util.openAuthorizedSession(token, Token.SessionReadWriteBehavior.RW_SESSION, output_, input_);
            output_.println("################################################################################");
            output_.print("Generating new 1024 bit RSA key-pair... ");
            output_.flush();
            HashSet supportedMechanisms = new HashSet(Arrays.asList(token.getMechanismList()));
            MechanismInfo signatureMechanismInfo;
            if (supportedMechanisms.contains(Mechanism.RSA_PKCS)) {
                signatureMechanismInfo = token.getMechanismInfo(Mechanism.RSA_PKCS);
            } else if (supportedMechanisms.contains(Mechanism.RSA_X_509)) {
                signatureMechanismInfo = token.getMechanismInfo(Mechanism.RSA_X_509);
            } else if (supportedMechanisms.contains(Mechanism.RSA_9796)) {
                signatureMechanismInfo = token.getMechanismInfo(Mechanism.RSA_9796);
            } else if (supportedMechanisms.contains(Mechanism.RSA_PKCS_OAEP)) {
                signatureMechanismInfo = token.getMechanismInfo(Mechanism.RSA_PKCS_OAEP);
            } else {
                signatureMechanismInfo = null;
            }
            Mechanism keyPairGenerationMechanism = Mechanism.RSA_PKCS_KEY_PAIR_GEN;
            RSAPublicKey rsaPublicKeyTemplate = new RSAPublicKey();
            RSAPrivateKey rsaPrivateKeyTemplate = new RSAPrivateKey();
            rsaPublicKeyTemplate.getModulusBits().setLongValue(new Long(1024));
            byte[] publicExponentBytes = { 0x01, 0x00, 0x01 };
            rsaPublicKeyTemplate.getPublicExponent().setByteArrayValue(publicExponentBytes);
            rsaPublicKeyTemplate.getToken().setBooleanValue(Boolean.FALSE);
            byte[] id = new byte[20];
            new Random().nextBytes(id);
            rsaPublicKeyTemplate.getId().setByteArrayValue(id);
            rsaPrivateKeyTemplate.getSensitive().setBooleanValue(Boolean.TRUE);
            rsaPrivateKeyTemplate.getToken().setBooleanValue(Boolean.FALSE);
            rsaPrivateKeyTemplate.getPrivate().setBooleanValue(Boolean.TRUE);
            rsaPrivateKeyTemplate.getId().setByteArrayValue(id);
            try {
                Version cryptokiVersion = pkcs11Module.getInfo().getCryptokiVersion();
                if ((cryptokiVersion.getMajor() >= 2) && (cryptokiVersion.getMinor() >= 20)) {
                    GenericTemplate wrapTemplate = new GenericTemplate();
                    GregorianCalendar startDate = new GregorianCalendar();
                    startDate.add(Calendar.HOUR_OF_DAY, -1);
                    DateAttribute startDateAttr = new DateAttribute(Attribute.START_DATE);
                    startDateAttr.setDateValue(startDate.getTime());
                    wrapTemplate.addAttribute(startDateAttr);
                    GregorianCalendar endDate = new GregorianCalendar();
                    endDate.add(Calendar.MONTH, 11);
                    DateAttribute endDateAttr = new DateAttribute(Attribute.END_DATE);
                    endDateAttr.setDateValue(endDate.getTime());
                    wrapTemplate.addAttribute(endDateAttr);
                    rsaPublicKeyTemplate.getWrapTemplate().setAttributeArrayValue(wrapTemplate);
                    Mechanism[] allowedMechanisms = new Mechanism[1];
                    Mechanism mechanism1 = new Mechanism(PKCS11Constants.CKM_RSA_PKCS);
                    allowedMechanisms[0] = mechanism1;
                    rsaPrivateKeyTemplate.getAllowedMechanisms().setMechanismAttributeArrayValue(allowedMechanisms);
                }
            } catch (TokenException e) {
            }
            if (signatureMechanismInfo != null) {
                rsaPublicKeyTemplate.getVerify().setBooleanValue(new Boolean(signatureMechanismInfo.isVerify()));
                rsaPublicKeyTemplate.getVerifyRecover().setBooleanValue(new Boolean(signatureMechanismInfo.isVerifyRecover()));
                rsaPublicKeyTemplate.getEncrypt().setBooleanValue(new Boolean(signatureMechanismInfo.isEncrypt()));
                rsaPublicKeyTemplate.getDerive().setBooleanValue(new Boolean(signatureMechanismInfo.isDerive()));
                rsaPublicKeyTemplate.getWrap().setBooleanValue(new Boolean(signatureMechanismInfo.isWrap()));
                rsaPrivateKeyTemplate.getSign().setBooleanValue(new Boolean(signatureMechanismInfo.isSign()));
                rsaPrivateKeyTemplate.getSignRecover().setBooleanValue(new Boolean(signatureMechanismInfo.isSignRecover()));
                rsaPrivateKeyTemplate.getDecrypt().setBooleanValue(new Boolean(signatureMechanismInfo.isDecrypt()));
                rsaPrivateKeyTemplate.getDerive().setBooleanValue(new Boolean(signatureMechanismInfo.isDerive()));
                rsaPrivateKeyTemplate.getUnwrap().setBooleanValue(new Boolean(signatureMechanismInfo.isUnwrap()));
            } else {
                rsaPrivateKeyTemplate.getSign().setBooleanValue(Boolean.TRUE);
                rsaPrivateKeyTemplate.getDecrypt().setBooleanValue(Boolean.TRUE);
                rsaPublicKeyTemplate.getVerify().setBooleanValue(Boolean.TRUE);
                rsaPublicKeyTemplate.getEncrypt().setBooleanValue(Boolean.TRUE);
            }
            rsaPublicKeyTemplate.getKeyType().setPresent(false);
            rsaPublicKeyTemplate.getObjectClass().setPresent(false);
            rsaPrivateKeyTemplate.getKeyType().setPresent(false);
            rsaPrivateKeyTemplate.getObjectClass().setPresent(false);
            long time0 = System.currentTimeMillis();
            KeyPair generatedKeyPair = session.generateKeyPair(keyPairGenerationMechanism, rsaPublicKeyTemplate, rsaPrivateKeyTemplate);
            long time1 = System.currentTimeMillis();
            RSAPublicKey generatedRSAPublicKey = (RSAPublicKey) generatedKeyPair.getPublicKey();
            RSAPrivateKey generatedRSAPrivateKey = (RSAPrivateKey) generatedKeyPair.getPrivateKey();
            output_.println("Success [took " + (time1 - time0) + " milliseconds]");
            output_.println("The public key is");
            output_.println("_______________________________________________________________________________");
            output_.println(generatedRSAPublicKey);
            output_.println("_______________________________________________________________________________");
            output_.println("The private key is");
            output_.println("_______________________________________________________________________________");
            output_.println(generatedRSAPrivateKey);
            output_.println("_______________________________________________________________________________");
            output_.println("################################################################################");
            output_.println("Writing the public key of the generated key-pair to file: " + args[1]);
            RSAPublicKey exportableRsaPublicKey = generatedRSAPublicKey;
            BigInteger modulus = new BigInteger(1, exportableRsaPublicKey.getModulus().getByteArrayValue());
            BigInteger publicExponent = new BigInteger(1, exportableRsaPublicKey.getPublicExponent().getByteArrayValue());
            RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(modulus, publicExponent);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            java.security.interfaces.RSAPublicKey javaRsaPublicKey = (java.security.interfaces.RSAPublicKey) keyFactory.generatePublic(rsaPublicKeySpec);
            X509EncodedKeySpec x509EncodedPublicKey = (X509EncodedKeySpec) keyFactory.getKeySpec(javaRsaPublicKey, X509EncodedKeySpec.class);
            FileOutputStream publicKeyFileStream = new FileOutputStream(args[1]);
            publicKeyFileStream.write(x509EncodedPublicKey.getEncoded());
            publicKeyFileStream.flush();
            publicKeyFileStream.close();
            output_.println("################################################################################");
            output_.println("################################################################################");
            output_.println("Trying to search for the public key of the generated key-pair by ID: " + Functions.toHexString(id));
            RSAPublicKey exportRsaPublicKeyTemplate = new RSAPublicKey();
            exportRsaPublicKeyTemplate.getId().setByteArrayValue(id);
            session.findObjectsInit(exportRsaPublicKeyTemplate);
            Object[] foundPublicKeys = session.findObjects(1);
            session.findObjectsFinal();
            if (foundPublicKeys.length != 1) {
                output_.println("Error: Cannot find the public key under the given ID!");
            } else {
                output_.println("Found public key!");
                output_.println("_______________________________________________________________________________");
                output_.println(foundPublicKeys[0]);
                output_.println("_______________________________________________________________________________");
            }
            output_.println("################################################################################");
            session.closeSession();
            pkcs11Module.finalize(null);
        } catch (Throwable thr) {
            thr.printStackTrace();
        }
    }
}