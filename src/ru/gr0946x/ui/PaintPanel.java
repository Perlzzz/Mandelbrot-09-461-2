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
                // тут логика
            }
        });
    }
    @Override
    public void paint(Graphics g){
        super.paint(g);
        painter.paint(g);
    }


}