package dev.nitjsefnie.shulkerenchantments.logic;

import dev.nitjsefnie.shulkerenchantments.ModEnchantments;
import dev.nitjsefnie.shulkerenchantments.config.ShulkerEnchantmentsConfig;
import dev.nitjsefnie.shulkerenchantments.container.ContainerDiscovery;
import dev.nitjsefnie.shulkerenchantments.container.ContainerNode;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.List;

/**
 * Siphon (spec 4.1) and Vacuum (spec 4.3) pickup-chain steps. Both reuse the same pair of
 * fill algorithms (see {@link FillAlgorithms}); only which switch selects which algorithm,
 * and which enchantment is searched for, differ.
 */
public final class SiphonVacuumLogic {
	private SiphonVacuumLogic() {
	}

	/** Runs Siphon on {@code incoming}, mutating its count down as items are absorbed. */
	public static void runSiphon(final Player player, final ItemStack incoming, final ShulkerEnchantmentsConfig config) {
		if (incoming.isEmpty()) {
			return;
		}
		if (player.isCreative() && !config.creativeSiphon) {
			return;
		}
		Holder<Enchantment> siphon = ModEnchantments.holder(player.level().registryAccess(), ModEnchantments.SIPHON);
		List<ContainerNode> containers = ContainerDiscovery.discover(player, siphon, config);
		if (config.strongerSiphon) {
			FillAlgorithms.gatedTwoPhase(containers, incoming);
		} else {
			FillAlgorithms.topUpOnly(containers, incoming);
		}
		flushAll(containers);
	}

	/** Runs Vacuum on {@code incoming}, mutating its count down as items are absorbed. */
	public static void runVacuum(final Player player, final ItemStack incoming, final ShulkerEnchantmentsConfig config) {
		if (incoming.isEmpty()) {
			return;
		}
		if (player.isCreative() && !config.creativeVacuum) {
			return;
		}
		Holder<Enchantment> vacuum = ModEnchantments.holder(player.level().registryAccess(), ModEnchantments.VACUUM);
		List<ContainerNode> containers = ContainerDiscovery.discover(player, vacuum, config);
		if (config.weakerVacuum) {
			FillAlgorithms.gatedTwoPhase(containers, incoming);
		} else {
			FillAlgorithms.singlePassEmptyOrMatch(containers, incoming);
		}
		flushAll(containers);
	}

	private static void flushAll(final List<ContainerNode> containers) {
		// Flush deepest-first (reverse of the pre-order discovery list). A nested container's
		// contents are written onto its host stack (an element of its parent's working list),
		// and ItemContainerContents.fromItems snapshots stacks by value - so a parent must be
		// re-encoded only AFTER its children have written their updated components, or the
		// nested write is silently lost.
		for (int i = containers.size() - 1; i >= 0; i--) {
			containers.get(i).flush();
		}
	}
}
