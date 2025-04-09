public class Test {        public String getChecksumHexed() {
            return StringUtil.hexify(digest.digest());
        }
}