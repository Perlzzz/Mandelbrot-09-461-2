package ru.gr0946x.ui.fractals;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal{

    private int maxIterations = 100;
    private final double R2 = 4;
    public double getR(){
        return sqrt(R2);
    }

    public void setMaxIterations(int maxIterations) {
        this.maxIterations = Math.max(10, maxIterations);
    }

    public int getMaxIterations() {
        return maxIterations;
    }

    // пункт 10
    public void updateIterationsByZoom(double currentWidth) { // ширина в координатах фрактала (не в пикслях)
        double baseWidth = 4.0;

        double zoom = baseWidth / currentWidth;

        if (zoom <= 1.0) {
            this.maxIterations = 100;
        } else {
            // если приблизили, увеличение итераций с помощью логарифма.
            this.maxIterations = 100 + (int) (500 * Math.log10(zoom)); // значение 200 можно менять(выше->больше итераций)
        }

        // вывод того, как меняются итерации
        System.out.println("текущий масштаб: " + (int)zoom + "x. итераций: " + this.maxIterations);
    }

    @Override
    public float inSetProbability(double x, double y) {
        double zx = 0.0;
        double zy = 0.0;
        double zx2 = 0.0;
        double zy2 = 0.0;
        int i = 0;
        while (zx2 * zy2 < R2 && ++i < maxIterations){
            // f(z) = z^2 + c
            // z = (zx + zy*i)
            // z^2 = (zx + zy*i)^2 = zx2 + 2zx*zy*i + zy2*i^2 = (zx2−zy2) + (2*zx*zy)*i
            // z^2 + c = (zx2−zy2) + x + (2*zx*zy+y)*i
            zy = 2.0 * zx * zy + y;
            zx = zx2 - zy2 + x;

            zx2 = zx * zx;
            zy2 = zy * zy;
        }
        return (float)i / maxIterations;
    }
}
