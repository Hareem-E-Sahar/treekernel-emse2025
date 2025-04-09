package com.bluesky.javawebbrowser.domain.html.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.log4j.Logger;
import org.jgroups.util.GetNetworkInterfaces1_4;
import com.bluesky.javawebbrowser.domain.html.tags.Tag;
import com.bluesky.javawebbrowser.domain.html.tags.TagType;
import com.bluesky.javawebbrowser.domain.html.tags.TextBlock;
import com.bluesky.javawebbrowser.domain.html.tags.Tag.TagProcessor;

public class RegexHtmlParser implements HtmlParser {

    private static Logger logger = Logger.getLogger(RegexHtmlParser.class);

    Pattern pTagHeadOrFoot = Pattern.compile("((<!\\[CDATA\\[.*?\\]\\]>)|(<!--.*?-->)|(<!\\w+.*?>)|(</?[\\s\\t\\r\\n]*(\\w+)([\\s\\t\\r\\n]+[\\w_:-]+[\\s\\t\\r\\n]*(=(([^\\s]*?)|(\"[^\"]*?\")|('[^']*?')))?)*[\\s\\t\\r\\n]*/?>))", Pattern.DOTALL);

    private int tagCount;

    private int warnningCount;

    private int errorCount;

    public ParseResult parse(String html, boolean autoUpgrade) {
        tagCount = 0;
        errorCount = 0;
        warnningCount = 0;
        ParseResult pr = new ParseResult();
        int index = parseTag(html, pr.getRoot());
        String remainText = html.substring(index);
        if (!remainText.trim().isEmpty()) {
            Tag t = new TextBlock(remainText);
            pr.getRoot().addChild(t);
        }
        if (autoUpgrade) {
            pr.getRoot().traverse(new TagProcessor() {

                @Override
                public boolean process(Tag tag) {
                    tag.upgrade();
                    return false;
                }
            });
        }
        pr.setWarnningCount(warnningCount);
        pr.setErrorCount(errorCount);
        pr.setTagCount(tagCount);
        return pr;
    }

    private String getNearSegment(String html, int index) {
        return html.substring(0, index);
    }

    private int parseTag(String html, Tag tag) {
        logger.debug("Step In at:" + tag.getTagType());
        int curIndex = 0;
        Matcher mTagHeadOrFoot;
        if (tag.getTagType().isOneTextChildOnly()) {
            String sPattern = String.format("<\\s*/\\s*%s\\s*>", tag.getTagName());
            Pattern pTheTagFoot = Pattern.compile(sPattern, Pattern.CASE_INSENSITIVE);
            mTagHeadOrFoot = pTheTagFoot.matcher(html);
        } else mTagHeadOrFoot = pTagHeadOrFoot.matcher(html);
        while (mTagHeadOrFoot.find()) {
            tagCount++;
            logger.debug("TAG_NUM:" + tagCount);
            String textBlock = html.substring(curIndex, mTagHeadOrFoot.start());
            if (textBlock.trim().length() > 0) {
                Tag newTag = new Tag(TagType.TEXT_BLOCK);
                newTag.setBody(textBlock);
                tag.addChild(newTag);
                logger.info("[TXT BODY]" + Tag.indents(tag.getDepth() - 1, "  ") + Tag.removeCR(textBlock));
                if (textBlock.matches("<.*>")) {
                    warnningCount++;
                    logger.warn("**problem text block[" + textBlock + "] near:" + getNearSegment(html, curIndex));
                }
            }
            String tagHeadOrFoot = mTagHeadOrFoot.group(0);
            logger.debug("tag:" + tagHeadOrFoot);
            if (!Tag.isStartTag(tagHeadOrFoot)) {
                String previousTagType = tag.getTagName();
                String currentTagType = Tag.extractTagName(tagHeadOrFoot);
                if (!previousTagType.equalsIgnoreCase(currentTagType)) {
                    warnningCount++;
                    String previousParentTagType = "";
                    try {
                        previousParentTagType = ((Tag) tag.getParent()).getTagName();
                    } catch (NullPointerException ex) {
                    }
                    logger.warn("***** tag expect:" + previousTagType + " but meet: " + currentTagType);
                    if (previousParentTagType.equalsIgnoreCase(currentTagType)) {
                        String fakeFoot = "</" + tag.getTagName() + ">";
                        logger.warn("[TAG FOOT]" + Tag.indents(tag.getDepth() - 1, "$$") + fakeFoot + "(AutoGenerate) near:" + getNearSegment(html, curIndex));
                        curIndex = mTagHeadOrFoot.start();
                        tag.setFoot(fakeFoot);
                        break;
                    } else {
                        logger.warn("[TAG FOOT]" + Tag.indents(tag.getDepth() - 1, "$$") + "</" + currentTagType + "> (AutoRemove), near:" + getNearSegment(html, curIndex));
                        curIndex = mTagHeadOrFoot.end();
                    }
                } else {
                    curIndex = mTagHeadOrFoot.end();
                    logger.info("[TAG FOOT]" + Tag.indents(tag.getDepth() - 1, "  ") + tagHeadOrFoot);
                    tag.setFoot(tagHeadOrFoot);
                    break;
                }
            } else {
                logger.info("[TAG HEAD]" + Tag.indents(tag.getDepth(), "  ") + Tag.removeCR(tagHeadOrFoot));
                Tag newTag = new Tag(tagHeadOrFoot);
                tag.addChild(newTag);
                if (newTag.getTagType().isSolo() || newTag.isStartAndEndTag()) {
                    logger.debug("TAG HAS NO BODY:" + newTag.getTagName());
                    curIndex = mTagHeadOrFoot.end();
                    continue;
                } else {
                    curIndex = mTagHeadOrFoot.end() + parseTag(html.substring(mTagHeadOrFoot.end()), newTag);
                    String content = html.substring(mTagHeadOrFoot.start(), curIndex);
                    newTag.setContent(content);
                    mTagHeadOrFoot.region(curIndex, html.length());
                }
            }
        }
        logger.debug("Step Out at:" + tag.getTagType());
        return curIndex;
    }

    public static void main(String[] args) {
        HtmlParser parser = new RegexHtmlParser();
        String lineBreakTag = "<A\n href=\"http://code.google.com/p/jack-lab/downloads/list\">google code1</a>";
        ParseResult result = parser.parse(lineBreakTag, false);
        System.out.println(result.getRoot().toHtml());
        Pattern pTagHeadOrFoot = Pattern.compile("((<!\\[CDATA\\[.*?\\]\\]>)|(<!--.*?-->)|(<!\\w+.*?>)|(</?[\\s\\t\\r\\n]*(\\w+)([\\s\\t\\r\\n]+[\\w_:-]+[\\s\\t\\r\\n]*(=(([^\\s]*?)|(\"[^\"]*?\")|('[^']*?')))?)*[\\s\\t\\r\\n]*/?>))", Pattern.DOTALL);
        String attributeValue = "[\\w\\s`~!@#$%^&*()-=_+\\[\\]\\{}|;':,./<>?#10#13]";
        String all = "[\\w\\s`~!@#$%^&*()-=_+\\[\\]\\{}|;':\",./<>?]";
        Pattern pTagHead = Pattern.compile("(<!--.*-->)|(<\\s*([\\w]+)(\\s+\\w+=((" + attributeValue + "+)|(\"" + attributeValue + "+\")))*\\s*/?>)");
        Pattern pTagFoot = Pattern.compile("</\\w+>");
        pTagHeadOrFoot = Pattern.compile("((<!--.*?-->)|(<!\\w+.*?>)|(</?\\s*(\\w+)(\\s+[\\w_:-]+(=(([^\\s]*?)|(\"[^\"]*?\")|('[^']*?')))?)*\\s*/?>))", Pattern.DOTALL);
        pTagHeadOrFoot = Pattern.compile("((<!--.*?-->)|(<!\\w+.*?>)|(</?\\s*(\\w+)(\\s+[\\w_:-]+\\s*(=(([^\\s]*?)|(\"[^\"]*?\")|('[^']*?')))?)*\\s*/?>))");
        Pattern pTagAttribute = Pattern.compile("\\s+(\\w+)=((\\w*)|\"(" + attributeValue + "*)\"|'(" + attributeValue + "*)')");
        Pattern pTagAttributeValue = Pattern.compile("(" + attributeValue + "+)");
        String t = "<link rel=\"stylesheet\" href=\"/w/index.php?title=MediaWiki:Common.css&amp;usemsgcache=yes&amp;ctype=text%2Fcss&amp;smaxage=2678400&amp;action=raw&amp;maxage=2678400\" type=\"text/css\" />";
        t = "<body color='red' onload=\"load(); a<1; B>2; ?c++&&\" a=1 b=\"2\"><script>";
        t = "<span\n dir='ltr' c=#123 b><a	href=\"/wiki/Category:Formal_languages\"	title=\"Category:Formal languages\">";
        t = "| <a\r\nhref=\"11.html\">aa</a>";
        t = "<a href=\"http://www.nytimes.com/2009/04/22/opinion/22friedman.html\">Friedman: Education and the Economy</a> | <a" + "\nhref=\"http://community.nytimes.com/article/comments/2009/04/22/opinion/22friedman.html\"><IMG src=\"http://graphics8.nytimes.com/images/section/opinion/comment_icon.gif\" width=\"9\" height=\"11\" class=\"inTextImage\" alt=\"\" /> <span class=\"commentText\">Comments</span> </span></a>";
        t = "<form action=\"#\" onsubmit=\"return false;\" target =\"_blank\" method=\"POST\" style=\"margin:0px;\">";
        Matcher m = pTagHeadOrFoot.matcher(t);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            System.out.println("m1:" + m.groupCount());
            Matcher m2 = pTagAttribute.matcher(m.group(0));
            while (m2.find()) {
                System.out.println("\t" + m2.groupCount());
                for (int i2 = 0; i2 < m2.groupCount(); i2++) {
                    System.out.println("\t:" + m2.group(i2));
                }
                Matcher m3 = pTagAttributeValue.matcher(m2.group(2));
                while (m3.find()) {
                    System.out.println("\t\t" + m3.groupCount());
                    for (int i3 = 0; i3 < m3.groupCount(); i3++) {
                        System.out.println("\t\t:" + m3.group(i3));
                    }
                }
            }
            for (int i = 0; i < m.groupCount(); i++) {
                System.out.println("group(" + i + "):" + m.group(i) + "\n");
            }
        }
        System.out.println(sb.toString());
    }
}
