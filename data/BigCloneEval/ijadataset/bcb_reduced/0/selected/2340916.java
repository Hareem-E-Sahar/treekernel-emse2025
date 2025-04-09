package com.bluebrim.solitarylayouteditor;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import javax.imageio.spi.IIORegistry;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;
import org.apache.batik.util.gui.MemoryMonitor;
import org.springframework.core.io.ClassPathResource;
import com.bluebrim.base.shared.CoFactoryManager;
import com.bluebrim.base.shared.CoLocalFactoryManager;
import com.bluebrim.base.shared.CoNamed;
import com.bluebrim.base.shared.CoStatusShower;
import com.bluebrim.base.shared.debug.CoAssertion;
import com.bluebrim.compositecontent.client.CoCompositeContentClient;
import com.bluebrim.content.impl.server.CoCompositeContentServerImpl;
import com.bluebrim.font.client.CoFontClient;
import com.bluebrim.font.impl.client.CoFontRepositoryManager;
import com.bluebrim.font.impl.server.CoFontServerImpl;
import com.bluebrim.font.shared.CoAbstractFontMapper;
import com.bluebrim.font.shared.CoFontException;
import com.bluebrim.font.shared.CoFontRepositoryIF;
import com.bluebrim.font.shared.CoFontServer;
import com.bluebrim.font.shared.CoFontServerProvider;
import com.bluebrim.gui.client.CoFrame;
import com.bluebrim.gui.client.CoGUI;
import com.bluebrim.gui.client.CoSplashScreen;
import com.bluebrim.gui.client.CoUIConstants;
import com.bluebrim.image.client.CoImageClient;
import com.bluebrim.image.impl.server.CoLocalImageClient;
import com.bluebrim.image.impl.shared.CoEpsImageReaderSpi;
import com.bluebrim.layout.client.CoLayoutClient;
import com.bluebrim.layout.impl.client.editor.CoLayoutEditor;
import com.bluebrim.layout.impl.client.editor.CoLayoutEditorClientConstants;
import com.bluebrim.layout.impl.client.editor.CoLayoutEditorConfiguration;
import com.bluebrim.layout.impl.client.editor.CoMenuExtender;
import com.bluebrim.layout.impl.server.CoLayoutServerImpl;
import com.bluebrim.layout.impl.server.CoPageItemFactories;
import com.bluebrim.layout.impl.server.CoPageItemPreferences;
import com.bluebrim.layout.impl.shared.CoPageItemPreferencesIF;
import com.bluebrim.layout.shared.CoLayoutParameters;
import com.bluebrim.menus.client.CoMenuBar;
import com.bluebrim.menus.client.CoMenuBuilder;
import com.bluebrim.menus.client.CoSubMenu;
import com.bluebrim.page.shared.CoPageSizeIF;
import com.bluebrim.page.shared.CoPageSizeRegistry;
import com.bluebrim.resource.shared.CoResourceLoader;
import com.bluebrim.text.client.CoTextClient;
import com.bluebrim.text.impl.server.CoTextServerImpl;
import com.bluebrim.text.shared.CoTextServerProvider;
import com.bluebrim.xml.shared.CoXmlConsumer;
import com.bluebrim.xml.shared.CoXmlContext;
import com.bluebrim.xml.shared.CoXmlReadException;

/**
 * Starts <code>CoLayoutEditor</code> as a standalone application with
 * file system persistance.
 * 
 * @author G�ran St�ck 2002-09-12
 *
 */
public class CoSolitaryLayoutEditor implements CoNamed {

    public static final String RELEASE_NOTES_URL = "CoSolitaryLayoutEditor.RELEASE_NOTES_URL";

    private static JFrame m_releaseNotesWindow;

    private List m_layoutDocuments = new ArrayList();

    private int m_untitledNumber = 1;

    private CoFileStoreSupport fileStoreSupport;

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
        Locale.setDefault(Locale.ENGLISH);
        CoAssertion.SIMULATION_SUPPORT = true;
        URL splashUrl = CoSolitaryLayoutEditor.class.getResource("/BlueBrimLogo.png");
        if (splashUrl == null) throw new RuntimeException("Can't find resource /BlueBrimLogo.png");
        Icon splashImage = new ImageIcon(splashUrl);
        CoSplashScreen splash = new CoSplashScreen(splashImage, CoUIConstants.BLUEBRIM_YELLOW);
        splash.toFront();
        splash.setVisible(true);
        CoSolitaryLayoutEditor app = new CoSolitaryLayoutEditor(splash);
        app.run(splash);
        splash.close();
    }

    public CoSolitaryLayoutEditor(CoSplashScreen splash) {
        CoFactoryManager factoryManager = new CoLocalFactoryManager();
        CoFactoryManager.setInstance(factoryManager);
        new CoPageItemFactories().init(true);
        addSystemProperties();
        initSingletons(splash);
        installFonts(splash);
        IIORegistry.getDefaultInstance().registerServiceProvider(new CoEpsImageReaderSpi());
        fileStoreSupport = new CoFileStoreSupport(getZipDocumentFileFilter(), true);
    }

    private void run(CoStatusShower shower) {
        createNewDocument(shower);
    }

    private final void initSingletons(CoStatusShower shower) {
        shower.showStatus("Initialize font server");
        CoFontServer fontServer = new CoFontServerImpl();
        CoFontServerProvider.setFontServer(fontServer);
        CoFontClient.setFontServer(fontServer);
        shower.showStatus("Initialize image server");
        CoImageClient.setInstance(new CoLocalImageClient());
        shower.showStatus("Initialize layout server");
        CoLayoutServerImpl layoutServer = new CoLayoutServerImpl();
        CoLayoutServerImpl.setInstance(layoutServer);
        CoLayoutClient.setLayoutServer(layoutServer);
        shower.showStatus("Initialize text server");
        CoTextServerImpl textServer = new CoTextServerImpl();
        CoTextServerProvider.setTextServer(textServer);
        CoTextClient.setTextServer(textServer);
        shower.showStatus("Initialize default layout preferences");
        CoPageItemPreferences defaultPageItemPreferences = createDefaultPageItemPreferences();
        layoutServer.setDefaultLayoutParameters(defaultPageItemPreferences);
        textServer.setDefaultStyledTextPreferences(defaultPageItemPreferences);
        CoCompositeContentServerImpl compositeContentServer = new CoCompositeContentServerImpl();
        CoCompositeContentClient.setCompositeContentServer(compositeContentServer);
    }

    private CoPageItemPreferences createDefaultPageItemPreferences() {
        CoPageItemPreferences defaultPageItemPreferences = new CoPageItemPreferences();
        defaultPageItemPreferences.addRGBColors();
        defaultPageItemPreferences.addCMYKColors();
        defaultPageItemPreferences.addDefaultStrokes();
        Iterator i = CoAbstractFontMapper.getFontMapper().getFontFamilies().iterator();
        while (i.hasNext()) {
            defaultPageItemPreferences.addFontFamily((String) i.next());
        }
        return defaultPageItemPreferences;
    }

    private CoLayoutEditorConfiguration createMainEditorConfiguration(CoLayoutDocument layoutDocument) {
        CoLayoutEditorConfiguration configuration = CoLayoutEditorConfiguration.EDITORIAL_INSTANCE.copy();
        configuration.setMenuExtender(createMenuExtender(layoutDocument));
        layoutDocument.addUIsToConfiguration(configuration);
        return configuration;
    }

    /**
	 * The page size that is applied on new documents. The user should be able to change this
	 * in the application preferences dialog.
	 */
    public CoPageSizeIF getDefaultPageSize() {
        return CoLayoutServerImpl.getInstance().getDefaultLayoutParameters().getPageSizeCollection().getPageSizeByName(CoPageSizeIF.A4);
    }

    /**
	 * Shows a filebrowser and let the user pick a file to import.
	 * The file returned from file stored support is the main file in the zip
	 * archive along with the attachement files. The files is placed in a
	 * temporary directory that is removed if something goes wrong.
	 */
    public void loadDocumentFromFile(CoFrame parent) throws IOException {
        File file = fileStoreSupport.chooseZipFileForLoading(parent, CoLayoutDocument.NAME_IN_ZIP_ARCHIVE);
        if (file != null) {
            CoXmlConsumer consumer = new CoXmlConsumer(createXmlContext(file));
            Object importedXml = null;
            InputStream stream = null;
            try {
                stream = new BufferedInputStream(new FileInputStream(file));
            } catch (FileNotFoundException e) {
                CoGUI.error("Hittar inte: " + file.getName(), parent);
                stream.close();
                deleteDirectory(file.getParentFile());
                return;
            }
            try {
                importedXml = consumer.readModelFrom(stream);
            } catch (CoXmlReadException e) {
                CoGUI.error("Fel vid l�sning av: " + fileStoreSupport.getFileName() + e.getMessage(), parent);
                stream.close();
                deleteDirectory(file.getParentFile());
                return;
            }
            if (importedXml != null) {
                CoLayoutDocument layoutDocument = (CoLayoutDocument) importedXml;
                layoutDocument.setFileStoreSupport(fileStoreSupport);
                openEditor(layoutDocument);
            }
            stream.close();
            deleteDirectory(file.getParentFile());
        }
    }

    /**
	 * Delete all files in the directory and the directory as well
	 */
    private void deleteDirectory(File dir) {
        if (!dir.isDirectory()) return;
        File[] files = dir.listFiles();
        for (int i = 0; i < files.length; i++) {
            files[i].delete();
        }
        ;
        dir.delete();
    }

    private CoXmlContext createXmlContext(File file) {
        CoPageItemPreferencesIF pageItemPreferences = (CoPageItemPreferencesIF) CoLayoutServerImpl.getInstance().getDefaultLayoutParameters();
        Object[][] contextObjects = { { CoSolitaryLayoutEditor.class, this }, { CoLayoutParameters.class, pageItemPreferences }, { CoPageSizeRegistry.class, pageItemPreferences.getPageSizeCollection() } };
        CoXmlContext xmlContext = new CoXmlContext(contextObjects);
        xmlContext.setUseGOI(false);
        xmlContext.setFileNameAndPath(file.getPath());
        return xmlContext;
    }

    private CoMenuExtender createMenuExtender(final CoLayoutDocument document) {
        return new CoMenuExtender() {

            public void extendMenu(CoMenuBar menuBar, CoMenuBuilder builder) {
                CoSubMenu fileMenu = (CoSubMenu) menuBar.getMenu(0);
                fileMenu.remove(0);
                fileMenu.remove(0);
                fileMenu.remove(0);
                JMenuItem mi;
                mi = fileMenu.insertBefore(CoLayoutEditorClientConstants.MENU_FILE_PRINT_SETTINGS, CoLayoutEditorClientConstants.MENU_FILE_NEW_DOCUMENT);
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ev) {
                        createNewDocument(null);
                    }
                });
                document.extendMenu(menuBar, builder);
                fileMenu.addSeparator();
                mi = fileMenu.add(CoLayoutEditorClientConstants.MENU_FILE_EXIT);
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ev) {
                        Iterator iter = m_layoutDocuments.iterator();
                        while (iter.hasNext()) {
                            CoLayoutDocument doc = (CoLayoutDocument) iter.next();
                            if (!doc.saveIfUserWantsOrCancel()) return;
                        }
                        ;
                        System.exit(0);
                    }
                });
                CoSubMenu helpMenu = (CoSubMenu) menuBar.getMenu(8);
                mi = helpMenu.add(CoSolitaryLayoutEditorConstants.MENU_HELP_RELEASE_NOTES);
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ev) {
                        showReleaseNotes();
                    }
                });
                mi = helpMenu.add(new JMenuItem("Show memory monitor"));
                mi.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent ev) {
                        showMemoryMonitor();
                    }
                });
            }
        };
    }

    /**
	 * Creates a new layout document with one master page and one page
	 * with two layers where one of the layers is the master page.
	 */
    private void createNewDocument(CoStatusShower shower) {
        if (shower != null) shower.showStatus("Create new empty layout");
        CoLayoutDocument layoutDocument = new CoLayoutDocument(this);
        layoutDocument.createAndAddMasterPage(getDefaultPageSize());
        layoutDocument.createAndAddPage(getDefaultPageSize());
        layoutDocument.setFileStoreSupport(fileStoreSupport);
        if (shower != null) shower.showStatus("Open layout editor");
        openEditor(layoutDocument);
    }

    private void openEditor(CoLayoutDocument layoutDocument) {
        layoutDocument.openEditor(createMainEditorConfiguration(layoutDocument));
        m_layoutDocuments.add(layoutDocument);
    }

    /**
	 * Is called by the document when the user close the documents window.
	 */
    public void isClosing(CoLayoutDocument layoutDocument) {
        m_layoutDocuments.remove(layoutDocument);
        if (m_layoutDocuments.size() < 1) System.exit(0);
    }

    public String getName() {
        return "frist�ende";
    }

    public int getUntitledNumber() {
        return m_untitledNumber++;
    }

    private FileFilter getDocumentFileFilter() {
        return new FileFilter() {

            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                String name = file.getName().toLowerCase();
                return name.endsWith(".xml");
            }

            public String getDescription() {
                return "XML";
            }
        };
    }

    private FileFilter getZipDocumentFileFilter() {
        return new FileFilter() {

            public boolean accept(File file) {
                if (file.isDirectory()) return true;
                ZipFile zipFile;
                try {
                    zipFile = new ZipFile(file);
                } catch (ZipException e) {
                    return false;
                } catch (IOException e) {
                    return false;
                }
                return (zipFile.getEntry(CoLayoutDocument.NAME_IN_ZIP_ARCHIVE) != null);
            }

            public String getDescription() {
                return "Layout editor document";
            }
        };
    }

    private void addSystemProperties() {
        System.setProperty(CoFontRepositoryIF.PROPERTY_KEY_FONT_PATH, getJavaFontDir().getPath());
    }

    private void showReleaseNotes() {
        URL url = CoResourceLoader.getURL(CoSolitaryLayoutEditor.class, CoSolitaryLayoutEditorResources.getName(RELEASE_NOTES_URL));
        CoLayoutEditor.showHTMLwindow(url, m_releaseNotesWindow, CoGUI.centerOnScreen(800, Integer.MAX_VALUE));
    }

    private void showMemoryMonitor() {
        new MemoryMonitor().setVisible(true);
    }

    private void installFonts(CoStatusShower shower) {
        CoFontRepositoryManager manager = CoFontRepositoryManager.getSingleton();
        installFonts(shower, manager, getJavaFontDir());
    }

    private void installFonts(CoStatusShower shower, CoFontRepositoryManager manager, File fontDir) {
        if (fontDir == null) return;
        File[] fonts = fontDir.listFiles();
        for (int i = 0; i < fonts.length; ++i) {
            File file = fonts[i];
            if (file.isDirectory()) continue;
            try {
                manager.installFontFile(file, shower);
            } catch (RuntimeException e) {
                e.printStackTrace();
                System.out.println("Unable to install font " + file.getName() + " " + e);
            } catch (CoFontException e) {
                e.printStackTrace();
                System.out.println("Unable to install font " + file.getName() + " " + e);
            }
        }
        ;
    }

    private File getJavaFontDir() {
        String javaHome = System.getProperty("java.home");
        return new File(javaHome, "lib/fonts");
    }

    private void loadFonts() {
        File fontsDir = null;
        try {
            fontsDir = new ClassPathResource("fonts.ini").getFile().getParentFile();
        } catch (IOException e) {
            throw new RuntimeException("Error when trying to read the fonts.ini file", e);
        }
        File[] files = fontsDir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".ttf");
            }
        });
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        for (int i = 0; i < files.length; i++) {
            try {
                try {
                    Font font = Font.createFont(Font.TRUETYPE_FONT, files[i]);
                    ge.registerFont(font);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException("There is no file: " + files[i].getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException("Error when reading: " + files[i].getAbsolutePath());
                }
            } catch (FontFormatException e) {
                System.out.println(files[i].getPath() + " is no true type font file");
            }
        }
    }

    private void listAllFonts() {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        Font[] fonts = ge.getAllFonts();
        for (int i = 0; i < fonts.length; i++) {
            System.out.println(fonts[i].getFontName(Locale.ENGLISH));
        }
    }
}
