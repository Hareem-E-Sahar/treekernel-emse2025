package nz.ac.waikato.mcennis.rat.dataAquisition;

import com.ice.tar.TarEntry;
import com.ice.tar.TarInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;
import nz.ac.waikato.mcennis.rat.graph.Graph;
import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptorFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptor;
import nz.ac.waikato.mcennis.rat.graph.descriptors.IODescriptor.Type;
import nz.ac.waikato.mcennis.rat.graph.descriptors.Parameter;
import nz.ac.waikato.mcennis.rat.graph.descriptors.ParameterFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.ParameterInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.Properties;
import nz.ac.waikato.mcennis.rat.graph.descriptors.PropertiesFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.PropertiesInternal;
import nz.ac.waikato.mcennis.rat.graph.descriptors.SyntaxCheckerFactory;
import nz.ac.waikato.mcennis.rat.graph.descriptors.SyntaxObject;
import nz.ac.waikato.mcennis.rat.graph.model.ModelShell;
import nz.ac.waikato.mcennis.rat.parser.Parser;
import nz.ac.waikato.mcennis.rat.parser.xmlHandler.LastFMUserExpansion;

/**

 * Class for parsing a (possibly compressed) tar file containing the files created

 * by the CrawlLastFM module.

 * 

 * @author Daniel McEnnis

 */
public class ReadLastFMProfileFile extends ModelShell implements DataAquisition {

    PropertiesInternal properties = PropertiesFactory.newInstance().create();

    LinkedList<IODescriptor> input = new LinkedList<IODescriptor>();

    LinkedList<IODescriptor> output = new LinkedList<IODescriptor>();

    Graph graph = null;

    public ReadLastFMProfileFile() {
        ParameterInternal name = ParameterFactory.newInstance().create("AlgorithmClass", String.class);
        SyntaxObject syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("Read LastFM Profile File");
        properties.add(name);
        name = ParameterFactory.newInstance().create("Name", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, Integer.MAX_VALUE, null, String.class);
        name.setRestrictions(syntax);
        name.add("Read LastFM Profile File");
        properties.add(name);
        name = ParameterFactory.newInstance().create("Category", String.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, String.class);
        name.setRestrictions(syntax);
        name.add("File Reader");
        properties.add(name);
        name = ParameterFactory.newInstance().create("ProfileFile", File.class);
        syntax = SyntaxCheckerFactory.newInstance().create(1, 1, null, File.class);
        name.setRestrictions(syntax);
        properties.add(name);
    }

    @Override
    public void start() {
        TarInputStream input = null;
        int count = 1;
        try {
            input = new TarInputStream(new GZIPInputStream(new FileInputStream((File) properties.get("ProfileFile").get())));
            LastFMUserExpansion userExpansion = new LastFMUserExpansion();
            Parser[] parsers = userExpansion.setUpParsers(null, graph);
            TarEntry entry = input.getNextEntry();
            while (entry != null) {
                if (count % 1000 == 0) {
                    Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.INFO, "Reading file " + count);
                }
                String name = entry.getName();
                String[] pieces = name.split("/");
                if (!entry.isDirectory() && pieces.length > 1) {
                    ByteArrayOutputStream copyOfFile = new ByteArrayOutputStream();
                    input.copyEntryContents(copyOfFile);
                    ByteArrayInputStream data = new ByteArrayInputStream(copyOfFile.toByteArray());
                    if (pieces[0].contentEquals("ArtistDirectory")) {
                        try {
                            parsers[4].parse(data, entry.getFile().getAbsolutePath());
                        } catch (Exception ex) {
                            Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                            Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                        }
                    } else {
                        if (pieces[2].contentEquals("friends.xml")) {
                            try {
                                parsers[5].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        } else if (pieces[2].contentEquals("topArtists.xml")) {
                            try {
                                parsers[1].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        } else if (pieces[2].contentEquals("profile.xml")) {
                            try {
                                parsers[0].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        } else if (pieces[2].contentEquals("tags.xml")) {
                            try {
                                parsers[3].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        } else if (pieces[2].contentEquals("neighbours.xml")) {
                            try {
                                parsers[6].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        } else {
                            try {
                                parsers[2].parse(data, entry.getFile().getAbsolutePath());
                            } catch (Exception ex) {
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, name);
                                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.FINER, null, ex);
                            }
                        }
                    }
                }
                count++;
                entry = input.getNextEntry();
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                input.close();
            } catch (IOException ex) {
                Logger.getLogger(ReadLastFMProfileFile.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void set(Graph g) {
        graph = g;
    }

    @Override
    public Graph get() {
        return graph;
    }

    @Override
    public void cancel() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<IODescriptor> getInputType() {
        return input;
    }

    @Override
    public List<IODescriptor> getOutputType() {
        return output;
    }

    @Override
    public Properties getParameter() {
        return properties;
    }

    @Override
    public Parameter getParameter(String param) {
        return properties.get(param);
    }

    /**

     * Parameters:<br/>

     * <br/>

     * <ul>

     * <li/>

     * <li/>

     * </ul>

     * @param map  properties to load - null is permitted.

     */
    public void init(Properties map) {
        if (properties.check(map)) {
            properties.merge(map);
            IODescriptor desc = IODescriptorFactory.newInstance().create(Type.ACTOR, (String) properties.get("Name").get(), "user", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "LastFM Homepage", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Age", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Avatar", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Country", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Gender", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Icon", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "MBox", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "PlayCount", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "name", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "Cluster ID", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "user", null, "User ID", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR, (String) properties.get("Name").get(), "artist", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR, (String) properties.get("Name").get(), "tag", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.ACTOR_PROPERTY, (String) properties.get("Name").get(), "tag", null, "URL", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "ArtistTag", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "UserTag", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "UserArtistTag", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK_PROPERTY, (String) properties.get("Name").get(), "UserArtistTag", null, "Tags", "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "ListensTo", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "Similar", null, null, "");
            output.add(desc);
            desc = IODescriptorFactory.newInstance().create(Type.LINK, (String) properties.get("Name").get(), "Friend", null, null, "");
            output.add(desc);
        }
    }

    public ReadLastFMProfileFile prototype() {
        return new ReadLastFMProfileFile();
    }
}
