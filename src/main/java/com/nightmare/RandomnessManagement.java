package com.nightmare;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomnessManagement {

    private final Random random = new Random();
    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    public RandomnessManagement() {}

    public int random(int origin, int bound) {
        return threadLocalRandom.nextInt(origin, bound);
    }

    public float random(float origin, float bound) {
        return threadLocalRandom.nextFloat(origin, bound);
    }

    public boolean is1percent() {
        return threadLocalRandom.nextFloat() <= 0.01f;
    }

    public boolean is5percent() {
        return threadLocalRandom.nextFloat() <= 0.05f;
    }

    public boolean is10percent() {
        return threadLocalRandom.nextFloat() <= 0.10f;
    }

    public boolean is15percent() {
        return threadLocalRandom.nextFloat() <= 0.15f;
    }

    public boolean is25percent() {
        return threadLocalRandom.nextFloat() <= 0.25f;
    }

    public boolean is30percent() {
        return threadLocalRandom.nextFloat() <= 0.30f;
    }

    public boolean is40percent() {
        return threadLocalRandom.nextFloat() <= 0.40f;
    }

    public boolean is50percent() {
        return threadLocalRandom.nextFloat() <= 0.50f;
    }

    public boolean is70percent() {
        return threadLocalRandom.nextFloat() <= 0.70f;
    }

    public boolean is80percent() {
        return threadLocalRandom.nextFloat() <= 0.80f;
    }

    public Random getRandomInstance() {
        return random;
    }

    public ThreadLocalRandom getThreadLocalRandom() {
        return threadLocalRandom;
    }
}