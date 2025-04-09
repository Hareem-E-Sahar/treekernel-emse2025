package com.hk.web.pub.action;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.hk.bean.BizCircle;
import com.hk.bean.Box;
import com.hk.bean.BoxPretype;
import com.hk.bean.BoxPrize;
import com.hk.bean.City;
import com.hk.bean.CmdCmp;
import com.hk.bean.CmdProduct;
import com.hk.bean.CmpActor;
import com.hk.bean.CmpActorPink;
import com.hk.bean.CmpAdminUser;
import com.hk.bean.CmpCheckInUserLog;
import com.hk.bean.CmpProduct;
import com.hk.bean.CmpZoneInfo;
import com.hk.bean.Company;
import com.hk.bean.CompanyKind;
import com.hk.bean.CompanyKindUtil;
import com.hk.bean.Country;
import com.hk.bean.Coupon;
import com.hk.bean.Feed;
import com.hk.bean.HkObj;
import com.hk.bean.HkObjOrder;
import com.hk.bean.HkObjOrderDef;
import com.hk.bean.Laba;
import com.hk.bean.Mayor;
import com.hk.bean.ParentKind;
import com.hk.bean.Province;
import com.hk.bean.User;
import com.hk.bean.UserLastCheckIn;
import com.hk.bean.UserOtherInfo;
import com.hk.frame.util.DataUtil;
import com.hk.frame.util.DesUtil;
import com.hk.frame.util.HkUtil;
import com.hk.frame.util.ServletUtil;
import com.hk.frame.util.page.SimplePage;
import com.hk.frame.web.http.HkRequest;
import com.hk.frame.web.http.HkResponse;
import com.hk.svr.BizCircleService;
import com.hk.svr.BoxService;
import com.hk.svr.CmpActorService;
import com.hk.svr.CmpAdminUserService;
import com.hk.svr.CmpCheckInService;
import com.hk.svr.CmpProductService;
import com.hk.svr.CompanyService;
import com.hk.svr.HkObjOrderService;
import com.hk.svr.HkObjService;
import com.hk.svr.LabaService;
import com.hk.svr.UserService;
import com.hk.svr.ZoneService;
import com.hk.svr.processor.CmpActorProcessor;
import com.hk.svr.processor.CmpCheckInProcessor;
import com.hk.svr.processor.FeedProcessor;
import com.hk.svr.pub.BoxPretypeUtil;
import com.hk.svr.pub.CheckInPointConfig;
import com.hk.svr.pub.Err;
import com.hk.svr.pub.HkDataCompositor;
import com.hk.svr.pub.ZoneUtil;
import com.hk.web.company.action.CompanyVo;
import com.hk.web.feed.action.FeedVo;
import com.hk.web.util.HkWebUtil;

@Component("/index")
public class IndexAction extends BaseAction {

    @Autowired
    private CompanyService companyService;

    @Autowired
    private HkObjOrderService hkObjOrderService;

    @Autowired
    private UserService userService;

    @Autowired
    private ZoneService zoneService;

    @Autowired
    private LabaService labaService;

    @Autowired
    private HkObjService hkObjService;

    @Autowired
    private BizCircleService bizCircleService;

    @Autowired
    private CmpProductService cmpProductService;

    @Autowired
    private CmpCheckInService cmpCheckInService;

    @Autowired
    private BoxService boxService;

    @Autowired
    private CmpCheckInProcessor cmpCheckInProcessor;

    @Autowired
    private FeedProcessor feedProcessor;

    @Autowired
    private CmpAdminUserService cmpAdminUserService;

    @Autowired
    private CmpActorProcessor cmpActorProcessor;

    @Autowired
    private CmpActorService cmpActorService;

    public String execute(HkRequest req, HkResponse resp) throws Exception {
        return "r:/square.do";
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String h(HkRequest req, HkResponse resp) {
        int pcityId = this.getPcityId(req);
        return "/index_city.do?cityId=" + pcityId;
    }

    /**
	 * wap新首页
	 * 
	 * @param req
	 * @param resp
	 * @return
	 *         2010-4-22
	 */
    public String city(HkRequest req, HkResponse resp) {
        int cityId = req.getIntAndSetAttr("cityId");
        User loginUser = this.getLoginUser(req);
        List<Box> boxlist = this.boxService.getCanOpenBoxListByCityId(cityId, Box.PINKFLG_TOP, 0, 1);
        if (boxlist.size() == 0) {
            boxlist.addAll(this.boxService.getCanOpenBoxListByNoCity(Box.PINKFLG_TOP, 0, 1));
        }
        boxlist.addAll(this.boxService.getCanOpenBoxListByCityId(cityId, Box.PINKFLG_PINK, 0, 3));
        boxlist.addAll(this.boxService.getCanOpenBoxListByNoCity(Box.PINKFLG_PINK, 0, 3));
        boxlist = DataUtil.subList(boxlist, 0, 3);
        req.setAttribute("pinkboxlist", boxlist);
        List<CmpCheckInUserLog> loglist = null;
        if (loginUser != null) {
            loglist = this.cmpCheckInProcessor.getFriendCmpCheckInUserLog(loginUser.getUserId(), true, true, 5);
        } else {
            loglist = this.cmpCheckInProcessor.getCmpCheckInUserLogListByPcityId(cityId, true, true, 0, 5);
        }
        req.setAttribute("loglist", loglist);
        List<Feed> feedlist = this.feedProcessor.getFeedListByFeedType(Feed.FEEDTYPE_OPENBOX, false, 0, 80);
        LinkedHashMap<Long, Feed> feedMap = new LinkedHashMap<Long, Feed>();
        for (Feed feed : feedlist) {
            if (!feedMap.containsKey(feed.getUserId())) {
                feedMap.put(feed.getUserId(), feed);
            }
        }
        feedlist = new ArrayList<Feed>(feedMap.values());
        List<FeedVo> boxfeedvolist = FeedVo.createList(req, feedlist, true, true);
        boxfeedvolist = DataUtil.subList(boxfeedvolist, 0, 5);
        req.setAttribute("boxfeedvolist", boxfeedvolist);
        return this.getWapJsp("index2.jsp");
    }

    /**
	 * 首页信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String all(HkRequest req, HkResponse resp) {
        this.processCompanyKindModule(req);
        this.processAllCompanyModule(req);
        this.processAllLabaModule(req);
        this.processAllUserModule(req);
        return this.getWapJsp("pub/index.jsp");
    }

    /**
	 * 首页信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String cityweb(HkRequest req, HkResponse resp) throws Exception {
        int cityId = req.getInt("cityId");
        List<HkObjOrder> orderlist = this.hkObjOrderService.getHkObjOrderListForOrder(HkObjOrderDef.KIND_LEVEL_1, cityId, 0, 20);
        List<Long> idList = new ArrayList<Long>();
        for (HkObjOrder o : orderlist) {
            idList.add(o.getHkObjId());
        }
        Map<Long, HkObj> ordermap = this.hkObjService.getHkObjMapInId(idList);
        for (HkObjOrder o : orderlist) {
            o.setHkObj(ordermap.get(o.getHkObjId()));
        }
        req.setAttribute("orderlist", orderlist);
        req.setAttribute("cityId", cityId);
        req.reSetAttribute("showorder");
        String zone = ZoneUtil.getZoneName(cityId);
        if (zone == null) {
            zone = req.getText("view.zone_all");
        }
        req.setAttribute("zone", zone);
        return this.getWeb2Jsp("pub/index.jsp");
    }

    /**
	 * 首页信息
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String docityweb(HkRequest req, HkResponse resp) throws Exception {
        int cityId = req.getInt("cityId");
        List<HkObjOrder> orderlist = this.hkObjOrderService.getHkObjOrderListForOrder(HkObjOrderDef.KIND_LEVEL_1, cityId, 0, 20);
        List<Long> idList = new ArrayList<Long>();
        for (HkObjOrder o : orderlist) {
            idList.add(o.getHkObjId());
        }
        Map<Long, HkObj> ordermap = this.hkObjService.getHkObjMapInId(idList);
        for (HkObjOrder o : orderlist) {
            o.setHkObj(ordermap.get(o.getHkObjId()));
        }
        req.setAttribute("orderlist", orderlist);
        req.setAttribute("cityId", cityId);
        req.reSetAttribute("showorder");
        return this.getWebJsp("pub/index.jsp");
    }

    private void processCompanyKindModule(HkRequest req) {
        List<CompanyKind> kindList = CompanyKindUtil.getCompanKindList();
        req.setAttribute("kindList", kindList);
    }

    private void processAllCompanyModule(HkRequest req) {
        Set<Long> idSet = new TreeSet<Long>();
        List<Company> moneyorderlist = this.companyService.getCompanyListEx(0, 0, null, null, moneyHkDataCompositor, 0, 2);
        for (Company o : moneyorderlist) {
            idSet.add(o.getCompanyId());
        }
        List<Company> memberorderlist = this.companyService.getCompanyListEx(0, 0, null, idSet, memberHkDataCompositor, 0, 2);
        for (Company o : memberorderlist) {
            idSet.add(o.getCompanyId());
        }
        List<Company> scoreorderlist = this.companyService.getCompanyListEx(0, 0, null, idSet, scoreHkDataCompositor, 0, 2);
        List<Company> cmplist = new ArrayList<Company>();
        cmplist.addAll(moneyorderlist);
        cmplist.addAll(memberorderlist);
        cmplist.addAll(scoreorderlist);
        List<CompanyVo> companyvolist = new ArrayList<CompanyVo>();
        for (Company c : cmplist) {
            CompanyVo vo = new CompanyVo(c);
            companyvolist.add(vo);
        }
        req.setAttribute("companyvolist", companyvolist);
    }

    private void processAllUserModule(HkRequest req) {
        List<User> userlist = this.userService.getUserListSortUserRecentUpdate(0, 4);
        if (userlist.size() == 4) {
            req.setAttribute("moreuser", true);
            userlist.remove(3);
        }
        req.setAttribute("listalluser", true);
        req.setAttribute("userlist", userlist);
    }

    private void processAllLabaModule(HkRequest req) {
        List<Laba> labalist = null;
        labalist = this.labaService.getLabaList(0, 7);
        List<LabaVo> labavolist = LabaVo.createVoList(labalist, this.getLabaParserCfg(req));
        if (labalist.size() == 7) {
            req.setAttribute("morelaba", true);
            labalist.remove(6);
        }
        req.setAttribute("listalllaba", true);
        req.setAttribute("labavolist", labavolist);
    }

    /**
	 * 到查询地区或城市页面 fn:2创建足迹
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String tosearchcity(HkRequest req, HkResponse resp) throws Exception {
        List<Country> countrylist = ZoneUtil.getCountryList();
        req.setAttribute("countrylist", countrylist);
        req.reSetAttribute("fn");
        req.reSetAttribute("kindId");
        req.reSetAttribute("hide_all");
        boolean show_country = req.getBoolean("show_country");
        req.setAttribute("show_country", show_country);
        return "/WEB-INF/page/pub/searchcity.jsp";
    }

    /**
	 * 附近足迹
	 * 
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String searchcity(HkRequest req, HkResponse resp) throws Exception {
        User loginUser = this.getLoginUser(req);
        String name = req.getString("name");
        boolean noresult = true;
        req.reSetAttribute("kindId");
        req.reSetAttribute("fn");
        req.setAttribute("noresult", noresult);
        req.reSetAttribute("countryId");
        boolean show_country = req.getBoolean("show_country");
        req.setAttribute("show_country", show_country);
        if (DataUtil.isEmpty(name)) {
            return "/WEB-INF/page/pub/searchcity.jsp";
        }
        if (!DataUtil.isEmpty(name)) {
            name = DataUtil.filterZoneName(name);
        }
        City city = this.zoneService.getCityLike(name);
        int cityId = 0;
        if (city != null) {
            cityId = city.getCityId();
        }
        if (cityId > 0) {
            if (loginUser != null) {
                User user = this.userService.getUser(loginUser.getUserId());
                if (user.getPcityId() != cityId) {
                    this.userService.updateUserPcityId(loginUser.getUserId(), cityId);
                }
                loginUser.setPcityId(cityId);
            }
        }
        if (cityId > 0) {
            return this.routefn(req, cityId);
        }
        return "/WEB-INF/page/pub/searchcity.jsp";
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 * @throws Exception
	 */
    public String routefn(HkRequest req, HkResponse resp) throws Exception {
        int cityId = req.getInt("cityId");
        return this.routefn(req, cityId);
    }

    public String routefn(HkRequest req, int cityId) throws Exception {
        int fn = req.getInt("fn");
        if (cityId > 0) {
            if (fn == 0) {
                return "r:/index_city.do?cityId=" + cityId;
            }
            if (fn == 1) {
                return "r:/e/cmp_list2.do?cityId=" + cityId + "&kindId=" + req.getInt("kindId");
            }
            if (fn == 2) {
                return "r:/e/op/op_toadd.do?cityId=" + cityId;
            }
        }
        if (fn == 3) {
            if (cityId == 0) {
                req.setText("func.select_city");
                req.setAttribute("fn", fn);
                return "/index_tosearchcity.do";
            }
            return "r:/admin/orderdef_cmpkindlist.do?cityId=" + cityId;
        }
        if (fn == 4) {
            if (cityId == 0) {
                req.setText("func.select_city");
                req.setAttribute("fn", fn);
                return "/index_tosearchcity.do";
            }
            return "r:/admin/orderdef_findkeytag.do?cityId=" + cityId;
        }
        if (fn == 5) {
            if (cityId == 0) {
                req.setText("func.select_city");
                req.setAttribute("fn", fn);
                return "/index_tosearchcity.do";
            }
            return "r:/e/op/cmporder_findkeytag.do?cityId=" + cityId;
        }
        return null;
    }

    public String chgcity(HkRequest req, HkResponse resp) {
        int pcityId = req.getInt("pcityId");
        HkWebUtil.setPcityId(req, resp, pcityId);
        return "r:/index_web.do";
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String web(HkRequest req, HkResponse resp) {
        List<ParentKind> parentKindList = CompanyKindUtil.getParentList();
        req.setAttribute("parentKindList", parentKindList);
        int pcityId = this.getPcityId(req);
        if (pcityId > 0) {
            City city = ZoneUtil.getCity(pcityId);
            if (city != null) {
                List<BizCircle> circlelist = this.bizCircleService.getBizCircleListForHasCmp(city.getCityId());
                req.setAttribute("circlelist", circlelist);
            }
            List<Company> hotcmplist = this.companyService.getCompanyListForHot(0, pcityId, 0, 8);
            req.setAttribute("hotcmplist", hotcmplist);
            List<CmdCmp> cmdcmplistdata = this.companyService.getCmdCmpList(pcityId, 0, 7);
            List<Long> idList = new ArrayList<Long>();
            for (CmdCmp cmdCmp : cmdcmplistdata) {
                idList.add(cmdCmp.getCompanyId());
            }
            List<Company> cmdcmplist = this.companyService.getCompanyListInId(idList, null);
            req.setAttribute("cmdcmplist", cmdcmplist);
            List<CmdProduct> cmdproductlistdata = this.cmpProductService.getCmdProductList(pcityId, 0, 8);
            idList = new ArrayList<Long>();
            for (CmdProduct cmdProduct : cmdproductlistdata) {
                idList.add(cmdProduct.getProductId());
            }
            List<CmpProduct> cmdproductlist = this.cmpProductService.getCmpProductListInId(idList);
            req.setAttribute("cmdproductlist", cmdproductlist);
            if (cmdproductlistdata.size() == 0) {
                int size = 6;
                int count = this.cmpProductService.countCmpProductByPcityIdAndGood(pcityId);
                int begin = DataUtil.getRandomPageBegin(count, size);
                List<CmpProduct> randomproductlist = this.cmpProductService.getCmpProductListByPcityIdAndGood(pcityId, begin, size);
                req.setAttribute("randomproductlist", randomproductlist);
            }
            List<Company> userlikecmplist = this.companyService.getCompanyListForUserLike(pcityId, 0, 4);
            req.setAttribute("userlikecmplist", userlikecmplist);
            List<CmpProduct> userlikeproductlist = this.cmpProductService.getCmpProductListByPcityIdAndGood(pcityId, 0, 4);
            req.setAttribute("userlikeproductlist", userlikeproductlist);
            req.setAttribute("pcityId", pcityId);
        }
        return this.getWeb3Jsp("index/index.jsp");
    }

    private HkDataCompositor moneyHkDataCompositor = new HkDataCompositor() {

        public String getField() {
            return "totalmoney";
        }

        public String getOrderType() {
            return "desc";
        }
    };

    private HkDataCompositor memberHkDataCompositor = new HkDataCompositor() {

        public String getField() {
            return "companyid";
        }

        public String getOrderType() {
            return "desc";
        }
    };

    private HkDataCompositor scoreHkDataCompositor = new HkDataCompositor() {

        public String getField() {
            return "totalscore";
        }

        public String getOrderType() {
            return "desc";
        }
    };

    private Color getRandColor(int fc, int bc) {
        int fc_a = fc;
        int bc_a = bc;
        Random random = new Random();
        if (fc > 255) fc_a = 255;
        if (bc > 255) bc_a = 255;
        int r = fc + random.nextInt(bc_a - fc_a);
        int g = fc + random.nextInt(bc_a - fc_a);
        int b = fc + random.nextInt(bc_a - fc_a);
        return new Color(r, g, b);
    }

    private void drawLine(Graphics g, Random random, int width, int height) {
        for (int i = 0; i < 155; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            g.drawLine(x, y, x + xl, y + yl);
        }
    }

    private String drawNumber(Graphics g, Random random) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            int number = (int) (Math.random() * 10);
            sb.append(number);
        }
        String rand = sb.toString();
        g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
        g.drawString(rand, 13 * 1 + 6, 16);
        return rand;
    }

    private void setProperty(Graphics g, int width, int height) {
        g.setColor(getRandColor(200, 250));
        g.fillRect(0, 0, width, height);
        g.setFont(new Font("Times New Roman", Font.PLAIN, 18));
        g.setColor(new Color(255, 255, 255));
        g.drawRect(0, 0, width - 1, height - 1);
    }

    /**
	 * 生成验证码
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
    public String showImg(HkRequest req, HkResponse resp) {
        resp.reset();
        resp.setContentType("image/png");
        resp.setHeader("Pragma", "No-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        int width = 70, height = 20;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        this.setProperty(g, width, height);
        Random random = new Random();
        this.drawLine(g, random, width, height);
        g.setColor(getRandColor(160, 200));
        String sRand = this.drawNumber(g, random);
        g.dispose();
        req.setSessionValue(HkUtil.CLOUD_IMAGE_AUTH, sRand);
        ServletOutputStream sos = null;
        try {
            sos = resp.getOutputStream();
            ImageIO.write(image, "JPEG", sos);
        } catch (IOException e) {
            System.out.println(e);
            e.printStackTrace();
        } finally {
            if (sos != null) try {
                sos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
	 * 首页显示地区的动态以及报到情况
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
    public String index4(HkRequest req, HkResponse resp) {
        if (req.getServerName().indexOf("huoku.net") != -1) {
            return this.index4_meifa(req, resp);
        }
        return this.index4_old(req, resp);
    }

    /**
	 * 首页显示地区的动态以及报到情况
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
    public String index4_meifa(HkRequest req, HkResponse resp) {
        int kindId = 4;
        List<Company> newcmplist = this.companyService.getCompanyListByKindIdForNew(kindId, 0, 6);
        req.setAttribute("newcmplist", newcmplist);
        List<Company> kucmplist = this.companyService.getCompanyListByKindIdForWorkCount(kindId, 0, 6);
        req.setAttribute("kucmplist", kucmplist);
        List<CmpActor> kuactorlist = this.cmpActorService.getCmpActorListForWorkCount(0, 8);
        req.setAttribute("kuactorlist", kuactorlist);
        List<CmpActorPink> pinkactorlist = this.cmpActorProcessor.getCmpActorPinkList(true, true, 0, 4);
        req.setAttribute("pinkactorlist", pinkactorlist);
        List<Coupon> couponlist = null;
        req.setAttribute("couponlist", couponlist);
        return this.getWeb4Jsp("index/index_meifa.jsp");
    }

    /**
	 * 首页显示地区的动态以及报到情况
	 * 
	 * @param req
	 * @param resp
	 * @return
	 */
    public String index4_old(HkRequest req, HkResponse resp) {
        req.setAttribute("to_index", true);
        int pcityId = this.getPcityId(req);
        List<Company> cmplist = this.companyService.getCompanyListByPcityId(pcityId, 0, 10);
        req.setAttribute("cmplist", cmplist);
        List<Mayor> c = this.getNewMayorList(pcityId);
        List<Long> idList = new ArrayList<Long>();
        for (Mayor o : c) {
            idList.add(o.getUserId());
        }
        Map<Long, User> usermap = this.userService.getUserMapInId(idList);
        Map<Long, UserLastCheckIn> lastCheckInMap = this.cmpCheckInService.getUserLastCheckInMapInId(idList);
        for (Mayor o : c) {
            o.setUser(usermap.get(o.getUserId()));
        }
        idList.clear();
        for (Mayor o : c) {
            UserLastCheckIn userLastCheckIn = lastCheckInMap.get(o.getUserId());
            if (userLastCheckIn != null) {
                long id = userLastCheckIn.getLastLogId();
                if (id > 0) {
                    idList.add(id);
                }
            }
        }
        Map<Long, CmpCheckInUserLog> logmap = this.cmpCheckInService.getCmpCheckInUserLogMapInId(idList);
        for (Mayor o : c) {
            UserLastCheckIn userLastCheckIn = lastCheckInMap.get(o.getUserId());
            if (userLastCheckIn != null) {
                long logId = userLastCheckIn.getLastLogId();
                CmpCheckInUserLog log = logmap.get(logId);
                if (log != null) {
                    o.setLastCmpCheckInUserLog(log);
                }
            }
        }
        idList.clear();
        for (Mayor o : c) {
            if (o.getLastCmpCheckInUserLog() != null) {
                idList.add(o.getLastCmpCheckInUserLog().getCompanyId());
            }
        }
        Map<Long, Company> cmpmap = this.companyService.getCompanyMapInId(idList);
        for (Mayor o : c) {
            if (o.getLastCmpCheckInUserLog() != null) {
                o.getLastCmpCheckInUserLog().setCompany(cmpmap.get(o.getLastCmpCheckInUserLog().getCompanyId()));
            }
        }
        Collections.reverse(c);
        req.setAttribute("mayorlist", c);
        List<Box> boxlist = this.boxService.getCanOpenBoxListByCityId(pcityId, Box.PINKFLG_TOP, 0, 1);
        if (boxlist.size() == 0) {
            boxlist.addAll(this.boxService.getCanOpenBoxListByNoCity(Box.PINKFLG_TOP, 0, 1));
        }
        List<Box> pinkboxlist = this.boxService.getCanOpenBoxListByCityId(pcityId, Box.PINKFLG_PINK, 0, 5);
        pinkboxlist.addAll(this.boxService.getCanOpenBoxListByNoCity(Box.PINKFLG_PINK, 0, 5));
        if (pinkboxlist.size() > 0 && boxlist.size() == 0) {
            boxlist.add(pinkboxlist.remove(0));
        }
        pinkboxlist = DataUtil.subList(pinkboxlist, 0, 5);
        req.setAttribute("pinkboxlist", pinkboxlist);
        if (boxlist.size() > 0) {
            Box box = boxlist.get(0);
            List<BoxPrize> boxprizelist = this.boxService.getBoxPrizeListByBoxId(box.getBoxId());
            if (boxprizelist.size() > 4) {
                boxprizelist = boxprizelist.subList(0, 4);
            }
            req.setAttribute("boxprizelist", boxprizelist);
            int piccount = 0;
            for (BoxPrize o : boxprizelist) {
                if (o.getPath() != null) {
                    piccount++;
                }
            }
            req.setAttribute("piccount", piccount);
            BoxPretype boxPretype = BoxPretypeUtil.getBoxPretype(box.getPretype());
            req.setAttribute("boxPretype", boxPretype);
            req.setAttribute("box", box);
            User loginUser = this.getLoginUser(req);
            if (loginUser != null) {
                UserOtherInfo userOtherInfo = this.userService.getUserOtherInfo(loginUser.getUserId());
                int canOpenBoxCount = userOtherInfo.getPoints() / CheckInPointConfig.getOpenBoxPoints();
                req.setAttribute("canOpenBoxCount", canOpenBoxCount);
            }
        }
        return this.getWeb4Jsp("index/index.jsp");
    }

    private List<Mayor> getNewMayorList(int pcityId) {
        boolean needmore = true;
        LinkedHashMap<Long, Mayor> map = new LinkedHashMap<Long, Mayor>();
        int page = 1;
        int size = 20;
        while (map.size() < 10 && needmore) {
            SimplePage sp = new SimplePage(size, page);
            List<Mayor> mayorlist = this.cmpCheckInService.getMayorListByPcityId(pcityId, sp.getBegin(), sp.getSize());
            if (mayorlist.size() == 0) {
                needmore = false;
                break;
            }
            Collections.reverse(mayorlist);
            for (Mayor o : mayorlist) {
                map.put(o.getUserId(), o);
            }
            if (map.size() >= 10) {
                needmore = false;
                break;
            }
            page++;
        }
        Collection<Mayor> c = map.values();
        List<Mayor> list = new ArrayList<Mayor>(c);
        return list;
    }

    /**
	 * 火酷动态
	 * 
	 * @param req
	 * @param resp
	 * @return
	 *         2010-4-12
	 */
    public String feed(HkRequest req, HkResponse resp) {
        req.setAttribute("to_feed", true);
        int pcityId = 0;
        int cityflg = req.getIntAndSetAttr("cityflg");
        if (cityflg == 1) {
            pcityId = this.getPcityId(req);
        }
        List<Feed> list = null;
        if (cityflg == 1) {
            list = this.feedProcessor.getFeedListByCityIdForIndex(pcityId, 0, 20);
        } else {
            list = this.feedProcessor.getFeedListForIndex(0, 20);
        }
        List<FeedVo> feedvolist = FeedVo.createList(req, list, false, false);
        req.setAttribute("feedvolist", feedvolist);
        List<CmpCheckInUserLog> cmpCheckInUserLogList = this.cmpCheckInProcessor.getCmpCheckInUserLogListByPcityId(pcityId, true, true, 0, 10);
        if (cmpCheckInUserLogList.size() == 0) {
            cmpCheckInUserLogList = this.cmpCheckInProcessor.getCmpCheckInUserLogListByPcityId(0, true, true, 0, 10);
        }
        req.setAttribute("cmpCheckInUserLogList", cmpCheckInUserLogList);
        return this.getWeb4Jsp("index/feed.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selzone(HkRequest req, HkResponse resp) {
        int provinceId = req.getIntAndSetAttr("provinceId");
        if (provinceId > 0) {
            Province province = ZoneUtil.getProvince(provinceId);
            req.setAttribute("province", province);
            List<City> citylist = this.zoneService.getCityList(provinceId);
            req.setAttribute("citylist", citylist);
            return this.getWeb4Jsp("zone/selcityfromprovince.jsp");
        }
        req.reSetAttribute("countryId");
        return this.getWeb4Jsp("zone/selznoe.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selcity(HkRequest req, HkResponse resp) {
        int cityId = req.getInt("cityId");
        if (cityId > 0 && ZoneUtil.getCity(cityId) != null) {
            HkWebUtil.setPcityId(req, resp, cityId);
            User loginUser = this.getLoginUser(req);
            if (loginUser != null) {
                loginUser.setPcityId(cityId);
                this.userService.updateUserPcityId(loginUser.getUserId(), cityId);
            }
            String return_url = req.getReturnUrl();
            if (!DataUtil.isEmpty(return_url)) {
                return "r:" + return_url;
            }
            return "r:/index_h.do";
        }
        return null;
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String changepcity(HkRequest req, HkResponse resp) {
        String zoneName = req.getString("zoneName");
        if (zoneName != null) {
            zoneName = DataUtil.filterZoneName(zoneName);
            City city = this.zoneService.getCityLike(zoneName);
            if (city != null) {
                HkWebUtil.setPcityId(req, resp, city.getCityId());
                User loginUser = this.getLoginUser(req);
                if (loginUser != null) {
                    loginUser.setPcityId(city.getCityId());
                    this.userService.updateUserPcityId(loginUser.getUserId(), city.getCityId());
                }
                return this.onSuccess2(req, "onlocationok", null);
            }
            Province province = this.zoneService.getProvinceLike(zoneName);
            if (province != null) {
                return this.onError(req, Err.ZONE_CITY_NOTFOUND, "onlocationerror", province.getProvinceId());
            }
            Country country = this.zoneService.getCountryLike(zoneName);
            if (country != null) {
                return this.onError(req, Err.ZONE_NOT_EXIST, "onlocationerror", country.getCountryId());
            }
        }
        return this.onError(req, Err.ZONE_NOT_EXIST, "onlocationerror", 0);
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String changecity2(HkRequest req, HkResponse resp) {
        String zoneName = req.getString("zoneName");
        int ch = req.getInt("ch");
        String return_url = req.getReturnUrl();
        int forsel = req.getIntAndSetAttr("forsel");
        if (ch == 0) {
            if (req.getInt("forcmp") == 1) {
                String name = req.getString("name");
                req.setEncodeAttribute("name", name);
                List<CmpZoneInfo> cmpzoneinfoList = companyService.getCmpZoneInfoList();
                req.setAttribute("cmpzoneinfoList", cmpzoneinfoList);
            }
            return this.getWapJsp("pub/changecity2.jsp");
        }
        if (zoneName != null) {
            zoneName = DataUtil.filterZoneName(zoneName);
            City city = this.zoneService.getCityLike(zoneName);
            if (city != null) {
                if (forsel != 1) {
                    HkWebUtil.setPcityId(req, resp, city.getCityId());
                    User loginUser = this.getLoginUser(req);
                    if (loginUser != null) {
                        loginUser.setPcityId(city.getCityId());
                        this.userService.updateUserPcityId(loginUser.getUserId(), city.getCityId());
                    }
                    req.setSessionText("view2.changecity.success");
                } else {
                    req.setSessionValue("zoneName", city.getCity());
                }
                if (!DataUtil.isEmpty(return_url)) {
                    return "r:" + return_url;
                }
                return "r:/index_h.do";
            }
            Province province = this.zoneService.getProvinceLike(zoneName);
            if (province != null) {
                return "r:/index_selcityfromprovince.do?provinceId=" + province.getProvinceId() + "&forsel=" + forsel + "&return_url=" + DataUtil.urlEncoder(return_url);
            }
            Country country = this.zoneService.getCountryLike(zoneName);
            if (country != null) {
                return "r:/index_selprovince.do?countryId=" + country.getCountryId() + "&forsel=" + forsel + "&return_url=" + DataUtil.urlEncoder(return_url);
            }
        }
        return "r:/index_selcountry.do?forsel=" + forsel + "&return_url=" + DataUtil.urlEncoder(return_url);
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selcityfromprovince(HkRequest req, HkResponse resp) {
        int provinceId = req.getIntAndSetAttr("provinceId");
        List<City> list = this.zoneService.getCityList(provinceId);
        req.setAttribute("list", list);
        req.reSetAttribute("forsel");
        return this.getWapJsp("pub/selcityfromprovince.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selprovince(HkRequest req, HkResponse resp) {
        int countryId = req.getIntAndSetAttr("countryId");
        List<Province> list = this.zoneService.getProvinceList(countryId);
        req.setAttribute("list", list);
        req.reSetAttribute("forsel");
        return this.getWapJsp("pub/selprovince.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selcountry(HkRequest req, HkResponse resp) {
        List<Country> list = ZoneUtil.getCountryList();
        req.setAttribute("list", list);
        req.reSetAttribute("forsel");
        return this.getWapJsp("pub/selcountry.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String selcity2(HkRequest req, HkResponse resp) {
        int cityId = req.getInt("cityId");
        if (cityId > 0 && ZoneUtil.getCity(cityId) != null) {
            if (req.getInt("forsel") != 1) {
                HkWebUtil.setPcityId(req, resp, cityId);
                User loginUser = this.getLoginUser(req);
                if (loginUser != null) {
                    loginUser.setPcityId(cityId);
                    this.userService.updateUserPcityId(loginUser.getUserId(), cityId);
                }
            } else {
                req.setSessionValue("zoneName", ZoneUtil.getCity(cityId).getCity());
            }
            String return_url = req.getReturnUrl();
            if (!DataUtil.isEmpty(return_url)) {
                return "r:" + return_url;
            }
            return "r:/index_h.do";
        }
        return null;
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String route(HkRequest req, HkResponse resp) {
        int forcesel = req.getInt("forcesel");
        if (forcesel == 0) {
            boolean pcbrowser = ServletUtil.isPc(req);
            if (pcbrowser) {
                User loginUser = this.getLoginUser(req);
                if (loginUser == null) {
                    return "r:/login";
                }
                if (req.getServerName().indexOf("huoku.net") != -1) {
                    return this.index4_meifa(req, resp);
                }
                return "r:/feed";
            }
            boolean wapbrowser = ServletUtil.isWap(req);
            if (wapbrowser) {
                return this.h(req, resp);
            }
        }
        return this.getWeb4Jsp("index/selpage.jsp");
    }

    /**
	 * @param req
	 * @param resp
	 * @return
	 */
    public String savepos(HkRequest req, HkResponse resp) {
        long companyId = req.getLong("companyId");
        String op = req.getString("op");
        if (op == null) {
            return null;
        }
        Company company = this.companyService.getCompany(companyId);
        if (company == null) {
            return null;
        }
        long userId = Long.valueOf(DesUtil.decode("opuserid", op));
        if (company.getUserId() != userId) {
            CmpAdminUser cmpAdminUser = this.cmpAdminUserService.getCmpAdminUserByCompanyIdAndUserId(companyId, userId);
            if (cmpAdminUser == null) {
                return null;
            }
        }
        double marker_x = req.getDouble("marker_x");
        double marker_y = req.getDouble("marker_y");
        this.companyService.updateCompanyMap(companyId, marker_x, marker_y);
        return null;
    }
}
