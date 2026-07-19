package dev.shulker.enchantments.logic;

import dev.shulker.enchantments.container.ContainerNode;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * The two "insert items into containers" scan strategies shared by Siphon and Vacuum
 * (spec 4.1/4.3). A single incoming stack is drained into the discovered containers, in
 * discovery order, until either it is empty or every container/slot has been visited.
 */
public final class FillAlgorithms {
	private FillAlgorithms() {
	}

	/**
	 * Default Siphon: top-up only, single ordered pass. Empty slots are always skipped.
	 */
	public static void topUpOnly(final List<ContainerNode> containers, final ItemStack incoming) {
		for (ContainerNode node : containers) {
			if (incoming.isEmpty()) {
				return;
			}
			for (int i = 0; i < node.size(); i++) {
				if (incoming.isEmpty()) {
					break;
				}
				ItemStack slot = node.getSlot(i);
				if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, incoming)) {
					continue;
				}
				topUp(node, slot, incoming);
			}
		}
	}

	/**
	 * "Stronger Siphon" / "weaker Vacuum": a container is skipped entirely unless it already
	 * has at least one matching (non-empty) slot; otherwise, top up matches first, then dump
	 * the remainder (uncapped) into the first empty slot.
	 */
	public static void gatedTwoPhase(final List<ContainerNode> containers, final ItemStack incoming) {
		for (ContainerNode node : containers) {
			if (incoming.isEmpty()) {
				return;
			}
			if (!hasExistingMatch(node, incoming)) {
				continue;
			}

			for (int i = 0; i < node.size(); i++) {
				if (incoming.isEmpty()) {
					break;
				}
				ItemStack slot = node.getSlot(i);
				if (slot.isEmpty() || !ItemStack.isSameItemSameComponents(slot, incoming)) {
					continue;
				}
				topUp(node, slot, incoming);
			}

			if (!incoming.isEmpty()) {
				for (int i = 0; i < node.size(); i++) {
					if (incoming.isEmpty()) {
						break;
					}
					ItemStack slot = node.getSlot(i);
					if (slot.isEmpty()) {
						dumpIntoEmpty(node, i, incoming);
					}
				}
			}
		}
	}

	/**
	 * Default Vacuum: no pre-existing-match gate; a single ordered pass where, per slot,
	 * whichever applies first - "empty" (uncapped dump) or "matching partial" (capped top-up)
	 * - wins, in slot-index order.
	 */
	public static void singlePassEmptyOrMatch(final List<ContainerNode> containers, final ItemStack incoming) {
		for (ContainerNode node : containers) {
			if (incoming.isEmpty()) {
				return;
			}
			for (int i = 0; i < node.size(); i++) {
				if (incoming.isEmpty()) {
					break;
				}
				ItemStack slot = node.getSlot(i);
				if (slot.isEmpty()) {
					dumpIntoEmpty(node, i, incoming);
				} else if (ItemStack.isSameItemSameComponents(slot, incoming)) {
					topUp(node, slot, incoming);
				}
			}
		}
	}

	private static boolean hasExistingMatch(final ContainerNode node, final ItemStack incoming) {
		for (int i = 0; i < node.size(); i++) {
			ItemStack slot = node.getSlot(i);
			if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, incoming)) {
				return true;
			}
		}
		return false;
	}

	/** Tops up an existing partial (or full) matching stack, capped to available room. */
	private static void topUp(final ContainerNode node, final ItemStack slot, final ItemStack incoming) {
		int room = slot.getMaxStackSize() - slot.getCount();
		int move = Math.min(room, incoming.getCount());
		if (move <= 0) {
			return;
		}
		slot.grow(move);
		node.markDirty();
		incoming.shrink(move);
	}

	/** Dumps the entire remaining incoming amount into a previously empty slot, uncapped. */
	private static void dumpIntoEmpty(final ContainerNode node, final int index, final ItemStack incoming) {
		ItemStack newStack = incoming.copyWithCount(incoming.getCount());
		node.setSlot(index, newStack);
		incoming.setCount(0);
	}
}
