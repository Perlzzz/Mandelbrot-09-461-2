package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.ui.fractals.ColorFunction;
import ru.gr0946x.ui.fractals.ColorSchemes;
import ru.gr0946x.ui.fractals.Fractal;
import ru.gr0946x.ui.fractals.Mandelbrot;
import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.FractalSaver;
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
        // Колёсико мыши — масштабирование
        mainPanel.addMouseWheelListener(e -> {
            pushHistory();
            double factor = (e.getWheelRotation() < 0) ? 0.9 : 1.1;
            double xCenter = conv.xScr2Crt(e.getX());
            double yCenter = conv.yScr2Crt(e.getY());
            double xMin = xCenter + (conv.getXMin() - xCenter) * factor;
            double xMax = xCenter + (conv.getXMax() - xCenter) * factor;
            double yMin = yCenter + (conv.getYMin() - yCenter) * factor;
            double yMax = yCenter + (conv.getYMax() - yCenter) * factor;
            conv.setXShape(xMin, xMax);
            conv.setYShape(yMin, yMax);
            mandelbrot.updateIterationsByZoom(xMax - xMin);
            mainPanel.repaint();
        });

// Правая кнопка мыши — сдвиг
        mainPanel.addMouseListener(new MouseAdapter() {
            private int lastX, lastY;

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    lastX = e.getX();
                    lastY = e.getY();
                    pushHistory();
                }
            }
        });

        mainPanel.addMouseMotionListener(new MouseAdapter() {
            private int lastX, lastY;

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    double shiftX = conv.xScr2Crt(0) - conv.xScr2Crt(dx);
                    double shiftY = conv.yScr2Crt(0) - conv.yScr2Crt(dy);
                    conv.setXShape(conv.getXMin() + shiftX, conv.getXMax() + shiftX);
                    conv.setYShape(conv.getYMin() + shiftY, conv.getYMax() + shiftY);
                    lastX = e.getX();
                    lastY = e.getY();
                    mainPanel.repaint();
                }
            }
        });
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

        FractalSaver saver = new FractalSaver(this, conv, mandelbrot, (FractalPainter) painter, mainPanel);

        saveFrac.addActionListener(e -> saver.saveFrac());
        saveJpg.addActionListener(e ->  saver.saveImage("jpg"));
        savePng.addActionListener(e ->  saver.saveImage("png"));
        open.addActionListener(e -> saver.openFrac());

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
