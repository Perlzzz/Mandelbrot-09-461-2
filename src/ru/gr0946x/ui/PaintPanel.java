package ru.gr0946x.ui;

import ru.gr0946x.ui.painting.Painter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class PaintPanel extends JPanel {

    private Painter painter;
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
                // Проверка правой кнопки мыши согласно пункту 1 и 8
                if (SwingUtilities.isRightMouseButton(e)) {
                    double mouseX = e.getX();
                    double mouseY = e.getY();

                    // Преобразование экранных координат в комплексные для константы C
                    double cRe = (mouseX - getWidth() / 2.0) / 100.0;
                    double cIm = (mouseY - getHeight() / 2.0) / 100.0;

                    openJuliaSetWindow(cRe, cIm);
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