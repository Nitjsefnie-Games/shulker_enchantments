package dev.shulker.enchantments.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.shulker.enchantments.ShulkerEnchantments;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * World/server-level configuration knobs (spec section 6). Loaded once at mod init from
 * {@code <configDir>/shulker_enchantments.json}, tolerant of a missing or partial file
 * (missing fields keep their Java-side defaults; a totally missing file gets one written
 * out with defaults).
 */
public final class ShulkerEnchantmentsConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final String FILE_NAME = "shulker_enchantments.json";

	private static ShulkerEnchantmentsConfig instance;

	// --- Survival obtainability ("generate") switches. Reimplementation default: all four enabled. ---
	public boolean generateSiphon = true;
	public boolean generateRefill = true;
	public boolean generateVacuum = true;
	public boolean generateVoid = true;

	// --- Creative-mode gates (all default off, per spec) ---
	public boolean creativeSiphon = false;
	public boolean creativeRefill = false;
	public boolean creativeVacuum = false;
	public boolean creativeVoid = false;

	// --- Container eligibility / search ---
	public boolean enchantableEnderChest = false;
	public int nestedContainerSearchDepth = 255;

	// --- Algorithm switches ---
	public boolean strongerSiphon = true;
	public boolean weakerVacuum = false;

	// --- Refill-specific knobs ---
	public boolean allowRefillingOffhand = true;
	public boolean allowRefillingNonStackable = false;
	public boolean allowRefillingProjectiles = true;

	/**
	 * Reference-mod behavior: refill-while-inventory-open is a client-side, per-player
	 * opt-in preference relayed to the server. This reimplementation has no client-side
	 * mod code / network channel to carry that preference, so the tick-based trigger
	 * cannot actually detect whether a player's own inventory screen is open. This field
	 * is kept (and defaults to matching the spec's "suppression on" default) for parity
	 * of the config surface, but the suppression itself is NOT enforced at runtime - see
	 * the gap noted in the project report.
	 */
	public boolean refillWhileInventoryOpenDefault = false;

	public static ShulkerEnchantmentsConfig get() {
		if (instance == null) {
			instance = load();
		}
		return instance;
	}

	private static ShulkerEnchantmentsConfig load() {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		ShulkerEnchantmentsConfig config = new ShulkerEnchantmentsConfig();
		if (Files.exists(path)) {
			try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
				ShulkerEnchantmentsConfig loaded = GSON.fromJson(reader, ShulkerEnchantmentsConfig.class);
				if (loaded != null) {
					config = loaded;
				}
			} catch (IOException | com.google.gson.JsonSyntaxException e) {
				ShulkerEnchantments.LOGGER.warn("Failed to read {}, falling back to defaults", FILE_NAME, e);
				config = new ShulkerEnchantmentsConfig();
			}
		} else {
			save(config);
		}
		return config;
	}

	private static void save(final ShulkerEnchantmentsConfig config) {
		Path path = FabricLoader.getInstance().getConfigDir().resolve(FILE_NAME);
		try {
			Files.createDirectories(path.getParent());
			try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
				GSON.toJson(config, writer);
			}
		} catch (IOException e) {
			ShulkerEnchantments.LOGGER.warn("Failed to write default {}", FILE_NAME, e);
		}
	}
}
