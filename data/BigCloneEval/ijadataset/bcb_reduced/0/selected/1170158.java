package net.cloneshop.apexcost;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.*;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import org.richfaces.event.UploadEvent;
import org.richfaces.model.UploadItem;
import net.cloneshop.apexcost.entity.*;
import net.cloneshop.apexcost.utils.*;

public class MainBean {

    private MainUtil uBean;

    private FileUtil fileUtil;

    private String rootPath;

    private Company com;

    private Contact con;

    private MarketingRequest mr;

    private ApexFile f;

    public MainBean() {
        super();
        ServletContext context;
        context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        rootPath = context.getRealPath("/").replace("\\.\\", "\\");
        fileUtil = new FileUtil();
        com = new Company();
        con = new Contact();
        mr = new MarketingRequest();
        f = new ApexFile();
    }

    public MainUtil getuBean() {
        return uBean;
    }

    public void setuBean(MainUtil uBean) {
        this.uBean = uBean;
    }

    public FileUtil getFileUtil() {
        return fileUtil;
    }

    public void setFileUtil(FileUtil fileUtil) {
        this.fileUtil = fileUtil;
    }

    public Company getCom() {
        return com;
    }

    public void setCom(Company com) {
        this.com = com;
    }

    public Contact getCon() {
        return con;
    }

    public void setCon(Contact con) {
        this.con = con;
    }

    public MarketingRequest getMr() {
        return mr;
    }

    public void setMr(MarketingRequest mr) {
        this.mr = mr;
    }

    public ApexFile getF() {
        return f;
    }

    public void setF(ApexFile f) {
        this.f = f;
    }

    public String loadObject() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        String ID = (String) context.getRequestParameterMap().get("oID");
        String Type = (String) context.getRequestParameterMap().get("Type");
        String returnString = null;
        switch(Integer.valueOf(Type)) {
            case 1:
                com = (Company) ObjectUtil.loadObject(uBean.getCompanies(), Long.valueOf(ID));
                returnString = "com";
                break;
            case 2:
                con = (Contact) ObjectUtil.loadObject(com.getContacts(), Long.valueOf(ID));
                break;
            case 3:
                f = (ApexFile) ObjectUtil.loadObject(uBean.getFiles(), Long.valueOf(ID));
                returnString = "f";
                break;
            case 4:
                mr = (MarketingRequest) ObjectUtil.loadObject(uBean.getMarketingRequests(), Long.valueOf(ID));
                returnString = "mr";
                break;
            case 11:
                com = new Company();
                returnString = "com";
                break;
            case 13:
                f = new ApexFile();
                returnString = "f";
                break;
            case 14:
                mr = new MarketingRequest();
                returnString = "mr";
                break;
            default:
                break;
        }
        return returnString;
    }

    public String removeObject() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        String ID = (String) context.getRequestParameterMap().get("oID");
        String Type = (String) context.getRequestParameterMap().get("Type");
        String returnString = null;
        switch(Integer.valueOf(Type)) {
            case 1:
                removeCompany(Long.valueOf(ID));
                break;
            case 2:
                removeCContact(Long.valueOf(ID));
                break;
            case 3:
                removeFile(Long.valueOf(ID));
                break;
            case 4:
                removeMarketingRequest(Long.valueOf(ID));
                break;
            default:
                break;
        }
        return returnString;
    }

    public String persistCompany() {
        uBean.getPojo().persistCompany(com);
        uBean.refreshCompanies();
        com = new Company();
        return "clist";
    }

    public String persistMarketingRequest() {
        mr.getMpackage().setMrequest(mr);
        Long mrID = uBean.getPojo().persistMarketingRequest(mr);
        uBean.refreshMarketingRequests();
        zipFilemaker(mrID);
        mr = new MarketingRequest();
        return "mrlist";
    }

    public String persistFile() {
        uBean.getPojo().persistFile(f);
        uBean.refreshFiles();
        f = new ApexFile();
        return "flist";
    }

    public String resetCompany() {
        if (null != com.getID()) uBean.getPojo().refreshCompany(com);
        com = new Company();
        return "clist";
    }

    public String resetMarketingRequest() {
        if (null != mr.getID()) uBean.getPojo().refreshMarketingRequest(mr);
        mr = new MarketingRequest();
        return "mrlist";
    }

    public String resetFile() {
        if (null != f.getID()) uBean.getPojo().refreshFile(f);
        f = new ApexFile();
        return "flist";
    }

    public void removeCompany(Long ID) {
        uBean.getPojo().removeCompany(ID);
        uBean.refreshCompanies();
    }

    public void removeMarketingRequest(Long ID) {
        uBean.getPojo().removeMarketingRequest(ID);
        uBean.refreshMarketingRequests();
    }

    public void removeFile(Long ID) {
        uBean.getPojo().removeFile(ID);
        uBean.refreshFiles();
    }

    public void addCContact() {
        con.setCompany(com);
        if (com.getContacts().contains(con)) {
            int i = com.getContacts().indexOf(con);
            com.getContacts().remove(i);
            com.getContacts().add(i, con);
        } else {
            com.getContacts().add(con);
        }
        con = new Contact();
    }

    public void resetCContact() {
        con = new Contact();
    }

    public void removeCContact(Long ID) {
        Contact c = (Contact) ObjectUtil.loadObject(com.getContacts(), ID);
        if (com.getContacts().contains(c)) {
            com.getContacts().remove(c);
        }
    }

    public List<SelectItem> CompanyContacts(Company co) {
        Contact c;
        ArrayList<SelectItem> outList = new ArrayList<SelectItem>(0);
        outList.add(new SelectItem(null, "---"));
        if (null != co) {
            Iterator<Contact> i = co.getContacts().iterator();
            while (i.hasNext()) {
                c = i.next();
                outList.add(new SelectItem(c, c.getName()));
            }
        }
        return outList;
    }

    public List<SelectItem> getClientContacts() {
        return CompanyContacts(mr.getClient());
    }

    public List<SelectItem> getOwnerContacts() {
        return CompanyContacts(mr.getOwner());
    }

    public void zipFilemaker(Long ID) {
        MarketingRequest mr = (MarketingRequest) ObjectUtil.loadObject(uBean.getMarketingRequests(), Long.valueOf(ID));
        if (null == mr) return;
        MarketingPackage mp = mr.getMpackage();
        if (null == mp || null == mp.getID()) return;
        try {
            ApexFile f;
            String path = rootPath + "files" + File.separatorChar;
            byte[] buf = new byte[1024];
            Iterator<ApexFile> i = mp.getFiles().iterator();
            ZipOutputStream zf = new ZipOutputStream(new FileOutputStream(path + "mpackage_" + mp.getID() + ".zip"));
            while (i.hasNext()) {
                f = i.next();
                FileInputStream in = new FileInputStream(path + f.getFsname());
                zf.putNextEntry(new ZipEntry(f.getName()));
                int len;
                while ((len = in.read(buf)) > 0) {
                    zf.write(buf, 0, len);
                }
                zf.closeEntry();
                in.close();
            }
            zf.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void fileListener(UploadEvent e) {
        UploadItem ui = e.getUploadItem();
        f.setUploadDate(new Date());
        f.setContentType(ui.getContentType());
        f.setName(ui.getFileName().substring(ui.getFileName().lastIndexOf(File.separatorChar) + 1));
        f.setSize(ui.getFileSize());
        if (ui.isTempFile()) {
            String path;
            int sindex = f.getName().lastIndexOf(".");
            if (sindex > -1) {
                f.setFsname(System.currentTimeMillis() + f.getName().substring(sindex));
            } else {
                f.setFsname(System.currentTimeMillis() + "");
            }
            path = rootPath + "files" + File.separatorChar + f.getFsname();
            ui.getFile().renameTo(new File(path));
        }
    }
}
