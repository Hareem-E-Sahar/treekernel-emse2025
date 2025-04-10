package com.googlecode.openmpis.action;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.DispatchAction;
import org.apache.struts.upload.FormFile;
import com.googlecode.openmpis.dto.Log;
import com.googlecode.openmpis.dto.Abductor;
import com.googlecode.openmpis.dto.Person;
import com.googlecode.openmpis.dto.User;
import com.googlecode.openmpis.form.AbductorForm;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.AbductorDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.LogDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.dao.impl.PersonDAOImpl;
import com.googlecode.openmpis.persistence.ibatis.service.AbductorService;
import com.googlecode.openmpis.persistence.ibatis.service.LogService;
import com.googlecode.openmpis.persistence.ibatis.service.PersonService;
import com.googlecode.openmpis.persistence.ibatis.service.impl.AbductorServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.LogServiceImpl;
import com.googlecode.openmpis.persistence.ibatis.service.impl.PersonServiceImpl;
import com.googlecode.openmpis.util.Constants;
import com.googlecode.openmpis.util.Validator;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;

/**
 * The AbductorAction class provides the methods to list, add, edit, delete and view
 * persons.
 *
 * @author  <a href="mailto:rvbabilonia@gmail.com">Rey Vincent Babilonia</a>
 * @version 1.0
 */
public class AbductorAction extends DispatchAction {

    /**
     * The abductor service
     */
    private PersonService personService = new PersonServiceImpl(new PersonDAOImpl());

    /**
     * The abductor service
     */
    private AbductorService abductorService = new AbductorServiceImpl(new AbductorDAOImpl());

    /**
     * The log service
     */
    private LogService logService = new LogServiceImpl(new LogDAOImpl());

    /**
     * The file logger
     */
    private Logger logger = Logger.getLogger(this.getClass());

    /**
     * The format for date (e.g. 2009-02-28)
     */
    private SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * Prepares the form for abductor creation.
     * This is the new abductor action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     */
    public ActionForward newAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            List<Abductor> abductorList = abductorService.listAllAbductors();
            request.setAttribute("abductorlist", abductorList);
            request.setAttribute("action", request.getParameter("action"));
            AbductorForm abductorForm = (AbductorForm) form;
            if (request.getAttribute("personid") != null) {
                Person person = personService.getPersonById((Integer) request.getAttribute("personid"));
                if (person != null) {
                    if (person.getAbductorId() != null) {
                        Abductor abductor = abductorService.getAbductorById(person.getAbductorId());
                        if (abductor.getPhoto() != null) {
                            abductorForm.setPhoto(abductor.getPhoto());
                        }
                        if (abductor.getAgedPhoto() != null) {
                            abductorForm.setAgedPhoto(abductor.getAgedPhoto());
                        }
                        abductorForm.setId(abductor.getId());
                        abductorForm.setPersonId(person.getId());
                        abductorForm.setFirstName(abductor.getFirstName());
                        abductorForm.setNickname(abductor.getNickname());
                        abductorForm.setMiddleName(abductor.getMiddleName());
                        abductorForm.setLastName(abductor.getLastName());
                        abductorForm.setBirthMonth(abductor.getBirthMonth());
                        abductorForm.setBirthDay(abductor.getBirthDay());
                        abductorForm.setBirthYear(abductor.getBirthYear());
                        if (abductor.getBirthMonth() != 0) {
                            abductorForm.setKnownBirthDate(true);
                        }
                        abductorForm.setAge(getAge(abductor.getBirthMonth() - 1, abductor.getBirthDay(), abductor.getBirthYear()));
                        abductorForm.setStreet(abductor.getStreet());
                        abductorForm.setCity(abductor.getCity());
                        abductorForm.setProvince(abductor.getProvince());
                        abductorForm.setCountry(abductor.getCountry());
                        abductorForm.setSex(abductor.getSex());
                        abductorForm.setFeet(abductor.getFeet());
                        abductorForm.setInches(abductor.getInches());
                        abductorForm.setWeight(abductor.getWeight());
                        abductorForm.setReligion(abductor.getReligion());
                        abductorForm.setRace(abductor.getRace());
                        abductorForm.setEyeColor(abductor.getEyeColor());
                        abductorForm.setHairColor(abductor.getHairColor());
                        abductorForm.setMarks(abductor.getMarks());
                        abductorForm.setPersonalEffects(abductor.getPersonalEffects());
                        abductorForm.setRemarks(abductor.getRemarks());
                        if (abductor.getCodisId() != null) {
                            abductorForm.setCodisId(abductor.getCodisId());
                        }
                        if (abductor.getAfisId() != null) {
                            abductorForm.setAfisId(abductor.getAfisId());
                        }
                        if (abductor.getDentalId() != null) {
                            abductorForm.setDentalId(abductor.getDentalId());
                        }
                        abductorForm.setRelationToAbductor(person.getRelationToAbductor());
                    } else {
                        abductorForm.setId(0);
                        abductorForm.setPhoto("");
                        abductorForm.setAgedPhoto("");
                        abductorForm.setFirstName("");
                        abductorForm.setNickname("");
                        abductorForm.setMiddleName("");
                        abductorForm.setLastName("");
                        abductorForm.setKnownBirthDate(false);
                        abductorForm.setStreet("");
                        abductorForm.setCity("All");
                        abductorForm.setProvince("All");
                        abductorForm.setCountry("Philippines");
                        abductorForm.setSex(0);
                        abductorForm.setFeet(0);
                        abductorForm.setInches(0);
                        abductorForm.setWeight(0);
                        abductorForm.setReligion(0);
                        abductorForm.setRace(0);
                        abductorForm.setEyeColor(0);
                        abductorForm.setHairColor(0);
                        abductorForm.setMarks("");
                        abductorForm.setPersonalEffects("");
                        abductorForm.setRemarks("");
                        abductorForm.setCodisId("");
                        abductorForm.setAfisId("");
                        abductorForm.setDentalId("");
                    }
                }
            } else {
                try {
                    abductorForm.setPersonId(Integer.parseInt(request.getParameter("personid")));
                } catch (NumberFormatException nfe) {
                    return mapping.findForward(Constants.LIST_PERSON);
                } catch (NullPointerException npe) {
                    return mapping.findForward(Constants.LIST_PERSON);
                }
            }
            return mapping.findForward(Constants.ADD_ABDUCTOR);
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Inserts an abductor into the database.
     * This is the add abductor action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward addAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            AbductorForm abductorForm = (AbductorForm) form;
            ActionMessages errors = new ActionMessages();
            List<Abductor> abductorList = abductorService.listAllAbductors();
            request.setAttribute("abductorlist", abductorList);
            if (abductorForm.getId() > 0) {
                Person person = new Person();
                person.setId(abductorForm.getPersonId());
                person.setAbductorId(abductorForm.getId());
                person.setRelationToAbductor(abductorForm.getRelationToAbductor());
                personService.updatePersonAbductor(person);
                Log addLog = new Log();
                addLog.setLog("Abductor " + abductorForm.getId() + " was attributed to person " + person.getId() + ".");
                addLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                logService.insertLog(addLog);
                logger.info(addLog.toString());
                person = personService.getPersonById(abductorForm.getPersonId());
                request.setAttribute("personid", person.getId());
                return mapping.findForward(Constants.SELECT_INVESTIGATOR);
            } else {
                request.setAttribute("action", request.getParameter("action"));
                if (isValidAbductor(request, form)) {
                    Abductor checker = new Abductor();
                    String firstName = abductorForm.getFirstName();
                    String lastName = abductorForm.getLastName();
                    checker.setId(abductorForm.getId());
                    checker.setFirstName(firstName);
                    checker.setLastName(lastName);
                    if (abductorService.isUniqueAbductor(checker)) {
                        Abductor abductor = new Abductor();
                        abductor.setFirstName(firstName);
                        abductor.setNickname(abductorForm.getNickname());
                        abductor.setMiddleName(abductorForm.getMiddleName());
                        abductor.setLastName(lastName);
                        if (abductorForm.isKnownBirthDate()) {
                            abductor.setBirthMonth(abductorForm.getBirthMonth());
                            abductor.setBirthDay(abductorForm.getBirthDay());
                            abductor.setBirthYear(abductorForm.getBirthYear());
                        }
                        abductor.setStreet(abductorForm.getStreet());
                        abductor.setCity(abductorForm.getCity());
                        abductor.setProvince(abductorForm.getProvince());
                        abductor.setCountry(abductorForm.getCountry());
                        abductor.setSex(abductorForm.getSex());
                        abductor.setFeet(abductorForm.getFeet());
                        abductor.setInches(abductorForm.getInches());
                        abductor.setWeight(abductorForm.getWeight());
                        abductor.setReligion(abductorForm.getReligion());
                        abductor.setRace(abductorForm.getRace());
                        abductor.setEyeColor(abductorForm.getEyeColor());
                        abductor.setHairColor(abductorForm.getHairColor());
                        abductor.setMarks(abductorForm.getMarks());
                        abductor.setPersonalEffects(abductorForm.getPersonalEffects());
                        abductor.setRemarks(abductorForm.getRemarks());
                        if (!abductorForm.getCodisId().isEmpty()) {
                            abductor.setCodisId(abductorForm.getCodisId());
                        }
                        if (!abductorForm.getAfisId().isEmpty()) {
                            abductor.setAfisId(abductorForm.getAfisId());
                        }
                        if (!abductorForm.getDentalId().isEmpty()) {
                            abductor.setDentalId(abductorForm.getDentalId());
                        }
                        int generatedId = abductorService.insertAbductor(abductor);
                        if (generatedId > 0) {
                            FormFile photoFile = abductorForm.getPhotoFile();
                            FormFile agedPhotoFile = abductorForm.getAgedPhotoFile();
                            String contextUnknownPhotoFilename = "photo/unknown.png";
                            String contextDefaultPhotoFilename = contextUnknownPhotoFilename;
                            String contextAgedPhotoFilename = contextUnknownPhotoFilename;
                            if ((photoFile.getFileName().length() > 0) || (agedPhotoFile.getFileName().length() > 0)) {
                                String tokens[] = photoFile.getFileName().toLowerCase().split("\\.");
                                String extensionName = tokens[1];
                                if (agedPhotoFile.getFileName().length() > 0) {
                                    tokens = agedPhotoFile.getFileName().toLowerCase().split("\\.");
                                    extensionName = tokens[1];
                                }
                                String directoryName = "abductor-" + createDirectoryName(generatedId);
                                int age = getAge(abductorForm.getBirthMonth() - 1, abductorForm.getBirthDay(), abductorForm.getBirthYear());
                                String contextPhotoDirectory = "photo/" + directoryName;
                                String contextDefaultPhotoDirectory = contextPhotoDirectory + "/default";
                                String contextAgedPhotoDirectory = contextPhotoDirectory + "/aged";
                                String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + directoryName;
                                String absoluteDefaultPhotoDirectory = absolutePhotoDirectory + File.separator + "default";
                                String absoluteAgedPhotoDirectory = absolutePhotoDirectory + File.separator + "aged";
                                File photoDirectory = new File(absolutePhotoDirectory);
                                File defaultPhotoDirectory = new File(absoluteDefaultPhotoDirectory);
                                File agedPhotoDirectory = new File(absoluteAgedPhotoDirectory);
                                if (!photoDirectory.exists()) {
                                    photoDirectory.mkdir();
                                    defaultPhotoDirectory.mkdir();
                                    agedPhotoDirectory.mkdir();
                                } else {
                                    if ((!defaultPhotoDirectory.exists()) || (!agedPhotoDirectory.exists())) {
                                        defaultPhotoDirectory.mkdir();
                                        agedPhotoDirectory.mkdir();
                                    }
                                }
                                if (photoFile.getFileName().length() > 0) {
                                    String absoluteDefaultPhotoFilename = absoluteDefaultPhotoDirectory + File.separator + directoryName + "-age-" + age + "." + extensionName;
                                    contextDefaultPhotoFilename = contextDefaultPhotoDirectory + "/" + directoryName + "-age-" + age + "." + extensionName;
                                    File file = new File(absoluteDefaultPhotoFilename);
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(photoFile.getFileData());
                                    fos.close();
                                    fos.flush();
                                }
                                if (agedPhotoFile.getFileName().length() > 0) {
                                    String absoluteAgedPhotoFilename = absoluteAgedPhotoDirectory + File.separator + directoryName + "." + extensionName;
                                    contextAgedPhotoFilename = contextAgedPhotoDirectory + "/" + directoryName + "." + extensionName;
                                    File file = new File(absoluteAgedPhotoFilename);
                                    FileOutputStream fos = new FileOutputStream(file);
                                    fos.write(agedPhotoFile.getFileData());
                                    fos.close();
                                    fos.flush();
                                }
                                abductor.setId(generatedId);
                                abductor.setPhoto(contextDefaultPhotoFilename);
                                if (agedPhotoFile.getFileName().length() > 0) {
                                    abductor.setAgedPhoto(contextAgedPhotoFilename);
                                } else {
                                    abductor.setAgedPhoto(contextUnknownPhotoFilename);
                                }
                                abductorService.updateAbductor(abductor);
                            }
                            Person person = new Person();
                            person.setId(abductorForm.getPersonId());
                            person.setAbductorId(generatedId);
                            person.setRelationToAbductor(abductorForm.getRelationToAbductor());
                            personService.updatePersonAbductor(person);
                            Log addLog = new Log();
                            if ((!firstName.isEmpty()) && (!lastName.isEmpty())) {
                                addLog.setLog(firstName + " '" + abductorForm.getNickname() + "' " + lastName + " was encoded by " + currentUser.getUsername() + ".");
                            } else {
                                addLog.setLog("' " + abductorForm.getNickname() + " '" + " was encoded by " + currentUser.getUsername() + ".");
                            }
                            addLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                            logService.insertLog(addLog);
                            logger.info(addLog.toString());
                            request.setAttribute("personid", abductorForm.getPersonId());
                            return mapping.findForward(Constants.SELECT_INVESTIGATOR);
                        } else {
                            return mapping.findForward(Constants.FAILURE);
                        }
                    } else {
                        errors.add("firstname", new ActionMessage("error.abductor.duplicate"));
                        saveErrors(request, errors);
                        logger.error("Duplicate abductor.");
                        return mapping.findForward(Constants.ADD_ABDUCTOR_REDO);
                    }
                } else {
                    return mapping.findForward(Constants.ADD_ABDUCTOR_REDO);
                }
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Retrieves a abductor from the database.
     * This is the view abductor action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward viewAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        AbductorForm abductorForm = (AbductorForm) form;
        int personId = 0;
        try {
            personId = Integer.parseInt(request.getParameter("personid"));
            Person person = personService.getPersonById(personId);
            abductorForm.setId(person.getAbductorId());
            if (person.getAbductorId() == Integer.parseInt(request.getParameter("id"))) {
                abductorForm.setRelationToAbductor(person.getRelationToAbductor());
            }
        } catch (NumberFormatException nfe) {
        } catch (NullPointerException npe) {
        }
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Abductor abductor = abductorService.getAbductorById(id);
            if (abductor.getPhoto() != null) {
                abductorForm.setPhoto(abductor.getPhoto());
            }
            if (abductor.getAgedPhoto() != null) {
                abductorForm.setAgedPhoto(abductor.getAgedPhoto());
            }
            abductorForm.setId(abductor.getId());
            abductorForm.setPersonId(personId);
            abductorForm.setFirstName(abductor.getFirstName());
            abductorForm.setNickname(abductor.getNickname());
            abductorForm.setMiddleName(abductor.getMiddleName());
            abductorForm.setLastName(abductor.getLastName());
            abductorForm.setBirthMonth(abductor.getBirthMonth());
            abductorForm.setBirthDay(abductor.getBirthDay());
            abductorForm.setBirthYear(abductor.getBirthYear());
            if (abductor.getBirthMonth() != 0) {
                abductorForm.setKnownBirthDate(true);
            }
            abductorForm.setAge(getAge(abductor.getBirthMonth() - 1, abductor.getBirthDay(), abductor.getBirthYear()));
            abductorForm.setStreet(abductor.getStreet());
            abductorForm.setCity(abductor.getCity());
            abductorForm.setProvince(abductor.getProvince());
            abductorForm.setCountry(abductor.getCountry());
            abductorForm.setSex(abductor.getSex());
            abductorForm.setFeet(abductor.getFeet());
            abductorForm.setInches(abductor.getInches());
            abductorForm.setWeight(abductor.getWeight());
            abductorForm.setReligion(abductor.getReligion());
            abductorForm.setRace(abductor.getRace());
            abductorForm.setEyeColor(abductor.getEyeColor());
            abductorForm.setHairColor(abductor.getHairColor());
            abductorForm.setMarks(abductor.getMarks());
            abductorForm.setPersonalEffects(abductor.getPersonalEffects());
            abductorForm.setRemarks(abductor.getRemarks());
            if (abductor.getCodisId() != null) {
                abductorForm.setCodisId(abductor.getCodisId());
            }
            if (abductor.getAfisId() != null) {
                abductorForm.setAfisId(abductor.getAfisId());
            }
            if (abductor.getDentalId() != null) {
                abductorForm.setDentalId(abductor.getDentalId());
            }
            if (request.getSession().getAttribute("currentuser") != null) {
                currentUser = (User) request.getSession().getAttribute("currentuser");
                request.setAttribute("action", request.getParameter("action"));
                if (currentUser.getGroupId() == 1) {
                    List<Abductor> abductorList = abductorService.listAllAbductors();
                    request.setAttribute("abductorlist", abductorList);
                    return mapping.findForward(Constants.EDIT_ABDUCTOR);
                } else {
                    return mapping.findForward(Constants.VIEW_ABDUCTOR);
                }
            } else {
                return mapping.findForward(Constants.VIEW_ABDUCTOR);
            }
        } catch (NumberFormatException nfe) {
            return mapping.findForward(Constants.LIST_PERSON);
        } catch (NullPointerException nfe) {
            return mapping.findForward(Constants.LIST_PERSON);
        }
    }

    /**
     * Updates a abductor.
     * This is the edit abductor action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward editAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if (currentUser.getGroupId() == 1) {
            AbductorForm abductorForm = (AbductorForm) form;
            ActionMessages errors = new ActionMessages();
            request.setAttribute("action", request.getParameter("action"));
            List<Abductor> abductorList = abductorService.listAllAbductors();
            request.setAttribute("abductorlist", abductorList);
            if (isValidAbductor(request, form)) {
                Abductor checker = new Abductor();
                String firstName = abductorForm.getFirstName();
                String lastName = abductorForm.getLastName();
                checker.setId(abductorForm.getId());
                checker.setFirstName(firstName);
                checker.setLastName(lastName);
                if (abductorService.isUniqueAbductor(checker)) {
                    FormFile photoFile = abductorForm.getPhotoFile();
                    FormFile agedPhotoFile = abductorForm.getAgedPhotoFile();
                    String contextUnknownPhotoFilename = "photo/unknown.png";
                    String contextDefaultPhotoFilename = contextUnknownPhotoFilename;
                    String contextAgedPhotoFilename = contextUnknownPhotoFilename;
                    if ((photoFile.getFileName().length() > 0) || (agedPhotoFile.getFileName().length() > 0)) {
                        String tokens[];
                        String extensionName = "";
                        if (photoFile.getFileName().length() > 0) {
                            tokens = photoFile.getFileName().toLowerCase().split("\\.");
                            extensionName = tokens[1];
                        }
                        if (agedPhotoFile.getFileName().length() > 0) {
                            tokens = agedPhotoFile.getFileName().toLowerCase().split("\\.");
                            extensionName = tokens[1];
                        }
                        String directoryName = "abductor-" + createDirectoryName(abductorForm.getId());
                        int age = getAge(abductorForm.getBirthMonth() - 1, abductorForm.getBirthDay(), abductorForm.getBirthYear());
                        String contextPhotoDirectory = "photo/" + directoryName;
                        String contextDefaultPhotoDirectory = contextPhotoDirectory + "/default";
                        String contextAgedPhotoDirectory = contextPhotoDirectory + "/aged";
                        String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + directoryName;
                        String absoluteDefaultPhotoDirectory = absolutePhotoDirectory + File.separator + "default";
                        String absoluteAgedPhotoDirectory = absolutePhotoDirectory + File.separator + "aged";
                        File photoDirectory = new File(absolutePhotoDirectory);
                        File defaultPhotoDirectory = new File(absoluteDefaultPhotoDirectory);
                        File agedPhotoDirectory = new File(absoluteAgedPhotoDirectory);
                        if (!photoDirectory.exists()) {
                            photoDirectory.mkdir();
                            defaultPhotoDirectory.mkdir();
                            agedPhotoDirectory.mkdir();
                        } else {
                            if ((!defaultPhotoDirectory.exists()) || (!agedPhotoDirectory.exists())) {
                                defaultPhotoDirectory.mkdir();
                                agedPhotoDirectory.mkdir();
                            }
                        }
                        String absoluteDefaultPhotoFilename = absoluteDefaultPhotoDirectory + File.separator + directoryName + "-age-" + age + "." + extensionName;
                        contextDefaultPhotoFilename = contextDefaultPhotoDirectory + "/" + directoryName + "-age-" + age + "." + extensionName;
                        File file = new File(absoluteDefaultPhotoFilename);
                        FileOutputStream fos = new FileOutputStream(file);
                        fos.write(photoFile.getFileData());
                        fos.close();
                        fos.flush();
                        if (agedPhotoFile.getFileName().length() > 0) {
                            String absoluteAgedPhotoFilename = absoluteAgedPhotoDirectory + File.separator + directoryName + "." + extensionName;
                            contextAgedPhotoFilename = contextAgedPhotoDirectory + "/" + directoryName + "." + extensionName;
                            file = new File(absoluteAgedPhotoFilename);
                            fos = new FileOutputStream(file);
                            fos.write(photoFile.getFileData());
                            fos.close();
                            fos.flush();
                        }
                    }
                    Abductor abductor = new Abductor();
                    abductor.setId(abductorForm.getId());
                    if (agedPhotoFile.getFileName().length() > 0) {
                        abductor.setPhoto(contextDefaultPhotoFilename);
                    } else {
                        abductor.setPhoto(abductorForm.getPhoto());
                    }
                    if (agedPhotoFile.getFileName().length() > 0) {
                        abductor.setAgedPhoto(contextAgedPhotoFilename);
                    }
                    abductor.setFirstName(firstName);
                    abductor.setNickname(abductorForm.getNickname());
                    abductor.setMiddleName(abductorForm.getMiddleName());
                    abductor.setLastName(lastName);
                    if (abductorForm.isKnownBirthDate()) {
                        abductor.setBirthMonth(abductorForm.getBirthMonth());
                        abductor.setBirthDay(abductorForm.getBirthDay());
                        abductor.setBirthYear(abductorForm.getBirthYear());
                    }
                    abductor.setStreet(abductorForm.getStreet());
                    abductor.setCity(abductorForm.getCity());
                    abductor.setProvince(abductorForm.getProvince());
                    abductor.setCountry(abductorForm.getCountry());
                    abductor.setSex(abductorForm.getSex());
                    abductor.setFeet(abductorForm.getFeet());
                    abductor.setInches(abductorForm.getInches());
                    abductor.setWeight(abductorForm.getWeight());
                    abductor.setReligion(abductorForm.getReligion());
                    abductor.setRace(abductorForm.getRace());
                    abductor.setEyeColor(abductorForm.getEyeColor());
                    abductor.setHairColor(abductorForm.getHairColor());
                    abductor.setMarks(abductorForm.getMarks());
                    abductor.setPersonalEffects(abductorForm.getPersonalEffects());
                    abductor.setRemarks(abductorForm.getRemarks());
                    if (!abductorForm.getCodisId().isEmpty()) {
                        abductor.setCodisId(abductorForm.getCodisId());
                    }
                    if (!abductorForm.getAfisId().isEmpty()) {
                        abductor.setAfisId(abductorForm.getAfisId());
                    }
                    if (!abductorForm.getDentalId().isEmpty()) {
                        abductor.setDentalId(abductorForm.getDentalId());
                    }
                    boolean isUpdated = abductorService.updateAbductor(abductor);
                    abductor = abductorService.getAbductorById(abductor.getId());
                    if (isUpdated) {
                        Person person = new Person();
                        person.setId(abductorForm.getPersonId());
                        person.setAbductorId(abductorForm.getId());
                        person.setRelationToAbductor(abductorForm.getRelationToAbductor());
                        personService.updatePersonAbductor(person);
                        Log editLog = new Log();
                        if ((abductor.getFirstName().equals(abductorForm.getFirstName())) && (abductor.getNickname().equals(abductorForm.getNickname())) && (abductor.getMiddleName().equals(abductorForm.getMiddleName())) && (abductor.getLastName().equals(abductorForm.getLastName()))) {
                            editLog.setLog("Abductor " + abductor.getNickname() + " was updated by " + currentUser.getUsername() + ".");
                        } else {
                            editLog.setLog("Abductor " + abductor.getFirstName() + " '" + abductor.getNickname() + "' " + abductor.getLastName() + " was renamed to " + firstName + " '" + abductorForm.getNickname() + "' " + lastName + " by " + currentUser.getUsername() + ".");
                        }
                        editLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                        logService.insertLog(editLog);
                        logger.info(editLog.toString());
                        request.setAttribute("personid", abductorForm.getPersonId());
                        return mapping.findForward(Constants.SELECT_INVESTIGATOR);
                    } else {
                        return mapping.findForward(Constants.FAILURE);
                    }
                } else {
                    errors.add("firstname", new ActionMessage("error.abductor.duplicate"));
                    saveErrors(request, errors);
                    logger.error("Duplicate abductor.");
                    return mapping.findForward(Constants.ADD_ABDUCTOR_REDO);
                }
            } else {
                return mapping.findForward(Constants.ADD_ABDUCTOR_REDO);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Prepares the form for deleting an abductor.
     * This is the erase abductor action called from the Struts framework.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward eraseAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if ((currentUser.getGroupId() == 0) || (currentUser.getGroupId() == 1)) {
            AbductorForm abductorForm = (AbductorForm) form;
            try {
                Abductor abductor = abductorService.getAbductorById(abductorForm.getId());
                abductorForm.setFirstName(abductor.getFirstName());
                abductorForm.setNickname(abductor.getNickname());
                abductorForm.setLastName(abductor.getLastName());
                abductorForm.setCode((int) (Math.random() * 7777) + 1000);
                if (currentUser.getGroupId() == 1) {
                    request.setAttribute("personcount", personService.countPersonsByAbductorId(abductor.getId()));
                    return mapping.findForward(Constants.DELETE_ABDUCTOR);
                } else {
                    return mapping.findForward(Constants.UNAUTHORIZED);
                }
            } catch (NumberFormatException nfe) {
                return mapping.findForward(Constants.LIST_PERSON);
            } catch (NullPointerException npe) {
                return mapping.findForward(Constants.LIST_PERSON);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Deletes an abductor from the database.
     * This is the delete abductor action called from the HTML form.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward deleteAbductor(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        User currentUser = null;
        if (request.getSession().getAttribute("currentuser") == null) {
            return mapping.findForward(Constants.EXPIRED);
        } else {
            currentUser = (User) request.getSession().getAttribute("currentuser");
        }
        if ((currentUser.getGroupId() == 0) || (currentUser.getGroupId() == 1)) {
            AbductorForm abductorForm = (AbductorForm) form;
            try {
                Abductor abductor = abductorService.getAbductorById(abductorForm.getId());
                if (abductorForm.getCode() == abductorForm.getUserCode()) {
                    if (currentUser.getGroupId() == 1) {
                        abductorService.deleteAbductor(abductor.getId());
                        String absolutePhotoDirectory = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + "abductor-" + createDirectoryName(abductor.getId());
                        File photoDirectory = new File(absolutePhotoDirectory);
                        if (photoDirectory.exists()) {
                            for (File primaryFile : photoDirectory.listFiles()) {
                                if (primaryFile.isDirectory()) {
                                    for (File secondaryFile : primaryFile.listFiles()) {
                                        secondaryFile.delete();
                                    }
                                }
                                primaryFile.delete();
                            }
                            photoDirectory.delete();
                        }
                        Log deleteLog = new Log();
                        deleteLog.setLog("Abductor " + abductor.getFirstName() + " \"" + abductor.getNickname() + "\" " + abductor.getLastName() + " was deleted by " + currentUser.getUsername() + ".");
                        deleteLog.setDate(simpleDateFormat.format(System.currentTimeMillis()));
                        logService.insertLog(deleteLog);
                        logger.info(deleteLog.toString());
                        request.setAttribute("abductor", abductor);
                        request.setAttribute("operation", "delete");
                        return mapping.findForward(Constants.DELETE_ABDUCTOR_SUCCESS);
                    } else {
                        return mapping.findForward(Constants.UNAUTHORIZED);
                    }
                } else {
                    abductorForm.setFirstName(abductor.getFirstName());
                    abductorForm.setNickname(abductor.getNickname());
                    abductorForm.setLastName(abductor.getLastName());
                    abductorForm.setCode((int) (Math.random() * 7777) + 1000);
                    ActionMessages errors = new ActionMessages();
                    errors.add("usercode", new ActionMessage("error.code.mismatch"));
                    saveErrors(request, errors);
                    logger.error("Codes did not match.");
                    return mapping.findForward(Constants.DELETE_ABDUCTOR_REDO);
                }
            } catch (NullPointerException npe) {
                return mapping.findForward(Constants.LIST_PERSON);
            }
        } else {
            return mapping.findForward(Constants.UNAUTHORIZED);
        }
    }

    /**
     * Prints the abductor's poster in PDF file.
     *
     * @param mapping       the ActionMapping used to select this instance
     * @param form          the optional ActionForm bean for this request
     * @param request       the HTTP Request we are processing
     * @param response      the HTTP Response we are processing
     * @return              the forwarding instance
     * @throws java.lang.Exception
     */
    public ActionForward printPoster(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        Document document = new Document(PageSize.LETTER, 50, 50, 50, 50);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);
        try {
            int id = Integer.parseInt(request.getParameter("id"));
            Abductor abductor = abductorService.getAbductorById(id);
            String absoluteDefaultPhotoFilename = getServlet().getServletContext().getRealPath("/") + "photo" + File.separator + "unknown.png";
            if (abductor.getPhoto() != null) {
                String tokens[] = abductor.getPhoto().split("\\/");
                String defaultPhotoBasename = "";
                for (int i = 0; i < tokens.length - 1; i++) {
                    defaultPhotoBasename += tokens[i] + File.separator;
                }
                defaultPhotoBasename += tokens[tokens.length - 1];
                absoluteDefaultPhotoFilename = getServlet().getServletContext().getRealPath("/") + defaultPhotoBasename;
            }
            document.addTitle("Poster");
            document.addAuthor("OpenMPIS");
            document.addSubject("Poster for " + abductor.getNickname());
            document.addKeywords("OpenMPIS, missing, found, unidentified");
            document.addProducer();
            document.addCreationDate();
            document.addCreator("OpenMPIS version " + Constants.VERSION);
            document.open();
            Paragraph wantedParagraph = new Paragraph("W A N T E D", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 36, Font.BOLD, new Color(255, 0, 0)));
            wantedParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(wantedParagraph);
            Paragraph redParagraph;
            if (!abductor.getNickname().isEmpty()) {
                redParagraph = new Paragraph(abductor.getFirstName() + " \"" + abductor.getNickname() + "\" " + abductor.getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            } else {
                redParagraph = new Paragraph(abductor.getFirstName() + " " + abductor.getLastName(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.BOLD, new Color(255, 0, 0)));
            }
            redParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(redParagraph);
            Image image = Image.getInstance(absoluteDefaultPhotoFilename);
            image.scaleAbsolute(200, 300);
            image.setAlignment(Image.ALIGN_CENTER);
            document.add(image);
            Paragraph blackParagraph;
            if (abductor.getBirthMonth() > 0) {
                blackParagraph = new Paragraph(getResources(request).getMessage("label.date.birth") + ": " + getResources(request).getMessage("month." + abductor.getBirthMonth()) + " " + abductor.getBirthDay() + ", " + abductor.getBirthYear(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
                blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
                document.add(blackParagraph);
            }
            blackParagraph = new Paragraph(getResources(request).getMessage("label.address.city") + ": " + abductor.getCity(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.sex") + ": " + getResources(request).getMessage("sex." + abductor.getSex()), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.height") + ": " + abductor.getFeet() + "' " + abductor.getInches() + "\"", FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.weight") + ": " + abductor.getWeight() + " " + getResources(request).getMessage("label.weight.lbs"), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.color.hair") + ": " + getResources(request).getMessage("color.hair." + abductor.getHairColor()), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.color.eye") + ": " + getResources(request).getMessage("color.eye." + abductor.getEyeColor()), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.race") + ": " + getResources(request).getMessage("race." + abductor.getRace()), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("label.remarks") + ": " + abductor.getRemarks(), FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph("---------------------------------------");
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            blackParagraph = new Paragraph(getResources(request).getMessage("global.contact"), FontFactory.getFont(FontFactory.HELVETICA, 14, Font.NORMAL, new Color(0, 0, 0)));
            blackParagraph.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(blackParagraph);
            document.close();
            response.setContentType("application/pdf");
            response.setContentLength(baos.size());
            response.setHeader("Content-disposition", "attachment; filename=Poster.pdf");
            baos.writeTo(response.getOutputStream());
            response.getOutputStream().flush();
            return null;
        } catch (NumberFormatException nfe) {
            return mapping.findForward(Constants.LIST_PERSON);
        } catch (NullPointerException npe) {
            return mapping.findForward(Constants.LIST_PERSON);
        }
    }

    /**
     * Validates the inputs from the abductor form.
     *
     * @param request       the HTTP Request we are processing
     * @param form          the ActionForm bean for this request
     * @return              <code>true</code> if there are no errors in the form; <code>false</code> otherwise
     */
    private boolean isValidAbductor(HttpServletRequest request, ActionForm form) throws Exception {
        ActionMessages errors = new ActionMessages();
        Validator validator = new Validator();
        boolean isValid = true;
        Calendar calendar = Calendar.getInstance();
        AbductorForm abductorForm = (AbductorForm) form;
        FormFile photoFile = abductorForm.getPhotoFile();
        FormFile agedPhotoFile = abductorForm.getAgedPhotoFile();
        String firstName = abductorForm.getFirstName();
        String nickname = abductorForm.getNickname();
        String middleName = abductorForm.getMiddleName();
        String lastName = abductorForm.getLastName();
        int birthDay = abductorForm.getBirthDay();
        int birthMonth = abductorForm.getBirthMonth() - 1;
        int birthYear = abductorForm.getBirthYear();
        String street = abductorForm.getStreet();
        String city = abductorForm.getCity();
        String province = abductorForm.getProvince();
        String remarks = abductorForm.getRemarks();
        String codisId = abductorForm.getCodisId();
        String afisId = abductorForm.getAfisId();
        String dentalId = abductorForm.getDentalId();
        Abductor existingAbductor = new Abductor();
        if (abductorForm.getId() > 0) {
            existingAbductor = abductorService.getAbductorById(abductorForm.getId());
            if (existingAbductor.getPhoto() != null) {
                abductorForm.setPhoto(existingAbductor.getPhoto());
            }
            if (existingAbductor.getAgedPhoto() != null) {
                abductorForm.setAgedPhoto(existingAbductor.getAgedPhoto());
            }
        }
        if ((photoFile.getFileName().length() > 1) && (!((photoFile.getContentType().equals("image/png")) || (photoFile.getContentType().equals("image/jpeg")) || (photoFile.getContentType().equals("image/gif"))))) {
            errors.add("photofile", new ActionMessage("error.photo.invalid"));
        }
        if ((agedPhotoFile.getFileName().length() > 1) && (!((agedPhotoFile.getContentType().equals("image/png")) || (agedPhotoFile.getContentType().equals("image/jpeg")) || (agedPhotoFile.getContentType().equals("image/gif"))))) {
            errors.add("agedphotofile", new ActionMessage("error.photo.invalid"));
        }
        if ((firstName.length() > 1) && (!validator.isValidFirstName(firstName))) {
            errors.add("firstname", new ActionMessage("error.firstname.invalid"));
        }
        if ((nickname.length() > 1) && (!validator.isValidFirstName(nickname))) {
            errors.add("nickname", new ActionMessage("error.nickname.invalid"));
        }
        if ((middleName.length() > 1) && (!validator.isValidLastName(middleName))) {
            errors.add("middlename", new ActionMessage("error.middlename.invalid"));
        }
        if ((lastName.length() > 1) && (!validator.isValidLastName(lastName))) {
            errors.add("lastname", new ActionMessage("error.lastname.invalid"));
        }
        if (birthMonth > calendar.get(Calendar.MONTH) && (birthYear == calendar.get(Calendar.YEAR))) {
            errors.add("birthdate", new ActionMessage("error.birthmonth.invalid"));
        }
        if ((birthMonth == calendar.get(Calendar.MONTH)) && (birthDay > calendar.get(Calendar.DATE)) && (birthYear == calendar.get(Calendar.YEAR))) {
            errors.add("birthdate", new ActionMessage("error.birthday.invalid"));
        }
        if ((street.length() > 1) && (!validator.isValidStreet(street))) {
            errors.add("street", new ActionMessage("error.street.invalid"));
        }
        if ((city.length() > 1) && (!validator.isValidCity(city))) {
            errors.add("city", new ActionMessage("error.city.invalid"));
        }
        if ((province.length() > 1) && (!validator.isValidProvince(province))) {
            errors.add("province", new ActionMessage("error.province.invalid"));
        }
        if (remarks.length() < 1) {
            errors.add("remarks", new ActionMessage("error.remarks.required"));
        } else {
            if (remarks.length() < 10) {
                errors.add("remarks", new ActionMessage("error.remarks.invalid"));
            }
        }
        if ((codisId.length() > 1) && (!validator.isValidId(codisId))) {
            errors.add("codisid", new ActionMessage("error.codisid.invalid"));
        }
        if ((afisId.length() > 1) && (!validator.isValidId(afisId))) {
            errors.add("afisid", new ActionMessage("error.afisid.invalid"));
        }
        if ((dentalId.length() > 1) && (!validator.isValidId(dentalId))) {
            errors.add("dentalid", new ActionMessage("error.dentalid.invalid"));
        }
        if (!errors.isEmpty()) {
            saveErrors(request, errors);
            isValid = false;
        }
        return isValid;
    }

    /**
     * Creates a unique directory name for the abductor's uploaded photos.
     * Adapted from http://snipplr.com/view/4321/generate-md5-hash-from-string/.
     *
     * @param id            the id of the abductor on which the directory name is based
     * @return              the 32 alphanumeric-equivalent of the nickname
     * @throws java.security.NoSuchAlgorithmException
     */
    private String createDirectoryName(Integer id) throws NoSuchAlgorithmException {
        StringBuffer uniqueDirectoryName = new StringBuffer();
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.reset();
        md5.update(id.byteValue());
        byte digest[] = md5.digest();
        for (int i = 0; i < digest.length; i++) {
            uniqueDirectoryName.append(Integer.toHexString(0xFF & digest[i]));
        }
        return uniqueDirectoryName.toString();
    }

    /**
     * Calculates the age of a abductor.
     * Adapted from http://www.coderanch.com/t/391834/Java-General-beginner/java/there-better-way-calculate-age
     *
     * @param birthMonth    the abductor's birth month
     * @param birthDay      the abductor's birth day
     * @param birthYear     the abductor's birth year
     * @return              the abductor's age
     */
    private int getAge(int birthMonth, int birthDay, int birthYear) {
        Calendar birthDate = Calendar.getInstance();
        birthDate.set(birthYear, birthMonth, birthDay);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }
        return age;
    }
}
