public class Test {    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {
        PageContext pageContext = null;
        HttpSession session = null;
        ServletContext application = null;
        ServletConfig config = null;
        JspWriter out = null;
        Object page = this;
        JspWriter _jspx_out = null;
        PageContext _jspx_page_context = null;
        try {
            response.setContentType("text/html; charset=utf-8");
            pageContext = _jspxFactory.getPageContext(this, request, response, "", true, 8192, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            session = pageContext.getSession();
            out = pageContext.getOut();
            _jspx_out = out;
            out.write('\r');
            out.write('\n');
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            response.setHeader("Pragma", "No-cache");
            response.setHeader("Cache-Control", "no-cache");
            response.setDateHeader("Expires", 0);
            String path = request.getContextPath();
            String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
            pageContext.setAttribute("basePath", basePath);
            out.write("\r\n");
            out.write("<html>\r\n");
            out.write("<head>\r\n");
            out.write("<base href=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${pageScope.basePath }", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\">\r\n");
            out.write("<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n");
            out.write("<title></title>\r\n");
            out.write("<link type=\"text/css\" rel=\"stylesheet\" href=\"system/css/style.css\" />\r\n");
            out.write("<script type=\"text/javascript\" src=\"system/js/public.js\"></script>\r\n");
            out.write("<link href=\"system/plugins/validateMyForm/css/plugin.css\" rel=\"stylesheet\" type=\"text/css\">\r\n");
            out.write(" <script type=\"text/javascript\" src=\"system/js/jquery-1.4.2.js\"></script>\r\n");
            out.write(" <script type=\"text/javascript\" src=\"system/js/verify1.js\"></script>\r\n");
            out.write(" <script type=\"text/javascript\" src=\"system/plugins/validateMyForm/js/jquery.validateMyForm.1.5.js\"></script>\r\n");
            out.write(" <script type=\"text/javascript\">  \r\n");
            out.write("\t$(document).ready(function(){ \r\n");
            out.write("\t    $(\"#form1\").validateMyForm(); \t\t\r\n");
            out.write("\t}); \r\n");
            out.write("\t\r\n");
            out.write("\tfunction toDate(str){\r\n");
            out.write("    \tvar sd=str.split(\"-\");\r\n");
            out.write("    \treturn new Date(sd[0],sd[1],sd[2]);\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction verMovDateValuble(){\r\n");
            out.write("\t\tvar qcRqnyrDate=toDate($(\"#qcRq\").val());\r\n");
            out.write("\t\tvar test0=$(\"#sbqsny\").val().substr(0,4)+'-'+$(\"#sbqsny\").val().substr(4,6);\r\n");
            out.write("\t\tvar test1=$(\"#qcRq\").val().substr(7,10);\r\n");
            out.write("\t\tvar test2=test0+test1;\r\n");
            out.write("\t\t//var sbqsnyr=DateTrans($(\"#sbqsny\").val())+$(\"#qcRq\").val().substr(7,10);\r\n");
            out.write("\t\tvar sbqsnyrDate=toDate(test2);\r\n");
            out.write("\t\tif(qcRqnyrDate>sbqsnyrDate){\r\n");
            out.write("\t\t\t$(\"#save\").attr(\"disabled\",true);\r\n");
            out.write("\t\t\talert(\"时间超出范围,请先完税!\");\r\n");
            out.write("\t\t\treturn ;\r\n");
            out.write("\t\t}\r\n");
            out.write("\t\t$(\"#save\").attr(\"disabled\",false);\r\n");
            out.write("\t\treturn ;\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("</script>  \r\n");
            out.write("<script type=\"text/javascript\" src=\"system/js/DatePicker/WdatePicker.js\"></script>\r\n");
            out.write("<body>\r\n");
            out.write("<div class=\"main\">\r\n");
            out.write("\t<div class=\"position\">当前位置: <a href=\"sysadm/desktop.jsp\">桌 面</a> → 车船迁出</div>\r\n");
            out.write("\t<div class=\"mainbody\">\r\n");
            out.write("\t\t<div class=\"operate_info\">操作说明：带 * 号必填</div>\r\n");
            out.write("\t\t\r\n");
            out.write("\t\t<div style=\"font-size:20px;font-weight:bold;color: blue\">车船迁出：</div>\r\n");
            out.write("\t\t<div class=\"table\">\r\n");
            out.write("\t\t");
            if (_jspx_meth_mytag_005fView_005f0(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t\t<form id=\"form1\" action=\"system/book/moveout_save.do\" method=\"post\">\r\n");
            out.write("\t\t\t\t<input type=\"hidden\"\" name=\"czlbDm\"  value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDmCcsCzlb.czlbDm}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/>\r\n");
            out.write("\t\t\t\t<table width=\"100%\" border=\"0\" cellpadding=\"1\" cellspacing=\"1\" class=\"table_form\" >\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">车主类别：</td>\r\n");
            out.write("\t\t\t\t\t\t");
            if (_jspx_meth_mytag_005fView_005f1(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t\t\t\t\t<td>\r\n");
            out.write("\t\t\t\t\t\t\t<input readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDmCcsCzlb.czlbMc}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/><!-- 需要显示名称,车主类别代码对应的名称 -->\r\n");
            out.write("\t\t\t\t\t\t</td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">登记状态：</td>\r\n");
            out.write("\t\t\t\t\t\t");
            if (_jspx_meth_mytag_005fView_005f2(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t\t\t\t\t<input type=\"hidden\" id=\"djztDm\" name=\"djztDm\" value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDmCcsDjzt.djztDm}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"djztMc\" id=\"djztMc\" readonly=\"readonly\" value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDmCcsDjzt.djztMc }", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">纳税人编码：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"nsrbm\" id=\"nsrbm\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.nsrbm}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">车船登记号：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"ccdjh\" id=\"ccdjh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.ccdjh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">车主名称：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"czMc\" id=\"czMc\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.czMc}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">身份证号码：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"sfzhm\" id=\"sfzhm\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.sfzhm}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">地址：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"czDz\" id=\"czDz\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.czDz}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">电话：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"czDh\" id=\"czDh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.czDh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">车船牌照号码：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"ccpzh\" id=\"ccpzh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.ccpzh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\" /></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">车船类型：</td>\r\n");
            out.write("\t\t\t\t\t\t<input type=\"hidden\" name=\"jjcclxDm\" id=\"jjcclxDm\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.jjcclxDm}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/>\r\n");
            out.write("\t\t\t\t\t\t");
            if (_jspx_meth_mytag_005fView_005f3(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t\t\t\t\t<td><input  readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDmCcsJjcclx.jjcclxMc}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">发动机号：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"fdjh\" id=\"fdjh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.fdjh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">车架号：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"cjh\" id=\"cjh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.cjh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">厂牌型号：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"cpxh\" id=\"cpxh\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.cpxh}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">购置时间：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"gzRq\" id=\"gzRq\" readonly value=\"");
            if (_jspx_meth_fmt_005fformatDate_005f0(_jspx_page_context)) return;
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">核定载重量(吨)：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"hdzzl\" id=\"hdzzl\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.hdzzl}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">核定载客量(人)：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"hdzkl\" id=\"hdzkl\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.hdzkl}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">整备质量(吨)：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"zbzl\" id=\"zbzl\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.zbzl}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">排气量：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"pql\" id=\"pql\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.pql}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">登记日期：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"djRq\" id=\"djRq\" readonly value=\"");
            if (_jspx_meth_fmt_005fformatDate_005f1(_jspx_page_context)) return;
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">行驶证发证日期：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"fzRq\" id=\"fzRq\" readonly value=\"");
            if (_jspx_meth_fmt_005fformatDate_005f2(_jspx_page_context)) return;
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">免税：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"msBj\" id=\"msBj\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.msBj}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t\t<td  align=\"right\">申报起始年月：</td>\r\n");
            out.write("\t\t\t\t\t\t<td><input name=\"sbqsny\" id=\"sbqsny\" readonly value=\"");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.sbqsny}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("\"/></td>\r\n");
            out.write("\t\t\t\t\t</tr>\t\t\t\t\t\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\">\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">迁出日期：</td>\r\n");
            out.write("\t\t\t\t\t\t<td colspan=\"3\"><input name=\"qcRq\" id=\"qcRq\" onFocus=\"new WdatePicker(this,'%Y-%M-%D',true,'default')\" onblur=\"verMovDateValuble()\" class=\"required date\" />&nbsp;<span style=\"color:red\">*</span></td>\r\n");
            out.write("\t\t\t\t\t\t\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t\r\n");
            out.write("\t\t\t\t\t<tr onMouseOver=\"mouseOver(this)\" onMouseOut=\"mouseOut(this)\" >\r\n");
            out.write("\t\t\t\t\t\t<td align=\"right\">备注：</td>\r\n");
            out.write("\t\t\t\t\t\t<td colspan=\"3\">\r\n");
            out.write("\t\t\t\t\t\t\t<textarea name=\"bz\" id=\"bz\" cols=\"50\" rows=\"3\" >");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.proprietaryEvaluate("${tDjCcdjxx.bz}", java.lang.String.class, (PageContext) _jspx_page_context, null, false));
            out.write("</textarea>\r\n");
            out.write("\t\t\t\t\t\t</td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t\t<tr>\r\n");
            out.write("\t\t\t\t\t\t<td colspan=\"4\" class=\"form_button\" style=\"padding-top:10px;\">\r\n");
            out.write("\t\t\t\t\t\t\t");
            if (_jspx_meth_c_005fif_005f0(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t\t\t\t\t\t<input type=\"button\"\" value=\"返回\" onclick=\"javascript:location.href='system/search/unisearch.jsp?module=book/moveoutform.jsp'\"/>\r\n");
            out.write("\t\t\t\t\t\t</td>\r\n");
            out.write("\t\t\t\t\t</tr>\r\n");
            out.write("\t\t\t\t</table>\r\n");
            out.write("\t\t\t</form>\r\n");
            out.write("\t\t</div>\r\n");
            out.write("\t</div>\r\n");
            out.write("</div>\r\n");
            out.write("</body>\r\n");
            out.write("</html>\r\n");
        } catch (Throwable t) {
            if (!(t instanceof SkipPageException)) {
                out = _jspx_out;
                if (out != null && out.getBufferSize() != 0) try {
                    out.clearBuffer();
                } catch (java.io.IOException e) {
                }
                if (_jspx_page_context != null) _jspx_page_context.handlePageException(t);
            }
        } finally {
            _jspxFactory.releasePageContext(_jspx_page_context);
        }
    }
}