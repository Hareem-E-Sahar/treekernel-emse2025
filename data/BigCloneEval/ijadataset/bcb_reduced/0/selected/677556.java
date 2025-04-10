package com.netx.cubigraf.servlets;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.File;
import java.text.DateFormat;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import com.netx.data.Connection;
import com.netx.data.DatabaseException;
import com.netx.ebs.EbsRequest;
import com.netx.ebs.EbsResponse;
import com.netx.ebs.TemplateValues;
import com.netx.generics.sql.Table;
import com.netx.generics.time.Date;
import com.netx.generics.time.Moment;
import com.netx.generics.util.Tools;
import com.netx.generics.basic.IntegrityException;
import com.netx.generics.io.Streams;
import com.netx.cubigraf.entities.Entities;
import com.netx.cubigraf.entities.Referencia;

public class SrvGestaoMiniaturas extends CubigrafServlet {

    public SrvGestaoMiniaturas() {
        super("gestao-miniaturas");
    }

    protected void doGet(EbsRequest request, EbsResponse response) throws IOException, DatabaseException {
        String operacao = request.getParameter("operacao");
        if (operacao == null) {
            response.setContentType("text/html");
            response.sendDisableCache();
            response.sendTextFile("gestao-miniaturas.form1.static");
        } else {
            Connection c = request.getDatabaseConnection();
            long cdgCliente = request.getLongParameter("cliente_val", true).longValue();
            String nomeCliente = Entities.getClientes(c).verCliente(cdgCliente).getString(1);
            if (operacao.equals("form")) {
                DateFormat df = c.getDatabase().getDriver().getDateFormat();
                Table t = c.select("data FROM gestao_miniaturas WHERE cdg_cliente=" + cdgCliente);
                String data = t.isEmpty() ? new Date(2004, 01, 01).format(df) : t.getString(1, 0);
                TemplateValues tv = new TemplateValues();
                tv.put("cliente", nomeCliente);
                tv.put("cliente_val", cdgCliente);
                tv.put("data", data);
                tv.put("hoje", new Moment().getDate().format(df));
                response.setContentType("text/html");
                response.sendDisableCache();
                response.sendTemplate("gestao-miniaturas.form2.tplt", tv);
            } else if (operacao.equals("zip")) {
                String data = request.getParameter("data");
                Table t = c.select("referencia, data_abertura FROM servicos WHERE cdg_cliente=" + cdgCliente + " AND data_abertura > '" + data + "' AND estado!='a'");
                if (t.isEmpty()) {
                    showAviso(response, "N�O H� MINIATURAS", Tools.toHTML("N�o existem servi�os efectuados por ") + "<strong>" + nomeCliente + "</strong>" + Tools.toHTML(" desde " + data + "."), false);
                } else {
                    response.setContentType("application/zip");
                    response.setHeader("Content-Disposition", "attachment; filename=\"Miniaturas-" + nomeCliente + ".zip\"");
                    ZipOutputStream out = new ZipOutputStream(response.getOutputStream());
                    for (int i = 0; i < t.getRowCount(); i++) {
                        Referencia ref = new Referencia(t.getDate(2, i), t.getInt(1, i));
                        File miniatura = new File(getServletContext().getRealPath("/Thumbnails/" + ref.toString() + ".jpg"));
                        if (miniatura.exists()) {
                            ZipEntry entry = new ZipEntry(miniatura.getName());
                            entry.setTime(miniatura.lastModified());
                            out.putNextEntry(entry);
                            FileInputStream in = new FileInputStream(miniatura);
                            Streams.copy(in, out);
                            out.closeEntry();
                            out.flush();
                        }
                    }
                    out.close();
                }
            } else {
                throw new IntegrityException(operacao);
            }
            c.close();
        }
    }

    protected void doPost(EbsRequest request, EbsResponse response) throws IOException, DatabaseException {
        long cdgCliente = request.getLongParameter("cliente_val", true).longValue();
        Connection c = request.getDatabaseConnection();
        Entities.getMiniaturas(c).marcar(cdgCliente);
        c.close();
        response.sendRedirect("gestao-miniaturas");
    }
}
