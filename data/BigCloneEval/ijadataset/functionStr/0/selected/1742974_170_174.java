public class Test {        public NodePreviewImage(TextureGraphNode node) {
            this.node = node;
            node.getChannel().addChannelChangeListener(this);
            updatePreviewImage();
        }
}