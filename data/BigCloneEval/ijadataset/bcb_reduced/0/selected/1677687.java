package org.ironrhino.core.remoting;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.entity.StringEntity;
import org.ironrhino.core.security.util.Blowfish;
import org.ironrhino.core.util.AppInfo;
import org.ironrhino.core.util.HttpClientUtils;
import org.ironrhino.core.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.remoting.support.RemoteInvocationBasedAccessor;

public class JsonCallClient extends RemoteInvocationBasedAccessor implements MethodInterceptor, FactoryBean<Object> {

    private static Logger log = LoggerFactory.getLogger(JsonCallClient.class);

    private ServiceRegistry serviceRegistry;

    private ExecutorService executorService;

    private String host;

    private int port = 8080;

    private String contextPath;

    private String version;

    private int maxRetryTimes = 3;

    private List<String> asyncMethods;

    private boolean urlFromDiscovery;

    private boolean discovered;

    private boolean poll;

    public void setPoll(boolean poll) {
        this.poll = poll;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public void setMaxRetryTimes(int maxRetryTimes) {
        this.maxRetryTimes = maxRetryTimes;
    }

    public void setAsyncMethods(String asyncMethods) {
        if (StringUtils.isNotBlank(asyncMethods)) {
            asyncMethods = asyncMethods.trim();
            String[] array = asyncMethods.split(",");
            this.asyncMethods = Arrays.asList(array);
        }
    }

    public void setServiceRegistry(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void afterPropertiesSet() {
        String serviceUrl = getServiceUrl();
        if (serviceUrl == null) {
            setServiceUrl("http://fakeurl/");
            discovered = false;
            urlFromDiscovery = true;
        }
        if (getServiceInterface() == null) {
            throw new IllegalArgumentException("Property 'serviceInterface' is required");
        }
        this.serviceProxy = new ProxyFactory(getServiceInterface(), this).getProxy(getBeanClassLoader());
    }

    @Override
    public Object invoke(final MethodInvocation invocation) throws Throwable {
        if (!discovered) {
            setServiceUrl(discoverServiceUrl(getServiceInterface().getName()));
            discovered = true;
        } else if (poll) setServiceUrl(discoverServiceUrl(getServiceInterface().getName()));
        if (executorService != null && asyncMethods != null) {
            String name = invocation.getMethod().getName();
            if (asyncMethods.contains(name)) {
                executorService.execute(new Runnable() {

                    public void run() {
                        try {
                            invoke(invocation, maxRetryTimes);
                        } catch (Throwable e) {
                            log.error(e.getMessage(), e);
                        }
                    }
                });
                return null;
            }
        }
        return invoke(invocation, maxRetryTimes);
    }

    public Object invoke(MethodInvocation invocation, int retryTimes) throws Throwable {
        retryTimes--;
        try {
            String url = getServiceUrl() + "/" + invocation.getMethod().getName();
            HttpPost postMethod = new HttpPost(url);
            if (invocation.getMethod().getParameterTypes().length > 0) postMethod.setEntity(new StringEntity(JsonUtils.toJson(invocation.getArguments())));
            HttpResponse rsp = HttpClientUtils.getDefaultInstance().execute(postMethod);
            StatusLine sl = rsp.getStatusLine();
            if (sl.getStatusCode() >= 300) {
                throw new RuntimeException("Did not receive successful HTTP response: status code = " + sl.getStatusCode() + ", status message = [" + sl.getReasonPhrase() + "]");
            }
            HttpEntity entity = rsp.getEntity();
            StringBuilder sb = new StringBuilder();
            InputStream is = entity.getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
            String line;
            while ((line = reader.readLine()) != null) sb.append(line).append("\n");
            reader.close();
            is.close();
            String responseBody = null;
            if (sb.length() > 0) {
                sb.deleteCharAt(sb.length() - 1);
                responseBody = sb.toString();
            }
            Type t = invocation.getMethod().getGenericReturnType();
            if (t.equals(Void.class) || responseBody == null) return null;
            return JsonUtils.fromJson(responseBody, t);
        } catch (ConnectTimeoutException e) {
            if (retryTimes < 0) throw e;
            if (urlFromDiscovery) {
                String serviceUrl = discoverServiceUrl(getServiceInterface().getName());
                if (!serviceUrl.equals(getServiceUrl())) {
                    setServiceUrl(serviceUrl);
                    log.info("relocate service url:" + serviceUrl);
                }
            }
            return invoke(invocation, retryTimes);
        }
    }

    protected String discoverServiceUrl(String serviceName) {
        StringBuilder sb = new StringBuilder();
        sb.append("http://");
        if (StringUtils.isBlank(host)) {
            if (serviceRegistry != null) {
                String ho = serviceRegistry.discover(serviceName);
                if (ho != null) {
                    sb.append(ho);
                    log.info("discovered " + serviceName + "@" + ho);
                } else {
                    sb.append("localhost");
                    log.error("couldn't discover service:" + serviceName);
                }
            } else {
                sb.append("localhost");
            }
        } else {
            sb.append(host);
        }
        if (port != 80) {
            sb.append(':');
            sb.append(port);
        }
        if (StringUtils.isNotBlank(contextPath)) sb.append(contextPath);
        sb.append("/remoting/jsoncall/");
        sb.append(serviceName);
        boolean first = true;
        if (StringUtils.isNotBlank(version)) {
            if (first) {
                sb.append('?');
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(Context.VERSION);
            sb.append('=');
            sb.append(version);
        }
        if (AppInfo.getStage() == AppInfo.Stage.PRODUCTION && port == 80) try {
            if (first) {
                sb.append('?');
                first = false;
            } else {
                sb.append('&');
            }
            sb.append(Context.KEY);
            sb.append('=');
            sb.append(URLEncoder.encode(Blowfish.encrypt(serviceName), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }

    private Object serviceProxy;

    public Object getObject() {
        return this.serviceProxy;
    }

    public Class<?> getObjectType() {
        return getServiceInterface();
    }

    public boolean isSingleton() {
        return true;
    }
}
