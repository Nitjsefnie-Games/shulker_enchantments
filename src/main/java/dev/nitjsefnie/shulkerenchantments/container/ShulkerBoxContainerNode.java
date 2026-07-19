package dev.nitjsefnie.shulkerenchantments.container;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;

/**
 * A shulker-box-category container. Its 27 slots are decoded once (from the host stack's
 * CONTAINER data component, or all-empty if that component was never written) into a working
 * list; if this node ends up dirty, {@link #flush()} re-encodes that working list back onto
 * the host stack.
 *
 * <p>{@code hostStack} must be the actual mutable ItemStack object that "is" this container -
 * either a stack living directly in the player's inventory (already live/persisted by
 * reference), or an element of a parent container's own working list (in which case writing
 * this node's CONTAINER component mutates that shared object in place, which the parent's own
 * eventual {@code fromItems(...)} call will pick up automatically).</p>
 */
public final class ShulkerBoxContainerNode extends ContainerNode {
	private static final int CAPACITY = 27;

	private final ItemStack hostStack;
	private final NonNullList<ItemStack> working;

	public ShulkerBoxContainerNode(final ContainerNode parent, final ItemStack hostStack) {
		super(parent);
		this.hostStack = hostStack;
		this.working = NonNullList.withSize(CAPACITY, ItemStack.EMPTY);
		hostStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(working);
	}

	@Override
	public int size() {
		return CAPACITY;
	}

	@Override
	public ItemStack getSlot(final int index) {
		return working.get(index);
	}

	@Override
	protected void setSlotRaw(final int index, final ItemStack stack) {
		working.set(index, stack);
	}

	@Override
	public void flush() {
		if (isDirty()) {
			hostStack.set(DataComponents.CONTAINER, ItemContainerContents.fromItems(working));
		}
	}
}
