package org.atricore.idbus.capabilities.sso.main.idp.producers;

import oasis.names.tc.saml._1_0.assertion.AudienceRestrictionConditionType;
import oasis.names.tc.saml._2_0.assertion.*;
import oasis.names.tc.saml._2_0.assertion.SubjectType;
import oasis.names.tc.saml._2_0.idbus.SecTokenAuthnRequestType;
import oasis.names.tc.saml._2_0.metadata.*;
import oasis.names.tc.saml._2_0.protocol.AuthnRequestType;
import oasis.names.tc.saml._2_0.protocol.RequestedAuthnContextType;
import oasis.names.tc.saml._2_0.protocol.ResponseType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.capabilities.sso.main.SSOException;
import org.atricore.idbus.capabilities.sso.main.claims.SSOClaimsRequest;
import org.atricore.idbus.capabilities.sso.main.claims.SSOClaimsResponse;
import org.atricore.idbus.capabilities.sso.main.common.AbstractSSOMediator;
import org.atricore.idbus.capabilities.sso.main.common.producers.SSOProducer;
import org.atricore.idbus.capabilities.sso.main.emitter.SamlR2SecurityTokenEmissionContext;
import org.atricore.idbus.capabilities.sso.main.emitter.plans.SamlR2SecurityTokenToAuthnAssertionPlan;
import org.atricore.idbus.capabilities.sso.main.idp.IdPSecurityContext;
import org.atricore.idbus.capabilities.sso.main.idp.IdentityProviderConstants;
import org.atricore.idbus.capabilities.sso.main.idp.SSOIDPMediator;
import org.atricore.idbus.capabilities.sso.main.idp.plans.IDPInitiatedAuthnReqToSamlR2AuthnReqPlan;
import org.atricore.idbus.capabilities.sso.main.idp.plans.SamlR2AuthnRequestToSamlR2ResponsePlan;
import org.atricore.idbus.capabilities.sso.support.SAMLR2Constants;
import org.atricore.idbus.capabilities.sso.support.auth.AuthnCtxClass;
import org.atricore.idbus.capabilities.sso.support.binding.SSOBinding;
import org.atricore.idbus.capabilities.sso.support.core.*;
import org.atricore.idbus.capabilities.sso.support.core.encryption.SamlR2Encrypter;
import org.atricore.idbus.capabilities.sso.support.core.signature.SamlR2SignatureException;
import org.atricore.idbus.capabilities.sso.support.core.signature.SamlR2SignatureValidationException;
import org.atricore.idbus.capabilities.sso.support.core.signature.SamlR2Signer;
import org.atricore.idbus.capabilities.sso.support.metadata.SSOService;
import org.atricore.idbus.capabilities.sts.main.SecurityTokenAuthenticationFailure;
import org.atricore.idbus.capabilities.sts.main.SecurityTokenEmissionException;
import org.atricore.idbus.capabilities.sts.main.WSTConstants;
import org.atricore.idbus.common.sso._1_0.protocol.*;
import org.atricore.idbus.kernel.main.authn.*;
import org.atricore.idbus.kernel.main.federation.metadata.*;
import org.atricore.idbus.kernel.main.mediation.*;
import org.atricore.idbus.kernel.main.mediation.binding.BindingChannel;
import org.atricore.idbus.kernel.main.mediation.camel.AbstractCamelEndpoint;
import org.atricore.idbus.kernel.main.mediation.camel.component.binding.CamelMediationExchange;
import org.atricore.idbus.kernel.main.mediation.camel.component.binding.CamelMediationMessage;
import org.atricore.idbus.kernel.main.mediation.channel.FederationChannel;
import org.atricore.idbus.kernel.main.mediation.channel.SPChannel;
import org.atricore.idbus.kernel.main.mediation.claim.*;
import org.atricore.idbus.kernel.main.mediation.endpoint.IdentityMediationEndpoint;
import org.atricore.idbus.kernel.main.mediation.policy.PolicyEnforcementRequest;
import org.atricore.idbus.kernel.main.mediation.policy.PolicyEnforcementRequestImpl;
import org.atricore.idbus.kernel.main.mediation.policy.PolicyEnforcementResponse;
import org.atricore.idbus.kernel.main.mediation.provider.FederatedLocalProvider;
import org.atricore.idbus.kernel.main.mediation.provider.FederatedProvider;
import org.atricore.idbus.kernel.main.session.SSOSessionManager;
import org.atricore.idbus.kernel.main.session.exceptions.NoSuchSessionException;
import org.atricore.idbus.kernel.main.store.SSOIdentityManager;
import org.atricore.idbus.kernel.main.util.UUIDGenerator;
import org.atricore.idbus.kernel.planning.*;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.AttributedString;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.BinarySecurityTokenType;
import org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.UsernameTokenType;
import org.xmlsoap.schemas.ws._2005._02.trust.RequestSecurityTokenResponseType;
import org.xmlsoap.schemas.ws._2005._02.trust.RequestSecurityTokenType;
import org.xmlsoap.schemas.ws._2005._02.trust.RequestedSecurityTokenType;
import org.xmlsoap.schemas.ws._2005._02.trust.wsdl.SecurityTokenService;
import javax.security.auth.Subject;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import java.math.BigInteger;
import java.security.Principal;
import java.util.*;

/**
 * @author <a href="mailto:sgonzalez@atricore.org">Sebastian Gonzalez Oyuela</a>
 * @version $Id: SingleSignOnProducer.java 1359 2009-07-19 16:57:57Z sgonzalez $
 */
public class SingleSignOnProducer extends SSOProducer {

    private static final Log logger = LogFactory.getLog(SingleSignOnProducer.class);

    private UUIDGenerator uuidGenerator = new UUIDGenerator();

    public SingleSignOnProducer(AbstractCamelEndpoint<CamelMediationExchange> endpoint) throws Exception {
        super(endpoint);
    }

    @Override
    protected void doProcess(CamelMediationExchange exchange) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        Object content = in.getMessage().getContent();
        AbstractSSOMediator mediator = (AbstractSSOMediator) channel.getIdentityMediator();
        in.getMessage().getState().setAttribute("SAMLR2Signer", mediator.getSigner());
        try {
            if (content instanceof IDPInitiatedAuthnRequestType) {
                doProcessIDPInitiantedSSO(exchange, (IDPInitiatedAuthnRequestType) content);
            } else if (content instanceof SecTokenAuthnRequestType) {
                doProcessAssertIdentityWithBasicAuth(exchange, (SecTokenAuthnRequestType) content);
            } else if (content instanceof AuthnRequestType) {
                doProcessAuthnRequest(exchange, (AuthnRequestType) content, in.getMessage().getRelayState());
            } else if (content instanceof SSOClaimsResponse) {
                doProcessClaimsResponse(exchange, (SSOClaimsResponse) content);
            } else if (content instanceof PolicyEnforcementResponse) {
                doProcessPolicyEnforcementResponse(exchange, (PolicyEnforcementResponse) content);
            } else if (content instanceof SPAuthnResponseType) {
                doProcessProxyResponse(exchange, (SPAuthnResponseType) content);
            } else {
                throw new IdentityMediationFault(StatusCode.TOP_RESPONDER.getValue(), null, StatusDetails.UNKNOWN_REQUEST.getValue(), content.getClass().getName(), null);
            }
        } catch (SSORequestException e) {
            throw new IdentityMediationFault(e.getTopLevelStatusCode() != null ? e.getTopLevelStatusCode().getValue() : StatusCode.TOP_RESPONDER.getValue(), e.getSecondLevelStatusCode() != null ? e.getSecondLevelStatusCode().getValue() : null, e.getStatusDtails() != null ? e.getStatusDtails().getValue() : StatusDetails.UNKNOWN_REQUEST.getValue(), e.getErrorDetails() != null ? e.getErrorDetails() : content.getClass().getName(), e);
        } catch (SSOException e) {
            throw new IdentityMediationFault(StatusCode.TOP_RESPONDER.getValue(), null, StatusDetails.UNKNOWN_REQUEST.getValue(), content.getClass().getName(), e);
        }
    }

    /**
     * This procedure will handle an IdP-initiated (aka IdP unsolicited response) request.
     */
    protected void doProcessIDPInitiantedSSO(CamelMediationExchange exchange, IDPInitiatedAuthnRequestType idpInitiatedAuthnRequest) throws SSOException {
        logger.debug("Processing IDP Initiated Single Sign-On with " + idpInitiatedAuthnRequest.getPreferredResponseFormat() + " preferred Response Format");
        try {
            CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
            String relayState = in.getMessage().getRelayState();
            in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:sso:protocol:responseMode", "unsolicited");
            in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:sso:protocol:responseFormat", idpInitiatedAuthnRequest.getPreferredResponseFormat());
            CircleOfTrustMemberDescriptor idp = this.resolveIdp(exchange);
            logger.debug("Using IdP " + idp.getAlias());
            EndpointType idpSsoEndpoint = resolveIdpSsoEndpoint(idp);
            EndpointDescriptor ed = new EndpointDescriptorImpl("IDPSSOEndpoint", "SingleSignOnService", idpSsoEndpoint.getBinding(), idpSsoEndpoint.getLocation(), idpSsoEndpoint.getResponseLocation());
            AuthnRequestType authnRequest = buildIdPInitiatedAuthnRequest(exchange, idp, ed, (FederationChannel) channel);
            in.getMessage().getState().setLocalVariable(SAMLR2Constants.SAML_PROTOCOL_NS + ":AuthnRequest", authnRequest);
            CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
            out.setMessage(new MediationMessageImpl(uuidGenerator.generateId(), authnRequest, "AuthnRequest", relayState, ed, in.getMessage().getState()));
            exchange.setOut(out);
        } catch (Exception e) {
            throw new SSOException(e);
        }
    }

    /**
     * This procedure will process an authn request.
     * <p/>
     * <p/>
     * If we already stablished identity for the 'presenter' (user) of the request, we'll generate
     * an assertion using the authn statement stored in session as security token.
     * The assertion will be sent to the SP in a new Response.
     * <p/>
     * <p/>
     * If we don't have user identity yet, we have to decide if we're handling the request or we are proxying it to a
     * different IDP.
     * If we handle the request, we'll search for a claims endpoint and start collecting claims.  If no claims endpoint
     * are available, we're sending a status error response. (we could look for a different IDP here!)
     */
    protected void doProcessAuthnRequest(CamelMediationExchange exchange, AuthnRequestType authnRequest, String relayState) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
        MediationState mediationState = in.getMessage().getState();
        String varName = getProvider().getName().toUpperCase() + "_SECURITY_CTX";
        IdPSecurityContext secCtx = (IdPSecurityContext) mediationState.getLocalVariable(varName);
        String responseMode = (String) mediationState.getLocalVariable("urn:org:atricore:idbus:sso:protocol:responseMode");
        String responseFormat = (String) mediationState.getLocalVariable("urn:org:atricore:idbus:sso:protocol:responseFormat");
        if (responseMode != null && responseMode.equalsIgnoreCase("unsolicited")) {
            logger.debug("Response Mode for Authentication Request " + authnRequest.getID() + " is unsolicited");
            logger.debug("Response Format for Authentication Request " + authnRequest.getID() + " is " + responseFormat);
        } else {
            logger.debug("Response Mode for Authentication Request " + authnRequest.getID() + " is NOT unsolicited");
        }
        SSOSessionManager sessionMgr = ((SPChannel) channel).getSessionManager();
        validateRequest(authnRequest, in.getMessage().getRawContent(), in.getMessage().getState());
        boolean isSsoSessionValid = false;
        if (secCtx != null && secCtx.getSessionIndex() != null) {
            try {
                sessionMgr.accessSession(secCtx.getSessionIndex());
                isSsoSessionValid = true;
                if (logger.isDebugEnabled()) logger.debug("SSO Session is valid : " + secCtx.getSessionIndex());
            } catch (NoSuchSessionException e) {
                if (logger.isDebugEnabled()) logger.debug("SSO Session is not valid : " + secCtx.getSessionIndex() + " " + e.getMessage(), e);
            }
        }
        AuthenticationState authnState = null;
        if (!isSsoSessionValid) {
            if (logger.isTraceEnabled()) logger.trace("Creating new AuthnState");
            authnState = newAuthnState(exchange);
        } else {
            if (logger.isTraceEnabled()) logger.trace("Using existing AuthnState, if any");
            authnState = getAuthnState(exchange);
        }
        authnState.setAuthnRequest(authnRequest);
        authnState.setReceivedRelayState(relayState);
        authnState.setResponseMode(responseMode);
        authnState.setResponseFormat(responseFormat);
        if (authnRequest.isForceAuthn() != null && authnRequest.isForceAuthn()) {
            if (logger.isDebugEnabled()) logger.debug("Forcing authentication for request " + authnRequest.getID());
            isSsoSessionValid = false;
        }
        if (!isSsoSessionValid) {
            SPChannel spChannel = (SPChannel) channel;
            if (spChannel.isProxyModeEnabled()) {
                BindingChannel proxyChannel = spChannel.getProxy();
                EndpointDescriptor proxyEndpoint = resolveSPInitiatedSSOProxyEndpointDescriptor(exchange, proxyChannel);
                logger.debug("Proxying SP-Initiated SSO Request to " + proxyChannel.getLocation() + proxyEndpoint.getLocation());
                SPInitiatedAuthnRequestType authnProxyRequest = buildAuthnProxyRequest(authnRequest);
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:sso:protocol:SPInitiatedAuthnRequest", authnProxyRequest);
                out.setMessage(new MediationMessageImpl(uuidGenerator.generateId(), authnProxyRequest, "AuthnProxyRequest", relayState, proxyEndpoint, in.getMessage().getState()));
                exchange.setOut(out);
            } else {
                logger.debug("No SSO Session found, asking for credentials");
                IdentityMediationEndpoint claimsEndpoint = selectNextClaimsEndpoint(authnState);
                if (claimsEndpoint == null) {
                    if (logger.isDebugEnabled()) logger.debug("No claims endpoint found for authn request : " + authnRequest.getID());
                    CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(authnRequest.getIssuer());
                    EndpointDescriptor ed = resolveSpAcsEndpoint(exchange, authnRequest);
                    ResponseType response = buildSamlResponse(exchange, authnState, null, sp, ed);
                    out.setMessage(new MediationMessageImpl(response.getID(), response, "Response", relayState, ed, in.getMessage().getState()));
                    exchange.setOut(out);
                    return;
                }
                logger.debug("Selected claims endpoint : " + claimsEndpoint);
                SSOClaimsRequest claimsRequest = new SSOClaimsRequest(authnRequest.getID(), channel, endpoint, ((SPChannel) channel).getClaimsProvider(), uuidGenerator.generateId());
                claimsRequest.setRelayState(mediationState.getLocalState().getId());
                claimsRequest.setTargetRelayState(in.getMessage().getRelayState());
                claimsRequest.setSpAlias(authnRequest.getIssuer().getValue());
                claimsRequest.setRequestedAuthnCtxClass(authnRequest.getRequestedAuthnContext());
                IdentityMediationEndpoint claimEndpoint = authnState.getCurrentClaimsEndpoint();
                ClaimChannel claimChannel = claimsRequest.getClaimsChannel();
                EndpointDescriptor ed = new EndpointDescriptorImpl(claimEndpoint.getBinding(), claimEndpoint.getType(), claimEndpoint.getBinding(), claimEndpoint.getLocation().startsWith("/") ? claimChannel.getLocation() + claimEndpoint.getLocation() : claimEndpoint.getLocation(), claimEndpoint.getResponseLocation());
                logger.debug("Collecting claims using endpoint " + claimEndpoint);
                SSOBinding edBinding = SSOBinding.asEnum(ed.getBinding());
                if (!edBinding.isFrontChannel()) {
                    SSOClaimsResponse cr = (SSOClaimsResponse) channel.getIdentityMediator().sendMessage(claimsRequest, ed, claimChannel);
                    doProcessClaimsResponse(exchange, cr);
                } else {
                    out.setMessage(new MediationMessageImpl(claimsRequest.getId(), claimsRequest, "ClaimsRequest", null, ed, in.getMessage().getState()));
                    exchange.setOut(out);
                }
            }
        } else {
            if (logger.isDebugEnabled()) logger.debug("Found valid SSO Session for AuthnRequest " + authnRequest.getID());
            EndpointDescriptor ed = resolveSpAcsEndpoint(exchange, authnRequest);
            SamlR2SecurityTokenEmissionContext securityTokenEmissionCtx = new SamlR2SecurityTokenEmissionContext();
            securityTokenEmissionCtx.setMember(resolveProviderDescriptor(authnRequest.getIssuer()));
            authnState.setAuthnRequest(authnRequest);
            securityTokenEmissionCtx.setAuthnState(authnState);
            securityTokenEmissionCtx.setSessionIndex(secCtx.getSessionIndex());
            securityTokenEmissionCtx.setSsoSession(sessionMgr.getSession(secCtx.getSessionIndex()));
            securityTokenEmissionCtx.setIssuerMetadata(((SPChannel) channel).getMember().getMetadata());
            securityTokenEmissionCtx.setIdentityPlanName(getSTSPlanName());
            securityTokenEmissionCtx.setSpAcs(ed);
            securityTokenEmissionCtx = emitAssertionFromPreviousSession(exchange, securityTokenEmissionCtx, authnRequest);
            if (logger.isDebugEnabled()) logger.debug("Created SAMLR2 Assertion " + securityTokenEmissionCtx.getAssertion().getID() + " for AuthnRequest " + authnRequest.getID());
            secCtx.register(authnRequest.getIssuer(), authnState.getReceivedRelayState());
            CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(authnRequest.getIssuer());
            ResponseType response = buildSamlResponse(exchange, authnState, securityTokenEmissionCtx.getAssertion(), sp, ed);
            if (responseFormat != null && responseFormat.equals("urn:oasis:names:tc:SAML:1.1")) {
                oasis.names.tc.saml._1_0.protocol.ResponseType saml11Response;
                saml11Response = transformSamlR2ResponseToSaml11(response);
                out.setMessage(new MediationMessageImpl(saml11Response.getResponseID(), saml11Response, "Response", relayState, ed, in.getMessage().getState()));
            } else {
                out.setMessage(new MediationMessageImpl(response.getID(), response, "Response", relayState, ed, in.getMessage().getState()));
            }
            exchange.setOut(out);
        }
    }

    public void doProcessAssertIdentityWithBasicAuth(CamelMediationExchange exchange, SecTokenAuthnRequestType authnRequest) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        AuthenticationState authnState = this.getAuthnState(exchange);
        NameIDType issuer = authnRequest.getIssuer();
        CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(issuer);
        EndpointDescriptor ed = this.resolveSpAcsEndpoint(exchange, authnRequest);
        SamlR2SecurityTokenEmissionContext securityTokenEmissionCtx = new SamlR2SecurityTokenEmissionContext();
        securityTokenEmissionCtx.setMember(sp);
        securityTokenEmissionCtx.setRoleMetadata(null);
        securityTokenEmissionCtx.setAuthnState(authnState);
        securityTokenEmissionCtx.setSessionIndex(uuidGenerator.generateId());
        securityTokenEmissionCtx.setIssuerMetadata(sp.getMetadata());
        securityTokenEmissionCtx.setIdentityPlanName(getSTSPlanName());
        securityTokenEmissionCtx.setSpAcs(ed);
        UsernameTokenType usernameToken = new UsernameTokenType();
        AttributedString usernameString = new AttributedString();
        usernameString.setValue(authnRequest.getUsername());
        usernameToken.setUsername(usernameString);
        usernameToken.getOtherAttributes().put(new QName(Constants.PASSWORD_NS), authnRequest.getPassword());
        usernameToken.getOtherAttributes().put(new QName(AuthnCtxClass.PASSWORD_AUTHN_CTX.getValue()), "TRUE");
        Claim claim = new ClaimImpl(AuthnCtxClass.PASSWORD_AUTHN_CTX.getValue(), usernameToken);
        ClaimSet claims = new ClaimSetImpl();
        claims.addClaim(claim);
        SamlR2SecurityTokenEmissionContext cxt = emitAssertionFromClaims(exchange, securityTokenEmissionCtx, claims, sp);
        AssertionType assertion = cxt.getAssertion();
        Subject authnSubject = cxt.getSubject();
        logger.debug("New Assertion " + assertion.getID() + " emmitted form request " + (authnRequest != null ? authnRequest.getID() : "<NULL>"));
        IdPSecurityContext secCtx = createSecurityContext(exchange, authnSubject, assertion);
        secCtx.register(authnRequest.getIssuer(), authnState.getReceivedRelayState());
        ResponseType response = buildSamlResponse(exchange, authnState, assertion, sp, ed);
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
        out.setMessage(new MediationMessageImpl(response.getID(), response, "Response", authnState.getReceivedRelayState(), ed, null));
        exchange.setOut(out);
    }

    /**
     * This will emit an assertion using the recieved claims.  If the process is successful, a SAML Response will
     * be issued to the original SP.
     * If an error occures, the procedure will decide to retry collecting claims with the las
     * claims endpoint selected or collect claims using a new claims endpoint.
     * <p/>
     * If no more claim endpoints are available, this will send an satus error response to the SP.
     *
     * @param exchange
     * @param claimsResponse
     * @throws Exception
     */
    protected void doProcessClaimsResponse(CamelMediationExchange exchange, SSOClaimsResponse claimsResponse) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
        AuthenticationState authnState = getAuthnState(exchange);
        AuthnRequestType authnRequest = authnState.getAuthnRequest();
        NameIDType issuer = authnRequest.getIssuer();
        CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(issuer);
        String responseMode = authnState.getResponseMode();
        String responseFormat = authnState.getResponseFormat();
        if (responseMode != null && responseMode.equalsIgnoreCase("unsolicited")) {
            logger.debug("Response Mode for Claim Response " + authnRequest.getID() + " is unsolicited");
            logger.debug("Response Format for Claim Response " + authnRequest.getID() + " is " + responseFormat);
        } else {
            logger.debug("Response Mode for Claim Response " + authnRequest.getID() + " is NOT unsolicited");
        }
        try {
            EndpointDescriptor ed = this.resolveSpAcsEndpoint(exchange, authnRequest);
            SamlR2SecurityTokenEmissionContext securityTokenEmissionCtx = new SamlR2SecurityTokenEmissionContext();
            securityTokenEmissionCtx.setIssuerMetadata(((SPChannel) channel).getMember().getMetadata());
            securityTokenEmissionCtx.setMember(sp);
            securityTokenEmissionCtx.setIdentityPlanName(getSTSPlanName());
            securityTokenEmissionCtx.setRoleMetadata(null);
            securityTokenEmissionCtx.setAuthnState(authnState);
            securityTokenEmissionCtx.setSessionIndex(uuidGenerator.generateId());
            securityTokenEmissionCtx.setSpAcs(ed);
            SamlR2SecurityTokenEmissionContext cxt = emitAssertionFromClaims(exchange, securityTokenEmissionCtx, claimsResponse.getClaimSet(), sp);
            AssertionType assertion = cxt.getAssertion();
            Subject authnSubject = cxt.getSubject();
            logger.debug("New Assertion " + assertion.getID() + " emitted form request " + (authnRequest != null ? authnRequest.getID() : "<NULL>"));
            IdPSecurityContext secCtx = createSecurityContext(exchange, authnSubject, assertion);
            secCtx.register(authnRequest.getIssuer(), authnState.getReceivedRelayState());
            ResponseType saml2Response = buildSamlResponse(exchange, authnState, assertion, sp, ed);
            oasis.names.tc.saml._1_0.protocol.ResponseType saml11Response = null;
            in.getMessage().getState().setLocalVariable(getProvider().getName().toUpperCase() + "_SECURITY_CTX", secCtx);
            in.getMessage().getState().getLocalState().addAlternativeId(IdentityProviderConstants.SEC_CTX_SSOSESSION_KEY, secCtx.getSessionIndex());
            if (responseFormat != null && responseFormat.equals("urn:oasis:names:tc:SAML:1.1")) {
                saml11Response = transformSamlR2ResponseToSaml11(saml2Response);
                SamlR2Signer signer = ((SSOIDPMediator) channel.getIdentityMediator()).getSigner();
                saml11Response = signer.sign(saml11Response);
            }
            clearAuthnState(exchange);
            List<SSOPolicyEnforcementStatement> stmts = getPolicyEnforcementStatements(assertion);
            if (stmts != null && stmts.size() > 0) {
                if (logger.isDebugEnabled()) logger.debug("Processing " + stmts.size() + " SSO Policy Enforcement Statements");
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponse", saml11Response != null ? saml11Response : saml2Response);
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseEndpoint", ed);
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseRelayState", authnState.getReceivedRelayState());
                EndpointDescriptor pweEd = new EndpointDescriptorImpl("PolicyEnforcementWarningService", "PolicyEnforcementWarningService", SSOBinding.SSO_ARTIFACT.getValue(), channel.getIdentityMediator().getWarningUrl(), null);
                EndpointDescriptor replyTo = resolveIdpSsoContinueEndpoint();
                PolicyEnforcementRequest per = new PolicyEnforcementRequestImpl(uuidGenerator.generateId(), replyTo);
                per.getStatements().addAll(stmts);
                out.setMessage(new MediationMessageImpl(per.getId(), per, "PolicyEnforcementWarning", null, pweEd, in.getMessage().getState()));
                return;
            }
            if (responseFormat != null && responseFormat.equals("urn:oasis:names:tc:SAML:1.1")) {
                out.setMessage(new MediationMessageImpl(saml11Response.getResponseID(), saml11Response, "Response", authnState.getReceivedRelayState(), ed, in.getMessage().getState()));
            } else {
                out.setMessage(new MediationMessageImpl(saml2Response.getID(), saml2Response, "Response", authnState.getReceivedRelayState(), ed, in.getMessage().getState()));
            }
            exchange.setOut(out);
        } catch (SecurityTokenAuthenticationFailure e) {
            Set<SSOPolicyEnforcementStatement> ssoPolicyEnforcements = e.getSsoPolicyEnforcements();
            if (logger.isDebugEnabled()) logger.debug("Security Token authentication failure : " + e.getMessage(), e);
            IdentityMediationEndpoint claimsEndpoint = selectNextClaimsEndpoint(authnState);
            if (claimsEndpoint == null) {
                logger.error("No claims endpoint found for authn request : " + authnRequest.getID());
                EndpointDescriptor ed = resolveSpAcsEndpoint(exchange, authnRequest);
                ResponseType response = buildSamlResponse(exchange, authnState, null, sp, ed);
                out.setMessage(new MediationMessageImpl(response.getID(), response, "Response", authnState.getReceivedRelayState(), ed, in.getMessage().getState()));
                exchange.setOut(out);
                return;
            }
            logger.debug("Selecting claims endpoint : " + endpoint.getName());
            SSOClaimsRequest claimsRequest = new SSOClaimsRequest(authnRequest.getID(), channel, endpoint, ((SPChannel) channel).getClaimsProvider(), uuidGenerator.generateId());
            claimsRequest.setLastErrorId("AUTHN_FAILED");
            claimsRequest.setLastErrorMsg(e.getMessage());
            claimsRequest.getSsoPolicyEnforcements().addAll(ssoPolicyEnforcements);
            claimsRequest.setRequestedAuthnCtxClass(authnRequest.getRequestedAuthnContext());
            authnState.setAuthnRequest(authnRequest);
            IdentityMediationEndpoint claimEndpoint = authnState.getCurrentClaimsEndpoint();
            ClaimChannel claimChannel = claimsRequest.getClaimsChannel();
            EndpointDescriptor ed = new EndpointDescriptorImpl(claimEndpoint.getBinding(), claimEndpoint.getType(), claimEndpoint.getBinding(), claimChannel.getLocation() + claimEndpoint.getLocation(), claimEndpoint.getResponseLocation());
            logger.debug("Collecting claims using endpoint " + claimEndpoint.getName() + " [" + ed.getLocation() + "]");
            out.setMessage(new MediationMessageImpl(claimsRequest.getId(), claimsRequest, "ClaimsRequest", null, ed, in.getMessage().getState()));
            exchange.setOut(out);
        }
    }

    /**
     * This will emit an assertion using the claims conveyed in the proxy response.  If the process is successful,
     * a SAML Response will be issued to the original SP.
     * If an error occurs, the error condition will be notified back to the requesting SP.
     *
     * @param exchange
     * @param proxyResponse
     * @throws Exception
     */
    protected void doProcessProxyResponse(CamelMediationExchange exchange, SPAuthnResponseType proxyResponse) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
        AuthenticationState authnState = getAuthnState(exchange);
        AuthnRequestType authnRequest = authnState.getAuthnRequest();
        NameIDType issuer = authnRequest.getIssuer();
        CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(issuer);
        String responseMode = authnState.getResponseMode();
        String responseFormat = authnState.getResponseFormat();
        if (responseMode != null && responseMode.equalsIgnoreCase("unsolicited")) {
            logger.debug("Response Mode for Proxy Response " + authnRequest.getID() + " is unsolicited");
            logger.debug("Response Format for Proxy Response " + authnRequest.getID() + " is " + responseFormat);
        } else {
            logger.debug("Response Mode for Proxy Response " + authnRequest.getID() + " is NOT unsolicited");
        }
        try {
            EndpointDescriptor ed = this.resolveSpAcsEndpoint(exchange, authnRequest);
            SamlR2SecurityTokenEmissionContext securityTokenEmissionCtx = new SamlR2SecurityTokenEmissionContext();
            securityTokenEmissionCtx.setIssuerMetadata(((SPChannel) channel).getMember().getMetadata());
            securityTokenEmissionCtx.setMember(sp);
            securityTokenEmissionCtx.setIdentityPlanName(getSTSPlanName());
            securityTokenEmissionCtx.setRoleMetadata(null);
            securityTokenEmissionCtx.setAuthnState(authnState);
            securityTokenEmissionCtx.setSessionIndex(uuidGenerator.generateId());
            securityTokenEmissionCtx.setSpAcs(ed);
            List<AbstractPrincipalType> proxyPrincipals = proxyResponse.getSubject().getAbstractPrincipal();
            ClaimSet claims = new ClaimSetImpl();
            UsernameTokenType usernameToken = new UsernameTokenType();
            for (Iterator<AbstractPrincipalType> iterator = proxyPrincipals.iterator(); iterator.hasNext(); ) {
                AbstractPrincipalType next = iterator.next();
                if (next instanceof SubjectNameIDType) {
                    SubjectNameIDType nameId = (SubjectNameIDType) next;
                    AttributedString usernameString = new AttributedString();
                    usernameString.setValue(nameId.getName());
                    usernameToken.setUsername(usernameString);
                    usernameToken.getOtherAttributes().put(new QName(Constants.PASSWORD_NS), nameId.getName());
                    usernameToken.getOtherAttributes().put(new QName(AuthnCtxClass.PASSWORD_AUTHN_CTX.getValue()), "TRUE");
                    Claim claim = new ClaimImpl(AuthnCtxClass.PASSWORD_AUTHN_CTX.getValue(), usernameToken);
                    claims.addClaim(claim);
                }
            }
            SamlR2SecurityTokenEmissionContext cxt = emitAssertionFromClaims(exchange, securityTokenEmissionCtx, claims, sp);
            AssertionType assertion = cxt.getAssertion();
            Subject authnSubject = cxt.getSubject();
            logger.debug("New Assertion " + assertion.getID() + " emitted form request " + (authnRequest != null ? authnRequest.getID() : "<NULL>"));
            IdPSecurityContext secCtx = createSecurityContext(exchange, authnSubject, assertion);
            secCtx.register(authnRequest.getIssuer(), authnState.getReceivedRelayState());
            ResponseType saml2Response = buildSamlResponse(exchange, authnState, assertion, sp, ed);
            oasis.names.tc.saml._1_0.protocol.ResponseType saml11Response = null;
            in.getMessage().getState().setLocalVariable(getProvider().getName().toUpperCase() + "_SECURITY_CTX", secCtx);
            in.getMessage().getState().getLocalState().addAlternativeId(IdentityProviderConstants.SEC_CTX_SSOSESSION_KEY, secCtx.getSessionIndex());
            if (responseFormat != null && responseFormat.equals("urn:oasis:names:tc:SAML:1.1")) {
                saml11Response = transformSamlR2ResponseToSaml11(saml2Response);
                SamlR2Signer signer = ((SSOIDPMediator) channel.getIdentityMediator()).getSigner();
                saml11Response = signer.sign(saml11Response);
            }
            clearAuthnState(exchange);
            List<SSOPolicyEnforcementStatement> stmts = getPolicyEnforcementStatements(assertion);
            if (stmts != null && stmts.size() > 0) {
                if (logger.isDebugEnabled()) logger.debug("Processing " + stmts.size() + " SSO Policy Enforcement Statements");
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponse", saml11Response != null ? saml11Response : saml2Response);
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseEndpoint", ed);
                in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseRelayState", authnState.getReceivedRelayState());
                EndpointDescriptor pweEd = new EndpointDescriptorImpl("PolicyEnforcementWarningService", "PolicyEnforcementWarningService", SSOBinding.SSO_ARTIFACT.getValue(), channel.getIdentityMediator().getWarningUrl(), null);
                EndpointDescriptor replyTo = resolveIdpSsoContinueEndpoint();
                PolicyEnforcementRequest per = new PolicyEnforcementRequestImpl(uuidGenerator.generateId(), replyTo);
                per.getStatements().addAll(stmts);
                out.setMessage(new MediationMessageImpl(per.getId(), per, "PolicyEnforcementWarning", null, pweEd, in.getMessage().getState()));
                return;
            }
            if (responseFormat != null && responseFormat.equals("urn:oasis:names:tc:SAML:1.1")) {
                out.setMessage(new MediationMessageImpl(saml11Response.getResponseID(), saml11Response, "Response", authnState.getReceivedRelayState(), ed, in.getMessage().getState()));
            } else {
                out.setMessage(new MediationMessageImpl(saml2Response.getID(), saml2Response, "Response", authnState.getReceivedRelayState(), ed, in.getMessage().getState()));
            }
            exchange.setOut(out);
        } catch (SecurityTokenAuthenticationFailure e) {
            if (logger.isDebugEnabled()) logger.debug("Security Token authentication failure : " + e.getMessage(), e);
        }
    }

    protected void doProcessPolicyEnforcementResponse(CamelMediationExchange exchange, PolicyEnforcementResponse response) throws Exception {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        CamelMediationMessage out = (CamelMediationMessage) exchange.getOut();
        Object saml11OrSaml2AuthnResponse = in.getMessage().getState().getLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponse");
        EndpointDescriptor acs = (EndpointDescriptor) in.getMessage().getState().getLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseEndpoint");
        String relayState = (String) in.getMessage().getState().getLocalVariable("urn:org:atricore:idbus:samlr2:idp:pendingAuthnResponseRelayState");
        if (saml11OrSaml2AuthnResponse instanceof oasis.names.tc.saml._1_0.protocol.ResponseType) {
            oasis.names.tc.saml._1_0.protocol.ResponseType saml11Response = (oasis.names.tc.saml._1_0.protocol.ResponseType) saml11OrSaml2AuthnResponse;
            out.setMessage(new MediationMessageImpl(saml11Response.getResponseID(), saml11Response, "Response", relayState, acs, in.getMessage().getState()));
        } else {
            ResponseType saml2Response = (ResponseType) saml11OrSaml2AuthnResponse;
            out.setMessage(new MediationMessageImpl(saml2Response.getID(), saml2Response, "Response", relayState, acs, in.getMessage().getState()));
        }
        exchange.setOut(out);
    }

    protected void validateRequest(AuthnRequestType request, String originalRequest, MediationState state) throws SSORequestException, SSOException {
        AbstractSSOMediator mediator = (AbstractSSOMediator) channel.getIdentityMediator();
        SamlR2Signer signer = mediator.getSigner();
        SamlR2Encrypter encrypter = mediator.getEncrypter();
        SPSSODescriptorType saml2SpMd = null;
        IDPSSODescriptorType saml2IdpMd = null;
        try {
            String spAlias = request.getIssuer().getValue();
            MetadataEntry spMd = getCotManager().findEntityRoleMetadata(spAlias, "urn:oasis:names:tc:SAML:2.0:metadata:SPSSODescriptor");
            saml2SpMd = (SPSSODescriptorType) spMd.getEntry();
            MetadataEntry idpMd = getCotManager().findEntityRoleMetadata(getCotMemberDescriptor().getAlias(), "urn:oasis:names:tc:SAML:2.0:metadata:IDPSSODescriptor");
            saml2IdpMd = (IDPSSODescriptorType) idpMd.getEntry();
        } catch (CircleOfTrustManagerException e) {
            throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, null, request.getIssuer().getValue(), e);
        }
        boolean validateSignature = mediator.isValidateRequestsSignature();
        if (saml2IdpMd.isWantAuthnRequestsSigned() != null) validateSignature = saml2IdpMd.isWantAuthnRequestsSigned();
        if (validateSignature) {
            if (!endpoint.getBinding().equals(SSOBinding.SAMLR2_REDIRECT.getValue())) {
                if (request.getSignature() == null) throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, StatusDetails.INVALID_REQUEST_SIGNATURE);
                try {
                    if (originalRequest != null) signer.validateDom(saml2SpMd, originalRequest); else signer.validate(saml2SpMd, request);
                } catch (SamlR2SignatureValidationException e) {
                    throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, StatusDetails.INVALID_RESPONSE_SIGNATURE, e);
                } catch (SamlR2SignatureException e) {
                    throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, StatusDetails.INVALID_RESPONSE_SIGNATURE, e);
                }
            } else {
                try {
                    signer.validateQueryString(saml2SpMd, state.getTransientVariable("SAMLRequest"), state.getTransientVariable("RelayState"), state.getTransientVariable("SigAlg"), state.getTransientVariable("Signature"), false);
                } catch (SamlR2SignatureValidationException e) {
                    throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, StatusDetails.INVALID_RESPONSE_SIGNATURE, e);
                } catch (SamlR2SignatureException e) {
                    throw new SSORequestException(request, StatusCode.TOP_REQUESTER, StatusCode.REQUEST_DENIED, StatusDetails.INVALID_RESPONSE_SIGNATURE, e);
                }
            }
        }
    }

    /**
     * TODO : Use a strategy here
     * This has the logic to select endpoings for claims collecting.
     */
    protected IdentityMediationEndpoint selectNextClaimsEndpoint(AuthenticationState status) {
        ClaimChannel claimChannel = ((SPChannel) channel).getClaimsProvider();
        RequestedAuthnContextType reqAuthnCtx = null;
        if (status.getAuthnRequest() != null && status.getAuthnRequest().getRequestedAuthnContext() != null) {
            reqAuthnCtx = status.getAuthnRequest().getRequestedAuthnContext();
        }
        if (status.getCurrentClaimsEndpoint() != null) {
            AuthnCtxClass authnCtxClass = AuthnCtxClass.asEnum(status.getCurrentClaimsEndpoint().getType());
            if (authnCtxClass.isPassive() || status.getCurrentClaimsEndpointTryCount() >= 5) {
                status.getUsedClaimsEndpoints().add(status.getCurrentClaimsEndpoint().getName());
                status.setCurrentClaimsEndpoint(null);
                status.setCurrentClaimsEndpointTryCount(0);
            } else {
                status.setCurrentClaimsEndpointTryCount(status.getCurrentClaimsEndpointTryCount() + 1);
            }
        }
        if (status.getCurrentClaimsEndpoint() != null) {
            if (logger.isDebugEnabled()) logger.debug("Retry current claims endpoint : " + status.getCurrentClaimsEndpoint());
            return status.getCurrentClaimsEndpoint();
        }
        if (logger.isTraceEnabled()) logger.trace("Starting to select next claims endpoint ...");
        IdentityMediationEndpoint requestedEndpoint = null;
        IdentityMediationEndpoint availableEndpoint = null;
        for (IdentityMediationEndpoint endpoint : claimChannel.getEndpoints()) {
            if (logger.isTraceEnabled()) logger.trace("Processing claims endpoint " + endpoint);
            if (!endpoint.getBinding().equals(SSOBinding.SSO_ARTIFACT.getValue()) && !endpoint.getBinding().equals(SSOBinding.SSO_LOCAL.getValue())) {
                if (logger.isTraceEnabled()) logger.trace("Skip claims endpoint. Unsupported binding " + endpoint);
                continue;
            }
            if (status.getUsedClaimsEndpoints().contains(endpoint.getName())) {
                if (logger.isTraceEnabled()) logger.trace("Skip claims endpoint. Already used " + endpoint);
                continue;
            }
            if (reqAuthnCtx != null) {
                for (String reqAuthnCtxClass : reqAuthnCtx.getAuthnContextClassRef()) {
                    if (logger.isTraceEnabled()) logger.trace("Requested AuthnCtxClass for claiming " + reqAuthnCtxClass);
                    if (reqAuthnCtxClass.equals(endpoint.getType())) {
                        if (logger.isTraceEnabled()) logger.trace("Found requested AuthnCtxClass for claiming " + reqAuthnCtxClass);
                        requestedEndpoint = endpoint;
                        break;
                    }
                }
            }
            AuthnCtxClass authnCtxClass = AuthnCtxClass.asEnum(endpoint.getType());
            if (status.getAuthnRequest().isIsPassive() != null && status.getAuthnRequest().isIsPassive()) {
                if (!authnCtxClass.isPassive()) {
                    if (logger.isTraceEnabled()) logger.trace("Skip claims endpoint. Non-passive " + endpoint);
                    continue;
                }
            }
            if (availableEndpoint == null) {
                if (reqAuthnCtx == null && !endpoint.getBinding().equals(SSOBinding.SSO_ARTIFACT.getValue())) {
                    if (logger.isTraceEnabled()) logger.trace("Unsupported binding for non-requested endpoint : " + endpoint.getBinding());
                    continue;
                }
                if (logger.isDebugEnabled()) logger.debug("Selecting available endpoint : " + endpoint.getName());
                availableEndpoint = endpoint;
            }
        }
        if (requestedEndpoint != null) {
            if (logger.isTraceEnabled()) logger.trace("Selecting requested endpoint : " + requestedEndpoint);
            status.setCurrentClaimsEndpoint(requestedEndpoint);
            status.setCurrentClaimsEndpointTryCount(0);
        } else if (availableEndpoint != null) {
            if (logger.isTraceEnabled()) logger.trace("Selecting available endpoint : " + availableEndpoint);
            status.setCurrentClaimsEndpoint(availableEndpoint);
            status.setCurrentClaimsEndpointTryCount(0);
        } else {
            if (logger.isDebugEnabled()) logger.debug("No available claims endpoint!");
            return null;
        }
        if (logger.isDebugEnabled()) logger.debug("Current claims endpoint : " + status.getCurrentClaimsEndpoint());
        return status.getCurrentClaimsEndpoint();
    }

    protected SamlR2SecurityTokenEmissionContext emitAssertionFromPreviousSession(CamelMediationExchange exchange, SamlR2SecurityTokenEmissionContext ctx, AuthnRequestType authnRequest) throws Exception {
        AssertionType assertion = null;
        IdentityPlan identityPlan = findIdentityPlanOfType(SamlR2SecurityTokenToAuthnAssertionPlan.class);
        IdentityPlanExecutionExchange ex = createIdentityPlanExecutionExchange();
        CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(authnRequest.getIssuer());
        ex.setProperty(VAR_DESTINATION_COT_MEMBER, sp);
        ex.setProperty(WSTConstants.RST_CTX, ctx);
        ex.setTransientProperty(VAR_SAMLR2_SIGNER, ((SSOIDPMediator) channel.getIdentityMediator()).getSigner());
        ex.setTransientProperty(VAR_SAMLR2_ENCRYPTER, ((SSOIDPMediator) channel.getIdentityMediator()).getEncrypter());
        Set<Principal> principals = new HashSet<Principal>();
        SSOIdentityManager identityMgr = ((SPChannel) channel).getIdentityManager();
        SSOUser ssoUser = identityMgr.findUser(ctx.getSsoSession().getUsername());
        principals.add(ssoUser);
        SSORole[] roles = identityMgr.findRolesByUsername(ssoUser.getName());
        principals.addAll(Arrays.asList(roles));
        ex.setProperty(VAR_SUBJECT, new Subject(true, principals, new java.util.HashSet(), new java.util.HashSet()));
        AuthnStatementType authnStmt = (AuthnStatementType) ctx.getSsoSession().getSecurityToken().getContent();
        IdentityArtifact<AuthnStatementType> in = new IdentityArtifactImpl<AuthnStatementType>(new QName(SAML_ASSERTION_NS, "AuthnStatement"), authnStmt);
        ex.setIn(in);
        IdentityArtifact<AssertionType> out = new IdentityArtifactImpl<AssertionType>(new QName(SAML_ASSERTION_NS, "Assertion"), new AssertionType());
        ex.setOut(out);
        identityPlan.prepare(ex);
        identityPlan.perform(ex);
        if (!ex.getStatus().equals(IdentityPlanExecutionStatus.SUCCESS)) {
            throw new SecurityTokenEmissionException("Identity plan returned : " + ex.getStatus());
        }
        if (ex.getOut() == null) throw new SecurityTokenEmissionException("Plan Exchange OUT must not be null!");
        assertion = (AssertionType) ex.getOut().getContent();
        ctx.setAssertion(assertion);
        return ctx;
    }

    /**
     * This will return an emission context with both, the required SAMLR2 Assertion and the associated Subject.
     *
     * @return SamlR2 Security emission context containing SAMLR2 Assertion and Subject.
     */
    protected SamlR2SecurityTokenEmissionContext emitAssertionFromClaims(CamelMediationExchange exchange, SamlR2SecurityTokenEmissionContext securityTokenEmissionCtx, ClaimSet receivedClaims, CircleOfTrustMemberDescriptor sp) throws Exception {
        MessageQueueManager aqm = getArtifactQueueManager();
        Artifact emitterCtxArtifact = aqm.pushMessage(securityTokenEmissionCtx);
        SecurityTokenService sts = ((SPChannel) channel).getSecurityTokenService();
        RequestSecurityTokenType rst = buildRequestSecurityToken(receivedClaims, emitterCtxArtifact.getContent());
        if (logger.isDebugEnabled()) logger.debug("Requesting Security Token (RST) w/context " + rst.getContext());
        RequestSecurityTokenResponseType rstrt = sts.requestSecurityToken(rst);
        if (logger.isDebugEnabled()) logger.debug("Received Request Security Token Response (RSTR) w/context " + rstrt.getContext());
        securityTokenEmissionCtx = (SamlR2SecurityTokenEmissionContext) aqm.pullMessage(ArtifactImpl.newInstance(rstrt.getContext()));
        JAXBElement<RequestedSecurityTokenType> token = (JAXBElement<RequestedSecurityTokenType>) rstrt.getAny().get(1);
        AssertionType assertion = (AssertionType) token.getValue().getAny();
        if (logger.isDebugEnabled()) logger.debug("Generated SamlR2 Assertion " + assertion.getID());
        securityTokenEmissionCtx.setAssertion(assertion);
        return securityTokenEmissionCtx;
    }

    protected IdPSecurityContext createSecurityContext(CamelMediationExchange exchange, Subject authnSubject, AssertionType assertion) throws Exception {
        AuthnStatementType authnStmt = null;
        for (StatementAbstractType stmt : assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
            if (stmt instanceof AuthnStatementType) {
                authnStmt = (AuthnStatementType) stmt;
                break;
            }
        }
        if (authnStmt == null) throw new SSOException("Assertion MUST contain an AuthnStatement");
        SecurityToken<AuthnStatementType> st = new SecurityTokenImpl<AuthnStatementType>(authnStmt.getSessionIndex(), authnStmt);
        Principal userId = authnSubject.getPrincipals(SimplePrincipal.class).iterator().next();
        if (logger.isDebugEnabled()) logger.debug("Using username : " + userId.getName());
        String ssoSessionId = ((SPChannel) channel).getSessionManager().initiateSession(userId.getName(), st);
        assert ssoSessionId.equals(st.getId()) : "SSO Session Manager MUST use security token ID as session ID";
        return new IdPSecurityContext(authnSubject, ssoSessionId, authnStmt);
    }

    /**
     * Creates a new SAML Response for the given assertion
     */
    protected ResponseType buildSamlResponse(CamelMediationExchange exchange, AuthenticationState authnState, AssertionType assertion, CircleOfTrustMemberDescriptor sp, EndpointDescriptor spEndpoint) throws Exception {
        IdentityPlan identityPlan = findIdentityPlanOfType(SamlR2AuthnRequestToSamlR2ResponsePlan.class);
        IdentityPlanExecutionExchange idPlanExchange = createIdentityPlanExecutionExchange();
        idPlanExchange.setProperty(VAR_DESTINATION_COT_MEMBER, sp);
        idPlanExchange.setProperty(VAR_SAMLR2_ASSERTION, assertion);
        idPlanExchange.setProperty(VAR_DESTINATION_ENDPOINT_DESCRIPTOR, spEndpoint);
        idPlanExchange.setProperty(VAR_REQUEST, authnState.getAuthnRequest());
        idPlanExchange.setProperty(VAR_RESPONSE_MODE, authnState.getResponseMode());
        IdentityArtifact<AuthnRequestType> in = new IdentityArtifactImpl<AuthnRequestType>(new QName(SAMLR2Constants.SAML_PROTOCOL_NS, "AuthnRequest"), authnState.getAuthnRequest());
        idPlanExchange.setIn(in);
        IdentityArtifact<ResponseType> out = new IdentityArtifactImpl<ResponseType>(new QName(SAMLR2Constants.SAML_PROTOCOL_NS, "Response"), new ResponseType());
        idPlanExchange.setOut(out);
        identityPlan.prepare(idPlanExchange);
        identityPlan.perform(idPlanExchange);
        if (!idPlanExchange.getStatus().equals(IdentityPlanExecutionStatus.SUCCESS)) {
            throw new SecurityTokenEmissionException("Identity plan returned : " + idPlanExchange.getStatus());
        }
        if (idPlanExchange.getOut() == null) throw new SecurityTokenEmissionException("Plan Exchange OUT must not be null!");
        return (ResponseType) idPlanExchange.getOut().getContent();
    }

    /**
     * Build an AuthnRequest for the target SP to which IDP's unsollicited response needs to be pushed to.
     */
    protected AuthnRequestType buildIdPInitiatedAuthnRequest(CamelMediationExchange exchange, CircleOfTrustMemberDescriptor idp, EndpointDescriptor ed, FederationChannel spChannel) throws IdentityPlanningException, SSOException {
        IdentityPlan identityPlan = findIdentityPlanOfType(IDPInitiatedAuthnReqToSamlR2AuthnReqPlan.class);
        IdentityPlanExecutionExchange idPlanExchange = createIdentityPlanExecutionExchange();
        idPlanExchange.setProperty(VAR_DESTINATION_COT_MEMBER, idp);
        idPlanExchange.setProperty(VAR_DESTINATION_ENDPOINT_DESCRIPTOR, ed);
        idPlanExchange.setProperty(VAR_COT_MEMBER, spChannel.getMember());
        idPlanExchange.setProperty(VAR_RESPONSE_CHANNEL, spChannel);
        IDPInitiatedAuthnRequestType ssoAuthnRequest = (IDPInitiatedAuthnRequestType) ((CamelMediationMessage) exchange.getIn()).getMessage().getContent();
        IdentityArtifact in = new IdentityArtifactImpl(new QName("urn:org:atricore:idbus:sso:protocol", "IDPInitiatedAuthnRequest"), ssoAuthnRequest);
        idPlanExchange.setIn(in);
        IdentityArtifact<AuthnRequestType> out = new IdentityArtifactImpl<AuthnRequestType>(new QName(SAMLR2Constants.SAML_PROTOCOL_NS, "AuthnRequest"), new AuthnRequestType());
        idPlanExchange.setOut(out);
        identityPlan.prepare(idPlanExchange);
        identityPlan.perform(idPlanExchange);
        if (!idPlanExchange.getStatus().equals(IdentityPlanExecutionStatus.SUCCESS)) {
            throw new SecurityTokenEmissionException("Identity plan returned : " + idPlanExchange.getStatus());
        }
        if (idPlanExchange.getOut() == null) throw new SecurityTokenEmissionException("Plan Exchange OUT must not be null!");
        return (AuthnRequestType) idPlanExchange.getOut().getContent();
    }

    protected CircleOfTrustMemberDescriptor resolveProviderDescriptor(NameIDType issuer) {
        if (issuer.getFormat() != null && !issuer.getFormat().equals(NameIDFormat.ENTITY.getValue())) {
            logger.warn("Invalid issuer format for entity : " + issuer.getFormat());
            return null;
        }
        return getCotManager().lookupMemberByAlias(issuer.getValue());
    }

    protected MetadataEntry resolveSpMetadata() {
        return null;
    }

    protected EndpointDescriptor resolveSpAcsEndpoint(CamelMediationExchange exchange, AuthnRequestType authnRequest) throws SSOException {
        try {
            String requestedBinding = authnRequest.getProtocolBinding();
            if (logger.isDebugEnabled()) logger.debug("Requested binding/service" + authnRequest.getProtocolBinding() + "/" + authnRequest.getAssertionConsumerServiceURL());
            CircleOfTrust cot = this.getCot();
            CircleOfTrustMemberDescriptor sp = resolveProviderDescriptor(authnRequest.getIssuer());
            CircleOfTrustManager cotMgr = ((SPChannel) channel).getProvider().getCotManager();
            MetadataEntry md = cotMgr.findEntityRoleMetadata(sp.getAlias(), "urn:oasis:names:tc:SAML:2.0:metadata:SPSSODescriptor");
            SPSSODescriptorType samlr2sp = (SPSSODescriptorType) md.getEntry();
            IndexedEndpointType acEndpoint = null;
            IndexedEndpointType defaultAcEndpoint = null;
            IndexedEndpointType postAcEndpoint = null;
            IndexedEndpointType artifactAcEndpoint = null;
            for (IndexedEndpointType ac : samlr2sp.getAssertionConsumerService()) {
                if (authnRequest.getAssertionConsumerServiceIndex() != null && authnRequest.getAssertionConsumerServiceIndex() >= 0) {
                    if (ac.getIndex() == authnRequest.getAssertionConsumerServiceIndex()) {
                        acEndpoint = ac;
                        break;
                    }
                }
                if (authnRequest.getAssertionConsumerServiceURL() != null) {
                    if (ac.getLocation().equals(authnRequest.getAssertionConsumerServiceURL())) {
                        acEndpoint = ac;
                        break;
                    }
                }
                if (ac.isIsDefault() != null && ac.isIsDefault()) defaultAcEndpoint = ac;
                if (ac.getBinding().equals(SSOBinding.SAMLR2_POST.getValue())) postAcEndpoint = ac;
                if (ac.getBinding().equals(SSOBinding.SAMLR2_ARTIFACT.getValue())) artifactAcEndpoint = ac;
                if (requestedBinding != null && ac.getBinding().equals(requestedBinding)) {
                    acEndpoint = ac;
                    break;
                }
            }
            if (acEndpoint == null) acEndpoint = defaultAcEndpoint;
            if (acEndpoint == null) acEndpoint = artifactAcEndpoint;
            if (acEndpoint == null) acEndpoint = postAcEndpoint;
            if (acEndpoint == null) throw new SSOException("Cannot resolve response SP SSO endpoint for " + sp.getAlias());
            if (logger.isTraceEnabled()) logger.trace("Resolved ACS endpoint " + acEndpoint.getLocation() + "/" + acEndpoint.getBinding());
            return new EndpointDescriptorImpl(acEndpoint.getBinding(), SSOService.AssertionConsumerService.toString(), acEndpoint.getBinding(), acEndpoint.getLocation(), acEndpoint.getResponseLocation());
        } catch (CircleOfTrustManagerException e) {
            throw new SSOException(e);
        }
    }

    protected CircleOfTrustMemberDescriptor resolveIdp(CamelMediationExchange exchange) throws SSOException {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        IDPInitiatedAuthnRequestType ssoAuthnReq = (IDPInitiatedAuthnRequestType) in.getMessage().getContent();
        String idpAlias = null;
        CircleOfTrustMemberDescriptor idp = null;
        for (int i = 0; i < ssoAuthnReq.getRequestAttribute().size(); i++) {
            RequestAttributeType a = ssoAuthnReq.getRequestAttribute().get(i);
            if (a.getName().equals("atricore_idp_alias")) idpAlias = a.getValue();
        }
        if (idpAlias != null) {
            if (logger.isDebugEnabled()) logger.debug("Using IdP alias from request attribute " + idpAlias);
            idp = getCotManager().lookupMemberByAlias(idpAlias);
            if (idp == null) {
                throw new SSOException("No IDP found in circle of trust for received alias [" + idpAlias + "], verify your setup.");
            }
        }
        if (idp != null) return idp;
        SSOIDPMediator mediator = (SSOIDPMediator) channel.getIdentityMediator();
        idpAlias = mediator.getPreferredIdpAlias();
        if (idpAlias != null) {
            if (logger.isDebugEnabled()) logger.debug("Using preferred IdP alias " + idpAlias);
            idp = getCotManager().lookupMemberByAlias(idpAlias);
            if (idp == null) {
                throw new SSOException("No IDP found in circle of trust for preferred alias [" + idpAlias + "], verify your setup.");
            }
        }
        if (idp != null) return idp;
        return ((FederationChannel) channel).getMember();
    }

    protected FederationChannel resolveIdpChannel(CircleOfTrustMemberDescriptor idpDescriptor) {
        SPChannel spChannel = (SPChannel) channel;
        FederatedLocalProvider sp = spChannel.getProvider();
        FederationChannel idpChannel = sp.getChannel();
        for (FederationChannel fChannel : sp.getChannels()) {
            FederatedProvider idp = fChannel.getTargetProvider();
            for (CircleOfTrustMemberDescriptor member : idp.getMembers()) {
                if (member.getAlias().equals(idpDescriptor.getAlias())) {
                    if (logger.isDebugEnabled()) logger.debug("Selected IdP channel " + fChannel.getName() + " for provider " + idp.getName());
                    idpChannel = fChannel;
                    break;
                }
            }
        }
        return idpChannel;
    }

    protected EndpointType resolveIdpSsoEndpoint(CircleOfTrustMemberDescriptor idp) throws SSOException {
        SSOIDPMediator mediator = (SSOIDPMediator) channel.getIdentityMediator();
        SSOBinding preferredBinding = mediator.getPreferredIdpSSOBindingValue();
        MetadataEntry idpMd = idp.getMetadata();
        if (idpMd == null || idpMd.getEntry() == null) throw new SSOException("No metadata descriptor found for IDP " + idp);
        if (idpMd.getEntry() instanceof EntityDescriptorType) {
            EntityDescriptorType md = (EntityDescriptorType) idpMd.getEntry();
            for (RoleDescriptorType role : md.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor()) {
                if (role instanceof IDPSSODescriptorType) {
                    IDPSSODescriptorType idpSsoRole = (IDPSSODescriptorType) role;
                    EndpointType defaultEndpoint = null;
                    for (EndpointType idpSsoEndpoint : idpSsoRole.getSingleSignOnService()) {
                        SSOBinding b = SSOBinding.asEnum(idpSsoEndpoint.getBinding());
                        if (b.equals(preferredBinding)) return idpSsoEndpoint;
                        if (b.equals(SSOBinding.SAMLR2_ARTIFACT)) defaultEndpoint = idpSsoEndpoint;
                        if (defaultEndpoint == null) defaultEndpoint = idpSsoEndpoint;
                    }
                    return defaultEndpoint;
                }
            }
        } else {
            throw new SSOException("Unknown metadata descriptor type " + idpMd.getEntry().getClass().getName());
        }
        logger.debug("No IDP Endpoint supporting binding : " + preferredBinding);
        throw new SSOException("IDP does not support preferred binding " + preferredBinding);
    }

    protected EndpointDescriptor resolveIdpSsoContinueEndpoint() {
        String location = endpoint.getLocation();
        if (location.startsWith("/")) location = channel.getLocation() + location;
        EndpointDescriptor ed = new EndpointDescriptorImpl(endpoint.getName(), endpoint.getType(), SSOBinding.SSO_ARTIFACT.getValue(), location, null);
        if (logger.isTraceEnabled()) logger.trace("Resolved IDP SSO 'Continue' endpoint to " + ed);
        return ed;
    }

    /**
     * Create a new RSTR based on the received claims.
     *
     * @param claims  the claims sent by the user.
     * @param context the context string used in the request.
     */
    protected RequestSecurityTokenType buildRequestSecurityToken(ClaimSet claims, String context) throws Exception {
        logger.debug("generating RequestSecurityToken...");
        org.xmlsoap.schemas.ws._2005._02.trust.ObjectFactory of = new org.xmlsoap.schemas.ws._2005._02.trust.ObjectFactory();
        RequestSecurityTokenType rstRequest = new RequestSecurityTokenType();
        rstRequest.getAny().add(of.createTokenType(WSTConstants.WST_SAMLR2_TOKEN_TYPE));
        rstRequest.getAny().add(of.createRequestType(WSTConstants.WST_ISSUE_REQUEST));
        org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory ofwss = new org.oasis_open.docs.wss._2004._01.oasis_200401_wss_wssecurity_secext_1_0.ObjectFactory();
        for (Claim claim : claims.getClaims()) {
            logger.debug("Adding Claim : " + claim.getQualifier() + " of type " + claim.getValue().getClass().getName());
            Object claimObj = claim.getValue();
            if (claimObj instanceof UsernameTokenType) {
                rstRequest.getAny().add(ofwss.createUsernameToken((UsernameTokenType) claim.getValue()));
            } else if (claimObj instanceof BinarySecurityTokenType) {
                rstRequest.getAny().add(ofwss.createBinarySecurityToken((BinarySecurityTokenType) claim.getValue()));
            } else {
                throw new SSOException("Claim type not supported " + claimObj.getClass().getName());
            }
        }
        if (context != null) rstRequest.setContext(context);
        logger.debug("generated RequestSecurityToken [" + rstRequest + "]");
        return rstRequest;
    }

    protected MessageQueueManager getArtifactQueueManager() {
        SSOIDPMediator mediator = (SSOIDPMediator) channel.getIdentityMediator();
        return mediator.getArtifactQueueManager();
    }

    protected AuthenticationState newAuthnState(CamelMediationExchange exchange) {
        logger.debug("Creating new AuthenticationState");
        AuthenticationState state = new AuthenticationState();
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        in.getMessage().getState().setLocalVariable("urn:org:atricore:idbus:samlr2:idp:authn-state", state);
        return state;
    }

    protected AuthenticationState getAuthnState(CamelMediationExchange exchange) {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        AuthenticationState state = null;
        try {
            state = (AuthenticationState) in.getMessage().getState().getLocalVariable("urn:org:atricore:idbus:samlr2:idp:authn-state");
            if (logger.isTraceEnabled()) logger.trace("Using existing AuthnState " + state);
        } catch (IllegalStateException e) {
            if (logger.isDebugEnabled()) logger.debug("Provider state not supported " + e.getMessage());
            if (logger.isTraceEnabled()) logger.trace(e.getMessage(), e);
            state = new AuthenticationState();
        }
        if (state == null) {
            state = newAuthnState(exchange);
        }
        return state;
    }

    protected void clearAuthnState(CamelMediationExchange exchange) {
        CamelMediationMessage in = (CamelMediationMessage) exchange.getIn();
        in.getMessage().getState().removeLocalVariable("urn:org:atricore:idbus:samlr2:idp:authn-state");
        in.getMessage().getState().removeLocalVariable("urn:org:atricore:idbus:sso:protocol:responseMode");
        in.getMessage().getState().removeLocalVariable("urn:org:atricore:idbus:sso:protocol:responseFormat");
    }

    protected oasis.names.tc.saml._1_0.protocol.ResponseType transformSamlR2ResponseToSaml11(ResponseType responseType) {
        oasis.names.tc.saml._1_0.assertion.ObjectFactory saml11AssertionObjectFactory = new oasis.names.tc.saml._1_0.assertion.ObjectFactory();
        oasis.names.tc.saml._1_0.protocol.ResponseType saml11Response = new oasis.names.tc.saml._1_0.protocol.ResponseType();
        saml11Response.setIssueInstant(responseType.getIssueInstant());
        saml11Response.setMinorVersion(BigInteger.valueOf(1));
        saml11Response.setMajorVersion(BigInteger.valueOf(1));
        saml11Response.setRecipient(responseType.getDestination());
        saml11Response.setResponseID(responseType.getID());
        saml11Response.setInResponseTo(responseType.getInResponseTo());
        oasis.names.tc.saml._1_0.protocol.StatusType saml11ResponseStatus = new oasis.names.tc.saml._1_0.protocol.StatusType();
        oasis.names.tc.saml._1_0.protocol.StatusCodeType saml11ResponseStatusCode = new oasis.names.tc.saml._1_0.protocol.StatusCodeType();
        if (responseType.getStatus().getStatusCode().getValue().equals("urn:oasis:names:tc:SAML:2.0:status:Success")) {
            saml11ResponseStatusCode.setValue(new QName("urn:oasis:names:tc:SAML:1.0:protocol", "Success"));
        }
        saml11ResponseStatus.setStatusCode(saml11ResponseStatusCode);
        saml11Response.setStatus(saml11ResponseStatus);
        for (Object aoe : responseType.getAssertionOrEncryptedAssertion()) {
            if (aoe instanceof oasis.names.tc.saml._2_0.assertion.AssertionType) {
                AssertionType saml2Assertion = (oasis.names.tc.saml._2_0.assertion.AssertionType) aoe;
                oasis.names.tc.saml._1_0.assertion.AssertionType saml11Assertion = new oasis.names.tc.saml._1_0.assertion.AssertionType();
                saml11Assertion.setMinorVersion(BigInteger.valueOf(1));
                saml11Assertion.setMajorVersion(BigInteger.valueOf(1));
                saml11Assertion.setAssertionID(saml2Assertion.getID());
                saml11Assertion.setIssuer(saml2Assertion.getIssuer().getValue());
                saml11Assertion.setIssueInstant(saml2Assertion.getIssueInstant());
                ConditionsType saml2Conditions = saml2Assertion.getConditions();
                oasis.names.tc.saml._1_0.assertion.ConditionsType saml11Conditions = new oasis.names.tc.saml._1_0.assertion.ConditionsType();
                saml11Conditions.setNotBefore(saml2Conditions.getNotBefore());
                saml11Conditions.setNotOnOrAfter(saml2Conditions.getNotOnOrAfter());
                saml11Assertion.setConditions(saml11Conditions);
                for (ConditionAbstractType cond : saml2Conditions.getConditionOrAudienceRestrictionOrOneTimeUse()) {
                    if (cond instanceof AudienceRestrictionType) {
                        AudienceRestrictionType saml2ar = (AudienceRestrictionType) cond;
                        oasis.names.tc.saml._1_0.assertion.AudienceRestrictionConditionType saml11arc = new AudienceRestrictionConditionType();
                        for (String audience : saml2ar.getAudience()) {
                            saml11arc.getAudience().add(audience);
                        }
                        saml11Conditions.getAudienceRestrictionConditionOrDoNotCacheConditionOrCondition().add(saml11arc);
                        break;
                    }
                }
                for (StatementAbstractType s : saml2Assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement()) {
                    if (s instanceof AuthnStatementType) {
                        AuthnStatementType saml2authnStatement = (AuthnStatementType) s;
                        oasis.names.tc.saml._1_0.assertion.AuthenticationStatementType saml11authnStatement = new oasis.names.tc.saml._1_0.assertion.AuthenticationStatementType();
                        oasis.names.tc.saml._1_0.assertion.AttributeStatementType saml11attrStatement = new oasis.names.tc.saml._1_0.assertion.AttributeStatementType();
                        saml11authnStatement.setAuthenticationInstant(saml2authnStatement.getAuthnInstant());
                        AuthnContextType saml2AuthnContext = saml2authnStatement.getAuthnContext();
                        if (saml2AuthnContext.getContent().size() > 0) {
                            JAXBElement acc = saml2AuthnContext.getContent().get(0);
                            String saml2authnCtxClassRef = (String) acc.getValue();
                            if (saml2authnCtxClassRef.equals("urn:oasis:names:tc:SAML:2.0:ac:classes:Password")) {
                                saml11authnStatement.setAuthenticationMethod("urn:oasis:names:tc:SAML:1.0:am:password");
                            }
                        } else {
                            saml11authnStatement.setAuthenticationMethod("urn:oasis:names:tc:SAML:1.0:am:unspecified");
                        }
                        SubjectType saml2Subject = saml2Assertion.getSubject();
                        oasis.names.tc.saml._1_0.assertion.SubjectType saml11Subject = new oasis.names.tc.saml._1_0.assertion.SubjectType();
                        for (JAXBElement sc : saml2Subject.getContent()) {
                            Object scv = sc.getValue();
                            if (scv instanceof NameIDType) {
                                NameIDType saml2nameid = (NameIDType) scv;
                                oasis.names.tc.saml._1_0.assertion.NameIdentifierType saml11nameid = new oasis.names.tc.saml._1_0.assertion.NameIdentifierType();
                                saml11nameid.setNameQualifier(saml2nameid.getNameQualifier());
                                saml11nameid.setValue(saml2nameid.getValue());
                                saml11Subject.getContent().add(saml11AssertionObjectFactory.createNameIdentifier(saml11nameid));
                            } else if (scv instanceof SubjectConfirmationType) {
                                SubjectConfirmationType saml2subjectConfirmation = (SubjectConfirmationType) scv;
                                oasis.names.tc.saml._1_0.assertion.SubjectConfirmationType saml11subjectConfirmation = new oasis.names.tc.saml._1_0.assertion.SubjectConfirmationType();
                                if (saml2subjectConfirmation.getMethod().equals("urn:oasis:names:tc:SAML:2.0:cm:bearer")) {
                                    saml11subjectConfirmation.getConfirmationMethod().add("urn:oasis:names:tc:SAML:1.0:cm:bearer");
                                    saml11Subject.getContent().add(saml11AssertionObjectFactory.createSubjectConfirmation(saml11subjectConfirmation));
                                }
                            }
                        }
                        saml11authnStatement.setSubject(saml11Subject);
                        saml11Assertion.getStatementOrSubjectStatementOrAuthenticationStatement().add(saml11authnStatement);
                    }
                }
                saml11Response.getAssertion().add(saml11Assertion);
            }
        }
        return saml11Response;
    }

    protected List<SSOPolicyEnforcementStatement> getPolicyEnforcementStatements(AssertionType assertion) {
        List<SSOPolicyEnforcementStatement> policyStatements = new ArrayList<SSOPolicyEnforcementStatement>();
        List<StatementAbstractType> stmts = assertion.getStatementOrAuthnStatementOrAuthzDecisionStatement();
        if (stmts == null) return policyStatements;
        for (StatementAbstractType stmt : stmts) {
            if (stmt instanceof AttributeStatementType) {
                AttributeStatementType attrStmt = (AttributeStatementType) stmt;
                if (attrStmt.getAttributeOrEncryptedAttribute() == null) continue;
                for (Object attrOrEncAttr : attrStmt.getAttributeOrEncryptedAttribute()) {
                    if (attrOrEncAttr == null) continue;
                    if (attrOrEncAttr instanceof AttributeType) {
                        AttributeType attr = (AttributeType) attrOrEncAttr;
                        SSOPasswordPolicyEnforcement policy = null;
                        if (attr.getName().startsWith(PasswordPolicyEnforcementWarning.NAMESPACE)) {
                            if (logger.isTraceEnabled()) logger.trace("Processing Password Policy Warning statement : " + attr.getFriendlyName());
                            policy = new PasswordPolicyEnforcementWarning(PasswordPolicyWarningType.fromName(attr.getFriendlyName()));
                            if (attr.getAttributeValue() != null) {
                                if (logger.isTraceEnabled()) logger.trace("Processing Password Policy Warning statement values, total " + attr.getAttributeValue().size());
                                policy.getValues().addAll(attr.getAttributeValue());
                            }
                        } else if (attr.getName().startsWith(PasswordPolicyEnforcementError.NAMESPACE)) {
                            if (logger.isTraceEnabled()) logger.trace("Processing Password Policy Error statement : " + attr.getFriendlyName());
                            policy = new PasswordPolicyEnforcementError(PasswordPolicyErrorType.fromName(attr.getFriendlyName()));
                        } else {
                            logger.trace("Ignoring attribute : " + attr.getName());
                        }
                        if (policy != null) policyStatements.add(policy);
                    } else {
                        logger.warn("Unsupported Attribute Type " + attrOrEncAttr.getClass().getName());
                    }
                }
            }
        }
        return policyStatements;
    }

    protected String getSTSPlanName() throws SSOException {
        Map<String, SamlR2SecurityTokenToAuthnAssertionPlan> stsPlans = applicationContext.getBeansOfType(SamlR2SecurityTokenToAuthnAssertionPlan.class);
        SamlR2SecurityTokenToAuthnAssertionPlan stsPlan = null;
        for (IdentityPlan plan : endpoint.getIdentityPlans()) {
            if (plan instanceof SamlR2SecurityTokenToAuthnAssertionPlan) {
                stsPlan = (SamlR2SecurityTokenToAuthnAssertionPlan) plan;
                break;
            }
        }
        if (stsPlan == null) throw new SSOException("No valid STS Plan found, looking for SamlR2SecurityTokenToAuthnAssertionPlan instances");
        for (String planName : stsPlans.keySet()) {
            SamlR2SecurityTokenToAuthnAssertionPlan registeredStsPlan = stsPlans.get(planName);
            if (registeredStsPlan == stsPlan) {
                if (logger.isTraceEnabled()) logger.trace("Using STS plan : " + planName);
                return planName;
            }
        }
        logger.warn("No STS plan found for endpoint : " + endpoint.getName());
        return null;
    }

    private EndpointDescriptor resolveSPInitiatedSSOProxyEndpointDescriptor(CamelMediationExchange exchange, BindingChannel bc) throws SSOException {
        try {
            logger.debug("Looking for " + SSOService.SPInitiatedSingleSignOnServiceProxy.toString());
            for (IdentityMediationEndpoint endpoint : bc.getEndpoints()) {
                logger.debug("Processing endpoint : " + endpoint.getType() + "[" + endpoint.getBinding() + "]");
                if (endpoint.getType().equals(SSOService.SPInitiatedSingleSignOnServiceProxy.toString())) {
                    if (endpoint.getBinding().equals(SSOBinding.SSO_ARTIFACT.getValue())) {
                        return bc.getIdentityMediator().resolveEndpoint(bc, endpoint);
                    }
                }
            }
        } catch (IdentityMediationException e) {
            throw new SSOException(e);
        }
        throw new SSOException("No SP Initiated SSO Proxy endpoint found for SP Initiated SSO using SSO Artifact binding");
    }

    /**
     * Creates an Authentication Proxy Request which is essentially - at least as the current release -
     *
     * @return
     */
    protected SPInitiatedAuthnRequestType buildAuthnProxyRequest(AuthnRequestType source) {
        SPInitiatedAuthnRequestType target = new SPInitiatedAuthnRequestType();
        target.setID(uuidGenerator.generateId());
        target.setPassive(source.isIsPassive());
        return target;
    }
}
