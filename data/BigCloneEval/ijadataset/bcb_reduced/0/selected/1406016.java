package de.spieleck.util;

/**
 * A simple implementation of a ternary trie to index an array of
 * CharSequence objects.
 *
 * @author frank nestel
 * @author $Author: nestefan $
 * @version $Revision: 2 $ $Date: 2006-03-27 23:00:21 +0200 (Mo, 27 Mrz 2006) $ $Author: nestefan $
 */
public class CharTrieBase implements CharTrie {

    private static final CharTrieBase TRIE_END = new CharTrieBase();

    private CharTrieBase() {
    }

    public CharTrieBase getLeft() {
        return null;
    }

    public CharTrieBase getCenter() {
        return null;
    }

    public CharTrieBase getRight() {
        return null;
    }

    /**
   * Traverse to the subtrie of the current trie,
   * guided by the presented character.
   */
    public CharTrie subtrie(char c) {
        CharTrieBase current = this.getCenter();
        while (current != null) {
            char splitChar = current.getSplit();
            if (c < splitChar) current = current.getLeft(); else if (c > splitChar) current = current.getRight(); else return current;
        }
        return null;
    }

    /**
   * Traverse to the subtrie of the current trie,
   * guided by the presented CharSequence.
   */
    public CharTrie subtrie(CharSequence chs) {
        int i = 0;
        CharTrieBase current = this;
        while (i < chs.length() && current != null) {
            current = (CharTrieBase) current.subtrie(charAt(chs, i++));
        }
        return current;
    }

    public int getId() {
        return NO_INDEX;
    }

    public char getSplit() {
        return END_CHAR;
    }

    /** 
   * A simple visualisation of a Trie.
   */
    public void print() {
        print("", Integer.MAX_VALUE, this, "");
    }

    /** 
   * A simple visualisation of a Trie.
   */
    public static void print(String indent, int w, CharTrieBase trie, String seq) {
        if (--w <= 0) return;
        if (trie == null) return;
        if (trie.getSplit() == END_CHAR) {
            System.out.println(indent + "<" + trie.getSplit() + ">[" + seq + "]");
        } else {
            System.out.println(indent + "<" + trie.getSplit() + ">");
            print(indent + "l", w, trie.getLeft(), seq);
            print(indent + " c", w, trie.getCenter(), seq + trie.getSplit());
            print(indent + "r", w, trie.getRight(), seq);
        }
    }

    /**
   * Create a CharTrie for a sorted array of CharSequence objects.
   */
    public static CharTrie createTrie(CharSequence[] seqs) {
        return new ForwardTree(NO_INDEX, END_CHAR, createTrieInternal(seqs, 0, 0, seqs.length));
    }

    /**
   * Create a CharTrie for a sorted array of CharSequence objects.
   */
    public static CharTrie createTrie(CharSequence[] seqs, int pos, int start, int end) {
        return new ForwardTree(NO_INDEX, END_CHAR, createTrieInternal(seqs, pos, start, end));
    }

    /**
   * Create a CharTrie for a partial sorted array of CharSequence objects
   * starting at a certain character position.
   * This method employs kind of a "cut at center" strategie which is
   * known to be simple and close to optimal.
   */
    private static CharTrieBase createTrieInternal(CharSequence[] seqs, int pos, int start, int end) {
        if (start >= end) return null;
        int mid = (start + end) / 2;
        char split = seqs[mid].charAt(pos);
        int goRight = mid;
        while (goRight < end && charAt(seqs[goRight], pos) == split) goRight++;
        int goLeft = mid;
        while (goLeft > start && charAt(seqs[goLeft - 1], pos) == split) goLeft--;
        int goLeft2 = goLeft;
        int id = NO_INDEX;
        if (seqs[goLeft].length() == pos + 1) {
            id = goLeft;
            goLeft2++;
        }
        if (start < goLeft || goRight < end) {
            return new FullTree(id, split, createTrieInternal(seqs, pos, start, goLeft), createTrieInternal(seqs, pos + 1, goLeft2, goRight), createTrieInternal(seqs, pos, goRight, end));
        } else {
            return new ForwardTree(start, split, createTrieInternal(seqs, pos + 1, goLeft2, goRight));
        }
    }

    private static class ForwardTree extends CharTrieBase {

        /** The split character of this node */
        private char split;

        /** The central subtrie */
        private CharTrieBase center;

        /** The indexed id in the primary array */
        private int id = NO_INDEX;

        public ForwardTree(int id, char split, CharTrieBase center) {
            this.id = id;
            this.split = split;
            this.center = center;
        }

        public int getId() {
            return id;
        }

        public char getSplit() {
            return split;
        }

        public CharTrieBase getCenter() {
            return center;
        }
    }

    private static class FullTree extends ForwardTree {

        /** The left subtrie */
        private CharTrieBase left;

        /** The right subtrie */
        private CharTrieBase right;

        public FullTree(int id, char split, CharTrieBase left, CharTrieBase center, CharTrieBase right) {
            super(id, split, center);
            this.left = left;
            this.right = right;
        }

        public CharTrieBase getLeft() {
            return left;
        }

        public CharTrieBase getRight() {
            return right;
        }
    }

    /** 
   * Helper: A "end safe" charAt operation.
   */
    public static final char charAt(CharSequence cs, int pos) {
        if (pos < cs.length()) return cs.charAt(pos);
        return END_CHAR;
    }

    /**
   * char to char to mask END_CHAR.
   */
    public static char sc(char c) {
        return c == END_CHAR ? '.' : c;
    }
}
