package io.github.ocelot.modelanima.api.common.molang;

@FunctionalInterface
public interface MolangJavaFunction
{
    float resolve(float[] parameters) throws RuntimeException;
}
