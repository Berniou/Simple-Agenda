package com.simpleagenda.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.simpleagenda.app.data.Converters;

import java.util.Date;

@Entity(tableName = "time_blocks")
@TypeConverters({Converters.class})
public class TimeBlock {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    
    private String title;
    private String description;
    private int hour; // 0-23
    private int duration; // en heures
    private TaskCategory category;
    private boolean isScheduled;
    private Date createdAt;
    private Date updatedAt;
    
    public TimeBlock() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isScheduled = false;
        this.category = TaskCategory.BLUE;
        this.duration = 1; // 1 heure par défaut
    }
    
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
        this.updatedAt = new Date();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = new Date();
    }
    
    public int getHour() {
        return hour;
    }
    
    public void setHour(int hour) {
        this.hour = hour;
        this.updatedAt = new Date();
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int duration) {
        this.duration = duration;
        this.updatedAt = new Date();
    }
    
    public TaskCategory getCategory() {
        return category;
    }
    
    public void setCategory(TaskCategory category) {
        this.category = category;
        this.updatedAt = new Date();
    }
    
    public boolean isScheduled() {
        return isScheduled;
    }
    
    public void setScheduled(boolean scheduled) {
        isScheduled = scheduled;
        this.updatedAt = new Date();
    }
    
    public Date getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
    
    public Date getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
