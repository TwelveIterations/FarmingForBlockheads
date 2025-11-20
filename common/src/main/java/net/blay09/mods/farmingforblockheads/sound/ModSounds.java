package net.blay09.mods.farmingforblockheads.sound;

import net.blay09.mods.balm.core.BalmRegistrar;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;

public class ModSounds {
    public static Holder<SoundEvent> falling;

    public static void initialize(BalmRegistrar.Scoped<SoundEvent> sounds) {
        falling = sounds.register("falling", SoundEvent::createVariableRangeEvent);
    }

}
