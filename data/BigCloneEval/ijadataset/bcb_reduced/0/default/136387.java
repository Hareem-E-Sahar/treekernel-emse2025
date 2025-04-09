import java.awt.Cursor;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.*;
import com.sun.org.apache.xerces.internal.dom.DocumentImpl;
import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;

/**
 * class for upload and dounload data from/to db
 *
 * @author anton
 *
 */
@SuppressWarnings("restriction")
public class EW_BaseSynchronize {

    private EW_Languages myLanguages;

    private EW_Topic mainTopic;

    private ArrayList<EW_Vocabulary> wordList;

    private Document xmldoc;

    /**
	 * create DB processor for upload and download data	
	 * @param sProfile - current profile
	 * @param topic - selected topic
	 */
    public EW_BaseSynchronize(EW_Profiles sProfile, EW_Topic topic) {
        myLanguages = EW_Languages.GetOneRowByName(sProfile.getStudyLanguage());
        mainTopic = topic;
    }

    /**
	 * create new file and upload from DB to file selected vocabulary
	 * @param parent - main form for fileDialog
	 */
    public void uploadVocabulary(JFrame parent) {
        File name = getFileName(parent, false);
        if (name == null) return; else createVocabularyXMLDocument(name.getAbsolutePath());
    }

    /**
	 * read selected file and download vocabulary date from file to DB
	 * @param parent - main form for fileDialog
	 */
    public void downloadVocabulary(JFrame parent) {
        File name = getFileName(parent, true);
        MainForm.getFrames()[0].setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        if (name == null) return; else readVocabularyXMLDocument(name.getAbsolutePath());
    }

    /**
	 * call file dialog and return selected file or path
	 * @param parent - main parent windows
	 * @param isDounload - boolean if true then open dialog, else save dialog
	 * @return - selected file
	 */
    private File getFileName(JFrame parent, boolean isDounload) {
        JFileChooser fd = new JFileChooser();
        if (isDounload) {
            fd.setDialogTitle("Выбирете файл словарь");
            fd.showOpenDialog(parent);
        } else {
            fd.setDialogTitle("Укажите новый файл словаря");
            fd.showSaveDialog(parent);
        }
        fd.setVisible(true);
        return fd.getSelectedFile();
    }

    /**
	 * get all words for one topic
	 * @param topic - topic for selections word
	 */
    private void selectWordsByTopic(EW_Topic topic) {
        EW_Vocabulary word = new EW_Vocabulary(new EW_Alphabet(myLanguages.getName(), ""));
        word.mySelect.language = myLanguages.getName();
        if (topic != null) {
            word.mySelect.topicName = topic.getTopicName();
        }
        wordList = word.getList(word.mySelect);
    }

    /**
	 * create xml vocabulary file 
	 * @param filePath - path to safe a file
	 */
    private void createVocabularyXMLDocument(String filePath) {
        xmldoc = new DocumentImpl();
        Element root = xmldoc.createElement("language");
        root.setAttribute("name", myLanguages.getName());
        fillDocumentXML(root, mainTopic);
        xmldoc.appendChild(root);
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(filePath);
            OutputFormat of = new OutputFormat("XML", "utf-8", true);
            XMLSerializer serializer = new XMLSerializer(fos, of);
            serializer.asDOMSerializer();
            serializer.serialize(xmldoc.getDocumentElement());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * prepare xml vocabulary document in memory
	 * @param root - base element for topics
	 * @param topic - topic which include words
	 */
    private void fillDocumentXML(Element root, EW_Topic topic) {
        Element topicEl = xmldoc.createElement("topic");
        if (topic != null) topicEl.setAttribute("name", topic.getTopicName());
        selectWordsByTopic(topic);
        for (EW_Vocabulary word : wordList) {
            Element wordEl = xmldoc.createElement("word");
            wordEl.setAttribute("original", word.getWord());
            wordEl.setAttribute("interpretation", word.getInterpretation());
            ArrayList<EW_Topic> wordTopics = word.getWordTopicsList();
            for (EW_Topic ewTopic : wordTopics) {
                Element wTopic = xmldoc.createElement("wordtopic");
                wTopic.setAttribute("name", ewTopic.getTopicName());
                wordEl.appendChild(wTopic);
            }
            topicEl.appendChild(wordEl);
        }
        root.appendChild(topicEl);
    }

    /**
	 * read selected xml document as vocabulary
	 * @param filePath - path for vocabulary file
	 */
    private void readVocabularyXMLDocument(String filePath) {
        try {
            File file = new File(filePath);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(file);
            doc.getDocumentElement().normalize();
            EW_Languages lang = null;
            EW_Topic commonTopic;
            if (doc.getDocumentElement().getNodeName().equals("language")) {
                lang = createNewLanguage(doc.getDocumentElement().getAttribute("name"));
            }
            NodeList topicList = doc.getElementsByTagName("topic");
            for (int i = 0; i < topicList.getLength(); i++) {
                Element thisTopic = (Element) topicList.item(i);
                commonTopic = createMainTopic(thisTopic.getAttribute("name"), lang);
                NodeList wordList = thisTopic.getElementsByTagName("word");
                for (int j = 0; j < wordList.getLength(); j++) {
                    Element word = (Element) wordList.item(j);
                    NodeList wordTopicsList = word.getElementsByTagName("wordtopic");
                    createWord(lang, commonTopic, wordTopicsList, word);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
	 * create word from xml vocabulary file
	 * @param lang - language for new word
	 * @param commonTopic - main topic which include current word
	 * @param topicList - all topics for new word
	 * @param curWord - current word from document (not parsed yet)
	 */
    private void createWord(EW_Languages lang, EW_Topic commonTopic, NodeList topicList, Element curWord) {
        String original = curWord.getAttribute("original");
        String interpretation = curWord.getAttribute("interpretation");
        EW_Vocabulary word = new EW_Vocabulary(new EW_Alphabet(lang.getName(), original.substring(0, 1)));
        word.setWord(original);
        if (word.getId() != 0) {
            word.setInterpretation(mergeInterpretation(word.getInterpretation(), interpretation));
        } else {
            word.setInterpretation(interpretation.toLowerCase());
        }
        word.setTopicToWord(commonTopic);
        for (int i = 0; i < topicList.getLength(); i++) {
            Element thisTopic = (Element) topicList.item(i);
            String topicName = thisTopic.getAttribute("name");
            EW_Topic myTopic = null;
            myTopic = createMainTopic(topicName, lang);
            if (!myTopic.equals(commonTopic)) word.setTopicToWord(myTopic);
        }
        word.insertCurrentRecord();
    }

    /**
	 * join interpretation in current db and interpretation in file
	 * @param oldInter - interpretation in db
	 * @param fileInter - interpretation in file
	 * @return - new interpretation
	 */
    private String mergeInterpretation(String oldInter, String fileInter) {
        String[] interInDB = oldInter.split("[,;]");
        String[] interInFile = fileInter.toLowerCase().split("[,;]");
        String newInter = "";
        for (int i = 0; i < interInFile.length; i++) {
            boolean isExists = false;
            for (int j = 0; j < interInDB.length; j++) {
                if (interInDB[j].toString().trim().equals(interInFile[i].toString().trim())) {
                    isExists = true;
                    break;
                }
            }
            if (!isExists) {
                newInter += interInFile[i].toString().trim() + "; ";
            }
        }
        newInter += oldInter;
        return newInter;
    }

    /**
	 * create new languages from vocabulary xml file
	 * @param langName - new language name
	 * @return - object of new language from db
	 */
    private EW_Languages createNewLanguage(String langName) {
        EW_Languages newLang = EW_Languages.GetOneRowByName(langName);
        if (newLang == null) {
            newLang = new EW_Languages();
            newLang.setName(langName);
        }
        if (!newLang.getIsExist()) {
            newLang.insertCurRecord();
        }
        return newLang;
    }

    /**
	 * create new topic from vocabulary xml file
	 * @param topicName - new topic name
	 * @param lang - language from file
	 * @return - topic from db
	 */
    private EW_Topic createMainTopic(String topicName, EW_Languages lang) {
        if (lang == null) return null;
        if (topicName.equals("")) return null;
        EW_Topic topic = new EW_Topic(topicName.toLowerCase(), lang.getName());
        return topic;
    }
}
