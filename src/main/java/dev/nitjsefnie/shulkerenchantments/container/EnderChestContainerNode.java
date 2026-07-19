package dev.nitjsefnie.shulkerenchantments.container;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

/**
 * An ender-chest-category container. Backed directly by the player's live, shared Ender
 * Chest storage (spec 1.4) - reads/writes apply immediately, there is nothing to flush.
 */
public final class EnderChestContainerNode extends ContainerNode {
	private final Container live;

	public EnderChestContainerNode(final ContainerNode parent, final Container live) {
		super(parent);
		this.live = live;
	}

	@Override
	public int size() {
		return live.getContainerSize();
	}

	@Override
	public ItemStack getSlot(final int index) {
		return live.getItem(index);
	}

	@Override
	protected void setSlotRaw(final int index, final ItemStack stack) {
		live.setItem(index, stack);
		live.setChanged();
	}

	@Override
	public void flush() {
		// Live-backed; nothing to persist.
	}
}
