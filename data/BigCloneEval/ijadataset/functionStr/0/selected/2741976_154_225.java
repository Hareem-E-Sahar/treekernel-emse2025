public class Test {    private void saveContent(DocumentCBF docBF, String docContent, String[] retAffixs, String blRtype, boolean blRte) throws Exception {
        try {
            CesGlobals cesGlobals = new CesGlobals();
            String uploadPath;
            cesGlobals.setConfigFile("platform.xml");
            uploadPath = cesGlobals.getCesXmlProperty("platform.datadir");
            uploadPath = new File(uploadPath).getPath();
            if (uploadPath.endsWith("\\")) {
                uploadPath = uploadPath.substring(0, uploadPath.length() - 1);
            }
            uploadPath = uploadPath + "/infoplat/workflow/docs/" + Function.getNYofDate(docBF.getCreateDate()) + "/";
            if (!new File(uploadPath).isDirectory()) {
                new File(uploadPath).mkdirs();
            }
            if (blRtype.equals("-1")) {
                if (docContent != null && docContent.length() > 0) {
                    if (true) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(docContent)));
                        StringBuffer sCon = new StringBuffer();
                        char b[] = new char[1024];
                        int iLen = 0;
                        while (iLen != -1) {
                            iLen = in.read(b);
                            sCon.append(new String(b));
                        }
                        in.close();
                        String content = sCon.toString();
                        if (!content.trim().toLowerCase().startsWith("<body>")) content = "<body>" + content + "</body>";
                        docBF.saveContent(content, true);
                        DocumentCBFDAO docCBFDAO = new DocumentCBFDAO();
                        DocContentResource[] docResource = docCBFDAO.getDocContentResourceList(docBF.getId());
                        List tmp = new ArrayList();
                        for (int j = 0; j < docResource.length; j++) {
                            tmp.add(docResource[j].getUri());
                        }
                        String fileName = "";
                        File ff;
                        uploadPath += "res/";
                        if (!new File(uploadPath).isDirectory()) {
                            new File(uploadPath).mkdirs();
                        }
                    }
                } else {
                    String newFileName = "d_" + docBF.getId() + ".data";
                    FileOperation.copy(docContent, uploadPath + newFileName);
                }
            } else {
                if (docContent != null && !docContent.equals("")) {
                    if (!docContent.trim().toLowerCase().startsWith("<body>")) docContent = "<body>" + docContent + "</body>";
                    if (blRte) {
                    } else {
                    }
                    if (blRte && retAffixs != null && retAffixs.length > 0) {
                        DocumentCBFDAO docCBFDAO = new DocumentCBFDAO();
                        DocContentResource[] docResource = docCBFDAO.getDocContentResourceList(docBF.getId());
                        List tmp = new ArrayList();
                        for (int j = 0; j < docResource.length; j++) {
                            tmp.add(docResource[j].getUri());
                        }
                        String fileName = "";
                        File ff;
                        uploadPath += "res/";
                        if (!new File(uploadPath).isDirectory()) {
                            new File(uploadPath).mkdirs();
                        }
                    }
                }
            }
        } catch (Exception e) {
            throw new Exception("������������ʧ��!" + e.getMessage());
        }
    }
}