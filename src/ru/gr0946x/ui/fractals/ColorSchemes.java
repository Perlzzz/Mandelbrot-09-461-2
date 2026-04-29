package ru.gr0946x.ui.fractals;

import java.awt.Color;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

public enum ColorSchemes implements ColorFunction {
    CLASSIC("Классическая") {
        @Override
        public Color getColor(float value) {
            if (value >= 1.0f) return Color.BLACK;

            float r = (float) abs(sin(5 * value));
            float g = (float) abs(cos(8 * value) * sin(3 * value));
            float b = (float) abs((sin(7 * value) + cos(15 * value)) / 2f);
            return new Color(r, g, b);
        }
    },

    FIRE("Огонь") {
        @Override
        public Color getColor(float value) {
            if (value >= 1.0f) return Color.BLACK;

            int red = clamp((int) (255 * value * 3));
            int green = clamp((int) (255 * value * value * 2));
            int blue = clamp((int) (80 * value));
            return new Color(red, green, blue);
        }
    },

    OCEAN("Океан") {
        @Override
        public Color getColor(float value) {
            if (value >= 1.0f) return Color.BLACK;

            int red = clamp((int) (60 * value));
            int green = clamp((int) (180 * value));
            int blue = clamp((int) (120 + 135 * value));
            return new Color(red, green, blue);
        }
    },

    GRAYSCALE("Серая") {
        @Override
        public Color getColor(float value) {
            if (value >= 1.0f) return Color.BLACK;

            int gray = clamp((int) (255 * value));
            return new Color(gray, gray, gray);
        }
    };

    private final String title;

    ColorSchemes(String title) {
        this.title = title;
    }

    @Override
    public String toString() {
        return title;
    }

    private static int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
}
