package es.alvsanand.webpage.services.admin;

import java.util.Collections;
import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import es.alvsanand.webpage.common.Globals;
import es.alvsanand.webpage.common.Logger;
import es.alvsanand.webpage.services.ServiceException;

/**
 * This class implements the service Tag
 * 
 * @author alvaro.santos
 * @date 18/11/2009
 * 
 */
public class WebAdminServiceImpl implements WebAdminService {

    private static final Logger logger = new Logger(WebAdminServiceImpl.class);

    public void eraseCache() {
        Cache cache;
        try {
            logger.debug("Erasing cache data");
            CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
            cache = cacheFactory.createCache(Collections.emptyMap());
            for (String cacheName : Globals.CAHE_NAMES) {
                cache.remove(cacheName);
            }
        } catch (CacheException cacheException) {
            logger.error("Error removing erasing cache data.", cacheException);
        }
    }

    public void sentEmail(Message message) throws ServiceException {
        try {
            logger.debug("Sending email");
            Transport.send(message);
        } catch (MessagingException messagingException) {
            logger.error("Error sending email.", messagingException);
            throw new ServiceException(messagingException);
        }
    }
}
