package com.google.api.gwt.client.impl;

import com.google.api.gwt.client.GoogleApiRequestTransport;
import com.google.web.bindery.requestfactory.shared.Receiver;
import com.google.web.bindery.requestfactory.shared.ServerFailure;

/**
 * Provides access to Google API endpoints for RequestFactory.
 *
 * @deprecated Use {@link GoogleApiRequestTransport}.
 */
@Deprecated
public class ClientGoogleApiRequestTransport implements com.google.api.gwt.shared.GoogleApiRequestTransport<ClientGoogleApiRequestTransport> {

    private String baseUrl = "https://www.googleapis.com";

    private String apiKey;

    private String applicationName;

    private GoogleApiRequestTransport transport;

    @Override
    public void create(@SuppressWarnings("rawtypes") Receiver<com.google.api.gwt.shared.GoogleApiRequestTransport> receiver) {
        if (this.transport != null) {
            receiver.onFailure(new ServerFailure("Transport is already created."));
        }
        this.transport = new GoogleApiRequestTransport(baseUrl, applicationName, apiKey);
        receiver.onSuccess(this);
    }

    @Override
    public void send(String payload, TransportReceiver receiver) {
        if (this.transport == null) {
            receiver.onTransportFailure(new ServerFailure("Must call create() before making requests."));
        }
        transport.send(payload, receiver);
    }

    @Override
    public ClientGoogleApiRequestTransport setBaseUrl(String baseUrl) {
        if (this.transport != null) {
            throw new IllegalStateException("Transport was already created.");
        }
        this.baseUrl = baseUrl;
        return this;
    }

    @Override
    public ClientGoogleApiRequestTransport setAccessToken(String accessToken) {
        if (this.transport != null) {
            throw new IllegalStateException("Transport was already created.");
        }
        return this;
    }

    @Override
    public ClientGoogleApiRequestTransport setApiAccessKey(String apiAccessKey) {
        if (this.transport != null) {
            throw new IllegalStateException("Transport was already created.");
        }
        this.apiKey = apiAccessKey;
        return this;
    }

    @Override
    public ClientGoogleApiRequestTransport setApplicationName(String applicationName) {
        if (this.transport != null) {
            throw new IllegalStateException("Transport was already created.");
        }
        this.applicationName = applicationName;
        return this;
    }
}
