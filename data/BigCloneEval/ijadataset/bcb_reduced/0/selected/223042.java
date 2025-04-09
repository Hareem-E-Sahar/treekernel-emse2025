package ch.olsen.servicecontainer.domain;

import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import ch.olsen.products.util.Application;
import ch.olsen.products.util.logging.Logger;
import ch.olsen.servicecontainer.commongwt.client.DomainInfos;
import ch.olsen.servicecontainer.internalservice.auth.User;
import ch.olsen.servicecontainer.internalservice.auth.AccessControlElement.Role;
import ch.olsen.servicecontainer.internalservice.log.LogInterface;
import ch.olsen.servicecontainer.naming.OscURI;
import ch.olsen.servicecontainer.naming.OsnURI;
import ch.olsen.servicecontainer.node.SCNode;
import ch.olsen.servicecontainer.service.StartServiceException;

public class RemoteDomain implements SCDomain {

    final ServiceAdapter adapter;

    OscURI oscUri;

    OsnURI osnUri;

    Object service;

    SCNode parentNode;

    ch.olsen.products.util.logging.Logger log;

    public RemoteDomain(SCNode parentNode, ServiceAdapterIfc adapter, OscURI oscUri) throws RemoteException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException {
        this.parentNode = parentNode;
        this.adapter = new ServiceAdapter();
        this.adapter.cl = adapter.getClassLoader();
        ClassLoader oldcl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(this.adapter.cl);
        this.adapter.adapterClass = adapter.getAdapterClass();
        this.adapter.ifc = adapter.getIfc();
        this.adapter.remoteClient = adapter.getRemoteClient();
        this.adapter.remoteIfc = adapter.getRemoteIfc();
        this.adapter.remoteStub = adapter.getRemoteStub();
        this.adapter.stub = adapter.getStub();
        this.adapter.uri = adapter.getUri();
        this.adapter.localDomainRemoteIfc = adapter.getLocalDomainRemoteIfc();
        service = this.adapter.remoteClient.getConstructors()[0].newInstance(this.adapter.stub);
        Thread.currentThread().setContextClassLoader(oldcl);
        this.oscUri = oscUri;
        this.osnUri = oscUri.toOsnURI();
        LogInterface l = parentNode.getRootDomain().getLoggerService();
        if (l != null) log = l.getLogger(this, ""); else log = Application.getLogger(this.oscUri.getURI());
    }

    public ServiceAdapter getAdapter() {
        return adapter;
    }

    public OscURI getOscUri() {
        return oscUri;
    }

    public OsnURI getOsnUri() {
        return osnUri;
    }

    public Object getProxy(ClassLoader targetClassLoader) throws InstantiationException {
        try {
            Object proxy = adapter.getProxy(this, targetClassLoader);
            return proxy;
        } catch (IllegalArgumentException e) {
            throw new InstantiationException(e.getMessage());
        } catch (SecurityException e) {
            throw new InstantiationException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new InstantiationException(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InstantiationException(e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    public Object getService() {
        return service;
    }

    public SCDomain lookup(OsnURI uri) {
        if (uri.equals(this.osnUri)) return this;
        return recurseLookup(uri);
    }

    public SCDomain recurseLookup(OsnURI uri) {
        if (uri.equals(this.osnUri)) return this;
        String nextPathElement = uri.isSubdomainOf(this.osnUri);
        if (nextPathElement != null) {
            try {
                ServiceAdapterIfc adapter = this.adapter.localDomainRemoteIfc.remoteLookup(uri);
                if (adapter != null) return new RemoteDomain(parentNode, adapter, uri.toOscUri(parentNode.getRmiName()));
            } catch (Exception e) {
                log.warn("Could not lookup remote service: " + e.getMessage(), e);
            }
            return null;
        }
        return parentNode.lookup(uri);
    }

    public SCDomain lookup(OscURI uri) {
        return parentNode.lookup(uri);
    }

    public SCDomain relookup() {
        return lookup(osnUri);
    }

    public void passivate() {
        try {
            this.adapter.localDomainRemoteIfc.passivate();
        } catch (RemoteException e) {
            log.warn("Could not shutdown remote domain: " + e.getMessage(), e);
        }
    }

    public void shutdown() {
        try {
            this.adapter.localDomainRemoteIfc.shutdown();
        } catch (RemoteException e) {
            log.warn("Could not shutdown remote domain: " + e.getMessage(), e);
        }
    }

    public void notifyServicePassivate(OsnURI osnUri) {
        try {
            this.adapter.localDomainRemoteIfc.notifyServicePassivate(osnUri);
        } catch (RemoteException e) {
            log.warn("Could not notify remote domain: " + e.getMessage(), e);
        }
    }

    public void notifyServiceShutdown(OsnURI osnUri) {
        try {
            this.adapter.localDomainRemoteIfc.notifyServiceShutdown(osnUri);
        } catch (RemoteException e) {
            log.warn("Could not notify remote domain: " + e.getMessage(), e);
        }
    }

    public final Logger getLogger() {
        return log;
    }

    public DomainInfos getDomainInfos(User user) {
        try {
            return this.adapter.localDomainRemoteIfc.getDomainInfos(user);
        } catch (RemoteException e) {
            log.warn("Error while getting domain infos for " + getOscUri().getURI(), e);
            return null;
        }
    }

    public void shutdownService(String session) {
        try {
            adapter.localDomainRemoteIfc.shutdownService(session);
        } catch (Exception e) {
            log.warn("Error while shutting down " + getOscUri().getURI(), e);
        }
    }

    public ClassLoader getClassLoader() {
        try {
            return adapter.localDomainRemoteIfc.getClassLoader();
        } catch (Exception e) {
            log.warn("Error while getting ownerId for " + getOscUri().getURI(), e);
        }
        return Thread.currentThread().getContextClassLoader();
    }

    public String getServiceClassName() {
        try {
            return adapter.localDomainRemoteIfc.getServiceClassName();
        } catch (Exception e) {
            log.warn("Error while getting ownerId for " + getOscUri().getURI(), e);
        }
        return null;
    }

    public String getServiceIfcName() {
        try {
            return adapter.localDomainRemoteIfc.getServiceIfcName();
        } catch (Exception e) {
            log.warn("Error while getting ownerId for " + getOscUri().getURI(), e);
        }
        return null;
    }

    public SCDomain startSubService(String session, String serviceClass, String serviceInterface, String name, ClassLoader classLoader) throws StartServiceException {
        try {
            ServiceAdapterIfc adapter = this.adapter.localDomainRemoteIfc.remoteStartSubService(session, serviceClass, serviceInterface, name, classLoader, false);
            if (adapter != null) return new RemoteDomain(parentNode, adapter, new OscURI(oscUri.getURI() + name + "/"));
        } catch (Exception e) {
            log.warn("Error while starting remote sub service " + getOscUri().getURI(), e);
        }
        return null;
    }

    public SCDomain restoreSubService(String session, String serviceClass, String serviceInterface, String name, ClassLoader classLoader) throws StartServiceException {
        try {
            ServiceAdapterIfc adapter = this.adapter.localDomainRemoteIfc.remoteStartSubService(session, serviceClass, serviceInterface, name, classLoader, true);
            if (adapter != null) return new RemoteDomain(parentNode, adapter, new OscURI(oscUri.getURI() + name + "/"));
        } catch (Exception e) {
            log.warn("Error while starting remote sub service " + getOscUri().getURI(), e);
        }
        return null;
    }

    public Role getRoleHierarchy() {
        try {
            return this.adapter.localDomainRemoteIfc.getRoleHierarchy();
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isAnonymousAccess() {
        try {
            return this.adapter.localDomainRemoteIfc.isAnonymousAccess();
        } catch (Exception e) {
            return false;
        }
    }
}
