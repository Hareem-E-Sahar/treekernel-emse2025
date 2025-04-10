package org.apache.rampart.builder;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.rahas.EncryptedKeyToken;
import org.apache.rahas.RahasConstants;
import org.apache.rahas.TrustException;
import org.apache.rampart.RampartConstants;
import org.apache.rampart.RampartException;
import org.apache.rampart.RampartMessageData;
import org.apache.rampart.policy.RampartPolicyData;
import org.apache.rampart.util.RampartUtil;
import org.apache.ws.secpolicy.SPConstants;
import org.apache.ws.secpolicy.model.AlgorithmSuite;
import org.apache.ws.secpolicy.model.IssuedToken;
import org.apache.ws.secpolicy.model.SecureConversationToken;
import org.apache.ws.secpolicy.model.SupportingToken;
import org.apache.ws.secpolicy.model.Token;
import org.apache.ws.secpolicy.model.X509Token;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSEncryptionPart;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.conversation.ConversationConstants;
import org.apache.ws.security.conversation.ConversationException;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.apache.ws.security.handler.WSHandlerResult;
import org.apache.ws.security.message.WSSecDKEncrypt;
import org.apache.ws.security.message.WSSecEncrypt;
import org.apache.ws.security.message.WSSecEncryptedKey;
import org.apache.ws.security.message.token.SecurityTokenReference;
import org.apache.ws.security.util.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class SymmetricBindingBuilder extends BindingBuilder {

    private static Log log = LogFactory.getLog(SymmetricBindingBuilder.class);

    private static Log tlog = LogFactory.getLog(RampartConstants.TIME_LOG);

    private boolean dotDebug = false;

    public SymmetricBindingBuilder() {
        dotDebug = tlog.isDebugEnabled();
    }

    public void build(RampartMessageData rmd) throws RampartException {
        log.debug("SymmetricBindingBuilder build invoked");
        RampartPolicyData rpd = rmd.getPolicyData();
        if (rpd.isIncludeTimestamp()) {
            this.addTimestamp(rmd);
        }
        if (rmd.isInitiator()) {
            initializeTokens(rmd);
        }
        if (SPConstants.ENCRYPT_BEFORE_SIGNING.equals(rpd.getProtectionOrder())) {
            this.doEncryptBeforeSig(rmd);
        } else {
            this.doSignBeforeEncrypt(rmd);
        }
        log.debug("SymmetricBindingBuilder build invoked : DONE");
    }

    private void doEncryptBeforeSig(RampartMessageData rmd) throws RampartException {
        long t0 = 0, t1 = 0, t2 = 0;
        RampartPolicyData rpd = rmd.getPolicyData();
        Vector signatureValues = new Vector();
        if (dotDebug) {
            t0 = System.currentTimeMillis();
        }
        Token encryptionToken = rpd.getEncryptionToken();
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);
        Vector sigParts = RampartUtil.getSignedParts(rmd);
        if (encryptionToken == null && encrParts.size() > 0) {
            throw new RampartException("encryptionTokenMissing");
        }
        if (encryptionToken != null && encrParts.size() > 0) {
            String tokenId = null;
            org.apache.rahas.Token tok = null;
            if (encryptionToken instanceof IssuedToken) {
                tokenId = rmd.getIssuedEncryptionTokenId();
                log.debug("Issued EncryptionToken Id : " + tokenId);
            } else if (encryptionToken instanceof SecureConversationToken) {
                tokenId = rmd.getSecConvTokenId();
                log.debug("SCT Id : " + tokenId);
            } else if (encryptionToken instanceof X509Token) {
                if (rmd.isInitiator()) {
                    tokenId = setupEncryptedKey(rmd, encryptionToken);
                } else {
                    tokenId = getEncryptedKey(rmd);
                }
            }
            if (tokenId == null || tokenId.length() == 0) {
                throw new RampartException("noSecurityToken");
            }
            if (tokenId.startsWith("#")) {
                tokenId = tokenId.substring(1);
            }
            tok = this.getToken(rmd, tokenId);
            boolean attached = false;
            Element encrTokenElement = null;
            Element refList = null;
            WSSecDKEncrypt dkEncr = null;
            WSSecEncrypt encr = null;
            Element encrDKTokenElem = null;
            if (SPConstants.INCLUDE_TOEKN_ALWAYS == encryptionToken.getInclusion() || SPConstants.INCLUDE_TOKEN_ONCE == encryptionToken.getInclusion() || (rmd.isInitiator() && SPConstants.INCLUDE_TOEKN_ALWAYS_TO_RECIPIENT == encryptionToken.getInclusion())) {
                encrTokenElement = RampartUtil.appendChildToSecHeader(rmd, tok.getToken());
                attached = true;
            } else if (encryptionToken instanceof X509Token && rmd.isInitiator()) {
                encrTokenElement = RampartUtil.appendChildToSecHeader(rmd, tok.getToken());
            }
            Document doc = rmd.getDocument();
            AlgorithmSuite algorithmSuite = rpd.getAlgorithmSuite();
            if (encryptionToken.isDerivedKeys()) {
                log.debug("Use drived keys");
                dkEncr = new WSSecDKEncrypt();
                if (attached && tok.getAttachedReference() != null) {
                    dkEncr.setExternalKey(tok.getSecret(), (Element) doc.importNode((Element) tok.getAttachedReference(), true));
                } else if (tok.getUnattachedReference() != null) {
                    dkEncr.setExternalKey(tok.getSecret(), (Element) doc.importNode((Element) tok.getUnattachedReference(), true));
                } else {
                    dkEncr.setExternalKey(tok.getSecret(), tok.getId());
                }
                try {
                    dkEncr.setSymmetricEncAlgorithm(algorithmSuite.getEncryption());
                    dkEncr.setDerivedKeyLength(algorithmSuite.getEncryptionDerivedKeyLength() / 8);
                    dkEncr.prepare(doc);
                    encrDKTokenElem = dkEncr.getdktElement();
                    RampartUtil.appendChildToSecHeader(rmd, encrDKTokenElem);
                    refList = dkEncr.encryptForExternalRef(null, encrParts);
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInDKEncr");
                } catch (ConversationException e) {
                    throw new RampartException("errorInDKEncr");
                }
            } else {
                log.debug("NO derived keys, use the shared secret");
                encr = new WSSecEncrypt();
                encr.setWsConfig(rmd.getConfig());
                encr.setEncKeyId(tokenId);
                RampartUtil.setEncryptionUser(rmd, encr);
                encr.setEphemeralKey(tok.getSecret());
                encr.setDocument(doc);
                encr.setSymmetricEncAlgorithm(algorithmSuite.getEncryption());
                encr.setEncryptSymmKey(false);
                if (!rmd.isInitiator() && tok instanceof EncryptedKeyToken) {
                    encr.setUseKeyIdentifier(true);
                    encr.setCustomReferenceValue(((EncryptedKeyToken) tok).getSHA1());
                    encr.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
                }
                try {
                    encr.prepare(doc, RampartUtil.getEncryptionCrypto(rpd.getRampartConfig(), rmd.getCustomClassLoader()));
                    refList = encr.encryptForExternalRef(null, encrParts);
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInEncryption", e);
                }
            }
            this.mainRefListElement = RampartUtil.appendChildToSecHeader(rmd, refList);
            if (dotDebug) {
                t1 = System.currentTimeMillis();
            }
            if (encrTokenElement != null) {
                this.setInsertionLocation(encrTokenElement);
            } else if (timestampElement != null) {
                this.setInsertionLocation(timestampElement);
            }
            RampartUtil.handleEncryptedSignedHeaders(encrParts, sigParts, doc);
            HashMap sigSuppTokMap = null;
            HashMap endSuppTokMap = null;
            HashMap sgndEndSuppTokMap = null;
            HashMap sgndEncSuppTokMap = null;
            HashMap endEncSuppTokMap = null;
            HashMap sgndEndEncSuppTokMap = null;
            if (this.timestampElement != null) {
                sigParts.add(new WSEncryptionPart(RampartUtil.addWsuIdToElement((OMElement) this.timestampElement)));
            }
            if (rmd.isInitiator()) {
                SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
                sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);
                SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();
                endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);
                SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
                sgndEndSuppTokMap = this.handleSupportingTokens(rmd, sgndEndSuppTokens);
                SupportingToken sgndEncryptedSuppTokens = rpd.getSignedEncryptedSupportingTokens();
                sgndEncSuppTokMap = this.handleSupportingTokens(rmd, sgndEncryptedSuppTokens);
                SupportingToken endorsingEncryptedSuppTokens = rpd.getEndorsingEncryptedSupportingTokens();
                endEncSuppTokMap = this.handleSupportingTokens(rmd, endorsingEncryptedSuppTokens);
                SupportingToken sgndEndEncSuppTokens = rpd.getSignedEndorsingEncryptedSupportingTokens();
                sgndEndEncSuppTokMap = this.handleSupportingTokens(rmd, sgndEndEncSuppTokens);
                Vector supportingToks = rpd.getSupportingTokensList();
                for (int i = 0; i < supportingToks.size(); i++) {
                    this.handleSupportingTokens(rmd, (SupportingToken) supportingToks.get(i));
                }
                SupportingToken encryptedSupportingToks = rpd.getEncryptedSupportingTokens();
                this.handleSupportingTokens(rmd, encryptedSupportingToks);
                sigParts = addSignatureParts(sigSuppTokMap, sigParts);
                sigParts = addSignatureParts(sgndEncSuppTokMap, sigParts);
                sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
                sigParts = addSignatureParts(sgndEndEncSuppTokMap, sigParts);
            } else {
                addSignatureConfirmation(rmd, sigParts);
            }
            if (sigParts.size() > 0) {
                signatureValues.add(this.doSymmSignature(rmd, encryptionToken, tok, sigParts));
                this.mainSigId = RampartUtil.addWsuIdToElement((OMElement) this.getInsertionLocation());
            }
            if (rmd.isInitiator()) {
                endSuppTokMap.putAll(endEncSuppTokMap);
                Vector endSigVals = this.doEndorsedSignatures(rmd, endSuppTokMap);
                for (Iterator iter = endSigVals.iterator(); iter.hasNext(); ) {
                    signatureValues.add(iter.next());
                }
                sgndEndSuppTokMap.putAll(sgndEndEncSuppTokMap);
                Vector sigEndSigVals = this.doEndorsedSignatures(rmd, sgndEndSuppTokMap);
                for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext(); ) {
                    signatureValues.add(iter.next());
                }
            }
            if (dotDebug) {
                t2 = System.currentTimeMillis();
                tlog.debug("Encryption took :" + (t1 - t0) + ", Signature tool :" + (t2 - t1));
            }
            if (rpd.isSignatureProtection() && this.mainSigId != null || encryptedTokensIdList.size() > 0 && rmd.isInitiator()) {
                long t3 = 0, t4 = 0;
                if (dotDebug) {
                    t3 = System.currentTimeMillis();
                }
                log.debug("Signature protection");
                Vector secondEncrParts = new Vector();
                if (rpd.isSignatureProtection()) {
                    secondEncrParts.add(new WSEncryptionPart(this.mainSigId, "Element"));
                }
                if (rmd.isInitiator()) {
                    for (int i = 0; i < encryptedTokensIdList.size(); i++) {
                        secondEncrParts.add(new WSEncryptionPart((String) encryptedTokensIdList.get(i), "Element"));
                    }
                }
                Element secondRefList = null;
                if (encryptionToken.isDerivedKeys()) {
                    try {
                        secondRefList = dkEncr.encryptForExternalRef(null, secondEncrParts);
                        RampartUtil.insertSiblingAfter(rmd, encrDKTokenElem, secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInDKEncr");
                    }
                } else {
                    try {
                        secondRefList = encr.encryptForExternalRef(null, encrParts);
                        RampartUtil.insertSiblingAfter(rmd, encrTokenElement, secondRefList);
                    } catch (WSSecurityException e) {
                        throw new RampartException("errorInEncryption", e);
                    }
                }
                if (dotDebug) {
                    t4 = System.currentTimeMillis();
                    tlog.debug("Signature protection took :" + (t4 - t3));
                }
            }
        } else {
            throw new RampartException("encryptionTokenMissing");
        }
    }

    private void doSignBeforeEncrypt(RampartMessageData rmd) throws RampartException {
        long t0 = 0, t1 = 0, t2 = 0;
        RampartPolicyData rpd = rmd.getPolicyData();
        Document doc = rmd.getDocument();
        if (dotDebug) {
            t0 = System.currentTimeMillis();
        }
        Token sigToken = rpd.getSignatureToken();
        String encrTokId = null;
        String sigTokId = null;
        org.apache.rahas.Token encrTok = null;
        org.apache.rahas.Token sigTok = null;
        Element sigTokElem = null;
        Vector signatureValues = new Vector();
        if (sigToken != null) {
            if (sigToken instanceof SecureConversationToken) {
                sigTokId = rmd.getSecConvTokenId();
            } else if (sigToken instanceof IssuedToken) {
                sigTokId = rmd.getIssuedSignatureTokenId();
            } else if (sigToken instanceof X509Token) {
                if (rmd.isInitiator()) {
                    sigTokId = setupEncryptedKey(rmd, sigToken);
                } else {
                    sigTokId = getEncryptedKey(rmd);
                }
            }
        } else {
            throw new RampartException("signatureTokenMissing");
        }
        if (sigTokId == null || sigTokId.length() == 0) {
            throw new RampartException("noSecurityToken");
        }
        sigTok = this.getToken(rmd, sigTokId);
        if (SPConstants.INCLUDE_TOEKN_ALWAYS == sigToken.getInclusion() || SPConstants.INCLUDE_TOKEN_ONCE == sigToken.getInclusion() || (rmd.isInitiator() && SPConstants.INCLUDE_TOEKN_ALWAYS_TO_RECIPIENT == sigToken.getInclusion())) {
            sigTokElem = RampartUtil.appendChildToSecHeader(rmd, sigTok.getToken());
            this.setInsertionLocation(sigTokElem);
        } else if ((rmd.isInitiator() && sigToken instanceof X509Token) || sigToken instanceof SecureConversationToken) {
            sigTokElem = RampartUtil.appendChildToSecHeader(rmd, sigTok.getToken());
            this.setInsertionLocation(sigTokElem);
        }
        HashMap sigSuppTokMap = null;
        HashMap endSuppTokMap = null;
        HashMap sgndEndSuppTokMap = null;
        HashMap sgndEncSuppTokMap = null;
        HashMap endEncSuppTokMap = null;
        HashMap sgndEndEncSuppTokMap = null;
        Vector sigParts = RampartUtil.getSignedParts(rmd);
        if (this.timestampElement != null) {
            sigParts.add(new WSEncryptionPart(RampartUtil.addWsuIdToElement((OMElement) this.timestampElement)));
        }
        if (rmd.isInitiator()) {
            SupportingToken sgndSuppTokens = rpd.getSignedSupportingTokens();
            sigSuppTokMap = this.handleSupportingTokens(rmd, sgndSuppTokens);
            SupportingToken endSuppTokens = rpd.getEndorsingSupportingTokens();
            endSuppTokMap = this.handleSupportingTokens(rmd, endSuppTokens);
            SupportingToken sgndEndSuppTokens = rpd.getSignedEndorsingSupportingTokens();
            sgndEndSuppTokMap = this.handleSupportingTokens(rmd, sgndEndSuppTokens);
            SupportingToken sgndEncryptedSuppTokens = rpd.getSignedEncryptedSupportingTokens();
            sgndEncSuppTokMap = this.handleSupportingTokens(rmd, sgndEncryptedSuppTokens);
            SupportingToken endorsingEncryptedSuppTokens = rpd.getEndorsingEncryptedSupportingTokens();
            endEncSuppTokMap = this.handleSupportingTokens(rmd, endorsingEncryptedSuppTokens);
            SupportingToken sgndEndEncSuppTokens = rpd.getSignedEndorsingEncryptedSupportingTokens();
            sgndEndEncSuppTokMap = this.handleSupportingTokens(rmd, sgndEndEncSuppTokens);
            Vector supportingToks = rpd.getSupportingTokensList();
            for (int i = 0; i < supportingToks.size(); i++) {
                this.handleSupportingTokens(rmd, (SupportingToken) supportingToks.get(i));
            }
            SupportingToken encryptedSupportingToks = rpd.getEncryptedSupportingTokens();
            this.handleSupportingTokens(rmd, encryptedSupportingToks);
            sigParts = addSignatureParts(sigSuppTokMap, sigParts);
            sigParts = addSignatureParts(sgndEncSuppTokMap, sigParts);
            sigParts = addSignatureParts(sgndEndSuppTokMap, sigParts);
            sigParts = addSignatureParts(sgndEndEncSuppTokMap, sigParts);
        } else {
            addSignatureConfirmation(rmd, sigParts);
        }
        if (sigParts.size() > 0) {
            signatureValues.add(this.doSymmSignature(rmd, sigToken, sigTok, sigParts));
            this.mainSigId = RampartUtil.addWsuIdToElement((OMElement) this.getInsertionLocation());
        }
        if (rmd.isInitiator()) {
            endSuppTokMap.putAll(endEncSuppTokMap);
            Vector endSigVals = this.doEndorsedSignatures(rmd, endSuppTokMap);
            for (Iterator iter = endSigVals.iterator(); iter.hasNext(); ) {
                signatureValues.add(iter.next());
            }
            sgndEndSuppTokMap.putAll(sgndEndEncSuppTokMap);
            Vector sigEndSigVals = this.doEndorsedSignatures(rmd, sgndEndSuppTokMap);
            for (Iterator iter = sigEndSigVals.iterator(); iter.hasNext(); ) {
                signatureValues.add(iter.next());
            }
        }
        if (dotDebug) {
            t1 = System.currentTimeMillis();
        }
        Token encrToken = rpd.getEncryptionToken();
        Element encrTokElem = null;
        if (sigToken.equals(encrToken)) {
            encrTokId = sigTokId;
            encrTok = sigTok;
            encrTokElem = sigTokElem;
        } else {
            encrTokId = rmd.getIssuedEncryptionTokenId();
            encrTok = this.getToken(rmd, encrTokId);
            if (SPConstants.INCLUDE_TOEKN_ALWAYS == encrToken.getInclusion() || SPConstants.INCLUDE_TOKEN_ONCE == encrToken.getInclusion() || (rmd.isInitiator() && SPConstants.INCLUDE_TOEKN_ALWAYS_TO_RECIPIENT == encrToken.getInclusion())) {
                encrTokElem = (Element) encrTok.getToken();
                RampartUtil.insertSiblingBefore(rmd, sigTokElem, encrTokElem);
            }
        }
        Vector encrParts = RampartUtil.getEncryptedParts(rmd);
        if (rpd.isSignatureProtection() && this.mainSigId != null) {
            encrParts.add(new WSEncryptionPart(this.mainSigId, "Element"));
        }
        if (rmd.isInitiator()) {
            for (int i = 0; i < encryptedTokensIdList.size(); i++) {
                encrParts.add(new WSEncryptionPart((String) encryptedTokensIdList.get(i), "Element"));
            }
        }
        Element refList = null;
        if (encrParts.size() > 0) {
            if (encrToken.isDerivedKeys()) {
                try {
                    WSSecDKEncrypt dkEncr = new WSSecDKEncrypt();
                    if (SPConstants.SP_V12 == encrToken.getVersion()) {
                        dkEncr.setWscVersion(ConversationConstants.VERSION_05_12);
                    }
                    if (encrTokElem != null && encrTok.getAttachedReference() != null) {
                        dkEncr.setExternalKey(encrTok.getSecret(), (Element) doc.importNode((Element) encrTok.getAttachedReference(), true));
                    } else if (encrTok.getUnattachedReference() != null) {
                        dkEncr.setExternalKey(encrTok.getSecret(), (Element) doc.importNode((Element) encrTok.getUnattachedReference(), true));
                    } else if (!rmd.isInitiator() && encrToken.isDerivedKeys()) {
                        SecurityTokenReference tokenRef = new SecurityTokenReference(doc);
                        if (encrTok instanceof EncryptedKeyToken) {
                            tokenRef.setKeyIdentifierEncKeySHA1(((EncryptedKeyToken) encrTok).getSHA1());
                        }
                        dkEncr.setExternalKey(encrTok.getSecret(), tokenRef.getElement());
                    } else {
                        dkEncr.setExternalKey(encrTok.getSecret(), encrTok.getId());
                    }
                    if (encrTok instanceof EncryptedKeyToken) {
                        dkEncr.setCustomValueType(WSConstants.SOAPMESSAGE_NS11 + "#" + WSConstants.ENC_KEY_VALUE_TYPE);
                    }
                    dkEncr.setSymmetricEncAlgorithm(rpd.getAlgorithmSuite().getEncryption());
                    dkEncr.setDerivedKeyLength(rpd.getAlgorithmSuite().getEncryptionDerivedKeyLength() / 8);
                    dkEncr.prepare(doc);
                    Element encrDKTokenElem = null;
                    encrDKTokenElem = dkEncr.getdktElement();
                    if (encrTokElem != null) {
                        RampartUtil.insertSiblingAfter(rmd, encrTokElem, encrDKTokenElem);
                    } else if (timestampElement != null) {
                        RampartUtil.insertSiblingAfter(rmd, this.timestampElement, encrDKTokenElem);
                    } else {
                        RampartUtil.insertSiblingBefore(rmd, this.getInsertionLocation(), encrDKTokenElem);
                    }
                    refList = dkEncr.encryptForExternalRef(null, encrParts);
                    RampartUtil.insertSiblingAfter(rmd, encrDKTokenElem, refList);
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInDKEncr");
                } catch (ConversationException e) {
                    throw new RampartException("errorInDKEncr");
                }
            } else {
                try {
                    WSSecEncrypt encr = new WSSecEncrypt();
                    encr.setWsConfig(rmd.getConfig());
                    if (encrTokId.startsWith("#")) {
                        encrTokId = encrTokId.substring(1);
                    }
                    encr.setEncKeyId(encrTokId);
                    encr.setEphemeralKey(encrTok.getSecret());
                    RampartUtil.setEncryptionUser(rmd, encr);
                    encr.setDocument(doc);
                    encr.setEncryptSymmKey(false);
                    encr.setSymmetricEncAlgorithm(rpd.getAlgorithmSuite().getEncryption());
                    if (!rmd.isInitiator()) {
                        if (encrTok instanceof EncryptedKeyToken) {
                            encr.setUseKeyIdentifier(true);
                            encr.setCustomReferenceValue(((EncryptedKeyToken) encrTok).getSHA1());
                            encr.setKeyIdentifierType(WSConstants.ENCRYPTED_KEY_SHA1_IDENTIFIER);
                        }
                    }
                    encr.prepare(doc, RampartUtil.getEncryptionCrypto(rpd.getRampartConfig(), rmd.getCustomClassLoader()));
                    refList = encr.encryptForExternalRef(null, encrParts);
                    if (encrTokElem != null) {
                        RampartUtil.insertSiblingAfter(rmd, encrTokElem, refList);
                    } else {
                        RampartUtil.insertSiblingBeforeOrPrepend(rmd, this.getInsertionLocation(), refList);
                    }
                } catch (WSSecurityException e) {
                    throw new RampartException("errorInEncryption", e);
                }
            }
        }
        if (dotDebug) {
            t2 = System.currentTimeMillis();
            tlog.debug("Signature took :" + (t1 - t0) + ", Encryption took :" + (t2 - t1));
        }
    }

    /**
     * @param rmd
     * @param sigToken
     * @return 
     * @throws RampartException
     */
    private String setupEncryptedKey(RampartMessageData rmd, Token sigToken) throws RampartException {
        try {
            WSSecEncryptedKey encrKey = this.getEncryptedKeyBuilder(rmd, sigToken);
            String id = encrKey.getId();
            byte[] secret = encrKey.getEphemeralKey();
            Date created = new Date();
            Date expires = new Date();
            expires.setTime(System.currentTimeMillis() + 300000);
            org.apache.rahas.EncryptedKeyToken tempTok = new org.apache.rahas.EncryptedKeyToken(id, (OMElement) encrKey.getEncryptedKeyElement(), created, expires);
            tempTok.setSecret(secret);
            tempTok.setSHA1(getSHA1(encrKey.getEncryptedEphemeralKey()));
            rmd.getTokenStorage().add(tempTok);
            String bstTokenId = encrKey.getBSTTokenId();
            if (bstTokenId != null && bstTokenId.length() > 0) {
                RampartUtil.appendChildToSecHeader(rmd, encrKey.getBinarySecurityTokenElement());
            }
            return id;
        } catch (TrustException e) {
            throw new RampartException("errorInAddingTokenIntoStore");
        }
    }

    private String getSHA1(byte[] input) throws RampartException {
        MessageDigest sha = null;
        try {
            sha = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e1) {
            throw new RampartException("noSHA1availabe", e1);
        }
        sha.reset();
        sha.update(input);
        byte[] data = sha.digest();
        return Base64.encode(data);
    }

    private String getEncryptedKey(RampartMessageData rmd) throws RampartException {
        Vector results = (Vector) rmd.getMsgContext().getProperty(WSHandlerConstants.RECV_RESULTS);
        for (int i = 0; i < results.size(); i++) {
            WSHandlerResult rResult = (WSHandlerResult) results.get(i);
            Vector wsSecEngineResults = rResult.getResults();
            for (int j = 0; j < wsSecEngineResults.size(); j++) {
                WSSecurityEngineResult wser = (WSSecurityEngineResult) wsSecEngineResults.get(j);
                Integer actInt = (Integer) wser.get(WSSecurityEngineResult.TAG_ACTION);
                if (actInt.intValue() == WSConstants.ENCR) {
                    if (wser.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_ID) != null && ((String) wser.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_ID)).length() != 0) {
                        try {
                            String encryptedKeyID = (String) wser.get(WSSecurityEngineResult.TAG_ENCRYPTED_KEY_ID);
                            Date created = new Date();
                            Date expires = new Date();
                            expires.setTime(System.currentTimeMillis() + 300000);
                            EncryptedKeyToken tempTok = new EncryptedKeyToken(encryptedKeyID, created, expires);
                            tempTok.setSecret((byte[]) wser.get(WSSecurityEngineResult.TAG_DECRYPTED_KEY));
                            tempTok.setSHA1(getSHA1((byte[]) wser.get(WSSecurityEngineResult.TAG_ENCRYPTED_EPHEMERAL_KEY)));
                            rmd.getTokenStorage().add(tempTok);
                            return encryptedKeyID;
                        } catch (TrustException e) {
                            throw new RampartException("errorInAddingTokenIntoStore");
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Setup the required tokens
     * @param rmd
     * @param rpd
     * @throws RampartException
     */
    private void initializeTokens(RampartMessageData rmd) throws RampartException {
        RampartPolicyData rpd = rmd.getPolicyData();
        MessageContext msgContext = rmd.getMsgContext();
        if (rpd.isSymmetricBinding() && !msgContext.isServerSide()) {
            log.debug("Processing symmetric binding: " + "Setting up encryption token and signature token");
            Token sigTok = rpd.getSignatureToken();
            Token encrTok = rpd.getEncryptionToken();
            if (sigTok instanceof IssuedToken) {
                log.debug("SignatureToken is an IssuedToken");
                if (rmd.getIssuedSignatureTokenId() == null) {
                    log.debug("No Issuedtoken found, requesting a new token");
                    IssuedToken issuedToken = (IssuedToken) sigTok;
                    String id = RampartUtil.getIssuedToken(rmd, issuedToken);
                    rmd.setIssuedSignatureTokenId(id);
                }
            } else if (sigTok instanceof SecureConversationToken) {
                log.debug("SignatureToken is a SecureConversationToken");
                String secConvTokenId = rmd.getSecConvTokenId();
                String action = msgContext.getOptions().getAction();
                boolean cancelReqResp = action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RSTR_ACTION_CANCEL_SCT) || action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RSTR_ACTION_CANCEL_SCT) || action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_CANCEL_SCT) || action.equals(RahasConstants.WST_NS_05_02 + RahasConstants.RST_ACTION_CANCEL_SCT);
                if (secConvTokenId != null && cancelReqResp) {
                    try {
                        rmd.getTokenStorage().getToken(secConvTokenId).setState(org.apache.rahas.Token.CANCELLED);
                        msgContext.setProperty(RampartMessageData.SCT_ID, secConvTokenId);
                        String contextIdentifierKey = RampartUtil.getContextIdentifierKey(msgContext);
                        RampartUtil.getContextMap(msgContext).remove(contextIdentifierKey);
                    } catch (TrustException e) {
                        throw new RampartException("errorExtractingToken");
                    }
                }
                if (secConvTokenId == null || (secConvTokenId != null && (!RampartUtil.isTokenValid(rmd, secConvTokenId) && !cancelReqResp))) {
                    log.debug("No SecureConversationToken found, " + "requesting a new token");
                    SecureConversationToken secConvTok = (SecureConversationToken) sigTok;
                    try {
                        String id = RampartUtil.getSecConvToken(rmd, secConvTok);
                        rmd.setSecConvTokenId(id);
                    } catch (TrustException e) {
                        throw new RampartException("errorInObtainingSct", e);
                    }
                }
            }
            if (sigTok.equals(encrTok) && sigTok instanceof IssuedToken) {
                log.debug("Symmetric binding uses a ProtectionToken, both" + " SignatureToken and EncryptionToken are the same");
                rmd.setIssuedEncryptionTokenId(rmd.getIssuedEncryptionTokenId());
            } else {
                log.debug("Obtaining the Encryption Token");
                if (rmd.getIssuedEncryptionTokenId() != null) {
                    log.debug("EncrytionToken not alredy set");
                    IssuedToken issuedToken = (IssuedToken) encrTok;
                    String id = RampartUtil.getIssuedToken(rmd, issuedToken);
                    rmd.setIssuedEncryptionTokenId(id);
                }
            }
        }
    }
}
