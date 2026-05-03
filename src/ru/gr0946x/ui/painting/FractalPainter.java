package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.Fractal;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class FractalPainter implements Painter{

    private final Fractal fractal;
    private final Converter conv;
    private final ColorFunction colorFunction;

    private final int threadCount = Runtime.getRuntime().availableProcessors();
    private final ExecutorService executor = Executors.newFixedThreadPool(threadCount);

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

    public FractalPainter(Fractal f, Converter conv, ColorFunction cf){
        this.fractal = f;
        this.conv = conv;
        this.colorFunction = cf;
    }

    public Converter getConverter() {
        return conv;
    }

    @Override
    public void paint(Graphics g) {
        var w = getWidth();
        var h = getHeight();

        if (w <= 0 || h <= 0) return;

        // рисуем в буфер
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

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

        g.drawImage(image, 0, 0, null);
    }

    public void shutdown() {
        executor.shutdown();
    }
}