public class Test {    public static void benchmarkComplexForward2DInput2D(int init_exp) {
        int[] sizes = new int[nsize];
        double[] times = new double[nsize];
        double[][] x;
        for (int i = 0; i < nsize; i++) {
            int exponent = init_exp + i;
            int N = (int) Math.pow(2, exponent);
            sizes[i] = N;
            System.out.println("Complex forward FFT 2D (input 2D) of size 2^" + exponent + " x 2^" + exponent);
            DoubleFft2D fft2 = new DoubleFft2D(N, N);
            x = new double[N][2 * N];
            if (doWarmup) {
                IoUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
                IoUtils.fillMatrix_2D(N, 2 * N, x);
                fft2.complexForward(x);
            }
            double av_time = 0;
            long elapsedTime = 0;
            for (int j = 0; j < niter; j++) {
                IoUtils.fillMatrix_2D(N, 2 * N, x);
                elapsedTime = System.nanoTime();
                fft2.complexForward(x);
                elapsedTime = System.nanoTime() - elapsedTime;
                av_time = av_time + elapsedTime;
            }
            times[i] = (double) av_time / 1000000.0 / (double) niter;
            System.out.println("\tAverage execution time: " + String.format("%.2f", av_time / 1000000.0 / (double) niter) + " msec");
            x = null;
            fft2 = null;
            System.gc();
        }
        IoUtils.writeFFTBenchmarkResultsToFile("benchmarkDoubleComplexForwardFFT_2D_input_2D.txt", nthread, niter, doWarmup, doScaling, sizes, times);
    }
}