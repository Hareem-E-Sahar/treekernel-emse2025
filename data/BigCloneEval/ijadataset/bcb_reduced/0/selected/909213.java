package com.afp.ines.component.persistence.rdbms;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.iptc.ines.binding.AnyItemBindingException;
import org.iptc.ines.binding.BindingProvider;
import org.iptc.ines.persist.PersistNewsMLG2Item;
import org.iptc.ines.searchengine.ISearchEngineIndexSupport;
import org.iptc.ines.searchengine.exception.IndexException;
import org.iptc.nar.core.model.AnyItemType;
import org.iptc.nar.core.model.newsitem.NewsItem;
import org.springframework.dao.DataAccessException;
import org.w3c.dom.Document;
import com.afp.ines.component.persistence.mapping.type.XMLHelper;
import com.afp.ines.component.persistence.model.NewsMLDocument;
import com.afp.ines.component.persistence.model.StoreAnyItem;
import com.afp.ines.component.persistence.rdbms.dao.StoreItemDao;

/**
 * This persistence implementation use Mysql database store and Lucene Search
 * engine. AnyItem object are store on MySql to provide simple access for users
 * who don't what to use INES API to retrieve data from NewsMLG2 Item.<br>
 * Mysql Storage can be request with simple SQL SELECT for simple application.
 * 
 * @author Bertrand Goupil
 * 
 */
public class PersistNewsMLG2ItemImpl implements PersistNewsMLG2Item {

    private StoreItemDao m_storeItemDao;

    private BindingProvider m_bindingProvider;

    /**
	 * It can be a mock object for RDBMS index strategy
	 */
    private ISearchEngineIndexSupport m_indexSupport;

    public PersistNewsMLG2ItemImpl(StoreItemDao storeItemDao, BindingProvider bindingProvider, ISearchEngineIndexSupport indexSupport) {
        super();
        m_storeItemDao = storeItemDao;
        m_bindingProvider = bindingProvider;
        m_indexSupport = indexSupport;
    }

    /**
	 * Save Object in Mysql and give XML form to Lucene Indexer
	 */
    public void saveOrUpdate(AnyItemType anyItem) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            m_bindingProvider.marshaller(anyItem, outputStream);
        } catch (AnyItemBindingException e) {
            e.printStackTrace();
        }
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Document document = XMLHelper.fileinputStreamToDocument(inputStream);
        StoreAnyItem storeItem = PersistNewsMLFacade.instance.buildStoreAnyItem(anyItem);
        NewsMLDocument newsMLDocument = new NewsMLDocument();
        newsMLDocument.setVersion(anyItem.getItemVersion());
        newsMLDocument.setXml(document);
        newsMLDocument.setStoreAnyItem(storeItem);
        try {
            boolean newindex = m_storeItemDao.saveOrUpdate(storeItem, newsMLDocument);
            if (newindex) {
                m_indexSupport.onSave(anyItem);
                System.out.println("new index");
            } else {
                m_indexSupport.onUpdate(anyItem);
                System.out.println("update index");
            }
        } catch (DataAccessException e) {
            System.out.println("roll back cause : " + e.getMessage());
            return;
        } catch (IndexException e) {
            System.out.println("roll back cause Indexing: " + e.getMessage());
            return;
        }
    }

    /**
	 * Use Lucene search query and map the result to NewsMLG2 object
	 */
    public List<AnyItemType> searchItemFromMetadata(String searchString) {
        return null;
    }

    /**
	 * Use Lucene search query and map the result as NewsItem. The search
	 * request will use customized search index for search on NewsContent.
	 */
    public List<NewsItem> searchItemFromNewsContent(String searchString) {
        return null;
    }
}
