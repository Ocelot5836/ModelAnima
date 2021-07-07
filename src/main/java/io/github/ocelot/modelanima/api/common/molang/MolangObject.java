package io.github.ocelot.modelanima.api.common.molang;

import io.github.ocelot.modelanima.api.common.molang.MolangExpression;

public interface MolangObject
{
    void set(String name, MolangExpression value);

    MolangExpression get(String name);

    boolean has(String name);
}
