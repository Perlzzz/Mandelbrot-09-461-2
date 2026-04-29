package ru.gr0946x.ui;

import ru.gr0946x.Converter;
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
        setContent();
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
