package com.googlecode.batchfb.impl;

import java.lang.reflect.Constructor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;
import com.googlecode.batchfb.Batcher;
import com.googlecode.batchfb.Later;
import com.googlecode.batchfb.err.FacebookException;
import com.googlecode.batchfb.err.OAuthException;
import com.googlecode.batchfb.err.PageMigratedException;
import com.googlecode.batchfb.err.PermissionException;
import com.googlecode.batchfb.err.QueryParseException;
import com.googlecode.batchfb.util.LaterWrapper;

/**
 * <p>Detects a Facebook error in the JSON result from a Graph API request and throws
 * the correct kind of exception, whatever that happens to be.  It tries to match the
 * type of the exception with an actual exception class of the correct name.</p>
 * 
 * <p>In addition, this detects the REALLY WEIRD cases where Facebook just spits back
 * "false".  We translate that into a null.</p>
 * 
 * <p>If there was no error, this wrapper just passes through normally.</p>
 * 
 * <p>Facebook coughs up errors in at least three different formats.  This
 * detects them all.</p>
 */
public class ErrorDetectingWrapper extends LaterWrapper<JsonNode, JsonNode> {

    public ErrorDetectingWrapper(Later<JsonNode> orig) {
        super(orig);
    }

    /** */
    @Override
    protected JsonNode convert(JsonNode node) {
        if (node == null || node.isBoolean() && !node.booleanValue()) return null;
        this.checkForStandardGraphError(node);
        this.checkForBatchError(node);
        this.checkForOldRestStyleError(node);
        return node;
    }

    /**
	 * The basic graph error looks like this:
<pre>
{
  error: {
    type: "OAuthException"
    message: "Error validating application."
  }
}
</pre>
	 */
    protected void checkForStandardGraphError(JsonNode node) {
        JsonNode errorNode = node.get("error");
        if (errorNode != null) {
            String type = errorNode.path("type").asText();
            if (type == null) return;
            String msg = errorNode.path("message").asText();
            if (msg == null) return;
            if (msg.startsWith("(#200)")) throw new PermissionException(msg);
            if (msg.startsWith("(#21)")) this.throwPageMigratedException(msg);
            String proposedExceptionType = Batcher.class.getPackage().getName() + ".err." + type;
            try {
                Class<?> exceptionClass = Class.forName(proposedExceptionType);
                Constructor<?> ctor = exceptionClass.getConstructor(String.class);
                throw (FacebookException) ctor.newInstance(msg);
            } catch (FacebookException e) {
                throw e;
            } catch (Exception e) {
                throw new FacebookException(type + ": " + msg);
            }
        }
    }

    /** Matches IDs in the error msg */
    private static final Pattern ID_PATTERN = Pattern.compile("ID [0-9]+");

    /**
	 * Builds the proper exception and throws it.
	 * @throws PageMigratedException always
	 */
    private void throwPageMigratedException(String msg) {
        Matcher matcher = ID_PATTERN.matcher(msg);
        long oldId = this.extractNextId(matcher, msg);
        long newId = this.extractNextId(matcher, msg);
        throw new PageMigratedException(msg, oldId, newId);
    }

    /**
	 * Gets the next id out of the matcher
	 */
    private long extractNextId(Matcher matcher, String msg) {
        if (!matcher.find()) throw new IllegalStateException("Facebook changed the error msg for page migration to something unfamiliar. The new msg is: " + msg);
        String idStr = matcher.group().substring("ID ".length());
        return Long.parseLong(idStr);
    }

    /**
	 * The batch call itself seems to have a funky error format:
	 * 
	 * {"error":190,"error_description":"Invalid OAuth access token signature."}
	 */
    protected void checkForBatchError(JsonNode root) {
        JsonNode errorCode = root.get("error");
        if (errorCode != null) {
            JsonNode errorDescription = root.get("error_description");
            if (errorDescription != null) {
                int code = errorCode.intValue();
                String msg = errorDescription.asText();
                this.throwCodeAndMessage(code, msg);
            }
        }
    }

    /**
	 * Old-style calls, including multiquery, has its own wacky error format:
<pre>
{
  "error_code": 602,
  "error_msg": "bogus is not a member of the user table.",
  "request_args": [
    {
      "key": "queries",
      "value": "{"query1":"SELECT uid FROM user WHERE uid=503702723",
"query2":"SELECT uid FROM user WHERE bogus=503702723"}"
    },
    {
      "key": "method",
      "value": "fql.multiquery"
    },
    {
      "key": "access_token",
      "value": "blahblahblah"
    },
    {
      "key": "format",
      "value": "json"
    }
  ]
}
</pre>
	 *
	 * The code interpretations rely heavily on http://wiki.developers.facebook.com/index.php/Error_codes
	 */
    protected void checkForOldRestStyleError(JsonNode node) {
        JsonNode errorCode = node.get("error_code");
        if (errorCode != null) {
            int code = errorCode.intValue();
            String msg = node.path("error_msg").asText();
            this.throwCodeAndMessage(code, msg);
        }
    }

    /**
	 * Throw the appropriate exception for the given legacy code and message.
	 * Always throws, never returns.
	 */
    protected void throwCodeAndMessage(int code, String msg) {
        switch(code) {
            case 0:
            case 101:
            case 102:
            case 190:
                throw new OAuthException(msg);
            default:
                if (code >= 200 && code < 300) throw new PermissionException(msg); else if (code >= 600 && code < 700) throw new QueryParseException(msg); else throw new FacebookException(msg + " (code " + code + ")");
        }
    }
}
