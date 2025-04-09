package local.ua;

import java.util.Enumeration;
import local.media.AudioClipPlayer;
import org.jjsip.sdp.MediaDescriptor;
import org.jjsip.sdp.MediaField;
import org.jjsip.sdp.SessionDescriptor;
import org.jjsip.sip.address.NameAddress;
import org.jjsip.sip.call.Call;
import org.jjsip.sip.call.SdpTools;
import org.jjsip.sip.endpoint.UserAgent;
import org.jjsip.sip.endpoint.UserAgentListener;
import org.jjsip.sip.endpoint.UserAgentProfile;
import org.jjsip.sip.header.StatusLine;
import org.jjsip.sip.message.Message;
import org.jjsip.sip.provider.SipProvider;
import org.jjsip.tools.Archive;
import org.jjsip.tools.LogLevel;
import org.jjsip.tools.Parser;

/**
 * Simple SIP user agent (UA). It includes audio/video applications.
 * <p>
 * It can use external audio/video tools as media applications. Currently only
 * RAT (Robust Audio Tool) and VIC are supported as external applications.
 */
public class MediaUserAgent extends UserAgent {

    /** Audio application */
    protected MediaLauncher audio_app = null;

    /** Video application */
    protected MediaLauncher video_app = null;

    /** Media file path */
    final String MEDIA_PATH = "media/local/ua/";

    /** On wav file */
    final String CLIP_ON = MEDIA_PATH + "on.wav";

    /** Off wav file */
    final String CLIP_OFF = MEDIA_PATH + "off.wav";

    /** Ring wav file */
    final String CLIP_RING = MEDIA_PATH + "ring.wav";

    /** Ring sound */
    AudioClipPlayer clip_ring;

    /** On sound */
    AudioClipPlayer clip_on;

    /** Off sound */
    AudioClipPlayer clip_off;

    /** Enables audio */
    public void setAudio(boolean enable) {
        ((MediaUserAgentProfile) userProfile).audio = enable;
    }

    /** Enables video */
    public void setVideo(boolean enable) {
        ((MediaUserAgentProfile) userProfile).video = enable;
    }

    /** Constructs a UA with a default media port */
    public MediaUserAgent(SipProvider sip_provider, MediaUserAgentProfile user_profile, UserAgentListener listener) {
        super(sip_provider, user_profile, listener);
        if (!user_profile.use_rat && !user_profile.use_jmf) {
            if (user_profile.audio && !user_profile.recv_only && user_profile.send_file == null && !user_profile.send_tone) local.media.AudioInput.initAudioLine();
            if (user_profile.audio && !user_profile.send_only && user_profile.recv_file == null) local.media.AudioOutput.initAudioLine();
        }
        if (!user_profile.use_rat) {
            try {
                String jar_file = UserAgentProfile.ua_jar;
                clip_on = new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file, CLIP_ON)), null);
                clip_off = new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file, CLIP_OFF)), null);
                clip_ring = new AudioClipPlayer(Archive.getAudioInputStream(Archive.getJarURL(jar_file, CLIP_RING)), null);
            } catch (Exception e) {
                printException(e, LogLevel.HIGH);
            }
        }
        initSessionDescriptor();
        if (user_profile.audio || !user_profile.video) addMediaDescriptor("audio", user_profile.audio_port, user_profile.audio_avp, user_profile.audio_codec, user_profile.audio_sample_rate);
        if (user_profile.video) addMediaDescriptor("video", user_profile.video_port, user_profile.video_avp, null, 0);
    }

    /** Closes an ongoing, incoming, or pending call */
    public void hangup() {
        if (clip_ring != null) clip_ring.stop();
        closeMediaApplication();
        super.hangup();
    }

    /** Closes an ongoing, incoming, or pending call */
    public void accept() {
        if (clip_ring != null) clip_ring.stop();
        super.accept();
    }

    /** Redirects an incoming call */
    public void redirect(String redirection) {
        if (clip_ring != null) clip_ring.stop();
        super.redirect(redirection);
    }

    /** Launches the Media Application (currently, the RAT audio tool) */
    protected void launchMediaApplication() {
        if (audio_app != null || video_app != null) {
            printLog("DEBUG: media application is already running", LogLevel.HIGH);
            return;
        }
        SessionDescriptor local_sdp = new SessionDescriptor(call.getLocalSessionDescriptor());
        String local_media_address = (new Parser(local_sdp.getConnection().toString())).skipString().skipString().getString();
        int local_audio_port = 0;
        int local_video_port = 0;
        for (Enumeration<MediaDescriptor> e = local_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); ) {
            MediaField media = ((MediaDescriptor) e.nextElement()).getMedia();
            if (media.getMedia().equals("audio")) local_audio_port = media.getPort();
            if (media.getMedia().equals("video")) local_video_port = media.getPort();
        }
        SessionDescriptor remote_sdp = new SessionDescriptor(call.getRemoteSessionDescriptor());
        String remote_media_address = (new Parser(remote_sdp.getConnection().toString())).skipString().skipString().getString();
        int remote_audio_port = 0;
        int remote_video_port = 0;
        for (Enumeration<MediaDescriptor> e = remote_sdp.getMediaDescriptors().elements(); e.hasMoreElements(); ) {
            MediaField media = ((MediaDescriptor) e.nextElement()).getMedia();
            if (media.getMedia().equals("audio")) remote_audio_port = media.getPort();
            if (media.getMedia().equals("video")) remote_video_port = media.getPort();
        }
        int dir = 0;
        if (userProfile.recv_only) dir = -1; else if (userProfile.send_only) dir = 1;
        if (((MediaUserAgentProfile) userProfile).audio && local_audio_port != 0 && remote_audio_port != 0) {
            if (((MediaUserAgentProfile) userProfile).use_rat) {
                audio_app = new RATLauncher(((MediaUserAgentProfile) userProfile).bin_rat, local_audio_port, remote_media_address, remote_audio_port, log);
            } else if (((MediaUserAgentProfile) userProfile).use_jmf) {
                try {
                    Class myclass = Class.forName("local.ua.JMFAudioLauncher");
                    Class[] parameter_types = { java.lang.Integer.TYPE, Class.forName("java.lang.String"), java.lang.Integer.TYPE, java.lang.Integer.TYPE, Class.forName("org.jjsip.tools.Log") };
                    Object[] parameters = { new Integer(local_audio_port), remote_media_address, new Integer(remote_audio_port), new Integer(dir), log };
                    java.lang.reflect.Constructor constructor = myclass.getConstructor(parameter_types);
                    audio_app = (MediaLauncher) constructor.newInstance(parameters);
                } catch (Exception e) {
                    printException(e, LogLevel.HIGH);
                    printLog("Error trying to create the JMFAudioLauncher", LogLevel.HIGH);
                }
            }
            if (audio_app == null) {
                String audio_in = null;
                if (userProfile.send_tone) audio_in = JAudioLauncher.TONE; else if (userProfile.send_file != null) audio_in = userProfile.send_file;
                String audio_out = null;
                if (userProfile.recv_file != null) audio_out = userProfile.recv_file;
                audio_app = new JAudioLauncher(local_audio_port, remote_media_address, remote_audio_port, dir, audio_in, audio_out, ((MediaUserAgentProfile) userProfile).audio_sample_rate, ((MediaUserAgentProfile) userProfile).audio_sample_size, ((MediaUserAgentProfile) userProfile).audio_frame_size, log);
            }
            audio_app.startMedia();
        }
        if (((MediaUserAgentProfile) userProfile).video && local_video_port != 0 && remote_video_port != 0) {
            if (((MediaUserAgentProfile) userProfile).use_vic) {
                video_app = new VICLauncher(((MediaUserAgentProfile) userProfile).bin_vic, local_video_port, remote_media_address, remote_video_port, log);
            } else if (((MediaUserAgentProfile) userProfile).use_jmf) {
                try {
                    Class myclass = Class.forName("local.ua.JMFVideoLauncher");
                    Class[] parameter_types = { java.lang.Integer.TYPE, Class.forName("java.lang.String"), java.lang.Integer.TYPE, java.lang.Integer.TYPE, Class.forName("org.jjsip.tools.Log") };
                    Object[] parameters = { new Integer(local_video_port), remote_media_address, new Integer(remote_video_port), new Integer(dir), log };
                    java.lang.reflect.Constructor constructor = myclass.getConstructor(parameter_types);
                    video_app = (MediaLauncher) constructor.newInstance(parameters);
                } catch (Exception e) {
                    printException(e, LogLevel.HIGH);
                    printLog("Error trying to create the JMFVideoLauncher", LogLevel.HIGH);
                }
            }
            if (video_app == null) {
                printLog("No external video application nor JMF has been provided: Video not started", LogLevel.HIGH);
                return;
            }
            video_app.startMedia();
        }
    }

    /** Close the Media Application */
    protected void closeMediaApplication() {
        if (audio_app != null) {
            audio_app.stopMedia();
            audio_app = null;
        }
        if (video_app != null) {
            video_app.stopMedia();
            video_app = null;
        }
    }

    /**
	 * Callback function called when arriving a new INVITE method (incoming
	 * call)
	 */
    public void onCallIncoming(Call call, NameAddress callee, NameAddress caller, String sdp, Message invite) {
        printLog("onCallIncoming()", LogLevel.LOW);
        if (call != this.call) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("INCOMING", LogLevel.HIGH);
        changeStatus(UA_INCOMING_CALL);
        call.ring();
        if (sdp != null) {
            SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
            SessionDescriptor local_sdp = new SessionDescriptor(localSession);
            SessionDescriptor new_sdp = new SessionDescriptor(remote_sdp.getOrigin(), remote_sdp.getSessionName(), local_sdp.getConnection(), local_sdp.getTime());
            new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
            new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp.getMediaDescriptors());
            new_sdp = SdpTools.sdpAttirbuteSelection(new_sdp, "rtpmap");
            localSession = new_sdp.toString();
        }
        if (clip_ring != null) clip_ring.loop();
        if (listener != null) listener.onUaCallIncoming(this, callee, caller);
    }

    /**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a 180 Ringing
	 */
    public void onCallRinging(Call call, Message resp) {
        printLog("onCallRinging()", LogLevel.LOW);
        if (call != this.call && call != callTransfer) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("RINGING", LogLevel.HIGH);
        if (clip_on != null) clip_on.replay();
        if (listener != null) listener.onUaCallRinging(this);
    }

    /** Callback function called when arriving a 2xx (call accepted) */
    public void onCallAccepted(Call call, String sdp, Message resp) {
        printLog("onCallAccepted()", LogLevel.LOW);
        if (call != this.call && call != callTransfer) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("ACCEPTED/CALL", LogLevel.HIGH);
        changeStatus(UA_ONCALL);
        if (userProfile.no_offer) {
            SessionDescriptor remote_sdp = new SessionDescriptor(sdp);
            SessionDescriptor local_sdp = new SessionDescriptor(localSession);
            SessionDescriptor new_sdp = new SessionDescriptor(remote_sdp.getOrigin(), remote_sdp.getSessionName(), local_sdp.getConnection(), local_sdp.getTime());
            new_sdp.addMediaDescriptors(local_sdp.getMediaDescriptors());
            new_sdp = SdpTools.sdpMediaProduct(new_sdp, remote_sdp.getMediaDescriptors());
            new_sdp = SdpTools.sdpAttirbuteSelection(new_sdp, "rtpmap");
            localSession = new_sdp.toString();
            call.ackWithAnswer(localSession);
        }
        if (clip_on != null) clip_on.replay();
        if (listener != null) listener.onUaCallAccepted(this);
        launchMediaApplication();
        if (call == callTransfer) {
            StatusLine status_line = resp.getStatusLine();
            int code = status_line.getCode();
            String reason = status_line.getReason();
            this.call.notify(code, reason);
        }
    }

    /** Callback function called when arriving an ACK method (call confirmed) */
    public void onCallConfirmed(Call call, String sdp, Message ack) {
        printLog("onCallConfirmed()", LogLevel.LOW);
        if (call != this.call) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("CONFIRMED/CALL", LogLevel.HIGH);
        changeStatus(UA_ONCALL);
        if (clip_on != null) clip_on.replay();
        launchMediaApplication();
        if (userProfile.hangup_time > 0) this.automaticHangup(userProfile.hangup_time);
    }

    /** Callback function called when arriving a 4xx (call failure) */
    public void onCallRefused(Call call, String reason, Message resp) {
        printLog("onCallRefused()", LogLevel.LOW);
        if (call != this.call) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("REFUSED (" + reason + ")", LogLevel.HIGH);
        changeStatus(UA_IDLE);
        if (call == callTransfer) {
            StatusLine status_line = resp.getStatusLine();
            int code = status_line.getCode();
            this.call.notify(code, reason);
            callTransfer = null;
        }
        if (clip_off != null) clip_off.replay();
        if (listener != null) listener.onUaCallFailed(this);
    }

    /**
	 * Callback function that may be overloaded (extended). Called when arriving
	 * a CANCEL request
	 */
    public void onCallCanceling(Call call, Message cancel) {
        printLog("onCallCanceling()", LogLevel.LOW);
        if (call != this.call) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("CANCEL", LogLevel.HIGH);
        changeStatus(UA_IDLE);
        if (clip_ring != null) clip_ring.stop();
        if (clip_off != null) clip_off.replay();
        if (listener != null) listener.onUaCallCancelled(this);
    }

    /** Callback function called when arriving a BYE request */
    public void onCallClosing(Call call, Message bye) {
        printLog("onCallClosing()", LogLevel.LOW);
        if (call != this.call && call != callTransfer) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        if (call != callTransfer && callTransfer != null) {
            printLog("CLOSE PREVIOUS CALL", LogLevel.HIGH);
            this.call = callTransfer;
            callTransfer = null;
            return;
        }
        printLog("CLOSE", LogLevel.HIGH);
        closeMediaApplication();
        if (clip_off != null) clip_off.replay();
        if (listener != null) listener.onUaCallClosed(this);
        changeStatus(UA_IDLE);
    }

    /** Callback function called when the invite expires */
    public void onCallTimeout(Call call) {
        printLog("onCallTimeout()", LogLevel.LOW);
        if (call != this.call) {
            printLog("NOT the current call", LogLevel.LOW);
            return;
        }
        printLog("NOT FOUND/TIMEOUT", LogLevel.HIGH);
        changeStatus(UA_IDLE);
        if (call == callTransfer) {
            int code = 408;
            String reason = "Request Timeout";
            this.call.notify(code, reason);
            callTransfer = null;
        }
        if (clip_off != null) clip_off.replay();
        if (listener != null) listener.onUaCallFailed(this);
    }

    @Override
    public MediaUserAgentProfile getUserProfile() {
        return ((MediaUserAgentProfile) userProfile);
    }
}
