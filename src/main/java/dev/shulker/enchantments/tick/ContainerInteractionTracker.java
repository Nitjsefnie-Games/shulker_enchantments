package dev.shulker.enchantments.tick;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Tracks, per player, the most recent server tick during which the player performed a
 * container-slot interaction (a {@code ServerboundContainerClickPacket}, e.g. moving,
 * dragging, or shift-clicking an item between slots). Written by
 * {@code ServerGamePacketListenerImplMixin}; read by {@link RefillTickHandler} so the
 * tick-based Refill trigger can be suppressed on any tick where the held-item count drop was
 * caused by a slot move rather than genuine consumption (spec 2.2/4.2 - see bug report
 * "Refill triggers when the held item is MOVED, not only when consumed").
 */
public final class ContainerInteractionTracker {
	private static final Map<UUID, Integer> LAST_INTERACTION_TICK = new HashMap<>();

	private ContainerInteractionTracker() {
	}

	/**
	 * Called from {@code ServerGamePacketListenerImplMixin} at the HEAD of
	 * {@code handleContainerClick}, before vanilla has mutated any slots.
	 */
	public static void recordInteraction(final UUID playerId, final int tick) {
		LAST_INTERACTION_TICK.put(playerId, tick);
	}

	/**
	 * Whether {@code playerId} performed a container-slot interaction on {@code currentTick} or
	 * within the preceding {@code tolerance} ticks. A container-click packet is processed (and
	 * its tick recorded here) on tick N, but the inventory count drop it causes is only observed
	 * by {@link RefillTickHandler}'s end-of-tick comparison on tick N+1 - so a tolerance of at
	 * least 1 is required to catch a move; an exact-tick match always misses by one.
	 * Both callers must derive the tick from the same counter
	 * ({@code ServerLevel#getServer()}{@code .getTickCount()}).
	 */
	public static boolean interactedWithinTicks(final UUID playerId, final int currentTick, final int tolerance) {
		Integer lastTick = LAST_INTERACTION_TICK.get(playerId);
		return lastTick != null && lastTick <= currentTick && lastTick >= currentTick - tolerance;
	}
}
