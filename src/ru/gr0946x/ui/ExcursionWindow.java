package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.animation.AnimationManager;
import ru.gr0946x.animation.KeyFrame;
import ru.gr0946x.animation.VideoRenderer;
import ru.gr0946x.ui.painting.FractalPainter;

import java.util.List;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;

public class ExcursionWindow extends JDialog {
    private final AnimationManager animationManager;
    private final Converter conv;
    private final FractalPainter painter;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    private final JProgressBar progressBar = new JProgressBar(0, 100);

    public ExcursionWindow(JFrame parent, Converter conv, AnimationManager animationManager, FractalPainter painter) {
        super(parent, "Экскурсия по фракталу", false);
        this.conv = conv;
        this.animationManager = animationManager;
        this.painter = painter;

        setSize(400, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));

        // список кадров
        JList<String> frameList = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(frameList);

        // кнопка добавить кадр
        JButton addFrameBtn = new JButton("Добавить текущий вид как ключевой кадр");
        addFrameBtn.addActionListener(e -> {
            KeyFrame frame = new KeyFrame(
                    conv.getXMin(), conv.getXMax(),
                    conv.getYMin(), conv.getYMax()
            );
            animationManager.addKeyFrame(frame);
            listModel.addElement(String.format(
                    "Кадр %d: x[%.3f, %.3f] y[%.3f, %.3f]",
                    listModel.size()+1,
                    frame.getXMin(), frame.getXMax(),
                    frame.getYMin(), frame.getYMax()
            ));
        });

        // кнопка удалить
        JButton removeFrameBtn = new JButton("Удалить выбранный");
        removeFrameBtn.addActionListener(e -> {
            int idx = frameList.getSelectedIndex();
            if (idx != -1) {
                listModel.remove(idx);
                // Синхронизируем менеджер со списком
                List<KeyFrame> currentFrames = animationManager.getKeyFrames();
                animationManager.clearKeyFrames();
                for (int i = 0; i < listModel.size(); i++) {
                    if (i < currentFrames.size() && i != idx) {
                        animationManager.addKeyFrame(currentFrames.get(i));
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Выберите кадр для удаления!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
            }
        });

        // поле длительности
        JPanel durationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        durationPanel.add(new JLabel("Длительность (сек):"));
        JTextField durationField = new JTextField("10", 5);
        durationPanel.add(durationField);

        // компоновка
        JPanel btnPanel = new JPanel(new GridLayout(1, 2, 4, 4));
        btnPanel.add(addFrameBtn);
        btnPanel.add(removeFrameBtn);

        // кнопка рендеринга
        JButton renderBtn = new JButton("Начать рендеринг видео");
        renderBtn.addActionListener(e -> {
            if (animationManager.getKeyFrames().size() < 2) {
                JOptionPane.showMessageDialog(this,
                        "Добавьте минимум 2 ключевых кадра!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int duration;
            try {
                duration = Integer.parseInt(durationField.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                        "Введите корректное число секунд!",
                        "Ошибка", JOptionPane.WARNING_MESSAGE);
                return;
            }

            JFileChooser chooser = new JFileChooser();chooser.setDialogTitle("Сохранить видео как MP4");
            if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
                File outputFile = chooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".mp4")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".mp4");
                }

                File finalOutputFile = outputFile;
                int finalDuration = duration;

                // Запускаем рендеринг в отдельном потоке
                new Thread(() -> {
                    SwingUtilities.invokeLater(() -> {
                        renderBtn.setEnabled(false);
                        addFrameBtn.setEnabled(false);
                        removeFrameBtn.setEnabled(false);
                        progressBar.setValue(0);
                        progressBar.setString("Подготовка...");
                    });

                    try {
                        int fps = 30;
                        int stepsPerTransition = (finalDuration * fps) /
                                Math.max(1, animationManager.getKeyFrames().size() - 1);
                        List<KeyFrame> frames = animationManager.generateExcursion(stepsPerTransition);
                        int totalFrames = frames.size();

                        VideoRenderer renderer = new VideoRenderer();
                        renderer.render(finalOutputFile, frames, painter, progress -> {
                            int currentFrame = (int) (progress * totalFrames);
                            SwingUtilities.invokeLater(() -> {
                                progressBar.setValue((int) (progress * 100));
                                progressBar.setString(String.format("Рендеринг: %d / %d", currentFrame, totalFrames));
                            });
                        });

                        SwingUtilities.invokeLater(() -> {
                            progressBar.setString("Готово!");
                            JOptionPane.showMessageDialog(this, "Видео успешно сохранено!", "Успех", JOptionPane.INFORMATION_MESSAGE);
                        });
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            progressBar.setString("Ошибка!");
                            JOptionPane.showMessageDialog(this, "Ошибка при сохранении видео: " + ex.getMessage(), "Ошибка", JOptionPane.ERROR_MESSAGE);
                        });
                    } finally {
                        SwingUtilities.invokeLater(() -> {
                            renderBtn.setEnabled(true);
                            addFrameBtn.setEnabled(true);
                            removeFrameBtn.setEnabled(true);
                        });
                    }
                }).start();
            }
        });

        progressBar.setStringPainted(true);
        progressBar.setPreferredSize(new Dimension(380, 25));

        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        durationPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        renderBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);

        bottomPanel.add(durationPanel);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(renderBtn);
        bottomPanel.add(Box.createVerticalStrut(5));
        bottomPanel.add(progressBar);

        add(btnPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}