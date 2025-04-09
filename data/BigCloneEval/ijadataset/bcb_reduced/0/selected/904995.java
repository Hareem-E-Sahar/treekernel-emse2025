package br.com.sinapp.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import br.com.caelum.vraptor.Resource;
import br.com.caelum.vraptor.Result;
import br.com.caelum.vraptor.Validator;
import br.com.caelum.vraptor.interceptor.download.Download;
import br.com.caelum.vraptor.interceptor.download.InputStreamDownload;
import br.com.caelum.vraptor.validator.Validations;
import br.com.caelum.vraptor.view.Results;
import br.com.sinapp.dao.PersistenciaLancamentoContabilDao;
import br.com.sinapp.model.LancamentoContabil;

@Resource
public class LancamentoContabilController {

    private Result result;

    private PersistenciaLancamentoContabilDao dao;

    private Validator validator;

    public LancamentoContabilController(PersistenciaLancamentoContabilDao dao, Result result, Validator validator) {
        this.dao = dao;
        this.result = result;
        this.validator = validator;
    }

    public void exibir() {
    }

    public Download gerarArquivo(final Date datIni, final Date datFim) {
        validator.checking(new Validations() {

            {
                that(datIni != null, "lancamentoContabil.datIni", "obrigatorio", i18n("lancamentoContabil.datIni"));
                that(datFim != null, "lancamentoContabil.datFim", "obrigatorio", i18n("lancamentoContabil.datFim"));
            }
        });
        validator.onErrorUse(Results.page()).of(this.getClass()).exibir();
        List<LancamentoContabil> lancamentoList = dao.list(datIni, datFim);
        try {
            File arq = new File("Lancamento.txt");
            PrintWriter pw = new PrintWriter(arq);
            StringBuffer texto;
            for (LancamentoContabil lanc : lancamentoList) {
                texto = new StringBuffer();
                texto.append("\"\",\"");
                texto.append(lanc.getContaDebito() + "\",\"");
                texto.append(lanc.getContaCredito() + "\",\"");
                texto.append(formataData(lanc.getData()) + "\",\"");
                texto.append((lanc.getCredtio() != null ? formataValor(lanc.getCredtio()) : formataValor(lanc.getDebito())) + "\",\"");
                texto.append("\",\"");
                texto.append("\",\"");
                texto.append((lanc.getNomHistorico() == null ? "" : lanc.getNomHistorico()) + "\",\"");
                texto.append(lanc.getBoleto().getNumDocumento() + "\"");
                pw.println(texto.toString());
            }
            pw.close();
            FileInputStream fis = new FileInputStream(arq);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int bytesRead = 0;
            while ((bytesRead = fis.read(buffer, 0, 8192)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            byte[] b = baos.toByteArray();
            InputStream input = new ByteArrayInputStream(b);
            result.include("sucess", "Arquivo Gerado com sucesso!");
            return new InputStreamDownload(input, "application/txt", "Lancamento.txt");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private String formataData(Date data) {
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        return df.format(data);
    }

    private String formataValor(Double valor) {
        if (valor > 0) {
            String val = valor.toString();
            val = val.replace(".", ":");
            String[] listVal = val.split(":");
            while (listVal[1].length() < 2) {
                listVal[1] = listVal[1] + "0";
            }
            return listVal[0] + "," + listVal[1];
        } else return null;
    }
}
