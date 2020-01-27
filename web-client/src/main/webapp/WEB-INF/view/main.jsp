<%@ page import="lsfusion.base.ServerMessages" %>
<%@ page isELIgnored="false" %>

<!DOCTYPE html>

<%@ page contentType="text/html;charset=UTF-8" %>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        
        <title>${title}</title>
        <link rel="shortcut icon" href="${logicsIcon}" />
        <link id="themeCss" rel="stylesheet" type="text/css" href="static/css/light.css"/>

        <style>
            @import url('https://fonts.googleapis.com/css?family=Montserrat:400,400i,700,700i|Roboto:400,400i,700,700i|Open+Sans:400,400i,700,700i&display=swap&subset=cyrillic');
        </style>
        
        <style type="text/css">
            #loading {
                border: 1px solid #ccc;
                position: absolute;
                left: 45%;
                top: 40%;
                padding: 2px;
                z-index: 20001;
                height: auto;
            }

            #loading a {
                color: #225588;
            }

            #loading .loadingIndicator {
                background: white;
                font: bold 13px tahoma, arial, helvetica;
                padding: 10px;
                margin: 0;
                height: auto;
                color: #444;
            }

            #loadingGif {
                vertical-align:top;
            }

            #loadingMsg {
                font: normal 13px arial, tahoma, sans-serif;
            }
        </style>

        <!-- need jquery for pivot table -->
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.11.2/jquery.min.js"></script>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui/1.11.4/jquery-ui.min.js"></script>

        <!-- export pivot to pdf -->
        <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/html-to-pdfmake/docs/browser.js"></script>
        <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/pdfmake@0.1.57/build/pdfmake.min.js"></script>
        <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/pdfmake@0.1.57/build/vfs_fonts.js"></script>

        <!-- export pivot to excel -->
        <script type="text/javascript" src="https://cdn.jsdelivr.net/gh/linways/table-to-excel@v1.0.4/dist/tableToExcel.js"></script>

        <!-- optional: mobile support with jqueryui-touch-punch -->
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/jqueryui-touch-punch/0.2.3/jquery.ui.touch-punch.min.js"></script>

        <!-- pivot table -->
        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/pivottable/2.23.0/pivot.min.css">
        <script type="text/javascript" src="static/js/pivot.js"></script>

        <!-- math for formulas in pivoting -->
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/mathjs/6.2.2/math.min.js"></script>
        <script type="text/javascript" src="static/js/utils.js"></script>

        <!-- subtotal.js libs : subtotal_renderers -->
        <link rel="stylesheet" type="text/css" href="https://cdn.jsdelivr.net/npm/subtotal@1.11.0-alpha.0/dist/subtotal.min.css">
        <script type="text/javascript" src="https://cdn.jsdelivr.net/npm/subtotal@1.11.0-alpha.0/dist/subtotal.min.js"></script>

        <!--  plotly libs : plotly_renderers  -->
        <script type="text/javascript" src="https://cdn.plot.ly/plotly-basic-latest.min.js"></script>
        <%-- will patch plotly_renderers with reverse parameter, since it's makes more sense to show rows on x axis, and columns on y axis --%>
        <%-- + horizontal moved to the end --%>
        <script type="text/javascript" src="static/js/plotly_renderers.js"></script>
<%--        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/2.23.0/plotly_renderers.min.js"></script>--%>

        <!--  c3 / d3 libs : d3_renderers -->
<%--        <link rel="stylesheet" type="text/css" href="https://cdnjs.cloudflare.com/ajax/libs/c3/0.7.11/c3.min.css">--%>
<%--  because d3_renderers doesn't work with v4+ d3 versions --%>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/d3/3.5.5/d3.min.js"></script>
<%--        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/c3/0.7.11/c3.min.js"></script>--%>
<%--        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/2.23.0/c3_renderers.min.js"></script>--%>
        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/2.23.0/d3_renderers.min.js"></script>

<%--        <!--  google charts: gchart_renderers  -->--%>
<%--        <script type="text/javascript" src="https://cdnjs.cloudflare.com/ajax/libs/pivottable/2.23.0/gchart_renderers.min.js"></script>--%>
<%--        <script type="text/javascript" src="https://www.google.com/jsapi"></script>--%>

        <!--  map  -->
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.6.0/dist/leaflet.css"/>
        <script type="text/javascript" src="https://unpkg.com/leaflet@1.6.0/dist/leaflet.js"></script>
    </head>
    <body>
        <script language="JavaScript">
            var pageSetup = {
                webAppRoot: "<%= request.getContextPath() + "/" %>",
                logicsName: "${logicsName}"
            };
        </script>

        <div id="loadingWrapper">
            <div id="loading" align="center">
                <div class="loadingIndicator">
                    <img id="loadingGif" src="static/images/loading.gif" width="16" height="16"/>
                    lsFusion<br/>
                    <span id="loadingMsg"><%= ServerMessages.getString(request, "loading") %></span>
                </div>
            </div>
        </div>
        <%-- gwt js src is <module name>/<module name>.nocache.js --%>
        <script type="text/javascript" language="javascript"
                src="main/main.nocache.js"></script>
    </body>
</html>
