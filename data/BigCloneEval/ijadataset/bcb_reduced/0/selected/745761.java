package javax.media.ding3d.utils.scenegraph.io.state.javax.media.ding3d;

import java.io.*;
import javax.media.ding3d.SpotLight;
import javax.media.ding3d.vecmath.Vector3f;
import javax.media.ding3d.utils.scenegraph.io.retained.Controller;
import javax.media.ding3d.utils.scenegraph.io.retained.SymbolTableData;

public class SpotLightState extends LightState {

    public SpotLightState(SymbolTableData symbol, Controller control) {
        super(symbol, control);
    }

    public void writeObject(DataOutput out) throws IOException {
        super.writeObject(out);
        Vector3f dir = new Vector3f();
        ((SpotLight) node).getDirection(dir);
        control.writeVector3f(out, dir);
        out.writeFloat(((SpotLight) node).getSpreadAngle());
        out.writeFloat(((SpotLight) node).getConcentration());
    }

    public void readObject(DataInput in) throws IOException {
        super.readObject(in);
        ((SpotLight) node).setDirection(control.readVector3f(in));
        ((SpotLight) node).setSpreadAngle(in.readFloat());
        ((SpotLight) node).setConcentration(in.readFloat());
    }

    protected javax.media.ding3d.SceneGraphObject createNode() {
        return new SpotLight();
    }
}
