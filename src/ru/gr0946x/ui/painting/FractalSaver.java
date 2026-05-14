package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorSchemes;
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
    private final Converter conv;   //координаты области , масштаб фрактала
    private final Mandelbrot mandelbrot;
    private final FractalPainter painter;
    private final JPanel panel;
    private final JComboBox<ColorSchemes> colorSchemeBox;

    public FractalSaver(JFrame parent, Converter conv, Mandelbrot mandelbrot,
                        FractalPainter painter, JPanel panel, JComboBox<ColorSchemes> colorSchemeBox) {
        this.parent = parent;
        this.conv = conv;
        this.mandelbrot = mandelbrot;
        this.painter = painter;
        this.panel = panel;
        this.colorSchemeBox = colorSchemeBox;
    }

    /**Сохранение параметров фрактала в .frac файл */
    public void saveFrac() {   //конструктор
        JFileChooser chooser = new JFileChooser();   //окно выбора файла
        chooser.setDialogTitle("Сохранить фрактал");
        chooser.setFileFilter(new FileNameExtensionFilter("Файл фрактала (*.frac)", "frac"));
        chooser.setAcceptAllFileFilterUsed(false);    //юзер не сможет выбрать all files

        if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) return; //нажал ли пользователь сохранить

        File file = ensureExtension(chooser.getSelectedFile(), "frac"); //расширение, автоматически добавляет
        System.out.println("Путь сохранения: " + file.getAbsolutePath());

        Properties props = new Properties();
        props.setProperty("xMin",    String.valueOf(conv.getXMin()));
        props.setProperty("xMax",    String.valueOf(conv.getXMax()));
        props.setProperty("yMin",    String.valueOf(conv.getYMin()));
        props.setProperty("yMax",    String.valueOf(conv.getYMax()));
        props.setProperty("maxIter", String.valueOf(mandelbrot.getMaxIterations()));
        props.setProperty("colorScheme", colorSchemeBox.getSelectedItem().toString());

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

    /**Сохранение изображения в jpg или png с подписью координат */
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
        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);  //создается пустое изображение
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

        Graphics2D g = result.createGraphics(); //создает объект для рисования на изображении result
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);//сглаживание графики

        // Копируем исходное изображение
        g.drawImage(src, 0, 0, null);

        // Фон подписи
        g.setColor(new Color(20, 20, 20));
        g.fillRect(0, src.getHeight(), src.getWidth(), captionHeight);

        // Текст с координатами
        g.setColor(Color.WHITE);
        g.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        String caption = String.format(
                "Re: [%.6f .. %.6f]   Im: [%.6f .. %.6f]   iter: %d", //действительная , мнимая , итерации
                conv.getXMin(), conv.getXMax(),
                conv.getYMin(), conv.getYMax(),
                mandelbrot.getMaxIterations());
        g.drawString(caption, 8, src.getHeight() + 18);
        g.dispose();

        return result;
    }

    /** Добавляет расширение к файлу если его нет или оно другое */
    private File ensureExtension(File file, String ext) {  //пользователь ввел mandel -> mandel.png
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
    public void openFrac() { //открывает сохраненный фрактал
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Открыть фрактал");
        chooser.setFileFilter(new FileNameExtensionFilter("Файл фрактала (*.frac)", "frac"));
        chooser.setAcceptAllFileFilterUsed(false);

        if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        Properties props = new Properties();                                        //загружает параметры

        try (FileInputStream fis = new FileInputStream(file)) {
            props.load(fis);                                                   //преобразуем из строк в числа
            double xMin    = Double.parseDouble(props.getProperty("xMin"));
            double xMax    = Double.parseDouble(props.getProperty("xMax"));
            double yMin    = Double.parseDouble(props.getProperty("yMin"));
            double yMax    = Double.parseDouble(props.getProperty("yMax"));
            int    maxIter = Integer.parseInt(props.getProperty("maxIter"));

            conv.setXShape(xMin, xMax);    //восстановление параметров
            conv.setYShape(yMin, yMax);
            mandelbrot.setMaxIterations(maxIter);            //востановление итераций

            String schemeName = props.getProperty("colorScheme");
            if (schemeName != null) {
                for (int i = 0; i < colorSchemeBox.getItemCount(); i++) {
                    if (colorSchemeBox.getItemAt(i).toString().equals(schemeName)) {
                        colorSchemeBox.setSelectedIndex(i);
                        break;
                    }
                }
            }
            // перерисовываем фрактал
            painter.getConverter().setXShape(xMin, xMax);
            painter.getConverter().setYShape(yMin, yMax);


            JOptionPane.showMessageDialog(parent,
                    "Открыто: " + file.getName(),
                    "Успех", JOptionPane.INFORMATION_MESSAGE);

            painter.renderAsync(() -> panel.repaint());    //заново вычисляется и отображается
            System.out.println("Загружено: xMin=" + xMin + " xMax=" + xMax + " yMin=" + yMin + " yMax=" + yMax);
        } catch (IOException | NumberFormatException e) {
            JOptionPane.showMessageDialog(parent,
                    "Ошибка открытия: " + e.getMessage(),
                    "Ошибка", JOptionPane.ERROR_MESSAGE);
        }
    }
}
