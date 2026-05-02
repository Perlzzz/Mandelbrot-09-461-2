package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PaintPanel extends JPanel {

    protected Painter painter;
    public PaintPanel(Painter painter){
        this.painter = painter;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                painter.setWidth(getWidth());
                painter.setHeight(getHeight());
                repaint();
            }
        });
        // слушатель событий мыши
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // Открытие окна множества Жюлиа по двойному клику левой кнопкой мыши
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    if (painter instanceof FractalPainter fp) {
                        var conv = fp.getConverter();
                        double cRe = conv.xScr2Crt(e.getX());
                        double cIm = conv.yScr2Crt(e.getY());
                        openJuliaSetWindow(cRe, cIm);
                    }
                }
            }
        });
    }
    private void openJuliaSetWindow(double cRe, double cIm) {
        JuliaSetWindow juliaWindow = new JuliaSetWindow(cRe, cIm);
        juliaWindow.setVisible(true);
    }
    @Override
    public void paint(Graphics g){
        super.paint(g);
        painter.paint(g);
    }


}