package net.roarsoftware.lastfm.scrobble;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.util.Collection;
import java.util.Collections;
import net.roarsoftware.lastfm.Caller;
import net.roarsoftware.lastfm.Session;
import static net.roarsoftware.util.StringUtilities.encode;
import static net.roarsoftware.util.StringUtilities.md5;

/**
 * This class manages communication with the server for scrobbling songs.
 * You can retrieve an instance of this class by calling {@link #newScrobbler(String, String, String) newScrobbler}.<br/>
 * It contains methods to perform the handshake, notify Last.fm about a now playing song and submitting songs to a
 * musical profile, aka scrobbling songs.<br/>
 * See <a href="http://www.last.fm/api/submissions">http://www.last.fm/api/submissions</a> for a deeper explanation
 * of the protocol and various guidelines on how to use the scrobbling service, since this class does not cover
 * error handling or caching.<br/>
 * All methods in this class, which are communicating with the server, return an instance of {@link ResponseStatus}
 * which contains information if the operation was successful or not.<br/>
 * This class respects the <code>proxy</code> property in the {@link Caller} class in all its HTTP calls. If you
 * need the <code>Scrobbler</code> to use a Proxy server, set it with {@link Caller#setProxy(java.net.Proxy)}.
 *
 * @author Janni Kovacs
 */
public class Scrobbler {

    private static final String HANDSHAKE_URL = "http://post.audioscrobbler.com/";

    private final String clientId, clientVersion;

    private final String user;

    private String sessionId;

    private String nowPlayingUrl;

    private String submissionUrl;

    private Scrobbler(String clientId, String clientVersion, String user) {
        this.clientId = clientId;
        this.clientVersion = clientVersion;
        this.user = user;
    }

    /**
	 * Creates a new <code>Scrobbler</code> instance bound to the specified <code>user</code>.
	 *
	 * @param clientId The client id (or "tst")
	 * @param clientVersion The client version (or "1.0")
	 * @param user The last.fm user
	 * @return a new <code>Scrobbler</code> instance
	 */
    public static Scrobbler newScrobbler(String clientId, String clientVersion, String user) {
        return new Scrobbler(clientId, clientVersion, user);
    }

    /**
	 * Performs a standard handshake with the user's password.
	 *
	 * @param password The user's password
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    public ResponseStatus handshake(String password) throws IOException {
        long time = System.currentTimeMillis() / 1000;
        String auth = md5(md5(password) + time);
        String url = String.format("%s?hs=true&p=1.2.1&c=%s&v=%s&u=%s&t=%s&a=%s", HANDSHAKE_URL, clientId, clientVersion, user, time, auth);
        return performHandshake(url);
    }

    /**
	 * Performs a web-service handshake.
	 *
	 * @param session An authenticated Session.
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 * @see net.roarsoftware.lastfm.Authenticator
	 */
    public ResponseStatus handshake(Session session) throws IOException {
        long time = System.currentTimeMillis() / 1000;
        String auth = md5(session.getSecret() + time);
        String url = String.format("%s?hs=true&p=1.2.1&c=%s&v=%s&u=%s&t=%s&a=%s&api_key=%s&sk=%s", HANDSHAKE_URL, clientId, clientVersion, user, time, auth, session.getApiKey(), session.getKey());
        return performHandshake(url);
    }

    /**
	 * Internally performs the handshake operation by calling the given <code>url</code> and examining the response.
	 *
	 * @param url The URL to call
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    private ResponseStatus performHandshake(String url) throws IOException {
        HttpURLConnection connection = Caller.getInstance().openConnection(url);
        InputStream is = connection.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String status = r.readLine();
        int statusCode = ResponseStatus.codeForStatus(status);
        ResponseStatus responseStatus;
        if (statusCode == ResponseStatus.OK) {
            this.sessionId = r.readLine();
            this.nowPlayingUrl = r.readLine();
            this.submissionUrl = r.readLine();
            responseStatus = new ResponseStatus(statusCode);
        } else if (statusCode == ResponseStatus.FAILED) {
            responseStatus = new ResponseStatus(statusCode, status.substring(status.indexOf(' ') + 1));
        } else {
            return new ResponseStatus(statusCode);
        }
        r.close();
        return responseStatus;
    }

    /**
	 * Submits 'now playing' information. This does not affect the musical profile of the user.
	 *
	 * @param artist The artist's name
	 * @param track The track's title
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    public ResponseStatus nowPlaying(String artist, String track) throws IOException {
        return nowPlaying(artist, track, null, -1, -1);
    }

    /**
	 * Submits 'now playing' information. This does not affect the musical profile of the user.
	 *
	 * @param artist The artist's name
	 * @param track The track's title
	 * @param album The album or <code>null</code>
	 * @param length The length of the track in seconds
	 * @param tracknumber The position of the track in the album or -1
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    public ResponseStatus nowPlaying(String artist, String track, String album, int length, int tracknumber) throws IOException {
        if (sessionId == null) throw new IllegalStateException("Perform successful handshake first.");
        String b = album != null ? encode(album) : "";
        String l = length == -1 ? "" : String.valueOf(length);
        String n = tracknumber == -1 ? "" : String.valueOf(tracknumber);
        String body = String.format("s=%s&a=%s&t=%s&b=%s&l=%s&n=%s&m=", sessionId, encode(artist), encode(track), b, l, n);
        if (Caller.getInstance().isDebugMode()) System.out.println("now playing: " + body);
        Proxy proxy = Caller.getInstance().getProxy();
        HttpURLConnection urlConnection = Caller.getInstance().openConnection(nowPlayingUrl);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(body);
        writer.close();
        InputStream is = urlConnection.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String status = r.readLine();
        r.close();
        return new ResponseStatus(ResponseStatus.codeForStatus(status));
    }

    /**
	 * Scrobbles a song.
	 *
	 * @param artist The artist's name
	 * @param track The track's title
	 * @param album The album or <code>null</code>
	 * @param length The length of the track in seconds
	 * @param tracknumber The position of the track in the album or -1
	 * @param source The source of the track
	 * @param startTime The time the track started playing in UNIX timestamp format and UTC time zone
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    public ResponseStatus submit(String artist, String track, String album, int length, int tracknumber, Source source, long startTime) throws IOException {
        return submit(new SubmissionData(artist, track, album, length, tracknumber, source, startTime));
    }

    /**
	 * Scrobbles a song.
	 *
	 * @param data Contains song information
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 */
    public ResponseStatus submit(SubmissionData data) throws IOException {
        return submit(Collections.singletonList(data));
    }

    /**
	 * Scrobbles up to 50 songs at once. Song info is contained in the <code>Collection</code> passed. Songs must be in
	 * chronological order of their play, that means the track first in the list has been played before the track second
	 * in the list and so on.
	 *
	 * @param data A list of song infos
	 * @return the status of the operation
	 * @throws IOException on I/O errors
	 * @throws IllegalArgumentException if data contains more than 50 entries
	 */
    public ResponseStatus submit(Collection<SubmissionData> data) throws IOException {
        if (sessionId == null) throw new IllegalStateException("Perform successful handshake first.");
        if (data.size() > 50) throw new IllegalArgumentException("Max 50 submissions at once");
        StringBuilder builder = new StringBuilder(data.size() * 100);
        int index = 0;
        for (SubmissionData submissionData : data) {
            builder.append(submissionData.toString(sessionId, index));
            builder.append('\n');
            index++;
        }
        String body = builder.toString();
        if (Caller.getInstance().isDebugMode()) System.out.println("submit: " + body);
        HttpURLConnection urlConnection = Caller.getInstance().openConnection(submissionUrl);
        urlConnection.setRequestMethod("POST");
        urlConnection.setDoOutput(true);
        OutputStream outputStream = urlConnection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
        writer.write(body);
        writer.close();
        InputStream is = urlConnection.getInputStream();
        BufferedReader r = new BufferedReader(new InputStreamReader(is));
        String status = r.readLine();
        r.close();
        int statusCode = ResponseStatus.codeForStatus(status);
        if (statusCode == ResponseStatus.FAILED) {
            return new ResponseStatus(statusCode, status.substring(status.indexOf(' ') + 1));
        }
        return new ResponseStatus(statusCode);
    }
}
