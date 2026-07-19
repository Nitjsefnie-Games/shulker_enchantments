package dev.shulker.enchantments.logic;

import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Item-pickup pre-filter (spec 2.1): Siphon, then Vacuum, then Void, each acting on whatever
 * remains of the incoming stack after the previous step. Called from the pickup mixin before
 * vanilla's normal "find room in my inventory" placement runs.
 */
public final class PickupHandler {
	private PickupHandler() {
	}

	/**
	 * Runs the Siphon -> Vacuum -> Void chain on {@code incoming}, mutating its count down.
	 * Returns true if the stack was fully consumed (caller should skip normal pickup entirely).
	 */
	public static boolean handlePickup(final Player player, final ItemStack incoming) {
		if (incoming.isEmpty()) {
			return false;
		}
		ShulkerEnchantmentsConfig config = ShulkerEnchantmentsConfig.get();

		SiphonVacuumLogic.runSiphon(player, incoming, config);
		if (incoming.isEmpty()) {
			return true;
		}

		SiphonVacuumLogic.runVacuum(player, incoming, config);
		if (incoming.isEmpty()) {
			return true;
		}

		VoidLogic.runVoid(player, incoming, config);
		return incoming.isEmpty();
	}
}
