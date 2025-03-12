package net.blay09.mods.farmingforblockheads.network;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import static net.blay09.mods.farmingforblockheads.FarmingForBlockheads.id;

/**
 * @deprecated TODO This shouldn't be necessary, we can use sendParticle on the server
 */
@Deprecated
public record ChickenNestEffectMessage(BlockPos pos) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ChickenNestEffectMessage> TYPE = new CustomPacketPayload.Type<>(id("chicken_nest_effect"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ChickenNestEffectMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            ChickenNestEffectMessage::pos,
            ChickenNestEffectMessage::new
    );

    public static void handle(Player player, ChickenNestEffectMessage message) {
        player.level().addParticle(ParticleTypes.EXPLOSION, message.pos.getX() + 0.5f, message.pos.getY() + 0.5f, message.pos.getZ() + 0.5f, 0, 0, 0);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
