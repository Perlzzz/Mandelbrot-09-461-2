package ru.gr0946x.ui;

// создаем класс окна для множества Жюлиа
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class JuliaSetWindow extends JFrame {
    private double cRe, cIm;  // комплексная константа для множества Жюлиа

    public JuliaSetWindow(double cRe, double cIm) {
        this.cRe = cRe;
        this.cIm = cIm;

        setTitle("Множество Жюлиа");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); //закрытие только этого окна
        setLocationRelativeTo(null);

        add(new JuliaSetPanel(cRe, cIm));
    }

    // внутренний класс панели для рисования множества Жюлиа
    class JuliaSetPanel extends JPanel {
        private double cRe, cIm;
        private BufferedImage image;

        public JuliaSetPanel(double cRe, double cIm) {
            this.cRe = cRe;
            this.cIm = cIm;
            // Инициализируем пустое изображение, заполним его позже
            image = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            // Отрисовываем подготовленное изображение на панели
            if (image != null) {
                g.drawImage(image, 0, 0, null);
            }
        }
    }
}