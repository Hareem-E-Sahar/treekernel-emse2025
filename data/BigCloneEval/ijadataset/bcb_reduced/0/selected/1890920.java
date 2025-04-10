package sdljava.x.swig;

public class SDL_AudioSpec {

    private long swigCPtr;

    protected boolean swigCMemOwn;

    protected SDL_AudioSpec(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(SDL_AudioSpec obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    protected void finalize() {
        delete();
    }

    public void delete() {
        if (swigCPtr != 0 && swigCMemOwn) {
            swigCMemOwn = false;
            SWIG_SDLAudioJNI.delete_SDL_AudioSpec(swigCPtr);
        }
        swigCPtr = 0;
    }

    public void setFreq(int freq) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_freq(swigCPtr, freq);
    }

    public int getFreq() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_freq(swigCPtr);
    }

    public void setFormat(int format) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_format(swigCPtr, format);
    }

    public int getFormat() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_format(swigCPtr);
    }

    public void setChannels(short channels) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_channels(swigCPtr, channels);
    }

    public short getChannels() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_channels(swigCPtr);
    }

    public void setSilence(short silence) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_silence(swigCPtr, silence);
    }

    public short getSilence() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_silence(swigCPtr);
    }

    public void setSamples(int samples) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_samples(swigCPtr, samples);
    }

    public int getSamples() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_samples(swigCPtr);
    }

    public void setPadding(int padding) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_padding(swigCPtr, padding);
    }

    public int getPadding() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_padding(swigCPtr);
    }

    public void setSize(long size) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_size(swigCPtr, size);
    }

    public long getSize() {
        return SWIG_SDLAudioJNI.get_SDL_AudioSpec_size(swigCPtr);
    }

    public void setCallback(SWIGTYPE_p_f_p_void_p_unsigned_char_int__void callback) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_callback(swigCPtr, SWIGTYPE_p_f_p_void_p_unsigned_char_int__void.getCPtr(callback));
    }

    public SWIGTYPE_p_f_p_void_p_unsigned_char_int__void getCallback() {
        long cPtr = SWIG_SDLAudioJNI.get_SDL_AudioSpec_callback(swigCPtr);
        return (cPtr == 0) ? null : new SWIGTYPE_p_f_p_void_p_unsigned_char_int__void(cPtr, false);
    }

    public void setUserdata(SWIGTYPE_p_void userdata) {
        SWIG_SDLAudioJNI.set_SDL_AudioSpec_userdata(swigCPtr, SWIGTYPE_p_void.getCPtr(userdata));
    }

    public SWIGTYPE_p_void getUserdata() {
        long cPtr = SWIG_SDLAudioJNI.get_SDL_AudioSpec_userdata(swigCPtr);
        return (cPtr == 0) ? null : new SWIGTYPE_p_void(cPtr, false);
    }

    public SDL_AudioSpec() {
        this(SWIG_SDLAudioJNI.new_SDL_AudioSpec(), true);
    }
}
