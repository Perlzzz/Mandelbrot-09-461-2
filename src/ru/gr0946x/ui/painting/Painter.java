package ru.gr0946x.ui.painting;

import java.awt.*;

public interface Painter {
    int getWidth();
    int getHeight();

    void setWidth(int width);
    void setHeight(int height);

    void paint(Graphics g);
    void renderAsync(Runnable oneDone);
}
