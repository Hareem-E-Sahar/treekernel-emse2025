package com.jmt.game.core;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import com.jme.bounding.BoundingBox;
import com.jme.math.FastMath;
import com.jme.math.Quaternion;
import com.jme.math.Vector3f;
import com.jme.scene.Node;
import com.jme.scene.TriMesh;
import com.jme.scene.SharedMesh;
import com.jme.scene.shape.Box;
import com.jme.util.export.binary.BinaryImporter;
import com.jmex.model.converters.FormatConverter;
import com.jmex.model.converters.MaxToJme;
import com.jmex.model.converters.ObjToJme;

public class ModelStore {

    private File localModelDirectory = new File("data/models");

    private File globalModelDirectory = new File("data/models");

    private Map<String, TriMesh> modelCache = new HashMap<String, TriMesh>();

    public File getLocalModelDirectory() {
        return localModelDirectory;
    }

    public void setLocalModelDirectory(File modelDirectory) {
        this.localModelDirectory = modelDirectory;
    }

    public void setGlobalModelDirectory(File modelDirectory) {
        this.globalModelDirectory = modelDirectory;
    }

    public void setGlobalDirectory(String modelDirectory) {
        this.globalModelDirectory = new File(modelDirectory);
    }

    public Node getModel(String name, String modelName) {
        Node modelNode = new Node(name);
        TriMesh model = modelCache.get(modelName);
        if (null == model) {
            model = loadModel(modelName);
        }
        SharedMesh mesh = new SharedMesh(name, model);
        modelNode.attachChild(mesh);
        modelNode.setModelBound(new BoundingBox());
        modelNode.updateModelBound();
        return modelNode;
    }

    public TriMesh loadModel(String modelName) {
        TriMesh model = null;
        try {
            ByteArrayOutputStream BO = new ByteArrayOutputStream();
            File modelFile = new File(localModelDirectory, modelName);
            if (!modelFile.exists() && !modelFile.canRead()) {
                modelFile = new File(globalModelDirectory, modelName);
            }
            InputStream modelStream = new FileInputStream(modelFile);
            FormatConverter converter = null;
            if (modelName.toLowerCase().indexOf(".3ds") > 0) {
                converter = new MaxToJme();
                converter.convert(new BufferedInputStream(modelStream), BO);
                Node node = (Node) BinaryImporter.getInstance().load(new ByteArrayInputStream(BO.toByteArray()));
                model = (TriMesh) node.getChild(0);
                Quaternion q = new Quaternion();
                q.fromAngles(-FastMath.HALF_PI, 0, 0);
                model.setLocalRotation(q);
            } else if (modelName.toLowerCase().indexOf(".obj") > 0) {
                converter = new ObjToJme();
                converter.convert(new BufferedInputStream(modelStream), BO);
                model = (TriMesh) BinaryImporter.getInstance().load(new ByteArrayInputStream(BO.toByteArray()));
            } else {
                throw new Exception("Unknown Model Format");
            }
        } catch (Exception e) {
            e.printStackTrace();
            model = new Box("PlaceHolder", new Vector3f(0f, 3f, 0f), 1f, 3f, 0.75f);
        }
        modelCache.put(modelName, model);
        return model;
    }
}
