package cn.nkjobsearch.others;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Backup {

    public static void getCityAndProvince() {
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader("E:\\Study\\Search\\code\\trunk\\owl\\nkjs.test.t.owl"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = null;
        try {
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rs = sb.toString();
        Pattern p = Pattern.compile("rdf:ID=\"C(.)+\"", Pattern.CASE_INSENSITIVE);
        Matcher matcher = p.matcher(rs);
        while (matcher.find()) {
            s = rs.substring(matcher.start() + 8, matcher.end() - 1);
            System.out.println(s);
        }
        p = Pattern.compile("rdf:ID=\"P(.)+\"", Pattern.CASE_INSENSITIVE);
        matcher = p.matcher(rs);
        while (matcher.find()) {
            s = rs.substring(matcher.start() + 8, matcher.end() - 1);
            System.out.println(s);
        }
    }

    public static void replaceCityAndProvinceForOWL() {
        String[] city = { "C其他", "C北京", "C上海", "C天津", "C广州", "C深圳", "C南京", "C苏州", "C杭州", "C重庆", "C合肥", "C福州", "C兰州", "C南宁", "C贵阳", "C海口", "C石家庄", "C郑州", "C哈尔滨", "C武汉", "C长沙", "C长春", "C南昌", "C沈阳", "C呼和浩特", "C银川", "C西宁", "C济南", "C太原", "C西安", "C成都", "C拉萨", "C乌鲁木齐", "C昆明", "C芜湖", "C安庆", "C马鞍山", "C巢湖", "C滁州", "C黄山", "C淮南", "C蚌埠", "C阜阳", "C六安", "C泉州", "C厦门", "C漳州", "C莆田", "C河源", "C汕头", "C汕尾", "C湛江", "C中山", "C东莞", "C江门", "C潮州", "C佛山", "C珠海", "C惠州", "C北海", "C桂林", "C柳州", "C遵义", "C三亚", "C保定", "C廊坊", "C秦皇岛", "C唐山", "C邯郸", "C邢台", "C开封", "C洛阳", "C大庆", "C佳木斯", "C牡丹江", "C齐齐哈尔", "C十堰", "C襄樊", "C宜昌", "C荆门", "C荆州", "C黄石", "C湘潭", "C株洲", "C常德", "C衡阳", "C益阳", "C郴州", "C岳阳", "C吉林", "C辽源", "C通化", "C常州", "C昆山", "C连云港", "C南通", "C张家港", "C无锡", "C徐州", "C扬州", "C镇江", "C盐城", "C九江", "C上饶", "C赣州", "C鞍山", "C大连", "C葫芦岛", "C营口", "C锦州", "C本溪", "C抚顺", "C丹东", "C铁岭", "C包头", "C赤峰", "C德州", "C东营", "C济宁", "C临沂", "C青岛", "C日照", "C泰安", "C威海", "C潍坊", "C烟台", "C淄博", "C菏泽", "C枣庄", "C聊城", "C莱芜", "C临汾", "C运城", "C宝鸡", "C咸阳", "C乐山", "C泸州", "C绵阳", "C内江", "C宜宾", "C自贡", "C日喀则", "C喀什", "C克拉玛依", "C伊犁", "C吐鲁番", "C大理", "C丽江", "C玉溪", "C曲靖", "C金华", "C丽水", "C宁波", "C嘉兴", "C舟山", "C温州", "C台州", "C衢州", "C绍兴", "C湖州", "C香港", "C澳门", "C台湾" };
        String[] province = { "P四川", "P广东", "P山西", "P河北", "P北京", "P天津", "P云南", "P辽宁", "P浙江", "P江苏", "P安徽", "P山东", "P上海", "P新疆", "P吉林", "P甘肃", "P陕西", "P宁夏", "P青海", "P湖南", "P重庆", "P黑龙江", "P河南", "P西藏", "P福建", "P澳门", "P湖北", "P江西", "P台湾", "P广西", "P内蒙", "P海南", "P贵州", "P香港" };
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader("E:\\Study\\Search\\code\\trunk\\owl\\utf8.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = null;
        try {
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rs = sb.toString();
        for (int i = 0; i < city.length; i++) {
            rs = rs.replaceAll(city[i], ("C" + i));
        }
        for (int i = 0; i < province.length; i++) {
            rs = rs.replaceAll(province[i], ("P" + (i + 1)));
        }
        System.out.println(rs);
    }

    public static void replaceForOWL() {
        String[] as = { "_.NET", "_3D", "_3G", "_3dmax", "ACCESS", "AJAX", "ASP", "ASP.NET", "ATL", "AUTOCAD", "ActiveX", "Android", "Apache", "Applet", "B2B", "B2C", "C", "C-", "C--", "CGI", "CSS", "CorelDraw", "DB", "DB2", "DBA", "DOM", "DOS", "Database", "Delphi", "Direct3D", "DirectX", "Dreamweaver", "ERP", "Eclipse", "Engineer", "FLEX", "Fireworks", "Flash", "GSM", "GTK", "GUI", "HTML", "Hibernate", "IIS", "Infomix", "J2EE", "J2ME", "J2SE", "JBoss", "JMS", "JS", "JSF", "JSP", "JSTL", "JUnit", "Java", "JavaEE", "Javascript", "Java技术", "Joomla", "LAMP", "Linux", "MAC", "MAYA", "MFC", "MSSQL", "MVC", "Manager", "MySQL", "OOA", "OOAD", "OOD", "OOP", "OpenGL", "Oracle", "P2P", "PC操作系统", "PHP", "PL", "PM", "Pascal", "Perl", "Photoshop", "Python", "QA", "QT", "RIA", "Redhat", "Ruby", "SEO", "SOAP", "SQA", "SQL", "SQL2000", "SQL2005", "Server", "Solaris", "Spring", "SqlServer", "Struts", "Support", "Swing", "Sybase", "Symbian", "TCP", "Test", "Tomcat", "Unix", "VB", "VBScript", "VS", "Visual_Studio", "WAP", "WEB", "WEB开发技术", "WML", "Weblogic", "Win", "WinCE", "Windows", "XHTML", "XML", "Zend", "arm", "driver", "hardware", "iPhone", "jQuery", "javaBean", "software", "编程技术", "编程框架", "编程语言", "操作系统", "测试", "程序员", "调试", "服务器", "服务器软件", "工程师", "互联网", "汇编", "架构", "交换机", "经理", "开发工具", "路由器", "美工", "面向对象", "嵌入式操作系统", "驱动", "软件", "软件工程", "设计", "设计师", "数据库", "图形编程技术", "图形软件", "网络", "网页", "网页设计", "网站", "维护", "文档", "协议", "硬件", "游戏", "职位" };
        BufferedReader bufferedReader = null;
        StringBuilder sb = new StringBuilder();
        try {
            bufferedReader = new BufferedReader(new FileReader("E:\\Study\\Search\\code\\trunk\\owl\\keyword.utf8.owl"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        String s = null;
        try {
            while ((s = bufferedReader.readLine()) != null) {
                sb.append(s);
                sb.append("\n");
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String rs = sb.toString();
        for (int i = 0; i < as.length; i++) {
            rs = rs.replaceAll("#" + as[i] + "\"", ("#K" + (i + 1) + "\""));
        }
        for (int j = 0; j < as.length; j++) {
            rs = rs.replaceAll("\"" + as[j] + "\"", ("\"K" + (j + 1) + "\""));
        }
        System.out.println(rs);
    }
}
