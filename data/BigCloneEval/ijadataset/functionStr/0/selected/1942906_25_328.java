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
            _jspx_resourceInjector = (org.glassfish.jsp.api.ResourceInjector) application.getAttribute("com.sun.appserv.jsp.resource.injector");
            out.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">\r\n");
            out.write("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\">\r\n");
            out.write("    <head>\r\n");
            out.write("        <title>General users</title>\r\n");
            out.write("        <meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\r\n");
            out.write("        <link rel=\"stylesheet\" type=\"text/css\" media=\"screen\" href=\"css/screen.css\" />\r\n");
            out.write("        <style>\r\n");
            out.write("            .slide {\r\n");
            out.write("                position: relative;\r\n");
            out.write("\t\t\t\tdisplay:block;\r\n");
            out.write("\t\t\t\tfloat:left;\r\n");
            out.write("\t\t\t\theight:100%;\r\n");
            out.write("\t\t\t\tz-index:999;\r\n");
            out.write("            }\r\n");
            out.write("            .slide .inner {\r\n");
            out.write("\t\t\t\tdisplay:block;\r\n");
            out.write("                position: relative;\r\n");
            out.write("                left: 0;\r\n");
            out.write("                bottom: 0;\r\n");
            out.write("\t\t\t\tz-index:3;\r\n");
            out.write("            }        \r\n");
            out.write("\t\t\t.slide .slidetab{\r\n");
            out.write("\t\t\t\tbackground-color:#666;\r\n");
            out.write("\t\t\t\tposition:relative;\r\n");
            out.write("\t\t\t\tz-index:999;\r\n");
            out.write("\t\t\t\t}\r\n");
            out.write("        #apDiv1 {\r\n");
            out.write("\tposition:absolute;\r\n");
            out.write("\tleft:12px;\r\n");
            out.write("\ttop:118px;\r\n");
            out.write("\twidth:224px;\r\n");
            out.write("\theight:181px;\r\n");
            out.write("\tz-index:2;\r\n");
            out.write("}\r\n");
            out.write("        </style>\r\n");
            out.write("        <!-- jQuery - the core -->\r\n");
            out.write("        <script src=\"js/jquery-1.3.2.min.js\" type=\"text/javascript\"></script>\r\n");
            out.write("        <script type=\"text/javascript\">\r\n");
            out.write("                    $(document).ready(function() {\r\n");
            out.write("                $('#slideleft button').click(function() {\r\n");
            out.write("                    var $lefty = $(this).next();\r\n");
            out.write("                    $lefty.animate({\r\n");
            out.write("                        left: parseInt($lefty.css('left'),10) == 0 ?\r\n");
            out.write("                            -$lefty.outerWidth() :\r\n");
            out.write("                            0\r\n");
            out.write("                    });\r\n");
            out.write("                });\r\n");
            out.write("            });\r\n");
            out.write("        </script>\r\n");
            out.write("        <script src=\"OpenLayers.js\"></script>\r\n");
            out.write("            <script type=\"text/javascript\">\t\t\t\r\n");
            out.write("            var SinglePoint = OpenLayers.Class.create();\r\n");
            out.write("            SinglePoint.prototype = OpenLayers.Class.inherit(OpenLayers.Handler.Point, {\r\n");
            out.write("                createFeature: function(evt) {\r\n");
            out.write("                    this.control.layer.removeFeatures(this.control.layer.features);\r\n");
            out.write("                    OpenLayers.Handler.Point.prototype.createFeature.apply(this, arguments);\r\n");
            out.write("                }\r\n");
            out.write("            });\r\n");
            out.write("\r\n");
            out.write("            var start_style = OpenLayers.Util.applyDefaults({\r\n");
            out.write("                externalGraphic: \"img/start.png\",\r\n");
            out.write("                graphicWidth: 18,\r\n");
            out.write("                graphicHeight: 26,\r\n");
            out.write("                graphicYOffset: -26,\r\n");
            out.write("                graphicOpacity: 1\r\n");
            out.write("            }, OpenLayers.Feature.Vector.style['default']);\r\n");
            out.write("\r\n");
            out.write("            var stop_style = OpenLayers.Util.applyDefaults({\r\n");
            out.write("                externalGraphic: \"img/stop.png\",\r\n");
            out.write("                graphicWidth: 18,\r\n");
            out.write("                graphicHeight: 26,\r\n");
            out.write("                graphicYOffset: -26,\r\n");
            out.write("                graphicOpacity: 1\r\n");
            out.write("            }, OpenLayers.Feature.Vector.style['default']);\r\n");
            out.write("\r\n");
            out.write("            var result_style = OpenLayers.Util.applyDefaults({\r\n");
            out.write("                strokeWidth: 3,\r\n");
            out.write("                strokeColor: \"#ff0000\",\r\n");
            out.write("                fillOpacity: 0\r\n");
            out.write("            }, OpenLayers.Feature.Vector.style['default']);\r\n");
            out.write("\r\n");
            out.write("            // global variables\r\n");
            out.write("            var map,start, stop,result, controls;\r\n");
            out.write("\r\n");
            out.write("            function init() {\r\n");
            out.write("                format=\"image/png\";\r\n");
            out.write("                var bounds = new OpenLayers.Bounds(\r\n");
            out.write("                617817.75, 3047534.75,\r\n");
            out.write("                650331.75, 3077605.75\r\n");
            out.write("            );\r\n");
            out.write("                var options = {\r\n");
            out.write("                    controls: [],\r\n");
            out.write("                    maxExtent: bounds,\r\n");
            out.write("                    maxResolution: 127.0078125,\r\n");
            out.write("                    projection: \"EPSG:3857\",\r\n");
            out.write("                    units: 'm'\r\n");
            out.write("                };\t\t\t\r\n");
            out.write("                map = new OpenLayers.Map('map', options);\r\n");
            out.write("                // setup tiled layer\r\n");
            out.write("                var tiled = new OpenLayers.Layer.WMS(\r\n");
            out.write("                \"cite:ktm_roads01 - Tiled\", \"http://localhost:8080/geoserver/cite/wms\",\r\n");
            out.write("                {\r\n");
            out.write("                    LAYERS: 'cite:ktm_roads01',\r\n");
            out.write("                    STYLES: '',\r\n");
            out.write("                    format: format,\r\n");
            out.write("                    tilesOrigin : map.maxExtent.left + ',' + map.maxExtent.bottom\r\n");
            out.write("                },\r\n");
            out.write("                {\r\n");
            out.write("                    buffer: 0,\r\n");
            out.write("                    displayOutsideMaxExtent: true,\r\n");
            out.write("                    isBaseLayer: true\r\n");
            out.write("                } \r\n");
            out.write("            );\t\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("                // create and add layers to the map\t\t\t\t\r\n");
            out.write("                start = new OpenLayers.Layer.Vector(\"Start point\", {style: start_style});\r\n");
            out.write("                stop = new OpenLayers.Layer.Vector(\"End point\", {style: stop_style});\r\n");
            out.write("                downtown = new OpenLayers.Layer.Vector(\"Downtown data area\",\r\n");
            out.write("                {style: result_style});\r\n");
            out.write("                result = new OpenLayers.Layer.Vector(\"Routing results\",\r\n");
            out.write("                {style: result_style});\r\n");
            out.write("\r\n");
            out.write("                // controls\r\n");
            out.write("                map.addControl(new OpenLayers.Control.LayerSwitcher());\r\n");
            out.write("                map.addControl(new OpenLayers.Control.MousePosition()); \r\n");
            out.write("                map.addControl(new OpenLayers.Control.Navigation());\r\n");
            out.write("                map.addControl(new OpenLayers.Control.PanZoomBar({\r\n");
            out.write("                    position: new OpenLayers.Pixel(2, 15)\r\n");
            out.write("                }));\t\t\t\r\n");
            out.write("                controls = {\r\n");
            out.write("                    start: new OpenLayers.Control.DrawFeature(start, SinglePoint),\r\n");
            out.write("                    stop: new OpenLayers.Control.DrawFeature(stop, SinglePoint)\r\n");
            out.write("                }\r\n");
            out.write("                for (var key in controls) {\r\n");
            out.write("                    map.addControl(controls[key]);\r\n");
            out.write("                }\r\n");
            out.write("                map.addLayers([tiled, start, stop,result]);\t\r\n");
            out.write("\t\t\t\t\r\n");
            out.write("                // set default position\r\n");
            out.write("                map.zoomToExtent(bounds);\r\n");
            out.write("            }\r\n");
            out.write("\r\n");
            out.write("            function toggleControl(element) {\r\n");
            out.write("                for (key in controls) {\r\n");
            out.write("                    if (element.value == key && element.checked) {\r\n");
            out.write("                        controls[key].activate();\r\n");
            out.write("                    } else {\r\n");
            out.write("                        controls[key].deactivate();\r\n");
            out.write("                    }\r\n");
            out.write("                }\r\n");
            out.write("            }\r\n");
            out.write("\r\n");
            out.write("            function compute() {\r\n");
            out.write("                var startPoint = start.features[0];\r\n");
            out.write("                var stopPoint = stop.features[0];\r\n");
            out.write("                //alert(startPoint.geometry.x+\",\"+startPoint.geometry.y);\r\n");
            out.write("                if (startPoint && stopPoint) {\r\n");
            out.write("                    var result = {\r\n");
            out.write("                        startpoint: startPoint.geometry.x + ' ' + startPoint.geometry.y,\r\n");
            out.write("                        finalpoint: stopPoint.geometry.x + ' ' + stopPoint.geometry.y,\r\n");
            out.write("                        method: OpenLayers.Util.getElement('method').value,\r\n");
            out.write("                        region: \"ktm_roads01\",\r\n");
            out.write("                        srid: \"3857\"\r\n");
            out.write("                    };\r\n");
            out.write("                    OpenLayers.loadURL(\"shortestpath.jsp?\"+\r\n");
            out.write("                        OpenLayers.Util.getParameterString(result),'',\r\n");
            out.write("                    null,\r\n");
            out.write("                    displayRoute);\r\n");
            out.write("                }\r\n");
            out.write("            }\r\n");
            out.write("\r\n");
            out.write("            function displayRoute(response) {\r\n");
            out.write("                \r\n");
            out.write("                if (response && response.responseXML) {\r\n");
            out.write("                    \r\n");
            out.write("                    // erase the previous results\r\n");
            out.write("                    result.removeFeatures(result.features);\r\n");
            out.write("\r\n");
            out.write("                    // parse the features\r\n");
            out.write("                    var parser = new OpenLayers.Format.WKT(map.baseLayer.projection);\r\n");
            out.write("                    //                    var line1=\"POINT(628994.4375 3062887.7695313)\";\r\n");
            out.write("                    //                    alert(line1);\r\n");
            out.write("                    //                    var features=parser.read(line1);\r\n");
            out.write("                    var edges = response.responseXML.getElementsByTagName('edge');\r\n");
            out.write("                    var features = [];\r\n");
            out.write("                    for (var i = 0; i < edges.length; i++) {\r\n");
            out.write("                        var g = parser.read(edges[i].getElementsByTagName('wkt')[0].textContent);\r\n");
            out.write("                        features.push(g);\r\n");
            out.write("                    }\r\n");
            out.write("                    result.addFeatures(features);\r\n");
            out.write("                }\r\n");
            out.write("            }\r\n");
            out.write("\t\t\t</script>        \r\n");
            out.write("    </head>\r\n");
            out.write("\r\n");
            out.write("<body onload=\"init();\">\r\n");
            out.write("        <!-- header starts-->\r\n");
            out.write("        <div id=\"apDiv1\">\r\n");
            out.write("          <form id=\"form1\" method=\"post\" action=\"\">\r\n");
            out.write("            <p>\r\n");
            out.write("              <input name=\"Source\" type=\"text\" id=\"Source\" value=\"Source\" />\r\n");
            out.write("            </p>\r\n");
            out.write("            <p>\r\n");
            out.write("              <input name=\"Destination\" type=\"text\" id=\"Destination\" value=\"Destination\" />\r\n");
            out.write("            </p>\r\n");
            out.write("            <p>\r\n");
            out.write("              <input type=\"submit\" name=\"Search\" id=\"Search\" value=\"Search\" />\r\n");
            out.write("            </p>\r\n");
            out.write("          </form>\r\n");
            out.write("        </div>\r\n");
            out.write("        <div id=\"header-wrap\">\r\n");
            out.write("          <div id=\"user_thumbs\" class=\"thumbs\"><img src=\"images/gravatar.jpg\" /></div>\r\n");
            out.write("        <div id=\"user_info\" class=\"container_user_info\">");
            out.write((java.lang.String) org.apache.jasper.runtime.PageContextImpl.evaluateExpression("${loginForm.userName}", java.lang.String.class, (PageContext) _jspx_page_context, null));
            out.write("</div>\r\n");
            out.write("        <div id=\"user_tools\">\r\n");
            out.write("            <input type=\"radio\" name=\"control\" id=\"noneToggle\"\r\n");
            out.write("                   onclick=\"toggleControl(this);\" checked=\"checked\" />\r\n");
            out.write("            <label for=\"noneToggle\">navigate</label>\r\n");
            out.write("            <input type=\"radio\" name=\"control\" value=\"start\" id=\"startToggle\"\r\n");
            out.write("                   onclick=\"toggleControl(this);\" />\r\n");
            out.write("            <label for=\"startToggle\">set start point</label>\r\n");
            out.write("            <input type=\"radio\" name=\"control\" value=\"stop\" id=\"stopToggle\"\r\n");
            out.write("                   onclick=\"toggleControl(this);\" />\r\n");
            out.write("          <label for=\"stopToggle\">set stop point</label>\r\n");
            out.write("          <input type=\"hidden\" name=\"method\" id=\"method\" value=\"SPD\" />\r\n");
            out.write("            <button onclick=\"compute()\">Calculate Route</button>\r\n");
            out.write("        </div>\r\n");
            out.write("            <div id=\"header\" class=\"container_16\">\t\t\r\n");
            out.write("                ");
            out.write("<!-- navigation -->\r\n");
            out.write("<div  id=\"nav\">\r\n");
            out.write("\t\t\t<ul>\r\n");
            out.write("\t\t\t\t<li id=\"current\"><a href=\"index.jsp\">Home</a></li>\r\n");
            out.write("\t\t\t\t<li><a href=\"index.jsp\">Link1 </a></li>\r\n");
            out.write("\t\t\t\t<li><a href=\"index.jsp\">Link2</a></li>\r\n");
            out.write("\t\t\t\t<li><a href=\"index.jsp\">link3</a></li>\r\n");
            out.write("\t\t\t\t<li><a href=\"index.jsp\">Link4</a></li>\r\n");
            out.write("\t\t\t\t<li><a href=\"index.jsp\">About</a></li>\t\t\r\n");
            out.write("\t\t\t</ul>\t\t\r\n");
            out.write("\t\t</div>\t\t\r\n");
            out.write("\t\t<!-- navigation ends here-->\r\n");
            out.write("  <form id=\"quick-search\" action=\"index.jsp\" method=\"get\" >\r\n");
            out.write("\t\t\t<p>\r\n");
            out.write("\t\t\t<label for=\"qsearch\">Search:</label>\r\n");
            out.write("\t\t\t<input class=\"tbox\" id=\"qsearch\" type=\"text\" name=\"qsearch\" value=\"Search...\" title=\"Start typing and hit ENTER\" />\r\n");
            out.write("\t\t\t<input class=\"btn\" alt=\"Search\" type=\"image\" name=\"searchsubmit\" title=\"Search\" src=\"images/search.gif\" />\r\n");
            out.write("\t\t\t</p>\r\n");
            out.write("\t\t</form>\t\t\t\r\n");
            out.write("\t<!-- header ends here -->\r\n");
            out.write("\r\n");
            out.write("\r\n");
            out.write("            </div>    \r\n");
            out.write("</div>\r\n");
            out.write("        <!-- content starts -->\r\n");
            out.write("\r\n");
            out.write("        <div id=\"content-outer\">\r\n");
            out.write("            <div id=\"content-wrapper\" class=\"container_16\">\r\n");
            out.write("             <div id='map' style='width: 950px; height: 550px;'></div>\r\n");
            out.write("            </div>\r\n");
            out.write("        </div>\r\n");
            out.write("        <!-- content ends here -->\r\n");
            out.write("        <div id=\"docs\">\r\n");
            out.write("        </div>\r\n");
            out.write("        <!-- footer starts here -->\t\r\n");
            out.write("        ");
            out.write("<div id=\"footer-wrapper\" class=\"container_16\">\t\r\n");
            out.write("\t\t<div id=\"footer-content\"></div>\r\n");
            out.write("</div>");
            out.write("\r\n");
            out.write("        <!-- footer ends here -->\r\n");
            out.write("\r\n");
            out.write("    </body>\r\n");
            out.write("</html>\r\n");
        } catch (Throwable t) {
            if (!(t instanceof SkipPageException)) {
                out = _jspx_out;
                if (out != null && out.getBufferSize() != 0) out.clearBuffer();
                if (_jspx_page_context != null) _jspx_page_context.handlePageException(t); else throw new ServletException(t);
            }
        } finally {
            _jspxFactory.releasePageContext(_jspx_page_context);
        }
    }
}