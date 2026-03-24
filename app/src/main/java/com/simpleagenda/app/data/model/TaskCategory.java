package com.simpleagenda.app.data.model;

public enum TaskCategory {
    BLUE("Blue", "#87c8f6"),
    GREEN("Green", "#d0e8d0"),
    ORANGE("Orange", "#eedcff");
    
    private final String displayName;
    private final String color;
    
    TaskCategory(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getColor() {
        return color;
    }
}
