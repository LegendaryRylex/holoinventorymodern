package dev.rylex.holoinventorymodern;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Rotation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = HoloInventoryModern.MOD_ID)
public final class HoloInventoryModernGameTests {

    private static final String SMALL = "empty_5x5x5";
    private static final int DEFAULT_TICKS = 100;

    private static final Map<Identifier, Consumer<GameTestHelper>> BODIES = new HashMap<>();

    private HoloInventoryModernGameTests() {}

    @SubscribeEvent
    static void registerInstanceType(RegisterEvent event) {
        event.register(
                Registries.TEST_INSTANCE_TYPE,
                registry -> registry.register(HoloInventoryModern.id("named"), HoloInventoryModernTestInstance.CODEC));
    }

    @SubscribeEvent
    static void registerTests(RegisterGameTestsEvent event) {
        BODIES.clear();
        ContainerPeekGameTest.register(new Batch(event, "holoinventorymodern_container_peek", SMALL));
    }

    static Consumer<GameTestHelper> body(Identifier name) {
        Consumer<GameTestHelper> body = BODIES.get(name);
        if (body == null) {
            throw new IllegalStateException("no game test body registered under " + name);
        }
        return body;
    }

    static final class Batch {

        private final RegisterGameTestsEvent event;
        private final String name;
        private final Identifier structure;
        private final Holder<TestEnvironmentDefinition<?>> environment;

        private Batch(RegisterGameTestsEvent event, String name, String structure) {
            this.event = event;
            this.name = name;
            this.structure = HoloInventoryModern.id(structure);
            this.environment = event.registerEnvironment(HoloInventoryModern.id(name));
        }

        Batch add(String test, Consumer<GameTestHelper> body) {
            Identifier id = HoloInventoryModern.id(name + "/" + test);
            BODIES.put(id, body);
            event.registerTest(
                    id,
                    new HoloInventoryModernTestInstance(
                            id, new TestData<>(environment, structure, DEFAULT_TICKS, 0, true, Rotation.NONE)));
            return this;
        }
    }
}
