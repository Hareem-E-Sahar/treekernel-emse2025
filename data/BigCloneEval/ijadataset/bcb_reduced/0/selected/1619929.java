package org.posterita.businesslogic;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import org.apache.struts.upload.FormFile;
import org.compiere.model.MAttachment;
import org.compiere.model.MAttachmentEntry;
import org.posterita.core.ThumbnailGenerator;
import org.posterita.exceptions.AttachmentNotFoundException;
import org.posterita.exceptions.InvalidContentTypeException;
import org.posterita.exceptions.OperationException;
import org.posterita.lib.UdiConstants;
import org.posterita.model.UDIMAttachment;

public class ProductImageUploader {

    public static void uploadImage(Properties ctx, Integer productIds[], FormFile file, String imageName, String trxName) throws OperationException, InvalidContentTypeException {
        for (int i = 0; i < productIds.length; i++) {
            uploadImage(ctx, productIds[i].intValue(), file, imageName, trxName);
        }
    }

    public static void uploadImage(Properties ctx, int product_id, FormFile file, String imageName, String trxName) throws OperationException, InvalidContentTypeException {
        if ((file.getContentType() == null) || (!file.getContentType().startsWith("image/"))) throw new InvalidContentTypeException("Invalid Content type! Expecting image");
        try {
            uploadImage(ctx, product_id, file.getFileData(), imageName, trxName);
        } catch (IOException ex) {
            throw new OperationException("Could not read form file", ex);
        }
    }

    public static void uploadDefaultImage(Properties ctx, int productId, File file, String trxName) throws OperationException {
        uploadImage(ctx, productId, file, UdiConstants.DEFAULT_IMAGE, trxName);
    }

    public static void uploadImage(Properties ctx, int productId, File file, String imageName, String trxName) throws OperationException {
        if (file == null) throw new OperationException("Image file cannot be null");
        if (!file.exists()) throw new OperationException("Image file does not exist");
        if (file.isDirectory()) throw new OperationException("File cannot be a directory");
        try {
            FileInputStream fileInStream = new FileInputStream(file);
            ByteArrayOutputStream byteArrStream = new ByteArrayOutputStream();
            BufferedInputStream bufferedInStream = new BufferedInputStream(fileInStream);
            byte data[] = new byte[1024];
            int read = 0;
            while ((read = bufferedInStream.read(data)) != -1) {
                byteArrStream.write(data, 0, read);
            }
            byteArrStream.flush();
            byte fileData[] = byteArrStream.toByteArray();
            bufferedInStream.close();
            byteArrStream.close();
            fileInStream.close();
            uploadImage(ctx, productId, fileData, imageName, trxName);
        } catch (IOException ex) {
            throw new OperationException("Could not read file: " + file.getAbsolutePath(), ex);
        }
    }

    public static void uploadDefaultImage(Properties ctx, int productId, byte[] imageData, String trxName) throws OperationException {
        uploadImage(ctx, productId, imageData, UdiConstants.DEFAULT_IMAGE, trxName);
    }

    public static void uploadImage(Properties ctx, int productId, byte[] imageData, String imageName, String trxName) throws OperationException {
        MAttachmentEntry attEntry = new MAttachmentEntry(imageName + ".jpg", imageData);
        ProductManager.addUpdateAttachment(ctx, productId, attEntry, trxName);
        try {
            generateSubImages(ctx, productId, imageName, trxName);
        } catch (AttachmentNotFoundException ex) {
            throw new OperationException("Could not generate sub images for product with ID: " + productId, ex);
        }
    }

    public static void generateAllSubImages(Properties ctx, int productId, String trxName) throws AttachmentNotFoundException, OperationException {
        MAttachment attachment = ProductManager.getAttachment(ctx, productId, trxName);
        if (attachment == null) throw new AttachmentNotFoundException("No attachment found for product with id: " + productId);
        MAttachmentEntry attachmentEntries[] = attachment.getEntries();
        for (int i = 0; i < attachmentEntries.length; i++) {
            if (attachmentEntries[i].getContentType().startsWith("image/")) {
                String imageFileName = attachmentEntries[i].getName();
                if (!(imageFileName.startsWith(UdiConstants.FULL_IMAGE_PREFIX) || imageFileName.startsWith(UdiConstants.THUMBNAIL_IMAGE_PREFIX) || imageFileName.equalsIgnoreCase(UdiConstants.FULL_IMAGE + UdiConstants.IMAGE_EXTENSION) || imageFileName.equalsIgnoreCase(UdiConstants.THUMBNAIL + UdiConstants.IMAGE_EXTENSION))) {
                    String imageName = imageFileName.replaceAll(UdiConstants.IMAGE_EXTENSION, "");
                    generateSubImages(ctx, productId, imageName, trxName);
                }
            }
        }
    }

    public static void generateSubImages(Properties ctx, int productId, String imageName, String trxName) throws OperationException, AttachmentNotFoundException {
        String originalImageName = imageName + UdiConstants.IMAGE_EXTENSION;
        MAttachment attachment = ProductManager.getAttachment(ctx, productId, trxName);
        byte originalImageData[] = ProductManager.getAttachmentData(attachment, originalImageName);
        byte[] scaledImageData = ThumbnailGenerator.getSquareThumbnail(originalImageData, 380);
        byte[] framedImageData = ProductImageManager.addImageSquareBorder(ctx, scaledImageData, 7.0, trxName);
        byte[] thumbImageData = ThumbnailGenerator.getSquareThumbnail(framedImageData, 180);
        String fullImageName = UdiConstants.FULL_IMAGE_PREFIX + imageName + UdiConstants.IMAGE_EXTENSION;
        String thumbnailImageName = UdiConstants.THUMBNAIL_IMAGE_PREFIX + imageName + UdiConstants.IMAGE_EXTENSION;
        int fullImageEntryInd = AttachmentManager.getAttachmentEntryIndex(ctx, attachment, fullImageName);
        if (fullImageEntryInd != -1) attachment.deleteEntry(fullImageEntryInd);
        int thumbnailEntryInd = AttachmentManager.getAttachmentEntryIndex(ctx, attachment, thumbnailImageName);
        if (thumbnailEntryInd != -1) attachment.deleteEntry(thumbnailEntryInd);
        MAttachmentEntry attFullImageEntry = new MAttachmentEntry(fullImageName, framedImageData);
        MAttachmentEntry attThumbImageEntry = new MAttachmentEntry(thumbnailImageName, thumbImageData);
        AttachmentManager.addAttachmentEntry(attachment, attFullImageEntry);
        AttachmentManager.addAttachmentEntry(attachment, attThumbImageEntry);
        UDIMAttachment udiAttachment = new UDIMAttachment(attachment);
        udiAttachment.save();
    }

    public static ArrayList<String> getRelatedProductImageNames(Properties ctx, int productId, String trxName) throws OperationException {
        MAttachment attachment = ProductManager.getAttachment(ctx, productId, trxName);
        MAttachmentEntry entries[] = attachment.getEntries();
        ArrayList<String> productImageNameList = new ArrayList<String>();
        for (int i = 0; i < entries.length; i++) {
            String productImageName = entries[i].getName();
            if (!(productImageName.startsWith(UdiConstants.THUMBNAIL_IMAGE_PREFIX) || productImageName.startsWith(UdiConstants.FULL_IMAGE_PREFIX))) {
                productImageName = productImageName.replaceAll(UdiConstants.IMAGE_EXTENSION, "");
                productImageNameList.add(productImageName);
            }
        }
        Collections.sort(productImageNameList);
        return productImageNameList;
    }

    public static String getImageAttachmentName(String imageName) {
        return imageName + UdiConstants.IMAGE_EXTENSION;
    }
}
