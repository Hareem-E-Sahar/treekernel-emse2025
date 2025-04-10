public class Test {    public void write(ScoreDoc doc, File dir, String mainFileName, PrintStream progressLogStream) throws IOException {
        progressLogStream.println("Writing Pinocchio 2010 file...");
        FileUtils.writeFile("generated by " + Zong.getNameAndVersion(Converter.PROJECT_FIRST_NAME) + " on " + new Date(), new File(dir, mainFileName).getAbsolutePath());
        if (doc.getFilePath() != null) {
            progressLogStream.println("Copy MusicXML file...");
            FileUtils.copyFile(doc.getFilePath(), new File(dir, "score.xml").getAbsolutePath());
        }
        Layout layout = doc.getLayout();
        progressLogStream.println("Write pages...");
        writeProgress(progressLogStream, 0, false);
        for (int i = 0; i < layout.pages.size(); i++) {
            PNGPrinter.print(layout, i, new FileOutputStream(new File(dir, "page" + i + ".png")));
            writeProgress(progressLogStream, 1f * (i + 1) / layout.pages.size(), false);
        }
        writeProgress(progressLogStream, 1, true);
        progressLogStream.println("Write beat offsets...");
        XMLWriter.writeFile(createCursorDocument(doc.getScore(), layout), new FileOutputStream(new File(dir, "cursor.xml")));
        createMIDIAndMPMappingAndWAVs(doc.getScore(), dir, progressLogStream);
    }
}