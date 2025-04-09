public class Test {        public String getChannelLink() {
            return this.feed.getRootElement().element("channel").element("link").getText();
        }
}