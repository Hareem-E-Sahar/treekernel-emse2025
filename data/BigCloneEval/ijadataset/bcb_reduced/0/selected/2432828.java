package org.acid3lib;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import org.apache.log4j.Logger;

/**
 * The main class to read/write ID3v2 tags.<br>
 * ID3v2 tags offer a flexible way of storing audio meta information within the
 * audio file itself. The information may be technical information, such as
 * equalisation curves, as well as title, performer, copyright etc.
 * <p>
 *
 * <b>Structure</b>
 * <br>Basically, an ID3v2 tag is nothing but a container for several
 * information blocks called 'frames', which store the actual data. Since you
 * have different types of data (artist, album, title - anything you can think
 * of), you also have different types of frames, which are identified by a
 * unique id.
 * <br>A frame now consists of several information parts, which are called frame
 * properties in this library. A frame's properties depend on its type - e.
 * g. there is no use for a frame property describing the text encoding in a
 * frame designed as a play counter.
 * <br>An ID3v2 tag may have padding, i.e. 0x00 bytes at the end of the tag. A
 * possible purpose of this padding is to allow for adding a few additional
 * frames or enlarge existing frames within the tag without having to rewrite
 * the entire file.
 * <br>Things are furthermore complicated by the fact, that there isn't already
 * a formal standard for the ID3v2 format. Currently, there are 3
 * <i>in</i>formal standards (2.2.1, 2.3.0 and 2.4.0). Each version slightly
 * differs in its binary form and adds additional features to the frames (e.g.
 *  compression  of a frame's content) and the overall tag (e.g. CRC32 include).
 * <br>Note that currently, the 2.3.0 standard is the one most widely supported.
 * Many popular programs, like WinAmp support only this standard.
 * <p>
 *
 * <b>Implementation notes</b><br>
 * To make the version struggle a bit easier, this library reflects the thought
 * that an ID3v2 tag basically stays an ID3v2 tag regardless of its version.
 * This means that you will always operate on an <code>ID3v2Tag</code> instance,
 * no matter if it has version 2.2, 2.3 or 2.4.<br>
 * In order to avoid the nessecity to fall back to the smallest common denominator
 * of the available standards and to be able to support the latest 2.4
 * features, the <code>ID3v2Tag</code> and {@link ID3v2Frame} class rely on
 * {@link ID3v2Spec} instances. They contain methods that operate on the tag
 * (i.e. read and write it) as well as methods that describe the spec's
 * possibilities.<br>
 * This means that if you, for example, call {@link #setUseUnsynchronisation(boolean)
 * ID3v2Tag.setUseUnsynchronisation(true)} to enable tag-wide
 * unsynchronisation, the tag will query its current spec about its ability to
 * unsynchronise on a tag-wide basis.<br>
 * If it is supported unsynchronisation will be enabled directly, otherwise the
 * behaviour depends on the tag's so-called spec policy: if it is set to {@link
 * ID3Constants#DYNAMIC_SPEC} the spec will be upgraded to a spec supporting
 * unsynchronisation, otherwise (if it is set to {@link
 * ID3Constants#FIXED_SPEC}), an {@link IllegalTagStateException} is thrown and
 * no further action is performed.
 * <p>
 * Note: for another approach on how to read ID3v2 tags, see the {@link FrameReader}
 * class.
 * <p>
 *
 * <b>Examples:</b><br>
 * <ol>
 * <li>
 * Read a tag:
 * <pre>
 * ID3v2Tag tag  = null;
 * File     file = new File("somemp3.mp3");
 * try {
 *     tag = ID3v2Tag.readTag(file);
 * } catch (FileNotFoundException e) {
 *     System.out.println("File "+file.getName()+" does not exist");
 * } catch (ID3v2NotFoundException e) {
 *     System.out.println("No tag found in file "+file.getName()+");
 * } catch (ID3v2ParseException e) {
 *     System.err.println("Parse error ("+e.getMessage()+")");
 * } catch (IOException e) {
 *     System.err.println("I/O error ("+e.getMessage()+")");
 * }
 * </pre>
 * </li>
 *
 * <li>
 * Create and write a tag:
 * <pre>
 * ID3v2Spec spec = ID3v2Spec.getSpec(3,0);
 * ID3v2Tag  tag  = new ID3v2Tag(spec);
 *
 * FramePropertyDefaults defaults = new FramePropertyDefaults();
 * defaults.add(FrameProperty.ENCODING, ID3Constants.CHARSET_ISO_8859_1);
 * defaults.add(FrameProperty.LANGUAGE, "eng");
 *
 * // basic text frames
 * ID3v2Frame artist = new ID3v2Frame(spec, ID3v2Frame.ARTIST, defaults,
 *     "Tool");
 * ID3v2Frame title  = new ID3v2Frame(spec, ID3v2Frame.TITLE, defaults,
 *     "Schism");
 * ID3v2Frame album  = new ID3v2Frame(spec, ID3v2Frame.ALBUM, defaults,
 *     "Lateralus");
 *
 * // some image data (would be read from file in real application)
 * byte[] data = new byte[] { 0x00 };
 *
 * // more complex frame
 * ID3v2Frame picture = new ID3v2Frame(spec, ID3v2Frame.PICTURE);
 * picture.setUseCompression(true);
 * picture.get(FrameProperty.ENCODING).setValue(ID3Constants.CHARSET_UTF_8);
 * picture.get(FrameProperty.MIME_TYPE).setValue("image/jpeg");
 * picture.get(FrameProperty.DESCRIPTION).setValue("");
 * picture.get(FrameProperty.PICTURE_TYPE).setValue(ID3Constants.COVER_FRONT);
 * picture.get(FrameProperty.MAIN).setValue(data);
 *
 * tag.add(artist);
 * tag.add(title);
 * tag.add(album);
 * tag.add(picture);
 *
 * tag.setPreferredSize(2048, ID3Constants.ABSOLUTE);
 *
 * System.out.println("Tag size - "+tag.getSize());
 * System.out.println("Writing tag to file file...");
 * try {
 *     tag.write(new File("somefile.mp3"), false);
 * } catch (IOException e) {
 *     System.err.println("I/O error");
 * }
 * System.out.println("done");
 * </pre>
 * </li>
 * </ol>
 *
 * @see FrameProperty
 * @see ID3v2Frame
 * @see ID3v2Spec
 * @see FrameReader
 * @author Jascha Ulrich
 */
public final class ID3v2Tag implements Serializable {

    public static final String FRAME_COUNT_PROPERTY = "frameCount";

    public static final String PREFERRED_SIZE_PROPERTY = "preferredSize";

    public static final String PREFERRED_SIZE_MODE_PROPERTY = "preferredSizeMode";

    public static final String SPEC_PROPERTY = "spec";

    public static final String USE_UNSYNCHRONISATION_PROPERTY = "useUnynchronisation";

    public static final String USE_FOOTER_PROPERTY = "useFooter";

    public static final String USE_CRC_PROPERTY = "useCRC";

    public static final String RESTRICTIONS_PROPERTY = "restrictions";

    public static final String EXPERIMENTAL_PROPERTY = "experimental";

    public static final String UPDATE_PROPERTY = "update";

    private static final Logger logger = Logger.getLogger(ID3v2Tag.class);

    private int contentSize;

    private int preferredSize;

    private int preferredSizeMode;

    private int specPolicy;

    private ID3v2Spec spec;

    private ID3v2Spec minimumSpec;

    private Vector frames;

    private ID3v2Frame lastRequestedFrame;

    private boolean sizeChanged;

    private boolean experimental;

    private boolean update;

    private boolean useUnsynchronisation;

    private boolean useFooter;

    private boolean useCRC;

    private ID3v2Restrictions restrictions;

    private PropertyChangeSupport changeSupport;

    /**
	 * Creates an empty ID3v2 tag, which uses the {@link org.acid3lib.spec.Spec230
	 * ID3v2.3.0 standard} and the spec policy {@link ID3Constants#FIXED_SPEC}.
	 * <br>An ID3v2 tag can't be written unless it contains at least one frame.
	 */
    public ID3v2Tag() {
        this(ID3v2Spec.getSpec(3, 0), ID3Constants.FIXED_SPEC);
    }

    /**
	 * Creates an empty ID3v2 tag, which uses the spec <code>spec</code> and the
	 * spec policy {@link ID3Constants#FIXED_SPEC}.
	 *
	 * @param spec the ID3v2 spec the tag will use.
	 */
    public ID3v2Tag(ID3v2Spec spec) {
        this(spec, ID3Constants.FIXED_SPEC);
    }

    /**
	 * Creates an empty ID3v2 tag, which uses the {@link org.acid3lib.spec.Spec230
	 * ID3v2.3.0 standard} and the spec policy <code>specPolicy</code>.
	 *
	 * @param specPolicy the tag's spec policy.
	 * @throws IllegalArgumentException if <code>specPolicy</code> neither is
	 * {@link ID3Constants#FIXED_SPEC} nor {@link ID3Constants#DYNAMIC_SPEC}.
	 */
    public ID3v2Tag(int specPolicy) throws IllegalArgumentException {
        this(ID3v2Spec.getSpec(3, 0), specPolicy);
    }

    /**
	 * Creates an empty ID3v2 tag, which uses the spec <code>spec</code> and the
	 * spec policy <code>specPolicy</code>.
	 *
	 * @param spec the spec the tag will use.
	 * @param specPolicy the spec policy the tag will use.
	 * @throws IllegalArgumentException if <code>specPolicy</code> neither is
	 * {@link ID3Constants#FIXED_SPEC} nor {@link ID3Constants#DYNAMIC_SPEC}.
	 */
    public ID3v2Tag(ID3v2Spec spec, int specPolicy) throws IllegalArgumentException {
        if (specPolicy != ID3Constants.FIXED_SPEC && specPolicy != ID3Constants.DYNAMIC_SPEC) throw new IllegalArgumentException(String.valueOf(specPolicy));
        this.spec = spec;
        this.specPolicy = specPolicy;
        this.minimumSpec = null;
        this.changeSupport = new PropertyChangeSupport(this);
        this.frames = new Vector(30, 10);
        this.contentSize = 10;
        this.preferredSize = 0;
        this.sizeChanged = false;
        this.update = false;
        this.experimental = false;
        this.useUnsynchronisation = false;
        this.useFooter = false;
        this.useCRC = false;
        this.restrictions = null;
    }

    /**
	 * Creates an empty ID3v2 tag using the {@link org.acid3lib.spec.Spec230
	 * ID3v2.3.0 standard} and the spec policy {@link ID3Constants#FIXED_SPEC},
	 * that contains frames representing the information in the ID3v1 tag
	 * <code>tag</code>.
	 *
	 * @param tag an ID3v1 tag.
	 */
    public ID3v2Tag(ID3v1Tag tag) {
        this(ID3Constants.FIXED_SPEC);
        this.update(tag);
    }

    public ID3v2Tag(ID3v2Tag tag) {
        this(tag.getSpec(), tag.getSpecPolicy());
        setExperimental(tag.isExperimental());
        setUseUnsynchronisation(tag.getUseUnsynchronisation());
        setUseFooter(tag.getUseFooter());
        setUseCRC(tag.getUseCRC());
        setRestrictions(tag.getRestrictions());
        setPreferredSize(tag.getPreferredSize(), tag.getPreferredSizeMode());
        for (Iterator i = tag.frames(); i.hasNext(); ) {
            ID3v2Frame frame = (ID3v2Frame) i.next();
            this.add((ID3v2Frame) frame.clone());
        }
    }

    public Object clone() {
        return new ID3v2Tag(this);
    }

    public static boolean isTag(byte[] bytes, int off) {
        if (bytes.length - off < 10) throw new IllegalArgumentException("array can't contain an ID3v2tag");
        if (bytes[off] == 0x49 && bytes[off + 1] == 0x44 && bytes[off + 2] == 0x33) {
            for (int i = off + 3; i < 2; ++i) if ((bytes[i] & 0xff) >= 0xff) return false;
            for (int i = off + 6; i < 4; ++i) if ((bytes[i] & 0xff) >= 0x80) return false;
            return true;
        }
        return false;
    }

    /**
	 * Requests an upgrade of the current spec.
	 * <br>
	 * It searches a spec and tests the return value of the spec's method named
	 * <code>method</code>.
	 *
	 * <code>method</code> must start with "supports" and return a boolean
	 * value.
	 *
	 * @param method the method to be tested.
	 * @throws IllegalArgumentException if<br>
	 * <ul>
	 *   <li><code>method</code> doesn't start with "supports"</li>
	 *   <li>The   method called <code>method</code> takes parameters or doesn't
	 * exist.</li>
	 * </ul>
	 * @throws IllegalTagStateException if <code>getSpecPolicy()</code> is
	 * <code>ID3Constants.FIXED_SPEC</code> and spec can therefore not be
	 * changed.
	 */
    public void requestSpecWhich(String method) throws IllegalArgumentException, IllegalTagStateException {
        if (this.specPolicy == ID3Constants.FIXED_SPEC) throw new IllegalTagStateException("ID3v2Tag.getSpecPolicy() is ID3Constants.FIXED_SPEC and getSpec()." + method + "() = false");
        for (Iterator iterator = ID3v2Spec.specs(); iterator.hasNext(); ) {
            ID3v2Spec current = (ID3v2Spec) iterator.next();
            boolean result = false;
            if (minimumSpec != null) if (current.getVersion() < minimumSpec.getVersion()) continue;
            try {
                Class c = spec.getClass();
                Method m = c.getMethod(method, (Class[]) null);
                if (m.getReturnType().isPrimitive() == false || m.getName().startsWith("supports") == false) throw new IllegalArgumentException(method);
                result = ((Boolean) m.invoke(spec, (Object[]) null)).booleanValue();
            } catch (IllegalAccessException e) {
            } catch (InvocationTargetException e) {
                throw new IllegalArgumentException("Method '" + method + "' must not take parameters");
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException("No such method: " + method);
            }
            if (result) {
                this.performSpecChange(current);
                break;
            }
        }
    }

    /**
	 * Adds the specified tag change listener to receive property change events
	 * from this tag.<br> If listener <code>l</code> is null, no exception is
	 * thrown and no action is performed.
	 *
	 * @param l the property change listener.
	 */
    public void addPropertyChangeListener(PropertyChangeListener l) {
        changeSupport.addPropertyChangeListener(l);
    }

    public void addPropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.addPropertyChangeListener(propertyName, listener);
    }

    /**
	 * Removes the specified tag change listener so that it no longer receives
	 * tag change events from this tag.
	 * @param l the tag change listener.
	 */
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }

    public void removePropertyChangeListener(String propertyName, PropertyChangeListener listener) {
        changeSupport.removePropertyChangeListener(propertyName, listener);
    }

    /**
	 * Adds the frame <code>frame</code> to the tag.
	 *
	 * @param frame the frame to be added.
	 * @throws IllegalArgumentException if the frame's {@link
	 * FrameProperty#MAIN MAIN} property is not set (i.e. empty).
	 */
    public void add(ID3v2Frame frame) throws IllegalArgumentException {
        if (frames.contains(frame)) {
            throw new IllegalArgumentException("frame is already part of the tag.");
        }
        frame.setTag(this);
        frames.add(frame);
        int frameCount = getFrameCount();
        changeSupport.firePropertyChange(FRAME_COUNT_PROPERTY, frameCount - 1, frameCount);
        sizeChanged = true;
    }

    /**
	 * Removes the frame <code>frame</code> from the tag.
	 *
	 * @param frame the frame to be removed.
	 * @return <code>true</code> if the frame was successfully removed, <code>false</code>
	 * otherwise.
	 */
    public boolean remove(ID3v2Frame frame) {
        boolean success = frames.remove(frame);
        if (success) {
            frame.setTag(null);
            int frameCount = getFrameCount();
            changeSupport.firePropertyChange(FRAME_COUNT_PROPERTY, frameCount + 1, frameCount);
            sizeChanged = true;
        }
        return success;
    }

    /**
	 * Removes the first frame of type <code>type</code> in the tag.
	 *
	 * @param type a frame type.
	 */
    public void remove(int type) {
        remove(type, 0);
    }

    /**
	 * Removes the <code>n</code>th frame of type <code>type</code> in the tag.
	 *
	 * @param type a frame type.
	 * @param n
	 */
    public void remove(int type, int n) {
        int j = 0;
        for (int i = 0; i < frames.size(); ++i) {
            ID3v2Frame frame = (ID3v2Frame) frames.elementAt(i);
            if (frame.getType() == type) {
                if (j == n) {
                    remove(frame);
                    break;
                }
                ++j;
            }
        }
    }

    /**
	 * Removes all frames of type <code>type</code> in the tag.
	 *
	 * @param type a frame type.
	 */
    public void removeAll(int type) {
        for (int i = 0; i < frames.size(); ++i) {
            ID3v2Frame frame = (ID3v2Frame) frames.elementAt(i);
            if (frame.getType() == type) {
                remove(frame);
                break;
            }
        }
    }

    /**
	 * Removes all frames from the tag.
	 */
    public void removeAll() {
        int frameCount = frames.size();
        frames.clear();
        if (frameCount > 0) {
            changeSupport.firePropertyChange(FRAME_COUNT_PROPERTY, frameCount, 0);
        }
    }

    /**
	 *
	 * @param tag
	 */
    public void update(ID3v1Tag tag) {
        ID3v2Frame f = null;
        byte defaultEncodingID = FrameProperty.getDefaultEncodingID();
        FramePropertyDefaults defaults = new FramePropertyDefaults();
        defaults.add(FrameProperty.ENCODING, defaultEncodingID);
        if (tag.getTitle().equals("") == false) {
            f = contains(ID3v2Frame.TITLE) ? get(ID3v2Frame.TITLE) : new ID3v2Frame(spec, ID3v2Frame.TITLE, defaults);
            f.get(FrameProperty.ENCODING).setValue(defaultEncodingID);
            f.get(FrameProperty.MAIN).setValue(tag.getTitle());
            add(f);
        }
        if (tag.getArtist().equals("") == false) {
            f = contains(ID3v2Frame.ARTIST) ? get(ID3v2Frame.ARTIST) : new ID3v2Frame(spec, ID3v2Frame.ARTIST, defaults);
            f.get(FrameProperty.MAIN).setValue(tag.getArtist());
            add(f);
        }
        if (tag.getAlbum().equals("") == false) {
            f = contains(ID3v2Frame.ALBUM) ? get(ID3v2Frame.ALBUM) : new ID3v2Frame(spec, ID3v2Frame.ALBUM, defaults);
            f.get(FrameProperty.MAIN).setValue(tag.getAlbum());
            add(f);
        }
        if (tag.getComment().equals("") == false) {
            f = new ID3v2Frame(spec, ID3v2Frame.COMMENT, defaults);
            f.get(FrameProperty.LANGUAGE).setValue(ID3Constants.Language.ENGLISH);
            f.get(FrameProperty.DESCRIPTION).setValue("Converted from ID3v1");
            f.get(FrameProperty.MAIN).setValue(tag.getComment());
            add(f);
        }
        if (tag.getYear().equals("") == false) {
            f = contains(ID3v2Frame.YEAR) ? get(ID3v2Frame.YEAR) : new ID3v2Frame(spec, ID3v2Frame.YEAR, defaults);
            f.get(FrameProperty.MAIN).setValue(tag.getYear());
            add(f);
        }
        if (tag.getTrack().equals("") == false) {
            f = contains(ID3v2Frame.TRACK) ? get(ID3v2Frame.TRACK) : new ID3v2Frame(spec, ID3v2Frame.TRACK, defaults);
            f.get(FrameProperty.MAIN).setValue(tag.getTrack());
            add(f);
        }
        if (tag.getGenre().equals("") == false) {
            f = contains(ID3v2Frame.CONTENT_TYPE) ? get(ID3v2Frame.CONTENT_TYPE) : new ID3v2Frame(spec, ID3v2Frame.CONTENT_TYPE, defaults);
            f.get(FrameProperty.MAIN).setValue(ID3v1Tag.getGenreName(tag.getGenreCode()));
            add(f);
        }
    }

    boolean getSizeChanged() {
        return sizeChanged;
    }

    /**
	 * Recalculates the binary size of the tag only if neccessary.
	 * @see #recalculateSize()
	 */
    void recalculateSizeIfNeccessary() {
        if (!sizeChanged) return;
        recalculateSize();
    }

    /**
	 * Recalculates the binary size of the tag.
	 * <br>
	 * Provides the values for {@link ID3v2Tag#getSize()}, {@link
	 * #getContentSize()} and {@link #getPaddingSize()}.
	 */
    public void recalculateSize() {
        try {
            this.contentSize = spec.getSize(this);
        } catch (RuntimeException e) {
            logger.error("ID3v2Tag::recalculateSize(): " + "Unexpected exception in spec " + spec + " getSize(ID3v2Tag).\n" + "Spec is probably buggy.", e);
            throw e;
        }
        sizeChanged = false;
    }

    /**
	 * Returns the tag's size including the padding.
	 * <br>
	 * Call {@link ID3v2Tag#recalculateSize()} to get an updated value.
	 *
	 * @return the tag's size.
	 */
    public int getSize() {
        recalculateSizeIfNeccessary();
        if (preferredSizeMode == ID3Constants.ABSOLUTE) return (contentSize < preferredSize ? preferredSize : contentSize); else return contentSize + preferredSize;
    }

    public int getPreferredSizeMode() {
        return preferredSizeMode;
    }

    /**
	 * Returns the preferred size of the tag.
	 *
	 * @return the preferred size of the tag.
	 */
    public int getPreferredSize() {
        return preferredSize;
    }

    /**
	 * Returns the tag size excluding the padding.
	 * <br>
	 * Call {@link org.acid3lib.ID3v2Tag#recalculateSize()} to get an updated value.
	 *
	 * @return the tag size excluding any padding.
	 */
    public int getContentSize() {
        recalculateSizeIfNeccessary();
        return contentSize;
    }

    /**
	 * Returns the amount of padding being used by the tag.
	 * <br>
	 * Call {@link org.acid3lib.ID3v2Tag#recalculateSize()} to get an updated value.
	 * Equal to <code>getPreferredSize()-getPureTagSize()</code>.
	 *
	 * @return the amount of padding.
	 */
    public int getPaddingSize() {
        if (preferredSizeMode == ID3Constants.ABSOLUTE) {
            recalculateSizeIfNeccessary();
            return (contentSize < preferredSize ? preferredSize - contentSize : 0);
        } else {
            return preferredSize;
        }
    }

    /**
	 * Returns the spec currently used by this tag.
	 *
	 * @return the spec currently used.
	 */
    public ID3v2Spec getSpec() {
        return spec;
    }

    /**
	 * Returns the current spec policy.
	 *
	 * @return the current spec policy.
	 * @see #setSpecPolicy(int)
	 */
    public int getSpecPolicy() {
        return specPolicy;
    }

    /**
	 * Returns the version number of the current spec.
	 *
	 * @return the version number of the current spec.
	 */
    public int getVersion() {
        return spec.getVersion();
    }

    /**
	 * Returns the revision number of the current spec.
	 *
	 * @return the revision number of the current spec.
	 */
    public int getRevision() {
        return spec.getRevision();
    }

    /**
	 * Returns a string formatted 2.x.y, where <code>x</code> is the version of
	 * the current spec and <code>y</code> is the revision.
	 *
	 * @return the version string.
	 */
    public String getVersionString() {
        return spec.toString();
    }

    /**
	 * Returns whether or not the tag contains a frame of type
	 * <code>type</code>.
	 *
	 * @param type a frame type (see constants declared in {@link ID3v2Frame}).
	 * @return <code>true</code> if the tag contains a frame of type
	 * <code>type</code>, <code>false</code> otherwise.
	 */
    public boolean contains(int type) {
        ID3v2Frame frame = get(type, 0);
        if (frame != null) lastRequestedFrame = frame;
        return frame != null;
    }

    public boolean contains(ID3v2Frame frame) {
        return frames.contains(frame);
    }

    /**
	 * Returns the total amount of frames in the tag.
	 *
	 * @return the total amount of frames in the tag.
	 */
    public int getFrameCount() {
        return frames.size();
    }

    /**
	 * Returns the amount of frames of type <code>type</code> in the tag.
	 *
	 * @param type a frame type.
	 * @return the amount of frames of type <code>type</code> in the tag.
	 */
    public int getFrameCount(int type) {
        int count = 0;
        for (int i = 0; i < frames.size(); ++i) {
            ID3v2Frame current = (ID3v2Frame) frames.get(i);
            if (current.getType() == type) ++count;
        }
        return count;
    }

    /**
	 * This method is a convenience for <code>get(id,0)</code>.
	 *
	 * @param type a frame type.
	 * @return the frame or <code>null</code> if no such frame is found.
	 * @see ID3v2Tag#get(int,int)
	 */
    public ID3v2Frame get(int type) {
        if (lastRequestedFrame != null) if (lastRequestedFrame.getType() == type) return lastRequestedFrame;
        lastRequestedFrame = get(type, 0);
        return lastRequestedFrame;
    }

    /**
	 * Returns the <code>n</code>th frame of type <code>type</code> in this tag.
	 * The constants declared in {@link ID3v2Frame} should be used in order to
	 * get a specific frame.
	 *
	 * @param type a frame type.
	 * @param n
	 * @return the frame or <code>null</code> if no such frame is found.
	 */
    public ID3v2Frame get(int type, int n) {
        int j = 0;
        for (int i = 0; i < frames.size(); ++i) {
            ID3v2Frame current = (ID3v2Frame) frames.get(i);
            if (current.getType() == type) {
                if (n == j) return current;
                ++j;
            }
        }
        return null;
    }

    /**
	 * Returns the frame which is of type <code>type</code> and additionally
	 * has set the value of the <code>FrameProperty</code> of
	 * type <code>propertyType</code> to <code>value</code>.
	 *
	 * @param frameType a frame type (see constants in {@link ID3v2Frame}).
	 * @param propertyType a property type (see constants in {@link
	 * FrameProperty}).
	 * @param value the value that the according property must have.
	 * @return the frame or <code>null</code> if no such frame is found.
	 * @see FrameProperty
	 */
    public ID3v2Frame get(int frameType, int propertyType, byte value) {
        return get(frameType, propertyType, new byte[] { value });
    }

    /**
	 * Returns the frame which is of type <code>type</code> and additionally
	 * has set the value of the <code>FrameProperty</code> of
	 * type <code>propertyType</code> to <code>value</code>.
	 *
	 * @param frameType a frame type (see constants in {@link ID3v2Frame}).
	 * @param propertyType a property type (see constants in {@link
	 * FrameProperty}).
	 * @param value the value that the according property must have.
	 * @return the frame or <code>null</code> if no such frame is found.
	 * @see FrameProperty
	 */
    public ID3v2Frame get(int frameType, int propertyType, byte[] value) {
        try {
            return get(frameType, propertyType, new String(value, 0, value.length, "8859_1"));
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    /**
	 * Returns the frame which is of type <code>type</code> and additionally
	 * has set the value of the <code>FrameProperty</code> of
	 * type <code>propertyType</code> to <code>value</code>.
	 *
	 * @param frameType a frame type (see constants in {@link ID3v2Frame}).
	 * @param propertyType a property type (see constants in {@link
	 * FrameProperty}).
	 * @param value the value that the according property must have.
	 * @return the frame or <code>null</code> if no such frame is found.
	 * @see FrameProperty
	 */
    public ID3v2Frame get(int frameType, int propertyType, String value) {
        for (int i = 0; i < frames.size(); ++i) {
            ID3v2Frame current = (ID3v2Frame) frames.elementAt(i);
            if (current.getType() == frameType) if (current.get(propertyType).toString().equals(value)) return current;
        }
        return null;
    }

    /**
	 * Returns an iteration over all frames in the tag.
	 *
	 * @return the iteration.
	 */
    public Iterator frames() {
        return frames.iterator();
    }

    /**
	 * Returns an iteration over all frames of type <code>type</code> in the
	 * tag.
	 *
	 * @param type a frame type (see constants in {@link ID3v2Frame}).
	 * @return the iteration.
	 */
    public Iterator frames(int type) {
        final Vector v = new Vector(frames.size());
        for (Iterator i = frames.iterator(); i.hasNext(); ) {
            ID3v2Frame current = ((ID3v2Frame) i.next());
            if (current.getType() == type) v.add(current);
        }
        return new Iterator() {

            Vector vector;

            int index;

            boolean removed;

            {
                this.vector = v;
                this.index = 0;
                removed = false;
            }

            public boolean hasNext() {
                return (index < vector.size());
            }

            public Object next() throws NoSuchElementException {
                if (index >= vector.size()) throw new NoSuchElementException();
                removed = false;
                return vector.get(index++);
            }

            public void remove() throws IllegalStateException {
                if (removed || index <= 0) throw new IllegalStateException();
                ID3v2Tag.this.remove((ID3v2Frame) vector.get(index - 1));
                removed = true;
            }
        };
    }

    /**
	 * Returns whether or not this tag is to be considered an update of a
	 * previous version of the tag.
	 *
	 * @return <code>true</code> if the tag is to be considered an update of a
	 * previous version, <code>false</code> otherwise.
	 */
    public boolean isUpdate() {
        return update;
    }

    /**
	 * Returns whether or not the tag is to be considered experimental.
	 *
	 * @return <code>true</code> if the tag is to be considered experimental,
	 * <code>false</code> otherwise.
	 */
    public boolean isExperimental() {
        return experimental;
    }

    /**
	 * Returns whether or not a CRC32 is included in the binary tag.
	 *
	 * @return <code>true</code> if a CRC32 is included, <code>false</code>
	 * otherwise.
	 */
    public boolean getUseCRC() {
        return useCRC;
    }

    /**
	 * Returns whether or not tag-wide unsynchronisation is enabled.
	 *
	 * @return <code>true</code> tag-wide unsynchronisation is enabled,
	 * <code>false</code> otherwise.
	 */
    public boolean getUseUnsynchronisation() {
        return useUnsynchronisation;
    }

    public boolean getUseFooter() {
        return useFooter;
    }

    /**
	 * Returns whether or not this tag is restricted in some way.
	 *
	 * @return <code>true</code> if the tag is restricted, <code>false</code>
	 * otherwise.
	 * @see ID3v2Tag#getRestrictions()
	 */
    public boolean isRestricted() {
        return restrictions != null;
    }

    /**
	 * Returns the restrictions that affect this tag or <code>null</code> if
	 * there aren't any.
	 *
	 * @return the restrictions that affect this tag or <code>null</code> if
	 * there aren't any.
	 */
    public ID3v2Restrictions getRestrictions() {
        return restrictions;
    }

    /**
	 * Sets the current spec to be <code>newSpec</code> if the spec policy is
	 * <code>ID3Constants.FIXED_SPEC</code>, or sets the minimum spec to be
	 * <code>newSpec</code> if the spec policy is
	 * <code>ID3Constants.DYNAMIC_SPEC</code>.
	 *
	 * @param newSpec the actual spec or minimum spec to be used.
	 */
    public void setSpec(ID3v2Spec newSpec) {
        if (newSpec == null) throw new NullPointerException("newSpec == null");
        if (specPolicy == ID3Constants.DYNAMIC_SPEC) {
            this.minimumSpec = newSpec;
            if (this.spec.getVersion() < minimumSpec.getVersion()) {
                this.performSpecChange(newSpec);
            }
        } else {
            this.performSpecChange(newSpec);
        }
    }

    /**
	 * Sets the spec policy, which may either be <code>ID3Constants.FIXED_SPEC</code>
	 * or <code>ID3Constants.DYNAMIC_SPEC</code>.
	 * <br>
	 * It indicates how this tag handles requests to "upgrade" the spec which is
	 * currently used, i.e. change it to a spec with a higher version.
	 * <br>
	 * For example, if one tries to set a frame compressed using
	 * {@link ID3v2Frame#setUseCompression(boolean)}, although the current spec
	 * doesn't support compression, the frame will call {@link
	 * #requestSpecWhich(String)}, which then tries to upgrade the spec to a spec
	 * that supports compression.
	 * <br>
	 * However, if this is forbidden, i.e. the spec policy is equal to
	 * <code>ID3Constants.FIXED_SPEC</code>, this will fail and the result will
	 * be an {@link IllegalTagStateException}.
		*
		* @param policy the new spec policy.
		* @throws IllegalArgumentException if the spec policy is neither <code>ID3Constants.FIXED_SPEC</code>
	 * nor <code>ID3Constants.DYNAMIC_SPEC</code>.
		*/
    public void setSpecPolicy(int policy) throws IllegalArgumentException {
        if (policy != ID3Constants.FIXED_SPEC && policy != ID3Constants.DYNAMIC_SPEC) throw new IllegalArgumentException(String.valueOf(policy));
        this.specPolicy = policy;
    }

    public void setPreferredSize(int value) throws IllegalArgumentException {
        setPreferredSize(value, preferredSizeMode);
    }

    /**
	 * Sets the preferred size of the tag. The preferred size must either be
	 * {@link ID3Constants#RELATIVE} or {@link ID3Constants#ABSOLUTE}.
	 * <br>If <code>preferredSizeMode</code> is relative, than
	 * <code>value</code> bytes of padding are added to the tag.
	 * <br>If <code>preferredSizeMode</code> is absolute, than padding bytes are
	 * added until the tag's size is equal to <code>value</code>.
	 *
	 * @param value the preferred size of the tag
	 * @param preferredSizeMode indicates relative or absolute mode.
	 * @throws IllegalArgumentException if <code>preferredSizeMode</code>
	 * neither is {@link ID3Constants#RELATIVE} nor {@link
	 * ID3Constants#ABSOLUTE}.
	 */
    public void setPreferredSize(int value, int preferredSizeMode) throws IllegalArgumentException {
        if ((preferredSizeMode != ID3Constants.ABSOLUTE && preferredSizeMode != ID3Constants.RELATIVE) || (value < 0)) throw new IllegalArgumentException();
        this.preferredSize = value;
        this.preferredSizeMode = preferredSizeMode;
    }

    public void setPreferredSizeMode(int mode) throws IllegalArgumentException {
        setPreferredSize(preferredSize, mode);
    }

    /**
	 * Sets whether or not the tag is to be considered experimental.
	 *
	 * @param b if <code>true</code>, the experimental flag will be set.
	 * @throws IllegalTagStateException if the exoerimental flag isn't
	 * supported by the current spec and the spec policy is
	 * <code>ID3Constants.FIXED_SPEC</code>.
	 */
    public void setExperimental(boolean b) throws IllegalTagStateException {
        if (!spec.supportsTagIsExperimentalIndicator()) requestSpecWhich("supportsTagIsExperimentalIndicator");
        boolean oldValue = experimental;
        boolean newValue = b;
        if (oldValue != newValue) {
            this.experimental = newValue;
            changeSupport.firePropertyChange(EXPERIMENTAL_PROPERTY, oldValue, newValue);
        }
        this.sizeChanged = true;
    }

    /**
	 * Sets whether or not tag-wide unsynchronisation is used.
	 *
	 * @param newValue if true, the tag will be unsynchronised.
	 * @throws IllegalTagStateException if tag-wide unsynchronisation isn't
	 * supported by the current spec and the spec policy is <code>ID3Constants.
	 * FIXED_SPEC</code>.
	 * @see ID3v2Frame#getUseUnsynchronisation()
	 */
    public void setUseUnsynchronisation(boolean newValue) throws IllegalTagStateException {
        if (!spec.supportsTagUnsynchronisation()) requestSpecWhich("supportsTagUnsynchronisation");
        boolean oldValue = useUnsynchronisation;
        if (oldValue != newValue) {
            this.useUnsynchronisation = newValue;
            changeSupport.firePropertyChange(USE_UNSYNCHRONISATION_PROPERTY, oldValue, newValue);
        }
        if (spec.supportsFrameUnsynchronisation()) {
            for (Iterator i = frames.iterator(); i.hasNext(); ) {
                ID3v2Frame frame = (ID3v2Frame) i.next();
                frame.setUseUnsynchronisation(newValue);
            }
        }
        this.sizeChanged = true;
    }

    public void setUseFooter(boolean newValue) throws IllegalTagStateException {
        if (!spec.supportsTagFooter()) requestSpecWhich("supportsTagFooter");
        boolean oldValue = useFooter;
        if (oldValue != newValue) {
            this.useFooter = newValue;
            changeSupport.firePropertyChange(USE_FOOTER_PROPERTY, oldValue, newValue);
        }
        this.sizeChanged = true;
    }

    public void setRestrictions(ID3v2Restrictions restrictions) throws IllegalTagStateException {
        if (!spec.supportsTagRestrictions()) requestSpecWhich("supportsTagRestrictions");
        ID3v2Restrictions oldValue = this.restrictions;
        ID3v2Restrictions newValue = restrictions;
        if (oldValue == null || !oldValue.equals(newValue)) {
            this.restrictions = newValue;
            changeSupport.firePropertyChange(RESTRICTIONS_PROPERTY, oldValue, newValue);
        }
        this.sizeChanged = true;
    }

    public void setUseCRC(boolean b) throws IllegalTagStateException {
        if (!spec.supportsTagCRC()) requestSpecWhich("supportsTagCRC");
        boolean oldValue = useCRC;
        boolean newValue = b;
        if (oldValue != newValue) {
            this.useCRC = newValue;
            changeSupport.firePropertyChange(USE_CRC_PROPERTY, oldValue, newValue);
        }
        this.sizeChanged = true;
    }

    /**
	 * Indicates whether or not this tag is to be considered an update of a
	 * previous version found in the file/stream.<br>
	 * However, this flag is not automatically set and actually has no effect.
	 *
	 * @param b if <code>true</code>, the update flag will be set.
	 * @throws IllegalTagStateException if the update flag isn't supported by
	 * the current spec and the spec policy is
	 * <code>ID3Constants.FIXED_SPEC</code>.
	 */
    public void setUpdate(boolean b) throws IllegalTagStateException {
        if (!spec.supportsTagIsUpdateIndicator()) requestSpecWhich("supportsTagIsUpdateIndicator");
        boolean oldValue = update;
        boolean newValue = b;
        if (oldValue != newValue) {
            this.update = newValue;
            changeSupport.firePropertyChange(UPDATE_PROPERTY, oldValue, newValue);
        }
        this.sizeChanged = true;
    }

    private void performSpecChange(ID3v2Spec newSpec) {
        ID3v2Spec oldSpec = spec;
        this.spec = newSpec;
        for (Iterator i = frames.iterator(); i.hasNext(); ) {
            ID3v2Frame frame = (ID3v2Frame) i.next();
            if (!newSpec.supports(frame.getType())) {
                logger.info("ID3v2Tag::performSpecChange(ID3v2Spec): Frame " + frame.getID() + " isn't supported by " + newSpec + " and discarded.");
                i.remove();
            } else {
                frame.setSpec(newSpec);
                if (useUnsynchronisation && spec.supportsFrameUnsynchronisation()) frame.setUseUnsynchronisation(true);
            }
        }
        changeSupport.firePropertyChange(SPEC_PROPERTY, oldSpec, newSpec);
        this.sizeChanged = true;
    }

    /**
	 * Really ugly method. Used by the ID3v2Spec instances to notify the tag of
	 * its size after reading.
	 */
    void setSize(int size) {
        this.contentSize = size;
        this.sizeChanged = false;
    }

    void notifyFramePropertyChanged(ID3v2Frame frame, FrameProperty property, String oldValue, String newValue) {
        if (oldValue == null && newValue != null) sizeChanged = true;
    }

    void notifyFlagChanged(ID3v2Frame frame) {
        sizeChanged = true;
    }

    private static void copyData(final InputStream in, final File dest) throws FileNotFoundException, IOException {
        FileOutputStream fout = new FileOutputStream(dest);
        copyData(in, fout);
        fout.close();
    }

    private static void copyData(final File src, final File dest) throws FileNotFoundException, IOException {
        FileOutputStream fout = new FileOutputStream(dest);
        FileInputStream fin = new FileInputStream(src);
        copyData(fin, fout);
        fout.close();
        fin.close();
    }

    private static void copyData(final InputStream in, final OutputStream out) throws IOException {
        final byte[] buffer = new byte[0xffff];
        int nbytes;
        while (true) {
            nbytes = in.read(buffer, 0, buffer.length);
            if (nbytes == -1) break;
            out.write(buffer, 0, nbytes);
        }
    }

    /**
	 * Writes the tag to the {@link java.io.OutputStream} <code>output</code>.
	 *
	 * @param out the <code>OutputStream</code> to write to.
	 * @throws IOException if an I/O error occurs.
	 */
    public int write(OutputStream out) throws IOException {
        if (frames.size() == 0) throw new IOException("Tag does not have any frames");
        logger.debug("ID3v2Tag::write(OutputStream): " + "Writing tag. Calling spec.write(this, out)");
        return spec.write(this, out);
    }

    /**
	 * Writes the tag to the file <code>f</code>.
	 * <br>
	 * If the file doesn't exist, the file will be created and the tag will be
	 * written into it.
	 * <br>
	 * If the specified file already contains a tag, this tag fits into the old
	 * tag and <code>forcePreferredSize</code> is <code>false</code>, the method
	 * will set this tag's size equal to the size of the old tag using
	 * {@link ID3v2Tag#setPreferredSize(int, int)} and the old tag is
	 * overwritten. Old settings of preferred size are ignored in this case.
	 * <br>
	 * If <code>forcePreferredSize</code> is <code>true</code> and this tag
	 * doesn't fit into the old one, the old tag is removed and this tag is
	 * prepended to the file <code>f</code>.
	 * <br>
	 * If the specified file doesn't contain an ID3v2 tag, this tag is prepended
	 * to the beginning of the file <code>f</code>.
	 *
	 * @param dest the file to write to.
	 * @param forcePreferredSize indicates whether or not the method is allowed
	 * to ignore the preferred size of a tag in order to allow an old tag to be
	 * overwritten rather than creating a temporary file and afterwards discard
	 * it.
	 * @throws IOException if an I/O error occurs.
	 */
    public void write(File dest, boolean forcePreferredSize) throws IOException {
        int bytesRead;
        if (getFrameCount() == 0) {
            throw new IOException("Tag doesn't contain any frames");
        }
        if (!dest.exists()) {
            OutputStream fout = new BufferedOutputStream(new FileOutputStream(dest));
            write(fout);
            fout.close();
            return;
        }
        if (!dest.canWrite()) {
            throw new IOException("Can't write on file " + dest);
        }
        byte[] headerData = new byte[10];
        File tempFile;
        boolean canOverwritePresentTag = false;
        boolean foundTag;
        int tagSize;
        InputStream fin = new PushbackInputStream(new BufferedInputStream(new FileInputStream(dest)), 10);
        bytesRead = fin.read(headerData, 0, 10);
        Bytes.checkRead(bytesRead, 10, "ID3v2Tag::write(File,boolean)");
        ((PushbackInputStream) fin).unread(headerData, 0, headerData.length);
        foundTag = isTag(headerData, 0);
        recalculateSizeIfNeccessary();
        logger.debug("ID3v2Tag::write(File,boolean): " + "Writing tag to file " + dest + "; forcePreferredSize = " + forcePreferredSize);
        if (foundTag) {
            tagSize = (int) Bytes.convertLong(headerData, 7, 6, 4) + 10;
            canOverwritePresentTag = (tagSize >= contentSize && !forcePreferredSize) || (tagSize == getSize() && forcePreferredSize);
            if (canOverwritePresentTag) {
                logger.info("ID3v2Tag::write(File,boolean):" + "Old tag will be overwritten");
                int oldPreferredSize = getPreferredSize();
                setPreferredSize(tagSize, ID3Constants.ABSOLUTE);
                fin.close();
                RandomAccessFile fout = new RandomAccessFile(dest, "rw");
                ByteArrayOutputStream bout = new ByteArrayOutputStream(getSize());
                write(bout);
                fout.write(bout.toByteArray());
                fout.close();
                setPreferredSize(oldPreferredSize, ID3Constants.ABSOLUTE);
                return;
            } else {
                logger.info("ID3v2Tag::write(File,boolean):" + "Old tag will be discarded");
                byte[] tagData = new byte[tagSize];
                bytesRead = fin.read(tagData, 0, tagData.length);
                Bytes.checkRead(bytesRead, tagData.length, "ID3v2Tag::write(File,boolean)");
                tagData = null;
            }
        }
        tempFile = File.createTempFile(dest.getName(), ".tmp", dest.getParentFile());
        copyData(fin, tempFile);
        fin.close();
        fin = new BufferedInputStream(new FileInputStream(tempFile));
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(dest);
            write(fout);
            copyData(fin, fout);
        } catch (IOException e) {
            fin.close();
            fout.close();
            boolean couldDelete = dest.delete();
            boolean couldRename = tempFile.renameTo(dest);
            logger.error("ID3v2Tag::write(File,boolean): " + "couldDelete: " + couldDelete + "; couldRename: " + couldRename, e);
            throw e;
        }
        fin.close();
        fout.close();
        if (!tempFile.delete()) tempFile.deleteOnExit();
    }

    /**
	 * Removes the ID3v2 tag found in the file <code>f</code>.
	 *
	 * @param f the file containing the tag to be removed.
	 * @return <code>true</code> if a tag was found and removed,
	 * <code>false</code> otherwise.
	 * @throws FileNotFoundException if the file <code>f</code> doesn't exist.
	 * @throws IOException if an I/O error occurs.
	 */
    public static boolean removeTag(File f) throws FileNotFoundException, IOException {
        byte[] headerData = new byte[10];
        byte[] buffer = new byte[0xffff];
        File tempFile;
        InputStream fin;
        int bytesRead;
        fin = new BufferedInputStream(new FileInputStream(f));
        bytesRead = fin.read(headerData, 0, 10);
        Bytes.checkRead(bytesRead, 10, "ID3v2Tag::removeTag(File)");
        if (!isTag(headerData, 0)) {
            fin.close();
            return false;
        }
        int tagSize = (int) Bytes.convertLong(headerData, 7, 6, 4) + 10;
        byte[] tagData = new byte[tagSize - 10];
        bytesRead = fin.read(tagData, 0, tagData.length);
        Bytes.checkRead(bytesRead, tagData.length, "ID3v2Tag::removeTag(File)");
        tagData = null;
        tempFile = File.createTempFile(f.getName(), ".tmp", f.getParentFile());
        try {
            copyData(fin, tempFile);
            fin.close();
        } catch (IOException e) {
            if (!tempFile.delete()) tempFile.deleteOnExit();
            logger.warn("ID3v2Tag::removeTag(File): " + "IOException occured during temporary file creation.", e);
            throw e;
        }
        boolean couldUseSimpleDelete = false;
        if (f.delete()) {
            logger.debug("ID3v2Tag::removeTag(File): Could delete original file.");
            if (tempFile.renameTo(f)) {
                logger.debug("ID3v2Tag::removeTag(File): " + "Could rename temporary file to original file." + "Tag removal was successful using 1 copy.");
                couldUseSimpleDelete = true;
            }
        }
        if (!couldUseSimpleDelete) {
            try {
                copyData(tempFile, f);
            } catch (IOException e) {
                logger.warn("ID3v2Tag::removeTag(File): " + "Could neither rename the temporary file to the " + "original file, nor copy the data from the temporary file." + "Data loss is possible!!", e);
                throw e;
            }
            logger.debug("ID3v2Tag::removeTag(File): Could copy from temporary file to original file. " + "Tag removal was successful using 2 copies.");
            if (!tempFile.delete()) tempFile.deleteOnExit();
        }
        return true;
    }

    public static ID3v2Tag readTag(String path) throws FileNotFoundException, ID3v2NotFoundException, ID3v2UnsupportedVersionException, ID3v2ParseException, IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(path));
        ID3v2Tag tag = readTag(in);
        in.close();
        return tag;
    }

    /**
	 * Reads an ID3v2 tag from the file <code>file</code>.
	 *
	 * @param file the file to read from.
	 * @return the tag read from the file.
	 * @throws FileNotFoundException if the file <code>file</code> doesn't
	 * exist.
	 * @throws ID3v2NotFoundException if no ID3v2 tag is found in the file.
	 * @throws ID3v2UnsupportedVersionException if no spec for the tag's version
	 * is registered.
	 * @throws ID3v2ParseException if the input data is malformed.
	 * @throws IOException if an I/O error occurs.
	 */
    public static ID3v2Tag readTag(File file) throws FileNotFoundException, ID3v2NotFoundException, ID3v2UnsupportedVersionException, ID3v2ParseException, IOException {
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        RandomAccessFile raFile = new RandomAccessFile(file, "r");
        raFile.seek(file.length() - 10);
        byte[] code = new byte[3];
        raFile.read(code);
        if (new String(code, "ISO-8859-1").equals("3DI")) {
            byte[] sizeDescriptor = new byte[4];
            raFile.skipBytes(2);
            raFile.read(sizeDescriptor);
            int tagSize = (int) Bytes.convertLong(sizeDescriptor, 7, 0, 4);
            long bytesToSkip = file.length() - 10 - tagSize;
            in.skip(bytesToSkip);
        }
        ID3v2Tag tag = readTag(in);
        in.close();
        return tag;
    }

    /**
	 * Reads an <code>ID3v2Tag</code> from the <code>InputStream</code>
	 * <code>in</code>.<br> For performance reasons, it is recommended to use an
	 * buffered input stream. <code>in</code> must point to the beginning of the
	 * tag.
	 *
	 * @param in the <code>InputStream</code> to read from.
	 * @return the tag read from the input stream.
	 * @throws ID3v2NotFoundException if no tag is found at the current position
	 * of the <code>InputStream</code>.
	 * @throws ID3v2UnsupportedVersionException if no spec for the tag's version
	 * is registered.
	 * @throws ID3v2NotFoundException if no ID3v2 tag is found.
	 * @throws ID3v2ParseException if the input data is malformed.
	 * @throws IOException if an I/O error occurs.
	 */
    public static ID3v2Tag readTag(InputStream in) throws ID3v2NotFoundException, ID3v2UnsupportedVersionException, ID3v2ParseException, IOException {
        logger.debug("ID3v2Tag::readTag(InputStream): Reading tag from InputStream.");
        final byte[] headerData = new byte[10];
        in = new PushbackInputStream(in, 10);
        in.read(headerData, 0, 10);
        ((PushbackInputStream) in).unread(headerData, 0, 10);
        if (new String(headerData, 0, 3).equals("ID3") == false) throw new ID3v2NotFoundException("No ID3v2 tag found in stream");
        int version = headerData[3];
        int revision = headerData[4];
        ID3v2Spec spec = ID3v2Spec.getSpec(version, revision);
        if (spec == null) throw new ID3v2UnsupportedVersionException("2." + version + "." + revision);
        logger.debug("ID3v2Tag::readTag(InputStream): " + "Found tag version " + spec.toString());
        ID3v2Tag tag = spec.readTag(in);
        return tag;
    }

    /**
	 * Tries to instantiate a {@link FrameReader}, which reads the frames of the tag
	 * found in the <code>InputStream</code> <code>in</code>.
	 *
	 * @param in
	 * @return a frame reader.
	 * @throws ID3v2NotFoundException if no ID3v2 tag is found.
	 * @throws ID3v2ParseException if the input data is malformed.
	 * @throws IOException if an I/O error occurs.
	 * @see FrameReader
	 */
    public static FrameReader readFrames(InputStream in) throws ID3v2NotFoundException, ID3v2ParseException, IOException {
        final byte[] headerData = new byte[10];
        in = new PushbackInputStream(in, 10);
        in.read(headerData, 0, 10);
        ((PushbackInputStream) in).unread(headerData, 0, 10);
        if (new String(headerData, 0, 3).equals("ID3") == false) throw new ID3v2NotFoundException("No ID3v2 tag found in stream");
        int version = headerData[3];
        int revision = headerData[4];
        ID3v2Spec spec = ID3v2Spec.getSpec(version, revision);
        return spec.getFrameReader(in);
    }

    public static void enableDebugLogging() {
    }
}
