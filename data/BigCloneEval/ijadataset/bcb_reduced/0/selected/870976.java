package org.apache.jackrabbit.demo.blog.model;

import java.util.ArrayList;
import java.util.Calendar;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.ValueFactory;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.jackrabbit.demo.blog.exception.InvalidUserException;

/**
 * This class <code>BlogManager</code> handles manupulating blog entries. <code>BlogManager</code> users following node structure
 * /blogRoot [nt:folder]
 * /blogRoot/user [blog:user]
 * /blogRoot/user/<yyyy> [nt:folder]
 * /blogRoot/user/<yyyy>/<mm> [nt:folder]
 * /blogRoot/user/<yyyy>/<mm>/blogEntry [blog:blogEntry]
 * /blogRoot/user/<yyyy>/<mm>/blogEntry/comment [blog:Comment]
 *
 */
public class BlogManager {

    /**
     * Logger
     */
    private static final Logger log = LoggerFactory.getLogger(BlogManager.class);

    public BlogManager() {
    }

    /**
	 * This method adds a blog entry to the given users blog space
	 * @param username username of the creater of the blog entry
	 * @param title title of the blog entry to be created 
	 * @param content content of the blog entry
	 * @param session JCR session to be used for adding the blogEntry node to user
	 */
    public static void addBlogEntry(String username, String title, String content, Session session) {
        try {
            Node userNode = (Node) session.getItem("/blogRoot/" + username);
            if (userNode != null) {
                Node yearNode;
                Node monthNode;
                Node blogEntryNode;
                String nodeName;
                Calendar calendar = Calendar.getInstance();
                String year = calendar.get(Calendar.YEAR) + "";
                String month = calendar.get(Calendar.MONTH) + "";
                if (userNode.hasNode(year)) {
                    yearNode = userNode.getNode(year);
                } else {
                    yearNode = userNode.addNode(year, "nt:folder");
                }
                if (yearNode.hasNode(month)) {
                    monthNode = yearNode.getNode(month);
                } else {
                    monthNode = yearNode.addNode(month, "nt:folder");
                }
                if (monthNode.hasNode(title)) {
                    nodeName = createUniqueName(title, monthNode);
                } else {
                    nodeName = title;
                }
                blogEntryNode = monthNode.addNode(nodeName, "blog:blogEntry");
                blogEntryNode.setProperty("blog:title", title);
                blogEntryNode.setProperty("blog:content", content);
                Value date = session.getValueFactory().createValue(Calendar.getInstance());
                blogEntryNode.setProperty("blog:created", date);
                session.save();
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
    }

    /**
	 * @param username the user whose blog entries we want to retrive
	 * @param session JCR session to access the repository
	 * @return returns an ArrayList of blog entries of the given user
	 * @throws InvalidUserException if the user is not a valid user of the system
	 */
    public static ArrayList<BlogEntry> getByUsername(String username, Session session) throws RepositoryException, InvalidUserException {
        QueryManager queryMgr = session.getWorkspace().getQueryManager();
        String xPath = "/jcr:root/blogRoot/" + username + "//element(*,blog:blogEntry) order by @blog:created descending";
        Query query = queryMgr.createQuery(xPath, Query.XPATH);
        QueryResult queryResult = query.execute();
        NodeIterator iter = queryResult.getNodes();
        ArrayList<BlogEntry> blogEntryList = new ArrayList<BlogEntry>();
        while (iter.hasNext()) {
            Node blogEntryNode = iter.nextNode();
            BlogEntry blogEntry = mapBlogEntry(blogEntryNode);
            blogEntryList.add(blogEntry);
        }
        return blogEntryList;
    }

    public static BlogEntry getByUUID(String UUID, Session session) throws RepositoryException {
        Node blogEntryNode = session.getNodeByUUID(UUID);
        BlogEntry blogEntry = mapBlogEntry(blogEntryNode);
        return blogEntry;
    }

    public static ArrayList<BlogEntry> getByDate(String username, Calendar from, Calendar to, Session session) throws RepositoryException {
        QueryManager queryMgr = session.getWorkspace().getQueryManager();
        ValueFactory factory = session.getValueFactory();
        String iso8601From = factory.createValue(from).getString();
        String iso8601To = factory.createValue(to).getString();
        String xPath = "/jcr:root/blogRoot//*[@blog:created > xs:dateTime('" + iso8601From + "') and @blog:created < xs:dateTime('" + iso8601To + "') ]" + "order by @blog:created descending";
        Query query = queryMgr.createQuery(xPath, Query.XPATH);
        QueryResult queryResult = query.execute();
        NodeIterator iter = queryResult.getNodes();
        ArrayList<BlogEntry> blogEntryList = new ArrayList<BlogEntry>();
        while (iter.hasNext()) {
            Node blogEntryNode = iter.nextNode();
            BlogEntry blogEntry = mapBlogEntry(blogEntryNode);
            blogEntryList.add(blogEntry);
        }
        return blogEntryList;
    }

    public static ArrayList<BlogEntry> getByContent(String content, Session session) throws RepositoryException {
        QueryManager queryMgr = session.getWorkspace().getQueryManager();
        String xPath = "/jcr:root/blogRoot//element(*,blog:blogEntry)[jcr:contains(@blog:content,'" + content + "')] order by @blog:created descending";
        Query query = queryMgr.createQuery(xPath, Query.XPATH);
        QueryResult queryResult = query.execute();
        NodeIterator iter = queryResult.getNodes();
        ArrayList<BlogEntry> blogEntryList = new ArrayList<BlogEntry>();
        while (iter.hasNext()) {
            Node blogEntryNode = iter.nextNode();
            BlogEntry blogEntry = mapBlogEntry(blogEntryNode);
            blogEntryList.add(blogEntry);
        }
        return blogEntryList;
    }

    public static void removeBlogEntry(String UUID, String username, Session session) throws RepositoryException {
        Node blogEntryNode = session.getNodeByUUID(UUID);
        blogEntryNode.remove();
        session.save();
    }

    public static void addComment(String UUID, String comment, String username, Session session) throws RepositoryException {
        Node blogEntryNode = session.getNodeByUUID(UUID);
        Node userNode = (Node) session.getItem("/blogRoot/" + username);
        String unique = createUniqueName("comment", blogEntryNode);
        Node commentNode = blogEntryNode.addNode(unique, "blog:comment");
        commentNode.setProperty("blog:content", comment);
        commentNode.setProperty("blog:commenter", userNode.getUUID());
        session.save();
    }

    public static void rateBlogEntry(String UUID, int rank, String username, Session session) throws RepositoryException {
        Node blogEntryNode = session.getNodeByUUID(UUID);
        long currentRank = blogEntryNode.getProperty("blog:rate").getLong();
        long newRank = (currentRank + rank) / 2;
        blogEntryNode.setProperty("blog:rate", newRank);
        session.save();
    }

    private static BlogEntry mapBlogEntry(Node blogEntryNode) throws RepositoryException {
        BlogEntry blogEntry = new BlogEntry();
        blogEntry.setTitle(blogEntryNode.getProperty("blog:title").getString());
        blogEntry.setContent(blogEntryNode.getProperty("blog:content").getString());
        blogEntry.setCreatedOn(blogEntryNode.getProperty("blog:created").getDate());
        blogEntry.setUser(blogEntryNode.getParent().getParent().getParent().getName());
        blogEntry.setRate(blogEntryNode.getProperty("blog:rate").getLong());
        blogEntry.setUUID(blogEntryNode.getUUID());
        blogEntry.setHasImage(blogEntryNode.hasNode("image"));
        blogEntry.setHasVideo(blogEntryNode.hasNode("video"));
        NodeIterator commentIter = blogEntryNode.getNodes("comment*");
        while (commentIter.hasNext()) {
            Node commentNode = commentIter.nextNode();
            Comment comment = new Comment();
            comment.setContent(commentNode.getProperty("blog:content").getString());
            String commenterUUID = commentNode.getProperty("blog:commenter").getString();
            comment.setCommenter(commentNode.getSession().getNodeByUUID(commenterUUID).getName());
            blogEntry.addComment(comment);
        }
        return blogEntry;
    }

    private static String createUniqueName(String name, Node node) throws RepositoryException {
        String unique;
        int i = 0;
        do {
            unique = name + (i++);
        } while (node.hasNode(unique));
        return unique;
    }
}
