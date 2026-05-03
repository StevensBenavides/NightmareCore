package com.nightmare;

import org.bukkit.World;

public class WorldTime {

    public long full_time;
    public long day;

    WorldTime(World world) {
        this.full_time = world.getGameTime();
        this.day = this.full_time / 24000L;
    }

    public void update(long full_time) {
        this.full_time = full_time;
        this.day = this.full_time / 24000L;
    }

    public long getFullTime() {
        return this.full_time;
    }

    public long getDay() {
        return this.day;
    }

    public boolean isDayBelow50() {
        return this.day < 50;
    }

    public boolean isDayAbove50() {
        return this.day > 50;
    }

    public boolean isDayAbove(long reference) {
        return this.day > reference;
    }

    public boolean isDayBelow(long reference) {
        return this.day < reference;
    }
    
}
