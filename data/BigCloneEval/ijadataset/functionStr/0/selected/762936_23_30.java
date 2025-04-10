public class Test {    public void execute(TestRun test) throws Exception {
        Class executorClass = test.getExecutorClass();
        Executor executor = (Executor) executorClass.newInstance();
        Duration readDuration = executor.read(test);
        Duration writeDuration = executor.write(test);
        System.err.printf("Execution of " + test.getId() + ": read=%s ms read-total=%s write=%s ms write-total=%s", readDuration.getOperation(), readDuration.getTotal(), writeDuration.getOperation(), writeDuration.getOperation());
        System.err.println();
    }
}