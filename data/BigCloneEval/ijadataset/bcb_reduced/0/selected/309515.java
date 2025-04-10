package android.net.cts;

import dalvik.annotation.TestTargets;
import dalvik.annotation.TestLevel;
import dalvik.annotation.TestTargetClass;
import dalvik.annotation.TestTargetNew;
import android.content.ContentUris;
import android.net.Uri;
import android.os.Parcel;
import android.test.AndroidTestCase;
import java.io.File;
import java.util.Arrays;

@TestTargetClass(Uri.class)
public class UriTest extends AndroidTestCase {

    @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test write to and read frome parcel.", method = "writeToParcel", args = { android.os.Parcel.class, android.net.Uri.class })
    public void testParcelling() {
        parcelAndUnparcel(Uri.parse("foo:bob%20lee"));
        parcelAndUnparcel(Uri.fromParts("foo", "bob lee", "fragment"));
        parcelAndUnparcel(new Uri.Builder().scheme("http").authority("crazybob.org").path("/rss/").encodedQuery("a=b").fragment("foo").build());
    }

    private void parcelAndUnparcel(Uri u) {
        Parcel p = Parcel.obtain();
        Uri.writeToParcel(p, u);
        p.setDataPosition(0);
        assertEquals(u, Uri.CREATOR.createFromParcel(p));
        p.setDataPosition(0);
        u = u.buildUpon().build();
        Uri.writeToParcel(p, u);
        p.setDataPosition(0);
        assertEquals(u, Uri.CREATOR.createFromParcel(p));
    }

    @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test buildUpon", method = "buildUpon", args = {  })
    public void testBuildUpon() {
        Uri u = Uri.parse("bob:lee").buildUpon().scheme("robert").build();
        assertEquals("robert", u.getScheme());
        assertEquals("lee", u.getEncodedSchemeSpecificPart());
        assertEquals("lee", u.getSchemeSpecificPart());
        assertNull(u.getQuery());
        assertNull(u.getPath());
        assertNull(u.getAuthority());
        assertNull(u.getHost());
        Uri a = Uri.fromParts("foo", "bar", "tee");
        Uri b = a.buildUpon().fragment("new").build();
        assertEquals("new", b.getFragment());
        assertEquals("bar", b.getSchemeSpecificPart());
        assertEquals("foo", b.getScheme());
        a = new Uri.Builder().scheme("foo").encodedOpaquePart("bar").fragment("tee").build();
        b = a.buildUpon().fragment("new").build();
        assertEquals("new", b.getFragment());
        assertEquals("bar", b.getSchemeSpecificPart());
        assertEquals("foo", b.getScheme());
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getSchemeSpecificPart", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getEncodedSchemeSpecificPart", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getEncodedPath", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getPath", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getEncodedQuery", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getQuery", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getEncodedFragment", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getHost", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getPort", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getUserInfo", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "getEncodedUserInfo", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test string uri.", method = "parse", args = { java.lang.String.class }) })
    public void testStringUri() {
        assertEquals("bob lee", Uri.parse("foo:bob%20lee").getSchemeSpecificPart());
        assertEquals("bob%20lee", Uri.parse("foo:bob%20lee").getEncodedSchemeSpecificPart());
        assertEquals("/bob%20lee", Uri.parse("foo:/bob%20lee").getEncodedPath());
        assertNull(Uri.parse("foo:bob%20lee").getPath());
        assertEquals("bob%20lee", Uri.parse("foo:?bob%20lee").getEncodedQuery());
        assertNull(Uri.parse("foo:bob%20lee").getEncodedQuery());
        assertNull(Uri.parse("foo:bar#?bob%20lee").getQuery());
        assertEquals("bob%20lee", Uri.parse("foo:#bob%20lee").getEncodedFragment());
        Uri uri = Uri.parse("http://localhost:42");
        assertEquals("localhost", uri.getHost());
        assertEquals(42, uri.getPort());
        uri = Uri.parse("http://bob@localhost:42");
        assertEquals("bob", uri.getUserInfo());
        assertEquals("localhost", uri.getHost());
        assertEquals(42, uri.getPort());
        uri = Uri.parse("http://bob%20lee@localhost:42");
        assertEquals("bob lee", uri.getUserInfo());
        assertEquals("bob%20lee", uri.getEncodedUserInfo());
        uri = Uri.parse("http://localhost");
        assertEquals("localhost", uri.getHost());
        assertEquals(-1, uri.getPort());
    }

    @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test compareTo", method = "compareTo", args = { android.net.Uri.class })
    public void testCompareTo() {
        Uri a = Uri.parse("foo:a");
        Uri b = Uri.parse("foo:b");
        Uri b2 = Uri.parse("foo:b");
        assertTrue(a.compareTo(b) < 0);
        assertTrue(b.compareTo(a) > 0);
        assertEquals(0, b.compareTo(b2));
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test equals and hashCode.", method = "equals", args = { java.lang.Object.class }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test equals and hashCode.", method = "hashCode", args = {  }) })
    public void testEqualsAndHashCode() {
        Uri a = Uri.parse("http://crazybob.org/test/?foo=bar#tee");
        Uri b = new Uri.Builder().scheme("http").authority("crazybob.org").path("/test/").encodedQuery("foo=bar").fragment("tee").build();
        Uri c = new Uri.Builder().scheme("http").encodedAuthority("crazybob.org").encodedPath("/test/").encodedQuery("foo=bar").encodedFragment("tee").build();
        assertFalse(Uri.EMPTY.equals(null));
        assertEquals(a, b);
        assertEquals(b, c);
        assertEquals(c, a);
        assertEquals(a.hashCode(), b.hashCode());
        assertEquals(b.hashCode(), c.hashCode());
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test encode and decode.", method = "encode", args = { java.lang.String.class }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test encode and decode.", method = "encode", args = { java.lang.String.class, java.lang.String.class }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test encode and decode.", method = "decode", args = { java.lang.String.class }) })
    public void testEncodeAndDecode() {
        String encoded = Uri.encode("Bob:/", "/");
        assertEquals(-1, encoded.indexOf(':'));
        assertTrue(encoded.indexOf('/') > -1);
        assertDecode(null);
        assertDecode("");
        assertDecode("Bob");
        assertDecode(":Bob");
        assertDecode("::Bob");
        assertDecode("Bob::Lee");
        assertDecode("Bob:Lee");
        assertDecode("Bob::");
        assertDecode("Bob:");
        assertDecode("::Bob::");
    }

    private void assertDecode(String s) {
        assertEquals(s, Uri.decode(Uri.encode(s, null)));
    }

    @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test fromFile.", method = "fromFile", args = { java.io.File.class })
    public void testFromFile() {
        File f = new File("/tmp/bob");
        Uri uri = Uri.fromFile(f);
        assertEquals("file:///tmp/bob", uri.toString());
        try {
            Uri.fromFile(null);
            fail("testFile fail");
        } catch (NullPointerException e) {
        }
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test get query parameters.", method = "getQueryParameter", args = { java.lang.String.class }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test get query parameters.", method = "getQueryParameters", args = { java.lang.String.class }) })
    public void testQueryParameters() {
        Uri uri = Uri.parse("content://user");
        assertEquals(null, uri.getQueryParameter("a"));
        uri = uri.buildUpon().appendQueryParameter("a", "b").build();
        assertEquals("b", uri.getQueryParameter("a"));
        uri = uri.buildUpon().appendQueryParameter("a", "b2").build();
        assertEquals(Arrays.asList("b", "b2"), uri.getQueryParameters("a"));
        uri = uri.buildUpon().appendQueryParameter("c", "d").build();
        assertEquals(Arrays.asList("b", "b2"), uri.getQueryParameters("a"));
        assertEquals("d", uri.getQueryParameter("c"));
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "getPathSegments", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "getLastPathSegment", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "", method = "withAppendedPath", args = { android.net.Uri.class, java.lang.String.class }) })
    public void testPathOperations() {
        Uri uri = Uri.parse("content://user/a/b");
        assertEquals(2, uri.getPathSegments().size());
        assertEquals("a", uri.getPathSegments().get(0));
        assertEquals("b", uri.getPathSegments().get(1));
        assertEquals("b", uri.getLastPathSegment());
        Uri first = uri;
        uri = uri.buildUpon().appendPath("c").build();
        assertEquals(3, uri.getPathSegments().size());
        assertEquals("c", uri.getPathSegments().get(2));
        assertEquals("c", uri.getLastPathSegment());
        assertEquals("content://user/a/b/c", uri.toString());
        uri = ContentUris.withAppendedId(uri, 100);
        assertEquals(4, uri.getPathSegments().size());
        assertEquals("100", uri.getPathSegments().get(3));
        assertEquals("100", uri.getLastPathSegment());
        assertEquals(100, ContentUris.parseId(uri));
        assertEquals("content://user/a/b/c/100", uri.toString());
        assertEquals(2, first.getPathSegments().size());
        assertEquals("b", first.getLastPathSegment());
        try {
            first.getPathSegments().get(2);
            fail("test path operations");
        } catch (IndexOutOfBoundsException e) {
        }
        assertEquals(null, Uri.EMPTY.getLastPathSegment());
        Uri withC = Uri.parse("foo:/a/b/").buildUpon().appendPath("c").build();
        assertEquals("/a/b/c", withC.getPath());
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "isAbsolute", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "isOpaque", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "isRelative", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "getHost", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "getPort", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "getScheme", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "getSchemeSpecificPart", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "fromParts", args = { java.lang.String.class, java.lang.String.class, java.lang.String.class }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test opaque uri.", method = "toString", args = {  }) })
    public void testOpaqueUri() {
        Uri uri = Uri.parse("mailto:nobody");
        testOpaqueUri(uri);
        uri = uri.buildUpon().build();
        testOpaqueUri(uri);
        uri = Uri.fromParts("mailto", "nobody", null);
        testOpaqueUri(uri);
        uri = uri.buildUpon().build();
        testOpaqueUri(uri);
        uri = new Uri.Builder().scheme("mailto").opaquePart("nobody").build();
        testOpaqueUri(uri);
        uri = uri.buildUpon().build();
        testOpaqueUri(uri);
    }

    private void testOpaqueUri(Uri uri) {
        assertEquals("mailto", uri.getScheme());
        assertEquals("nobody", uri.getSchemeSpecificPart());
        assertEquals("nobody", uri.getEncodedSchemeSpecificPart());
        assertNull(uri.getFragment());
        assertTrue(uri.isAbsolute());
        assertTrue(uri.isOpaque());
        assertFalse(uri.isRelative());
        assertFalse(uri.isHierarchical());
        assertNull(uri.getAuthority());
        assertNull(uri.getEncodedAuthority());
        assertNull(uri.getPath());
        assertNull(uri.getEncodedPath());
        assertNull(uri.getUserInfo());
        assertNull(uri.getEncodedUserInfo());
        assertNull(uri.getQuery());
        assertNull(uri.getEncodedQuery());
        assertNull(uri.getHost());
        assertEquals(-1, uri.getPort());
        assertTrue(uri.getPathSegments().isEmpty());
        assertNull(uri.getLastPathSegment());
        assertEquals("mailto:nobody", uri.toString());
        Uri withFragment = uri.buildUpon().fragment("top").build();
        assertEquals("mailto:nobody#top", withFragment.toString());
    }

    @TestTargets({ @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getAuthority", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getScheme", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getEncodedAuthority", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getPath", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getEncodedPath", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getQuery", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getEncodedQuery", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getFragment", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getEncodedFragment", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "getSchemeSpecificPart", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "isAbsolute", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "isHierarchical", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "isOpaque", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "isRelative", args = {  }), @TestTargetNew(level = TestLevel.COMPLETE, notes = "Test hierarchical uris.", method = "toString", args = {  }) })
    public void testHierarchicalUris() {
        testHierarchical("http", "google.com", "/p1/p2", "query", "fragment");
        testHierarchical("file", null, "/p1/p2", null, null);
        testHierarchical("content", "contact", "/p1/p2", null, null);
        testHierarchical("http", "google.com", "/p1/p2", null, "fragment");
        testHierarchical("http", "google.com", "", null, "fragment");
        testHierarchical("http", "google.com", "", "query", "fragment");
        testHierarchical("http", "google.com", "", "query", null);
        testHierarchical("http", null, "/", "query", null);
    }

    private static void testHierarchical(String scheme, String authority, String path, String query, String fragment) {
        StringBuilder sb = new StringBuilder();
        if (authority != null) {
            sb.append("//").append(authority);
        }
        if (path != null) {
            sb.append(path);
        }
        if (query != null) {
            sb.append('?').append(query);
        }
        String ssp = sb.toString();
        if (scheme != null) {
            sb.insert(0, scheme + ":");
        }
        if (fragment != null) {
            sb.append('#').append(fragment);
        }
        String uriString = sb.toString();
        Uri uri = Uri.parse(uriString);
        compareHierarchical(uriString, ssp, uri, scheme, authority, path, query, fragment);
        compareHierarchical(uriString, ssp, uri, scheme, authority, path, query, fragment);
        uri = uri.buildUpon().build();
        compareHierarchical(uriString, ssp, uri, scheme, authority, path, query, fragment);
        compareHierarchical(uriString, ssp, uri, scheme, authority, path, query, fragment);
        Uri built = new Uri.Builder().scheme(scheme).encodedAuthority(authority).encodedPath(path).encodedQuery(query).encodedFragment(fragment).build();
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
        built = new Uri.Builder().scheme(scheme).authority(authority).path(path).query(query).fragment(fragment).build();
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
        built = built.buildUpon().build();
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
        compareHierarchical(uriString, ssp, built, scheme, authority, path, query, fragment);
    }

    private static void compareHierarchical(String uriString, String ssp, Uri uri, String scheme, String authority, String path, String query, String fragment) {
        assertEquals(scheme, uri.getScheme());
        assertEquals(authority, uri.getAuthority());
        assertEquals(authority, uri.getEncodedAuthority());
        assertEquals(path, uri.getPath());
        assertEquals(path, uri.getEncodedPath());
        assertEquals(query, uri.getQuery());
        assertEquals(query, uri.getEncodedQuery());
        assertEquals(fragment, uri.getFragment());
        assertEquals(fragment, uri.getEncodedFragment());
        assertEquals(ssp, uri.getSchemeSpecificPart());
        if (scheme != null) {
            assertTrue(uri.isAbsolute());
            assertFalse(uri.isRelative());
        } else {
            assertFalse(uri.isAbsolute());
            assertTrue(uri.isRelative());
        }
        assertFalse(uri.isOpaque());
        assertTrue(uri.isHierarchical());
        assertEquals(uriString, uri.toString());
    }
}
