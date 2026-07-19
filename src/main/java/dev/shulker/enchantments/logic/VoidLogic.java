package dev.shulker.enchantments.logic;

import dev.shulker.enchantments.ModEnchantments;
import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import dev.shulker.enchantments.container.ContainerDiscovery;
import dev.shulker.enchantments.container.ContainerNode;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Void (spec 4.4): all-or-nothing. If any Void-enchanted container has even a single
 * matching item anywhere in it, the entire remaining incoming stack is deleted; containers
 * are never modified.
 */
public final class VoidLogic {
	private VoidLogic() {
	}

	public static void runVoid(final Player player, final ItemStack incoming, final ShulkerEnchantmentsConfig config) {
		if (incoming.isEmpty()) {
			return;
		}
		if (player.isCreative() && !config.creativeVoid) {
			return;
		}
		Holder<Enchantment> voidEnchant = ModEnchantments.holder(player.level().registryAccess(), ModEnchantments.VOID);
		List<ContainerNode> containers = ContainerDiscovery.discover(player, voidEnchant, config);
		for (ContainerNode node : containers) {
			for (int i = 0; i < node.size(); i++) {
				ItemStack slot = node.getSlot(i);
				if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, incoming)) {
					incoming.setCount(0);
					return;
				}
			}
		}
	}
}
