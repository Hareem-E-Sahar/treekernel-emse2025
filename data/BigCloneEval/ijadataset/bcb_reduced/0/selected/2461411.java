package org.tokaf;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import org.tokaf.algorithm.Algorithm;
import org.tokaf.bestcolumnfinder.SimpleColumnFinder;
import org.tokaf.datasearcher.DataModifier;
import org.tokaf.datasearcher.DataSearcher;
import org.tokaf.datasearcher.MemSearcher;
import org.tokaf.datasearcher.SesameRepositorySearcher;
import org.tokaf.datasearcher.SesameSearcher;
import org.tokaf.normalizer.HashMapNormalizer;
import org.tokaf.normalizer.Normalizer;
import org.tokaf.rater.Rater;
import org.tokaf.rater.WeightAverage;
import org.tokaf.rater.WeightRater;

/**
 * <p> Class MultipleRatings performs the multi-user decision problem. It
 * contains various functions to perform the actions to help finding the
 * solution of top k problem in the multiple user rated data. </p> <p> Copyright
 * (c) 2006 </p>
 * @author Alan Eckhardt
 * @version 1.0
 */
public class MultipleRatings {

    public RatingsContainer rc;

    private String namespace;

    private QueryFinder queryFinder;

    private DataSearcher master;

    private DataModifier masterModifier;

    String algorithmName;

    int count;

    WeightRater rater;

    UserNormalizers userNormalizers;

    public MultipleRatings(DataSearcher master, DataModifier masterModifier, QueryFinder queryFinder, String algorithmName, int count) {
        this(master, masterModifier, queryFinder, "http://www.muaddib.wz.cz/", algorithmName, count);
    }

    public MultipleRatings(DataSearcher master, DataModifier masterModifier, QueryFinder queryFinder, String namespace, String algorithmName, int count) {
        this(master, masterModifier, new WeightAverage(), queryFinder, namespace, algorithmName, count);
    }

    public MultipleRatings(DataSearcher master, DataModifier masterModifier, WeightRater rater, QueryFinder queryFinder, String namespace, String algorithmName, int count) {
        this.master = master;
        this.masterModifier = masterModifier;
        this.queryFinder = queryFinder;
        this.namespace = namespace;
        this.algorithmName = algorithmName;
        this.count = count;
        this.userNormalizers = new UserNormalizers(queryFinder, master);
        this.rater = rater;
    }

    protected String checkURI(String string) {
        if (string == null) return null;
        URI a = java.net.URI.create(string);
        if (a.getAuthority() == null) return namespace + string; else return string;
    }

    /**
	 * <p> getAlg </p> Returns the algorithm of name <i>algorithmName</i>, with
	 * the dataSearchers and rater passed as argument.
	 * @param data DataSearcher[]
	 * @param rater Rater
	 * @return Algorithm
	 * @throws ClassNotFoundException, IllegalArgumentException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 */
    private Algorithm getAlg(DataSearcher[] data, Rater r) throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
        Algorithm alg = null;
        Class c = Class.forName(algorithmName);
        Constructor[] a = c.getConstructors();
        Object par[] = new Object[5];
        par[0] = data;
        par[1] = r;
        par[2] = new SimpleColumnFinder();
        par[3] = namespace;
        par[4] = new Integer(count);
        alg = (Algorithm) a[0].newInstance(par);
        return alg;
    }

    /**
	 * <p> getHashMap </p> Transform arraylist of TopKElements into Hashmap. The
	 * key is the name of TopKElement and the value is the element itself.
	 * @param al ArrayList
	 * @return HashMap
	 */
    protected HashMap<Object, Double> getHashMap(ArrayList al) {
        HashMap<Object, Double> hm = new HashMap<Object, Double>();
        for (int j = 0; j < al.size(); j++) hm.put(((TopKElement) al.get(j)).name, new Double(((TopKElement) al.get(j)).rating));
        return hm;
    }

    /**
	 * <p> getPreferencesOnClass </p> Returns user preferences on specified
	 * class. The returned array is sorted to match the order of parameter
	 * ratings.
	 * @param ratings String[]
	 * @param type String
	 * @return double[]
	 */
    protected double[] getPreferencesOnClass(String className, String[] ratings) {
        double weights[] = new double[ratings.length];
        UserRatings ur = new UserRatings(namespace, queryFinder, master, masterModifier);
        for (int i = 0; i < ratings.length; i++) {
            ur.setRatingNames(ratings[i], null);
            try {
                weights[i] = ur.getUserRatingOnEntity(className);
            } catch (Exception ex) {
                weights[i] = 0;
            }
        }
        return weights;
    }

    /**
	 * Finds all names of users in database, using
	 * RatingsContainer.userClassName to specify the class.
	 * @return String[]
	 */
    public String[] getUsers() {
        ArrayList<String> params = new ArrayList<String>();
        params.add(checkURI(RatingsContainer.userClassName));
        params.add("");
        String usersQuery = queryFinder.getQuery("GetListOfTypedEntities", 1, params);
        DataSearcher data = (DataSearcher) master.clone();
        data.initSearch(usersQuery);
        ArrayList<String> al = new ArrayList<String>();
        while (data.hasNext()) {
            String name = data.getField(0).toString();
            al.add(name);
            if (data.advance() == -1) break;
        }
        String s[] = new String[al.size()];
        for (int i = 0; i < al.size(); i++) s[i] = al.get(i);
        return s;
    }

    /**
	 * <p> getRatings </p> Returns the ratings in the database. Name of ratings
	 * is specified in the constructor. Objects are connected to <i>type</i> by
	 * predicate <i>predicate</i>.
	 * @return String[]
	 */
    public String[] getRatings(boolean computed) {
        ArrayList<String> params = new ArrayList<String>();
        if (computed) params.add(checkURI(RatingsContainer.computedRatingName)); else params.add(checkURI(RatingsContainer.ratingName));
        params.add("");
        String predicateQuery = queryFinder.getQuery("GetListOfSubproperties", 1, params);
        DataSearcher predData = (DataSearcher) master.clone();
        predData.initSearch(predicateQuery);
        ArrayList<String> al = new ArrayList<String>();
        String temp;
        if (computed) {
            temp = checkURI(RatingsContainer.computedRatingName);
        } else {
            temp = checkURI(RatingsContainer.ratingName);
        }
        while (predData.hasNext()) {
            if (!temp.equals(predData.getField(0).toString())) al.add(predData.getField(0).toString());
            if (predData.advance() == -1) break;
        }
        String s[] = new String[al.size()];
        for (int i = 0; i < al.size(); i++) s[i] = al.get(i);
        return s;
    }

    /**
	 * <p> getSubTypesSearcher </p> Returns the searcher, which returns in each
	 * row - <p> at first position the name of object. Object is of type <i>type</i>.
	 * </p> <p> at every other position i the name of object. Object is
	 * connected to the first object by predicate[i]. </p>
	 * @param type String
	 * @param predicates String[]
	 * @return SesameRepositorySearcher
	 */
    protected DataSearcher getSubTypesSearcher(String type, String[] predicates) {
        ArrayList<String> params = new ArrayList<String>();
        params.add(checkURI(type));
        for (int i = 0; i < predicates.length; i++) params.add(checkURI(predicates[i]));
        for (int i = params.size(); i < 5; i++) params.add(namespace);
        String tripQuery = queryFinder.getQuery("GetListOfSubjectsObjects", 1, params);
        DataSearcher places = (DataSearcher) master.clone();
        places.initSearch(tripQuery);
        return places;
    }

    /**
	 * <p> getTypeByGlobalPreferences </p> First, gets the name of ratings in
	 * the database and than calls the function getTypeByGlobalPreferences to
	 * obtain ordered type.
	 * @param type String
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return int
	 */
    public Algorithm getTypeByGlobalPreferences(String type) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String[] ratings = getRatings(true);
        return getTypeByGlobalPreferences(type, null, ratings);
    }

    /**
	 * <p> getTypeByGlobalPreferences </p> Returns standard top k algorithm,
	 * which computes the top k objects of the type <i>type</i> that most
	 * satysfies every user.
	 * @param type String
	 * @param ratings String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return Algorithm
	 */
    public Algorithm getTypeByGlobalPreferences(String type, Normalizer[] norm, String[] ratings) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        double[] weights = getPreferencesOnClass(type, ratings);
        if (norm == null) norm = userNormalizers.getUserNormalizers(transformUserRatingIntoUserName(ratings), type);
        DataSearcher[] data = getTypeByRatings(type, norm, ratings, false);
        WeightAverage r = new WeightAverage(ratings, weights);
        Algorithm a = getAlg(data, r);
        a.run();
        return a;
    }

    /**
	 * <p> getTypeByOnePreference </p> Returns one type ordered by one rating.
	 * @param type String
	 * @param ratings String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return ArrayList
	 */
    protected ArrayList getTypeByOnePreference(String type, Normalizer[] norm, String rating, boolean optional) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        DataSearcher[] data = getTypeByRatings(type, norm, new String[] { rating }, optional);
        ArrayList res = new ArrayList();
        WeightAverage r = new WeightAverage(new String[] { rating }, new double[] { 1 });
        Algorithm a = getAlg(data, r);
        res = a.run();
        return res;
    }

    /**
	 * <p> getTypeByRatings </p> Returns user preferences on specified class.
	 * The returned array is sorted to match the order of parameter ratings. If
	 * optional is specified, even NULL values are returned. This means also,
	 * that every object of type <i>type</i> will be returned.
	 * @param ratings String[]
	 * @param type String
	 * @param optional boolean
	 * @return DataSearcher[]
	 */
    protected DataSearcher[] getTypeByRatings(String type, Normalizer[] norm, String[] ratings, boolean optional) {
        DataSearcher[] data = new DataSearcher[ratings.length];
        for (int i = 0; i < ratings.length; i++) {
            data[i] = (DataSearcher) master.clone();
            if (norm != null && norm[i] != null) data[i].setNormalizer(norm[i]);
            ArrayList<String> params = new ArrayList<String>();
            params.add(ratings[i]);
            params.add(checkURI(type));
            String tripQuery = queryFinder.getQuery("GetListOfTypedEntities", 2, params);
            if (optional) {
                tripQuery = queryFinder.getQuery("GetListOfTypedEntitiesOptional", 1, params);
            }
            data[i].initSearch(tripQuery);
            if (data[i] instanceof SesameSearcher) ((SesameSearcher) data[i]).sort(2);
        }
        return data;
    }

    /**
	 * <p> getTypeBySubTypes </p> First transform the arraylists into hashmaps
	 * and than calls the function getTypeBySubTypes to compute top K sorted
	 * objects.
	 * @param type DataSearcher
	 * @param r Rater
	 * @param subTypes ArrayList[]
	 * @return ArrayList
	 */
    protected ArrayList<TopKElement> getTypeBySubTypes(DataSearcher type, Rater r, ArrayList[] subTypes) {
        HashMap[] hm = new HashMap[subTypes.length];
        for (int i = 0; i < subTypes.length; i++) {
            hm[i] = new HashMap();
            for (int j = 0; j < subTypes[i].size(); j++) hm[i].put(((TopKElement) subTypes[i].get(j)).name, new Double(((TopKElement) subTypes[i].get(j)).rating));
        }
        return getTypeBySubTypes(type, r, hm);
    }

    /**
	 * <p> getTypeBySubTypes </p> Returns the arraylist of objects of type
	 * <i>type</i>, sorted by rating. Rating is computed from the ratings of
	 * subtypes. In the hashmap subTypes is the ratings of the subtypes, the key
	 * is the URI representing the object and value is double value. Then, using
	 * HashMapNormalizer, the standard TopKAlgorithm is used to compute the top
	 * k objects.
	 * @param type DataSearcher
	 * @param r Rater
	 * @param subTypes HashMap[]
	 * @return ArrayList
	 */
    protected ArrayList<TopKElement> getTypeBySubTypes(DataSearcher type, Rater r, HashMap[] subTypes) {
        ArrayList<TopKElement> al = new ArrayList<TopKElement>();
        DataSearcher[] data = new DataSearcher[subTypes.length];
        for (int i = 0; i < subTypes.length; i++) {
            data[i] = new SesameRepositorySearcher();
            data[i].setNormalizer(new HashMapNormalizer(subTypes[i]));
        }
        while (type.hasNext()) {
            String nazev = type.getField(0).toString();
            TopKElement el = new TopKElement(nazev, -1, subTypes.length);
            for (int i = 0; i < subTypes.length; i++) {
                el.setRating(i, type.getField(i + 1), data[i].getNormalizer());
            }
            r.rate(el, data);
            al.add(el);
            if (type.advance() == -1) break;
        }
        Collections.sort(al, new MyTopKCompareNames());
        al = reduceArrayListDuplicates(al);
        Collections.sort(al, new MyTopKCompareValues());
        return al;
    }

    /**
	 * <p> getTypeBySubtypes </p> Gets the ratings from database and calls the
	 * getTypeBySubtypes with ratings argument.
	 * @param type String
	 * @param types String[]
	 * @param weights double[]
	 * @param predicates String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return DataSearcher[]
	 */
    public DataSearcher[] getTypeBySubtypes(String type, String[] types, double[] weights, String[] predicates) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String[] ratings = getRatings(true);
        return getTypeBySubtypes(type, types, weights, predicates, ratings);
    }

    /**
	 * <p> getTypeBySubtypes </p> Returns datasearchers, everyone returns the
	 * type <i>type</i>. Objects are ordered by the ratings of their
	 * subtypes.Ratings of subtypes are one user's rating.
	 * @param type String
	 * @param types String[]
	 * @param weights double[]
	 * @param predicates String[]
	 * @param ratings String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return DataSearcher[]
	 */
    public DataSearcher[] getTypeBySubtypes(String type, String[] types, double[] weights, String[] predicates, String[] ratings) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        WeightAverage r = new WeightAverage(types, weights);
        DataSearcher[] data = new DataSearcher[ratings.length];
        for (int i = 0; i < ratings.length; i++) {
            ArrayList sub[] = new ArrayList[types.length];
            for (int j = 0; j < types.length; j++) {
                Normalizer norm[] = new Normalizer[predicates.length];
                for (int k = 0; k < predicates.length; k++) {
                    norm[k] = userNormalizers.getUserNormalizer(ratings[i], predicates[k]);
                }
                sub[j] = getTypeByOnePreference(types[j], norm, ratings[i], false);
            }
            DataSearcher places = getSubTypesSearcher(type, predicates);
            ArrayList<TopKElement> al = getTypeBySubTypes(places, r, sub);
            data[i] = new MemSearcher(ratings[i], al);
        }
        return data;
    }

    /**
	 * <p> getWeightOnClass </p> Returns the average weight of user ratings on
	 * specified type.
	 * @param type String
	 * @return double
	 */
    protected double getWeightOnType(String type) {
        String[] ratigns = getRatings(true);
        double[] weights = getPreferencesOnClass(type, ratigns);
        double weight = 0;
        for (int i = 0; i < weights.length; i++) weight += weights[i];
        weight /= weights.length;
        return weight;
    }

    /**
	 * <p> getWeightsOnPredicates </p> Computes the weights associated by
	 * userRating to each of predicates.
	 * @param predicates String[]
	 * @param userRating String
	 * @return double[]
	 */
    private double[] getWeightsOnTypes(String[] types, String userRating, String computedUserRating) {
        double[] weightsOnPredicates = new double[types.length];
        for (int j = 0; j < types.length; j++) {
            double[] temp2 = getPreferencesOnClass(types[j], new String[] { userRating });
            double[] temp = getPreferencesOnClass(types[j], new String[] { computedUserRating });
            if (temp != null && temp.length > 0) weightsOnPredicates[j] = temp[0]; else if (temp2 != null && temp2.length > 0 && temp2[0] > weightsOnPredicates[j]) weightsOnPredicates[j] = temp2[0]; else weightsOnPredicates[j] = 0;
            if (temp2 != null && temp2.length > 0 && temp2[0] > weightsOnPredicates[j]) weightsOnPredicates[j] = temp2[0];
        }
        return weightsOnPredicates;
    }

    /**
	 * <p> reduceArrayListDuplicates </p> Removes duplicates from arraylist. It
	 * leaves the TopKElement with maximum rating.
	 * @param al ArrayList
	 * @return ArrayList
	 */
    protected ArrayList<TopKElement> reduceArrayListDuplicates(ArrayList<TopKElement> al) {
        TopKElement last = null;
        int lastIndex = -1;
        for (int i = 0; i < al.size(); i++) {
            TopKElement el = al.get(i);
            if (last != null && el.name.equals(last.name)) {
                if (el.rating < last.rating) {
                    al.remove(i);
                    i--;
                    continue;
                } else {
                    al.remove(lastIndex);
                    i = lastIndex - 1;
                    continue;
                }
            }
            last = el;
            lastIndex = i;
        }
        return al;
    }

    /**
	 * <p> transformUserRatingIntoUserName </p> Transforms the user rating into
	 * the name of user, whose rating it is.
	 * @param userRating String
	 * @return String
	 */
    protected String transformUserRatingIntoUserName(String userRating) {
        ArrayList<String> params = new ArrayList<String>();
        params.add(checkURI(RatingsContainer.ratingPredicate));
        params.add(checkURI(userRating));
        String tripQuery = queryFinder.getQuery("GetListOfEntities", 4, params);
        DataSearcher searcher = (DataSearcher) master.clone();
        searcher.initSearch(tripQuery);
        if (searcher.hasNext()) {
            return searcher.getField(0).toString();
        }
        params = new ArrayList<String>();
        params.add(checkURI(RatingsContainer.computedRatingPredicate));
        params.add(checkURI(userRating));
        tripQuery = queryFinder.getQuery("GetListOfEntities", 4, params);
        searcher.initSearch(tripQuery);
        if (searcher.hasNext()) {
            return searcher.getField(0).toString();
        }
        return null;
    }

    /**
	 * <p> transformUserRatingIntoUserName </p> Transforms the user ratings into
	 * the names of user.
	 * @param userRating String
	 * @return String
	 */
    protected String[] transformUserRatingIntoUserName(String userRatings[]) {
        String[] names = new String[userRatings.length];
        for (int i = 0; i < names.length; i++) names[i] = transformUserRatingIntoUserName(userRatings[i]);
        return names;
    }

    /**
	 * <p> getFirstLocal </p> First, applies the one user ratings, thus getting
	 * the objects of type <i>type</i> ordered by every user's rating. Then
	 * uses the standard top k algorithm to compute global top k objects of type
	 * <i>type</i>.
	 * @param type String
	 * @param types String[]
	 * @param weights double[]
	 * @param predicates String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return ArrayList
	 */
    public ArrayList getFirstLocal(String type, String[] types, double[] weights, String[] predicates) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        String[] ratings = getRatings(true);
        DataSearcher[] data = getTypeBySubtypes(type, types, weights, predicates, ratings);
        weights = getPreferencesOnClass(type, ratings);
        WeightAverage r = new WeightAverage(ratings, weights);
        Algorithm a = getAlg(data, r);
        ArrayList res = a.run();
        return res;
    }

    /**
	 * <p> getFirstGlobal </p> First, applies the one user ratings, thus getting
	 * the objects of type <i>type</i> ordered by every user's rating. Then
	 * uses the standard top k algorithm to compute global top k objects of type
	 * <i>type</i>.
	 * @param type String
	 * @param types String[]
	 * @param predicates String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return ArrayList
	 */
    public ArrayList getFirstGlobal(String type, String[] types, String[] predicates) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        double[] weights = new double[predicates.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = getWeightOnType(predicates[i]);
        }
        return getFirstGlobal(type, types, weights, predicates);
    }

    /**
	 * <p> getFirstGlobal </p> First, applies the global average of user ratings
	 * on every subtype. Then uses the standard top k algorithm to compute top k
	 * objects of type <i>type</i> using the ratings of subtypes.
	 * @param type String
	 * @param types String[]
	 * @param weights double[]
	 * @param predicates String[]
	 * @throws IllegalArgumentException, ClassNotFoundException,
	 *             InstantiationException, IllegalAccessException,
	 *             InvocationTargetException
	 * @return ArrayList
	 */
    public ArrayList getFirstGlobal(String type, String[] types, double[] weights, String[] predicates) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ArrayList sub[] = new ArrayList[types.length];
        String[] ratings = getRatings(true);
        Normalizer[] norm = new Normalizer[types.length];
        for (int i = 0; i < sub.length; i++) {
            Normalizer[] normTemp = userNormalizers.getUserNormalizers(transformUserRatingIntoUserName(ratings), predicates[i]);
            Algorithm a = getTypeByGlobalPreferences(types[i], normTemp, ratings);
            norm[i] = new HashMapNormalizer(a.getTopKHashMap());
        }
        DataSearcher[] data = getTypeByRatings(type, norm, predicates, false);
        WeightAverage r = new WeightAverage(predicates, weights);
        Algorithm alg = getAlg(data, r);
        return alg.run();
    }

    /**
	 * <p> updateRatings </p> First, deletes every rating on specified type and
	 * than add the newRating.
	 * @param type String
	 * @param subject String
	 * @param ratingName String
	 * @param newRating double
	 * @return void
	 */
    protected void updateRatings(String type, String subject, String newuserRating, double newRating) {
        ArrayList<String> params = new ArrayList<String>();
        params.add(checkURI(newuserRating));
        params.add(checkURI(type));
        params.add(checkURI(subject));
        String tripQuery = queryFinder.getQuery("GetListOfTypedEntities", 4, params);
        DataSearcher searcher = (DataSearcher) master.clone();
        searcher.initSearch(tripQuery);
        while (searcher.hasNext()) {
            Object sub = searcher.getField(0);
            Object pred = searcher.getField(1);
            Object obj = searcher.getField(2);
            masterModifier.deleteTriple(sub, pred, obj);
            searcher.advance();
        }
        masterModifier.addTriple(subject, newuserRating, Double.toString(newRating), "http://www.w3.org/2001/10/XMLSchema#decimal");
    }

    /**
	 * <p> copyRatingsToComputedRatings </p> Copies the userRating to
	 * newUserRating. Copies only the ratings on <i>objects</i> of type <i>type</i>.
	 * @param userRating String
	 * @param newUserRating String
	 * @param objects String[]
	 * @param type String
	 */
    public void copyRatingsToComputedRatings(String userRating, String newUserRating, String[] objects, String type) {
        double[] weightsOnPredicates = getWeightsOnTypes(objects, userRating, newUserRating);
        for (int i = 0; i < objects.length; i++) {
            updateRatings(type, objects[i], newUserRating, weightsOnPredicates[i]);
        }
    }

    /**
	 * <p> copyRatingsToComputedRatings </p> Copies the userRating to
	 * newUserRating. Copies only the ratings on objects of type <i>type</i>.
	 * @param userRating String
	 * @param newUserRating String
	 * @param type String
	 */
    public void copyRatingsToComputedRatings(String userRating, String newUserRating, String type) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        ArrayList al = getTypeByOnePreference(type, null, userRating, false);
        for (int i = 0; i < al.size(); i++) {
            TopKElement el = (TopKElement) al.get(i);
            double weight = (el.rating);
            updateRatings(type, el.name.toString(), newUserRating, weight);
        }
    }
}
