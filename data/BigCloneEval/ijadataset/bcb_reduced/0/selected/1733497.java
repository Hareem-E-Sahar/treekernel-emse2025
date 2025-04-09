package es.ulpgc.dis.heuristicide.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import es.ulpgc.dis.heuriskein.model.solver.Agent;
import es.ulpgc.dis.heuriskein.model.solver.Execution;
import es.ulpgc.dis.heuriskein.model.solver.ExecutionGroup;
import es.ulpgc.dis.heuriskein.model.solver.MetaHeuristic;
import es.ulpgc.dis.heuriskein.model.solver.Population;
import es.ulpgc.dis.heuristicide.project.INameable;
import es.ulpgc.dis.heuristicide.project.Project;

public class ZIPProjectWriter {

    private static ZipOutputStream out;

    public static void write(Project project, String filename) {
        File target = new File(project.getPath());
        if (!target.exists()) {
            target.mkdir();
        }
        try {
            ArrayList<String> files = new ArrayList<String>();
            files.add("Problem.xml");
            files.add("Project.xml");
            for (Object ob : project.getPopulationList().toArray()) {
                files.add(project.getPopulationsPath() + ((Population) ob).getFilename());
            }
            for (Object ob : project.getMetaheuristicList().toArray()) {
                files.add(project.getMetaHeuristicsPath() + ((MetaHeuristic) ob).getFilename());
            }
            for (Object ob : project.getAgentList().toArray()) {
                files.add(project.getAgentsPath() + ((Agent) ob).getFilename());
            }
            for (Object ob : project.getExcutionList().toArray()) {
                if (ob instanceof ExecutionGroup) {
                    for (Object exe : ((ExecutionGroup) ob).getExecutions().toArray()) {
                        files.add(project.getExecutionsPath() + ((Execution) exe).getFilename());
                    }
                } else {
                    files.add(project.getExecutionsPath() + ((Execution) ob).getFilename());
                }
            }
            FileOutputStream stream = new FileOutputStream(filename);
            out = new ZipOutputStream(stream);
            for (String fullname : files) {
                writeFile(fullname, project.getTmpDirectory());
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void writeFile(String fullname, String path) throws FileNotFoundException {
        byte buffer[] = new byte[1024];
        FileInputStream input = new FileInputStream(new File(path + fullname));
        try {
            out.putNextEntry(new ZipEntry(fullname));
            int len;
            while ((len = input.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }
            out.closeEntry();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
