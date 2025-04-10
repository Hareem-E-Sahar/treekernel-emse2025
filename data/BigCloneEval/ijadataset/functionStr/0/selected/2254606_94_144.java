public class Test {    public static RssChannel getRssChannel(final String rssUrl, final boolean parseItems) {
        if (!StringUtils.hasText(rssUrl)) {
            return null;
        }
        try {
            final URL url = new URL(rssUrl);
            final RssChannel channel = new RssChannel();
            new AbstractXmlDocument(url) {

                @Override
                protected void init() {
                    final Element ele = getRoot().element("channel");
                    if (ele == null) {
                        return;
                    }
                    channel.title = ele.elementTextTrim("title");
                    channel.link = ele.elementTextTrim("link");
                    if (StringUtils.hasText(channel.link)) {
                        final String link = channel.link.toLowerCase();
                        if ((link.charAt(0) != '/') && !link.startsWith("http://")) {
                            channel.link = "http://" + link;
                        }
                    }
                    if (parseItems) {
                        final Iterator<?> it = ele.elementIterator("item");
                        while (it.hasNext()) {
                            final Element iele = (Element) it.next();
                            final RssChannelItem item = new RssChannelItem();
                            channel.getChannelItems().add(item);
                            item.title = iele.elementTextTrim("title");
                            item.link = iele.elementTextTrim("link");
                            item.description = iele.elementTextTrim("description");
                            final String dateString = iele.elementTextTrim("pubDate");
                            for (final String mask : RFC822_MASKS) {
                                final SimpleDateFormat sdf = new SimpleDateFormat(mask, Locale.ENGLISH);
                                try {
                                    item.pubDate = sdf.parse(dateString);
                                    break;
                                } catch (final ParseException e) {
                                }
                            }
                            item.comments = iele.elementTextTrim("comments");
                        }
                    }
                }
            };
            return channel;
        } catch (final IOException ex) {
            throw HandleException.wrapException(ex);
        }
    }
}