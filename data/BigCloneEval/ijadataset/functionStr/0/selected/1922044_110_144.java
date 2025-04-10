public class Test {    public boolean fromSaiphXML(Node node) throws NumberFormatException, DOMException, SecurityException, IllegalArgumentException, InvalidMidiDataException, ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
        boolean ok = super.fromSaiphXML(node);
        if (!ok) return false;
        Node pgNode = ((Element) node).getElementsByTagName("pitchGenerator").item(0);
        Node vgNode = ((Element) pgNode).getFirstChild();
        String typeAttr = ((Element) vgNode).getAttribute("type");
        String pgClassName = ((Element) vgNode).getAttribute("class");
        Class pgClass = Class.forName(pgClassName);
        Constructor pgClassConstructor = pgClass.getConstructor(new Class[] { int.class });
        Object pg = pgClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) pg).fromSaiphXML(vgNode);
        if (!ok) return false;
        setPitchGenerator((ValuesGenerator) pg);
        Node velgNode = ((Element) node).getElementsByTagName("velocityGenerator").item(0);
        vgNode = ((Element) velgNode).getFirstChild();
        typeAttr = ((Element) vgNode).getAttribute("type");
        String velgClassName = ((Element) vgNode).getAttribute("class");
        Class velgClass = Class.forName(velgClassName);
        Constructor velgClassConstructor = velgClass.getConstructor(new Class[] { int.class });
        Object velg = velgClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) velg).fromSaiphXML(vgNode);
        if (!ok) return false;
        setVelocityGenerator((ValuesGenerator) velg);
        Node agNode = ((Element) node).getElementsByTagName("articulationGenerator").item(0);
        vgNode = ((Element) agNode).getFirstChild();
        typeAttr = ((Element) vgNode).getAttribute("type");
        String agClassName = ((Element) vgNode).getAttribute("class");
        Class agClass = Class.forName(agClassName);
        Constructor agClassConstructor = agClass.getConstructor(new Class[] { int.class });
        Object ag = agClassConstructor.newInstance(new Object[] { Integer.valueOf(typeAttr) });
        ok = ((SaiphXML) ag).fromSaiphXML(vgNode);
        if (!ok) return false;
        setArticulationGenerator((ValuesGenerator) ag);
        return ok;
    }
}