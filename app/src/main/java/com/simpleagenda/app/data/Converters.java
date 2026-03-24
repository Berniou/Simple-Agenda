package com.simpleagenda.app.data;

import androidx.room.TypeConverter;
import com.simpleagenda.app.data.model.TaskCategory;
import java.util.Date;

public class Converters {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @TypeConverter
    public static TaskCategory fromString(String value) {
        return value == null ? null : TaskCategory.valueOf(value);
    }

    @TypeConverter
    public static String taskCategoryToString(TaskCategory category) {
        return category == null ? null : category.name();
    }
}
