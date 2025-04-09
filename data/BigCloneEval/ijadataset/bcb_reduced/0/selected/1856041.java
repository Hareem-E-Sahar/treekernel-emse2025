package org.gbif.portal.web.view;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.gbif.portal.service.DataResourceManager;
import org.gbif.portal.service.GeospatialManager;
import org.gbif.portal.service.TaxonomyManager;
import org.gbif.portal.util.request.TemplateUtils;
import org.springframework.web.servlet.view.AbstractView;

/**
 * A simple Velocity view for using velocity templates to generate a view;
 * 
 * @author dmartin
 */
public class ZippedVelocityView extends AbstractView {

    protected TaxonomyManager taxonomyManager;

    protected DataResourceManager dataResourceManager;

    protected GeospatialManager geospatialManager;

    /** The path of the template to use */
    protected String headerTemplatePath;

    /** The path of the template to use */
    protected String footerTemplatePath;

    /** The path of the template to use */
    protected String templatePath;

    /** results model key */
    protected String resultsModelKey;

    /**
	 * @see org.springframework.web.servlet.view.AbstractView#renderMergedOutputModel(java.util.Map, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
    @Override
    protected void renderMergedOutputModel(Map map, HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; cell-densities.zip");
        OutputStream out = response.getOutputStream();
        ZipOutputStream zout = new ZipOutputStream(out);
        zout.putNextEntry(new ZipEntry("taxon-cell-density.kml"));
        VelocityContext velocityContext = new VelocityContext(map);
        velocityContext.put("hostUrl", request.getHeader("host"));
        Object results = map.get(resultsModelKey);
        writeTemplate(velocityContext, request, headerTemplatePath, zout);
        writeTemplate(velocityContext, request, templatePath, zout);
        writeTemplate(velocityContext, request, footerTemplatePath, zout);
        zout.closeEntry();
        zout.close();
    }

    /**
	 * Write out the supplied template.
	 * 
	 * @param templatePath
	 * @param outputStream
	 * @throws Exception
	 */
    protected void writeTemplate(VelocityContext velocityContext, HttpServletRequest request, String templatePath, OutputStream outputStream) throws Exception {
        if (StringUtils.isNotEmpty(templatePath)) {
            Template template = Velocity.getTemplate(templatePath);
            template.initDocument();
            TemplateUtils tu = new TemplateUtils();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream);
            tu.merge(template, velocityContext, writer);
            writer.flush();
        }
    }

    /**
	 * @param taxonomyManager the taxonomyManager to set
	 */
    public void setTaxonomyManager(TaxonomyManager taxonomyManager) {
        this.taxonomyManager = taxonomyManager;
    }

    /**
	 * @param dataResourceManager the dataResourceManager to set
	 */
    public void setDataResourceManager(DataResourceManager dataResourceManager) {
        this.dataResourceManager = dataResourceManager;
    }

    /**
	 * @param geospatialManager the geospatialManager to set
	 */
    public void setGeospatialManager(GeospatialManager geospatialManager) {
        this.geospatialManager = geospatialManager;
    }

    /**
	 * @param headerTemplatePath the headerTemplatePath to set
	 */
    public void setHeaderTemplatePath(String headerTemplatePath) {
        this.headerTemplatePath = headerTemplatePath;
    }

    /**
	 * @param footerTemplatePath the footerTemplatePath to set
	 */
    public void setFooterTemplatePath(String footerTemplatePath) {
        this.footerTemplatePath = footerTemplatePath;
    }

    /**
	 * @param templatePath the templatePath to set
	 */
    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    /**
	 * @param resultsModelKey the resultsModelKey to set
	 */
    public void setResultsModelKey(String resultsModelKey) {
        this.resultsModelKey = resultsModelKey;
    }
}
