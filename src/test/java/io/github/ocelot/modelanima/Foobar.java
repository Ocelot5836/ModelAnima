package io.github.ocelot.modelanima;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

public class Foobar
{
    public static final Codec<Foobar> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("foo").forGetter(Foobar::isFoo),
                    Codec.INT_STREAM.xmap(IntStream::toArray, Arrays::stream).optionalFieldOf("bar", new int[0]).forGetter(Foobar::getBar),
                    BlockState.CODEC.optionalFieldOf("blockstate_example").forGetter(Foobar::getBlockState)
            ).apply(instance, (aBoolean, integers, blockState) -> new Foobar(aBoolean, integers, blockState.orElse(null)))
    );

    private final boolean foo;
    private final int[] bar;
    private final BlockState blockState;

    public Foobar(boolean foo, int[] bar, BlockState blockState)
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Foobar foobar = (Foobar) o;
        return foo == foobar.foo &&
                Arrays.equals(bar, foobar.bar) &&
                Objects.equals(blockState, foobar.blockState);
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hash(foo, blockState);
        result = 31 * result + Arrays.hashCode(bar);
        return result;
    }

    @Override
    public String toString()
    {
        return "Foobar{" +
                "foo=" + foo +
                ", bar=" + Arrays.toString(bar) +
                ", blockState=" + blockState +
                '}';
    }
}
