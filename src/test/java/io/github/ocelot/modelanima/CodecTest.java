package io.github.ocelot.modelanima;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTDynamicOps;

import java.util.Optional;

public class CodecTest
{
    public static void test()
    {
        CompoundNBT nbt = new CompoundNBT();
        nbt.putBoolean("foo", true);
        nbt.putIntArray("bar", new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9});

        CompoundNBT blockStateNBT = new CompoundNBT();
        blockStateNBT.putString("Name", "diamond_block");
        nbt.put("blockstate_example", blockStateNBT);

        Optional<Foobar> nbtOptional = Foobar.CODEC.parse(NBTDynamicOps.INSTANCE, nbt).get().left();

        JsonObject json = new JsonObject();
        json.addProperty("foo", true);

        JsonArray barJson = new JsonArray();
        barJson.add(0);
        barJson.add(2);
        barJson.add(3);
        barJson.add(7);
        barJson.add(8);
        barJson.add(13);

        json.add("bar", barJson);

        JsonObject blockStateJson = new JsonObject();
        blockStateJson.addProperty("Name", "stone");
        json.add("blockstate_example", blockStateJson);

        Optional<Foobar> jsonOptional = Foobar.CODEC.parse(JsonOps.INSTANCE, json).get().left();

        System.out.println("NBT");
        nbtOptional.ifPresent(System.out::println);
        System.out.println("JSON");
        jsonOptional.ifPresent(System.out::println);
    }
}
