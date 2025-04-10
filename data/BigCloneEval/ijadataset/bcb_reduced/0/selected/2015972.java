package org.jcrom;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogManager;
import javax.jcr.Node;
import javax.jcr.Repository;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.core.TransientRepository;
import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Olafur Gauti Gudmundsson
 */
public class TestLazyLoading {

    private Repository repo;

    private Session session;

    @Before
    public void setUpRepository() throws Exception {
        repo = (Repository) new TransientRepository();
        session = repo.login(new SimpleCredentials("a", "b".toCharArray()));
        ClassLoader loader = TestMapping.class.getClassLoader();
        URL url = loader.getResource("logger.properties");
        if (url == null) {
            url = loader.getResource("/logger.properties");
        }
        LogManager.getLogManager().readConfiguration(url.openStream());
    }

    @After
    public void tearDownRepository() throws Exception {
        session.logout();
        deleteDir(new File("repository"));
        new File("repository.xml").delete();
        new File("derby.log").delete();
    }

    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

    @Test
    public void testLazyLoading() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(Tree.class).map(LazyObject.class);
        TreeNode homeNode = new TreeNode();
        homeNode.setName("home");
        TreeNode newsNode = new TreeNode();
        newsNode.setName("news");
        TreeNode productsNode = new TreeNode();
        productsNode.setName("products");
        TreeNode templateNode = new TreeNode();
        templateNode.setName("template");
        homeNode.addChild(newsNode);
        homeNode.addChild(productsNode);
        LazyInterface lazyObject1 = new LazyObject();
        lazyObject1.setName("one");
        lazyObject1.setString("a");
        LazyInterface lazyObject2 = new LazyObject();
        lazyObject2.setName("two");
        lazyObject2.setString("b");
        Tree tree = new Tree();
        tree.setName("Tree");
        tree.setPath("/");
        tree.addChild(homeNode);
        tree.setTemplateNode(templateNode);
        tree.setLazyObject(lazyObject1);
        tree.addLazyObject(lazyObject1);
        tree.addLazyObject(lazyObject2);
        Node treeRootNode = jcrom.addNode(session.getRootNode(), tree);
        Tree fromNode = jcrom.fromNode(Tree.class, treeRootNode);
        assertEquals(tree.getChildren().size(), fromNode.getChildren().size());
        assertEquals(lazyObject1.getString(), fromNode.getLazyObject().getString());
        assertEquals(tree.getLazyObjects().size(), fromNode.getLazyObjects().size());
        assertEquals(lazyObject2.getString(), fromNode.getLazyObjects().get(1).getString());
        assertNull(fromNode.getStartNode());
        TreeNode homeFromNode = fromNode.getChildren().get(0);
        assertTrue(homeFromNode.getChildren().size() == homeNode.getChildren().size());
        assertTrue(homeFromNode.getChildren().get(0).getName().equals(newsNode.getName()));
        fromNode.addFavourite(newsNode);
        fromNode.setStartNode(productsNode);
        jcrom.updateNode(treeRootNode, fromNode);
        Tree modifiedFromNode = jcrom.fromNode(Tree.class, treeRootNode);
        assertTrue(modifiedFromNode.getFavourites().size() == fromNode.getFavourites().size());
        assertTrue(modifiedFromNode.getStartNode().getName().equals(productsNode.getName()));
        assertTrue(modifiedFromNode.getStartNode().getChildren().size() == productsNode.getChildren().size());
    }

    @Test
    public void testDynamicMaps() throws Exception {
        Jcrom jcrom = new Jcrom(true, true);
        jcrom.map(DynamicObject.class).map(TreeNode.class);
        TreeNode node1 = new TreeNode("node1");
        TreeNode node2 = new TreeNode("node2");
        List<Object> treeNodes = new ArrayList<Object>();
        treeNodes.add(new TreeNode("multiNode1"));
        treeNodes.add(new TreeNode("multiNode2"));
        JcrFile file1 = TestMapping.createFile("file1.jpg");
        JcrFile file2 = TestMapping.createFile("file2.jpg");
        List<JcrFile> files = new ArrayList<JcrFile>();
        files.add(TestMapping.createFile("multifile1.jpg"));
        files.add(TestMapping.createFile("multifile2.jpg"));
        DynamicObject dynamicObj = new DynamicObject();
        dynamicObj.setName("dynamic");
        dynamicObj.putSingleValueChild(node1.getName(), node1);
        dynamicObj.putSingleValueChild(node2.getName(), node2);
        dynamicObj.putMultiValueChild("many", treeNodes);
        dynamicObj.putSingleFile(file1.getName(), file1);
        dynamicObj.putSingleFile(file2.getName(), file2);
        dynamicObj.putMultiFile("manyFiles", files);
        Node newNode = jcrom.addNode(session.getRootNode(), dynamicObj);
        DynamicObject fromNode = jcrom.fromNode(DynamicObject.class, newNode);
        assertTrue(fromNode.getSingleValueChildren().size() == dynamicObj.getSingleValueChildren().size());
        assertTrue(fromNode.getMultiValueChildren().size() == dynamicObj.getMultiValueChildren().size());
        TreeNode node1FromList = (TreeNode) fromNode.getMultiValueChildren().get("many").get(0);
        assertEquals("multiNode1", node1FromList.getName());
        TreeNode node2FromList = (TreeNode) fromNode.getMultiValueChildren().get("many").get(1);
        assertEquals("multiNode2", node2FromList.getName());
        TreeNode node1FromNode = (TreeNode) fromNode.getSingleValueChildren().get(node1.getName());
        assertTrue(node1FromNode.getName().equals(node1.getName()));
        TreeNode node2FromNode = (TreeNode) fromNode.getSingleValueChildren().get(node2.getName());
        assertTrue(node2FromNode.getName().equals(node2.getName()));
        assertTrue(fromNode.getSingleFiles().size() == dynamicObj.getSingleFiles().size());
        assertTrue(fromNode.getMultiFiles().size() == dynamicObj.getMultiFiles().size());
        assertTrue(fromNode.getSingleFiles().get(file1.getName()).getMimeType().equals(file1.getMimeType()));
        assertTrue(fromNode.getSingleFiles().get(file2.getName()).getMimeType().equals(file2.getMimeType()));
        assertTrue(fromNode.getMultiFiles().get("manyFiles").get(0).getName().equals("multifile1.jpg"));
        assertTrue(fromNode.getMultiFiles().get("manyFiles").get(1).getName().equals("multifile2.jpg"));
        fromNode.getSingleFiles().get(file1.getName()).getDataProvider().getInputStream().available();
        fromNode.getSingleFiles().get(file2.getName()).getDataProvider().getInputStream().available();
        TreeNode ref1 = new TreeNode("ref1");
        TreeNode ref2 = new TreeNode("ref2");
        jcrom.addNode(session.getRootNode(), ref1);
        jcrom.addNode(session.getRootNode(), ref2);
        List<Object> multiRefs = new ArrayList<Object>();
        multiRefs.add(ref1);
        multiRefs.add(ref2);
        fromNode.putSingleReference(ref1.getName(), ref1);
        fromNode.putSingleReference(ref2.getName(), ref2);
        fromNode.putMultiReference("manyRefs", multiRefs);
        jcrom.updateNode(newNode, fromNode);
        session.save();
        DynamicObject updatedNode = jcrom.fromNode(DynamicObject.class, newNode);
        assertTrue(updatedNode.getSingleReferences().size() == fromNode.getSingleReferences().size());
        assertTrue(updatedNode.getMultiReferences().size() == fromNode.getMultiReferences().size());
        TreeNode ref1FromList = (TreeNode) updatedNode.getMultiReferences().get("manyRefs").get(0);
        assertTrue(ref1FromList.getName().equals(ref1.getName()));
        TreeNode ref2FromList = (TreeNode) updatedNode.getMultiReferences().get("manyRefs").get(1);
        assertTrue(ref2FromList.getName().equals(ref2.getName()));
        TreeNode ref1FromNode = (TreeNode) updatedNode.getSingleReferences().get(ref1.getName());
        assertTrue(ref1FromNode.getName().equals(ref1.getName()));
        TreeNode ref2FromNode = (TreeNode) updatedNode.getSingleReferences().get(ref2.getName());
        assertTrue(ref2FromNode.getName().equals(ref2.getName()));
    }
}
