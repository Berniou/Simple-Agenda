package com.simpleagenda.app.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.simpleagenda.app.data.Converters;

import java.util.Date;

@Entity(tableName = "tasks")
@TypeConverters({Converters.class})
public class Task {
    @PrimaryKey(autoGenerate = true)
    private Long id;
    
    private String title;
    private String description;
    private Date startTime;
    private Date endTime;
    private TaskCategory category;
    private boolean isCompleted;
    private Date createdAt;
    private Date updatedAt;
    
    public Task() {
        this.createdAt = new Date();
        this.updatedAt = new Date();
        this.isCompleted = false;
        this.category = TaskCategory.BLUE;
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
    
    public Date getStartTime() {
        return startTime;
    }
    
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
        this.updatedAt = new Date();
    }
    
    public Date getEndTime() {
        return endTime;
    }
    
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
        this.updatedAt = new Date();
    }
    
    public TaskCategory getCategory() {
        return category;
    }
    
    public void setCategory(TaskCategory category) {
        this.category = category;
        this.updatedAt = new Date();
    }
    
    public boolean isCompleted() {
        return isCompleted;
    }
    
    public void setCompleted(boolean completed) {
        isCompleted = completed;
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
