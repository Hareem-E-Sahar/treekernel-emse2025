package testing;

import data.DataInfo;
import data.DataLookup;

public class AveragePredict implements NetflixPredictor {

    public double predictRating(int userID, short movieID) {
        DataLookup ld = DataLookup.getInstance();
        int userIndex = DataInfo.getUserIndex(userID);
        double userA = ld.getUserAverage(userIndex);
        double movieA = ld.getMovieAverage(movieID);
        double userV = ld.getUserStdDev(userIndex);
        double movieV = ld.getMovieStdDev(movieID);
        DataInfo.checkInvalidDouble("movieV", movieV);
        DataInfo.checkInvalidDouble("userV", userV);
        double userR = 0.0;
        if (userV > 0) userR = 1 / userV;
        double movieR = 0.0;
        if (movieR > 0) movieR = 1 / movieV;
        double sumR = userR + movieR;
        if (sumR == 0) {
            return (userA + movieA) / 2;
        }
        DataInfo.checkInvalidDouble("sumR", sumR);
        double avg = (userA + movieA) / 2;
        return avg;
    }

    public final double predictRating(int userID, short movieID, String date) {
        return predictRating(userID, movieID);
    }

    public static void main(String args[]) {
        DataLookup.getInstance();
        AveragePredict tp = new AveragePredict();
        System.out.println("Calculating error on probe dataset");
        System.out.println("This will take a long time if the data hasn't been previously calculated");
        double error = ThreadedPredictionTester.getProbeError(tp, false, false);
        System.out.println("Error on probe dataset is: " + error);
        String outputFilename = "/home/bob/qual01.txt";
        System.out.println("Creating Qualification file at " + outputFilename);
        PredictionTester.createQualifyingSubmission(tp, outputFilename);
    }
}
