public class Test {    protected Result[] searchSupport(SearchParameter[] searchParameters, Map searchContext, int desired_max_matches, int absolute_max_matches, String headers, ResultListener listener) throws SearchException {
        debugStart();
        boolean only_if_mod = !searchContext.containsKey(Engine.SC_FORCE_FULL);
        pageDetails page_details = super.getWebPageContent(searchParameters, searchContext, headers, only_if_mod);
        String page = page_details.getContent();
        if (listener != null) {
            listener.contentReceived(this, page);
        }
        if (page == null || page.length() == 0) {
            return (new Result[0]);
        }
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(page.getBytes("UTF-8"));
            RSSFeed rssFeed = StaticUtilities.getRSSFeed(bais);
            RSSChannel[] channels = rssFeed.getChannels();
            List results = new ArrayList();
            for (int i = 0; i < channels.length; i++) {
                RSSChannel channel = channels[i];
                SimpleXMLParserDocumentNode[] channel_kids = channel.getNode().getChildren();
                int auto_dl_state = AUTO_DL_SUPPORTED_YES;
                for (int j = 0; j < channel_kids.length; j++) {
                    SimpleXMLParserDocumentNode child = channel_kids[j];
                    String lc_full_child_name = child.getFullName().toLowerCase();
                    if (lc_full_child_name.equals("vuze:auto_dl_enabled")) {
                        if (!child.getValue().equalsIgnoreCase("true")) {
                            auto_dl_state = AUTO_DL_SUPPORTED_NO;
                        }
                    }
                }
                setLocalLong(LD_AUTO_DL_SUPPORTED, auto_dl_state);
                RSSItem[] items = channel.getItems();
                for (int j = 0; j < items.length; j++) {
                    RSSItem item = items[j];
                    WebResult result = new WebResult(this, getRootPage(), getBasePage(), getDateParser(), "");
                    result.setPublishedDate(item.getPublicationDate());
                    result.setNameFromHTML(item.getTitle());
                    URL cdp_link = item.getLink();
                    if (cdp_link != null) {
                        result.setCDPLink(cdp_link.toExternalForm());
                    }
                    String uid = item.getUID();
                    if (uid != null) {
                        result.setUID(uid);
                    }
                    boolean got_seeds_peers = false;
                    SimpleXMLParserDocumentNode node = item.getNode();
                    if (node != null) {
                        SimpleXMLParserDocumentNode[] children = node.getChildren();
                        boolean vuze_feed = false;
                        for (int k = 0; k < children.length; k++) {
                            SimpleXMLParserDocumentNode child = children[k];
                            String lc_full_child_name = child.getFullName().toLowerCase();
                            if (lc_full_child_name.startsWith("vuze:")) {
                                vuze_feed = true;
                                break;
                            }
                        }
                        for (int k = 0; k < children.length; k++) {
                            SimpleXMLParserDocumentNode child = children[k];
                            String lc_child_name = child.getName().toLowerCase();
                            String lc_full_child_name = child.getFullName().toLowerCase();
                            String value = child.getValue();
                            if (lc_child_name.equals("enclosure")) {
                                SimpleXMLParserDocumentAttribute typeAtt = child.getAttribute("type");
                                if (typeAtt != null && typeAtt.getValue().equalsIgnoreCase("application/x-bittorrent")) {
                                    SimpleXMLParserDocumentAttribute urlAtt = child.getAttribute("url");
                                    if (urlAtt != null) {
                                        result.setTorrentLink(urlAtt.getValue());
                                    }
                                    SimpleXMLParserDocumentAttribute lengthAtt = child.getAttribute("length");
                                    if (lengthAtt != null) {
                                        result.setSizeFromHTML(lengthAtt.getValue());
                                    }
                                }
                            } else if (lc_child_name.equals("category")) {
                                result.setCategoryFromHTML(value);
                            } else if (lc_child_name.equals("comments")) {
                                result.setCommentsFromHTML(value);
                            } else if (lc_child_name.equals("link") || lc_child_name.equals("guid")) {
                                String lc_value = value.toLowerCase();
                                try {
                                    URL url = new URL(value);
                                    if (lc_value.endsWith(".torrent") || lc_value.startsWith("magnet:") || lc_value.startsWith("bc:") || lc_value.startsWith("bctp:") || lc_value.startsWith("dht:")) {
                                        result.setTorrentLink(value);
                                    } else if (lc_child_name.equals("link") && !vuze_feed) {
                                        long test = getLocalLong(LD_LINK_IS_TORRENT, 0);
                                        if (test == 1) {
                                            result.setTorrentLink(value);
                                        } else if (test == 0 || SystemTime.getCurrentTime() - test > 60 * 1000) {
                                            if (linkIsToTorrent(url)) {
                                                result.setTorrentLink(value);
                                                setLocalLong(LD_LINK_IS_TORRENT, 1);
                                            } else {
                                                setLocalLong(LD_LINK_IS_TORRENT, SystemTime.getCurrentTime());
                                            }
                                        }
                                    }
                                } catch (Throwable e) {
                                    SimpleXMLParserDocumentAttribute typeAtt = child.getAttribute("type");
                                    if (typeAtt != null && typeAtt.getValue().equalsIgnoreCase("application/x-bittorrent")) {
                                        SimpleXMLParserDocumentAttribute hrefAtt = child.getAttribute("href");
                                        if (hrefAtt != null) {
                                            String href = hrefAtt.getValue().trim();
                                            try {
                                                result.setTorrentLink(new URL(href).toExternalForm());
                                            } catch (Throwable f) {
                                            }
                                        }
                                    }
                                }
                            } else if (lc_child_name.equals("content") && rssFeed.isAtomFeed()) {
                                SimpleXMLParserDocumentAttribute srcAtt = child.getAttribute("src");
                                String src = srcAtt == null ? null : srcAtt.getValue();
                                if (src != null) {
                                    boolean is_dl_link = false;
                                    SimpleXMLParserDocumentAttribute typeAtt = child.getAttribute("type");
                                    if (typeAtt != null && typeAtt.getValue().equalsIgnoreCase("application/x-bittorrent")) {
                                        is_dl_link = true;
                                    }
                                    if (!is_dl_link) {
                                        is_dl_link = src.toLowerCase().indexOf(".torrent") != -1;
                                    }
                                    if (is_dl_link) {
                                        try {
                                            new URL(src);
                                            result.setTorrentLink(src);
                                        } catch (Throwable e) {
                                        }
                                    }
                                }
                            } else if (lc_full_child_name.equals("vuze:size")) {
                                result.setSizeFromHTML(value);
                            } else if (lc_full_child_name.equals("vuze:seeds")) {
                                got_seeds_peers = true;
                                result.setNbSeedsFromHTML(value);
                            } else if (lc_full_child_name.equals("vuze:superseeds")) {
                                got_seeds_peers = true;
                                result.setNbSuperSeedsFromHTML(value);
                            } else if (lc_full_child_name.equals("vuze:peers")) {
                                got_seeds_peers = true;
                                result.setNbPeersFromHTML(value);
                            } else if (lc_full_child_name.equals("vuze:rank")) {
                                result.setRankFromHTML(value);
                            } else if (lc_full_child_name.equals("vuze:contenttype")) {
                                String type = value.toLowerCase();
                                if (type.startsWith("video")) {
                                    type = Engine.CT_VIDEO;
                                } else if (type.startsWith("audio")) {
                                    type = Engine.CT_AUDIO;
                                } else if (type.startsWith("games")) {
                                    type = Engine.CT_GAME;
                                }
                                result.setContentType(type);
                            } else if (lc_full_child_name.equals("vuze:downloadurl")) {
                                result.setTorrentLink(value);
                            } else if (lc_full_child_name.equals("vuze:playurl")) {
                                result.setPlayLink(value);
                            } else if (lc_full_child_name.equals("vuze:drmkey")) {
                                result.setDrmKey(value);
                            } else if (lc_full_child_name.equals("vuze:assethash")) {
                                result.setHash(value);
                            }
                        }
                    }
                    if (!got_seeds_peers) {
                        try {
                            SimpleXMLParserDocumentNode desc_node = node.getChild("description");
                            if (desc_node != null) {
                                String desc = desc_node.getValue().trim();
                                Matcher m = seed_leecher_pat.matcher(desc);
                                while (m.find()) {
                                    String num = m.group(1);
                                    String type = m.group(2);
                                    if (type.toLowerCase().charAt(0) == 's') {
                                        result.setNbSeedsFromHTML(num);
                                    } else {
                                        result.setNbPeersFromHTML(num);
                                    }
                                }
                            }
                        } catch (Throwable e) {
                        }
                    }
                    String dlink = result.getDownloadLink();
                    if (dlink == null || dlink.length() == 0) {
                        String name = result.getName();
                        if (name != null) {
                            String magnet = UrlUtils.parseTextForMagnets(name);
                            if (magnet != null) {
                                result.setTorrentLink(magnet);
                            }
                        }
                    }
                    results.add(result);
                    if (absolute_max_matches >= 0 && results.size() == absolute_max_matches) {
                        break;
                    }
                }
            }
            Result[] res = (Result[]) results.toArray(new Result[results.size()]);
            debugLog("success: found " + res.length + " results");
            return (res);
        } catch (Throwable e) {
            debugLog("failed: " + Debug.getNestedExceptionMessageAndStack(e));
            if (e instanceof SearchException) {
                throw ((SearchException) e);
            }
            throw (new SearchException("RSS matching failed", e));
        }
    }
}