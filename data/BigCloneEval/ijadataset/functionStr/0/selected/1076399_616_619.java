public class Test {        @Override
        public int hashCode() {
            return getChannel().hashCode() ^ getGroup().hashCode() ^ getNetworkInterface().hashCode();
        }
}