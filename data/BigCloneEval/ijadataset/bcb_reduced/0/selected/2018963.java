package net.sourceforge.pebble.decorator;

import net.sourceforge.pebble.domain.BlogEntry;
import net.sourceforge.pebble.domain.Blog;
import net.sourceforge.pebble.domain.StaticPage;
import net.sourceforge.pebble.api.decorator.ContentDecoratorContext;
import org.radeox.api.engine.RenderEngine;
import org.radeox.api.engine.WikiRenderEngine;
import org.radeox.api.engine.context.InitialRenderContext;
import org.radeox.api.engine.context.RenderContext;
import org.radeox.engine.BaseRenderEngine;
import org.radeox.engine.context.BaseInitialRenderContext;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Decorates blog entries and comments by rendering them with Radeox, internal
 * links pointing to static pages within the blog.
 *
 * @author Simon Brown
 */
public class RadeoxDecorator extends ContentDecoratorSupport {

    private static final String WIKI_START_TAG = "<wiki>";

    private static final String WIKI_END_TAG = "</wiki>";

    /**
   * Decorates the specified blog entry.
   *
   * @param context   the context in which the decoration is running
   * @param blogEntry the blog entry to be decorated
   */
    public void decorate(ContentDecoratorContext context, BlogEntry blogEntry) {
        InitialRenderContext initialContext = new BaseInitialRenderContext();
        initialContext.set(RenderContext.INPUT_LOCALE, getBlog().getLocale());
        RenderEngine engineWithContext = new RadeoxWikiRenderEngine(initialContext, getBlog());
        blogEntry.setExcerpt(wikify(blogEntry.getExcerpt(), engineWithContext, initialContext));
        blogEntry.setBody(wikify(blogEntry.getBody(), engineWithContext, initialContext));
    }

    /**
   * Decorates the specified static page.
   *
   * @param context    the context in which the decoration is running
   * @param staticPage the static page to be decorated
   */
    public void decorate(ContentDecoratorContext context, StaticPage staticPage) {
        InitialRenderContext initialContext = new BaseInitialRenderContext();
        initialContext.set(RenderContext.INPUT_LOCALE, getBlog().getLocale());
        RenderEngine engineWithContext = new RadeoxWikiRenderEngine(initialContext, getBlog());
        staticPage.setBody(wikify(staticPage.getBody(), engineWithContext, initialContext));
    }

    private String wikify(String content, RenderEngine renderEngine, InitialRenderContext renderContext) {
        if (content == null || content.length() == 0) {
            return "";
        }
        Pattern p = Pattern.compile(WIKI_START_TAG + ".+?" + WIKI_END_TAG, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Matcher m = p.matcher(content);
        while (m.find()) {
            int start = m.start();
            int end = m.end();
            String textToWikify = content.substring(start, end);
            textToWikify = textToWikify.substring(WIKI_START_TAG.length(), textToWikify.length() - WIKI_END_TAG.length());
            textToWikify = renderEngine.render(textToWikify, renderContext);
            content = content.substring(0, start) + textToWikify + content.substring(end, content.length());
            m = p.matcher(content);
        }
        return content;
    }
}

class RadeoxWikiRenderEngine extends BaseRenderEngine implements WikiRenderEngine {

    private Blog blog;

    public RadeoxWikiRenderEngine(InitialRenderContext context, Blog blog) {
        super(context);
        context.setRenderEngine(this);
        this.blog = blog;
    }

    public boolean exists(String name) {
        return blog.getStaticPageIndex().contains(name);
    }

    public boolean showCreate() {
        return true;
    }

    public void appendLink(StringBuffer buffer, String name, String view) {
        appendLink(buffer, name, view, null);
    }

    public void appendLink(StringBuffer buffer, String name, String view, String anchor) {
        buffer.append("<a href=\"");
        buffer.append(blog.getUrl() + "pages/" + name + ".html");
        if (anchor != null && anchor.trim().length() > 0) {
            buffer.append("#");
            buffer.append(anchor);
        }
        buffer.append("\">");
        buffer.append(view);
        buffer.append("</a>");
    }

    public void appendCreateLink(StringBuffer buffer, String name, String view) {
        buffer.append("<a href=\"addStaticPage.secureaction?name=");
        buffer.append(name);
        buffer.append("\">");
        buffer.append(view);
        buffer.append("</a><sup>?</sup>");
    }
}
