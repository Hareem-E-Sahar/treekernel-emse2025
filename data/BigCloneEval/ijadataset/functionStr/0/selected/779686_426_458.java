public class Test {    public String toString() {
        StringBuffer buff = new StringBuffer();
        buff.append("{MP_POST: ");
        buff.append("userId");
        buff.append("=");
        buff.append(getUserId());
        buff.append(", ");
        buff.append("channelName");
        buff.append("=");
        buff.append(getChannelName());
        buff.append(", ");
        buff.append("postEmail");
        buff.append("=");
        buff.append(getPostEmail());
        buff.append(", ");
        buff.append("postDate");
        buff.append("=");
        buff.append(getPostDate());
        buff.append(", ");
        buff.append("title");
        buff.append("=");
        buff.append(getTitle());
        buff.append(", ");
        buff.append("description");
        buff.append("=");
        buff.append(getDescription());
        buff.append(", ");
        buff.append("imageMimeType");
        buff.append("=");
        buff.append(getImageMimeType());
        buff.append("}");
        return buff.toString();
    }
}