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
            renderJuliaSet();
        }
        private void renderJuliaSet() {
            int width = image.getWidth();
            int height = image.getHeight();
            double zoom = 1.5;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    double zx = 1.5 * (x - width / 2.0) / (0.5 * zoom * width);
                    double zy = (y - height / 2.0) / (0.5 * zoom * height);
                    int iteration = 0;
                    int maxIterations = 300;

                    while (zx * zx + zy * zy < 4 && iteration < maxIterations) {
                        double temp = zx * zx - zy * zy + cRe;
                        zy = 2.0 * zx * zy + cIm;
                        zx = temp;
                        iteration++;
                    }

                    int color = iteration | (iteration << 8);
                    image.setRGB(x, y, iteration < maxIterations ? color : 0);
                }
            }
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