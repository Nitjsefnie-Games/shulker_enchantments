package dev.nitjsefnie.shulkerenchantments.mixin;

import dev.nitjsefnie.shulkerenchantments.tick.ContainerInteractionTracker;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Bug fix: "Refill triggers when the held item is MOVED, not only when consumed". Moving/
 * dragging/shift-clicking an item on the server goes through one of two packet handlers -
 * {@code handleContainerClick} (survival and normal container UIs) or
 * {@code handleSetCreativeModeSlot} (the creative inventory). Genuine consumption (eating,
 * shooting, placing, attacking) goes through neither. We record the current server tick as
 * this player's "last container interaction tick" for both, so {@code RefillTickHandler} can
 * suppress the tick-based Refill trigger for the count drop that move produces (which it
 * observes on the following tick - see {@link ContainerInteractionTracker}).
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handleContainerClick", at = @At("HEAD"))
	private void shulker_enchantments_reborn$recordContainerClick(
		final ServerboundContainerClickPacket packet, final CallbackInfo ci
	) {
		if (this.player == null) {
			return;
		}
		ContainerInteractionTracker.recordInteraction(this.player.getUUID(), this.player.level().getServer().getTickCount());
	}

	@Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"))
	private void shulker_enchantments_reborn$recordCreativeSlot(
		final ServerboundSetCreativeModeSlotPacket packet, final CallbackInfo ci
	) {
		if (this.player == null) {
			return;
		}
		ContainerInteractionTracker.recordInteraction(this.player.getUUID(), this.player.level().getServer().getTickCount());
	}
}
