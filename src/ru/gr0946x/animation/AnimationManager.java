package ru.gr0946x.animation;

import java.util.ArrayList;
import java.util.List;


    //Менеджер анимации, управляющий списком ключевых кадров
    //и генерацией полной последовательности кадров для экскурсии

public class AnimationManager {
    private final List<KeyFrame> keyFrames = new ArrayList<>();
    private final FractalInterpolator interpolator = new FractalInterpolator();


      //Добавить ключевой кадр в список.

    public void addKeyFrame(KeyFrame frame) {
        keyFrames.add(frame);
    }


    //Очистить все ключевые кадры.

    public void clearKeyFrames() {
        keyFrames.clear();
    }


      //Генерирует полную последовательность кадров для всей экскурсии.

      //@param stepsPerTransition Количество промежуточных кадров между каждой парой ключевых точек.
      //@return Полный список кадров для рендеринга видео.

    public List<KeyFrame> generateExcursion(int stepsPerTransition) {
        List<KeyFrame> fullPath = new ArrayList<>();
        
        if (keyFrames.size() < 2) {
            return fullPath;
        }

        for (int i = 0; i < keyFrames.size() - 1; i++) {
            List<KeyFrame> segment = interpolator.interpolate(
                keyFrames.get(i), 
                keyFrames.get(i + 1), 
                stepsPerTransition
            );
            
            // Чтобы избежать дублирования конечного кадра сегмента и начального кадра следующего
            if (i > 0 && !segment.isEmpty()) {
                segment.remove(0);
            }
            
            fullPath.addAll(segment);
        }
        
        return fullPath;
    }

    public List<KeyFrame> getKeyFrames() {
        return new ArrayList<>(keyFrames);
    }
}
