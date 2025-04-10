package sexpression;

import java.io.ByteArrayInputStream;
import java.io.CharArrayReader;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import sexpression.lexer.*;
import sexpression.parser.*;
import sexpression.stream.ASEInputStreamReader;
import sexpression.stream.InvalidVerbatimStreamException;

/**
 * From <a href="http://theory.lcs.mit.edu/~rivest/sexp.html">MIT</a>,
 * "S-expressions are a data structure for representing complex data. They are a
 * variation on LISP S-expressions. (Lisp was invented by John McCarthy)."<br>
 * <br>
 * 
 * Here, my purpose is to accurately represent the notion of an S-expression in
 * a java class hierarchy.<br>
 * <br>
 * 
 * An SExpression is either a byte-string or a list of simpler SExpressions.<br>
 * <br>
 * 
 * ASExpression also has a hook method for invoking visitors. This conforms with
 * the visitor design pattern.
 * 
 * @author Kyle
 * 
 */
public abstract class ASExpression {

    /**
     * Parse a String into an expression. This method utilizes the s-expression
     * parser.<br>
     * <br>
     * NB: For now, this method constructs an array which represents the string,
     * a reader for this array, a lexer for this reader, and a parser for this
     * lexer. Because all this allocation is unwise, this might be optimized
     * later.
     * 
     * @param expression
     *            Parse this string into an s-expression. This can be any string
     *            generated by ASExpression.toString().
     * @return This method returns the s-expression which represents the string.
     * @see sexpression.parser.Parser
     */
    public static ASExpression make(String expression) {
        return new Parser(new Lexer(new CharArrayReader(expression.toCharArray()))).read();
    }

    /**
     * Parse a verbatim string into an expression. This method utilizes the
     * verbatim stream parser.
     * 
     * @param bytes
     *            Parse this stream of bytes into an expression.
     * @return This method returns the s-expression which represents the given
     *         stream of bytes.
     * @throws InvalidVerbatimStreamException
     * @throws
     * @see sexpression.stream.ASEInputStreamReader
     */
    public static ASExpression makeVerbatim(byte[] bytes) throws InvalidVerbatimStreamException {
        try {
            return new ASEInputStreamReader(new ByteArrayInputStream(bytes)).read();
        } catch (IOException e) {
            throw new RuntimeException();
        }
    }

    /**
     * Compute the sha-1 hash of a given byte array.
     * 
     * @param expression
     *            Compute the hash of this byte string.
     * @return This method returns the SHA-1 hash of the given byte array.
     */
    public static byte[] computeSHA1(byte[] expression) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA");
            md.update(expression);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 not supported on this platform");
        }
        return md.digest();
    }

    private byte[] _hash = null;

    private byte[] _verbatim = null;

    private String _string = null;

    /**
     * Treat this ASExpression as a pattern and attempt to match another
     * expression against it.
     * 
     * @param target
     *            The target expression to be matched.
     * @return If target does not match the pattern, NoMatch.SINGLETON is
     *         returned. If target matches the pattern, a ListExpression is
     *         returned. If pattern has no wildcards, the returned list is
     *         empty. If pattern contains wildcards, the returned list contains,
     *         in order of inorder traversal, the subexpressions of target that
     *         were matched by those wildcards.
     * @see sexpression.Wildcard
     * @see sexpression.ListWildcard
     * @see sexpression.StringWildcard
     */
    public abstract ASExpression match(ASExpression target);

    /**
     * In order to make the process of creating a verbatim string more
     * efficient, each type must define a representative ordered set of byte
     * arrays. Copying this into all into a single byte array will open happen
     * once per call to toVerbatim().
     * 
     * @return This method returns the verbatim byte-string representation of
     *         this ASExpression.
     * @throws IncorrectUseException
     *             This method throws if this has objects composing it that make
     *             it unable to be converted into a format that can be
     *             serialized.
     */
    public abstract ByteArrayBuffer toVerbatimHelp();

    /**
     * In order to make toString more efficient, each class of ASExpression must
     * define a toStringHelp which returns a string buffer, rather than an
     * actual string. This will make the construction of a representative string
     * much more efficient.
     * 
     * @return This method returns a string buffer which represents this
     *         expression.
     */
    public abstract StringBuffer toStringHelp();

    /**
     * Get the size of this expression. For lists, this is the number of
     * elements. For strings, this is the number of bytes. For anything else,
     * this will return 0.
     * 
     * @return This method returns the size of this expression.
     */
    public abstract int size();

    /**
     * Treat this ASExpression as a pattern and attempt to match against another
     * expression. This operation differs from match in that it only hands back
     * subexpressions which match named patterns.
     * 
     * @param target
     *            Treat "this" as a pattern and match against target.
     * @return This method returns a mapping (name->subexpression) where for
     *         every named pattern in the pattern expression with name "x",
     *         there exists a mapping x->y where "y" is the subexpression in the
     *         target that matches the named pattern x.
     * @throws IncorrectUseException
     * @see sexpression.NamedPattern
     */
    public HashMap<String, ASExpression> namedMatch(ASExpression target) {
        if (match(target) == NoMatch.SINGLETON) return NamedNoMatch.SINGLETON;
        return new HashMap<String, ASExpression>();
    }

    /**
     * Convert this s-expression into Rivest verbatim format:<br>
     * String: "[len]:[bytes]"<br>
     * List: "([elt] ...)"<br>
     * 
     * @return
     * @throws IncorrectUseException
     */
    public byte[] toVerbatim() {
        if (_verbatim == null) {
            _verbatim = toVerbatimHelp().getBytes();
        }
        return _verbatim;
    }

    /**
     * This method computes the SHA1 hash of the verbatim representation of this
     * S-Expression.
     * 
     * @return This method returns the SHA1 hash of this sexpression.
     * @throws IncorrectUseException
     *             This method throws if "This" cannot be converted into a
     *             verbatim string.
     */
    public byte[] getSHA1() {
        if (_hash == null) {
            _hash = computeSHA1(toVerbatim());
        }
        return _hash;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (_string == null) _string = toStringHelp().toString();
        return _string;
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        byte[] sha = getSHA1();
        return (((int) sha[0]) << 24) | (((int) sha[1]) << 16) | (((int) sha[2]) << 8) | (int) sha[3];
    }
}
