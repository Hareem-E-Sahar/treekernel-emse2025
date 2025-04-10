public class Test {    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (request.getParameter("empty") != null) {
            response.setStatus(200);
            response.flushBuffer();
            return;
        }
        if (request.getParameter("sleep") != null) {
            try {
                long s = Long.parseLong(request.getParameter("sleep"));
                Thread.sleep(s / 2);
                response.sendError(102);
                Thread.sleep(s / 2);
            } catch (InterruptedException e) {
                return;
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
        if (request.getParameter("continue") != null) {
            Continuation continuation = ContinuationSupport.getContinuation(request, null);
            continuation.suspend(Long.parseLong(request.getParameter("continue")));
        }
        request.setAttribute("Dump", this);
        getServletContext().setAttribute("Dump", this);
        String length = request.getParameter("length");
        if (length != null && length.length() > 0) {
            response.setContentLength(Integer.parseInt(length));
        }
        String data = request.getParameter("data");
        String block = request.getParameter("block");
        String dribble = request.getParameter("dribble");
        if (data != null && data.length() > 0) {
            int d = Integer.parseInt(data);
            int b = (block != null && block.length() > 0) ? Integer.parseInt(block) : 50;
            byte[] buf = new byte[b];
            for (int i = 0; i < b; i++) {
                buf[i] = (byte) ('0' + (i % 10));
                if (i % 10 == 9) buf[i] = (byte) '\n';
            }
            buf[0] = 'o';
            OutputStream out = response.getOutputStream();
            response.setContentType("text/plain");
            while (d > 0) {
                if (b == 1) {
                    out.write(d % 80 == 0 ? '\n' : '.');
                    d--;
                } else if (d >= b) {
                    out.write(buf);
                    d = d - b;
                } else {
                    out.write(buf, 0, d);
                    d = 0;
                }
                if (dribble != null) {
                    out.flush();
                    try {
                        Thread.sleep(Long.parseLong(dribble));
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
                    }
                }
            }
            return;
        }
        String info = request.getPathInfo();
        if (info != null && info.endsWith("Exception")) {
            try {
                throw (Throwable) Thread.currentThread().getContextClassLoader().loadClass(info.substring(1)).newInstance();
            } catch (Throwable th) {
                throw new ServletException(th);
            }
        }
        String reset = request.getParameter("reset");
        if (reset != null && reset.length() > 0) {
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            response.setHeader("SHOULD_NOT", "BE SEEN");
            response.reset();
        }
        String redirect = request.getParameter("redirect");
        if (redirect != null && redirect.length() > 0) {
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            response.sendRedirect(redirect);
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            return;
        }
        String error = request.getParameter("error");
        if (error != null && error.length() > 0) {
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            response.sendError(Integer.parseInt(error));
            response.getOutputStream().println("THIS SHOULD NOT BE SEEN!");
            return;
        }
        String buffer = request.getParameter("buffer");
        if (buffer != null && buffer.length() > 0) response.setBufferSize(Integer.parseInt(buffer));
        String charset = request.getParameter("charset");
        if (charset == null) charset = "UTF-8";
        response.setCharacterEncoding(charset);
        response.setContentType("text/html");
        if (info != null && info.indexOf("Locale/") >= 0) {
            try {
                String locale_name = info.substring(info.indexOf("Locale/") + 7);
                Field f = java.util.Locale.class.getField(locale_name);
                response.setLocale((Locale) f.get(null));
            } catch (Exception e) {
                e.printStackTrace();
                response.setLocale(Locale.getDefault());
            }
        }
        String cn = request.getParameter("cookie");
        String cv = request.getParameter("cookiev");
        if (cn != null && cv != null) {
            Cookie cookie = new Cookie(cn, cv);
            if (request.getParameter("version") != null) cookie.setVersion(Integer.parseInt(request.getParameter("version")));
            cookie.setComment("Cookie from dump servlet");
            response.addCookie(cookie);
        }
        String pi = request.getPathInfo();
        if (pi != null && pi.startsWith("/ex")) {
            OutputStream out = response.getOutputStream();
            out.write("</H1>This text should be reset</H1>".getBytes());
            if ("/ex0".equals(pi)) throw new ServletException("test ex0", new Throwable()); else if ("/ex1".equals(pi)) throw new IOException("test ex1"); else if ("/ex2".equals(pi)) throw new UnavailableException("test ex2"); else if (pi.startsWith("/ex2/")) throw new UnavailableException("test ex2", Integer.parseInt(pi.substring(5)));
            throw new RuntimeException("test");
        }
        if ("true".equals(request.getParameter("close"))) response.setHeader("Connection", "close");
        String buffered = request.getParameter("buffered");
        PrintWriter pout = null;
        try {
            pout = response.getWriter();
        } catch (IllegalStateException e) {
            pout = new PrintWriter(new OutputStreamWriter(response.getOutputStream(), charset));
        }
        if (buffered != null) pout = new PrintWriter(new BufferedWriter(pout, Integer.parseInt(buffered)));
        try {
            pout.write("<html>\n<body>\n");
            pout.write("<h1>Dump Servlet</h1>\n");
            pout.write("<table width=\"95%\">");
            pout.write("<tr>\n");
            pout.write("<th align=\"right\">getMethod:&nbsp;</th>");
            pout.write("<td>" + request.getMethod() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContentLength:&nbsp;</th>");
            pout.write("<td>" + Integer.toString(request.getContentLength()) + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContentType:&nbsp;</th>");
            pout.write("<td>" + request.getContentType() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestURI:&nbsp;</th>");
            pout.write("<td>" + request.getRequestURI() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestURL:&nbsp;</th>");
            pout.write("<td>" + request.getRequestURL() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getContextPath:&nbsp;</th>");
            pout.write("<td>" + request.getContextPath() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServletPath:&nbsp;</th>");
            pout.write("<td>" + request.getServletPath() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getPathInfo:&nbsp;</th>");
            pout.write("<td>" + request.getPathInfo() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getPathTranslated:&nbsp;</th>");
            pout.write("<td>" + request.getPathTranslated() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getQueryString:&nbsp;</th>");
            pout.write("<td>" + request.getQueryString() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getProtocol:&nbsp;</th>");
            pout.write("<td>" + request.getProtocol() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getScheme:&nbsp;</th>");
            pout.write("<td>" + request.getScheme() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServerName:&nbsp;</th>");
            pout.write("<td>" + request.getServerName() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getServerPort:&nbsp;</th>");
            pout.write("<td>" + Integer.toString(request.getServerPort()) + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalName:&nbsp;</th>");
            pout.write("<td>" + request.getLocalName() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalAddr:&nbsp;</th>");
            pout.write("<td>" + request.getLocalAddr() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocalPort:&nbsp;</th>");
            pout.write("<td>" + Integer.toString(request.getLocalPort()) + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteUser:&nbsp;</th>");
            pout.write("<td>" + request.getRemoteUser() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteAddr:&nbsp;</th>");
            pout.write("<td>" + request.getRemoteAddr() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemoteHost:&nbsp;</th>");
            pout.write("<td>" + request.getRemoteHost() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRemotePort:&nbsp;</th>");
            pout.write("<td>" + request.getRemotePort() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getRequestedSessionId:&nbsp;</th>");
            pout.write("<td>" + request.getRequestedSessionId() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">isSecure():&nbsp;</th>");
            pout.write("<td>" + request.isSecure() + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">isUserInRole(admin):&nbsp;</th>");
            pout.write("<td>" + request.isUserInRole("admin") + "</td>");
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"right\">getLocale:&nbsp;</th>");
            pout.write("<td>" + request.getLocale() + "</td>");
            Enumeration locales = request.getLocales();
            while (locales.hasMoreElements()) {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">getLocales:&nbsp;</th>");
                pout.write("<td>" + locales.nextElement() + "</td>");
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Other HTTP Headers:</big></th>");
            Enumeration h = request.getHeaderNames();
            String name;
            while (h.hasMoreElements()) {
                name = (String) h.nextElement();
                Enumeration h2 = request.getHeaders(name);
                while (h2.hasMoreElements()) {
                    String hv = (String) h2.nextElement();
                    pout.write("</tr><tr>\n");
                    pout.write("<th align=\"right\">" + name + ":&nbsp;</th>");
                    pout.write("<td>" + hv + "</td>");
                }
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Request Parameters:</big></th>");
            h = request.getParameterNames();
            while (h.hasMoreElements()) {
                name = (String) h.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">" + name + ":&nbsp;</th>");
                pout.write("<td>" + request.getParameter(name) + "</td>");
                String[] values = request.getParameterValues(name);
                if (values == null) {
                    pout.write("</tr><tr>\n");
                    pout.write("<th align=\"right\">" + name + " Values:&nbsp;</th>");
                    pout.write("<td>" + "NULL!" + "</td>");
                } else if (values.length > 1) {
                    for (int i = 0; i < values.length; i++) {
                        pout.write("</tr><tr>\n");
                        pout.write("<th align=\"right\">" + name + "[" + i + "]:&nbsp;</th>");
                        pout.write("<td>" + values[i] + "</td>");
                    }
                }
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Cookies:</big></th>");
            Cookie[] cookies = request.getCookies();
            for (int i = 0; cookies != null && i < cookies.length; i++) {
                Cookie cookie = cookies[i];
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">" + cookie.getName() + ":&nbsp;</th>");
                pout.write("<td>" + cookie.getValue() + "</td>");
            }
            String content_type = request.getContentType();
            if (content_type != null && !content_type.startsWith("application/x-www-form-urlencoded") && !content_type.startsWith("multipart/form-data")) {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"left\" valign=\"top\" colspan=\"2\"><big><br/>Content:</big></th>");
                pout.write("</tr><tr>\n");
                pout.write("<td><pre>");
                char[] content = new char[4096];
                int len;
                try {
                    Reader in = request.getReader();
                    while ((len = in.read(content)) >= 0) pout.write(content, 0, len);
                } catch (IOException e) {
                    pout.write(e.toString());
                }
                pout.write("</pre></td>");
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Request Attributes:</big></th>");
            Enumeration a = request.getAttributeNames();
            while (a.hasMoreElements()) {
                name = (String) a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\" valign=\"top\">" + name + ":&nbsp;</th>");
                pout.write("<td>" + "<pre>" + toString(request.getAttribute(name)) + "</pre>" + "</td>");
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Servlet InitParameters:</big></th>");
            a = getInitParameterNames();
            while (a.hasMoreElements()) {
                name = (String) a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">" + name + ":&nbsp;</th>");
                pout.write("<td>" + toString(getInitParameter(name)) + "</td>");
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Context InitParameters:</big></th>");
            a = getServletContext().getInitParameterNames();
            while (a.hasMoreElements()) {
                name = (String) a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">" + name + ":&nbsp;</th>");
                pout.write("<td>" + toString(getServletContext().getInitParameter(name)) + "</td>");
            }
            pout.write("</tr><tr>\n");
            pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Context Attributes:</big></th>");
            a = getServletContext().getAttributeNames();
            while (a.hasMoreElements()) {
                name = (String) a.nextElement();
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\" valign=\"top\">" + name + ":&nbsp;</th>");
                pout.write("<td>" + "<pre>" + toString(getServletContext().getAttribute(name)) + "</pre>" + "</td>");
            }
            String res = request.getParameter("resource");
            if (res != null && res.length() > 0) {
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"left\" colspan=\"2\"><big><br/>Get Resource: \"" + res + "\"</big></th>");
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">this.getClass().getResource(...):&nbsp;</th>");
                pout.write("<td>" + this.getClass().getResource(res) + "</td>");
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">this.getClass().getClassLoader().getResource(...):&nbsp;</th>");
                pout.write("<td>" + this.getClass().getClassLoader().getResource(res) + "</td>");
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">Thread.currentThread().getContextClassLoader().getResource(...):&nbsp;</th>");
                pout.write("<td>" + Thread.currentThread().getContextClassLoader().getResource(res) + "</td>");
                pout.write("</tr><tr>\n");
                pout.write("<th align=\"right\">getServletContext().getResource(...):&nbsp;</th>");
                try {
                    pout.write("<td>" + getServletContext().getResource(res) + "</td>");
                } catch (Exception e) {
                    pout.write("<td>" + "" + e + "</td>");
                }
            }
            pout.write("</tr></table>\n");
            pout.write("<h2>Request Wrappers</h2>\n");
            ServletRequest rw = request;
            int w = 0;
            while (rw != null) {
                pout.write((w++) + ": " + rw.getClass().getName() + "<br/>");
                if (rw instanceof HttpServletRequestWrapper) rw = ((HttpServletRequestWrapper) rw).getRequest(); else if (rw instanceof ServletRequestWrapper) rw = ((ServletRequestWrapper) rw).getRequest(); else rw = null;
            }
            pout.write("<br/>");
            pout.write("<h2>International Characters (UTF-8)</h2>");
            pout.write("LATIN LETTER SMALL CAPITAL AE<br/>\n");
            pout.write("Directly uni encoded(\\u1d01): ᴁ<br/>");
            pout.write("HTML reference (&amp;AElig;): &AElig;<br/>");
            pout.write("Decimal (&amp;#7425;): &#7425;<br/>");
            pout.write("Javascript unicode (\\u1d01) : <script language='javascript'>document.write(\"ᴁ\");</script><br/>");
            pout.write("<br/>");
            pout.write("<h2>Form to generate GET content</h2>");
            pout.write("<form method=\"GET\" action=\"" + response.encodeURL(getURI(request)) + "\">");
            pout.write("TextField: <input type=\"text\" name=\"TextField\" value=\"value\"/><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"Submit\">");
            pout.write("</form>");
            pout.write("<br/>");
            pout.write("<h2>Form to generate POST content</h2>");
            pout.write("<form method=\"POST\" accept-charset=\"utf-8\" action=\"" + response.encodeURL(getURI(request)) + "\">");
            pout.write("TextField: <input type=\"text\" name=\"TextField\" value=\"value\"/><br/>\n");
            pout.write("Select: <select multiple name=\"Select\">\n");
            pout.write("<option>ValueA</option>");
            pout.write("<option>ValueB1,ValueB2</option>");
            pout.write("<option>ValueC</option>");
            pout.write("</select><br/>");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"Submit\"><br/>");
            pout.write("</form>");
            pout.write("<br/>");
            pout.write("<h2>Form to generate UPLOAD content</h2>");
            pout.write("<form method=\"POST\" enctype=\"multipart/form-data\" accept-charset=\"utf-8\" action=\"" + response.encodeURL(getURI(request)) + "\">");
            pout.write("TextField: <input type=\"text\" name=\"TextField\" value=\"comment\"/><br/>\n");
            pout.write("File 1: <input type=\"file\" name=\"file1\" /><br/>\n");
            pout.write("File 2: <input type=\"file\" name=\"file2\" /><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"Submit\"><br/>");
            pout.write("</form>");
            pout.write("<h2>Form to set Cookie</h2>");
            pout.write("<form method=\"POST\" action=\"" + response.encodeURL(getURI(request)) + "\">");
            pout.write("cookie: <input type=\"text\" name=\"cookie\" /><br/>\n");
            pout.write("value: <input type=\"text\" name=\"cookiev\" /><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"setCookie\">");
            pout.write("</form>\n");
            pout.write("<h2>Form to get Resource</h2>");
            pout.write("<form method=\"POST\" action=\"" + response.encodeURL(getURI(request)) + "\">");
            pout.write("resource: <input type=\"text\" name=\"resource\" /><br/>\n");
            pout.write("<input type=\"submit\" name=\"Action\" value=\"getResource\">");
            pout.write("</form>\n");
        } catch (Exception e) {
            getServletContext().log("dump", e);
        }
        if (request.getParameter("stream") != null) {
            pout.flush();
            Continuation continuation = ContinuationSupport.getContinuation(request, null);
            continuation.suspend(Long.parseLong(request.getParameter("stream")));
        }
        String lines = request.getParameter("lines");
        if (lines != null) {
            char[] line = "<span>A line of characters. Blah blah blah blah.  blooble blooble</span></br>\n".toCharArray();
            for (int l = Integer.parseInt(lines); l-- > 0; ) {
                pout.write("<span>" + l + " </span>");
                pout.write(line);
            }
        }
        pout.write("</body>\n</html>\n");
        pout.close();
        if (pi != null) {
            if ("/ex4".equals(pi)) throw new ServletException("test ex4", new Throwable());
            if ("/ex5".equals(pi)) throw new IOException("test ex5");
            if ("/ex6".equals(pi)) throw new UnavailableException("test ex6");
        }
    }
}