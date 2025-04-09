package org.ochan.control;

import java.io.ByteArrayOutputStream;
import java.net.SocketException;
import java.util.Date;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.javasimon.SimonManager;
import org.javasimon.Split;
import org.javasimon.Stopwatch;
import org.ochan.entity.ImagePost;
import org.ochan.entity.Post;
import org.ochan.service.BlobService;
import org.ochan.service.PostService;
import org.ochan.util.Throttler;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

/**
 * This class handles generating a zip of the thread. We should probably
 * consider saving these to the filesystem and busting them when a new post is
 * made.. since this is probably pretty expensive to make.
 * 
 * @author dseymore
 * 
 */
@ManagedResource(description = "ThreadZIp", objectName = "Ochan:util=controller,name=ThreadZip", logFile = "jmx.log")
public class ThreadZipController implements Controller {

    private static final Log LOG = LogFactory.getLog(ThreadZipController.class);

    private static final Preferences PREFS = Preferences.userNodeForPackage(ThreadZipController.class);

    private PostService postService;

    private BlobService blobService;

    /**
	 * The default zip generation length of time that would cause an exception
	 * to be logged
	 */
    private static final String LOG_ON_THIS_TIME = "200";

    public static final Long REQUESTS_PER_MINUTE = Long.valueOf(10);

    private static long totalTimeInMillis = 0;

    private static long numberOfZips = 0;

    private static long lastTimeInMillis = 0;

    private static long totalGenerationTimeInMillis = 0;

    private static long lastGenerationTimeInMillis = 0;

    private static Stopwatch requestWaitTime = SimonManager.getStopwatch(ThreadZipController.class.getName() + "Request");

    @ManagedAttribute(description = "The average time a requests waits around being throttled")
    public double getRequestWaitTime() {
        return requestWaitTime.getStatProcessor().getMean();
    }

    @ManagedAttribute(description = "The number of requests the thumbnailer will pump out a minute")
    public String getRequestsPerMinute() {
        return PREFS.get("requestsPerMinute", REQUESTS_PER_MINUTE.toString());
    }

    @ManagedAttribute(description = "The number of requests the thumbnailer will pump out a minute")
    public void setRequestsPerMinute(String requestsPerMinute) {
        if (StringUtils.isNumeric(requestsPerMinute)) {
            PREFS.put("requestsPerMinute", requestsPerMinute);
        }
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The time in milliseconds that is threshold before logging exceptions due to poor performance.")
    public Long getTimeLength() {
        return new Long(PREFS.get("TIME", LOG_ON_THIS_TIME));
    }

    /**
	 * 
	 * @param value
	 */
    @ManagedAttribute(defaultValue = "200", description = "The time in milliseconds that is threshold before logging exceptions due to poor performance.", persistPolicy = "OnUpdate")
    @ManagedOperationParameters(@ManagedOperationParameter(description = "Time in Milliseconds", name = "value"))
    @ManagedOperation(description = "")
    public void setTimeLength(String value) {
        PREFS.put("TIME", value);
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The average time in milliseconds the system is encountering on zip requests")
    public long getAverageTimeInMillis() {
        if (totalTimeInMillis != 0 && numberOfZips != 0) {
            return totalTimeInMillis / numberOfZips;
        }
        return 0;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The average time in milliseconds the system is encountering on generating zip requests")
    public long getAverageGenerationTimeInMillis() {
        if (totalGenerationTimeInMillis != 0 && numberOfZips != 0) {
            return totalGenerationTimeInMillis / numberOfZips;
        }
        return 0;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The number of zips that have been requested")
    public long getNumberOfZips() {
        return numberOfZips;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The last time we generated & transferred a zip in milliseconds")
    public long getLastTimeInMIllis() {
        return lastTimeInMillis;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The total time in milliseconds it has taken to generate & transferred all zips")
    public long getTotalTimeInMIllis() {
        return totalTimeInMillis;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The last time we generated a zip in milliseconds")
    public long getLastGenerationTimeInMillis() {
        return lastGenerationTimeInMillis;
    }

    /**
	 * 
	 * @return
	 */
    @ManagedAttribute(description = "The total time in milliseconds it has taken to generate all zips")
    public long getTotalGenerationTimeInMIllis() {
        return totalGenerationTimeInMillis;
    }

    /**
	 * @return the postService
	 */
    public PostService getPostService() {
        return postService;
    }

    /**
	 * @param postService
	 *            the postService to set
	 */
    public void setPostService(PostService postService) {
        this.postService = postService;
    }

    /**
	 * @param blobService
	 *            the blobService to set
	 */
    public void setBlobService(BlobService blobService) {
        this.blobService = blobService;
    }

    /**
	 * For a parameter of 'identifier' that is a thread, it creates a zip output
	 * of all the images.
	 */
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Split requestSplit = requestWaitTime.start();
        Throttler requestThrottler = new Throttler(Long.valueOf(getRequestsPerMinute()).intValue(), 60000);
        requestThrottler.StartRequest();
        requestSplit.stop();
        long start = new Date().getTime();
        try {
            numberOfZips++;
            Long id = Long.valueOf(request.getParameter("identifier"));
            org.ochan.entity.Thread t = new org.ochan.entity.Thread();
            t.setIdentifier(id);
            List<Post> posts = postService.retrieveThreadPosts(t.getIdentifier());
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ZipOutputStream zipfile = new ZipOutputStream(bos);
            for (Post post : posts) {
                if (post instanceof ImagePost) {
                    ImagePost image = (ImagePost) post;
                    Byte[] fullImageBytes = blobService.getBlob(image.getImageIdentifier());
                    ZipEntry zipentry = new ZipEntry(post.getIdentifier() + ".jpg");
                    zipfile.putNextEntry(zipentry);
                    byte[] datum = new byte[fullImageBytes.length];
                    int i = 0;
                    for (Byte val : fullImageBytes) {
                        datum[i] = val.byteValue();
                        i++;
                    }
                    zipfile.write((byte[]) datum);
                }
            }
            zipfile.close();
            byte[] datum = bos.toByteArray();
            long generationEnd = new Date().getTime();
            lastGenerationTimeInMillis = generationEnd - start;
            totalGenerationTimeInMillis += generationEnd - start;
            response.setContentType("application/zip");
            response.setHeader("Cache-Control", "no-cache");
            response.setHeader("Pragma", "no-cache");
            response.setDateHeader("Expires", 0);
            LOG.debug("file length is " + datum.length);
            response.setContentLength(datum.length);
            response.setHeader("Content-Disposition", " inline; filename=" + id + ".zip");
            FileCopyUtils.copy(datum, response.getOutputStream());
        } catch (SocketException se) {
            LOG.trace("Socket exception", se);
        } catch (Exception e) {
            LOG.error("Unable to create thumbnail", e);
        }
        long end = new Date().getTime();
        lastTimeInMillis = end - start;
        totalTimeInMillis += end - start;
        if (lastTimeInMillis > getTimeLength()) {
            LOG.warn("Zip times are getting excessive: " + lastTimeInMillis + " ms. for thread id:" + request.getParameter("identifier"));
        }
        return null;
    }
}
