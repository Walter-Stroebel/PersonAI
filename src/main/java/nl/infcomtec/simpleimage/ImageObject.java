package nl.infcomtec.simpleimage;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Semaphore;

/**
 * Thread safe wrapper around BufferedImage.
 *
 * @author Walter Stroebel
 */
public class ImageObject extends Image {

    private BufferedImage image;
    private final Semaphore lock = new Semaphore(0);
    private final List<ImageObjectListener> listeners = new LinkedList<>();

    public ImageObject(Image image) {
        putImage(image);
    }

    /**
     * Image can only have one owner
     *
     * @return The most recent image.
     */
    public BufferedImage getImage() {
        try {
            lock.acquire();
        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
        return image;
    }

    /**
     * Just release the lock.
     */
    public void releaseImage() {
        lock.release();
    }

    /**
     * Stay informed.
     *
     * @param listener called when another client changes the image.
     */
    public synchronized void addListener(ImageObjectListener listener) {
        listeners.add(listener);
    }

    public enum MouseEvents {
        clicked
    };

    public synchronized void forwardMouse(MouseEvents ev, MouseEvent e) {
        for (ImageObjectListener listener : listeners) {
            System.out.println("Calling listener " + listener.name);
            listener.mouseEvent(this, ev, e);
        }

    }

    /**
     * Replace image or release image.
     *
     * @param replImage If null image may still have been altered in place, else
     * replace image with replImage. All listeners will be notified.
     */
    public synchronized final void putImage(Image replImage) {
        if (null != replImage) {
            this.image = new BufferedImage(replImage.getWidth(null), replImage.getHeight(null), BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = this.image.createGraphics();
            g2.drawImage(replImage, 0, 0, null);
            g2.dispose();
        }
        releaseImage();
        for (ImageObjectListener listener : listeners) {
            System.out.println("Calling listener " + listener.name);
            listener.imageChanged(this);
        }
    }

    public int getWidth() {
        return getWidth(null);
    }

    @Override
    public int getWidth(ImageObserver io) {
        return image.getWidth(io);
    }

    @Override
    public int getHeight(ImageObserver io) {
        return image.getHeight(io);
    }

    public int getHeight() {
        return getHeight(null);
    }

    @Override
    public ImageProducer getSource() {
        return image.getSource();
    }

    @Override
    public Graphics getGraphics() {
        return image.getGraphics();
    }

    @Override
    public Object getProperty(String string, ImageObserver io) {
        return image.getProperty(string, null);
    }

    /**
     * Callback listener.
     */
    public static class ImageObjectListener {

        public final String name;

        public ImageObjectListener(String name) {
            this.name = name;
        }

        /**
         * Image may have been altered.
         *
         * @param imgObj Source.
         */
        public void imageChanged(ImageObject imgObj) {
            // default is no action
        }

        public void mouseEvent(ImageObject imgObj, MouseEvents ev, MouseEvent e) {
            // default ignore
        }
    }
}
