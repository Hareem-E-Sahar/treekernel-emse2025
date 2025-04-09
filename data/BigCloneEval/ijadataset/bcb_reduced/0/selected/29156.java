package com.windsor.node.admin.transaction;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.ServletRequestUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import com.windsor.node.admin.util.AdminConstants;
import com.windsor.node.admin.util.VisitUtils;
import com.windsor.node.common.domain.Document;
import com.windsor.node.common.domain.NodeTransaction;
import com.windsor.node.common.domain.NodeVisit;
import com.windsor.node.common.domain.PartnerIdentity;
import com.windsor.node.common.service.admin.TransactionService;
import com.windsor.node.common.util.NodeClientService;
import com.windsor.node.service.helper.client.NodeClientFactory;

public class TransactionDownloadController extends AbstractController {

    private NodeClientFactory nodeClientFactory;

    private TransactionService transactionService;

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception {
        NodeVisit visit = VisitUtils.getVisit(request);
        if (visit == null) {
            logger.debug(AdminConstants.UNAUTHED + " transaction " + AdminConstants.ACCESS_REQUEST);
            return VisitUtils.getUnauthedView(request);
        }
        String transactionId = ServletRequestUtils.getRequiredStringParameter(request, "transactionId");
        NodeTransaction trans = getTransactionService().get(transactionId, visit);
        NodeTransaction downloadTrans = null;
        File zipFile = null;
        if (trans != null) {
            PartnerIdentity oldPartner = new PartnerIdentity();
            oldPartner.setUrl(trans.getNetworkEndpointUrl());
            oldPartner.setVersion(trans.getNetworkEndpointVersion());
            NodeClientService ncl = getNodeClientFactory().makeAndConfigure(oldPartner);
            trans.setDocuments(new ArrayList<Document>());
            downloadTrans = ncl.download(trans);
            if (downloadTrans.getDocuments() != null) {
                zipFile = File.createTempFile("transdownload", ".zip");
                if (zipFile.exists()) {
                    zipFile.delete();
                }
                ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipFile));
                for (int i = 0; i < downloadTrans.getDocuments().size(); i++) {
                    Document currentDoc = downloadTrans.getDocuments().get(i);
                    writeFileToZip(zipOut, currentDoc.getContent(), currentDoc.getDocumentName());
                }
                zipOut.flush();
                zipOut.close();
            }
        }
        if (zipFile != null) {
            response.setHeader("Cache-Control", "must-revalidate");
            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + trans.getNetworkId() + ".zip\"");
            FileCopyUtils.copy(new FileInputStream(zipFile), response.getOutputStream());
            return null;
        }
        return new ModelAndView("redirect:tran.htm?id=" + transactionId);
    }

    private void writeFileToZip(ZipOutputStream zipOut, byte[] bytes, String fileName) throws IOException {
        ZipEntry entry = new ZipEntry(fileName);
        zipOut.putNextEntry(entry);
        zipOut.write(bytes, 0, bytes.length);
        zipOut.closeEntry();
    }

    public NodeClientFactory getNodeClientFactory() {
        return nodeClientFactory;
    }

    public void setNodeClientFactory(NodeClientFactory nodeClientFactory) {
        this.nodeClientFactory = nodeClientFactory;
    }

    public TransactionService getTransactionService() {
        return transactionService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }
}
