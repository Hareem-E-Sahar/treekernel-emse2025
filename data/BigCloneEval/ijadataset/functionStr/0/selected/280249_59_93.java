public class Test {    public LogReaderMenu() {
        super("Log reader implementation");
        ButtonGroup bGroup = new ButtonGroup();
        nfs2AggressiveReader = new JRadioButtonMenuItem("NikeFS2 Aggressive Optimization", false);
        nfs2AggressiveReader.setToolTipText("Aggressively optimized virtual file system fixed-size block allocation; true random access; read-write persistency");
        nfs2AggressiveReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_AGGRESSIVE);
        nfs2AggressiveReader.addActionListener(this);
        bGroup.add(nfs2AggressiveReader);
        this.add(nfs2AggressiveReader);
        nfs2ConservativeReader = new JRadioButtonMenuItem("NikeFS2 Conservative Optimization", false);
        nfs2ConservativeReader.setToolTipText("Conservatively optimized virtual file system fixed-size block allocation; true random access; read-write persistency");
        nfs2ConservativeReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS2_CONSERVATIVE);
        nfs2ConservativeReader.addActionListener(this);
        bGroup.add(nfs2ConservativeReader);
        this.add(nfs2ConservativeReader);
        vfsReader = new JRadioButtonMenuItem("Buffered log reader (NikeFS)", false);
        vfsReader.setToolTipText("Virtual file system block allocation; true random access; read-write persistency");
        vfsReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_NIKEFS);
        vfsReader.addActionListener(this);
        bGroup.add(vfsReader);
        this.add(vfsReader);
        rfbReader = new JRadioButtonMenuItem("Buffered log reader (dedicated files)", false);
        rfbReader.setToolTipText("OS-level file allocation; true random access; read-write persistency");
        rfbReader.setActionCommand(LogReaderMenu.ACTION_BUFFERED_CACHEDFILES);
        rfbReader.addActionListener(this);
        bGroup.add(rfbReader);
        this.add(rfbReader);
        classicReader = new JRadioButtonMenuItem("Classic log reader (heap-based)", true);
        classicReader.setToolTipText("Heap space allocation; no random access; read-only");
        classicReader.setActionCommand(LogReaderMenu.ACTION_CLASSIC);
        classicReader.addActionListener(this);
        bGroup.add(classicReader);
        this.add(classicReader);
        restore();
    }
}