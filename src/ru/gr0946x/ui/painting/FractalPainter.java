package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FractalPainter implements Painter {

    private final Fractal fractal;
    private final Converter conv;
    private final ColorFunction colorFunction;

    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

    // Кэш для отрисовки в UI
    private volatile BufferedImage lastImage = null;
    private volatile boolean isRendering = false;

    @Override
    public int getWidth() {
        return conv.getWidth();
    }

    @Override
    public int getHeight() {
        return conv.getHeight();
    }

    @Override
    public void setWidth(int width) {
        conv.setWidth(width);
    }

    @Override
    public void setHeight(int height) {
        conv.setHeight(height);
    }

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf) {
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
    }

    public Converter getConverter() {
        return conv;
    }

    /**
     * Асинхронная отрисовка для UI.
     */
    @Override
    public void renderAsync(Runnable oneDone) {
        if (isRendering) return;
        isRendering = true;

        executor.submit(() -> {
            try {
                int w = getWidth();
                int h = getHeight();
                if (w > 0 && h > 0) {
                    BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                    renderToImage(img);
                    lastImage = img;
                }
            } finally {
                isRendering = false;
                if (oneDone != null) {
                    SwingUtilities.invokeLater(oneDone);
                }
            }
        });
    }

    /**
     * Отрисовка на графический контекст.
     */
    @Override
    public void paint(Graphics g) {
        // Если мы в потоке Swing (UI), рисуем кэшированное изображение
        if (SwingUtilities.isEventDispatchThread()) {
            if (lastImage != null) {
                g.drawImage(lastImage, 0, 0, null);
            }
        } else {
            // Если мы в фоновом потоке (например, VideoRenderer), рисуем честно и синхронно
            int w = getWidth();
            int h = getHeight();
            if (w > 0 && h > 0) {
                BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
                renderToImage(img);
                g.drawImage(img, 0, 0, null);
            }
        }
    }

    /**
     * Внутренний метод для многопоточной отрисовки в BufferedImage.
     */
    private void renderToImage(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        List<Future<?>> futures = new ArrayList<>();
        int chunkHeight = Math.max(1, h / threadCount);

        for (int t = 0; t < threadCount; t++) {
            final int startY = t * chunkHeight;
            final int endY = (t == threadCount - 1) ? h : Math.min(h, startY + chunkHeight);
            
            if (startY >= h) break;

            futures.add(executor.submit(() -> {
                for (int j = startY; j < endY; j++) {
                    for (int i = 0; i < w; i++) {
                        double x = conv.xScr2Crt(i);
                        double y = conv.yScr2Crt(j);
                        float res = fractal.inSetProbability(x, y);
                        image.setRGB(i, j, colorFunction.getColor(res).getRGB());
                    }
                }
            }));
        }

        for (Future<?> f : futures) {
            try { f.get(); }
            catch (Exception e) { e.printStackTrace(); }
        }
    }

    public void shutdown() {
        executor.shutdown();
    }
}
