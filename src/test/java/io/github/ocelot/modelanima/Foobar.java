package io.github.ocelot.modelanima;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.block.BlockState;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.IntStream;

public class Foobar
{
    public static final Codec<Foobar> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("foo").forGetter(Foobar::isFoo),
                    Codec.INT_STREAM.xmap(IntStream::toArray, Arrays::stream).fieldOf("bar").withDefault(() -> new int[0]).forGetter(Foobar::getBar),
                    BlockState.BLOCKSTATE_CODEC.optionalFieldOf("blockstate_example").forGetter(Foobar::getBlockState)
            ).apply(instance, (aBoolean, integers, blockState) -> new Foobar(aBoolean, integers, blockState.orElse(null)))
    );

    private final boolean foo;
    private final int[] bar;
    private final BlockState blockState;

    public Foobar(boolean foo, int[] bar, @Nullable BlockState blockState)
    {
        this.foo = foo;
        this.bar = bar;
        this.blockState = blockState;
    }

    public boolean isFoo()
    {
        return foo;
    }

    public int[] getBar()
    {
        return bar;
    }

    public Optional<BlockState> getBlockState()
    {
        return Optional.ofNullable(this.blockState);
    }
}
