package dev.shulker.enchantments.mixin;

import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import dev.shulker.enchantments.logic.RefillLogic;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Projectile-consumption Refill trigger (spec 2.3/4.2 "Projectile-consumption trigger
 * specifics"). {@code ProjectileWeaponItem#useAmmo} is the single choke point both BowItem and
 * CrossbowItem funnel through to actually consume one unit of ammo from the shooter's
 * inventory; we record which hand slot it came from at HEAD (before the vanilla method has
 * mutated anything), then, at RETURN, check whether a real consumption happened (the method
 * returns ItemStack.EMPTY on failure, or a copy tagged INTANGIBLE_PROJECTILE when nothing was
 * actually taken from the inventory - e.g. infinite ammo) and trigger exactly a 1-unit Refill
 * targeting that recorded slot.
 */
@Mixin(ProjectileWeaponItem.class)
public abstract class ProjectileWeaponItemMixin {
	@Unique
	private static final ThreadLocal<Integer> shulker_enchantments$pendingSlot = new ThreadLocal<>();

	@Inject(method = "useAmmo", at = @At("HEAD"))
	private static void shulker_enchantments$captureSlot(
		final ItemStack weapon, final ItemStack projectile, final LivingEntity holder, final boolean forceInfinite,
		final CallbackInfoReturnable<ItemStack> cir
	) {
		shulker_enchantments$pendingSlot.remove();
		ShulkerEnchantmentsConfig config = ShulkerEnchantmentsConfig.get();
		if (!config.allowRefillingProjectiles) {
			return;
		}
		if (!(holder instanceof ServerPlayer player)) {
			return;
		}
		int slot = shulker_enchantments$locateHandSlot(player, projectile);
		if (slot >= 0) {
			shulker_enchantments$pendingSlot.set(slot);
		}
	}

	@Inject(method = "useAmmo", at = @At("RETURN"))
	private static void shulker_enchantments$onUseAmmo(
		final ItemStack weapon, final ItemStack projectile, final LivingEntity holder, final boolean forceInfinite,
		final CallbackInfoReturnable<ItemStack> cir
	) {
		Integer slot = shulker_enchantments$pendingSlot.get();
		shulker_enchantments$pendingSlot.remove();
		if (slot == null) {
			return;
		}
		ItemStack used = cir.getReturnValue();
		if (used == null || used.isEmpty() || used.has(DataComponents.INTANGIBLE_PROJECTILE)) {
			return;
		}
		if (!(holder instanceof ServerPlayer player)) {
			return;
		}
		RefillLogic.resolveRefill(player, slot, used, 1, ShulkerEnchantmentsConfig.get());
	}

	@Unique
	private static int shulker_enchantments$locateHandSlot(final ServerPlayer player, final ItemStack projectile) {
		Inventory inventory = player.getInventory();
		int selected = inventory.getSelectedSlot();
		if (inventory.getItem(selected) == projectile) {
			return selected;
		}
		if (inventory.getItem(Inventory.SLOT_OFFHAND) == projectile) {
			return Inventory.SLOT_OFFHAND;
		}
		return -1;
	}
}
