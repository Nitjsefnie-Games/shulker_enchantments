package dev.shulker.enchantments.logic;

import dev.shulker.enchantments.ModEnchantments;
import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import dev.shulker.enchantments.container.ContainerDiscovery;
import dev.shulker.enchantments.container.ContainerNode;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Refill's amount-resolution routine (spec 4.2, "Refill amount resolution"), shared by both
 * the tick-based trigger and the projectile-consumption trigger.
 */
public final class RefillLogic {
	private RefillLogic() {
	}

	/**
	 * Draws up to {@code amountNeeded} units matching {@code typeTemplate} (item + components,
	 * count ignored) from Refill-enchanted containers into {@code targetSlot} of
	 * {@code player}'s inventory (0-8 = hotbar, 40 = offhand). Returns the amount actually
	 * transferred (0 if nothing was available).
	 */
	public static int resolveRefill(
		final Player player, final int targetSlot, final ItemStack typeTemplate, final int amountNeeded, final ShulkerEnchantmentsConfig config
	) {
		if (amountNeeded <= 0 || typeTemplate.isEmpty()) {
			return 0;
		}

		Holder<Enchantment> refill = ModEnchantments.holder(player.level().registryAccess(), ModEnchantments.REFILL);
		List<ContainerNode> containers = ContainerDiscovery.discover(player, refill, config);
		Inventory inventory = player.getInventory();
		int maxStack = typeTemplate.getMaxStackSize();

		int stillNeeded = amountNeeded;
		int transferredTotal = 0;

		outer:
		for (ContainerNode node : containers) {
			for (int i = 0; i < node.size(); i++) {
				if (stillNeeded <= 0) {
					break outer;
				}
				ItemStack src = node.getSlot(i);
				if (src.isEmpty() || !ItemStack.isSameItemSameComponents(src, typeTemplate)) {
					continue;
				}

				ItemStack targetStack = inventory.getItem(targetSlot);
				int currentTargetCount = targetStack.isEmpty() ? 0 : targetStack.getCount();
				int room = maxStack - currentTargetCount;
				int transfer = Math.min(Math.min(stillNeeded, room), src.getCount());
				if (transfer <= 0) {
					continue;
				}

				if (targetStack.isEmpty()) {
					inventory.setItem(targetSlot, src.copyWithCount(transfer));
				} else {
					targetStack.grow(transfer);
				}

				src.shrink(transfer);
				node.markDirty();
				stillNeeded -= transfer;
				transferredTotal += transfer;
			}
		}

		// Flush deepest-first (reverse of the pre-order discovery list): a nested source
		// container writes its component onto its host stack, which is an element of its
		// parent's working list, and fromItems snapshots by value - so parents must be
		// re-encoded only after their children flush, else a draw from a nested container
		// fails to persist (item enters the hand but is never removed from the nested box).
		for (int i = containers.size() - 1; i >= 0; i--) {
			containers.get(i).flush();
		}

		return transferredTotal;
	}
}
