package com.nightmare;

public class PlayerTime {

    private long startTime;       
    private long thirstyStartTime; 

    public long minutes;
    public long seconds;
    public long thirstyMinutes;
    
    public long previousMinutes;
    public long previousSeconds;
    
    public PlayerTime() {
        long now = System.currentTimeMillis();
        
        this.startTime = now;
        this.thirstyStartTime = now;
        
        this.minutes = 0L;
        this.seconds = 0L;
        this.thirstyMinutes = 0L;
        this.previousMinutes = 0L;
        this.previousSeconds = 0L;
    }

    public void update() {
        long now = System.currentTimeMillis();

        this.previousMinutes = this.minutes;
        this.previousSeconds = this.seconds;

        long totalElapsedSecs = (now - this.startTime) / 1000;
        this.minutes = totalElapsedSecs / 60;
        this.seconds = totalElapsedSecs % 60;

        long thirstyElapsedSecs = (now - this.thirstyStartTime) / 1000;
        this.thirstyMinutes = thirstyElapsedSecs / 60;
    }

    public void resetThirstyMinutes() {
        this.thirstyStartTime = System.currentTimeMillis();
        this.thirstyMinutes = 0L;
    }

    public long getThirstyMinutes() { 
        return this.thirstyMinutes;
    }
    
    public long getPreviousMinutes() { 
        return this.previousMinutes; 
    }
    
    public long getPreviousSeconds() { 
        return this.previousSeconds;
    }
    
    public long getMinutes() { 
        return this.minutes; 
    }
    
    public long getSeconds() { 
        return this.seconds; 
    }
}