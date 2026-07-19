package dev.shulker.enchantments.mixin;

import dev.shulker.enchantments.logic.PickupHandler;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Hooks the item-pickup path (spec 2.1) by redirecting the {@code Inventory#add} call that
 * {@link ItemEntity#playerTouch(Player)} makes just before normal placement. This only fires
 * once vanilla's own pickup-delay/target gating has already passed (it guards the very call
 * we're redirecting), so we don't need to reimplement that gating ourselves.
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
	@Redirect(
		method = "playerTouch",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z")
	)
	private boolean shulker_enchantments$siphonVacuumVoid(final Inventory inventory, final ItemStack itemStack, final Player player) {
		boolean fullyConsumed = PickupHandler.handlePickup(player, itemStack);
		if (fullyConsumed) {
			return true;
		}
		return inventory.add(itemStack);
	}
}
