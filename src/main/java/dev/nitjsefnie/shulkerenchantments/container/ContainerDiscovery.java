package dev.nitjsefnie.shulkerenchantments.container;

import dev.nitjsefnie.shulkerenchantments.ShulkerEnchantments;
import dev.nitjsefnie.shulkerenchantments.config.ShulkerEnchantmentsConfig;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.core.registries.Registries;

import java.util.ArrayList;
import java.util.List;

/**
 * Container discovery search shared by Siphon, Refill, Vacuum and Void (spec section 1.2/1.3).
 */
public final class ContainerDiscovery {
	public static final TagKey<net.minecraft.world.item.Item> ENCHANTABLE_CONTAINER = TagKey.create(
		Registries.ITEM, Identifier.fromNamespaceAndPath(ShulkerEnchantments.MOD_ID, "enchantable_container")
	);

	private static final int[] ARMOR_SLOTS = {36, 37, 38, 39};
	private static final int OFFHAND_SLOT = 40;

	private ContainerDiscovery() {
	}

	/**
	 * Runs the full depth-first pre-order search over {@code player}'s inventory for
	 * containers carrying {@code targetEnchantment} at level >= 1.
	 */
	public static List<ContainerNode> discover(final Player player, final Holder<Enchantment> targetEnchantment, final ShulkerEnchantmentsConfig config) {
		List<ContainerNode> result = new ArrayList<>();
		for (ItemStack stack : flatInventory(player)) {
			visit(stack, null, 0, false, targetEnchantment, config, player, result);
		}
		return result;
	}

	private static List<ItemStack> flatInventory(final Player player) {
		Inventory inventory = player.getInventory();
		List<ItemStack> flat = new ArrayList<>(41);
		for (int i = 0; i < 36; i++) {
			flat.add(inventory.getItem(i));
		}
		for (int armorSlot : ARMOR_SLOTS) {
			flat.add(inventory.getItem(armorSlot));
		}
		flat.add(inventory.getItem(OFFHAND_SLOT));
		return flat;
	}

	private static void visit(
		final ItemStack stack,
		final ContainerNode parent,
		final int depth,
		final boolean alreadyPassedEnderChest,
		final Holder<Enchantment> targetEnchantment,
		final ShulkerEnchantmentsConfig config,
		final Player player,
		final List<ContainerNode> out
	) {
		if (stack == null || stack.isEmpty()) {
			return;
		}

		boolean isEnderChestItem = config.enchantableEnderChest && stack.is(Items.ENDER_CHEST);
		boolean isShulkerBoxItem = stack.is(ENCHANTABLE_CONTAINER);
		if (!isEnderChestItem && !isShulkerBoxItem) {
			return;
		}

		if (EnchantmentHelper.getItemEnchantmentLevel(targetEnchantment, stack) < 1) {
			return;
		}

		if (!isEnderChestItem && stack.getCount() != 1) {
			return;
		}

		if (isEnderChestItem && alreadyPassedEnderChest) {
			return;
		}

		ContainerNode node = isEnderChestItem
			? new EnderChestContainerNode(parent, player.getEnderChestInventory())
			: new ShulkerBoxContainerNode(parent, stack);
		out.add(node);

		if (depth < config.nestedContainerSearchDepth) {
			boolean nextPassedEnderChest = alreadyPassedEnderChest || isEnderChestItem;
			for (int i = 0; i < node.size(); i++) {
				visit(node.getSlot(i), node, depth + 1, nextPassedEnderChest, targetEnchantment, config, player, out);
			}
		}
	}
}
