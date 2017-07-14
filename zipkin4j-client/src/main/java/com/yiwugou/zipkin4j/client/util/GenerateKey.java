package com.yiwugou.zipkin4j.client.util;

import java.util.Random;

public class GenerateKey {
    private static Random RANDOM = new Random();

    public static Long longKey() {
        return RANDOM.nextLong();
    }
}
