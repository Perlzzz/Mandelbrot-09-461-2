package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.animation.AnimationManager;
import ru.gr0946x.animation.KeyFrame;

import java.util.List;

import javax.swing.*;
import java.awt.*;

public class ExcursionWindow extends JDialog {
    private final AnimationManager animationManager;
    private final Converter conv;
    private final DefaultListModel<String> listModel = new DefaultListModel<>();
    public ExcursionWindow(JFrame parent, Converter conv, AnimationManager animationManager) {
        super(parent, "Экскурсия по фракталу", false);
        this.conv = conv;
        this.animationManager = animationManager;

        setSize(400, 400);
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
            int fps = 30;
            int stepsPerTransition = (duration * fps) /
                    Math.max(1, animationManager.getKeyFrames().size() - 1);
            List<KeyFrame> frames = animationManager.generateExcursion(stepsPerTransition);
            // TODO: сохранение видео — задача другого участника
            JOptionPane.showMessageDialog(this,
                    "Сгенерировано кадров: " + frames.size(),
                    "Готово", JOptionPane.INFORMATION_MESSAGE);
        });

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1, 4, 4));
        bottomPanel.add(durationPanel);
        bottomPanel.add(renderBtn);

        add(btnPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }
}
