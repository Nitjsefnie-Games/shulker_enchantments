package dev.shulker.enchantments;

import dev.shulker.enchantments.config.ShulkerEnchantmentsConfig;
import dev.shulker.enchantments.tick.RefillTickHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerEnchantments implements ModInitializer {
	public static final String MOD_ID = "shulker_enchantments";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ShulkerEnchantmentsConfig.get();
		RefillTickHandler.register();
		LootInjection.register();
		LOGGER.info("Shulker Enchantments initialized");
	}
}
