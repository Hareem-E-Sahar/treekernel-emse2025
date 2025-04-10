package edu.mta.ok.nworkshop.model;

import java.util.HashMap;
import edu.mta.ok.nworkshop.Constants;
import edu.mta.ok.nworkshop.PredictorProperties;
import edu.mta.ok.nworkshop.utils.FileUtils;
import edu.mta.ok.nworkshop.utils.ModelUtils;

/**
 * A class that holds a user indexed model with movie ids and residuals of global effects as ratings.
 * Ratings are held in a two dimensional double type array.
 */
public class UserIndexedModelResiduals implements UserIndexedModel {

    private static String DEFAULT_RESIDUALS_FILE_NAME = Constants.NETFLIX_OUTPUT_DIR + "/globalEffects/userIndexedResidualEffect11.data";

    private short[][] movieIds;

    private double[][] ratings;

    private HashMap<Integer, Integer> userIndices;

    private ModelSorter sorter = new ModelSorter();

    public UserIndexedModelResiduals() {
        this(PredictorProperties.getInstance().getUserIndexedModelFile(), PredictorProperties.getInstance().getUserIndicesMappingFile(), DEFAULT_RESIDUALS_FILE_NAME);
    }

    public UserIndexedModelResiduals(String modelFileName) {
        this(modelFileName, PredictorProperties.getInstance().getUserIndicesMappingFile(), DEFAULT_RESIDUALS_FILE_NAME);
    }

    public UserIndexedModelResiduals(String modelFileName, String indicesFileName, String residualsFileName) {
        super();
        loadModel(modelFileName, residualsFileName);
        loadUserIndices(indicesFileName);
    }

    /**
	 * Loads the model from a given file
	 * 
	 * @param fileName the full path of the file we want to load the model from
	 */
    private void loadModel(String fileName, String residualsFileName) {
        Object[] retVal = ModelUtils.loadUserIndexedModel(fileName, false);
        movieIds = (short[][]) retVal[0];
        retVal[1] = null;
        ratings = FileUtils.loadDataFromFile(residualsFileName);
        System.out.println("Finished loading userIndexed model");
    }

    /**
	 * Loads the user indices mappings from a given file
	 * 
	 * @param fileName the full path of the file that we want to load the user indices mappings from
	 */
    private void loadUserIndices(String fileName) {
        userIndices = FileUtils.loadDataFromFile(fileName);
    }

    @Override
    public void sortModel() {
        sorter.sortData();
    }

    /**
	 * Convert the given user id to the matching index in the model
	 * 
	 * @param userId the user id we want to convert
	 * @return a number representing the index in the model the given user id is placed in
	 */
    private int convertIdToIndex(int userId) {
        return userIndices.get(userId);
    }

    @Override
    public short[] getRatedMovies(int userId) {
        return getRatedMoviesByIndex(convertIdToIndex(userId));
    }

    @Override
    public short[] getRatedMoviesByIndex(int userId) {
        return movieIds[userId];
    }

    @Override
    public Object[] getUserData(int userId) {
        Object[] retVal = new Object[2];
        retVal[0] = movieIds[convertIdToIndex(userId)];
        retVal[1] = ratings[convertIdToIndex(userId)];
        return retVal;
    }

    @Override
    public Object getUserRatings(int userId) {
        return getUserRatingsByIndex(convertIdToIndex(userId));
    }

    @Override
    public Object getUserRatingsByIndex(int userInd) {
        return ratings[userInd];
    }

    @Override
    public double getUserRating(int index, int userID) {
        return ratings[getUserIndex(userID)][index];
    }

    @Override
    public void removeUserDataByIndex(int userInd) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public short[][] getMovieIds() {
        return movieIds;
    }

    @Override
    public Object[] getRatings() {
        return ratings;
    }

    @Override
    public Object[] getModelArray() {
        int[] userIds = new int[Constants.TRAIN_RATINGS_NUM];
        short[] movieIds = new short[Constants.TRAIN_RATINGS_NUM];
        double[] ratings = new double[Constants.TRAIN_RATINGS_NUM];
        short[] ratedMovies = null;
        double[] currRatings = null;
        int counter = 0;
        System.out.println("Start reshaping the user indexed model into three dimensional array");
        for (int userIndex = 0; userIndex < Constants.NUM_USERS; userIndex++) {
            ratedMovies = getRatedMoviesByIndex(userIndex);
            currRatings = (double[]) getUserRatingsByIndex(userIndex);
            for (int mi = 0; mi < ratedMovies.length; mi++) {
                userIds[counter] = userIndex;
                movieIds[counter] = ratedMovies[mi];
                ratings[counter] = currRatings[mi];
                counter++;
            }
        }
        System.out.println("Finish reshaping the user indexed model into three dimensional array");
        return new Object[] { userIds, movieIds, ratings };
    }

    @Override
    public int getUserIndex(int userId) {
        return convertIdToIndex(userId);
    }

    /**
	 * Sorts the model in ascending order according to the movie ids 
	 *
	 */
    private class ModelSorter {

        public void sortData() {
            if (movieIds != null && movieIds.length > 0) {
                long start = System.currentTimeMillis();
                for (int i = 0; i < movieIds.length; i++) {
                    if (movieIds[i] != null && movieIds[i].length > 0) {
                        sort(i);
                    }
                    if (i % 100 == 0 && i > 0) {
                        start = System.currentTimeMillis();
                        System.out.println("Finished " + i + " movies. took: " + (System.currentTimeMillis() - start));
                    }
                }
            }
        }

        private void sort(int itemIndex) {
            double[] scoreTmpArray = new double[ratings[itemIndex].length];
            short[] contentsTmpArray = new short[movieIds[itemIndex].length];
            mergeSort(contentsTmpArray, scoreTmpArray, 0, ratings[itemIndex].length - 1, itemIndex);
        }

        public void mergeSort(short[] tempArrayContents, double[] tempArrayScores, int left, int right, int itemIndex) {
            if (left < right) {
                int center = (left + right) / 2;
                mergeSort(tempArrayContents, tempArrayScores, left, center, itemIndex);
                mergeSort(tempArrayContents, tempArrayScores, center + 1, right, itemIndex);
                merge(tempArrayContents, tempArrayScores, left, center + 1, right, itemIndex);
            }
        }

        private void merge(short[] contentsArray, double[] scoresArray, int leftPos, int rightPos, int rightEnd, int itemIndex) {
            int leftEnd = rightPos - 1;
            int tmpPos = leftPos;
            int numElements = rightEnd - leftPos + 1;
            while (leftPos <= leftEnd && rightPos <= rightEnd) {
                if (movieIds[itemIndex][leftPos] < movieIds[itemIndex][rightPos]) {
                    scoresArray[tmpPos] = ratings[itemIndex][leftPos];
                    contentsArray[tmpPos++] = movieIds[itemIndex][leftPos++];
                } else {
                    scoresArray[tmpPos] = ratings[itemIndex][rightPos];
                    contentsArray[tmpPos++] = movieIds[itemIndex][rightPos++];
                }
            }
            while (leftPos <= leftEnd) {
                scoresArray[tmpPos] = ratings[itemIndex][leftPos];
                contentsArray[tmpPos++] = movieIds[itemIndex][leftPos++];
            }
            while (rightPos <= rightEnd) {
                scoresArray[tmpPos] = ratings[itemIndex][rightPos];
                contentsArray[tmpPos++] = movieIds[itemIndex][rightPos++];
            }
            for (int i = 0; i < numElements; i++, rightEnd--) {
                ratings[itemIndex][rightEnd] = scoresArray[rightEnd];
                movieIds[itemIndex][rightEnd] = contentsArray[rightEnd];
            }
        }
    }
}
