package org.jaudiotagger.audio.mp4;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.NullBoxIdException;
import org.jaudiotagger.audio.mp4.atom.Mp4BoxHeader;
import org.jaudiotagger.audio.mp4.atom.Mp4MetaBox;
import org.jaudiotagger.audio.mp4.atom.Mp4StcoBox;
import org.jaudiotagger.audio.mp4.atom.NullPadding;
import org.jaudiotagger.logging.ErrorMessage;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

/**
 * Tree representing atoms in the mp4 file
 * <p/>
 * Note it doesn't create the complete tree it delves into subtrees for atom we know about and are interested in. (Note
 * it would be impossible to create a complete tree for any file without understanding all the nodes because
 * some atoms such as meta contain data and children and therefore need to be specially preprocessed)
 * <p/>
 * This class is currently only used when writing tags because it better handles the difficulties of mdat aand free
 * atoms being optional/multiple places then the older sequential method. It is expected this class will eventually
 * be used when reading tags as well.
 * <p/>
 * Uses a TreeModel for the tree, with convenience methods holding onto references to most common nodes so they
 * can be used without having to traverse the tree again.
 */
public class Mp4AtomTree {

    private DefaultMutableTreeNode rootNode;

    private DefaultTreeModel dataTree;

    private DefaultMutableTreeNode moovNode;

    private DefaultMutableTreeNode mdatNode;

    private DefaultMutableTreeNode stcoNode;

    private DefaultMutableTreeNode ilstNode;

    private DefaultMutableTreeNode metaNode;

    private DefaultMutableTreeNode udtaNode;

    private DefaultMutableTreeNode hdlrWithinMdiaNode;

    private DefaultMutableTreeNode hdlrWithinMetaNode;

    private List<DefaultMutableTreeNode> freeNodes = new ArrayList<DefaultMutableTreeNode>();

    private List<DefaultMutableTreeNode> mdatNodes = new ArrayList<DefaultMutableTreeNode>();

    private List<DefaultMutableTreeNode> trakNodes = new ArrayList<DefaultMutableTreeNode>();

    private Mp4StcoBox stco;

    private ByteBuffer moovBuffer;

    private Mp4BoxHeader moovHeader;

    public static Logger logger = Logger.getLogger("org.jaudiotagger.audio.mp4");

    /**
     * Create Atom Tree
     *
     * @param raf
     * @throws IOException
     * @throws CannotReadException
     */
    public Mp4AtomTree(RandomAccessFile raf) throws IOException, CannotReadException {
        buildTree(raf, true);
    }

    /**
     * Create Atom Tree and maintain open channel to raf, should only be used if will continue
     * to use raf after this call, you will have to close raf yourself.
     *
     * @param raf
     * @param closeOnExit to keep randomfileaccess open, only used when randomaccessfile already being used
     * @throws IOException
     * @throws CannotReadException
     */
    public Mp4AtomTree(RandomAccessFile raf, boolean closeOnExit) throws IOException, CannotReadException {
        buildTree(raf, closeOnExit);
    }

    /**
     * Build a tree of the atoms in the file
     *
     * @param raf
     * @param closeExit false to keep randomfileacces open, only used when randomaccessfile already being used
     * @return
     * @throws java.io.IOException
     * @throws org.jaudiotagger.audio.exceptions.CannotReadException
     */
    public DefaultTreeModel buildTree(RandomAccessFile raf, boolean closeExit) throws IOException, CannotReadException {
        FileChannel fc = null;
        try {
            fc = raf.getChannel();
            fc.position(0);
            rootNode = new DefaultMutableTreeNode();
            dataTree = new DefaultTreeModel(rootNode);
            ByteBuffer headerBuffer = ByteBuffer.allocate(Mp4BoxHeader.HEADER_LENGTH);
            while (fc.position() < fc.size()) {
                Mp4BoxHeader boxHeader = new Mp4BoxHeader();
                headerBuffer.clear();
                fc.read(headerBuffer);
                headerBuffer.rewind();
                try {
                    boxHeader.update(headerBuffer);
                } catch (NullBoxIdException ne) {
                    if (moovNode != null & mdatNode != null) {
                        NullPadding np = new NullPadding(fc.position() - Mp4BoxHeader.HEADER_LENGTH, fc.size());
                        DefaultMutableTreeNode trailingPaddingNode = new DefaultMutableTreeNode(np);
                        rootNode.add(trailingPaddingNode);
                        logger.warning(ErrorMessage.NULL_PADDING_FOUND_AT_END_OF_MP4.getMsg(np.getFilePos()));
                        break;
                    } else {
                        throw ne;
                    }
                }
                boxHeader.setFilePos(fc.position() - Mp4BoxHeader.HEADER_LENGTH);
                DefaultMutableTreeNode newAtom = new DefaultMutableTreeNode(boxHeader);
                if (boxHeader.getId().equals(Mp4NotMetaFieldKey.MOOV.getFieldName())) {
                    moovNode = newAtom;
                    moovHeader = boxHeader;
                    long filePosStart = fc.position();
                    moovBuffer = ByteBuffer.allocate(boxHeader.getDataLength());
                    fc.read(moovBuffer);
                    moovBuffer.rewind();
                    buildChildrenOfNode(moovBuffer, newAtom);
                    fc.position(filePosStart);
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.FREE.getFieldName())) {
                    freeNodes.add(newAtom);
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.MDAT.getFieldName())) {
                    mdatNode = newAtom;
                    mdatNodes.add(newAtom);
                }
                rootNode.add(newAtom);
                fc.position(fc.position() + boxHeader.getDataLength());
            }
            return dataTree;
        } finally {
            if (mdatNode == null) {
                throw new CannotReadException(ErrorMessage.MP4_CANNOT_FIND_AUDIO.getMsg());
            }
            if (closeExit) {
                fc.close();
            }
        }
    }

    /**
     * Display atom tree
     */
    @SuppressWarnings("unchecked")
    public void printAtomTree() {
        Enumeration<DefaultMutableTreeNode> e = rootNode.preorderEnumeration();
        DefaultMutableTreeNode nextNode;
        while (e.hasMoreElements()) {
            nextNode = e.nextElement();
            Mp4BoxHeader header = (Mp4BoxHeader) nextNode.getUserObject();
            if (header != null) {
                String tabbing = "";
                for (int i = 1; i < nextNode.getLevel(); i++) {
                    tabbing += "\t";
                }
                if (header instanceof NullPadding) {
                    System.out.println(tabbing + "Null pad " + " @ " + header.getFilePos() + " of size:" + header.getLength() + " ,ends @ " + (header.getFilePos() + header.getLength()));
                } else {
                    System.out.println(tabbing + "Atom " + header.getId() + " @ " + header.getFilePos() + " of size:" + header.getLength() + " ,ends @ " + (header.getFilePos() + header.getLength()));
                }
            }
        }
    }

    /**
     *
     * @param moovBuffer
     * @param parentNode
     * @throws IOException
     * @throws CannotReadException
     */
    public void buildChildrenOfNode(ByteBuffer moovBuffer, DefaultMutableTreeNode parentNode) throws IOException, CannotReadException {
        Mp4BoxHeader boxHeader;
        Mp4BoxHeader parentBoxHeader = (Mp4BoxHeader) parentNode.getUserObject();
        int justAfterHeaderPos = moovBuffer.position();
        if (parentBoxHeader.getId().equals(Mp4NotMetaFieldKey.META.getFieldName())) {
            Mp4MetaBox meta = new Mp4MetaBox(parentBoxHeader, moovBuffer);
            meta.processData();
            try {
                boxHeader = new Mp4BoxHeader(moovBuffer);
            } catch (NullBoxIdException nbe) {
                moovBuffer.position(moovBuffer.position() - Mp4MetaBox.FLAGS_LENGTH);
            } finally {
                moovBuffer.position(moovBuffer.position() - Mp4BoxHeader.HEADER_LENGTH);
            }
        }
        int startPos = moovBuffer.position();
        while (moovBuffer.position() < ((startPos + parentBoxHeader.getDataLength()) - Mp4BoxHeader.HEADER_LENGTH)) {
            boxHeader = new Mp4BoxHeader(moovBuffer);
            if (boxHeader != null) {
                boxHeader.setFilePos(moovHeader.getFilePos() + moovBuffer.position());
                logger.finest("Atom " + boxHeader.getId() + " @ " + boxHeader.getFilePos() + " of size:" + boxHeader.getLength() + " ,ends @ " + (boxHeader.getFilePos() + boxHeader.getLength()));
                DefaultMutableTreeNode newAtom = new DefaultMutableTreeNode(boxHeader);
                parentNode.add(newAtom);
                if (boxHeader.getId().equals(Mp4NotMetaFieldKey.UDTA.getFieldName())) {
                    udtaNode = newAtom;
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.META.getFieldName()) && parentBoxHeader.getId().equals(Mp4NotMetaFieldKey.UDTA.getFieldName())) {
                    metaNode = newAtom;
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.HDLR.getFieldName()) && parentBoxHeader.getId().equals(Mp4NotMetaFieldKey.META.getFieldName())) {
                    hdlrWithinMetaNode = newAtom;
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.HDLR.getFieldName())) {
                    hdlrWithinMdiaNode = newAtom;
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.STCO.getFieldName())) {
                    if (stco == null) {
                        stco = new Mp4StcoBox(boxHeader, moovBuffer);
                        stcoNode = newAtom;
                    }
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.ILST.getFieldName())) {
                    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentNode.getParent();
                    if (parent != null) {
                        Mp4BoxHeader parentsParent = (Mp4BoxHeader) (parent).getUserObject();
                        if (parentsParent != null) {
                            if (parentBoxHeader.getId().equals(Mp4NotMetaFieldKey.META.getFieldName()) && parentsParent.getId().equals(Mp4NotMetaFieldKey.UDTA.getFieldName())) {
                                ilstNode = newAtom;
                            }
                        }
                    }
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.FREE.getFieldName())) {
                    freeNodes.add(newAtom);
                } else if (boxHeader.getId().equals(Mp4NotMetaFieldKey.TRAK.getFieldName())) {
                    trakNodes.add(newAtom);
                }
                if ((boxHeader.getId().equals(Mp4NotMetaFieldKey.TRAK.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.MDIA.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.MINF.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.STBL.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.UDTA.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.META.getFieldName())) || (boxHeader.getId().equals(Mp4NotMetaFieldKey.ILST.getFieldName()))) {
                    buildChildrenOfNode(moovBuffer, newAtom);
                }
                moovBuffer.position(moovBuffer.position() + boxHeader.getDataLength());
            }
        }
        moovBuffer.position(justAfterHeaderPos);
    }

    /**
     *
     * @return
     */
    public DefaultTreeModel getDataTree() {
        return dataTree;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getMoovNode() {
        return moovNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getStcoNode() {
        return stcoNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getIlstNode() {
        return ilstNode;
    }

    /**
     *
     * @param node
     * @return
     */
    public Mp4BoxHeader getBoxHeader(DefaultMutableTreeNode node) {
        if (node == null) {
            return null;
        }
        return (Mp4BoxHeader) node.getUserObject();
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getMdatNode() {
        return mdatNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getUdtaNode() {
        return udtaNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getMetaNode() {
        return metaNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getHdlrWithinMetaNode() {
        return hdlrWithinMetaNode;
    }

    /**
     *
     * @return
     */
    public DefaultMutableTreeNode getHdlrWithinMdiaNode() {
        return hdlrWithinMdiaNode;
    }

    /**
     *
     * @return
     */
    public List<DefaultMutableTreeNode> getFreeNodes() {
        return freeNodes;
    }

    /**
     *
     * @return
     */
    public List<DefaultMutableTreeNode> getTrakNodes() {
        return trakNodes;
    }

    /**
     *
     * @return
     */
    public Mp4StcoBox getStco() {
        return stco;
    }

    /**
     *
     * @return
     */
    public ByteBuffer getMoovBuffer() {
        return moovBuffer;
    }

    /**
     *
     * @return
     */
    public Mp4BoxHeader getMoovHeader() {
        return moovHeader;
    }
}
