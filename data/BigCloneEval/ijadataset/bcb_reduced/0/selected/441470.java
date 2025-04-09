package ru.spbspu.staub.bean.admin;

import static org.jboss.seam.ScopeType.SESSION;
import org.jboss.seam.annotations.In;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.Synchronized;
import org.jboss.seam.contexts.Contexts;
import org.richfaces.event.UploadEvent;
import ru.spbspu.staub.bean.GenericListBean;
import ru.spbspu.staub.entity.*;
import ru.spbspu.staub.exception.RemoveException;
import ru.spbspu.staub.model.list.FormProperties;
import ru.spbspu.staub.model.list.FormTable;
import ru.spbspu.staub.service.*;
import ru.spbspu.staub.util.DownloadResource;
import ru.spbspu.staub.util.ImageResource;
import javax.xml.bind.JAXBException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Webbean for manipulating list data of <code>Question</code> entities.
 *
 * @author Konstantin Grigoriev
 */
@Name("questionListBean")
@Scope(SESSION)
@Synchronized(timeout = 10000)
public class QuestionListBean extends GenericListBean<Question> {

    private static final long serialVersionUID = 5563120994479387998L;

    private static final String XML_EXTENSION = ".xml";

    private static final int EXPORT_ROWS_ON_PAGE = 100;

    private static final String EXPORT_DEFAULT_FILENAME = "exportedQuestions";

    @In
    private QuestionService questionService;

    @In
    private DisciplineService disciplineService;

    @In
    private CategoryService categoryService;

    @In
    private TopicService topicService;

    @In
    private DifficultyService difficultyService;

    @In
    private User user;

    private List<Discipline> disciplineList;

    private List<Category> categoryList;

    private List<Topic> topicList;

    private List<Difficulty> difficultyList;

    private Discipline discipline;

    private Category category;

    private Topic topic;

    private Difficulty difficulty;

    private Integer questionId;

    private File uploadedFile;

    /**
     * {@inheritDoc}
     */
    @Override
    protected FormTable findObjects(FormProperties formProperties) {
        return questionService.find(formProperties, discipline, category, topic, difficulty, questionId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void prepareBean() {
        fillDisciplineList();
        fillDifficultiesList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doDelete() {
        try {
            questionService.remove(getSelected());
            doRefresh();
        } catch (RemoveException e) {
            addFacesMessageFromResourceBundle("common.messages.deleteFailed");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String doCreate() {
        Contexts.getConversationContext().set(Discipline.class.getName(), discipline);
        Contexts.getConversationContext().set(Category.class.getName(), category);
        Contexts.getConversationContext().set(Topic.class.getName(), topic);
        Contexts.getConversationContext().set(Difficulty.class.getName(), difficulty);
        return super.doCreate();
    }

    private void fillDisciplineList() {
        disciplineList = disciplineService.findAll();
    }

    private void fillCategoryList() {
        if (discipline != null) {
            categoryList = categoryService.find(discipline);
        } else {
            categoryList = null;
        }
    }

    private void fillTopicList() {
        if (category != null) {
            topicList = topicService.find(category);
        } else {
            topicList = null;
        }
    }

    private void fillDifficultiesList() {
        difficultyList = difficultyService.findAll();
    }

    public void setDiscipline() {
        fillCategoryList();
        setCategory(null);
        setTopic(null);
        setQuestionId(null);
        doRefresh();
    }

    public void setCategory() {
        fillTopicList();
        setTopic(null);
        setQuestionId(null);
        doRefresh();
    }

    public void setTopic() {
        setQuestionId(null);
        doRefresh();
    }

    public void setDifficulty() {
        setQuestionId(null);
        doRefresh();
    }

    public void setQuestionId() {
        setDiscipline(null);
        setCategory(null);
        setTopic(null);
        setDifficulty(null);
        doRefresh();
    }

    public void doExportQuestions() throws Exception {
        logger.debug(">>> Export questions...");
        int currentRowsOnPage = getRowsOnPage();
        int currentPage = getCurrentPage();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ZipOutputStream zipStream = new ZipOutputStream(result);
        try {
            setRowsOnPage(EXPORT_ROWS_ON_PAGE);
            for (int i = 1; i <= getPageQuantity(); i++) {
                setCurrentPage(i);
                doExactPage();
                for (Object row : formTable.getRows()) {
                    Question question = (Question) row;
                    zipStream.putNextEntry(new ZipEntry(String.valueOf(question.getId()) + XML_EXTENSION));
                    questionService.exportQuestion(question.getId(), zipStream);
                    List<String> images = getImagesFromText(question.getDefinition().getDescription());
                    for (String image : images) {
                        logger.debug("  found image reference : #0", image);
                        FileInputStream fi = null;
                        try {
                            fi = new FileInputStream(ImageResource.getResourceDirectory() + File.separator + image);
                            zipStream.putNextEntry(new ZipEntry(image));
                            byte[] buf = new byte[100];
                            int size;
                            while ((size = fi.read(buf)) > 0) {
                                zipStream.write(buf, 0, size);
                            }
                        } finally {
                            closeStream(fi);
                        }
                    }
                }
            }
            zipStream.close();
            byte[] data = result.toByteArray();
            Contexts.getSessionContext().set(DownloadResource.SESSION_KEY, data);
            Contexts.getSessionContext().set(DownloadResource.FILENAME_KEY, EXPORT_DEFAULT_FILENAME);
        } catch (IOException e) {
            throw new Exception("error during exporting to xml", e);
        } catch (JAXBException e) {
            throw new Exception("error during exporting to xml", e);
        } finally {
            closeStream(result);
            closeStream(zipStream);
            setRowsOnPage(currentRowsOnPage);
            setCurrentPage(currentPage);
        }
        logger.debug("<<< Export questions...Ok");
    }

    private List<String> getImagesFromText(String text) {
        List<String> result = new ArrayList<String>();
        int position = 0;
        while (text.indexOf(ImageResource.RESOURCE_PATH, position) != -1) {
            int start = text.indexOf(ImageResource.RESOURCE_PATH, position) + ImageResource.RESOURCE_PATH.length() + 1;
            int end = text.indexOf("\"/>", start);
            String image = text.substring(start, end);
            result.add(image);
            position = start;
        }
        return result;
    }

    private void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                logger.error("error during stream closing", e);
            }
        }
    }

    /**
     * Cancels export, clears session attributes.
     */
    public void doCancelExport() {
        Contexts.getSessionContext().set(DownloadResource.SESSION_KEY, null);
        Contexts.getSessionContext().set(DownloadResource.FILENAME_KEY, null);
    }

    public void doImportQuestions() throws Exception {
        logger.debug(">>> Import questions...");
        logger.debug(" importing from file : #0", uploadedFile);
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(new FileInputStream(uploadedFile));
            ZipEntry zipEntry;
            while ((zipEntry = zipStream.getNextEntry()) != null) {
                logger.debug(" processing zip entry : #0", zipEntry.getName());
                if (zipEntry.getName().endsWith(XML_EXTENSION)) {
                    logger.debug("  processing XML...");
                    questionService.importQuestion(zipStream, user);
                    logger.debug("  processing XML...Ok");
                } else {
                    logger.debug("  processing image...");
                    FileOutputStream fo = null;
                    try {
                        File file = new File(ImageResource.getResourceDirectory() + File.separator + zipEntry.getName());
                        file.createNewFile();
                        fo = new FileOutputStream(file);
                        byte[] buf = new byte[100];
                        int size;
                        while ((size = zipStream.read(buf)) > 0) {
                            fo.write(buf, 0, size);
                        }
                    } finally {
                        closeStream(fo);
                    }
                    logger.debug("  processing image...Ok");
                }
            }
        } catch (IOException e) {
            throw new Exception("error during importing questions", e);
        } catch (JAXBException e) {
            throw new Exception("error during importing questions", e);
        } finally {
            closeStream(zipStream);
        }
        doRefresh();
        uploadedFile = null;
        logger.debug("<<< Import questions...Ok");
    }

    public void doUploadQuestions(UploadEvent event) {
        logger.debug(">>> Upload questions...");
        uploadedFile = event.getUploadItem().getFile();
        logger.debug("<<< Upload questions...Ok");
    }

    public boolean isReadyForImport() {
        return uploadedFile != null;
    }

    public List<Discipline> getDisciplineList() {
        return disciplineList;
    }

    public List<Category> getCategoryList() {
        return categoryList;
    }

    public List<Topic> getTopicList() {
        return topicList;
    }

    public List<Difficulty> getDifficultyList() {
        return difficultyList;
    }

    public Discipline getDiscipline() {
        return discipline;
    }

    public void setDiscipline(Discipline discipline) {
        this.discipline = discipline;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Topic getTopic() {
        return topic;
    }

    public void setTopic(Topic topic) {
        this.topic = topic;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }
}
