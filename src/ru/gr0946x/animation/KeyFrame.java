package ru.gr0946x.animation;

/**
 * Класс, представляющий ключевой кадр анимации.
 * Хранит координаты области просмотра фрактала.
 */
public class KeyFrame {
    private final double xMin;
    private final double xMax;
    private final double yMin;
    private final double yMax;

    public KeyFrame(double xMin, double xMax, double yMin, double yMax) {
        this.xMin = xMin;
        this.xMax = xMax;
        this.yMin = yMin;
        this.yMax = yMax;
    }

    public double getXMin() { return xMin; }
    public double getXMax() { return xMax; }
    public double getYMin() { return yMin; }
    public double getYMax() { return yMax; }
}
