package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.FractalPainter;
import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class PaintPanel extends JPanel {

    protected Painter painter;
    private Point lastPoint;

    public PaintPanel(Painter painter){
        this.painter = painter;
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                super.componentResized(e);
                painter.setWidth(getWidth());
                painter.setHeight(getHeight());
//                repaint();
                if (painter instanceof FractalPainter fp) {
                    fp.renderAsync(() -> repaint());
                } else {
                    repaint();
                }
            }
        });
        // слушатель событий мыши
        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = e.getPoint();
                }

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

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    lastPoint = null;
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e) && lastPoint != null) {
                    if (painter instanceof FractalPainter fp) {
                        var conv = fp.getConverter();

                        double dx = conv.xScr2Crt(lastPoint.x) - conv.xScr2Crt(e.getX());
                        double dy = conv.yScr2Crt(lastPoint.y) - conv.yScr2Crt(e.getY());

                        conv.setXShape(conv.getXMin() + dx, conv.getXMax() + dx);
                        conv.setYShape(conv.getYMin() + dy, conv.getYMax() + dy);

                        lastPoint = e.getPoint();
                        fp.renderAsync(() -> repaint());
                    }
                }
            }
        };

        addMouseListener(ma);
        addMouseMotionListener(ma);
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