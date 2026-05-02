package ru.gr0946x.animation;

import java.util.ArrayList;
import java.util.List;

/*
  Класс для вычисления промежуточных кадров (интерполяции) между ключевыми кадрами
 */
public class FractalInterpolator {

    /*
     Генерирует список промежуточных кадров между двумя ключевыми кадрами
      @param start Начальный кадр
      @param end Конечный кадр
      @param steps Количество промежуточных шагов (кадров)
      @return Список интерполированных кадров
     */
    public List<KeyFrame> interpolate(KeyFrame start, KeyFrame end, int steps) {
        List<KeyFrame> frames = new ArrayList<>();
        
        for (int i = 0; i <= steps; i++) {
            double progress = (double) i / steps;
            
            double xMin = interpolateValue(start.getXMin(), end.getXMin(), progress);
            double xMax = interpolateValue(start.getXMax(), end.getXMax(), progress);
            double yMin = interpolateValue(start.getYMin(), end.getYMin(), progress);
            double yMax = interpolateValue(start.getYMax(), end.getYMax(), progress);
            
            frames.add(new KeyFrame(xMin, xMax, yMin, yMax));
        }
        
        return frames;
    }

    /*
      Линейная интерполяция (LERP) для одного значения
     */
    private double interpolateValue(double start, double end, double progress) {
        return start + (end - start) * progress;
    }
}
