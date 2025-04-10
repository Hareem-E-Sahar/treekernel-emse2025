public class Test {    public void genJavaCode() throws IOException {
        String pkg = getJavaPackage();
        String pkgpath = pkg.replaceAll("\\.", "/");
        File pkgdir = new File(pkgpath);
        if (!pkgdir.exists()) {
            boolean ret = pkgdir.mkdirs();
            if (!ret) {
                System.out.println("Cannnot create directory: " + pkgpath);
                System.exit(1);
            }
        } else if (!pkgdir.isDirectory()) {
            System.out.println(pkgpath + " is not a directory.");
            System.exit(1);
        }
        File jfile = new File(pkgdir, getName() + ".java");
        FileWriter jj = new FileWriter(jfile);
        jj.write("// File generated by hadoop record compiler. Do not edit.\n");
        jj.write("package " + getJavaPackage() + ";\n\n");
        jj.write("import com.yahoo.jute.*;\n");
        jj.write("public class " + getName() + " implements Record {\n");
        for (Iterator i = mFields.iterator(); i.hasNext(); ) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaDecl());
        }
        jj.write("  public " + getName() + "() {\n");
        jj.write("  }\n");
        jj.write("  public " + getName() + "(\n");
        int fIdx = 0;
        int fLen = mFields.size();
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaConstructorParam(jf.getName()));
            jj.write((fLen - 1 == fIdx) ? "" : ",\n");
        }
        jj.write(") {\n");
        fIdx = 0;
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaConstructorSet(jf.getName()));
        }
        jj.write("  }\n");
        fIdx = 0;
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaGetSet(fIdx));
        }
        jj.write("  public void serialize(OutputArchive a_, String tag) throws java.io.IOException {\n");
        jj.write("    a_.startRecord(this,tag);\n");
        fIdx = 0;
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaWriteMethodName());
        }
        jj.write("    a_.endRecord(this,tag);\n");
        jj.write("  }\n");
        jj.write("  public void deserialize(InputArchive a_, String tag) throws java.io.IOException {\n");
        jj.write("    a_.startRecord(tag);\n");
        fIdx = 0;
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaReadMethodName());
        }
        jj.write("    a_.endRecord(tag);\n");
        jj.write("}\n");
        jj.write("  public String toString() {\n");
        jj.write("    try {\n");
        jj.write("      java.io.ByteArrayOutputStream s =\n");
        jj.write("        new java.io.ByteArrayOutputStream();\n");
        jj.write("      CsvOutputArchive a_ = \n");
        jj.write("        new CsvOutputArchive(s);\n");
        jj.write("      a_.startRecord(this,\"\");\n");
        fIdx = 0;
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaWriteMethodName());
        }
        jj.write("      a_.endRecord(this,\"\");\n");
        jj.write("      return new String(s.toByteArray(), \"UTF-8\");\n");
        jj.write("    } catch (Throwable ex) {\n");
        jj.write("      ex.printStackTrace();\n");
        jj.write("    }\n");
        jj.write("    return \"ERROR\";\n");
        jj.write("  }\n");
        jj.write("  public void write(java.io.DataOutput out) throws java.io.IOException {\n");
        jj.write("    BinaryOutputArchive archive = new BinaryOutputArchive(out);\n");
        jj.write("    serialize(archive, \"\");\n");
        jj.write("  }\n");
        jj.write("  public void readFields(java.io.DataInput in) throws java.io.IOException {\n");
        jj.write("    BinaryInputArchive archive = new BinaryInputArchive(in);\n");
        jj.write("    deserialize(archive, \"\");\n");
        jj.write("  }\n");
        jj.write("  public int compareTo (Object peer_) throws ClassCastException {\n");
        jj.write("    if (!(peer_ instanceof " + getName() + ")) {\n");
        jj.write("      throw new ClassCastException(\"Comparing different types of records.\");\n");
        jj.write("    }\n");
        jj.write("    " + getName() + " peer = (" + getName() + ") peer_;\n");
        jj.write("    int ret = 0;\n");
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaCompareTo());
            jj.write("    if (ret != 0) return ret;\n");
        }
        jj.write("     return ret;\n");
        jj.write("  }\n");
        jj.write("  public boolean equals(Object peer_) {\n");
        jj.write("    if (!(peer_ instanceof " + getName() + ")) {\n");
        jj.write("      return false;\n");
        jj.write("    }\n");
        jj.write("    if (peer_ == this) {\n");
        jj.write("      return true;\n");
        jj.write("    }\n");
        jj.write("    " + getName() + " peer = (" + getName() + ") peer_;\n");
        jj.write("    boolean ret = false;\n");
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaEquals());
            jj.write("    if (!ret) return ret;\n");
        }
        jj.write("     return ret;\n");
        jj.write("  }\n");
        jj.write("  public int hashCode() {\n");
        jj.write("    int result = 17;\n");
        jj.write("    int ret;\n");
        for (Iterator i = mFields.iterator(); i.hasNext(); fIdx++) {
            JField jf = (JField) i.next();
            jj.write(jf.genJavaHashCode());
            jj.write("    result = 37*result + ret;\n");
        }
        jj.write("    return result;\n");
        jj.write("  }\n");
        jj.write("  public static String signature() {\n");
        jj.write("    return \"" + getSignature() + "\";\n");
        jj.write("  }\n");
        jj.write("}\n");
        jj.close();
    }
}