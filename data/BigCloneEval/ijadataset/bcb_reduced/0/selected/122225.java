package com.luzan.app.map.tool;

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.io.IOUtils;
import org.hibernate.criterion.Expression;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;
import java.util.Set;
import java.io.File;
import java.io.InputStream;
import com.sun.xfile.XFile;
import com.sun.xfile.XFilenameFilter;
import com.sun.xfile.XFileInputStream;
import com.sun.xfile.XFileOutputStream;
import com.luzan.db.TransactionManager;
import com.luzan.db.DBConnectionFactoryImpl;
import com.luzan.db.dao.GenericDAO;
import com.luzan.db.dao.DAOFactory;
import com.luzan.bean.User;
import com.luzan.parser.map.bean.ozi.WPTPoint;
import com.luzan.parser.map.bean.ozi.WayPointRow;
import com.luzan.parser.map.bean.ozi.PLTTrack;
import com.luzan.parser.map.bean.ozi.MapProjection;
import com.luzan.parser.map.WPTPointParser;
import com.luzan.parser.map.PLTTrackParser;
import com.luzan.parser.map.MAPParser;
import com.luzan.app.map.processor.bean.MapTrackPointsScaleRequest;
import com.luzan.app.map.service.bean.BeanFactory;
import com.luzan.app.map.bean.user.UserMapOriginal;
import com.luzan.app.map.bean.user.UserMapTrack;
import com.luzan.app.map.bean.user.UserMapPoint;
import com.luzan.app.map.pool.GeneralCompleteStrategy;
import com.luzan.app.map.utils.Configuration;
import com.luzan.app.map.manager.MapManager;
import com.luzan.common.pool.PoolClientInterface;
import com.luzan.common.pool.PoolFactory;
import com.luzan.common.pool.StatesStack;

/**
 * Importer
 *
 * @author Alexander Bondar
 */
public class Importer {

    private static final Logger logger = Logger.getLogger(Importer.class);

    protected XFile srcDir;

    protected String login;

    protected String cfg;

    public void setSrcDir(String srcDir) {
        this.srcDir = new XFile(srcDir);
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setCfg(String cfg) {
        this.cfg = cfg;
    }

    private void doIt() throws Throwable {
        GenericDAO<User> dao = DAOFactory.createDAO(User.class);
        try {
            final User user = dao.findUniqueByCriteria(Expression.eq("login", login));
            if (user == null) throw new IllegalArgumentException("Specified user isn't exist");
            if (srcDir.isDirectory() && srcDir.exists()) {
                final String[] fileList = srcDir.list(new XFilenameFilter() {

                    public boolean accept(XFile dir, String file) {
                        String[] fNArr = file.split("\\.");
                        return (fNArr.length > 1 && (fNArr[fNArr.length - 1].equalsIgnoreCase("map") || fNArr[fNArr.length - 1].equalsIgnoreCase("plt") || fNArr[fNArr.length - 1].equalsIgnoreCase("wpt")));
                    }
                });
                int pointsCounter = 0;
                int tracksCounter = 0;
                int mapsCounter = 0;
                for (final String fName : fileList) {
                    try {
                        TransactionManager.beginTransaction();
                    } catch (Throwable e) {
                        logger.error(e);
                        throw e;
                    }
                    final XFile file = new XFile(srcDir, fName);
                    InputStream in = new XFileInputStream(file);
                    try {
                        ArrayList<UserMapOriginal> maps = new ArrayList<UserMapOriginal>();
                        ArrayList<MapTrackPointsScaleRequest> tracks = new ArrayList<MapTrackPointsScaleRequest>();
                        final byte[] headerBuf = new byte[1024];
                        if (in.read(headerBuf) <= 0) continue;
                        final String fileHeader = new String(headerBuf);
                        final boolean isOziWPT = (fileHeader.indexOf("OziExplorer Waypoint File") >= 0);
                        final boolean isOziPLT = (fileHeader.indexOf("OziExplorer Track Point File") >= 0);
                        final boolean isOziMAP = (fileHeader.indexOf("OziExplorer Map Data File") >= 0);
                        final boolean isKML = (fileHeader.indexOf("<kml xmlns=") >= 0);
                        if (isOziMAP || isOziPLT || isOziWPT || isKML) {
                            in.close();
                            in = new XFileInputStream(file);
                        } else continue;
                        WPTPoint wp;
                        if (isOziWPT) {
                            try {
                                wp = new WPTPointParser(in, "Cp1251").parse();
                            } catch (Throwable t) {
                                wp = null;
                            }
                            if (wp != null) {
                                Set<WayPointRow> rows = wp.getPoints();
                                for (WayPointRow row : rows) {
                                    final UserMapPoint p = BeanFactory.createUserPoint(row, user);
                                    logger.info("point:" + p.getGuid());
                                }
                                pointsCounter += rows.size();
                            }
                        } else if (isOziPLT) {
                            PLTTrack plt;
                            try {
                                plt = new PLTTrackParser(in, "Cp1251").parse();
                            } catch (Throwable t) {
                                plt = null;
                            }
                            if (plt != null) {
                                final UserMapTrack t = BeanFactory.createUserTrack(plt, user);
                                tracks.add(new MapTrackPointsScaleRequest(t));
                                tracksCounter++;
                                logger.info("track:" + t.getGuid());
                            }
                        } else if (isOziMAP) {
                            MapProjection projection;
                            MAPParser parser = new MAPParser(in, "Cp1251");
                            try {
                                projection = parser.parse();
                            } catch (Throwable t) {
                                projection = null;
                            }
                            if (projection != null && projection.getPoints() != null && projection.getPoints().size() >= 2) {
                                GenericDAO<UserMapOriginal> mapDao = DAOFactory.createDAO(UserMapOriginal.class);
                                final UserMapOriginal mapOriginal = new UserMapOriginal();
                                mapOriginal.setName(projection.getTitle());
                                mapOriginal.setUser(user);
                                mapOriginal.setState(UserMapOriginal.State.UPLOAD);
                                mapOriginal.setSubstate(UserMapOriginal.SubState.COMPLETE);
                                MapManager.updateProjection(projection, mapOriginal);
                                final XFile srcFile = new XFile(srcDir, projection.getPath());
                                if (!srcFile.exists() || !srcFile.isFile()) throw new IllegalArgumentException("file: " + srcFile.getPath() + " not found");
                                final XFile mapStorage = new XFile(new XFile(Configuration.getInstance().getPrivateMapStorage().toString()), mapOriginal.getGuid());
                                mapStorage.mkdir();
                                XFile dstFile = new XFile(mapStorage, mapOriginal.getGuid());
                                XFileOutputStream out = new XFileOutputStream(dstFile);
                                XFileInputStream inImg = new XFileInputStream(srcFile);
                                IOUtils.copy(inImg, out);
                                out.flush();
                                out.close();
                                inImg.close();
                                mapDao.save(mapOriginal);
                                maps.add(mapOriginal);
                                srcFile.delete();
                                mapsCounter++;
                                logger.info("map:" + mapOriginal.getGuid());
                            }
                        } else logger.warn("unsupported file format: " + file.getName());
                        TransactionManager.commitTransaction();
                        for (MapTrackPointsScaleRequest track : tracks) {
                            if (track != null) {
                                try {
                                    PoolClientInterface pool = PoolFactory.getInstance().getClientPool();
                                    if (pool == null) throw new IllegalStateException("pool not found");
                                    pool.put(track, new StatesStack(new byte[] { 0x00, 0x11 }), GeneralCompleteStrategy.class);
                                } catch (Throwable t) {
                                    logger.error(t);
                                }
                            }
                        }
                    } catch (Throwable e) {
                        logger.error("Error importing", e);
                        TransactionManager.rollbackTransaction();
                    } finally {
                        in.close();
                        file.delete();
                    }
                }
                logger.info("waypoints: " + pointsCounter + "\ntracks: " + tracksCounter + "\nmaps: " + mapsCounter);
            }
        } catch (Throwable e) {
            logger.error("Error importing", e);
        }
    }

    public static void main(String args[]) {
        Importer importer = new Importer();
        String allArgs = StringUtils.join(args, ' ');
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(Importer.class, Object.class);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                Pattern p = Pattern.compile("-" + pd.getName() + "\\s*([\\S]*)", Pattern.CASE_INSENSITIVE);
                final Matcher m = p.matcher(allArgs);
                if (m.find()) {
                    pd.getWriteMethod().invoke(importer, m.group(1));
                }
            }
            Configuration.getInstance().load(importer.cfg);
            DBConnectionFactoryImpl.configure(importer.cfg);
            PoolFactory.getInstance(new File("pool.properties").toURL().toString());
            importer.doIt();
        } catch (Throwable e) {
            logger.error("error", e);
            System.out.println(e.getMessage());
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(Importer.class);
                System.out.println("Options:");
                for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                    System.out.println("-" + pd.getName());
                }
            } catch (Throwable t) {
                System.out.print("Internal error");
            }
        }
    }
}
