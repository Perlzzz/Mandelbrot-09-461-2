package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Mandelbrot;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Properties;

public class FractalSaver {

    private final JFrame parent;
    private final Converter conv;
    private final Mandelbrot mandelbrot;
    private final FractalPainter painter;

    public FractalSaver(JFrame parent, Converter conv, Mandelbrot mandelbrot, FractalPainter painter) {
        this.parent = parent;
        this.conv = conv;
        this.mandelbrot = mandelbrot;
        this.painter = painter;
    }

    /** Часть 2: Сохранение параметров фрактала в .frac файл */
    public void saveFrac() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить фрактал");
        chooser.setFileFilter(new FileNameExtensionFilter("Файл фрактала (*.frac)", "frac"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = ensureExtension(chooser.getSelectedFile(), "frac");

        Properties props = new Properties();
        props.setProperty("xMin",    String.valueOf(conv.getXMin()));
        props.setProperty("xMax",    String.valueOf(conv.getXMax()));
        props.setProperty("yMin",    String.valueOf(conv.getYMin()));
        props.setProperty("yMax",    String.valueOf(conv.getYMax()));
        props.setProperty("maxIter", String.valueOf(mandelbrot.getMaxIterations()));

        try (FileOutputStream fos = new FileOutputStream(file)) {
            props.store(fos, "Mandelbrot Fractal Save File");
            JOptionPane.showMessageDialog(parent,
                    "Сохранено: " + file.getAbsolutePath(),
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Ошибка сохранения: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Часть 3: Сохранение изображения в jpg или png с подписью координат */
    public void saveImage(String format) {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Сохранить изображение");
        chooser.setFileFilter(new FileNameExtensionFilter(
                "Изображение " + format.toUpperCase() + " (*." + format + ")", format));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = ensureExtension(chooser.getSelectedFile(), format);

        // Получаем текущее изображение фрактала
        int w = painter.getWidth();
        int h = painter.getHeight();
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        painter.paint(image.getGraphics());

        // Добавляем подпись с координатами
        BufferedImage result = addCaption(image);

        try {
            ImageIO.write(result, format, file);
            JOptionPane.showMessageDialog(parent,
                    "Сохранено: " + file.getAbsolutePath(),
                    "Успех", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parent,
                    "Ошибка сохранения: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Рисует подпись с координатами внизу изображения */
    private BufferedImage addCaption(BufferedImage src) {
        int captionHeight = 28;
        BufferedImage result = new BufferedImage(
                src.getWidth(),
                src.getHeight() + captionHeight,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Копируем исходное изображение
        g.drawImage(src, 0, 0, null);

        // Фон подписи
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, src.getHeight(), src.getWidth(), captionHeight);

        // Текст с координатами
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        String caption = String.format(
                "Re: [%.6f .. %.6f]   Im: [%.6f .. %.6f]   iter: %d",
                conv.getXMin(), conv.getXMax(),
                conv.getYMin(), conv.getYMax(),
                mandelbrot.getMaxIterations());
        g.drawString(caption, 8, src.getHeight() + 18);
        g.dispose();

        return result;
    }

    /** Добавляет расширение к файлу если его нет или оно другое */
    private File ensureExtension(File file, String ext) {
        String name = file.getName();
        int dot = name.lastIndexOf('.');
        if (dot > 0 && !name.substring(dot + 1).equalsIgnoreCase(ext)) {
            name = name.substring(0, dot);
        }
        if (!name.toLowerCase().endsWith("." + ext)) {
            name = name + "." + ext;
        }
        return new File(file.getParentFile(), name);
    }
}
