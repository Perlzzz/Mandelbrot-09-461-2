package ru.gr0946x.animation;

import org.jcodec.api.awt.AWTSequenceEncoder;
import ru.gr0946x.Converter;
import ru.gr0946x.ui.painting.FractalPainter;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

/**
 * Класс для рендеринга последовательности кадров во фрактальное видео.
 */
public class VideoRenderer {

    /**
     * Рендерит видео из списка ключевых кадров.
     */
    public void render(File outputFile, List<KeyFrame> frames, FractalPainter painter, Consumer<Double> onProgress) throws IOException {
        if (frames == null || frames.isEmpty()) {
            throw new IOException("Список кадров пуст!");
        }

        int width = painter.getWidth();
        int height = painter.getHeight();

        // JCodec и многие видеокодеки требуют четных размеров кадра.
        if (width % 2 != 0) width--;
        if (height % 2 != 0) height--;

        if (width <= 0 || height <= 0) {
            throw new IOException("Некорректный размер кадра: " + width + "x" + height + ". Убедитесь, что окно фрактала достаточно большое.");
        }

        System.out.println("Начинается рендеринг видео: " + width + "x" + height + ", кадров: " + frames.size());

        AWTSequenceEncoder encoder = AWTSequenceEncoder.createSequenceEncoder(outputFile, 30); // 30 FPS

        try {
            Converter conv = painter.getConverter();

            for (int i = 0; i < frames.size(); i++) {
                KeyFrame frame = frames.get(i);

                // 1. Устанавливаем координаты кадра
                conv.setXShape(frame.getXMin(), frame.getXMax());
                conv.setYShape(frame.getYMin(), frame.getYMax());

                // 2. Отрисовываем фрактал СИНХРОННО
                BufferedImage fractalImage = painter.renderSync();
                if (fractalImage == null) continue;

                // 3. Копируем в TYPE_3BYTE_BGR (для JCodec)
                BufferedImage videoFrame = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
                Graphics2D g2d = videoFrame.createGraphics();
                g2d.drawImage(fractalImage, 0, 0, width, height, null);
                g2d.dispose();

                // 4. Добавляем кадр в видео
                encoder.encodeImage(videoFrame);

                // 5. Уведомляем о прогрессе
                if (onProgress != null) {
                    onProgress.accept((double) (i + 1) / frames.size());
                }

                if (i % 10 == 0 || i == frames.size() - 1) {
                    System.out.println("Отрендерено кадров: " + (i + 1) + " / " + frames.size());
                }
            }
        } finally {
            // САМОЕ ВАЖНОЕ: это сохранит видео, даже если произойдет сбой
            encoder.finish();
        }
        System.out.println("Рендеринг завершен. Файл сохранен: " + outputFile.getAbsolutePath());
    }
}