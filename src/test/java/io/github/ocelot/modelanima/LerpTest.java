package io.github.ocelot.modelanima;

public class LerpTest
{
    private static final float[] VALUES = {0, 2, 3, 2, 3, 1};

    public static void main(String[] args)
    {
        float t = 0;

        System.out.println(catmullRom(1, 0.5F));
    }

    private static float catmullRom(int index, float t)
    {
        float P0 = VALUES[index - 1];
        float P1 = VALUES[index];
        float P2 = VALUES[index + 1];
        float P3 = VALUES[index + 2];

        return 0.5F * ((2 * P1) + (-P0 + P2) * t + (2 * P0 - 5 * P1 + 4 * P2 - P3) * t * t + (-P0 + 3 * P1 - 3 * P2 + P3) * t * t * t);
    }
}
