package org.pjsip.pjsua;

public class pjmedia_aud_param {

    private long swigCPtr;

    protected boolean swigCMemOwn;

    protected pjmedia_aud_param(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(pjmedia_aud_param obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0 && swigCMemOwn) {
            swigCMemOwn = false;
            pjsuaJNI.delete_pjmedia_aud_param(swigCPtr);
        }
        swigCPtr = 0;
    }

    public void setDir(pjmedia_dir value) {
        pjsuaJNI.pjmedia_aud_param_dir_set(swigCPtr, this, value.swigValue());
    }

    public pjmedia_dir getDir() {
        return pjmedia_dir.swigToEnum(pjsuaJNI.pjmedia_aud_param_dir_get(swigCPtr, this));
    }

    public void setRec_id(int value) {
        pjsuaJNI.pjmedia_aud_param_rec_id_set(swigCPtr, this, value);
    }

    public int getRec_id() {
        return pjsuaJNI.pjmedia_aud_param_rec_id_get(swigCPtr, this);
    }

    public void setPlay_id(int value) {
        pjsuaJNI.pjmedia_aud_param_play_id_set(swigCPtr, this, value);
    }

    public int getPlay_id() {
        return pjsuaJNI.pjmedia_aud_param_play_id_get(swigCPtr, this);
    }

    public void setClock_rate(long value) {
        pjsuaJNI.pjmedia_aud_param_clock_rate_set(swigCPtr, this, value);
    }

    public long getClock_rate() {
        return pjsuaJNI.pjmedia_aud_param_clock_rate_get(swigCPtr, this);
    }

    public void setChannel_count(long value) {
        pjsuaJNI.pjmedia_aud_param_channel_count_set(swigCPtr, this, value);
    }

    public long getChannel_count() {
        return pjsuaJNI.pjmedia_aud_param_channel_count_get(swigCPtr, this);
    }

    public void setSamples_per_frame(long value) {
        pjsuaJNI.pjmedia_aud_param_samples_per_frame_set(swigCPtr, this, value);
    }

    public long getSamples_per_frame() {
        return pjsuaJNI.pjmedia_aud_param_samples_per_frame_get(swigCPtr, this);
    }

    public void setBits_per_sample(long value) {
        pjsuaJNI.pjmedia_aud_param_bits_per_sample_set(swigCPtr, this, value);
    }

    public long getBits_per_sample() {
        return pjsuaJNI.pjmedia_aud_param_bits_per_sample_get(swigCPtr, this);
    }

    public void setFlags(long value) {
        pjsuaJNI.pjmedia_aud_param_flags_set(swigCPtr, this, value);
    }

    public long getFlags() {
        return pjsuaJNI.pjmedia_aud_param_flags_get(swigCPtr, this);
    }

    public void setExt_fmt(SWIGTYPE_p_pjmedia_format value) {
        pjsuaJNI.pjmedia_aud_param_ext_fmt_set(swigCPtr, this, SWIGTYPE_p_pjmedia_format.getCPtr(value));
    }

    public SWIGTYPE_p_pjmedia_format getExt_fmt() {
        return new SWIGTYPE_p_pjmedia_format(pjsuaJNI.pjmedia_aud_param_ext_fmt_get(swigCPtr, this), true);
    }

    public void setInput_latency_ms(long value) {
        pjsuaJNI.pjmedia_aud_param_input_latency_ms_set(swigCPtr, this, value);
    }

    public long getInput_latency_ms() {
        return pjsuaJNI.pjmedia_aud_param_input_latency_ms_get(swigCPtr, this);
    }

    public void setOutput_latency_ms(long value) {
        pjsuaJNI.pjmedia_aud_param_output_latency_ms_set(swigCPtr, this, value);
    }

    public long getOutput_latency_ms() {
        return pjsuaJNI.pjmedia_aud_param_output_latency_ms_get(swigCPtr, this);
    }

    public void setInput_vol(long value) {
        pjsuaJNI.pjmedia_aud_param_input_vol_set(swigCPtr, this, value);
    }

    public long getInput_vol() {
        return pjsuaJNI.pjmedia_aud_param_input_vol_get(swigCPtr, this);
    }

    public void setOutput_vol(long value) {
        pjsuaJNI.pjmedia_aud_param_output_vol_set(swigCPtr, this, value);
    }

    public long getOutput_vol() {
        return pjsuaJNI.pjmedia_aud_param_output_vol_get(swigCPtr, this);
    }

    public void setInput_route(pjmedia_aud_dev_route value) {
        pjsuaJNI.pjmedia_aud_param_input_route_set(swigCPtr, this, value.swigValue());
    }

    public pjmedia_aud_dev_route getInput_route() {
        return pjmedia_aud_dev_route.swigToEnum(pjsuaJNI.pjmedia_aud_param_input_route_get(swigCPtr, this));
    }

    public void setOutput_route(pjmedia_aud_dev_route value) {
        pjsuaJNI.pjmedia_aud_param_output_route_set(swigCPtr, this, value.swigValue());
    }

    public pjmedia_aud_dev_route getOutput_route() {
        return pjmedia_aud_dev_route.swigToEnum(pjsuaJNI.pjmedia_aud_param_output_route_get(swigCPtr, this));
    }

    public void setEc_enabled(int value) {
        pjsuaJNI.pjmedia_aud_param_ec_enabled_set(swigCPtr, this, value);
    }

    public int getEc_enabled() {
        return pjsuaJNI.pjmedia_aud_param_ec_enabled_get(swigCPtr, this);
    }

    public void setEc_tail_ms(long value) {
        pjsuaJNI.pjmedia_aud_param_ec_tail_ms_set(swigCPtr, this, value);
    }

    public long getEc_tail_ms() {
        return pjsuaJNI.pjmedia_aud_param_ec_tail_ms_get(swigCPtr, this);
    }

    public void setPlc_enabled(int value) {
        pjsuaJNI.pjmedia_aud_param_plc_enabled_set(swigCPtr, this, value);
    }

    public int getPlc_enabled() {
        return pjsuaJNI.pjmedia_aud_param_plc_enabled_get(swigCPtr, this);
    }

    public void setCng_enabled(int value) {
        pjsuaJNI.pjmedia_aud_param_cng_enabled_set(swigCPtr, this, value);
    }

    public int getCng_enabled() {
        return pjsuaJNI.pjmedia_aud_param_cng_enabled_get(swigCPtr, this);
    }

    public pjmedia_aud_param() {
        this(pjsuaJNI.new_pjmedia_aud_param(), true);
    }
}
