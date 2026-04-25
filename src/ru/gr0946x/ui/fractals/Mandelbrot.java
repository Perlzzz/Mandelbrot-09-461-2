package ru.gr0946x.ui.fractals;

import ru.smak.math.Complex;

import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class Mandelbrot implements Fractal{

    private int maxIterations = 100;
    private double R2 = 4;
    public double getR(){
        return sqrt(R2);
    }

    @Override
    public float inSetProbability(double x, double y) {
        var c = new Complex(x, y);
        var z = new Complex();
        int i = 0;
        while (z.getAbsoluteValue2() < R2 && ++i < maxIterations){
            z.timesAssign(z);
            z.plusAssign(c);
        }
        return (float)i / maxIterations;
    }
}
