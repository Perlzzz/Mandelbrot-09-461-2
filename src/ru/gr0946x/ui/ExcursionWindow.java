package ru.gr0946x.ui;

import ru.gr0946x.Converter;
import ru.gr0946x.animation.AnimationManager;

import javax.swing.*;
import java.awt.*;

public class ExcursionWindow extends JDialog {
    private final AnimationManager animationManager;
    private final Converter conv;

    public ExcursionWindow(JFrame parent, Converter conv, AnimationManager animationManager) {
        super(parent, "Эксукрсия по фракталу", false);
        this.conv = conv;
        this.animationManager = animationManager;

        setSize(400, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));
    }
}
