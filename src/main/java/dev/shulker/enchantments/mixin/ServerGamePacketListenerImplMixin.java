package dev.shulker.enchantments.mixin;

import dev.shulker.enchantments.tick.ContainerInteractionTracker;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bug fix: "Refill triggers when the held item is MOVED, not only when consumed". Every
 * inventory move/drag/shift-click of the held stack goes through
 * {@code ServerGamePacketListenerImpl#handleContainerClick(ServerboundContainerClickPacket)} on
 * the server; genuine consumption (eating, shooting, placing, attacking) never does. We record
 * the current server tick as this player's "last container interaction tick" at HEAD, before
 * vanilla mutates any slots, so {@code RefillTickHandler} can suppress the tick-based Refill
 * trigger for that tick.
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleContainerClick", at = @At("HEAD"))
	private void shulker_enchantments$recordContainerInteraction(
		final ServerboundContainerClickPacket packet, final CallbackInfo ci
	) {
		if (this.player == null) {
			return;
		}
		int tick = this.player.level().getServer().getTickCount();
		ContainerInteractionTracker.recordInteraction(this.player.getUUID(), tick);
	}
}
