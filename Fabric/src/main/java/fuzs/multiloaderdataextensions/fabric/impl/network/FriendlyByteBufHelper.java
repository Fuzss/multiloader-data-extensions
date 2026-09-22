package fuzs.multiloaderdataextensions.fabric.impl.network;

import com.google.common.collect.Maps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import org.apache.commons.lang3.function.TriConsumer;

import java.util.Map;
import java.util.function.BiFunction;

/**
 * Fabric replacement for the key-aware {@code FriendlyByteBuf#readMap} / {@code FriendlyByteBuf#writeMap} overloads
 * that NeoForge adds to the vanilla buffer.
 */
public final class FriendlyByteBufHelper {
    private FriendlyByteBufHelper() {}

    public static <K, V> Map<K, V> readMap(FriendlyByteBuf friendlyByteBuf, StreamDecoder<? super FriendlyByteBuf, K> keyReader, BiFunction<FriendlyByteBuf, K, V> valueReader) {
        final int size = friendlyByteBuf.readVarInt();
        final Map<K, V> map = Maps.newHashMapWithExpectedSize(size);

        for (int i = 0; i < size; ++i) {
            final K k = keyReader.decode(friendlyByteBuf);
            map.put(k, valueReader.apply(friendlyByteBuf, k));
        }

        return map;
    }

    public static <K, V> void writeMap(FriendlyByteBuf friendlyByteBuf, Map<K, V> map, StreamEncoder<? super FriendlyByteBuf, K> keyWriter, TriConsumer<FriendlyByteBuf, K, V> valueWriter) {
        friendlyByteBuf.writeVarInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(friendlyByteBuf, key);
            valueWriter.accept(friendlyByteBuf, key, value);
        });
    }
}
