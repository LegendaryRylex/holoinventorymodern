package dev.rylex.holoinventorymodern.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

final class PeekCache<K> {

    static final long REQUEST_INTERVAL_MS = 1000L;
    static final long ENTRY_LIFETIME_MS = 2500L;

    private static final long REQUEST_LIFETIME_MS = 6000L;

    private final Map<K, Long> requested = new HashMap<>();
    private final Map<K, Entry> entries = new HashMap<>();

    record Entry(Component name, List<ItemStack> stacks, long receivedAt) {}

    boolean shouldRequest(K key, long now) {
        Long last = requested.get(key);
        if (last != null && now - last < REQUEST_INTERVAL_MS) {
            return false;
        }
        requested.put(key, now);
        return true;
    }

    void store(K key, Component name, List<ItemStack> stacks, long now) {
        entries.put(key, new Entry(name, stacks, now));
    }

    @Nullable
    Entry get(K key, long now) {
        Entry entry = entries.get(key);
        return entry != null && now - entry.receivedAt() <= ENTRY_LIFETIME_MS ? entry : null;
    }

    void expire(long now) {
        entries.values().removeIf(entry -> now - entry.receivedAt() > ENTRY_LIFETIME_MS);
        requested.values().removeIf(at -> now - at > REQUEST_LIFETIME_MS);
    }

    void clear() {
        entries.clear();
        requested.clear();
    }
}
