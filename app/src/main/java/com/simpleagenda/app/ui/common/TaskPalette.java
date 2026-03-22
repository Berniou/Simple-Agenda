package com.simpleagenda.app.ui.common;

import android.content.Context;

import androidx.annotation.ColorInt;
import androidx.core.content.ContextCompat;

import com.simpleagenda.app.R;

/**
 * Couleurs des pastels (maquette) indexées 0–4.
 */
public final class TaskPalette {

    private TaskPalette() {
    }

    public static final int COUNT = 5;

    @ColorInt
    public static int lightBackground(Context context, int colorIndex) {
        return ContextCompat.getColor(context, lightBackgroundRes(normalize(colorIndex)));
    }

    @ColorInt
    public static int accent(Context context, int colorIndex) {
        return ContextCompat.getColor(context, accentRes(normalize(colorIndex)));
    }

    private static int normalize(int colorIndex) {
        if (colorIndex < 0 || colorIndex >= COUNT) {
            return 0;
        }
        return colorIndex;
    }

    private static int lightBackgroundRes(int idx) {
        switch (idx) {
            case 0:
                return R.color.task_blue_light;
            case 1:
                return R.color.task_green_light;
            case 2:
                return R.color.task_orange_light;
            case 3:
                return R.color.task_coral_light;
            default:
                return R.color.task_gray_light;
        }
    }

    private static int accentRes(int idx) {
        switch (idx) {
            case 0:
                return R.color.task_blue_dark;
            case 1:
                return R.color.task_green_dark;
            case 2:
                return R.color.task_orange_dark;
            case 3:
                return R.color.task_coral_dark;
            default:
                return R.color.task_gray_dark;
        }
    }
}
