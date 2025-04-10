package com.taobao.api.request;

import com.taobao.api.internal.util.RequestCheckUtils;
import java.util.Map;
import com.taobao.api.TaobaoRequest;
import com.taobao.api.internal.util.TaobaoHashMap;
import com.taobao.api.response.PosterChannelGetResponse;
import com.taobao.api.ApiRuleException;

/**
 * TOP API: taobao.poster.channel.get request
 * 
 * @author auto create
 * @since 1.0, 2011-10-24 16:00:43
 */
public class PosterChannelGetRequest implements TaobaoRequest<PosterChannelGetResponse> {

    private TaobaoHashMap udfParams;

    private Long timestamp;

    /** 
	* 根据频道ID获取频道信息
	 */
    private Long channelId;

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public Long getChannelId() {
        return this.channelId;
    }

    public Long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getApiMethodName() {
        return "taobao.poster.channel.get";
    }

    public Map<String, String> getTextParams() {
        TaobaoHashMap txtParams = new TaobaoHashMap();
        txtParams.put("channel_id", this.channelId);
        if (udfParams != null) {
            txtParams.putAll(this.udfParams);
        }
        return txtParams;
    }

    public void putOtherTextParam(String key, String value) {
        if (this.udfParams == null) {
            this.udfParams = new TaobaoHashMap();
        }
        this.udfParams.put(key, value);
    }

    public Class<PosterChannelGetResponse> getResponseClass() {
        return PosterChannelGetResponse.class;
    }

    public void check() throws ApiRuleException {
        RequestCheckUtils.checkNotEmpty(channelId, "channelId");
    }
}
