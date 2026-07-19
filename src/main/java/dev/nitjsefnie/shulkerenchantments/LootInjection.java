package dev.nitjsefnie.shulkerenchantments;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

/**
 * Adds the four container enchantments (as a treasure enchanted book) to a small, thematic set
 * of vanilla loot tables - fishing treasure and end city treasure (shulkers originate in end
 * cities). This uses the Fabric loot API's MODIFY event, which ADDS a pool to the existing
 * table at load time rather than replacing the datapack file, so it composes with any other
 * mod/datapack that also touches these tables instead of clobbering them.
 *
 * <p>The books are also obtainable from librarian villager trades via the vanilla
 * {@code #minecraft:tradeable} enchantment tag; that path needs no code here.
 */
public final class LootInjection {
	/** Enchantments eligible to roll onto a book in the loot tables below. */
	public static final TagKey<Enchantment> LOOT_TAG = TagKey.create(
		Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(ShulkerEnchantments.MOD_ID, "loot"));

	private static final float FISHING_TREASURE_CHANCE = 0.05f;
	private static final float END_CITY_TREASURE_CHANCE = 0.15f;

	private LootInjection() {
	}

	public static void register() {
		LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
			final float chance;
			if (key.equals(BuiltInLootTables.FISHING_TREASURE)) {
				chance = FISHING_TREASURE_CHANCE;
			} else if (key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
				chance = END_CITY_TREASURE_CHANCE;
			} else {
				return;
			}

			HolderSet<Enchantment> enchantments = registries.lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(LOOT_TAG);
			tableBuilder.pool(
				LootPool.lootPool()
					.setRolls(ConstantValue.exactly(1.0F))
					.when(LootItemRandomChanceCondition.randomChance(chance))
					.add(LootItem.lootTableItem(Items.BOOK)
						.apply(EnchantRandomlyFunction.randomEnchantment().withOneOf(enchantments)))
					.build()
			);
		});
	}
}
