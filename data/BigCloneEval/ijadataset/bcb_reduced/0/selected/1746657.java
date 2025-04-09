package com.tetratech.edas2.wqx.exporter;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import com.tetratech.edas2.model.Activity;
import com.tetratech.edas2.model.ActivityProjectAssociation;
import com.tetratech.edas2.model.MonitoringLocation;
import com.tetratech.edas2.model.Organization;
import com.tetratech.edas2.model.Project;
import com.tetratech.edas2.model.Result;
import com.tetratech.edas2.model.ResultDetectionQuantitationLimit;
import com.tetratech.edas2.model.ResultFrequencyClass;
import com.tetratech.edas2.model.ResultTaxonDetail;
import com.tetratech.edas2.model.ResultTaxonFeedingGroup;
import com.tetratech.edas2.model.ResultTaxonHabit;

/**
 * The Class WQXExporter.
 */
public class WQXExporter {

    /** The Constant ZIP_COMPRESSION_LEVEL contains the compression level used in archiving a file. */
    private static final int ZIP_COMPRESSION_LEVEL = 9;

    /** The Constant EXPORT_TYPE_XML. */
    public static final String EXPORT_TYPE_XML = ".xml";

    /** The Constant EXPORT_TYPE_ZIP. */
    public static final String EXPORT_TYPE_ZIP = ".zip";

    /** The emf. */
    private EntityManagerFactory emf = null;

    /** The em. */
    private EntityManager em = null;

    /** The xml. */
    private XMLStreamWriter xml = null;

    /** The param. */
    private WQXExporterParameters param = null;

    /**
	 * Instantiates a new wQX exporter.
	 */
    public WQXExporter() {
    }

    /**
	 * Export stream.
	 * 
	 * @param exportType the export type
	 * @param param the param
 	 * @param _em the entity manager, if null one will be created
	 * 
	 * @return the output stream
	 * 
	 * @throws Exception the exception
	 */
    public ByteArrayOutputStream exportStream(String exportType, WQXExporterParameters param, EntityManager _em) throws Exception {
        ByteArrayOutputStream baos = null;
        ZipOutputStream zipStream = null;
        try {
            this.param = param;
            if (_em == null) {
                emf = Persistence.createEntityManagerFactory("edas2");
                em = emf.createEntityManager();
            } else {
                em = _em;
            }
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            baos = new ByteArrayOutputStream();
            if (exportType.equals(EXPORT_TYPE_ZIP)) {
                zipStream = new ZipOutputStream(baos);
                xml = factory.createXMLStreamWriter(zipStream);
                zipStream.setMethod(ZipOutputStream.DEFLATED);
                zipStream.setLevel(ZIP_COMPRESSION_LEVEL);
                ZipEntry zipEntry = new ZipEntry("edas2wqx.xml");
                zipStream.putNextEntry(zipEntry);
            } else {
                xml = factory.createXMLStreamWriter(baos);
            }
            createDocument();
            xml.flush();
            if (exportType.equals(EXPORT_TYPE_ZIP)) zipStream.flush();
        } finally {
            if (exportType.equals(EXPORT_TYPE_ZIP)) try {
                zipStream.closeEntry();
            } catch (Exception e) {
            }
            try {
                xml.close();
            } catch (Exception e) {
            }
            if (exportType.equals(EXPORT_TYPE_ZIP)) try {
                zipStream.close();
            } catch (Exception e) {
            }
            try {
                baos.close();
            } catch (Exception e) {
            }
            if (_em == null) {
                try {
                    em.clear();
                } catch (Exception e) {
                }
                try {
                    emf.close();
                } catch (Exception e) {
                }
            }
        }
        return baos;
    }

    /**
	 * Export file.
	 * 
	 * @param path the path
	 * @param fileName the file name (without extension)
	 * @param exportType the export type
	 * @param param the param
	 * @param _em the entity manager, if null one will be created
	 * 
	 * @throws Exception the exception
	 */
    public void exportFile(String path, String fileName, String exportType, WQXExporterParameters param, EntityManager _em) throws Exception {
        FileOutputStream fos = null;
        ZipOutputStream zipStream = null;
        try {
            this.param = param;
            if (_em == null) {
                emf = Persistence.createEntityManagerFactory("edas2");
                em = emf.createEntityManager();
            } else {
                em = _em;
            }
            XMLOutputFactory factory = XMLOutputFactory.newInstance();
            fos = new FileOutputStream(path + fileName + exportType);
            if (exportType.equals(EXPORT_TYPE_ZIP)) {
                zipStream = new ZipOutputStream(fos);
                xml = factory.createXMLStreamWriter(zipStream);
                zipStream.setMethod(ZipOutputStream.DEFLATED);
                zipStream.setLevel(ZIP_COMPRESSION_LEVEL);
                ZipEntry zipEntry = new ZipEntry(path + fileName + ".xml");
                zipStream.putNextEntry(zipEntry);
            } else {
                xml = factory.createXMLStreamWriter(fos);
            }
            createDocument();
            xml.flush();
            if (exportType.equals(EXPORT_TYPE_ZIP)) zipStream.flush();
        } finally {
            if (exportType.equals(EXPORT_TYPE_ZIP)) try {
                zipStream.closeEntry();
            } catch (Exception e) {
            }
            try {
                xml.close();
            } catch (Exception e) {
            }
            if (exportType.equals(EXPORT_TYPE_ZIP)) try {
                zipStream.close();
            } catch (Exception e) {
            }
            try {
                fos.close();
            } catch (Exception e) {
            }
            if (_em == null) {
                try {
                    em.clear();
                } catch (Exception e) {
                }
                try {
                    emf.close();
                } catch (Exception e) {
                }
            }
        }
    }

    /**
	 * Write full element without attributes.
	 * 
	 * @param localName the local name
	 * @param content the content
	 */
    private void writeFullElement(String localName, String content) throws Exception {
        if ((content != null) && (!content.equals(""))) {
            xml.writeStartElement(localName);
            xml.writeCharacters(content);
            xml.writeEndElement();
        }
    }

    /**
	 * Creates the document.
	 * 
	 * @throws Exception the exception
	 */
    private void createDocument() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        xml.writeStartDocument();
        xml.writeStartElement("Document");
        xml.writeAttribute("Id", "ID001");
        xml.writeStartElement("Header");
        writeFullElement("Author", "EDAS2");
        writeFullElement("Organization", "Tetra Tech");
        writeFullElement("Title", "WQX");
        writeFullElement("CreationTime", sdf.format(new Date()));
        writeFullElement("Comment", "This is an EDAS2 WQX export file.");
        writeFullElement("ContactInfo", "EDAS2 edas2@tetratech.com");
        xml.writeEndElement();
        xml.writeStartElement("Payload");
        xml.writeAttribute("Operation", "Update-Insert");
        xml.writeStartElement("WQX");
        xml.writeNamespace("xmlns", "http://www.exchangenetwork.net/schema/wqx/2");
        xml.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        xml.writeAttribute("xsi:schemaLocation", "http://www.exchangenetwork.net/schema/wqx/2 http://www.exchangenetwork.net/schema/wqx/2/0/WQX_WQX_v2.0.xsd");
        writeOrganizationXML();
        xml.writeEndElement();
        xml.writeEndElement();
        xml.writeEndElement();
        xml.writeEndDocument();
    }

    /**
	 * Writes the organization xml.
	 * 
	 * @throws Exception the exception
	 */
    private void writeOrganizationXML() throws Exception {
        List<Organization> orgs = em.createQuery("select o from Organization o where o.uid = :uid").setParameter("uid", param.getOrganizationId()).getResultList();
        for (Iterator<Organization> i = orgs.iterator(); i.hasNext(); ) {
            Organization o = i.next();
            xml.writeStartElement("Organization");
            xml.writeStartElement("OrganizationDescription");
            writeFullElement("OrganizationIdentifier", o.getId());
            writeFullElement("OrganizationFormalName", o.getName());
            writeFullElement("OrganizationDescriptionText", o.getDescription());
            xml.writeEndElement();
            writeProjectXML(o.getUid());
            writeMonitoringLocationXML(o.getUid());
            writeActivityXML(o.getUid());
            xml.writeEndElement();
        }
    }

    /**
	 * Write project xml.
	 * 
	 * @param orgId the org id
	 * 
	 * @throws Exception the exception
	 */
    private void writeProjectXML(Long orgId) throws Exception {
        List<Project> prjs = em.createQuery("select p from Project p where p.organization.uid = :uid").setParameter("uid", orgId).getResultList();
        for (Iterator<Project> i = prjs.iterator(); i.hasNext(); ) {
            Project p = i.next();
            xml.writeStartElement("Project");
            writeFullElement("ProjectIdentifier", p.getId());
            writeFullElement("ProjectName", p.getName());
            writeFullElement("ProjectDescriptionText", p.getDescription());
            if (p.getQAPPApproved() != null) writeFullElement("QAPPApprovedIndicator", p.getQAPPApproved().booleanValue() ? "true" : "false");
            writeFullElement("QAPPApprovalAgencyName", p.getQAPPApprovalAgencyName());
            xml.writeEndElement();
        }
    }

    /**
	 * Write monitoring location xml.
	 * 
	 * @param orgId the org id
	 * 
	 * @throws Exception the exception
	 */
    private void writeMonitoringLocationXML(Long orgId) throws Exception {
        List<MonitoringLocation> prjs = em.createQuery("select m from MonitoringLocation m " + "join fetch m.type " + "join fetch m.horizontalReferenceDatum " + "join fetch m.horizontalCollectionMethod " + "left join fetch m.country " + "left join fetch m.state " + "left join fetch m.county " + "where m.organization.uid = :uid").setParameter("uid", orgId).getResultList();
        for (Iterator<MonitoringLocation> i = prjs.iterator(); i.hasNext(); ) {
            MonitoringLocation m = i.next();
            if ((m.getWqx20Valid() != null) && (!m.getWqx20Valid().booleanValue()) && (param.getExportValidOnly() != null) && (param.getExportValidOnly().booleanValue())) continue;
            xml.writeStartElement("MonitoringLocation");
            xml.writeStartElement("MonitoringLocationIdentity");
            writeFullElement("MonitoringLocationIdentifier", m.getId());
            writeFullElement("MonitoringLocationName", m.getName());
            writeFullElement("MonitoringLocationTypeName", m.getType().getName());
            writeFullElement("MonitoringLocationDescriptionText", (m.getDescription() != null) ? m.getDescription() : null);
            writeFullElement("HUCEightDigitCode", (m.getHuc8() != null) ? m.getHuc8() : null);
            writeFullElement("HUCTwelveDigitCode", (m.getHuc12() != null) ? m.getHuc12() : null);
            xml.writeEndElement();
            xml.writeStartElement("MonitoringLocationGeospatial");
            writeFullElement("LatitudeMeasure", m.getLatitude().toPlainString());
            writeFullElement("LongitudeMeasure", m.getLongitude().toPlainString());
            writeFullElement("SourceMapScaleNumeric", (m.getSourceMapScale() != null) ? m.getSourceMapScale().toString() : null);
            writeFullElement("HorizontalCollectionMethodName", m.getHorizontalCollectionMethod().getName());
            writeFullElement("HorizontalCoordinateReferenceSystemDatumName", m.getHorizontalReferenceDatum().getName());
            if (m.getCountry() != null) writeFullElement("CountryCode", m.getCountry().getAbbreviation());
            if (m.getState() != null) writeFullElement("StateCode", m.getState().getAbbreviation());
            if (m.getCounty() != null) writeFullElement("CountyCode", m.getCounty().getFipsCode());
            xml.writeEndElement();
            xml.writeEndElement();
        }
    }

    /**
	 * Write activity xml.
	 * 
	 * @param orgId the org id
	 * 
	 * @throws Exception the exception
	 */
    private void writeActivityXML(Long orgId) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        List<Activity> acts = em.createQuery("select a from Activity a " + "join fetch a.media " + "join fetch a.type " + "left join fetch a.monitoringLocation " + "left join fetch a.assemblage " + "left join fetch a.samplingCollectionEquipment " + "left join fetch a.mediaSubdivision " + "left join fetch a.horizontalReferenceDatum " + "left join fetch a.horizontalCollectionMethod " + "left join fetch a.relativeDepth " + "left join fetch a.elevation.unit " + "left join fetch a.upperElevation.unit " + "left join fetch a.lowerElevation.unit " + "where a.organization.uid = :uid").setParameter("uid", orgId).getResultList();
        for (Iterator<Activity> i = acts.iterator(); i.hasNext(); ) {
            Activity a = i.next();
            if ((a.getWqx20Valid() != null) && (!a.getWqx20Valid().booleanValue()) && (param.getExportValidOnly() != null) && (param.getExportValidOnly().booleanValue())) continue;
            a.setGetTrueValues(true);
            xml.writeStartElement("Activity");
            xml.writeStartElement("ActivityDescription");
            writeFullElement("ActivityIdentifier", a.getId());
            writeFullElement("ActivityTypeCode", a.getType().getCode());
            writeFullElement("ActivityMediaName", a.getMedia().getName());
            writeFullElement("ActivityMediaSubDivisionName", (a.getMediaSubdivision() != null) ? a.getMediaSubdivision().getName() : null);
            writeFullElement("ActivityStartDate", sdf.format(a.getStartDate()));
            writeFullElement("ActivityEndDate", (a.getEndDate() != null) ? sdf.format(a.getEndDate()) : null);
            if (a.getRelativeDepth() != null) writeFullElement("ActivityRelativeDepthName", a.getRelativeDepth().getName());
            if (a.getElevation() != null) {
                xml.writeStartElement("ActivityDepthHeightMeasure");
                writeFullElement("MeasureValue", a.getElevation().getValue());
                if (a.getElevation().getUnit() != null) writeFullElement("MeasureUnitCode", a.getElevation().getUnit().getCode());
                xml.writeEndElement();
            }
            if (a.getUpperElevation() != null) {
                xml.writeStartElement("ActivityTopDepthHeightMeasure");
                writeFullElement("MeasureValue", a.getUpperElevation().getValue());
                if (a.getUpperElevation().getUnit() != null) writeFullElement("MeasureUnitCode", a.getUpperElevation().getUnit().getCode());
                xml.writeEndElement();
            }
            if (a.getLowerElevation() != null) {
                xml.writeStartElement("ActivityBottomDepthHeightMeasure");
                writeFullElement("MeasureValue", a.getLowerElevation().getValue());
                if (a.getLowerElevation().getUnit() != null) writeFullElement("MeasureUnitCode", a.getLowerElevation().getUnit().getCode());
                xml.writeEndElement();
            }
            writeActivityProjectXML(a.getUid());
            if (a.getMonitoringLocation() != null) writeFullElement("MonitoringLocationIdentifier", a.getMonitoringLocation().getId());
            writeFullElement("ActivityCommentText", (a.getComments() != null) ? a.getComments() : null);
            xml.writeEndElement();
            if ((a.getLatitude() != null) || (a.getLongitude() != null) || (a.getSourceMapScale() != null) || (a.getHorizontalCollectionMethod() != null) || (a.getHorizontalReferenceDatum() != null)) {
                xml.writeStartElement("ActivityLocation");
                writeFullElement("LatitudeMeasure", (a.getLatitude() != null) ? a.getLatitude().toPlainString() : null);
                writeFullElement("LongitudeMeasure", (a.getLongitude() != null) ? a.getLongitude().toPlainString() : null);
                writeFullElement("SourceMapScaleNumeric", (a.getSourceMapScale() != null) ? a.getSourceMapScale().toString() : null);
                writeFullElement("HorizontalCollectionMethodName", (a.getHorizontalCollectionMethod() != null) ? a.getHorizontalCollectionMethod().getName() : null);
                writeFullElement("HorizontalCoordinateReferenceSystemDatumName", (a.getHorizontalReferenceDatum() != null) ? a.getHorizontalReferenceDatum().getName() : null);
                xml.writeEndElement();
            }
            if (a.getAssemblage() != null) {
                xml.writeStartElement("BiologicalActivityDescription");
                writeFullElement("AssemblageSampledName", a.getAssemblage().getName());
                xml.writeEndElement();
            }
            if (((a.getCollectionMethod() != null) && ((a.getCollectionMethod().getId() != null) || (a.getCollectionMethod().getContext() != null) || (a.getCollectionMethod().getName() != null))) || (a.getSamplingCollectionEquipment() != null)) {
                xml.writeStartElement("SampleDescription");
                if (a.getCollectionMethod() != null) {
                    xml.writeStartElement("SampleCollectionMethod");
                    writeFullElement("MethodIdentifier", a.getCollectionMethod().getId());
                    writeFullElement("MethodIdentifierContext", a.getCollectionMethod().getContext());
                    writeFullElement("MethodName", a.getCollectionMethod().getName());
                    xml.writeEndElement();
                }
                if (a.getSamplingCollectionEquipment() != null) {
                    writeFullElement("SampleCollectionEquipmentName", a.getSamplingCollectionEquipment().getName());
                }
                xml.writeEndElement();
            }
            writeResultXML(a.getUid());
            xml.writeEndElement();
        }
    }

    /**
	 * Write activity project xml.
	 * 
	 * @param actId the act id
	 * 
	 * @throws Exception the exception
	 */
    private void writeActivityProjectXML(Long actId) throws Exception {
        List<ActivityProjectAssociation> aps = em.createQuery("select ap from ActivityProjectAssociation ap " + "join fetch ap.project " + "where ap.activity.uid = :uid").setParameter("uid", actId).getResultList();
        for (Iterator<ActivityProjectAssociation> i = aps.iterator(); i.hasNext(); ) {
            ActivityProjectAssociation ap = i.next();
            writeFullElement("ProjectIdentifier", ap.getProject().getId());
        }
    }

    /**
	 * Write result xml.
	 * 
	 * @param actId the act id
	 * 
	 * @throws Exception the exception
	 */
    private void writeResultXML(Long actId) throws Exception {
        List<Result> res = em.createQuery("select r from Result r " + "left join fetch r.characteristic " + "left join fetch r.sampleFraction " + "left join fetch r.primaryMeasurement.unit " + "left join fetch r.status " + "left join fetch r.valueType " + "left join fetch r.biologicalIntent " + "left join fetch r.taxon " + "left join fetch r.analyticalMethod " + "left join fetch r.analyticalMethod.context " + "left join fetch r.measurementQualifier " + "left join fetch r.detectionCondition " + "left join fetch r.elevation.unit " + "left join fetch r.weightBasis " + "left join fetch r.sampleTissueAnatomy " + "where r.activity.uid = :uid").setParameter("uid", actId).getResultList();
        for (Iterator<Result> i = res.iterator(); i.hasNext(); ) {
            Result r = i.next();
            if ((r.getWqx20Valid() != null) && (!r.getWqx20Valid().booleanValue()) && (param.getExportValidOnly() != null) && (param.getExportValidOnly().booleanValue())) continue;
            r.setGetTrueValues(true);
            xml.writeStartElement("Result");
            if ((r.getDetectionCondition() != null) || (r.getCharacteristic() != null) || (r.getSampleFraction() != null) || ((r.getPrimaryMeasurement() != null) && ((r.getPrimaryMeasurement().getValue() != null) || (r.getPrimaryMeasurement().getUnit() != null))) || (r.getMeasurementQualifier() != null) || (r.getStatus() != null) || (r.getValueType() != null) || (r.getWeightBasis() != null) || (r.getComments() != null) || ((r.getElevation() != null) && ((r.getElevation().getValue() != null) || (r.getElevation().getUnit() != null))) || (r.getMeasurementPrecision() != null) || (r.getMeasurementBias() != null) || (r.getMeasurementConfidenceInterval() != null) || (r.getMeasurementLowerConfidenceLimit() != null) || (r.getMeasurementUpperConfidenceLimit() != null)) {
                xml.writeStartElement("ResultDescription");
                if (r.getDetectionCondition() != null) writeFullElement("ResultDetectionConditionText", r.getDetectionCondition().getName());
                if (r.getCharacteristic() != null) writeFullElement("CharacteristicName", r.getCharacteristic().getName());
                if (r.getSampleFraction() != null) writeFullElement("ResultSampleFractionText", r.getSampleFraction().getName());
                if (((r.getPrimaryMeasurement() != null) && ((r.getPrimaryMeasurement().getValue() != null) || (r.getPrimaryMeasurement().getUnit() != null))) || (r.getMeasurementQualifier() != null)) {
                    xml.writeStartElement("ResultMeasure");
                    if (r.getPrimaryMeasurement() != null) {
                        writeFullElement("ResultMeasureValue", r.getPrimaryMeasurement().getValue());
                        if (r.getPrimaryMeasurement().getUnit() != null) writeFullElement("MeasureUnitCode", r.getPrimaryMeasurement().getUnit().getCode());
                    }
                    writeFullElement("MeasureQualifierCode", (r.getMeasurementQualifier() != null) ? r.getMeasurementQualifier().getCode() : null);
                    xml.writeEndElement();
                }
                if (r.getStatus() != null) writeFullElement("ResultStatusIdentifier", r.getStatus().getName());
                if (r.getValueType() != null) writeFullElement("ResultValueTypeName", r.getValueType().getName());
                if (r.getWeightBasis() != null) writeFullElement("ResultWeightBasisText", r.getWeightBasis().getName());
                if ((r.getMeasurementPrecision() != null) || (r.getMeasurementBias() != null) || (r.getMeasurementConfidenceInterval() != null) || (r.getMeasurementLowerConfidenceLimit() != null) || (r.getMeasurementUpperConfidenceLimit() != null)) {
                    xml.writeStartElement("DataQuality");
                    writeFullElement("PrecisionValue", r.getMeasurementPrecision());
                    writeFullElement("BiasValue", r.getMeasurementBias());
                    writeFullElement("ConfidenceIntervalValue", r.getMeasurementConfidenceInterval());
                    writeFullElement("UpperConfidenceLimitValue", r.getMeasurementUpperConfidenceLimit());
                    writeFullElement("LowerConfidenceLimitValue", r.getMeasurementLowerConfidenceLimit());
                    xml.writeEndElement();
                }
                writeFullElement("ResultCommentText", r.getComments());
                if (((r.getElevation() != null) && ((r.getElevation().getValue() != null) || (r.getElevation().getUnit() != null)))) {
                    xml.writeStartElement("ResultDepthHeightMeasure");
                    writeFullElement("MeasureValue", r.getElevation().getValue());
                    if (r.getElevation().getUnit() != null) writeFullElement("MeasureUnitCode", r.getElevation().getUnit().getCode());
                    xml.writeEndElement();
                }
                xml.writeEndElement();
            }
            List<ResultFrequencyClass> rfcs = getResultFrequencyClasses(r.getOrganization().getUid(), r.getUid());
            List<ResultTaxonDetail> rtds = getTaxonomicDetails(r.getOrganization().getUid(), r.getUid());
            if ((r.getBiologicalIntent() != null) || (r.getBioIndividualId() != null) || (r.getTaxon() != null) || (r.getSpeciesId() != null) || (r.getSampleTissueAnatomy() != null) || (rtds != null) || (rfcs != null)) {
                xml.writeStartElement("BiologicalResultDescription");
                if (r.getBiologicalIntent() != null) writeFullElement("BiologicalIntentName", r.getBiologicalIntent().getName());
                writeFullElement("BiologicalIndividualIdentifier", r.getBioIndividualId());
                if (r.getTaxon() != null) writeFullElement("SubjectTaxonomicName", r.getTaxon().getName());
                writeFullElement("UnidentifiedSpeciesIdentifier", r.getSpeciesId());
                if (r.getSampleTissueAnatomy() != null) writeFullElement("SampleTissueAnatomyName", r.getSampleTissueAnatomy().getName());
                if (rtds != null) {
                    Iterator<ResultTaxonDetail> i2 = rtds.iterator();
                    xml.writeStartElement("TaxonomicDetails");
                    while (i2.hasNext()) {
                        ResultTaxonDetail rtd = i2.next();
                        writeHabitXML(r.getOrganization().getUid(), r.getUid());
                        writeFullElement("TaxonomicPollutionTolerance", rtd.getPollutionTolerance());
                        writeFullElement("TaxonomicPollutionToleranceScaleText", rtd.getPollutionToleranceScale());
                        writeFullElement("TrophicLevelName", rtd.getTrophicLevel());
                        writeFunctionalFeedingGroupXML(r.getOrganization().getUid(), r.getUid());
                    }
                    xml.writeEndElement();
                }
                if (rfcs != null) {
                    Iterator<ResultFrequencyClass> i2 = rfcs.iterator();
                    xml.writeStartElement("FrequencyClassInformation");
                    while (i2.hasNext()) {
                        ResultFrequencyClass rfc = i2.next();
                        writeFullElement("FrequencyClassDescriptorCode", rfc.getFrequencyClassDescriptor().getName());
                        if (rfc.getBoundsUnit() != null) writeFullElement("FrequencyClassDescriptorUnitCode", rfc.getBoundsUnit().getCode());
                        writeFullElement("LowerClassBoundValue", (rfc.getLowerBound() != null) ? rfc.getLowerBound().toString() : null);
                        writeFullElement("UpperClassBoundValue", (rfc.getUpperBound() != null) ? rfc.getUpperBound().toString() : null);
                    }
                    xml.writeEndElement();
                }
                xml.writeEndElement();
            }
            if (r.getAnalyticalMethod() != null) {
                xml.writeStartElement("ResultAnalyticalMethod");
                writeFullElement("MethodIdentifier", r.getAnalyticalMethod().getId());
                writeFullElement("MethodIdentifierContext", r.getAnalyticalMethod().getContext().getCode());
                writeFullElement("MethodName", r.getAnalyticalMethod().getName());
                xml.writeEndElement();
            }
            List<ResultDetectionQuantitationLimit> rdqls = getResultDetectionQuantitationLimits(r.getOrganization().getUid(), r.getUid());
            if ((rdqls != null) || (false)) {
                xml.writeStartElement("ResultLabInformation");
                if (rdqls != null) {
                    Iterator<ResultDetectionQuantitationLimit> i2 = rdqls.iterator();
                    while (i2.hasNext()) {
                        ResultDetectionQuantitationLimit rdq = i2.next();
                        rdq.setGetTrueValues(true);
                        xml.writeStartElement("ResultDetectionQuantitationLimit");
                        if (rdq.getType() != null) writeFullElement("DetectionQuantitationLimitTypeName", rdq.getType().getName());
                        if ((rdq.getMeasurement() != null) && ((rdq.getMeasurement().getValue() != null) || (rdq.getMeasurement().getUnit() != null))) {
                            xml.writeStartElement("DetectionQuantitationLimitMeasure");
                            writeFullElement("MeasureValue", rdq.getMeasurement().getValue());
                            if (rdq.getMeasurement().getUnit() != null) writeFullElement("MeasureUnitCode", rdq.getMeasurement().getUnit().getCode());
                            xml.writeEndElement();
                        }
                        xml.writeEndElement();
                    }
                }
                xml.writeEndElement();
            }
            xml.writeEndElement();
        }
    }

    /**
	 * Gets the result frequency classes.
	 * 
	 * @param orgId the org id
	 * @param resId the res id
	 * 
	 * @return the result frequency classes
	 * 
	 * @throws Exception the exception
	 */
    private List<ResultFrequencyClass> getResultFrequencyClasses(Long orgId, Long resId) throws Exception {
        List<ResultFrequencyClass> rfcs = em.createQuery("select rfc from ResultFrequencyClass rfc " + "join fetch rfc.frequencyClassDescriptor " + "left join fetch rfc.boundsUnit " + "where rfc.result.uid = :uid and rfc.organization.uid = :ouid").setParameter("uid", resId).setParameter("ouid", orgId).getResultList();
        if ((rfcs != null) && (!rfcs.isEmpty())) return rfcs; else return null;
    }

    /**
	 * Gets the taxonomic details.
	 * 
	 * @param orgId the org id
	 * @param resId the res id
	 * 
	 * @return the taxonomic details
	 * 
	 * @throws Exception the exception
	 */
    private List<ResultTaxonDetail> getTaxonomicDetails(Long orgId, Long resId) throws Exception {
        List<ResultTaxonDetail> rtds = em.createQuery("select rtd from ResultTaxonDetail rtd " + "where rtd.uid = :uid and rtd.organization.uid = :ouid").setParameter("uid", resId).setParameter("ouid", orgId).getResultList();
        if ((rtds != null) && (!rtds.isEmpty())) return rtds; else return null;
    }

    /**
	 * Write habit xml.
	 * 
	 * @param orgId the org id
	 * @param resId the res id
	 * 
	 * @throws Exception the exception
	 */
    private void writeHabitXML(Long orgId, Long resId) throws Exception {
        List<ResultTaxonHabit> rths = em.createQuery("select rth from ResultTaxonHabit rth " + "join fetch rth.habit " + "where rth.id.resUid = :uid and rth.organization.uid = :ouid").setParameter("uid", resId).setParameter("ouid", orgId).getResultList();
        Iterator<ResultTaxonHabit> i = rths.iterator();
        while (i.hasNext()) {
            ResultTaxonHabit rth = i.next();
            writeFullElement("HabitName", rth.getHabit().getName());
        }
    }

    /**
	 * Write functional feeding group xml.
	 * 
	 * @param orgId the org id
	 * @param resId the res id
	 * 
	 * @throws Exception the exception
	 */
    private void writeFunctionalFeedingGroupXML(Long orgId, Long resId) throws Exception {
        List<ResultTaxonFeedingGroup> rtfgs = em.createQuery("select rtfg from ResultTaxonFeedingGroup rtfg " + "where rtfg.id.resUid = :uid and rtfg.organization.uid = :ouid").setParameter("uid", resId).setParameter("ouid", orgId).getResultList();
        Iterator<ResultTaxonFeedingGroup> i = rtfgs.iterator();
        while (i.hasNext()) {
            ResultTaxonFeedingGroup rtfg = i.next();
            writeFullElement("FunctionalFeedingGroupName", rtfg.getId().getFeedingGroup());
        }
    }

    /**
	 * Gets the result detection quantitation limits.
	 * 
	 * @param orgId the org id
	 * @param resId the res id
	 * 
	 * @return the result detection quantitation limits
	 * 
	 * @throws Exception the exception
	 */
    private List<ResultDetectionQuantitationLimit> getResultDetectionQuantitationLimits(Long orgId, Long resId) throws Exception {
        List<ResultDetectionQuantitationLimit> rdqls = em.createQuery("select rdql from ResultDetectionQuantitationLimit rdql " + "left join fetch rdql.type " + "left join fetch rdql.measurement.unit " + "where rdql.result.uid = :uid and rdql.organization.uid = :ouid").setParameter("uid", resId).setParameter("ouid", orgId).getResultList();
        if ((rdqls != null) && (!rdqls.isEmpty())) return rdqls; else return null;
    }

    public static void main(String[] args) throws Exception {
        WQXExporterParameters p = new WQXExporterParameters();
        p.setOrganizationId(new Long(32));
        p.setExportValidOnly(new Boolean(false));
        WQXExporter e = new WQXExporter();
        e.exportFile("", "test32", EXPORT_TYPE_XML, p, null);
    }
}
