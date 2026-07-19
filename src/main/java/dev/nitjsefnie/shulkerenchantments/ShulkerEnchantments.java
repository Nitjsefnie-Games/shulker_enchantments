package dev.nitjsefnie.shulkerenchantments;

import dev.nitjsefnie.shulkerenchantments.config.ShulkerEnchantmentsConfig;
import dev.nitjsefnie.shulkerenchantments.tick.RefillTickHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShulkerEnchantments implements ModInitializer {
	public static final String MOD_ID = "shulker_enchantments_reborn";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ShulkerEnchantmentsConfig.get();
		RefillTickHandler.register();
		LootInjection.register();
		LOGGER.info("Shulker Enchantments initialized");
	}
}
