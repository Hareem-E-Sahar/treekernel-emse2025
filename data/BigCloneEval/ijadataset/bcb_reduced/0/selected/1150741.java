package com.threerings.s3.client;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * An exception that indicates a generic S3 error.
 */
public class S3ServerException extends S3Exception {

    public S3ServerException(String message) {
        this(message, null, null);
    }

    public S3ServerException(String message, Exception cause) {
        super(message, cause);
    }

    public S3ServerException(String message, boolean isTransient) {
        this(message, null, null, isTransient);
    }

    /**
     * Initialize an AWS exception with the associated message, AWS S3 requestId,
     * and AWS S3 hostId.
     * @param message Error message provided by S3.
     * @param requestId Request ID provided by S3.
     * @param hostId Host ID provided by S3;
     */
    public S3ServerException(String message, String requestId, String hostId) {
        super(message);
        _requestId = requestId;
        _hostId = hostId;
    }

    /**
     * Initialize an AWS exception with the associated message, AWS S3 requestId,
     * and AWS S3 hostId.
     * @param message Error message provided by S3.
     * @param requestId Request ID provided by S3.
     * @param hostId Host ID provided by S3;
     * @param isTransient If true, this exception represents a transient error
     */
    public S3ServerException(String message, String requestId, String hostId, boolean isTransient) {
        super(message, null, isTransient);
        _requestId = requestId;
        _hostId = hostId;
    }

    /**
     * Extract the child node's text.
     * @param node Parent node.
     */
    private static String _extractXmlChildText(Node node) {
        Node textNode = node.getFirstChild();
        if (textNode == null) return null;
        return textNode.getNodeValue();
    }

    /**
     * Attempts to instantiate an exception from this class for the given error code. This could
     * use a static mapping, but exceptions are rare conditions and there's enough ugly generated
     * code in here as it is.
     */
    private static S3ServerException _exceptionForS3Error(String code, String errorMessage, String requestId, String hostId) {
        try {
            Constructor<? extends S3ServerException> construct;
            Class<? extends S3ServerException> cls;
            final Class<?> loadedClass;
            loadedClass = Class.forName("com.threerings.s3.client.S3ServerException$" + code + "Exception");
            cls = loadedClass.asSubclass(S3ServerException.class);
            construct = cls.getConstructor(String.class, String.class, String.class);
            return construct.newInstance(errorMessage, requestId, hostId);
        } catch (Exception e) {
            return new S3ServerException("An unhandled S3 error code was returned: " + code, requestId, hostId);
        }
    }

    private static S3ServerException _createGenericException(String reason, byte[] document, Exception cause) {
        try {
            return new S3ServerException(reason + ": " + new String(document, "UTF-8"), cause);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 not available?", e);
        }
    }

    /**
     * Convert an S3 XML error document into a S3ServerException instance.
     * @param documentString A string containing the XML error document.
     */
    public static S3ServerException exceptionForS3Error(byte[] document) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document doc;
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            doc = db.parse(new InputSource(new ByteArrayInputStream(document)));
        } catch (ParserConfigurationException e) {
            return _createGenericException("Error parsing S3 error document", document, e);
        } catch (SAXException e) {
            return _createGenericException("Error parsing S3 error document", document, e);
        } catch (IOException e) {
            return _createGenericException("I/O error parsing S3 error document", document, e);
        }
        Node node;
        String code = null;
        String errorMessage = null;
        String requestId = null;
        String hostId = null;
        for (node = doc.getDocumentElement().getFirstChild(); node != null; node = node.getNextSibling()) {
            if (node.getNodeName().toLowerCase().equals("code")) {
                code = _extractXmlChildText(node);
                continue;
            }
            if (node.getNodeName().toLowerCase().equals("message")) {
                errorMessage = _extractXmlChildText(node);
                continue;
            }
            if (node.getNodeName().toLowerCase().equals("requestid")) {
                requestId = _extractXmlChildText(node);
                continue;
            }
            if (node.getNodeName().toLowerCase().equals("hostid")) {
                hostId = _extractXmlChildText(node);
                continue;
            }
        }
        return _exceptionForS3Error(code, errorMessage, requestId, hostId);
    }

    /**
     * Creates an S3ServerException for the given HTTP status code. This should only be used if
     * there isn't a response body to be passed to {@link #exceptionForS3Error(String)}.
     *
     * @param message - the message to be included in the exception
     */
    public static S3ServerException exceptionForS3ErrorCode(int statusCode, String message) {
        return _exceptionForS3Error("S3Server" + statusCode, message, null, null);
    }

    /** Get the Amazon S3 request ID. */
    public String getRequestId() {
        return _requestId;
    }

    /** Get the Amazon S3 host ID. */
    public String getHostId() {
        return _hostId;
    }

    /** Amazon S3 Request ID */
    private String _requestId;

    /** Amazon S3 Host ID */
    private String _hostId;

    /** Access Denied  */
    public static class AccessDeniedException extends S3Server403Exception {

        public AccessDeniedException(String message) {
            this(message, null, null);
        }

        public AccessDeniedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** There is a problem with your AWS account that prevents the operation from completing successfully. Please use <a class="ulink" href="http://aws.amazon.com/contact-us/" target="_blank">Contact Us</a>.  */
    public static class AccountProblemException extends S3Server403Exception {

        public AccountProblemException(String message) {
            this(message, null, null);
        }

        public AccountProblemException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The e-mail address you provided is associated with more than one account.  */
    public static class AmbiguousGrantByEmailAddressException extends S3Server400Exception {

        public AmbiguousGrantByEmailAddressException(String message) {
            this(message, null, null);
        }

        public AmbiguousGrantByEmailAddressException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The Content-MD5 you specified did not match what we received.  */
    public static class BadDigestException extends S3Server400Exception {

        public BadDigestException(String message) {
            this(message, null, null);
        }

        public BadDigestException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The requested bucket name is not available. The bucket namespace is shared by all users of the system. Please select a different name and try again.  */
    public static class BucketAlreadyExistsException extends S3Server409Exception {

        public BucketAlreadyExistsException(String message) {
            this(message, null, null);
        }

        public BucketAlreadyExistsException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your previous request to create the named bucket succeeded and you already own it.  */
    public static class BucketAlreadyOwnedByYouException extends S3Server409Exception {

        public BucketAlreadyOwnedByYouException(String message) {
            this(message, null, null);
        }

        public BucketAlreadyOwnedByYouException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The bucket you tried to delete is not empty.  */
    public static class BucketNotEmptyException extends S3Server409Exception {

        public BucketNotEmptyException(String message) {
            this(message, null, null);
        }

        public BucketNotEmptyException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** This request does not support credentials.  */
    public static class CredentialsNotSupportedException extends S3Server400Exception {

        public CredentialsNotSupportedException(String message) {
            this(message, null, null);
        }

        public CredentialsNotSupportedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Cross location logging not allowed. Buckets in one geographic location cannot log information to a bucket in another location.  */
    public static class CrossLocationLoggingProhibitedException extends S3Server403Exception {

        public CrossLocationLoggingProhibitedException(String message) {
            this(message, null, null);
        }

        public CrossLocationLoggingProhibitedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your proposed upload is smaller than the minimum allowed object size.  */
    public static class EntityTooSmallException extends S3Server400Exception {

        public EntityTooSmallException(String message) {
            this(message, null, null);
        }

        public EntityTooSmallException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your proposed upload exceeds the maximum allowed object size.  */
    public static class EntityTooLargeException extends S3Server400Exception {

        public EntityTooLargeException(String message) {
            this(message, null, null);
        }

        public EntityTooLargeException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The provided token has expired.  */
    public static class ExpiredTokenException extends S3Server400Exception {

        public ExpiredTokenException(String message) {
            this(message, null, null);
        }

        public ExpiredTokenException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** You did not provide the number of bytes specified by the Content-Length HTTP header  */
    public static class IncompleteBodyException extends S3Server400Exception {

        public IncompleteBodyException(String message) {
            this(message, null, null);
        }

        public IncompleteBodyException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** POST requires exactly one file upload per request.  */
    public static class IncorrectNumberOfFilesInPostRequestException extends S3Server400Exception {

        public IncorrectNumberOfFilesInPostRequestException(String message) {
            this(message, null, null);
        }

        public IncorrectNumberOfFilesInPostRequestException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Inline data exceeds the maximum allowed size.  */
    public static class InlineDataTooLargeException extends S3Server400Exception {

        public InlineDataTooLargeException(String message) {
            this(message, null, null);
        }

        public InlineDataTooLargeException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** We encountered an internal error. Please try again.  */
    public static class InternalErrorException extends S3Server500Exception {

        public InternalErrorException(String message) {
            this(message, null, null);
        }

        public InternalErrorException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The AWS Access Key Id you provided does not exist in our records.  */
    public static class InvalidAccessKeyIdException extends S3Server403Exception {

        public InvalidAccessKeyIdException(String message) {
            this(message, null, null);
        }

        public InvalidAccessKeyIdException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** You must specify the Anonymous role.  */
    public static class InvalidAddressingHeaderException extends S3ServerException {

        public InvalidAddressingHeaderException(String message) {
            this(message, null, null);
        }

        public InvalidAddressingHeaderException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Invalid Argument  */
    public static class InvalidArgumentException extends S3Server400Exception {

        public InvalidArgumentException(String message) {
            this(message, null, null);
        }

        public InvalidArgumentException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The specified bucket is not valid.  */
    public static class InvalidBucketNameException extends S3Server400Exception {

        public InvalidBucketNameException(String message) {
            this(message, null, null);
        }

        public InvalidBucketNameException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The Content-MD5 you specified was an invalid.  */
    public static class InvalidDigestException extends S3Server400Exception {

        public InvalidDigestException(String message) {
            this(message, null, null);
        }

        public InvalidDigestException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The specified location constraint is not valid. For more information about Regions, see <a class="ulink" href="http://docs.amazonwebservices.com/AmazonS3/2006-03-01/dev/index.html?LocationSelection.html" target="_blank">How to Select a Region for Your Buckets</a>.   */
    public static class InvalidLocationConstraintException extends S3Server400Exception {

        public InvalidLocationConstraintException(String message) {
            this(message, null, null);
        }

        public InvalidLocationConstraintException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** All access to this object has been disabled.  */
    public static class InvalidPayerException extends S3Server403Exception {

        public InvalidPayerException(String message) {
            this(message, null, null);
        }

        public InvalidPayerException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The content of the form does not meet the conditions specified in the policy document.  */
    public static class InvalidPolicyDocumentException extends S3Server400Exception {

        public InvalidPolicyDocumentException(String message) {
            this(message, null, null);
        }

        public InvalidPolicyDocumentException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The requested range cannot be satisfied.  */
    public static class InvalidRangeException extends S3Server416Exception {

        public InvalidRangeException(String message) {
            this(message, null, null);
        }

        public InvalidRangeException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The provided security credentials are not valid.  */
    public static class InvalidSecurityException extends S3Server403Exception {

        public InvalidSecurityException(String message) {
            this(message, null, null);
        }

        public InvalidSecurityException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The SOAP request body is invalid.  */
    public static class InvalidSOAPRequestException extends S3Server400Exception {

        public InvalidSOAPRequestException(String message) {
            this(message, null, null);
        }

        public InvalidSOAPRequestException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The storage class you specified is not valid.  */
    public static class InvalidStorageClassException extends S3Server400Exception {

        public InvalidStorageClassException(String message) {
            this(message, null, null);
        }

        public InvalidStorageClassException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The target bucket for logging does not exist, is not owned by you, or does not have the appropriate grants for the log-delivery group.   */
    public static class InvalidTargetBucketForLoggingException extends S3Server400Exception {

        public InvalidTargetBucketForLoggingException(String message) {
            this(message, null, null);
        }

        public InvalidTargetBucketForLoggingException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The provided token is malformed or otherwise invalid.  */
    public static class InvalidTokenException extends S3Server400Exception {

        public InvalidTokenException(String message) {
            this(message, null, null);
        }

        public InvalidTokenException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Couldn't parse the specified URI.  */
    public static class InvalidURIException extends S3Server400Exception {

        public InvalidURIException(String message) {
            this(message, null, null);
        }

        public InvalidURIException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your key is too long.  */
    public static class KeyTooLongException extends S3Server400Exception {

        public KeyTooLongException(String message) {
            this(message, null, null);
        }

        public KeyTooLongException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The XML you provided was not well-formed or did not validate against our published schema.  */
    public static class MalformedACLErrorException extends S3Server400Exception {

        public MalformedACLErrorException(String message) {
            this(message, null, null);
        }

        public MalformedACLErrorException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The body of your POST request is not well-formed multipart/form-data.  */
    public static class MalformedPOSTRequestException extends S3Server400Exception {

        public MalformedPOSTRequestException(String message) {
            this(message, null, null);
        }

        public MalformedPOSTRequestException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** This happens when the user sends a malformed xml (xml that doesn't conform to the published xsd) for the configuration. The error message is, "The XML you provided was not well-formed or did not validate against our published schema."   */
    public static class MalformedXMLException extends S3Server400Exception {

        public MalformedXMLException(String message) {
            this(message, null, null);
        }

        public MalformedXMLException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your request was too big.  */
    public static class MaxMessageLengthExceededException extends S3Server400Exception {

        public MaxMessageLengthExceededException(String message) {
            this(message, null, null);
        }

        public MaxMessageLengthExceededException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your POST request fields preceding the upload file were too large.  */
    public static class MaxPostPreDataLengthExceededErrorException extends S3Server400Exception {

        public MaxPostPreDataLengthExceededErrorException(String message) {
            this(message, null, null);
        }

        public MaxPostPreDataLengthExceededErrorException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your metadata headers exceed the maximum allowed metadata size.  */
    public static class MetadataTooLargeException extends S3Server400Exception {

        public MetadataTooLargeException(String message) {
            this(message, null, null);
        }

        public MetadataTooLargeException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The specified method is not allowed against this resource.  */
    public static class MethodNotAllowedException extends S3Server405Exception {

        public MethodNotAllowedException(String message) {
            this(message, null, null);
        }

        public MethodNotAllowedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** A SOAP attachment was expected, but none were found.  */
    public static class MissingAttachmentException extends S3ServerException {

        public MissingAttachmentException(String message) {
            this(message, null, null);
        }

        public MissingAttachmentException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** You must provide the Content-Length HTTP header.  */
    public static class MissingContentLengthException extends S3Server411Exception {

        public MissingContentLengthException(String message) {
            this(message, null, null);
        }

        public MissingContentLengthException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** This happens when the user sends an empty xml document as a request. The error message is, "Request body is empty."   */
    public static class MissingRequestBodyErrorException extends S3Server400Exception {

        public MissingRequestBodyErrorException(String message) {
            this(message, null, null);
        }

        public MissingRequestBodyErrorException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The SOAP 1.1 request is missing a security element.  */
    public static class MissingSecurityElementException extends S3Server400Exception {

        public MissingSecurityElementException(String message) {
            this(message, null, null);
        }

        public MissingSecurityElementException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your request was missing a required header.  */
    public static class MissingSecurityHeaderException extends S3Server400Exception {

        public MissingSecurityHeaderException(String message) {
            this(message, null, null);
        }

        public MissingSecurityHeaderException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** There is no such thing as a logging status sub-resource for a key.  */
    public static class NoLoggingStatusForKeyException extends S3Server400Exception {

        public NoLoggingStatusForKeyException(String message) {
            this(message, null, null);
        }

        public NoLoggingStatusForKeyException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The specified bucket does not exist.  */
    public static class NoSuchBucketException extends S3Server404Exception {

        public NoSuchBucketException(String message) {
            this(message, null, null);
        }

        public NoSuchBucketException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The specified key does not exist.  */
    public static class NoSuchKeyException extends S3Server404Exception {

        public NoSuchKeyException(String message) {
            this(message, null, null);
        }

        public NoSuchKeyException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** A header you provided implies functionality that is not implemented.  */
    public static class NotImplementedException extends S3Server501Exception {

        public NotImplementedException(String message) {
            this(message, null, null);
        }

        public NotImplementedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your account is not signed up for the Amazon&nbsp;<acronym class="acronym">S3</acronym> service. You must sign up before you can use Amazon&nbsp;<acronym class="acronym">S3</acronym>. You can sign up at the following URL: http://aws.amazon.com/s3  */
    public static class NotSignedUpException extends S3Server403Exception {

        public NotSignedUpException(String message) {
            this(message, null, null);
        }

        public NotSignedUpException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** A conflicting conditional operation is currently in progress against this resource. Please try again.  */
    public static class OperationAbortedException extends S3Server409Exception {

        public OperationAbortedException(String message) {
            this(message, null, null);
        }

        public OperationAbortedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The bucket you are attempting to access must be addressed using the specified endpoint. Please send all future requests to this endpoint.  */
    public static class PermanentRedirectException extends S3Server301Exception {

        public PermanentRedirectException(String message) {
            this(message, null, null);
        }

        public PermanentRedirectException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** At least one of the pre-conditions you specified did not hold.  */
    public static class PreconditionFailedException extends S3Server412Exception {

        public PreconditionFailedException(String message) {
            this(message, null, null);
        }

        public PreconditionFailedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Temporary redirect.  */
    public static class RedirectException extends S3Server307Exception {

        public RedirectException(String message) {
            this(message, null, null);
        }

        public RedirectException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Bucket POST must be of the enclosure-type multipart/form-data.  */
    public static class RequestIsNotMultiPartContentException extends S3Server400Exception {

        public RequestIsNotMultiPartContentException(String message) {
            this(message, null, null);
        }

        public RequestIsNotMultiPartContentException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Your socket connection to the server was not read from or written to within the timeout period.  */
    public static class RequestTimeoutException extends S3Server400Exception {

        public RequestTimeoutException(String message) {
            this(message, null, null);
        }

        public RequestTimeoutException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The difference between the request time and the server's time is too large.  */
    public static class RequestTimeTooSkewedException extends S3Server403Exception {

        public RequestTimeTooSkewedException(String message) {
            this(message, null, null);
        }

        public RequestTimeTooSkewedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Requesting the torrent file of a bucket is not permitted.  */
    public static class RequestTorrentOfBucketErrorException extends S3Server400Exception {

        public RequestTorrentOfBucketErrorException(String message) {
            this(message, null, null);
        }

        public RequestTorrentOfBucketErrorException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The request signature we calculated does not match the signature you provided. Check your AWS Secret Access Key and signing method. For more information, see <a class="ulink" href="http://docs.amazonwebservices.com/AmazonS3/2006-03-01/dev/index.html?RESTAuthentication.html" target="_blank">REST Authentication</a> and <a class="ulink" href="http://docs.amazonwebservices.com/AmazonS3/2006-03-01/dev/index.html?SOAPAuthentication.html" target="_blank">SOAP Authentication</a> for details.  */
    public static class SignatureDoesNotMatchException extends S3Server403Exception {

        public SignatureDoesNotMatchException(String message) {
            this(message, null, null);
        }

        public SignatureDoesNotMatchException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** Please reduce your request rate.  */
    public static class SlowDownException extends S3Server503Exception {

        public SlowDownException(String message) {
            this(message, null, null);
        }

        public SlowDownException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** You are being redirected to the bucket while DNS updates.  */
    public static class TemporaryRedirectException extends S3Server307Exception {

        public TemporaryRedirectException(String message) {
            this(message, null, null);
        }

        public TemporaryRedirectException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The provided token must be refreshed.  */
    public static class TokenRefreshRequiredException extends S3Server400Exception {

        public TokenRefreshRequiredException(String message) {
            this(message, null, null);
        }

        public TokenRefreshRequiredException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** You have attempted to create more buckets than allowed.  */
    public static class TooManyBucketsException extends S3Server400Exception {

        public TooManyBucketsException(String message) {
            this(message, null, null);
        }

        public TooManyBucketsException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** This request does not support content.  */
    public static class UnexpectedContentException extends S3Server400Exception {

        public UnexpectedContentException(String message) {
            this(message, null, null);
        }

        public UnexpectedContentException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The e-mail address you provided does not match any account on record.  */
    public static class UnresolvableGrantByEmailAddressException extends S3Server400Exception {

        public UnresolvableGrantByEmailAddressException(String message) {
            this(message, null, null);
        }

        public UnresolvableGrantByEmailAddressException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** The bucket POST must contain the specified field name. If it is specified, please check the order of the fields.  */
    public static class UserKeyMustBeSpecifiedException extends S3Server400Exception {

        public UserKeyMustBeSpecifiedException(String message) {
            this(message, null, null);
        }

        public UserKeyMustBeSpecifiedException(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 301, which means Moved Permanently  */
    public static class S3Server301Exception extends S3ServerException {

        public S3Server301Exception(String message) {
            this(message, null, null);
        }

        public S3Server301Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 307, which means Temporary Redirect  */
    public static class S3Server307Exception extends S3ServerException {

        public S3Server307Exception(String message) {
            this(message, null, null);
        }

        public S3Server307Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 400, which means Bad Request  */
    public static class S3Server400Exception extends S3ServerException {

        public S3Server400Exception(String message) {
            this(message, null, null);
        }

        public S3Server400Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 403, which means Forbidden  */
    public static class S3Server403Exception extends S3ServerException {

        public S3Server403Exception(String message) {
            this(message, null, null);
        }

        public S3Server403Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 404, which means Not Found  */
    public static class S3Server404Exception extends S3ServerException {

        public S3Server404Exception(String message) {
            this(message, null, null);
        }

        public S3Server404Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 405, which means Method Not Allowed  */
    public static class S3Server405Exception extends S3ServerException {

        public S3Server405Exception(String message) {
            this(message, null, null);
        }

        public S3Server405Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 409, which means Conflict  */
    public static class S3Server409Exception extends S3ServerException {

        public S3Server409Exception(String message) {
            this(message, null, null);
        }

        public S3Server409Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 411, which means Length Required  */
    public static class S3Server411Exception extends S3ServerException {

        public S3Server411Exception(String message) {
            this(message, null, null);
        }

        public S3Server411Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 412, which means Precondition Failed  */
    public static class S3Server412Exception extends S3ServerException {

        public S3Server412Exception(String message) {
            this(message, null, null);
        }

        public S3Server412Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 416, which means Requested Range Not Satisfiable  */
    public static class S3Server416Exception extends S3ServerException {

        public S3Server416Exception(String message) {
            this(message, null, null);
        }

        public S3Server416Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 500, which means Internal Server Error  */
    public static class S3Server500Exception extends S3ServerException {

        public S3Server500Exception(String message) {
            this(message, null, null);
        }

        public S3Server500Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId, true);
        }
    }

    /** S3 returned a status code 501, which means Not Implemented  */
    public static class S3Server501Exception extends S3ServerException {

        public S3Server501Exception(String message) {
            this(message, null, null);
        }

        public S3Server501Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }

    /** S3 returned a status code 503, which means Service Unavailable  */
    public static class S3Server503Exception extends S3ServerException {

        public S3Server503Exception(String message) {
            this(message, null, null);
        }

        public S3Server503Exception(String message, String requestId, String hostId) {
            super(message, requestId, hostId);
        }
    }
}
