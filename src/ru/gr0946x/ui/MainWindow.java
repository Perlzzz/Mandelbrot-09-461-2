package ru.gr0946x.ui;

import ru.gr0946x.Converter;
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
    public MainWindow(){
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 650));
        mandelbrot = new Mandelbrot();
        conv = new Converter(-2.0, 1.0, -1.0, 1.0);
        painter = new FractalPainter((x, y) -> mandelbrot.inSetProbability(x, y), conv, (value)->{
            if (value == 1.0) return Color.BLACK;
            var r = (float)abs(sin(5 * value));
            var g = (float)abs(cos(8 * value) * sin (3 * value));
            var b = (float)abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        });
        mainPanel = new SelectablePanel(painter);
        mainPanel.setBackground(Color.WHITE);
        mainPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    // Используем наш конвертер conv, чтобы получить точные координаты если нажата правая кнопка
                    double cRe = conv.xScr2Crt(e.getX());
                    double cIm = conv.yScr2Crt(e.getY());

                    // открываем окно Жюлиа
                    JuliaSetWindow juliaWindow = new JuliaSetWindow(cRe, cIm);
                    juliaWindow.setVisible(true);
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
        setJMenuBar(menuBar);
    }

    private void setContent(){
        var gl = new GroupLayout(getContentPane());
        setLayout(gl);
        gl.setVerticalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
        gl.setHorizontalGroup(gl.createSequentialGroup()
                .addGap(8)
                .addComponent(mainPanel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE)
                .addGap(8)
        );
    }
}
