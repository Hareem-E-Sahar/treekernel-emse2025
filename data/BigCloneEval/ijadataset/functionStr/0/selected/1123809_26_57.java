public class Test {    public void setUp() throws Exception {
        File root = new File(System.getProperty("java.io.tmpdir"), "EditControllerTest");
        File subdir = new File(root, "subdir");
        subdir.mkdirs();
        File photo = new File(System.getProperty("project.root"), "build/test/exif-nordf.jpg");
        FileUtils.copyFileToDirectory(photo, root);
        FileUtils.copyFileToDirectory(photo, subdir);
        path1 = "/exif-nordf.jpg";
        path2 = "/subdir/exif-nordf.jpg";
        FileSystem filesystem = new FileSystemImpl();
        filesystem.setRoot(root);
        persister = new PersisterImpl();
        persister.setFilesystem(filesystem);
        persister.setTranslator(new Translator());
        FileSystemBrowser browser = new FileSystemBrowser();
        browser.setFilesystem(filesystem);
        browser.setPersister(persister);
        File index = new File(System.getProperty("java.io.tmpdir"), "test-index");
        SearchIndex searchIndex = new SearchIndex(index);
        Searcher searcher = new Searcher();
        searcher.setIndex(searchIndex);
        controller = new EditController();
        controller.setFilesystem(filesystem);
        controller.setPersister(persister);
        controller.setSearcher(searcher);
        StaticApplicationContext context = new StaticApplicationContext();
        context.registerSingleton(StaticApplicationContext.MESSAGE_SOURCE_BEAN_NAME, StaticMessageSource.class, null);
        context.refresh();
        context.addMessage("format.dateTime", Locale.US, "MM/dd/yyyy hh:mm aaa");
        context.addMessage("message.edit.success", Locale.US, "success");
        controller.setApplicationContext(context);
    }
}