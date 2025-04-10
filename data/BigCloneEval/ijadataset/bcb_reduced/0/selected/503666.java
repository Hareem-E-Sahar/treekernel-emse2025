package com.agimatec.mojo.wait;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/** @goal wait */
public class WaitMojo extends AbstractMojo {

    /** @parameter default-value="http" */
    String protocol;

    /** @parameter default-value="localhost" */
    String host;

    /** @parameter default-value="8080" */
    int port;

    /** @parameter default-value="" */
    String file;

    /** @parameter default-value="30000" */
    int timeout;

    /** @parameter default-value="0" */
    int maxcount;

    /** @parameter default-value="false" */
    boolean skip;

    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("skipped waiting for " + protocol + "://" + host);
            return;
        }
        URL url = getURL();
        int count = maxcount;
        int trials = 1;
        getLog().info("(timeout: " + timeout + " maxcount: " + maxcount + ")");
        while (true) {
            try {
                getLog().info(trials + ": Try to connect to " + url);
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(timeout);
                InputStream stream = connection.getInputStream();
                getLog().info("success - reached " + url);
                stream.close();
                break;
            } catch (IOException e) {
                if (count > 1) {
                    count--;
                } else if (count != 0) {
                    getLog().warn("cannot connect to " + url, e);
                    throw new MojoExecutionException("cannot connect to " + url, e);
                }
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e1) {
                }
                trials++;
            }
        }
    }

    public URL getURL() throws MojoExecutionException {
        try {
            return new URL(protocol, host, port, file);
        } catch (MalformedURLException e) {
            throw new MojoExecutionException(protocol + ", " + host + ", " + port + ", " + file + ": cannot create URL", e);
        }
    }
}
