public class Test {    private static void drawShapes(Renderer r) {
        BufferedImage bi = r.mBufferedImage;
        int width = bi.getWidth();
        int height = bi.getHeight();
        long now = System.currentTimeMillis();
        lag = (int) (((now - mLastRenderTime) * 0.1d) + (0.9d * lag));
        mLastRenderTime = now;
        int maxscale = (width + height) / 2;
        if ((lag < 200) && (r.mCurrentScale < maxscale)) r.mCurrentScale++; else if ((lag > 300) && (r.mCurrentScale > 10)) r.mCurrentScale = r.mCurrentScale / 2;
        double scale = r.mCurrentScale;
        double percent = (double) (System.currentTimeMillis() % 10000) / 10000d;
        double magic = Math.PI * 2d * percent;
        double x1 = Math.sin(magic) / 4d + 0.5d;
        double y1 = 0.5d;
        double z1 = Math.cos(magic) / 4d + 0.5d;
        double x2 = 0.5d;
        double y2 = 0.75d;
        double z2 = 0.5d;
        double x3 = 0.5d;
        double y3 = 0.25d;
        double z3 = 0.5d;
        Vector3d nonorm = new Vector3d();
        r.mSpace.drawLine(new Point3d(1d, 1d, 0d), new Point3d(1d, 1d, 1d), scale, Color.BLUE.getRGB(), 0, nonorm);
        r.mSpace.drawLine(new Point3d(1d, 1d, 0d), new Point3d(1d, 1d, 1d), scale, Color.BLUE.getRGB(), 0, nonorm);
        r.mSpace.drawLine(new Point3d(1d, 0d, 0d), new Point3d(1d, 0d, 1d), scale, Color.BLUE.getRGB(), 0, nonorm);
        r.mSpace.drawLine(new Point3d(0d, 1d, 0d), new Point3d(0d, 1d, 1d), scale, Color.BLUE.getRGB(), 0, nonorm);
        r.mSpace.drawLine(new Point3d(0d, 0d, 0d), new Point3d(0d, 0d, 1d), scale, Color.BLUE.getRGB(), 0, nonorm);
        r.mSpace.drawTriangle(new Point3d(1d, 0.75d, 0.25d), new Point3d(1d, 0.75d, 0.75d), new Point3d(1d, 0.25d, 0.25d), scale, Color.YELLOW.getRGB() & 0xFFFFFFFF, 0);
        r.mSpace.drawTriangle(new Point3d(1d, 0.75d, 0.75d), new Point3d(1d, 0.25d, 0.25d), new Point3d(1d, 0.25d, 0.75d), scale, Color.GREEN.getRGB() & 0xFFFFFFFF, 0);
        r.mSpace.drawTriangle(new Point3d(x2, y2, z2), new Point3d(x3, y3, z3), new Point3d(x1, y1, z1), scale, Color.RED.getRGB(), 0);
        r.mSpace.drawSphere(scale, new Point3d(x1, y1, z1), 0.25d, 12, Color.CYAN.getRGB() & 0x44FFFFFF, 0.5);
        r.mSpace.drawLight(new Point3d(0.5d, 0.5d, 0d), new Point3d(0.5d, 0.5d, 0d), new Point3d(0.5d, 0.5d, 1d), 1d, new Vector3d(1, 1, 1), new Vector3d(0, 0, 0));
    }
}