package gnu.inet.encoding;

public class NFKC {

    /**
   * Applies NFKC normalization to a string.
   *
   * @param in The string to normalize.
   * @return An NFKC normalized string.
   */
    public static String normalizeNFKC(String in) {
        StringBuffer out = new StringBuffer();
        for (int i = 0; i < in.length(); i++) {
            char code = in.charAt(i);
            if (code >= 0xAC00 && code <= 0xD7AF) {
                out.append(decomposeHangul(code));
            } else {
                int index = decomposeIndex(code);
                if (index == -1) {
                    out.append(code);
                } else {
                    out.append(DecompositionMappings.m[index]);
                }
            }
        }
        canonicalOrdering(out);
        int last_cc = 0;
        int last_start = 0;
        for (int i = 0; i < out.length(); i++) {
            int cc = combiningClass(out.charAt(i));
            if (i > 0 && (last_cc == 0 || last_cc != cc)) {
                char a = out.charAt(last_start);
                char b = out.charAt(i);
                int c = compose(a, b);
                if (c != -1) {
                    out.setCharAt(last_start, (char) c);
                    out.deleteCharAt(i);
                    i--;
                    if (i == last_start) {
                        last_cc = 0;
                    } else {
                        last_cc = combiningClass(out.charAt(i - 1));
                    }
                    continue;
                }
            }
            if (cc == 0) {
                last_start = i;
            }
            last_cc = cc;
        }
        return out.toString();
    }

    /**
   * Returns the index inside the decomposition table, implemented
   * using a binary search.
   *
   * @param c Character to look up.
   * @return Index if found, -1 otherwise.
   */
    static int decomposeIndex(char c) {
        int start = 0;
        int end = DecompositionKeys.k.length / 2;
        while (true) {
            int half = (start + end) / 2;
            int code = DecompositionKeys.k[half * 2];
            if (c == code) {
                return DecompositionKeys.k[half * 2 + 1];
            }
            if (half == start) {
                return -1;
            } else if (c > code) {
                start = half;
            } else {
                end = half;
            }
        }
    }

    /**
   * Returns the combining class of a given character.
   *
   * @param c The character.
   * @return The combining class.
   */
    static int combiningClass(char c) {
        int h = c >> 8;
        int l = c & 0xff;
        int i = CombiningClass.i[h];
        if (i > -1) {
            return CombiningClass.c[i][l];
        } else {
            return 0;
        }
    }

    /**
   * Rearranges characters in a stringbuffer in order to respect the
   * canonical ordering properties.
   *
   * @param The StringBuffer to rearrange.
   */
    static void canonicalOrdering(StringBuffer in) {
        boolean isOrdered = false;
        while (!isOrdered) {
            isOrdered = true;
            int lastCC = combiningClass(in.charAt(0));
            for (int i = 0; i < in.length() - 1; i++) {
                int nextCC = combiningClass(in.charAt(i + 1));
                if (nextCC != 0 && lastCC > nextCC) {
                    for (int j = i + 1; j > 0; j--) {
                        if (combiningClass(in.charAt(j - 1)) <= nextCC) {
                            break;
                        }
                        char t = in.charAt(j);
                        in.setCharAt(j, in.charAt(j - 1));
                        in.setCharAt(j - 1, t);
                        isOrdered = false;
                    }
                    nextCC = lastCC;
                }
                lastCC = nextCC;
            }
        }
    }

    /**
   * Returns the index inside the composition table.
   *
   * @param a Character to look up.
   * @return Index if found, -1 otherwise.
   */
    static int composeIndex(char a) {
        if (a >> 8 >= Composition.composePage.length) {
            return -1;
        }
        int ap = Composition.composePage[a >> 8];
        if (ap == -1) {
            return -1;
        }
        return Composition.composeData[ap][a & 0xff];
    }

    /**
   * Tries to compose two characters canonically.
   *
   * @param a First character.
   * @param b Second character.
   * @return The composed character or -1 if no composition could be
   * found.
   */
    static int compose(char a, char b) {
        int h = composeHangul(a, b);
        if (h != -1) {
            return h;
        }
        int ai = composeIndex(a);
        if (ai >= Composition.singleFirstStart && ai < Composition.singleSecondStart) {
            if (b == Composition.singleFirst[ai - Composition.singleFirstStart][0]) {
                return Composition.singleFirst[ai - Composition.singleFirstStart][1];
            } else {
                return -1;
            }
        }
        int bi = composeIndex(b);
        if (bi >= Composition.singleSecondStart) {
            if (a == Composition.singleSecond[bi - Composition.singleSecondStart][0]) {
                return Composition.singleSecond[bi - Composition.singleSecondStart][1];
            } else {
                return -1;
            }
        }
        if (ai >= 0 && ai < Composition.multiSecondStart && bi >= Composition.multiSecondStart && bi < Composition.singleFirstStart) {
            char[] f = Composition.multiFirst[ai];
            if (bi - Composition.multiSecondStart < f.length) {
                char r = f[bi - Composition.multiSecondStart];
                if (r == 0) {
                    return -1;
                } else {
                    return r;
                }
            }
        }
        return -1;
    }

    /**
   * Entire hangul code copied from:
   * http://www.unicode.org/unicode/reports/tr15/
   *
   * Several hangul specific constants
   */
    static final int SBase = 0xAC00;

    static final int LBase = 0x1100;

    static final int VBase = 0x1161;

    static final int TBase = 0x11A7;

    static final int LCount = 19;

    static final int VCount = 21;

    static final int TCount = 28;

    static final int NCount = VCount * TCount;

    static final int SCount = LCount * NCount;

    /**
   * Decomposes a hangul character.
   *
   * @param s A character to decompose.
   * @return A string containing the hangul decomposition of the input
   * character. If no hangul decomposition can be found, a string
   * containing the character itself is returned.
   */
    static String decomposeHangul(char s) {
        int SIndex = s - SBase;
        if (SIndex < 0 || SIndex >= SCount) {
            return String.valueOf(s);
        }
        StringBuffer result = new StringBuffer();
        int L = LBase + SIndex / NCount;
        int V = VBase + (SIndex % NCount) / TCount;
        int T = TBase + SIndex % TCount;
        result.append((char) L);
        result.append((char) V);
        if (T != TBase) result.append((char) T);
        return result.toString();
    }

    /**
   * Composes two hangul characters.
   *
   * @param a First character.
   * @param b Second character.
   * @return Returns the composed character or -1 if the two
   * characters cannot be composed.
   */
    static int composeHangul(char a, char b) {
        int LIndex = a - LBase;
        if (0 <= LIndex && LIndex < LCount) {
            int VIndex = b - VBase;
            if (0 <= VIndex && VIndex < VCount) {
                return SBase + (LIndex * VCount + VIndex) * TCount;
            }
        }
        int SIndex = a - SBase;
        if (0 <= SIndex && SIndex < SCount && (SIndex % TCount) == 0) {
            int TIndex = b - TBase;
            if (0 <= TIndex && TIndex <= TCount) {
                return a + TIndex;
            }
        }
        return -1;
    }
}
