public class Test {    public void _jspService(HttpServletRequest request, HttpServletResponse response) throws java.io.IOException, ServletException {
        PageContext pageContext = null;
        ServletContext application = null;
        ServletConfig config = null;
        JspWriter out = null;
        Object page = this;
        JspWriter _jspx_out = null;
        PageContext _jspx_page_context = null;
        try {
            response.setContentType("text/html; charset=utf-8");
            pageContext = _jspxFactory.getPageContext(this, request, response, null, false, 0, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            out = pageContext.getOut();
            _jspx_out = out;
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write("\n");
            out.write('\n');
            org.eclipse.birt.report.presentation.aggregation.IFragment fragment = null;
            synchronized (request) {
                fragment = (org.eclipse.birt.report.presentation.aggregation.IFragment) _jspx_page_context.getAttribute("fragment", PageContext.REQUEST_SCOPE);
                if (fragment == null) {
                    throw new java.lang.InstantiationException("bean fragment not found within scope");
                }
            }
            out.write('\n');
            org.eclipse.birt.report.context.BaseAttributeBean attributeBean = null;
            synchronized (request) {
                attributeBean = (org.eclipse.birt.report.context.BaseAttributeBean) _jspx_page_context.getAttribute("attributeBean", PageContext.REQUEST_SCOPE);
                if (attributeBean == null) {
                    throw new java.lang.InstantiationException("bean attributeBean not found within scope");
                }
            }
            out.write('\n');
            out.write('\n');
            String baseHref = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
            if (!attributeBean.isDesigner()) {
                String baseURL = ParameterAccessor.getBaseURL();
                if (baseURL != null) baseHref = baseURL;
            }
            baseHref += request.getContextPath() + fragment.getJSPRootPath();
            out.write('\n');
            out.write('\n');
            out.write("\n");
            out.write("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/REC-html40/strict.dtd\">\n");
            out.write("<HTML>\n");
            out.write("\t<HEAD>\n");
            out.write("\t\t<TITLE>");
            out.print(attributeBean.getReportTitle());
            out.write("</TITLE>\n");
            out.write("\t\t<BASE href=\"");
            out.print(baseHref);
            out.write("\" >\n");
            out.write("\t\t\n");
            out.write("\t\t<!-- Mimics Internet Explorer 7, it just works on IE8. -->\n");
            out.write("\t\t<META HTTP-EQUIV=\"X-UA-Compatible\" CONTENT=\"IE=EmulateIE7\">\n");
            out.write("\t\t<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; CHARSET=utf-8\">\n");
            out.write("\t\t<LINK REL=\"stylesheet\" HREF=\"birt/styles/style.css\" TYPE=\"text/css\">\n");
            out.write("\t\t");
            if (attributeBean.isRtl()) {
                out.write("\n");
                out.write("\t\t<LINK REL=\"stylesheet\" HREF=\"birt/styles/dialogbase_rtl.css\" MEDIA=\"screen\" TYPE=\"text/css\"/>\n");
                out.write("\t\t");
            } else {
                out.write("\n");
                out.write("\t\t<LINK REL=\"stylesheet\" HREF=\"birt/styles/dialogbase.css\" MEDIA=\"screen\" TYPE=\"text/css\"/>\t\n");
                out.write("\t\t");
            }
            out.write("\n");
            out.write("\t\t<script type=\"text/javascript\">\t\t\t\n");
            out.write("\t\t\t");
            if (request.getAttribute("SoapURL") != null) {
                out.write("\n");
                out.write("\t\t\tvar soapURL = \"");
                out.print((String) request.getAttribute("SoapURL"));
                out.write("\";\n");
                out.write("\t\t\t");
            } else {
                out.write("\n");
                out.write("\t\t\tvar soapURL = document.location.href;\n");
                out.write("\t\t\t");
            }
            out.write("\n");
            out.write("\t\t\tvar rtl = ");
            out.print(attributeBean.isRtl());
            out.write(";\n");
            out.write("\t\t</script>\n");
            out.write("\t\t\n");
            out.write("\t\t<script src=\"birt/ajax/utility/Debug.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/lib/prototype.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<!-- Mask -->\n");
            out.write("\t\t<script src=\"birt/ajax/core/Mask.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/utility/BrowserUtility.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<!-- Drag and Drop -->\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtDndManager.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<script src=\"birt/ajax/utility/Constants.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/utility/BirtUtility.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtEventDispatcher.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtEvent.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<script src=\"birt/ajax/mh/BirtBaseResponseHandler.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/mh/BirtGetUpdatedObjectsResponseHandler.js\" type=\"text/javascript\"></script>\n");
            out.write("\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/AbstractUIComponent.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/AbstractBaseToolbar.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/BirtToolbar.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/BirtNavigationBar.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/AbstractBaseToc.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/BirtToc.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/app/BirtProgressBar.js\" type=\"text/javascript\"></script>\n");
            out.write("\n");
            out.write(" \t\t<script src=\"birt/ajax/ui/report/AbstractReportComponent.js\" type=\"text/javascript\"></script>\n");
            out.write(" \t\t<script src=\"birt/ajax/ui/report/AbstractBaseReportDocument.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/report/BirtReportDocument.js\" type=\"text/javascript\"></script>\n");
            out.write("\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/AbstractBaseDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtTabedDialogBase.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/AbstractParameterDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtParameterDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtSimpleExportDataDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtExportReportDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtPrintReportDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtPrintReportServerDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/AbstractExceptionDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtExceptionDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/ui/dialog/BirtConfirmationDialog.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t\t<script src=\"birt/ajax/utility/BirtPosition.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/utility/Printer.js\" type=\"text/javascript\"></script>\n");
            out.write("\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtCommunicationManager.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtSoapRequest.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t<script src=\"birt/ajax/core/BirtSoapResponse.js\" type=\"text/javascript\"></script>\n");
            out.write("\t\t\n");
            out.write("\t</HEAD>\n");
            out.write("\t\n");
            out.write("\t<BODY \n");
            out.write("\t\tCLASS=\"BirtViewer_Body\"  \n");
            out.write("\t\tONLOAD=\"javascript:init( );\" \n");
            out.write("\t\tSCROLL=\"no\" \n");
            out.write("\t\tLEFTMARGIN='0px' \n");
            out.write("\t\tSTYLE='overflow:hidden; direction: ");
            out.print(attributeBean.isRtl() ? "rtl" : "ltr");
            out.write("'\n");
            out.write("\t\t>\n");
            out.write("\t\t<!-- Header section -->\n");
            out.write("\t\t<TABLE ID='layout' CELLSPACING='0' CELLPADDING='0' STYLE='width:100%;height:100%'>\n");
            out.write("\t\t\t");
            if (attributeBean.isShowTitle()) {
                out.write("\n");
                out.write("\t\t\t<TR CLASS='body_caption_top'>\n");
                out.write("\t\t\t\t<TD COLSPAN='2'></TD>\n");
                out.write("\t\t\t</TR>\n");
                out.write("\t\t\t<TR CLASS='body_caption' VALIGN='bottom'>\n");
                out.write("\t\t\t\t<TD COLSPAN='2'>\n");
                out.write("\t\t\t\t\t<TABLE BORDER=0 CELLSPACING=\"0\" CELLPADDING=\"1px\" WIDTH=\"100%\">\n");
                out.write("\t\t\t\t\t\t<TR>\n");
                out.write("\t\t\t\t\t\t\t<TD WIDTH=\"3px\"/>\n");
                out.write("\t\t\t\t\t\t\t<TD>\n");
                out.write("\t\t\t\t\t\t\t\t<B>");
                out.print(attributeBean.getReportTitle());
                out.write("\n");
                out.write("\t\t\t\t\t\t\t\t</B>\n");
                out.write("\t\t\t\t\t\t\t</TD>\n");
                out.write("\t\t\t\t\t\t\t<TD ALIGN='right'>\n");
                out.write("\t\t\t\t\t\t\t</TD>\n");
                out.write("\t\t\t\t\t\t\t<TD WIDTH=\"3px\"/>\n");
                out.write("\t\t\t\t\t\t</TR>\n");
                out.write("\t\t\t\t\t</TABLE>\n");
                out.write("\t\t\t\t</TD>\n");
                out.write("\t\t\t</TR>\n");
                out.write("\t\t\t");
            }
            out.write("\n");
            out.write("\t\t\t\n");
            out.write("\t\t\t");
            if (fragment != null) {
                fragment.callBack(request, response);
            }
            out.write("\n");
            out.write("\t\t</TABLE>\n");
            out.write("\t</BODY>\n");
            out.write("\n");
            out.write("\t");
            out.write('\n');
            out.write('\n');
            out.write("\n");
            out.write("<script type=\"text/javascript\">\n");
            out.write("// <![CDATA[\t\n");
            out.write("\t// Error msgs\n");
            out.write("\tConstants.error.invalidPageRange = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.dialog.page.error.invalidpagerange"));
            out.write("';\n");
            out.write("\tConstants.error.parameterRequired = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.error.parameterrequired"));
            out.write("';\n");
            out.write("\tConstants.error.parameterNotAllowBlank = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.error.parameternotallowblank"));
            out.write("';\n");
            out.write("\tConstants.error.parameterNotSelected = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.error.parameternotselected"));
            out.write("';\n");
            out.write("\tConstants.error.invalidPageNumber = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.navbar.error.blankpagenum"));
            out.write("';\n");
            out.write("\tConstants.error.unknownError = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.error.unknownerror"));
            out.write("';\n");
            out.write("\tConstants.error.generateReportFirst = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.error.generatereportfirst"));
            out.write("';\n");
            out.write("\tConstants.error.printPreviewAlreadyOpen = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.dialog.print.printpreviewalreadyopen"));
            out.write("';\n");
            out.write("\tConstants.error.confirmCancelTask = '");
            out.print(BirtResources.getJavaScriptMessage("birt.viewer.progressbar.confirmcanceltask"));
            out.write("';\n");
            out.write("// ]]>\n");
            out.write("</script>\n");
            out.write('	');
            out.write('\n');
            out.write('	');
            out.write('\n');
            out.write('\n');
            out.write("\n");
            out.write("<script type=\"text/javascript\">\n");
            out.write("// <![CDATA[\n");
            out.write("            \n");
            out.write("    Constants.nullValue = '");
            out.print(IBirtConstants.NULL_VALUE);
            out.write("';\n");
            out.write("    \n");
            out.write("\t// Request attributes\n");
            out.write("\tif ( !Constants.request )\n");
            out.write("\t{\n");
            out.write("\t\tConstants.request = {};\n");
            out.write("\t}\n");
            out.write("\tConstants.request.format = '");
            out.print(ParameterAccessor.getFormat(request));
            out.write("';\n");
            out.write("\tConstants.request.rtl = ");
            out.print(ParameterAccessor.isRtl(request));
            out.write(";\n");
            out.write("\tConstants.request.isDesigner = ");
            out.print(ParameterAccessor.isDesigner());
            out.write(";\n");
            out.write("\tConstants.request.servletPath = \"");
            out.print(request.getAttribute("ServletPath"));
            out.write("\".substr(1);\n");
            out.write("\t");
            IViewingSession viewingSession = ViewingSessionUtil.getSession(request);
            String subSessionId = null;
            if (viewingSession != null) {
                subSessionId = viewingSession.getId();
            }
            out.write("\n");
            out.write("\tConstants.viewingSessionId = ");
            out.print(subSessionId != null ? "\"" + subSessionId + "\"" : "null");
            out.write(";\t\n");
            out.write("// ]]>\n");
            out.write("</script>\n");
            out.write("\t\n");
            out.write("\n");
            out.write("\t<script type=\"text/javascript\">\n");
            out.write("\t// <![CDATA[\n");
            out.write("\t\tvar hasSVGSupport = false;\n");
            out.write("\t\tvar useVBMethod = false;\n");
            out.write("\t\tif ( navigator.mimeTypes != null && navigator.mimeTypes.length > 0 )\n");
            out.write("\t\t{\n");
            out.write("\t\t    if ( navigator.mimeTypes[\"image/svg+xml\"] != null )\n");
            out.write("\t\t    {\n");
            out.write("\t\t        hasSVGSupport = true;\n");
            out.write("\t\t    }\n");
            out.write("\t\t}\n");
            out.write("\t\telse\n");
            out.write("\t\t{\n");
            out.write("\t\t    useVBMethod = true;\n");
            out.write("\t\t}\n");
            out.write("\t\t\n");
            out.write("\t// ]]>\n");
            out.write("\t</script>\n");
            out.write("\t\n");
            out.write("\t<script type=\"text/vbscript\">\n");
            out.write("\t\tOn Error Resume Next\n");
            out.write("\t\tIf useVBMethod = true Then\n");
            out.write("\t\t    hasSVGSupport = IsObject(CreateObject(\"Adobe.SVGCtl\"))\n");
            out.write("\t\tEnd If\n");
            out.write("\t</script>\n");
            out.write("\n");
            out.write("\t<script type=\"text/javascript\">\n");
            out.write("\t\tvar Mask =  new Mask(false); //create mask using \"div\"\n");
            out.write("\t\tvar BrowserUtility = new BrowserUtility();\n");
            out.write("\t\tDragDrop = new BirtDndManager();\n");
            out.write("\n");
            out.write("\t\tvar birtToolbar = new BirtToolbar( 'toolbar' );\n");
            out.write("\t\tvar navigationBar = new BirtNavigationBar( 'navigationBar' );\n");
            out.write("\t\tvar birtToc = new BirtToc( 'display0' );\n");
            out.write("\t\tvar birtProgressBar = new BirtProgressBar( 'progressBar' );\n");
            out.write("\t\tvar birtReportDocument = new BirtReportDocument( \"Document\", birtToc );\n");
            out.write("\n");
            out.write("\t\tvar birtParameterDialog = new BirtParameterDialog( 'parameterDialog', 'frameset' );\n");
            out.write("\t\tvar birtSimpleExportDataDialog = new BirtSimpleExportDataDialog( 'simpleExportDataDialog' );\n");
            out.write("\t\tvar birtExportReportDialog = new BirtExportReportDialog( 'exportReportDialog' );\n");
            out.write("\t\tvar birtPrintReportDialog = new BirtPrintReportDialog( 'printReportDialog' );\n");
            out.write("\t\tvar birtPrintReportServerDialog = new BirtPrintReportServerDialog( 'printReportServerDialog' );\n");
            out.write("\t\tvar birtExceptionDialog = new BirtExceptionDialog( 'exceptionDialog' );\n");
            out.write("\t\tvar birtConfirmationDialog = new BirtConfirmationDialog( 'confirmationDialog' );\n");
            out.write("\n");
            out.write("\t\t// register the base elements to the mask, so their input\n");
            out.write("\t\t// will be disabled when a dialog is popped up.\n");
            out.write("\t\tMask.setBaseElements( new Array( birtToolbar.__instance, navigationBar.__instance, birtReportDocument.__instance) );\n");
            out.write("\t\t\n");
            out.write("\t\tfunction init()\n");
            out.write("\t\t{\n");
            out.write("\t\t\tsoapURL = birtUtility.initSessionId( soapURL );\n");
            out.write("\t\t\t\n");
            out.write("\t\t");
            if (attributeBean.isShowParameterPage()) {
                out.write("\n");
                out.write("\t\t\tbirtParameterDialog.__cb_bind( );\n");
                out.write("\t\t");
            } else {
                out.write("\n");
                out.write("\t\t\tsoapURL = birtUtility.initDPI( soapURL );\n");
                out.write("\t\t\tnavigationBar.__init_page( );\n");
                out.write("\t\t");
            }
            out.write("\n");
            out.write("\t\t}\n");
            out.write("\t\t\n");
            out.write("\t\t// When link to internal bookmark, use javascript to fire an Ajax request\n");
            out.write("\t\tfunction catchBookmark( bookmark )\n");
            out.write("\t\t{\t\n");
            out.write("\t\t\tbirtEventDispatcher.broadcastEvent( birtEvent.__E_GETPAGE, { name : \"__bookmark\", value : bookmark } );\t\t\n");
            out.write("\t\t}\n");
            out.write("\t\t\n");
            out.write("\t</script>\n");
            out.write("</HTML>\n");
            out.write("\n");
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