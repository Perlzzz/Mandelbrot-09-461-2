package ru.gr0946x.ui.painting;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.Mandelbrot;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.*;
import java.util.Properties;

public class FractalSaver {

    private final JFrame parent;
    private final Converter conv;
    private final Mandelbrot mandelbrot;

    public FractalSaver(JFrame parent, Converter conv, Mandelbrot mandelbrot) {
        this.parent = parent;
        this.conv = conv;
        this.mandelbrot = mandelbrot;
    }

    /** Сохранение параметров фрактала в .frac файл */
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