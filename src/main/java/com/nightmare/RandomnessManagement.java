package com.nightmare;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RandomnessManagement {

    private final Random random = new Random();
    private float current_value = 0.0f;
    private final ThreadLocalRandom threadLocalRandom = ThreadLocalRandom.current();

    RandomnessManagement RandomnessManagement() {
        current_value = random.nextFloat(0.0f, 1.0f);
        return this;
    }

    public int random(int origin, int bound) {
        return random.nextInt(origin, bound);
    }

    public float random(float origin, float bound) {
        return random.nextFloat(origin, bound);
    }

    public boolean is50percent() {
        return current_value * 100.0 <= 50.0;
    }

    public boolean is25percent() {
        return current_value * 100.0 <= 25.0;
    }

    public boolean is5percent() {
        return current_value * 100.0 <= 5.0;
    }

    public Random getRandomInstance() {
        return random;
    }

    public ThreadLocalRandom getThreadLocalRandom() {
        return threadLocalRandom;
    }
}
