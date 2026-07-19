package dev.shulker.enchantments.tick;

import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import dev.shulker.enchantments.logic.RefillLogic;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * The once-per-tick held-item-depletion trigger that drives Refill (spec 2.2/4.2). Registered
 * once from the mod's entrypoint.
 */
public final class RefillTickHandler {
	private static final Map<UUID, State> PREVIOUS = new HashMap<>();

	private RefillTickHandler() {
	}

	public static void register() {
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			ShulkerEnchantmentsConfig config = ShulkerEnchantmentsConfig.get();
			for (ServerPlayer player : server.getPlayerList().getPlayers()) {
				tickPlayer(player, config);
			}
		});
	}

	private static void tickPlayer(final ServerPlayer player, final ShulkerEnchantmentsConfig config) {
		Inventory inventory = player.getInventory();
		int selectedNow = inventory.getSelectedSlot();
		ItemStack mainNow = inventory.getSelectedItem();
		ItemStack offNow = inventory.getItem(Inventory.SLOT_OFFHAND);

		State previous = PREVIOUS.get(player.getUUID());
		if (previous == null) {
			PREVIOUS.put(player.getUUID(), new State(selectedNow, mainNow.copy(), offNow.copy()));
			return;
		}

		boolean creativeBlocked = player.isCreative() && !config.creativeRefill;
		// NOTE: refill-while-inventory-open suppression is NOT enforced here - see
		// ShulkerEnchantmentsConfig#refillWhileInventoryOpenDefault javadoc for why.

		// A container-slot interaction (move/drag/shift-click, handled server-side by
		// ServerGamePacketListenerImpl#handleContainerClick) can drop the held stack's count
		// exactly like consumption does. ServerGamePacketListenerImplMixin records the tick of
		// any such interaction; if it happened on this tick, the count drop we're about to
		// observe is a move, not a consumption, so the tick-based trigger must not fire.
		boolean movedThisTick = ContainerInteractionTracker.interactedOnTick(
			player.getUUID(), player.level().getServer().getTickCount()
		);

		if (!creativeBlocked && !movedThisTick) {
			boolean swapped = ItemStack.matches(mainNow, previous.offhand) && ItemStack.matches(offNow, previous.mainhand);
			boolean selectedChanged = selectedNow != previous.selectedSlot;

			if (!swapped && !selectedChanged) {
				Shortfall mainShortfall = computeShortfall(previous.mainhand, mainNow, config, false);
				Shortfall offShortfall = computeShortfall(previous.offhand, offNow, config, true);

				if (mainShortfall.qualifies) {
					RefillLogic.resolveRefill(player, selectedNow, mainShortfall.template, mainShortfall.amount, config);
				} else if (offShortfall.qualifies) {
					RefillLogic.resolveRefill(player, Inventory.SLOT_OFFHAND, offShortfall.template, offShortfall.amount, config);
				}
			}
		}

		// The comparison baseline always advances, whether or not a refill happened this tick.
		previous.selectedSlot = selectedNow;
		previous.mainhand = mainNow.copy();
		previous.offhand = offNow.copy();
	}

	private static Shortfall computeShortfall(
		final ItemStack before, final ItemStack after, final ShulkerEnchantmentsConfig config, final boolean isOffhand
	) {
		if (before.isEmpty()) {
			return Shortfall.NONE;
		}

		int amount;
		if (after.isEmpty()) {
			amount = before.getCount();
		} else if (ItemStack.isSameItemSameComponents(before, after) && after.getCount() < before.getCount()) {
			amount = before.getCount() - after.getCount();
		} else {
			return Shortfall.NONE;
		}

		if (isOffhand && !config.allowRefillingOffhand) {
			return Shortfall.NONE;
		}

		if (!before.isStackable() && !config.allowRefillingNonStackable) {
			return Shortfall.NONE;
		}

		return new Shortfall(true, amount, before);
	}

	private static final class State {
		int selectedSlot;
		ItemStack mainhand;
		ItemStack offhand;

		State(final int selectedSlot, final ItemStack mainhand, final ItemStack offhand) {
			this.selectedSlot = selectedSlot;
			this.mainhand = mainhand;
			this.offhand = offhand;
		}
	}

	private static final class Shortfall {
		static final Shortfall NONE = new Shortfall(false, 0, ItemStack.EMPTY);

		final boolean qualifies;
		final int amount;
		final ItemStack template;

		Shortfall(final boolean qualifies, final int amount, final ItemStack template) {
			this.qualifies = qualifies;
			this.amount = amount;
			this.template = template;
		}
	}
}
