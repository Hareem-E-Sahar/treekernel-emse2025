package nhap.rep.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import nhap.NhapParams;
import nhap.rep.AbstractLocation;
import nhap.rep.Field;
import nhap.rep.Level;
import nhap.rep.Player;
import nhap.rep.Representation;
import nhap.rep.RepresentationFactory;
import nhap.utils.ErrorUtils;
import nhap.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import com.thoughtworks.xstream.XStream;

public class RepresentationManager {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(RepresentationManager.class);

    private Representation representation;

    private PlayerManager playerManager;

    private LevelManager levelManager;

    private boolean fPlayerChanged = true;

    public RepresentationManager(final NhapParams nhapParams) {
        representation = RepresentationFactory.getInstance(nhapParams);
        playerManager = new PlayerManager(representation.getPlayer());
        Level<Field> currentLevel = representation.getCurrentLevel();
        levelManager = new LevelManager(currentLevel);
    }

    public Representation getRepresentation() {
        return representation;
    }

    public void setRepresentation(Representation representation) {
        this.representation = representation;
    }

    public RepresentationManager(final Reader reader) {
        loadRepresentationFromXML(reader);
    }

    public RepresentationManager(final InputStream inputStream) {
        loadRepresentationFromXML(inputStream);
    }

    public RepresentationManager(final URL url) {
        loadRepresentation(url);
    }

    private void loadRepresentation(final URL url) {
        try {
            final URI uri = url.toURI();
            loadRepresentation(uri);
        } catch (final URISyntaxException eSyntaxException) {
            ErrorUtils.logAndThrowRuntimeException("Appearently URL " + url.toExternalForm() + " has an incorrect syntax", eSyntaxException);
        }
    }

    private void loadRepresentation(final URI uri) {
        final File file = new File(uri);
        final String filename = file.getName();
        if (StringUtils.endsWith(filename, ".zip")) {
            readFromZip(uri, file);
        } else if (StringUtils.endsWith(filename, ".xml")) {
            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(file);
                loadRepresentationFromXML(inputStream);
            } catch (final FileNotFoundException fileNotFoundException) {
                ErrorUtils.logAndThrowRuntimeException("The file specified in URI " + uri.toASCIIString() + " was not found", fileNotFoundException);
            } finally {
                IOUtils.close(inputStream, uri);
            }
        } else {
            ErrorUtils.logAndThrowRuntimeException("The file specified in URI " + uri.toASCIIString() + " has an incorrect suffix");
        }
        playerManager = new PlayerManager(representation.getPlayer());
        Level<Field> currentLevel = representation.getCurrentLevel();
        levelManager = new LevelManager(currentLevel);
    }

    private void readFromZip(final URI uri, final File file) {
        ZipInputStream zipInputStream = null;
        try {
            zipInputStream = new ZipInputStream(new FileInputStream(file));
            zipInputStream.getNextEntry();
            loadRepresentationFromXML(zipInputStream);
        } catch (final FileNotFoundException fileNotFoundException) {
            ErrorUtils.logAndThrowRuntimeException("The file specified in URI " + uri.toASCIIString() + " was not found", fileNotFoundException);
        } catch (final IOException exception) {
            ErrorUtils.logAndThrowRuntimeException(exception);
        } finally {
            if (zipInputStream != null) {
                try {
                    zipInputStream.closeEntry();
                    zipInputStream.close();
                } catch (final IOException exception) {
                    ErrorUtils.logAndThrowRuntimeException("Could not close URI " + uri.toASCIIString(), exception);
                }
            }
        }
    }

    public Player getPlayer() {
        return representation.getPlayer();
    }

    public Level<Field> getCurrentLevel() {
        return representation.getCurrentLevel();
    }

    public void toXML(final OutputStream outputStream) {
        final XStream xStream = new XStream();
        xStream.toXML(representation, outputStream);
    }

    public void toXML(final File file) {
        final String filename = file.getName();
        if (StringUtils.endsWith(filename, ".zip")) {
            ZipOutputStream zipOutputStream = null;
            try {
                zipOutputStream = new ZipOutputStream(new FileOutputStream(file));
                final ZipEntry zipEntry = new ZipEntry("representation.xml");
                zipOutputStream.putNextEntry(zipEntry);
                toXML(zipOutputStream);
            } catch (final FileNotFoundException fileNotFoundException) {
                ErrorUtils.logAndThrowRuntimeException("The file specified in URL " + file.getAbsolutePath() + " was not found", fileNotFoundException);
            } catch (final IOException exception) {
                ErrorUtils.logAndThrowRuntimeException(exception);
            } finally {
                if (zipOutputStream != null) {
                    try {
                        zipOutputStream.closeEntry();
                        zipOutputStream.close();
                    } catch (final IOException exception2) {
                        ErrorUtils.logAndThrowRuntimeException(exception2);
                    }
                }
            }
        }
    }

    public void loadRepresentationFromXML(final Reader reader) {
        final XStream xStream = new XStream();
        representation = (Representation) xStream.fromXML(reader);
    }

    public void loadRepresentationFromXML(final InputStream inputStream) {
        final XStream xStream = new XStream();
        representation = (Representation) xStream.fromXML(inputStream);
    }

    public void update() {
        if (fPlayerChanged || true) {
            playerManager.update();
            fPlayerChanged = false;
        }
        final Player player = representation.getPlayer();
        final AbstractLocation location = player.getLocation();
        levelManager.update(location);
    }

    public String currentLevelToString() {
        return getCurrentLevel().displayLevel();
    }
}
