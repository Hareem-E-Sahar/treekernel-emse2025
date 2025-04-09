package org.nightlabs.nightlybuild;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.tools.ant.Location;
import org.nightlabs.nightlybuild.tools.Logger;
import org.nightlabs.util.Utils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNStatus;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

@SuppressWarnings("unused")
public class ErrorOriginator {

    private static Pattern errorPattern = Pattern.compile("^([^:\\n]+):(\\d+):\\s*(.*)$", Pattern.MULTILINE);

    private static Pattern additionalInformationPattern = Pattern.compile("^.*?$\\s*\\^\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

    private String authorHint;

    private String author;

    private String eMail;

    private ErrorOriginator(String authorHint) {
        this.authorHint = authorHint;
    }

    public static Map<JavacError, Collection<ErrorOriginator>> getOriginators(Throwable e) {
        Collection<JavacError> errors = getJavacErrorsInExistingFiles(e);
        Map<JavacError, Collection<ErrorOriginator>> originators = new HashMap<JavacError, Collection<ErrorOriginator>>();
        for (JavacError error : errors) {
            originators.put(error, getOriginators(error.getFile()));
        }
        return originators;
    }

    private static Collection<ErrorOriginator> getOriginators(File file) {
        Collection<ErrorOriginator> originators = new ArrayList<ErrorOriginator>();
        try {
            DefaultSVNOptions options = SVNWCUtil.createDefaultOptions(true);
            SVNClientManager svnClientManager = SVNClientManager.newInstance(options, "user", "password");
            SVNStatus status;
            status = svnClientManager.getStatusClient().doStatus(file, false);
            String author = status.getAuthor();
            if (author != null) {
                Logger.debug("ErrorOriginator: Found svn author for error file: " + author);
                originators.add(new ErrorOriginator(author));
            }
        } catch (SVNException e) {
            Logger.error("ErrorOriginator: Error getting svn status", e);
        }
        return originators;
    }

    protected static String getAntErrorFilename(Throwable e) {
        org.apache.tools.ant.BuildException be = findAntBuildException(e);
        if (be != null && be.getLocation() != null && be.getLocation() != Location.UNKNOWN_LOCATION) {
            return be.getLocation().getFileName();
        }
        return null;
    }

    protected static Collection<JavacError> getJavacErrorsInExistingFiles(Throwable e) {
        BuildException be = findBuildException(e);
        if (be == null) return Collections.emptyList();
        String errorLog = be.getErrorLog();
        if (errorLog == null) {
            Logger.warning("ErrorOriginator: findBuildException(e).getErrorLog() returned null!", e);
            return Collections.emptyList();
        }
        Collection<JavacError> javacErrors = getJavacErrors(errorLog);
        for (Iterator<JavacError> iterator = javacErrors.iterator(); iterator.hasNext(); ) {
            JavacError javacError = iterator.next();
            if (!javacError.getFile().exists()) {
                Logger.warning("ErrorOriginator: Found error file in javac log, but file does not exist: " + javacError.getFile());
                iterator.remove();
            }
        }
        return javacErrors;
    }

    protected static String findJavacOutputs(String fullOutput) {
        if (fullOutput == null) return "";
        StringBuilder javacOutputs = new StringBuilder();
        int findOffset = 0;
        Pattern javacPattern = Pattern.compile("^\\s*\\[javac\\]", Pattern.MULTILINE);
        Matcher m = javacPattern.matcher(fullOutput);
        while (m.find(findOffset)) {
            int javacBegin = m.end();
            int javacEnd = fullOutput.length();
            Pattern taskPattern = Pattern.compile("^\\s*\\[[^\\]]+\\]", Pattern.MULTILINE);
            Matcher m2 = taskPattern.matcher(fullOutput);
            if (m2.find(javacBegin)) {
                javacEnd = m2.start();
            }
            javacOutputs.append(fullOutput.substring(javacBegin, javacEnd));
            if (javacEnd == fullOutput.length()) break;
            findOffset = javacEnd;
        }
        return javacOutputs.toString();
    }

    public static Collection<JavacError> getJavacErrors(String fullOutput) {
        String javacLog = findJavacOutputs(fullOutput);
        Collection<JavacError> errors = new ArrayList<JavacError>();
        Matcher m = errorPattern.matcher(javacLog);
        while (m.find()) {
            File f = new File(m.group(1));
            if (!f.getName().endsWith(".java")) {
                continue;
            }
            JavacError javacError = new JavacError(f, Integer.parseInt(m.group(2)), m.group(3));
            Matcher m2 = additionalInformationPattern.matcher(javacLog);
            if (m2.find(m.end()) && m2.start() - m.end() <= 2) {
                javacError.setAdditionalInformation(m2.group().trim());
            }
            errors.add(javacError);
        }
        return errors;
    }

    protected static BuildException findBuildException(Throwable e) {
        return (BuildException) findException(BuildException.class, e);
    }

    protected static org.apache.tools.ant.BuildException findAntBuildException(Throwable e) {
        return (org.apache.tools.ant.BuildException) findException(BuildException.class, e);
    }

    @SuppressWarnings("unchecked")
    protected static Throwable findException(Class exceptionType, Throwable e) {
        if (exceptionType.isAssignableFrom(e.getClass())) return e;
        if (e.getCause() != null) return findException(exceptionType, e.getCause());
        return null;
    }

    @Override
    public String toString() {
        return Utils.toString(this);
    }
}
