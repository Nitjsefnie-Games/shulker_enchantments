package dev.nitjsefnie.shulkerenchantments.container;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * A shulker-box-category container. Its slots are decoded once (from the host stack's CONTAINER
 * data component, or all-empty if that component was never written) into a working list; if this
 * node ends up dirty, {@link #flush()} re-encodes that working list back onto the host stack.
 *
 * <p>The slot count is the host item's real container capacity, not a fixed 27: reinforced and
 * other modded shulker boxes hold more (up to 108). It is derived once per item type from the
 * backing block's freshly-built block entity ({@link Container#getContainerSize()}) and cached,
 * falling back to the vanilla 27 when the item is not a block-backed container. Sizing the working
 * list to the true capacity is what keeps a larger box's slots past 27 from being dropped on flush.
 *
 * <p>{@code hostStack} must be the actual mutable ItemStack object that "is" this container -
 * either a stack living directly in the player's inventory (already live/persisted by
 * reference), or an element of a parent container's own working list (in which case writing
 * this node's CONTAINER component mutates that shared object in place, which the parent's own
 * eventual {@code fromItems(...)} call will pick up automatically).</p>
 */
public final class ShulkerBoxContainerNode extends ContainerNode {
	/** Vanilla shulker-box capacity; also the fallback when a box's real size can't be read. */
	private static final int DEFAULT_CAPACITY = 27;
	/** Hard ceiling matching the CONTAINER component's own maximum slot count. */
	private static final int MAX_CAPACITY = 256;
	/** Per-item capacity cache - the block entity is only ever built once per item type. */
	private static final Map<Item, Integer> CAPACITY_BY_ITEM = new ConcurrentHashMap<>();

	private final ItemStack hostStack;
	private final NonNullList<ItemStack> working;

	public ShulkerBoxContainerNode(final ContainerNode parent, final ItemStack hostStack) {
		super(parent);
		this.hostStack = hostStack;
		this.working = NonNullList.withSize(capacityOf(hostStack.getItem()), ItemStack.EMPTY);
		hostStack.getOrDefault(DataComponents.CONTAINER, ItemContainerContents.EMPTY).copyInto(working);
	}

	@Override
	public int size() {
		return working.size();
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

	/**
	 * Real container capacity for {@code item}: for a block-backed container item, the slot count
	 * of a freshly built block entity; otherwise the vanilla default. Cached per item type.
	 */
	private static int capacityOf(final Item item) {
		return CAPACITY_BY_ITEM.computeIfAbsent(item, key -> {
			if (key instanceof BlockItem blockItem && blockItem.getBlock() instanceof EntityBlock entityBlock) {
				try {
					final BlockEntity be = entityBlock.newBlockEntity(BlockPos.ZERO, blockItem.getBlock().defaultBlockState());
					if (be instanceof Container container) {
						final int slots = container.getContainerSize();
						if (slots > 0) {
							return Math.min(MAX_CAPACITY, slots);
						}
					}
				} catch (final Exception ignored) {
					// Unexpected block-entity construction failure - fall back to the vanilla default.
				}
			}
			return DEFAULT_CAPACITY;
		});
	}
}
