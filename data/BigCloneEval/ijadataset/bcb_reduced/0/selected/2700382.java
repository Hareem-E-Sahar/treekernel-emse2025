package com.fddtool.si.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import com.fddtool.pd.account.Person;
import com.fddtool.pd.account.Role;
import com.fddtool.pd.document.Document;
import com.fddtool.pd.fddproject.Activity;
import com.fddtool.pd.fddproject.Feature;
import com.fddtool.pd.fddproject.FeatureMilestoneProgress;
import com.fddtool.pd.fddproject.FeatureProgress;
import com.fddtool.pd.fddproject.IProgressInfo;
import com.fddtool.pd.fddproject.MilestoneDescription;
import com.fddtool.pd.fddproject.MilestoneSetDescription;
import com.fddtool.pd.fddproject.Project;
import com.fddtool.pd.fddproject.ProjectGroup;
import com.fddtool.pd.fddproject.ProjectMember;
import com.fddtool.pd.fddproject.ProjectAspect;
import com.fddtool.pd.fddproject.SourceCodeAssignment;
import com.fddtool.pd.fddproject.SourceCodeUnit;
import com.fddtool.pd.fddproject.SubjectArea;
import com.fddtool.pd.fddproject.Workpackage;
import com.fddtool.pd.report.ReportData;
import com.fddtool.pd.task.Task;
import com.fddtool.util.Utils;

/**
 * This class exports the projects according to FDDI specication with FDDPMA
 * extensions. It produces the zip-file, which contains the following entries:
 * <ul>
 * <li> XML export file </li>
 * <li> XSD schema for FDDI </li>
 * <li> XSD schema for FDDPMA </li>
 * <li> Binary attachment files for the xported projects </li>
 * </ul>
 * 
 * @author skhramtc
 */
public class ProjectExporter {

    /**
     * The project to be exported.
     */
    private List<Project> projects = new ArrayList<Project>();

    /**
     * List of documents included into output file.
     */
    private List<Document> savedDocuments = new ArrayList<Document>();

    /**
     * Creates a new exporter for the project.
     * 
     * @param project
     *            the Project to be exported.
     */
    public ProjectExporter(Project project) {
        Utils.assertNotNullParam(project, "project");
        projects.add(project);
    }

    /**
     * Creates a new exporter for the number of projects.
     * 
     * @param projects
     *            the List of project to be exported.
     */
    public ProjectExporter(List<Project> projects) {
        Utils.assertNotNullParam(projects, "projects");
        this.projects.addAll(projects);
    }

    /**
     * Creates the DOM document containing that will be used to produce the XML
     * export file.
     * 
     * @param out
     *            the ZipOutputStream where the exported projects are to be
     *            stored.
     * 
     * @return org.jdom.Document object.
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private org.jdom.Document createProjectXml(ZipOutputStream out) throws IOException {
        Element program = exportProgram(out);
        org.jdom.Document doc = new org.jdom.Document(program);
        return doc;
    }

    /**
     * Creates an fddi:program element that contains imformation about all the
     * exported projects.
     * 
     * @param out
     *            the ZipOutputStream where the exported projects are to be
     *            stored.
     * 
     * @return the Element object that complies with fddi:program definition.
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportProgram(ZipOutputStream out) throws IOException {
        Element program = new Element(FDDI.PROGRAM, FDDI.NAMESPACE);
        program.addNamespaceDeclaration(FDDI.NAMESPACE);
        program.addNamespaceDeclaration(FDDPMA.NAMESPACE);
        program.addNamespaceDeclaration(XSI.NAMESPACE);
        program.setAttribute("schemaLocation", FDDI.SCHEMA + " " + FDDI.SCHEMA_FILE + " " + FDDPMA.SCHEMA + " " + FDDPMA.SCHEMA_FILE, XSI.NAMESPACE);
        Element name = new Element(FDDI.NAME);
        name.setText(ProjectGroup.getRootProjectGroup().getName());
        program.addContent(name);
        Iterator<Project> iter = projects.iterator();
        while (iter.hasNext()) {
            Project project = iter.next();
            Element projectElement = exportProject(out, project);
            program.addContent(projectElement);
        }
        Iterator<Document> docs = savedDocuments.iterator();
        while (docs.hasNext()) {
            Document doc = docs.next();
            Element docElement = exportDocument(out, doc);
            program.addContent(docElement);
        }
        return program;
    }

    /**
     * Creates a DOM element that contains information about a single exported
     * project.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * 
     * @param project
     *            the Project to be exported.
     * 
     * @return the Element object that complies with fddi:project definition.
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportProject(ZipOutputStream out, Project project) throws IOException {
        Element projectElement = new Element(FDDI.PROJECT, FDDI.NAMESPACE);
        projectElement.setAttribute(FDDPMA.TIME_ZONE, "" + project.getTimeZone(), FDDPMA.NAMESPACE);
        Element name = new Element(FDDI.NAME);
        name.setText(project.getName());
        projectElement.addContent(name);
        Iterator<ProjectAspect> aspects = project.listAspects().iterator();
        while (aspects.hasNext()) {
            ProjectAspect aspect = aspects.next();
            Element aspectElement = exportAspect(out, aspect);
            projectElement.addContent(aspectElement);
        }
        Element projectType = new Element(FDDPMA.PROJECT_TYPE, FDDPMA.NAMESPACE);
        projectType.setText(project.getProjectType().getName());
        projectElement.addContent(projectType);
        Iterator<SourceCodeUnit> codeUnits = project.listSourceCodeUnits(null).iterator();
        while (codeUnits.hasNext()) {
            SourceCodeUnit unit = codeUnits.next();
            Element unitElement = exportSourceCodeUnit(out, unit);
            projectElement.addContent(unitElement);
        }
        Iterator<ProjectMember> members = project.listMembers().iterator();
        while (members.hasNext()) {
            ProjectMember member = members.next();
            Element memberElement = exportProjectMember(out, member);
            projectElement.addContent(memberElement);
        }
        Iterator<ReportData> reports = ReportData.listReportData(project).iterator();
        while (reports.hasNext()) {
            ReportData report = reports.next();
            Element reportElement = exportReport(out, report);
            projectElement.addContent(reportElement);
        }
        return projectElement;
    }

    /**
     * Creates a DOM element that contains the information about a project
     * member.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param member
     *            teh ProjectMember to be exported
     * 
     * @return Element object that complies with fddpma:projectMember
     *         definition.
     */
    private Element exportProjectMember(ZipOutputStream out, ProjectMember member) {
        Element result = new Element(FDDPMA.PROJECT_MEMBER, FDDPMA.NAMESPACE);
        Element username = new Element(FDDPMA.USERNAME);
        username.setText(member.getPerson().getUserName());
        result.addContent(username);
        Element email = new Element(FDDPMA.EMAIL);
        email.setText(member.getPerson().getDisplayEmail());
        result.addContent(email);
        Iterator<Role> roles = member.listRoles().iterator();
        while (roles.hasNext()) {
            Role role = roles.next();
            Element roleElement = exportRole(out, role);
            result.addContent(roleElement);
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a role.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param role
     *            the Role to be exported.
     * 
     * @return Element object that complies with fddpma:role definition.
     */
    private Element exportRole(ZipOutputStream out, Role role) {
        Element result = new Element(FDDPMA.ROLE, FDDPMA.NAMESPACE);
        Element name = new Element(FDDI.NAME);
        name.setText(role.getId());
        result.addContent(name);
        return result;
    }

    /**
     * Creates a DOM element that contains information about source code unit.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param unit
     *            the SourceCodeUnit to be exported.
     * 
     * @return Element object that complies with fddpma:codeUnit definition
     */
    private Element exportSourceCodeUnit(ZipOutputStream out, SourceCodeUnit unit) {
        Element result = new Element(FDDPMA.CODE_UNIT, FDDPMA.NAMESPACE);
        result.setAttribute(FDDPMA.CODE_UNIT_ID, unit.getId());
        Element name = new Element(FDDI.NAME);
        name.setText(unit.getName());
        result.addContent(name);
        if (unit.getOwner() != null) {
            Element owner = exportOwnerRef(out, unit.getOwner());
            result.addContent(owner);
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a aspect (aspect)
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param aspect
     *            the ProjectAspect to be exported.
     * 
     * @return Element object that complies with fddi:aspect definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportAspect(ZipOutputStream out, ProjectAspect aspect) throws IOException {
        Element result = new Element(FDDI.ASPECT, FDDI.NAMESPACE);
        Element name = new Element(FDDI.NAME);
        name.setText(aspect.getAspectType().getName());
        result.addContent(name);
        String aspectWeight = "" + aspect.getAspectType().getWeight();
        result.setAttribute(FDDPMA.WEIGHT, aspectWeight, FDDPMA.NAMESPACE);
        if (!aspect.getAspectType().isDevelopment()) {
            String percent = "" + (int) aspect.getPercentComplete();
            result.setAttribute(FDDPMA.COMPLETION, percent, FDDPMA.NAMESPACE);
        }
        if (aspect.getPlannedStart() != null) {
            String date = XSI.format(aspect.getPlannedStart());
            result.setAttribute(FDDPMA.PLANNED_START, date, FDDPMA.NAMESPACE);
        }
        if (aspect.getPlannedEnd() != null) {
            String date = XSI.format(aspect.getPlannedEnd());
            result.setAttribute(FDDPMA.PLANNED_END, date, FDDPMA.NAMESPACE);
        }
        if (aspect.getActualStart() != null) {
            String date = XSI.format(aspect.getActualStart());
            result.setAttribute(FDDPMA.ACTUAL_START, date, FDDPMA.NAMESPACE);
        }
        if (aspect.getAspectType().isDevelopment()) {
            Element info = exportAspectInfo(out, aspect);
            result.addContent(info);
            Iterator<SubjectArea> areas = aspect.listSubjectAreas().iterator();
            while (areas.hasNext()) {
                SubjectArea area = areas.next();
                Element subject = exportSubjectArea(out, area);
                result.addContent(subject);
            }
            Iterator<Workpackage> workpackages = aspect.listWorkpackages().iterator();
            while (workpackages.hasNext()) {
                Workpackage w = workpackages.next();
                Element workpackageElement = exportWorkpackage(out, w);
                result.addContent(workpackageElement);
            }
        } else {
            Iterator<Task> tasks = aspect.listTasks().iterator();
            while (tasks.hasNext()) {
                Task task = tasks.next();
                Element taskElement = exportTask(out, task);
                result.addContent(taskElement);
            }
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a task
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param task
     *            the Task to exported.
     * @return Element object that complies with fddpma:task definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportTask(ZipOutputStream out, Task task) throws IOException {
        Element result = new Element(FDDPMA.TASK, FDDPMA.NAMESPACE);
        result.setAttribute(FDDPMA.WEIGHT, "" + task.getWeight());
        if (task.getPlannedStart() != null) {
            String date = XSI.format(task.getPlannedStart());
            result.setAttribute(FDDPMA.PLANNED_START, date);
        }
        if (task.getPlannedEnd() != null) {
            String date = XSI.format(task.getPlannedEnd());
            result.setAttribute(FDDPMA.PLANNED_END, date);
        }
        if (task.getActualStart() != null) {
            String date = XSI.format(task.getActualStart());
            result.setAttribute(FDDPMA.ACTUAL_START, date);
        }
        int completion = (int) task.getPercentComplete();
        result.setAttribute(FDDPMA.COMPLETION, "" + completion);
        String cancelled = Boolean.toString(task.isCancelled());
        result.setAttribute(FDDPMA.CANCELLED, cancelled);
        Element name = new Element(FDDI.NAME);
        name.setText(task.getName());
        result.addContent(name);
        if (!Utils.isEmpty(task.getDescription())) {
            Element remarks = new Element(FDDPMA.REMARKS);
            remarks.setText(task.getDescription());
            result.addContent(remarks);
        }
        if (task.getOwner() != null) {
            Element owner = exportOwnerRef(out, task.getOwner());
            result.addContent(owner);
        }
        Iterator<Document> docs = Document.find(task).iterator();
        while (docs.hasNext()) {
            Document doc = docs.next();
            Element docElement = exportDocumentRef(out, doc);
            result.addContent(docElement);
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a workpackage
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param w
     *            the Workpackage to be exported.
     * 
     * @return Element object that complies with fddpma:workpackage definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportWorkpackage(ZipOutputStream out, Workpackage w) throws IOException {
        Element result = new Element(FDDPMA.WORKPACKAGE, FDDPMA.NAMESPACE);
        result.setAttribute(FDDPMA.WORKPACKAGE_ID, w.getId());
        Element name = new Element(FDDI.NAME);
        name.setText(w.getName());
        result.addContent(name);
        if (!Utils.isEmpty(w.getDescription())) {
            Element remarks = new Element(FDDPMA.REMARKS);
            remarks.setText(w.getDescription());
            result.addContent(remarks);
        }
        if (w.getOwner() != null) {
            Element owner = exportOwnerRef(out, w.getOwner());
            result.addContent(owner);
        }
        Iterator<SourceCodeAssignment> codeAssignments = w.listSourceCodeAssignments().iterator();
        while (codeAssignments.hasNext()) {
            SourceCodeAssignment a = codeAssignments.next();
            Element assignmentElement = exportSourceCodeAssignment(out, a);
            result.addContent(assignmentElement);
        }
        Iterator<Document> docs = w.listAttachedDocuments().iterator();
        while (docs.hasNext()) {
            Document doc = docs.next();
            Element docElement = exportDocumentRef(out, doc);
            result.addContent(docElement);
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a source code unit
     * assigned to a workpackage.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param assignment
     *            the SourceCodeAssignment to be exported.
     * 
     * @return Element object that complies with fddpma:codeAssignment
     *         definition
     */
    private Element exportSourceCodeAssignment(ZipOutputStream out, SourceCodeAssignment assignment) {
        Element result = new Element(FDDPMA.CODE_ASSIGNMENT, FDDPMA.NAMESPACE);
        result.setAttribute(FDDPMA.CODE_UNIT_ID, assignment.getCodeUnit().getId());
        if (!Utils.isEmpty(assignment.getComment())) {
            Element remarks = new Element(FDDPMA.REMARKS);
            remarks.setText(assignment.getComment());
            result.addContent(remarks);
        }
        if (assignment.getOverrideOwner() != null) {
            Element owner = exportOwnerRef(out, assignment.getOverrideOwner());
            result.addContent(owner);
        }
        return result;
    }

    /**
     * Creates a DOM element that contains information about a subject area.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param area
     *            the SubjectArea to be exported.
     * 
     * @return Element object that complies with fddi:subject definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportSubjectArea(ZipOutputStream out, SubjectArea area) throws IOException {
        Element subjectArea = new Element(FDDI.SUBJECT);
        Element prefix = new Element(FDDI.SUBJECT_PREFIX);
        prefix.setText(area.getPrefix());
        subjectArea.addContent(prefix);
        Element name = new Element(FDDI.NAME);
        name.setText(area.getName());
        subjectArea.addContent(name);
        Iterator<Activity> iter = area.listActivities().iterator();
        while (iter.hasNext()) {
            Activity activity = iter.next();
            Element activityElement = exportActivity(out, activity);
            subjectArea.addContent(activityElement);
        }
        return subjectArea;
    }

    /**
     * Creates a DOM element that contains information about an activity.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param activity
     *            the Activity to be exported
     * 
     * @return Element object that complies with fddi:activity definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportActivity(ZipOutputStream out, Activity activity) throws IOException {
        Element activityElement = new Element(FDDI.ACTIVITY);
        Element name = new Element(FDDI.NAME);
        name.setText(activity.getName());
        activityElement.addContent(name);
        if (activity.getOwner() != null) {
            Element initials = new Element(FDDI.INITIALS);
            initials.setText(activity.getOwner().getInitials());
            activityElement.addContent(initials);
        }
        if (activity.getTargetDate() != null) {
            String date = XSI.format(activity.getTargetDate());
            activityElement.setAttribute(FDDPMA.PLANNED_END, date, FDDPMA.NAMESPACE);
        }
        Iterator<Feature> iter = activity.listFeatures().iterator();
        while (iter.hasNext()) {
            Feature feature = iter.next();
            Element featureElement = exportFeature(out, feature);
            activityElement.addContent(featureElement);
        }
        if (activity.getOwner() != null) {
            Element owner = exportOwnerRef(out, activity.getOwner());
            activityElement.addContent(owner);
        }
        return activityElement;
    }

    /**
     * Creates a DOM element that contains information about a a feature.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param feature
     *            the Feature to be exported.
     * 
     * @return Element object that complies with fddi:feature definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     */
    private Element exportFeature(ZipOutputStream out, Feature feature) throws IOException {
        Element featureElement = new Element(FDDI.FEATURE);
        featureElement.setAttribute(FDDI.SEQ, "" + feature.getSequence());
        if (feature.getWorkpackage() != null) {
            featureElement.setAttribute(FDDPMA.WORKPACKAGE_ID, "" + feature.getWorkpackage().getId(), FDDPMA.NAMESPACE);
        }
        featureElement.setAttribute(FDDPMA.CANCELLED, Boolean.toString(feature.isCancelled()), FDDPMA.NAMESPACE);
        Element name = new Element(FDDI.NAME);
        name.setText(feature.getName());
        featureElement.addContent(name);
        if (feature.getOwner() != null) {
            Element initials = new Element(FDDI.INITIALS);
            initials.setText(feature.getOwner().getInitials());
            featureElement.addContent(initials);
        }
        FeatureProgress progress = feature.findFeatureProgress();
        if (!progress.isEmpty()) {
            Iterator<FeatureMilestoneProgress> iter = progress.listMilestones().iterator();
            while (iter.hasNext()) {
                FeatureMilestoneProgress mp = iter.next();
                if (!mp.isEmpty()) {
                    Element milestoneElement = exportMilestoneProgress(out, mp);
                    featureElement.addContent(milestoneElement);
                }
            }
        }
        if (!Utils.isEmpty(feature.getDescription())) {
            Element remarks = new Element(FDDI.REMARKS);
            remarks.setText(feature.getDescription());
            featureElement.addContent(remarks);
        }
        if (feature.getOwner() != null) {
            Element owner = exportOwnerRef(out, feature.getOwner());
            featureElement.addContent(owner);
        }
        if (!Utils.isEmpty(feature.getReasonBehindSchedule())) {
            Element reason = new Element(FDDPMA.REASON_BEHIND, FDDPMA.NAMESPACE);
            reason.setText(feature.getReasonBehindSchedule());
            featureElement.addContent(reason);
        }
        Iterator<Document> documents = Document.find(feature).iterator();
        while (documents.hasNext()) {
            Document doc = documents.next();
            Element docElement = exportDocumentRef(out, doc);
            featureElement.addContent(docElement);
        }
        return featureElement;
    }

    /**
     * Creates a DOM element that contains reference to the exported document.
     * This method also adds the binary content of the document to the out zip
     * stream.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param doc
     *            the Document to be exported.
     * 
     * @return Element object that complies with fddpma:documentRef definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     * 
     * @see addDocumentToZip(ZipOutputStream, Document);
     * @see exportDocument(ZipOutputStream, Document);
     */
    private Element exportDocumentRef(ZipOutputStream out, Document doc) throws IOException {
        Element docElement = new Element(FDDPMA.DOCUMENT_REF, FDDPMA.NAMESPACE);
        docElement.setAttribute(FDDPMA.DOCUMENT_ID, doc.getId());
        addDocumentToZip(out, doc);
        return docElement;
    }

    /**
     * Creates a DOM element that contains information about a document. This
     * method only exports description of the document, but not its binary
     * content. Since the documents may be shared across the projects, the
     * binary content will be exported after all the documents for all the
     * projects are discovered.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param doc
     *            the Document to be exported.
     * 
     * @return Element object that complies with fddpma:document definition
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     * 
     * @see addDocumentToZip(ZipOutputStream, Document);
     * @see exportDocumentRef(ZipOutputStream, Document);
     */
    private Element exportDocument(ZipOutputStream out, Document doc) throws IOException {
        Element docElement = new Element(FDDPMA.DOCUMENT, FDDPMA.NAMESPACE);
        docElement.setAttribute(FDDPMA.DOCUMENT_ID, doc.getId());
        Element name = new Element(FDDI.NAME);
        name.setText(doc.getName());
        docElement.addContent(name);
        Element contentType = new Element(FDDPMA.CONTENT_TYPE);
        contentType.setText(doc.getContentType());
        docElement.addContent(contentType);
        return docElement;
    }

    /**
     * Creates a DOM element that contains information about a report. 
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param report
     *            the ReportData to be exported.
     * 
     * @return Element object that complies with fddpma:report definition
     * @throws IOException 
     * 
     * @throws IOException
     *             if information cannot be stored into the output stream.
     * 
     * @see addReportToZip(ZipOutputStream, ReportData);
     */
    private Element exportReport(ZipOutputStream out, ReportData report) throws IOException {
        Element reportElement = new Element(FDDPMA.REPORT, FDDPMA.NAMESPACE);
        reportElement.setAttribute(FDDPMA.REPORT_ID, report.getId());
        reportElement.setAttribute(FDDPMA.REPORT_TYPE, report.getReportType().getId());
        reportElement.setAttribute(FDDPMA.REPORT_DATE, XSI.format(report.getDateCollected()));
        addReportToZip(out, report);
        return reportElement;
    }

    /**
     * Returns FDDI status string for given progress information.
     * 
     * @param progress
     *            the IProgressInfo implementation that contains progress
     *            information.
     * 
     * @return the String containg one of FDDI progress enumeration values. See
     *         FDDI specification, statusEnum element definition.
     */
    private String getFddiStatus(IProgressInfo progress) {
        if (progress.isCompleted()) {
            return FDDI.COMPLETE;
        } else if (progress.isLate()) {
            return FDDI.ATTENTION;
        } else if (progress.isInProgress()) {
            return FDDI.UNDERWAY;
        } else {
            return FDDI.INACTIVE;
        }
    }

    /**
     * Creates a DOM element that contains information feature milestone
     * progress.
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param mp
     *            the FeatureMilestoneProgress to be exported.
     * 
     * @return Element object that complies with fddi:milestone definition
     */
    private Element exportMilestoneProgress(ZipOutputStream out, FeatureMilestoneProgress mp) {
        Element milestoneProgress = new Element(FDDI.MILESTONE);
        Date planned = mp.getPlannedCompletion();
        if (planned == null) {
            planned = mp.getActualCompletion();
        }
        milestoneProgress.setAttribute(FDDI.PLANNED, XSI.format(planned));
        if (mp.getActualCompletion() != null) {
            Date actual = mp.getActualCompletion();
            milestoneProgress.setAttribute(FDDI.ACTUAL, XSI.format(actual));
        }
        String status = getFddiStatus(mp);
        milestoneProgress.setAttribute(FDDI.STATUS, status);
        return milestoneProgress;
    }

    /**
     * Creates a DOM element that contains reference information pointing to a
     * person.
     * 
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param owner
     *            the Person whose reference to be created.
     * 
     * @return Element object that complies with fddpma:ownerRef definition
     * 
     * @see exportProjectMember(ZipOutputStream, ProjectMember)
     */
    private Element exportOwnerRef(ZipOutputStream out, Person owner) {
        Element ownerElement = new Element(FDDPMA.OWNER_REF, FDDPMA.NAMESPACE);
        Element username = new Element(FDDPMA.USERNAME);
        username.setText(owner.getUserName());
        ownerElement.addContent(username);
        return ownerElement;
    }

    /**
     * Creates a DOM element that contains reference information for aspect.
     * 
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param aspect
     *            the ProjectAspect whose reference information to be exported.
     * 
     * @return Element object that complies with fddi:info definition
     * 
     * @see exportAspect(ZipOutputStream, ProjectAspect)
     */
    private Element exportAspectInfo(ZipOutputStream out, ProjectAspect aspect) {
        if (aspect.getAspectType().isDevelopment()) {
            Element info = new Element(FDDI.INFO);
            Element subjectName = new Element(FDDI.SUBJECT_NAME);
            subjectName.setText(FDDPMA.SUBJECT_AREA);
            info.addContent(subjectName);
            Element activityName = new Element(FDDI.ACTIVITY_NAME);
            activityName.setText(FDDPMA.BUSINESS_ACTIVITY);
            info.addContent(activityName);
            Element featureName = new Element(FDDI.FEATURE_NAME);
            featureName.setText(FDDPMA.FEATURE);
            info.addContent(featureName);
            Element milestoneName = new Element(FDDI.MILESTONE_NAME);
            milestoneName.setText(FDDPMA.MILESTONE);
            info.addContent(milestoneName);
            MilestoneSetDescription milestones = aspect.getAspectType().getMilestones();
            Iterator<MilestoneDescription> iter = milestones.listMilestones().iterator();
            while (iter.hasNext()) {
                MilestoneDescription md = iter.next();
                Element milestoneInfo = exportMilestoneInfo(out, md);
                info.addContent(milestoneInfo);
            }
            Element milsetoneSetName = new Element(FDDPMA.MILESTONE_SET, FDDPMA.NAMESPACE);
            milsetoneSetName.setText(milestones.getName());
            info.addContent(milsetoneSetName);
            return info;
        }
        return null;
    }

    /**
     * Creates a DOM element that contains information for a milestone
     * definition
     * 
     * @param out
     *            the ZipOutputStream where the export information is to be
     *            stored.
     * @param md
     *            the MilestoneDescription to be exported.
     * 
     * @return Element object that complies with fddi:milestoneInfo definition
     */
    private Element exportMilestoneInfo(ZipOutputStream out, MilestoneDescription md) {
        Element milestoneInfo = new Element(FDDI.MILESTONE_INFO);
        Element name = new Element(FDDI.NAME);
        name.setText(md.getName());
        milestoneInfo.addContent(name);
        milestoneInfo.setAttribute(FDDI.EFFORT, "" + md.getEffort());
        return milestoneInfo;
    }

    /**
     * Exports the project(s) specified in the constrcor into zipped data.
     * 
     * @return array of bytes that contains exported zipped data.
     * 
     * @throws IOException
     *             if error happens while during export operation.
     */
    public byte[] export() throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(buf);
        org.jdom.Document doc = createProjectXml(out);
        addResourceToZip(out, FDDI.SCHEMA_FILE);
        addResourceToZip(out, FDDPMA.SCHEMA_FILE);
        addProjectXmlToZip(out, doc);
        out.flush();
        out.close();
        return buf.toByteArray();
    }

    /**
     * Creates a new entry in the zip output stream and writes conetent of the
     * document into it. The new entryt in the zip stream will have name
     * "document_XXX" where XXX is the document id.
     * 
     * @param out
     *            the ZipOutputStream where the document content is to be
     *            stored.
     * @param doc
     *            the Document to be exported.
     * 
     * @throws IOException
     *             if error happens while writing document into the output
     *             stream.
     * 
     * @see exportDocument(ZipOutputStream, Document)
     */
    private void addDocumentToZip(ZipOutputStream out, Document doc) throws IOException {
        if (!savedDocuments.contains(doc)) {
            ZipEntry entry = new ZipEntry(FDDPMA.DOCUMENT_FILE_PREFIX + doc.getId());
            out.putNextEntry(entry);
            out.write(doc.getContent());
            savedDocuments.add(doc);
        }
    }

    /**
     * Creates a new entry in the zip output stream and writes conetent of the
     * document into it. The new entryt in the zip stream will have name
     * "document_XXX" where XXX is the document id.
     * 
     * @param out
     *            the ZipOutputStream where the document content is to be
     *            stored.
     * @param doc
     *            the Document to be exported.
     * 
     * @throws IOException
     *             if error happens while writing document into the output
     *             stream.
     * 
     * @see exportDocument(ZipOutputStream, Document)
     */
    private void addReportToZip(ZipOutputStream out, ReportData report) throws IOException {
        ZipEntry entry = new ZipEntry(FDDPMA.REPORT_FILE_PREFIX + report.getId());
        out.putNextEntry(entry);
        out.write(report.getXmlData().getBytes());
    }

    /**
     * Adds the named resource into the zip file. The resource in question must
     * be in teh same location as this class. This method creates a new entry in
     * the zip stream and writes the content of the resource into it.
     * 
     * @param out
     *            the ZipOutputStream where the resource is to be stored.
     * 
     * @param resourceName
     *            the Name of the resource to store. This also will serve as a
     *            name of entry in the zip stream.
     * 
     * @throws IOException
     *             if error happens while writing resource into the output
     *             stream.
     */
    private void addResourceToZip(ZipOutputStream out, String resourceName) throws IOException {
        ZipEntry entry = new ZipEntry(resourceName);
        out.putNextEntry(entry);
        String className = getClass().getName();
        String fileName = className.replaceAll("\\x2E", "/");
        fileName = fileName.substring(0, fileName.lastIndexOf("/"));
        fileName = fileName + "/" + resourceName;
        InputStream in = getResourceAsStream(fileName);
        byte[] tmp = new byte[1024];
        int len = in.read(tmp);
        while (len > 0) {
            out.write(tmp, 0, len);
            len = in.read(tmp);
        }
    }

    /**
     * Creates an XML text containg the export information and adds it to the
     * zip stream.
     * 
     * @param out
     *            the ZipOutputStream where the XML to be stored.
     * @param doc
     *            the DOM document to converted into XML text.
     * 
     * @throws IOException
     *             if error happens while storing XML into the output stream.
     */
    private void addProjectXmlToZip(ZipOutputStream out, org.jdom.Document doc) throws IOException {
        ZipEntry entry = new ZipEntry(FDDPMA.PROJECT_ENTRY);
        out.putNextEntry(entry);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        ByteArrayOutputStream xmlBuf = new ByteArrayOutputStream();
        outputter.output(doc, xmlBuf);
        out.write(xmlBuf.toByteArray());
    }

    /**
     * Returns input stream to the named resource with the given full name.
     * 
     * @param name
     *            String containg name of the resourse to open.
     * 
     * @return InputStream or <code>null</code>, if the named resource is not
     *         found.
     */
    protected static InputStream getResourceAsStream(final String name) {
        final ClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<ClassLoader>() {

            public ClassLoader run() {
                return Thread.currentThread().getContextClassLoader();
            }
        });
        return AccessController.doPrivileged(new PrivilegedAction<InputStream>() {

            public InputStream run() {
                if (loader != null) {
                    return loader.getResourceAsStream(name);
                } else {
                    return ClassLoader.getSystemResourceAsStream(name);
                }
            }
        });
    }
}
