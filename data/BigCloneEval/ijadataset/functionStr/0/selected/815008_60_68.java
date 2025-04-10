public class Test {    public OutputStream addPack(int packNumber, String name, String id, List osConstraints, boolean required, String description, boolean preselected) throws Exception {
        sendMsg("Adding pack #" + packNumber + " : " + name + " ...");
        Pack pack = new Pack(name, id, description, osConstraints, required, preselected);
        packs.add(packNumber, pack);
        String entryName = "packs/pack" + packNumber;
        ZipEntry entry = new ZipEntry(entryName);
        outJar.putNextEntry(entry);
        return outJar;
    }
}