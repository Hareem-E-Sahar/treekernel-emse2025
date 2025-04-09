package algorithms.fibonacci;

import pram.ProcessorState;
import algorithms.AbstractAlgorithm;

public class Fibonacci extends AbstractAlgorithm implements Runnable {

    private int min, max;

    private int result;

    public Fibonacci(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public void run() {
        setupProcessorData(max - min);
        System.out.println(min + " . . . " + max);
        result = fibonacci(max, min);
        System.out.println("Achou: Processor " + processor.getId() + " result: " + result);
        processor.setState(ProcessorState.COMPLETED);
        processor.getAlgData().updateValues(1, 1, 1);
        processor.getModel().setData(processor.getModel().getData() + result);
    }

    public int fibonacci(int num) {
        if (num == 0) {
            return 0;
        } else if (num == 1) {
            return 1;
        } else {
            processor.getAlgData().setRecursiveCalls(processor.getAlgData().getRecursiveCalls() + 2);
            return fibonacci(num - 1) + fibonacci(num - 2);
        }
    }

    public int fibonacci(int num, int min) {
        int result = 0;
        if (num == 0) {
            processor.getAlgData().setNumComparisons(processor.getAlgData().getNumComparisons() + 1);
            result = 0;
        } else if (num == 1) {
            processor.getAlgData().setNumComparisons(processor.getAlgData().getNumComparisons() + 1);
            result = 1;
        } else if (num == min) {
            processor.getAlgData().setNumComparisons(processor.getAlgData().getNumComparisons() + 1);
            result = 0;
        } else {
            processor.getAlgData().setRecursiveCalls(processor.getAlgData().getRecursiveCalls() + 2);
            result = fibonacci(num - 1, min) + fibonacci(num - 2, min);
        }
        return result;
    }
}
