package dev.shulker.enchantments;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Resource keys for the four data-driven enchantments this mod defines, plus a small
 * helper to resolve their {@link Holder} from a live {@link RegistryAccess} (server
 * registries are only available once the server/world has started).
 */
public final class ModEnchantments {
	public static final ResourceKey<Enchantment> SIPHON = key("siphon");
	public static final ResourceKey<Enchantment> REFILL = key("refill");
	public static final ResourceKey<Enchantment> VACUUM = key("vacuum");
	public static final ResourceKey<Enchantment> VOID = key("void");

	private ModEnchantments() {
	}

	private static ResourceKey<Enchantment> key(final String path) {
		return ResourceKey.create(Registries.ENCHANTMENT, Identifier.fromNamespaceAndPath(ShulkerEnchantments.MOD_ID, path));
	}

	public static Holder<Enchantment> holder(final RegistryAccess registryAccess, final ResourceKey<Enchantment> key) {
		HolderLookup.RegistryLookup<Enchantment> lookup = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		return lookup.get(key).orElseThrow(() -> new IllegalStateException("Unknown enchantment " + key.identifier()));
	}
}
