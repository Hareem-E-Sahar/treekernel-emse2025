package com.unytech.watersoil.action.security;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import org.springframework.stereotype.Controller;
import com.unytech.watersoil.action.BaseAction;
import com.unytech.watersoil.common.GlobalEnum;
import com.unytech.watersoil.common.Globals;
import com.unytech.watersoil.entity.security.Permission;
import com.unytech.watersoil.entity.security.User;
import com.unytech.watersoil.service.security.UserService;
import com.unytech.watersoil.service.security.impl.UserServiceImpl;
import com.unytech.watersoil.utils.PermissionUtil;

@Controller("/security")
public class LoginAction extends BaseAction {

    private String username;

    private String password;

    private String verifycode;

    UserService userService = new UserServiceImpl();

    public String userLogin() {
        String verifycodeinsession = (String) getSession().get(GlobalEnum.LOGINVERIFYCODE.toString());
        if (verifycodeinsession == null || verifycodeinsession.trim().equals("") || !verifycodeinsession.equals(this.verifycode)) {
            getSession().remove(GlobalEnum.LOGINVERIFYCODE.toString());
            setMessage("验证码错误！");
            return "message";
        }
        if (this.username == null || this.username.trim().equals("") || this.password == null || this.password.trim().equals("")) {
            setMessage("用户名或密码不能为空！");
            return "message";
        }
        User user = userService.loginVerify(this.username, this.password);
        if (user == null) {
            setMessage("用户名或密码错误！");
            return "message";
        }
        if (user.getState() == 0) {
            setMessage("此账户已被冻结，请联系管理员！");
            return "message";
        }
        user.setLoginip(getRequest().getRemoteAddr());
        user.setLogintime(new Date());
        System.out.println("Path:" + getRequest().getRequestURL());
        System.out.println("Path:" + getRequest().getRequestURI());
        Set<User> onlineusers = (Set<User>) getApplication().get(GlobalEnum.ONLINEUSERS.toString());
        if (onlineusers == null) {
            getSession().put(GlobalEnum.LOGINUSER.toString(), user);
            Set<User> adduser = new HashSet<User>();
            User curuser = new User();
            curuser.setUid(user.getUid());
            curuser.setUsername(user.getUsername());
            curuser.setLoginip(user.getLoginip());
            curuser.setLogintime(user.getLogintime());
            adduser.add(curuser);
            getApplication().put(GlobalEnum.ONLINEUSERS.toString(), adduser);
        } else {
            for (Iterator it = onlineusers.iterator(); it.hasNext(); ) {
                User tempuser = (User) it.next();
                if (tempuser.getUsername().equals(user.getUsername())) {
                    if (!tempuser.getLoginip().equals(user.getLoginip())) {
                        setMessage("此用户已在其他地方登录，请联系管理员！");
                        return "message";
                    }
                } else {
                    onlineusers.add(user);
                    getApplication().put(GlobalEnum.ONLINEUSERS.toString(), onlineusers);
                    break;
                }
            }
        }
        List<Permission> permlist = null;
        if (this.username.toLowerCase().equals(Globals.KINGNAME.toLowerCase())) {
            PermissionUtil permutil = new PermissionUtil();
            permlist = permutil.getXMLPermission();
        } else {
            permlist = userService.getUserPermission(user);
        }
        getSession().put(GlobalEnum.SESSIONPERMISSION.toString(), permlist);
        List<Permission> firstmenulist = new ArrayList<Permission>();
        for (int i = 0; i < permlist.size(); i++) {
            if (permlist.get(i).getParentId().trim().equals(Globals.ROOTPERMISSIONID)) {
                firstmenulist.add(permlist.get(i));
            }
        }
        getRequest().setAttribute("firstmenulist", firstmenulist);
        return "loginok";
    }

    public String userLogout() {
        Set<String> onlineusers = (Set<String>) getApplication().get(GlobalEnum.ONLINEUSERS.toString());
        User user = (User) getSession().get(GlobalEnum.LOGINUSER.toString());
        if (onlineusers.contains(user)) {
            onlineusers.remove(user);
            getApplication().put(GlobalEnum.ONLINEUSERS.toString(), onlineusers);
        }
        getSession().remove(GlobalEnum.LOGINUSER.toString());
        getSession().remove(GlobalEnum.SESSIONPERMISSION.toString());
        getSession().remove(GlobalEnum.LOGINVERIFYCODE.toString());
        System.out.println("退出系统！");
        return "logoutok";
    }

    public String verifyCode() {
        try {
            int width = 40;
            int height = 18;
            int length = 4;
            Random random = new Random();
            String sRand = "";
            for (int i = 0; i < length; i++) {
                String rand = String.valueOf(random.nextInt(10));
                sRand += rand;
            }
            String code = sRand;
            System.out.println("验证码：" + code);
            getSession().put(GlobalEnum.LOGINVERIFYCODE.toString(), code);
            getResponse().setContentType("images/jpeg");
            getResponse().setHeader("Pragma", "No-cache");
            getResponse().setHeader("Cache-Control", "no-cache");
            getResponse().setDateHeader("Expires", 0);
            ServletOutputStream out = getResponse().getOutputStream();
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            Graphics g = image.getGraphics();
            g.setColor(getRandColor(200, 250));
            g.fillRect(0, 0, width, height);
            Font mFont = new Font("Times New Roman", Font.BOLD, 18);
            g.setFont(mFont);
            g.setColor(getRandColor(160, 200));
            for (int ii = 0; ii < 155; ii++) {
                int x2 = random.nextInt(width);
                int y2 = random.nextInt(height);
                int x3 = random.nextInt(12);
                int y3 = random.nextInt(12);
                g.drawLine(x2, y2, x2 + x3, y2 + y3);
            }
            g.setColor(new Color(20 + random.nextInt(110), 20 + random.nextInt(110), 20 + random.nextInt(110)));
            g.drawString(code, 2, 16);
            g.dispose();
            ImageIO.write((BufferedImage) image, "JPEG", out);
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Color getRandColor(int fc, int bc) {
        Random random = new Random();
        if (fc > 255) fc = 255;
        if (bc > 255) bc = 255;
        int r = fc + random.nextInt(bc - fc);
        int g = fc + random.nextInt(bc - fc);
        int b = fc + random.nextInt(bc - fc);
        return new Color(r, g, b);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setVerifycode(String verifycode) {
        this.verifycode = verifycode;
    }
}
