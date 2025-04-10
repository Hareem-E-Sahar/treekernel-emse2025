public class Test {    public static void benchmarkDoubleCGLS(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        System.out.println("Benchmarking DoubleCGLS using " + threads + " threads");
        double av_time_deblur = 0;
        long elapsedTime_deblur = 0;
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            ParallelHRRTDeconvolution.deconvolveCGLS(pathToDeblurredImage, pathToBlurredImage, pathToCalibrationMatrix, pathToMotionInformation, pathToSegmentationInformation, segmentationDataSeries, quaternionsWeight, minSegmentSize, samplingRate, scanDuration, initialTimeOffset, interpolation, output, "DOUBLE", stoppingTol, threshold, maxIters, logConvergence, new Integer(threads).toString(), showIters);
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;
            System.gc();
        }
        System.out.println("Average execution time: " + String.format(format, av_time_deblur / 1000000000.0 / NITER) + " sec");
        writeResultsToFile("DoubleCGLS_" + threads + "_threads.txt", av_time_deblur / 1000000000.0 / NITER);
    }
}