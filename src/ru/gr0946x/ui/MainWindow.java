package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.animation.AnimationManager;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.ColorSchemes;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static java.lang.Math.*;

import java.util.ArrayDeque;
import javax.swing.KeyStroke;

public class MainWindow extends JFrame {

    private final SelectablePanel mainPanel;
    private final Painter painter;
    private Mandelbrot mandelbrot;
    private final Converter conv;
    private ColorFunction selectedColorScheme = ColorSchemes.CLASSIC;
    private final AnimationManager animationManager = new AnimationManager();
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter(
                (x, y) -> mandelbrot.inSetProbability(x, y),
                conv,
                (value) -> selectedColorScheme.getColor(value)
        );
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.addSelectListener((r)->{
            pushHistory(); // сохраняем перед изменением
            var xMin = conv.xScr2Crt(r.x);
            var xMax = conv.xScr2Crt(r.x + r.width);
            var yMin = conv.yScr2Crt(r.y + r.height);
            var yMax = conv.yScr2Crt(r.y);
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);

            // фикс пропорций — подгоняем Y под соотношение сторон панели
            double panelRatio = (double) mainPanel.getWidth() / mainPanel.getHeight();
            double fractalWidth = xMax - xMin;
            double fractalHeight = fractalWidth / panelRatio;
            double yCenter = (yMin + yMax) / 2.0;
            yMin = yCenter - fractalHeight / 2.0;
            yMax = yCenter + fractalHeight / 2.0;

            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mandelbrot.updateIterationsByZoom(fractalWidth);
            mainPanel.repaint();

            // пункт 10
            double newWidth = xMax - xMin; // новая ширина фрактала
            mandelbrot.updateIterationsByZoom(newWidth);
            mainPanel.repaint();
        });
        setupMenu();
        setContent();
    }

    // каждый элемент — снимок состояния: xMin, xMax, yMin, yMax
    private final ArrayDeque<double[]> history = new ArrayDeque<>();

    private void pushHistory() {
        // сохраняем текущее состояние перед каждым зумом/сдвигом
        history.push(new double[]{
                conv.getXMin(), conv.getXMax(),
                conv.getYMin(), conv.getYMax()
        });
        // ограничиваем историю 100 шагами (пункт 7 лабы)
        if (history.size() > 100) history.removeLast();
    }


    private void setupMenu() {
        JMenuBar menuBar = new JMenuBar();

        // файл
        JMenu fileMenu = new JMenu("Файл");
        JMenuItem saveFrac = new JMenuItem("Сохранить как .frac");
        JMenuItem saveJpg = new JMenuItem("Сохранить как JPG");
        JMenuItem savePng = new JMenuItem("Сохранить как PNG");
        JMenuItem open = new JMenuItem("Открыть .frac");

        // заглушки — ActionListener-ы добавят участники 1 и 2
        saveFrac.addActionListener(e -> { /* TODO: участник 1 */ });
        saveJpg.addActionListener(e ->  { /* TODO: участник 2 */ });
        savePng.addActionListener(e ->  { /* TODO: участник 2 */ });
        open.addActionListener(e ->     { /* TODO: участник 1 */ });

        fileMenu.add(saveFrac);
        fileMenu.add(saveJpg);
        fileMenu.add(savePng);
        fileMenu.addSeparator();
        fileMenu.add(open);

        // Правка
        JMenu editMenu = new JMenu("Правка");

        JMenuItem undo = new JMenuItem("Отменить");
        undo.setAccelerator(KeyStroke.getKeyStroke("ctrl Z")); // Ctrl+Z
        undo.addActionListener(e -> {
            if (!history.isEmpty()) {
                double[] prev = history.pop();
                conv.setXShape(prev[0], prev[1]);
                conv.setYShape(prev[2], prev[3]);
                mainPanel.repaint();
            }
        });
        editMenu.add(undo);


        menuBar.add(fileMenu);
        menuBar.add(editMenu);

        JMenu excursionMenu = new JMenu("Экскурсия");
        JMenuItem openExcursion = new JMenuItem("Управление экскурсией...");
        openExcursion.addActionListener(e -> {
            ExcursionWindow excursionWindow = new ExcursionWindow(this, conv, animationManager, (FractalPainter) painter);
            excursionWindow.setVisible(true);
        });
        excursionMenu.add(openExcursion);
        menuBar.add(excursionMenu);

        setJMenuBar(menuBar);
    }

    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);

        var colorSchemeBox = new JComboBox<>(ColorSchemes.values());
        colorSchemeBox.addActionListener(e -> {
            selectedColorScheme = (ColorFunction) colorSchemeBox.getSelectedItem();
            mainPanel.repaint();
        });

        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(colorSchemeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addGroup(gl.createParallelGroup()
                        .addComponent(colorSchemeBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                )
                .addGap(8)
        );
    }
}