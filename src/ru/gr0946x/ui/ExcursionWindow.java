package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.animation.AnimationManager;
import ru.gr0946x.animation.KeyFrame;

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

        add(addFrameBtn, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }
}
