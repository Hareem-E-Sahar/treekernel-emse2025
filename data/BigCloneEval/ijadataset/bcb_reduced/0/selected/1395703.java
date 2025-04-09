package wesodi.logic.update.Controller;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import saheba.util.conf.GlobalConfigurationManager;
import wesodi.entities.persi.AccessPoint;
import wesodi.entities.persi.AvailableProduct;
import wesodi.entities.persi.DistributedProduct;
import wesodi.entities.persi.UpdateRule;
import wesodi.entities.transi.DPUpdateData;
import wesodi.entities.transi.Media;
import wesodi.entities.transi.ProductUpdate;
import wesodi.logic.manage.updateManager.DataHandlerGetter;
import wesodi.logic.update.UpdateAction;
import wesodi.logic.update.UpdateType;
import wesodi.logic.update.Distributor.UpdateDistributorBean;
import wesodi.logic.update.Distributor.UpdateDistributorBeanLocal;
import wesodi.persistence.facade.AccessPointFacadeLocal;
import wesodi.persistence.facade.AvailableProductFacadeLocal;
import wesodi.plugins.handDraufAdvertiser.Advertisement;
import wesodi.plugins.handDraufAdvertiser.HandDraufAdvertiserProxy;
import wesodi.plugins.handDraufAdvertiser.Sponsor;

/**
 * Session Bean implementation class UpdateControllerBean
 *  The Update Controller to be called from the repository interface
 * @author Bretti
 */
@Stateless
public class UpdateControllerBean implements UpdateControllerBeanRemote, UpdateControllerBeanLocal, Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = -4663013802999280227L;

    @EJB
    AvailableProductFacadeLocal availableProductFacade;

    @EJB
    AccessPointFacadeLocal accessPointFacade;

    @EJB
    UpdateDistributorBeanLocal distributer;

    private FileWriter fw;

    /**
	 * Creates an UpdateController for product data.
	 */
    public UpdateControllerBean() {
        if (GlobalConfigurationManager.getCurrentConfigurationManager() != null) {
            String logLocation = GlobalConfigurationManager.getProperty("wesodi.update.distribution.log.location");
            try {
                fw = new FileWriter(new File(logLocation + hashCode() + "_" + new GregorianCalendar().getTimeInMillis() + ".txt"));
            } catch (Exception e) {
                System.out.println(e.getMessage() + " Actions will not be logged!");
            }
        } else {
            System.out.println("No properties for logging found.");
        }
    }

    /**
	 * Initiates the distribution of a ProductUpdate. This involves creating meta data and retrieving the actual 
	 * software from a repository.
	 * @param testEnvironment must be true if not executed on a server
	 */
    public void update(ProductUpdate productUpdate) {
        log("Update: ");
        UpdateType ut = null;
        try {
            ut = UpdateType.getTypeByVersionDiff(productUpdate.getPreviousVersion(), productUpdate.getVersion());
        } catch (Exception e) {
            log(e.getMessage());
        }
        log((ut != null) ? (ut.getName() + " ") : "no Type");
        AvailableProduct availableProduct;
        try {
            availableProduct = availableProductFacade.findByName(productUpdate.getName());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        if (availableProduct != null) {
            log(availableProduct.getName() + " ");
            AccessPoint accessPoint = availableProduct.getAccessPoint();
            DataHandlerGetter dhGetter = new DataHandlerGetter();
            DataHandler dataHandler = null;
            try {
                String tUID = null;
                String tPW = null;
                if (accessPoint.getTechnicalUserId() != null && !accessPoint.getTechnicalUserId().equals("")) {
                    tUID = accessPoint.getTechnicalUserId();
                }
                if (accessPoint.getTechnicalPassword() != null && !accessPoint.getTechnicalPassword().equals("")) {
                    tPW = accessPoint.getTechnicalPassword();
                }
                dataHandler = dhGetter.getHandlerFor(accessPoint.getUrl(), availableProduct.getPathToProduct(), tUID, tPW);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (dataHandler != null) {
                Set<DistributedProduct> distributedProducts = availableProduct.getDistributedProducts();
                Set<DPUpdateData> updateDataSet = new HashSet<DPUpdateData>();
                for (DistributedProduct dProd : distributedProducts) {
                    AccessPoint aPoint = dProd.getDistributionPoint().getAccessPoint();
                    DPUpdateData ud = new DPUpdateData(aPoint.getTechnicalUserId(), aPoint.getTechnicalPassword(), aPoint.getUrl(), getUpdateActionForUpdateRule(dProd.getUpdateRule(), ut));
                    updateDataSet.add(ud);
                }
                DPUpdateData[] updateData = new DPUpdateData[updateDataSet.size()];
                updateData = updateDataSet.toArray(updateData);
                DataHandler[] mediaHandlers = getMediaHandlers(productUpdate);
                DataHandler zipHandler = createZipStreamFromDataHandlers(mediaHandlers);
                distributer.distribute(updateData, dataHandler, zipHandler, productUpdate);
                advertiseProduct(productUpdate.getName(), updateData[0].getDpUrl());
            }
        }
    }

    /**
	 * Determines the action to take for a certain type of update using an update rule
	 * @param updateRule the UpdateRule use
	 * @param ut the UpdateType of the product
	 * @return the UpdateAction to be executed in the shop
	 */
    private UpdateAction getUpdateActionForUpdateRule(UpdateRule updateRule, UpdateType ut) {
        try {
            String ruleStr = updateRule.getName();
            int actionIndex = ruleStr.indexOf(ut.getID() + "=") + 2;
            String actIdStr = ruleStr.substring(actionIndex, ruleStr.indexOf(",", actionIndex));
            System.out.println(actIdStr);
            int actId = Integer.parseInt(actIdStr);
            return UpdateAction.getActionByID(actId);
        } catch (Exception e) {
            e.printStackTrace();
            return UpdateAction.NONE;
        }
    }

    /**
	 * Returns DataHandlers for all media files
	 * @param productUpdate
	 * @return the DataHandlers
	 */
    private DataHandler[] getMediaHandlers(ProductUpdate productUpdate) {
        try {
            ArrayList<Media> media = productUpdate.getMedia();
            int l = media.size();
            javax.activation.DataHandler[] mediaDH = new DataHandler[l];
            DataHandlerGetter dhGetter = new DataHandlerGetter();
            for (int i = 0; i < l; i++) {
                try {
                    mediaDH[i] = dhGetter.getHandlerFor(media.get(i).getUrl(), "", null, null);
                } catch (Exception e) {
                    System.out.println("Wrong URL for Media");
                }
            }
            return mediaDH;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
	 * Adds an advertisement for the new product at Hand Drauf
	 * @param name the product's name
	 * @param dpURL the distribution point's URL
	 */
    private void advertiseProduct(String name, String dpURL) {
        if (GlobalConfigurationManager.getCurrentConfigurationManager() != null) {
            String doAdStr = GlobalConfigurationManager.getProperty("wesodi.update.advertisement.handdrauf.advertise");
            String adEdpoint = GlobalConfigurationManager.getProperty("wesodi.update.advertisement.handdrauf.endpoint");
            String adUser = GlobalConfigurationManager.getProperty("wesodi.update.advertisement.handdrauf.user");
            String adPW = GlobalConfigurationManager.getProperty("wesodi.update.advertisement.handdrauf.password");
            String adClaim = GlobalConfigurationManager.getProperty("wesodi.update.advertisement.claim");
            if (doAdStr.equalsIgnoreCase("true")) {
                HandDraufAdvertiserProxy proxy = new HandDraufAdvertiserProxy(adEdpoint);
                Sponsor accountDetails = new Sponsor(adUser, adPW);
                String shopUrl = "";
                if (dpURL.startsWith("http://")) {
                    shopUrl = dpURL.substring(0, dpURL.indexOf("/", 7));
                } else {
                    shopUrl = dpURL.substring(0, dpURL.indexOf("/"));
                }
                Advertisement advertisement = new Advertisement(adClaim, name, shopUrl);
                try {
                    String ret = proxy.addAdvertisement(accountDetails, advertisement);
                    if (ret != null) {
                        System.out.println(ret);
                    }
                } catch (Exception e) {
                    System.out.println("No Advertisement, server unavailable: " + e.getMessage());
                }
            } else {
                System.out.println("No Advertisement");
            }
        }
    }

    /**
	 * Writes Data from multiple DataHandlers in a .zip -File and creates a DataHandler from this file
	 * @param mediaDH DataHandlers for media
	 * @return DataHandler for the .zip - File
	 */
    private DataHandler createZipStreamFromDataHandlers(DataHandler[] mediaDH) {
        if (GlobalConfigurationManager.getCurrentConfigurationManager() != null) {
            String tempDir = GlobalConfigurationManager.getProperty("wesodi.update.temp.directory");
            Calendar now = new GregorianCalendar();
            String nowTime = "" + now.getTimeInMillis();
            File zipOutDir = new File(tempDir);
            if (!zipOutDir.exists()) {
                zipOutDir.mkdirs();
            }
            File zipOutFile = new File(tempDir + "Temp_" + nowTime + ".zip");
            try {
                zipOutFile.createNewFile();
                FileOutputStream dest = new FileOutputStream(zipOutFile);
                ZipOutputStream zipOutStream = new ZipOutputStream(new BufferedOutputStream(dest));
                for (DataHandler dh : mediaDH) {
                    ZipEntry entry = new ZipEntry(new File(dh.getName()).getName());
                    zipOutStream.putNextEntry(entry);
                    dh.writeTo(zipOutStream);
                }
                zipOutStream.close();
                DataHandler out = new DataHandler(new FileDataSource(zipOutFile));
                return out;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
	 * Appends a String with date and time in the log file.
	 * @param s the String to append
	 */
    private void log(String s) {
        String logStr = (new GregorianCalendar().getTime().toString() + " " + s + "\r\n");
        if (fw != null) {
            try {
                fw.append(logStr);
                fw.flush();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(logStr);
    }
}
