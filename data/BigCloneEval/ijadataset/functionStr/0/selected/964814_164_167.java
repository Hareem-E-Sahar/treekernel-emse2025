public class Test {        public MergeThread(IndexWriter writer, MergePolicy.OneMerge startMerge) throws IOException {
            this.writer = writer;
            this.startMerge = startMerge;
        }
}