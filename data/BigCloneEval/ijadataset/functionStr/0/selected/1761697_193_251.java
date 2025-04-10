public class Test {    protected void processJarFile(final File file) throws Exception {
        logger.verbose("starting jar file : " + file.toURL());
        File tempFile = File.createTempFile(file.getName(), null, new File(file.getAbsoluteFile().getParent()));
        try {
            FileOutputStream fout = new FileOutputStream(tempFile, false);
            try {
                final ZipOutputStream out = new ZipOutputStream(fout);
                ZipEntryHandler transformer = new ZipEntryHandler() {

                    public void handleEntry(ZipEntry entry, byte[] byteCode) throws Exception {
                        logger.verbose("starting entry : " + entry.toString());
                        if (!entry.isDirectory()) {
                            DataInputStream din = new DataInputStream(new ByteArrayInputStream(byteCode));
                            if (din.readInt() == CLASS_MAGIC) {
                                ClassDescriptor descriptor = getClassDescriptor(byteCode);
                                ClassTransformer transformer = getClassTransformer(descriptor);
                                if (transformer == null) {
                                    logger.verbose("skipping entry : " + entry.toString());
                                } else {
                                    logger.info("processing class [" + descriptor.getName() + "]; entry = " + file.toURL());
                                    byteCode = transformer.transform(getClass().getClassLoader(), descriptor.getName(), null, null, descriptor.getBytes());
                                }
                            } else {
                                logger.verbose("ignoring zip entry : " + entry.toString());
                            }
                        }
                        ZipEntry outEntry = new ZipEntry(entry.getName());
                        outEntry.setMethod(entry.getMethod());
                        outEntry.setComment(entry.getComment());
                        outEntry.setSize(byteCode.length);
                        if (outEntry.getMethod() == ZipEntry.STORED) {
                            CRC32 crc = new CRC32();
                            crc.update(byteCode);
                            outEntry.setCrc(crc.getValue());
                            outEntry.setCompressedSize(byteCode.length);
                        }
                        out.putNextEntry(outEntry);
                        out.write(byteCode);
                        out.closeEntry();
                    }
                };
                ZipFileProcessor processor = new ZipFileProcessor(transformer);
                processor.process(file);
                out.close();
            } finally {
                fout.close();
            }
            if (file.delete()) {
                File newFile = new File(tempFile.getAbsolutePath());
                if (!newFile.renameTo(file)) {
                    throw new IOException("can not rename " + tempFile + " to " + file);
                }
            } else {
                throw new IOException("can not delete " + file);
            }
        } finally {
            tempFile.delete();
        }
    }
}