package memodivx.memolution;

import java.util.ArrayList;

public class SortedKeywordsList {

    private ArrayList list;

    /**
     * Build a new empty SortefKeywordsList
     */
    public SortedKeywordsList() {
        list = new ArrayList();
    }

    /**
     * add and element if he didn't exist and returns it PRECONDITION: the
     * keyword contains no invalid character POSTCONDITION : an object Keyword
     * is added(if necessary) and return which have for keyword the parameter
     * COMPLEXITY : O(log(n)) where n is the number of element in the base
     * 
     * @param keyword
     *            the string of the keyword to add
     * @return the link to the associated Keyword object
     */
    public Keyword addAndGet(String keyword, char type) {
        if (list.size() == 0) {
            Keyword k = new Keyword(keyword);
            list.add(k);
            return k;
        }
        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (getKeywordString(mid).compareTo(keyword) < 0) left = mid + 1; else if (getKeywordString(mid).compareTo(keyword) > 0) right = mid - 1; else {
                if (!get(mid).hasGenre(type)) get(mid).addGenre(type);
                return get(mid);
            }
        }
        Keyword k = new Keyword(keyword, type);
        if (left >= list.size()) list.add(k); else list.add(left, k);
        return k;
    }

    /**
     * COMPLEXITY: O(log(n)) where n is the number of keywords in the list
     * 
     * @param keywords
     *            the one we are looking at
     * @return the unique keyword object (if he exist) that have for keyword
     *         the parameter
     */
    public Keyword get(String keywords) {
        int left = 0;
        int right = list.size() - 1;
        while (left <= right) {
            int mid = (left + right) / 2;
            if (getKeywordString(mid).compareTo(keywords) < 0) left = mid + 1; else if (getKeywordString(mid).compareTo(keywords) > 0) right = mid - 1; else return get(mid);
        }
        return null;
    }

    /**
     * @param index
     *            the position in the list
     * @return the keyword object at the position given in parameter
     */
    public Keyword get(int index) {
        return (Keyword) list.get(index);
    }

    /**
     * @param index
     *            the position in the list
     * @return the keyword of the keyword object at the position given in
     *         parameter
     */
    public String getKeywordString(int index) {
        return get(index).getKeyword();
    }

    /**
     * @return the size of the list
     */
    public int size() {
        return list.size();
    }
}
