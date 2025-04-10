package yaw.cjef.templates.project.templates.cJEF.gl;

import java.io.*;
import yaw.core.codegenerator.ICodeGenerator;
import yaw.cjef.templates.project.Model;

public class System_java implements ICodeGenerator<Model> {

    public void generate(Writer writer, Model model) throws IOException {
        writer.write("/* \r\n * $Id: System_java.java 73 2012-02-17 08:24:49Z jgundelach $\r\n * \r\n * ");
        writer.write(model.getProjectName());
        writer.write("\r\n * ");
        writer.write(model.getCopyright());
        writer.write("\r\n * ");
        writer.write(model.getLegal());
        writer.write("\r\n */\r\npackage ");
        writer.write(model.getProjectPackage());
        writer.write(".gl;\r\n\r\nimport de.carus.cjfc.tfc.sys.*;\r\nimport de.carus.cjfc.tfc.sys.da.*;\r\nimport de.carus.cjfc.tfc.sys.factory.*;\r\nimport de.carus.cjfc.tfc.dto.TMCID;\r\nimport de.carus.cjfc.tfc.da.channel.*;\r\n\r\n/**\r\n * Abbildung des Gesamtsystems ");
        writer.write(model.getProjectName());
        writer.write(".\r\n * \r\n * @author ");
        writer.write(model.getUser());
        writer.write("\r\n */\r\npublic class ");
        writer.write(model.getSystem());
        writer.write(" extends TMSystem {\r\n  //{{MEMBER_DECL\r\n  //}}MEMBER_DECL\r\n  \r\n  public ");
        writer.write(model.getSystem());
        writer.write("() {\r\n    super();\r\n  }\r\n\r\n  //{{CID\r\n  //}}CID\r\n\r\n  protected void initSystem() {\r\n    //{{FACTORY\r\n    //}}FACTORY\r\n  }\r\n\r\n  protected TMSubsystem createMainSubsystem() {\r\n    return new ");
        writer.write(model.getSubsystem());
        writer.write("();\r\n  }\r\n\r\n  protected void addSubsystems() {\r\n    //{{SUBSYSTEMS\r\n    //}}SUBSYSTEMS\r\n  }\r\n\r\n  protected void initChannel() {\r\n    setDefaultDAChannelFactory(");
        writer.write(model.getDatabaseclass());
        writer.write(".class, new TMDAJDBConnectData(\"");
        writer.write(model.getDatabaseURL());
        writer.write("\",\"");
        writer.write(model.databaseUser);
        writer.write("\", \"");
        writer.write(model.databasePassword);
        writer.write("\"), true);\r\n  }\r\n\r\n  @Override\r\n  protected Properties loadPropertyFiles() {\r\n    File file = new File(\"config/");
        writer.write(model.getApplicationName());
        writer.write(".properties\");\r\n    return loadPropertyFile(file, System.getProperties());\r\n  }\r\n\t\r\n  @Override\r\n  protected void initChannel() {\r\n    TMIDAChannelFactory systemPropertiesDAConnectionFactory = TMDAJDBCConnectionUtil.getChannelFactoryByProperties(getDefaultChannelName());\r\n    addFactory(systemPropertiesDAConnectionFactory);\r\n  }\r\n}");
    }
}
