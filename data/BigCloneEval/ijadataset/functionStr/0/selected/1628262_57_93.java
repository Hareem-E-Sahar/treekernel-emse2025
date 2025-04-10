public class Test {    protected void createPage(WebContext context) {
        logo = new A(new H1(new Plain("Annone")));
        logo.setHRef("/?channelId=" + context.getChannel().getId());
        logo.setId("logo");
        A optionsLink = new A(new Plain(Text.get(context.getLocale(), "Options")));
        optionsLink.setHRef("/o");
        A helpLink = new A(new Plain(Text.get(context.getLocale(), "Help")));
        helpLink.setHRef("/h");
        options = new DIV(optionsLink, new Nbsp(), new Plain("|"), new Nbsp(), helpLink);
        options.setId("options");
        user = new DIV(new Plain(Text.get(context.getLocale(), "User")));
        user.setId("user");
        header = new DIV();
        content = new DIV();
        extra = new DIV();
        footer = new DIV();
        container = new DIV();
        header.setId("header");
        content.setId("content");
        extra.setId("extra");
        footer.setId("footer");
        container.setId("container");
        header.add(logo, options, user);
        footer.add(new Plain(Text.get(context.getLocale(), "Copyright © 2010 theCar")));
        container.add(header, content, extra, footer);
        title = new TITLE();
        head = new HEAD(title, new LINK("stylesheet", "/c"));
        addScript("/scripts/prototype.js");
        addScript("/scripts/controls/Control.js");
        addScript("/scripts/controls/Value.js");
        addScript("/scripts/controls/Box.js");
        addScript("/scripts/controls/Edit.js");
        body = new BODY(container);
        html = new HTML(head, body);
        html.setDoctype(HTML.HTML_4_01_STRICT);
        html.setLang(context.getLocale().getLanguage());
    }
}