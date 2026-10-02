package dev.rylex.holoinventorymodern;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestInstance;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

public final class HoloInventoryModernTestInstance extends GameTestInstance {

    public static final MapCodec<HoloInventoryModernTestInstance> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                            Identifier.CODEC.fieldOf("name").forGetter(HoloInventoryModernTestInstance::name),
                            TestData.CODEC.forGetter(HoloInventoryModernTestInstance::data))
                    .apply(instance, HoloInventoryModernTestInstance::new));

    private final Identifier name;

    public HoloInventoryModernTestInstance(Identifier name, TestData<Holder<TestEnvironmentDefinition<?>>> info) {
        super(info);
        this.name = name;
    }

    @Override
    public void run(GameTestHelper helper) {
        HoloInventoryModernGameTests.body(name).accept(helper);
    }

    @Override
    public MapCodec<HoloInventoryModernTestInstance> codec() {
        return CODEC;
    }

    @Override
    protected MutableComponent typeDescription() {
        return Component.literal(HoloInventoryModern.MOD_ID);
    }

    private Identifier name() {
        return name;
    }

    private TestData<Holder<TestEnvironmentDefinition<?>>> data() {
        return info();
    }
}
