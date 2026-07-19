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
	 * Whether {@code playerId} performed a container-slot interaction during {@code tick}.
	 * Both callers must derive {@code tick} from the same counter
	 * ({@code ServerLevel#getServer()}{@code .getTickCount()}) for this comparison to be
	 * meaningful.
	 */
	public static boolean interactedOnTick(final UUID playerId, final int tick) {
		Integer lastTick = LAST_INTERACTION_TICK.get(playerId);
		return lastTick != null && lastTick == tick;
	}
}
