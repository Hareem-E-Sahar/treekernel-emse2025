public class Test {    public void testHandler() throws IOException {
        assertTrue("This test can only be run once in a single JVM", imageHasNotBeenInstalledInThisJVM);
        URL url;
        ImageSource urlHandlerImageSource = (ImageSource) new ClassPathXmlApplicationContext("org/springframework/richclient/image/application-context.xml").getBean("imageSource");
        try {
            url = new URL("image:test");
            imageHasNotBeenInstalledInThisJVM = false;
        } catch (MalformedURLException e) {
            fail("protocol was not installed");
        }
        url = new URL("image:image.that.does.not.exist");
        try {
            url.openConnection();
            fail();
        } catch (NoSuchImageResourceException e) {
        }
        url = new URL("image:test.image.key");
        url.openConnection();
    }
}