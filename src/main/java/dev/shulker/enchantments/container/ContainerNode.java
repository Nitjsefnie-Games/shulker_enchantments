package dev.shulker.enchantments.container;

import net.minecraft.world.item.ItemStack;

/**
 * One discovered eligible container (spec section 1.2). Wraps either a shulker-box-category
 * item stack's contents (immutable data-component backed, needs an explicit write-back) or
 * the player's live, shared Ender Chest storage (already "live", no write-back needed).
 *
 * <p>Dirty tracking: whenever any slot of this node (or of a descendant node reached via
 * recursion) is modified, {@link #markDirty()} is called, which flags this node AND
 * propagates up through {@link #parent}. After a whole enchantment-processing pass is done,
 * every discovered node is asked to {@link #flush()}; shulker-box-backed nodes only rewrite
 * their host stack's CONTAINER data component if they ended up dirty.</p>
 */
public abstract class ContainerNode {
	private final ContainerNode parent;
	private boolean dirty;

	protected ContainerNode(final ContainerNode parent) {
		this.parent = parent;
	}

	public abstract int size();

	public abstract ItemStack getSlot(int index);

	/**
	 * Replaces the stack at {@code index} outright (used when filling a previously empty
	 * slot). Marks this node (and its ancestors) dirty.
	 */
	public final void setSlot(final int index, final ItemStack stack) {
		setSlotRaw(index, stack);
		markDirty();
	}

	protected abstract void setSlotRaw(int index, ItemStack stack);

	/**
	 * Call this after mutating a slot's ItemStack in place (e.g. via grow()/shrink()) rather
	 * than replacing the slot outright - the backing list already reflects the change by
	 * object identity, but persistence still needs to know this subtree changed.
	 */
	public final void markDirty() {
		dirty = true;
		if (parent != null) {
			parent.markDirty();
		}
	}

	public final boolean isDirty() {
		return dirty;
	}

	/**
	 * Writes this node's working contents back to its host, if (and only if) it is dirty.
	 * Safe to call unconditionally on every discovered node after a processing pass.
	 */
	public abstract void flush();
}
