public class Test {    public static void benchmarkDoubleTikhonov_Reflexive_2D(int threads) {
        ConcurrencyUtils.setNumberOfThreads(threads);
        Opener o = new Opener();
        ImagePlus blurImage = o.openImage(path + blur_image);
        ImagePlus psfImage = o.openImage(path + psf_image);
        double av_time_deblur = 0;
        double av_time_deblur_regParam = 0;
        double av_time_update = 0;
        long elapsedTime_deblur = 0;
        long elapsedTime_deblur_regParam = 0;
        long elapsedTime_update = 0;
        System.out.println("Benchmarking DoubleTikhonov_Reflexive_2D using " + threads + " threads");
        for (int i = 0; i < NITER; i++) {
            elapsedTime_deblur = System.nanoTime();
            DoubleReflexiveTikhonov2D tsvd = new DoubleReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, -1, threshold);
            ImagePlus imX = tsvd.deconvolve();
            elapsedTime_deblur = System.nanoTime() - elapsedTime_deblur;
            av_time_deblur = av_time_deblur + elapsedTime_deblur;
            elapsedTime_deblur_regParam = System.nanoTime();
            tsvd = new DoubleReflexiveTikhonov2D(blurImage, psfImage, resizing, output, false, double_regParam_deblur, threshold);
            imX = tsvd.deconvolve();
            elapsedTime_deblur_regParam = System.nanoTime() - elapsedTime_deblur_regParam;
            av_time_deblur_regParam = av_time_deblur_regParam + elapsedTime_deblur_regParam;
            elapsedTime_update = System.nanoTime();
            tsvd.update(double_regParam_update, threshold, imX);
            elapsedTime_update = System.nanoTime() - elapsedTime_update;
            av_time_update = av_time_update + elapsedTime_update;
            imX = null;
            tsvd = null;
        }
        blurImage = null;
        psfImage = null;
        System.out.println("Average execution time (deblur()): " + String.format(format, av_time_deblur / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (deblur(regParam)): " + String.format(format, av_time_deblur_regParam / 1000000000.0 / (double) NITER) + " sec");
        System.out.println("Average execution time (update()): " + String.format(format, av_time_update / 1000000000.0 / (double) NITER) + " sec");
        writeResultsToFile("DoubleTikhonov_Reflexive_2D_" + threads + "_threads.txt", (double) av_time_deblur / 1000000000.0 / (double) NITER, (double) av_time_deblur_regParam / 1000000000.0 / (double) NITER, (double) av_time_update / 1000000000.0 / (double) NITER);
    }
}