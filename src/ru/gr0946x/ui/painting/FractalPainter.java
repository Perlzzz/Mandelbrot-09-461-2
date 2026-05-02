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

        // рисуем в буфер, а не сразу на экран
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

//        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<?>> futures = new ArrayList<>();

        int chunkHeight = h / threadCount;

        System.out.println("=== Начало отрисовки. Потоков: " + threadCount + " ===");
        long startTime = System.currentTimeMillis();

        for (int t = 0; t < threadCount; t++) {
            final int startY = t * chunkHeight;
            final int endY = (t == threadCount - 1) ? h : startY + chunkHeight;

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

        executor.shutdown();
        long elapsed = System.currentTimeMillis() - startTime;
        System.out.println("=== Отрисовка завершена за " + elapsed + " мс ===\n");

        g.drawImage(image, 0, 0, null);
        }

    }

