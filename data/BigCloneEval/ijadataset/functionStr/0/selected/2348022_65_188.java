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
            response.setContentType("text/html");
            pageContext = _jspxFactory.getPageContext(this, request, response, null, true, 8192, true);
            _jspx_page_context = pageContext;
            application = pageContext.getServletContext();
            config = pageContext.getServletConfig();
            session = pageContext.getSession();
            out = pageContext.getOut();
            _jspx_out = out;
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<script src=\"./js/jQuery.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/calendar.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/jquery.alphanumeric.pack.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/jquery.tablesorter.js\" type=\"text/javascript\"></script>\r\n");
            out.write("<script src=\"./js/linhaTabela.js\" type=\"text/javascript\"></script>\r\n");
            out.write("\r\n");
            out.write("<script>\r\n");
            out.write("\t\r\n");
            out.write("\tfunction alterar() {\r\n");
            out.write("\t\tvar form = document.forms[0];\r\n");
            out.write("\t\tform.metodo.value = 'editar';\r\n");
            out.write("\t\tform.submit();\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction isVazia(param){\r\n");
            out.write("\t\tif(param.value == '' || param.length == 0){\r\n");
            out.write("\t\t\treturn true;\r\n");
            out.write("\t\t}\r\n");
            out.write("\t\treturn false;\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction validarUsuario(){\r\n");
            out.write("\t\terros = 0;\r\n");
            out.write("<!--\t\tif(isVazia(document.getElementById('nome').value) && erros == 0){-->\r\n");
            out.write("<!--\t\t\talert('O campo nome é obrigatório.');-->\r\n");
            out.write("<!--\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("<!--\t\tif(isVazia(document.getElementById('login').value) && erros == 0){-->\r\n");
            out.write("<!--\t\t\talert('O campo login é obrigatório.');-->\r\n");
            out.write("<!--\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("<!--\t\tif(isVazia(document.getElementById('senha').value) && erros == 0){-->\r\n");
            out.write("<!--\t\t\talert('Informe a senha.');-->\r\n");
            out.write("<!--\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("<!--\t\tif(isVazia(document.getElementById('senhaConfirmacao').value) && erros == 0){-->\r\n");
            out.write("<!--\t\t\talert('Confirme a senha.');-->\r\n");
            out.write("<!--\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("<!--\t\tif(isVazia(document.getElementById('grupoUsuario').value) && erros == 0){-->\r\n");
            out.write("<!--\t\t\talert('O campo Grupo de Acesso  é obrigatório.');-->\r\n");
            out.write("<!--\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("<!--\t\t-->\r\n");
            out.write("<!--\t\tif(erros == 0){-->\r\n");
            out.write("<!--\t\t\tif(document.getElementById('senha').value != document.getElementById('senhaConfirmacao').value){-->\r\n");
            out.write("<!--\t\t\t\talert('A confirmação da senha não confere com a senha informada.');-->\r\n");
            out.write("<!--\t\t\t\terros = 1;-->\r\n");
            out.write("<!--\t\t\t}-->\r\n");
            out.write("<!--\t\t}-->\r\n");
            out.write("\t\t\r\n");
            out.write("\t\tif(erros == 0){\r\n");
            out.write("\t\t\tdocument.getElementById('metodo').value = 'cadastrar';\r\n");
            out.write("\t  \t  \tdocument.form.submit();\r\n");
            out.write("\t\t}\r\n");
            out.write("\t\t\r\n");
            out.write("\t}\r\n");
            out.write("\t\r\n");
            out.write("\tfunction submeter(){\r\n");
            out.write("\t\t\tdocument.getElementById('metodo').value = 'cadastrar';\r\n");
            out.write("\t  \t  \tdocument.forms[0].submit();\r\n");
            out.write("\t}\r\n");
            out.write("\r\n");
            out.write("\t\r\n");
            out.write("\t\r\n");
            out.write("</script>\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("<div id=\"corpo\">\r\n");
            out.write("    <div class=\"breadcrumb\">\n");
            out.write("    ");
            if (_jspx_meth_html_005flink_005f0(_jspx_page_context)) return;
            out.write("\n");
            out.write("      &raquo;  ");
            if (_jspx_meth_html_005flink_005f1(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("\t\t&raquo;<a class=\"ativo\" href=\"#\">Cadastro de Usuário</a> </div>\n");
            out.write("\r\n");
            out.write("\t<h2>Cadastro de Usuários</h2>\r\n");
            out.write(" \t\r\n");
            out.write("\r\n");
            if (_jspx_meth_html_005fform_005f0(_jspx_page_context)) return;
            out.write("\r\n");
            out.write("</div>\r\n");
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