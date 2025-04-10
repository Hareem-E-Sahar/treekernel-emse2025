package org.atricore.idbus.capabilities.sso.main.idp.plans.actions;

import oasis.names.tc.saml._2_0.assertion.NameIDType;
import oasis.names.tc.saml._2_0.metadata.*;
import oasis.names.tc.saml._2_0.protocol.AuthnRequestType;
import oasis.names.tc.saml._2_0.protocol.NameIDPolicyType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.atricore.idbus.capabilities.sso.main.SSOException;
import org.atricore.idbus.capabilities.sso.main.common.plans.actions.AbstractSSOAction;
import org.atricore.idbus.capabilities.sso.main.idp.SSOIDPMediator;
import org.atricore.idbus.capabilities.sso.support.binding.SSOBinding;
import org.atricore.idbus.capabilities.sso.support.core.NameIDFormat;
import org.atricore.idbus.capabilities.sso.support.metadata.SSOService;
import org.atricore.idbus.common.sso._1_0.protocol.IDPInitiatedAuthnRequestType;
import org.atricore.idbus.common.sso._1_0.protocol.RequestAttributeType;
import org.atricore.idbus.kernel.main.federation.metadata.CircleOfTrustManager;
import org.atricore.idbus.kernel.main.federation.metadata.CircleOfTrustMemberDescriptor;
import org.atricore.idbus.kernel.main.federation.metadata.MetadataEntry;
import org.atricore.idbus.kernel.main.mediation.Channel;
import org.atricore.idbus.kernel.main.mediation.channel.FederationChannel;
import org.atricore.idbus.kernel.main.mediation.channel.SPChannel;
import org.atricore.idbus.kernel.main.mediation.endpoint.IdentityMediationEndpoint;
import org.atricore.idbus.kernel.main.mediation.provider.FederatedLocalProvider;
import org.atricore.idbus.kernel.main.mediation.provider.FederatedRemoteProvider;
import org.atricore.idbus.kernel.main.mediation.provider.Provider;
import org.atricore.idbus.kernel.planning.IdentityArtifact;
import org.jbpm.graph.exe.ExecutionContext;

/**
 * @author <a href="mailto:gbrigand@atricore.org">Gianluca Brigandi</a>
 * @version $Id: InitializeAuthnRequestAction.java 1359 2009-07-19 16:57:57Z sgonzalez $
 */
public class InitializeAuthnRequestAction extends AbstractSSOAction {

    private static final Log logger = LogFactory.getLog(InitializeAuthnRequestAction.class);

    protected void doExecute(IdentityArtifact in, IdentityArtifact out, ExecutionContext executionContext) throws Exception {
        if (in == null || out == null) return;
        IDPInitiatedAuthnRequestType ssoAuthnReq = (IDPInitiatedAuthnRequestType) in.getContent();
        AuthnRequestType authn = (AuthnRequestType) out.getContent();
        SPChannel channel = (SPChannel) executionContext.getContextInstance().getVariable(VAR_CHANNEL);
        FederationChannel spChannel = (FederationChannel) executionContext.getContextInstance().getVariable(VAR_RESPONSE_CHANNEL);
        IdentityMediationEndpoint endpoint = (IdentityMediationEndpoint) executionContext.getContextInstance().getVariable(VAR_ENDPOINT);
        CircleOfTrustMemberDescriptor idp = (CircleOfTrustMemberDescriptor) executionContext.getContextInstance().getVariable(VAR_DESTINATION_COT_MEMBER);
        SSOIDPMediator mediator = (SSOIDPMediator) spChannel.getIdentityMediator();
        CircleOfTrustMemberDescriptor spCotMember = resolveSpAlias(channel, ssoAuthnReq);
        assert spCotMember != null : "Destination SP for IDP Initiated SSO not found!";
        NameIDType issuer = new NameIDType();
        issuer.setFormat(NameIDFormat.ENTITY.getValue());
        issuer.setValue(spCotMember.getAlias());
        authn.setIssuer(issuer);
        String nameIdPolicyFormat = resolveNameIdFormat(idp, mediator.getPreferredNameIdPolicy());
        NameIDPolicyType nameIdPolicy = new NameIDPolicyType();
        nameIdPolicy.setFormat(nameIdPolicyFormat);
        nameIdPolicy.setAllowCreate(true);
        authn.setNameIDPolicy(nameIdPolicy);
        authn.setForceAuthn(false);
        authn.setIsPassive(ssoAuthnReq != null && ssoAuthnReq.isPassive());
        MetadataEntry destinationSPMetadataEntry = spCotMember.getMetadata();
        oasis.names.tc.saml._2_0.metadata.EntityDescriptorType entity;
        entity = (oasis.names.tc.saml._2_0.metadata.EntityDescriptorType) destinationSPMetadataEntry.getEntry();
        SPSSODescriptorType destinationSPMetadata = (SPSSODescriptorType) entity.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor().get(0);
        IndexedEndpointType acEndpoint = null;
        acEndpoint = destinationSPMetadata.getAssertionConsumerService().get(0);
        if (logger.isTraceEnabled()) logger.trace("Resolved ACS endpoint " + acEndpoint.getLocation() + "/" + acEndpoint.getBinding());
        assert acEndpoint != null : "Cannot resolve Assertion Consumer Service Endpoint for Destination SP : " + destinationSPMetadata.getID();
        authn.setAssertionConsumerServiceURL(acEndpoint.getLocation());
        authn.setProtocolBinding(acEndpoint.getBinding());
    }

    /**
     * This finds the ACS endpoint where we want IDP to send responses, based on the destination IDP,
     * the channel used to receive requests from that IdP and the endpoint where the incoming message was received, if any.
     * 
     * @param idpChannel The channel we're mediating
     * @param idp the identity provider metadata
     * @return
     */
    protected IdentityMediationEndpoint resolveAcsEndpoint(CircleOfTrustMemberDescriptor idp, FederationChannel idpChannel, IdentityMediationEndpoint incomingEndpoint) {
        if (log.isDebugEnabled()) log.debug("Looking for ACS endpoint. Idp: " + idp.getAlias() + ", federation channel: " + idpChannel.getName());
        SSOBinding incomingEndpointBinding = null;
        IdentityMediationEndpoint acsEndpoint = null;
        IdentityMediationEndpoint acsPostEndpoint = null;
        IdentityMediationEndpoint acsArtEndpoint = null;
        String acsEndpointType = SSOService.AssertionConsumerService.toString();
        if (log.isDebugEnabled()) log.debug("Selected IdP channel " + idpChannel.getName());
        if (incomingEndpoint != null) {
            incomingEndpointBinding = SSOBinding.asEnum(incomingEndpoint.getBinding());
            if (log.isTraceEnabled()) log.trace("Incomming endpoint " + incomingEndpoint);
        }
        for (IdentityMediationEndpoint endpoint : idpChannel.getEndpoints()) {
            if (endpoint.getType().equals(acsEndpointType)) {
                SSOBinding endpointBinding = SSOBinding.asEnum(endpoint.getBinding());
                if (incomingEndpointBinding != null) {
                    if (incomingEndpointBinding.isFrontChannel() == endpointBinding.isFrontChannel()) {
                        acsEndpoint = endpoint;
                    }
                } else {
                    acsEndpoint = endpoint;
                    if (endpoint.getBinding().equals(SSOBinding.SAMLR2_POST.getValue())) acsPostEndpoint = endpoint;
                    if (endpoint.getBinding().equals(SSOBinding.SAMLR2_ARTIFACT.getValue())) acsArtEndpoint = endpoint;
                }
            }
        }
        if (acsEndpoint == null) acsEndpoint = acsArtEndpoint;
        if (acsEndpoint == null) acsEndpoint = acsPostEndpoint;
        if (log.isDebugEnabled()) log.debug("Selected ACS endpoint " + (acsEndpoint != null ? acsEndpoint.getName() : "<Null>"));
        return acsEndpoint;
    }

    /**
     * This will select the name ID format from the IdP metadata descriptor as follows:<br>
     * <br>
     * 1. If <b>preferredNameIdFormat</b> is supported by the IdP, it will be selected.<br>
     * 2. Else, if <b>transient</b> name id format is supported by the IdP, it will be selected.<br>
     * 3. Else, the first format supported by the IdP will be selected.
     *
     */
    protected String resolveNameIdFormat(CircleOfTrustMemberDescriptor idp, String preferredNameIdFormat) throws SSOException {
        MetadataEntry idpMd = idp.getMetadata();
        String selectedNameIdFormat = null;
        String defaultNameIdFormat = null;
        if (idpMd.getEntry() instanceof EntityDescriptorType) {
            EntityDescriptorType md = (EntityDescriptorType) idpMd.getEntry();
            for (RoleDescriptorType role : md.getRoleDescriptorOrIDPSSODescriptorOrSPSSODescriptor()) {
                if (role instanceof IDPSSODescriptorType) {
                    IDPSSODescriptorType idpSsoRole = (IDPSSODescriptorType) role;
                    for (String nameIdFormat : idpSsoRole.getNameIDFormat()) {
                        if (preferredNameIdFormat != null && nameIdFormat.equals(preferredNameIdFormat)) selectedNameIdFormat = nameIdFormat;
                        if (nameIdFormat.equals(NameIDFormat.TRANSIENT.toString())) defaultNameIdFormat = nameIdFormat;
                        if (defaultNameIdFormat == null) defaultNameIdFormat = nameIdFormat;
                    }
                }
            }
        } else throw new SSOException("Unsupported Metadata type " + idpMd.getEntry() + ", SAML 2 Metadata expected");
        if (selectedNameIdFormat == null) selectedNameIdFormat = defaultNameIdFormat;
        if (logger.isDebugEnabled()) logger.debug("Selected NameIDFormat for " + idp.getAlias() + " is " + selectedNameIdFormat);
        return selectedNameIdFormat;
    }

    protected CircleOfTrustMemberDescriptor resolveSpAlias(SPChannel spChannel, IDPInitiatedAuthnRequestType ssoAuthnReq) throws SSOException {
        CircleOfTrustMemberDescriptor spDescr = null;
        SSOIDPMediator mediator = (SSOIDPMediator) spChannel.getIdentityMediator();
        if (spChannel.getTargetProvider() != null) {
            Provider sp = spChannel.getTargetProvider();
            if (sp instanceof FederatedRemoteProvider) {
                FederatedRemoteProvider spr = (FederatedRemoteProvider) sp;
                if (spr.getMembers().size() > 0) {
                    if (logger.isTraceEnabled()) logger.trace("Using first member descriptor for remote SP provider " + sp.getName());
                    spDescr = spr.getMembers().get(0);
                } else {
                    logger.error("No Circle of Trust Member descriptor found for remote SP Definition " + spr.getName());
                }
            } else {
                FederatedLocalProvider spl = (FederatedLocalProvider) sp;
                if (spl.getChannels() != null) {
                    for (Channel c : spl.getChannels()) {
                        if (c instanceof FederationChannel) {
                            FederationChannel fc = (FederationChannel) c;
                            if (fc.getTargetProvider() != null && fc.getTargetProvider().getName().equals(spChannel.getProvider().getName())) {
                                if (logger.isTraceEnabled()) logger.trace("Using SP Alias " + fc.getMember().getAlias() + " from channel " + fc.getName());
                                spDescr = fc.getMember();
                            }
                        }
                    }
                }
                if (spDescr == null) {
                    if (logger.isTraceEnabled()) logger.trace("Using SP Alias " + spl.getChannel().getMember().getAlias() + " from default channel " + spl.getChannel().getName());
                    spDescr = spl.getChannel().getMember();
                }
            }
        } else {
            String spAlias = mediator.getPreferredSpAlias();
            CircleOfTrustManager cotManager = spChannel.getProvider().getCotManager();
            if (ssoAuthnReq != null) {
                for (RequestAttributeType a : ssoAuthnReq.getRequestAttribute()) {
                    if (a.getName().equals("atricore_sp_id")) {
                        spDescr = cotManager.loolkupMemberById(a.getValue());
                        break;
                    }
                    if (a.getName().equals("atricore_sp_alias")) {
                        spDescr = cotManager.lookupMemberByAlias(a.getValue());
                        break;
                    }
                }
            }
            if (spDescr == null) spDescr = cotManager.lookupMemberByAlias(spAlias);
            if (logger.isTraceEnabled()) logger.trace("Using Preferred SP Alias " + spAlias);
            if (spDescr == null) {
                throw new SSOException("Cannot find SP for AuthnRequest ");
            }
        }
        if (logger.isDebugEnabled()) logger.debug("Resolved SP " + (spDescr != null ? spDescr.getAlias() : "NULL"));
        return spDescr;
    }
}
