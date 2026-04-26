package ru.gr0946x.ui;

// создаем класс окна для множества Жюлиа
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JuliaSetWindow extends JFrame {
    private double cRe, cIm;  // комплексная константа для множества Жюлиа

    // внутренний класс панели для рисования множества Жюлиа
    class JuliaSetPanel extends JPanel {
        private double cRe, cIm;
        private BufferedImage image;
    }
}