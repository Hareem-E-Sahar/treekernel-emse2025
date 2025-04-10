package org.loon.framework.javase.game.srpg.actor;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.util.HashMap;
import org.loon.framework.javase.game.utils.CollectionUtils;
import org.loon.framework.javase.game.utils.GraphicsUtils;

/**
 * Copyright 2008 - 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email：ceponline@yahoo.com.cn
 * @version 0.1
 */
public class SRPGAnimation {

    private static final HashMap animations = new HashMap();

    Image[] downImages;

    Image[] upImages;

    Image[] leftImages;

    Image[] rightImages;

    /**
	 * 以RMVX的角色格式创建对象(总图大小96x128，每格大小32x32)
	 * 
	 * @param fileName
	 * @return
	 */
    public static SRPGAnimation makeRMVXObject(String fileName) {
        return makeObject(fileName, 4, 3, 32, 32);
    }

    /**
	 * 以RMXP的角色格式创建对象(总图大小128x192，每格大小32x48)
	 * 
	 * @param fileName
	 * @return
	 */
    public static SRPGAnimation makeRMXPObject(String fileName) {
        return makeObject(fileName, 4, 4, 32, 48);
    }

    /**
	 * 以E社的角色格式创建对象(总图大小200x200，每格大小40x50)
	 * 
	 * @param fileName
	 * @return
	 */
    public static SRPGAnimation makeEObject(String fileName) {
        return makeObject(fileName, 40, 50, Color.green);
    }

    /**
	 * 以RMVX的角色格式创建分解头象
	 * 
	 * @param fileName
	 * @return
	 */
    public static Image[] makeFace(String fileName) {
        return GraphicsUtils.getSplitImages(fileName, 96, 96);
    }

    /**
	 * 绘制一个RMVX样式的游标
	 * 
	 * @return
	 */
    public static Image makeCursor(int w, int h) {
        BufferedImage cursor = GraphicsUtils.createImage(w, h, true);
        Graphics g = cursor.getGraphics();
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(0, 0, w, h);
        g.setColor(new Color(255, 255, 255, 255));
        g.fillRect(1, 1, w - 2, h - 2);
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(4, 4, w - 8, h - 8);
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(w / 4, 0, w / 2, h);
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(0, h / 4, w, h / 2);
        g.dispose();
        GraphicsUtils.transparencyColor(cursor, Color.black.getRGB());
        return cursor;
    }

    public static SRPGAnimation makeObject(String fileName, int row, int col, int tileWidth, int tileHeight) {
        String key = fileName.trim().toLowerCase();
        SRPGAnimation animation = (SRPGAnimation) animations.get(key);
        if (animation == null) {
            Image[][] images = GraphicsUtils.getSplit2Images(fileName, tileWidth, tileHeight);
            Image[][] result = new Image[row][col];
            for (int y = 0; y < col; y++) {
                for (int x = 0; x < row; x++) {
                    result[x][y] = images[y][x];
                }
            }
            images = null;
            animations.put(key, animation = makeObject(result[0], result[1], result[2], result[3]));
        }
        return animation;
    }

    public static SRPGAnimation makeObject(String fileName, int tileWidth, int tileHeight, Color col) {
        String key = fileName.trim().toLowerCase();
        SRPGAnimation animation = (SRPGAnimation) animations.get(key);
        if (animation == null) {
            Image image = GraphicsUtils.loadNotCacheImage(fileName);
            int c = col.getRGB();
            int wlength = image.getWidth(null) / tileWidth;
            int hlength = image.getHeight(null) / tileHeight;
            Image[][] images = new Image[wlength][hlength];
            for (int y = 0; y < hlength; y++) {
                for (int x = 0; x < wlength; x++) {
                    images[x][y] = GraphicsUtils.createImage(tileWidth, tileHeight, true);
                    Graphics g = images[x][y].getGraphics();
                    g.drawImage(image, 0, 0, tileWidth, tileHeight, (x * tileWidth), (y * tileHeight), tileWidth + (x * tileWidth), tileHeight + (y * tileHeight), null);
                    g.dispose();
                    g = null;
                    PixelGrabber pgr = new PixelGrabber(images[x][y], 0, 0, -1, -1, true);
                    try {
                        pgr.grabPixels();
                    } catch (InterruptedException ex) {
                        ex.getStackTrace();
                    }
                    int pixels[] = (int[]) pgr.getPixels();
                    for (int i = 0; i < pixels.length; i++) {
                        if (pixels[i] == c) {
                            pixels[i] = 0;
                        }
                    }
                    ImageProducer ip = new MemoryImageSource(pgr.getWidth(), pgr.getHeight(), pixels, 0, pgr.getWidth());
                    images[x][y] = GraphicsUtils.toolKit.createImage(ip);
                }
            }
            Image[][] result = new Image[hlength][wlength];
            for (int y = 0; y < wlength; y++) {
                for (int x = 0; x < hlength; x++) {
                    result[x][y] = images[y][x];
                }
            }
            images = null;
            animations.put(key, animation = makeObject(result[0], result[1], result[3], result[2]));
        }
        return animation;
    }

    public static final SRPGAnimation makeObject(Image[] down, Image[] left, Image[] right, Image[] up) {
        SRPGAnimation animation = new SRPGAnimation();
        animation.downImages = down;
        animation.leftImages = left;
        animation.rightImages = right;
        animation.upImages = up;
        return animation;
    }

    public static final void dispose(Image[] images) {
        if (images == null) {
            return;
        }
        for (int i = 0; i < images.length; i++) {
            images[i].flush();
            images[i] = null;
        }
    }

    SRPGAnimation() {
    }

    public SRPGAnimation(SRPGAnimation animation) {
        leftImages = (Image[]) CollectionUtils.copyOf(animation.leftImages);
        downImages = (Image[]) CollectionUtils.copyOf(animation.downImages);
        upImages = (Image[]) CollectionUtils.copyOf(animation.upImages);
        rightImages = (Image[]) CollectionUtils.copyOf(animation.rightImages);
    }

    public void dispose() {
        dispose(downImages);
        dispose(upImages);
        dispose(leftImages);
        dispose(rightImages);
        animations.remove(this);
    }
}
