# Shulker Enchantments Reborn

A **Fabric** mod for **Minecraft 26.2** that adds four helpful enchantments for shulker boxes.
Server-side logic (works on a server even if clients don't have it installed).

📦 **[Available on Modrinth](https://modrinth.com/mod/shulker-enchantments-reborn)**

This is an independent, **clean-room reimplementation** of the *ideas* behind the four
pickup/refill enchantments popularized by the GPL-licensed *EnchantedShulkers* mod. No code
was copied or adapted from that project — the behavior was reimplemented from a written
specification against the Minecraft 26.2 API. This mod is licensed under the **MIT License**.

## Enchantments

All four are **enabled by default** and apply to any of the 17 shulker boxes. Max level 1 each.
They are *treasure* enchantments — obtain them from enchanted books (anvil), villager trades,
loot, or `/enchant`; they never appear from a plain enchanting-table roll.

| Enchantment | Effect |
|---|---|
| **Siphon** | Picked-up items flow into an enchanted container that already holds a matching stack (top-up only). |
| **Vacuum** | Picked-up items flow into an enchanted container wherever they fit (no pre-existing stack needed). |
| **Void**   | Picked-up items that match something already in an enchanted container are deleted. |
| **Refill** | When a held stack (either hand) is used up, it is replenished from an enchanted container. |

On pickup the order is fixed: **Siphon → Vacuum → Void**, each acting on whatever the previous
step left. A single shulker box may carry any combination of the four.

## Configuration

A config file is written to `config/shulker_enchantments.json` on first run. Notable knobs:

- `generateSiphon` / `generateRefill` / `generateVacuum` / `generateVoid` — survival obtainability (all `true`)
- `creativeSiphon` / `creativeRefill` / `creativeVacuum` / `creativeVoid` — allow for creative players (all `false`)
- `nestedContainerSearchDepth` — how deep to search containers-in-containers (default `255`)
- `strongerSiphon` — Siphon also fills empty slots (gated by an existing match)
- `weakerVacuum` — Vacuum requires an existing match
- `allowRefillingOffhand` (`true`), `allowRefillingNonStackable` (`false`), `allowRefillingProjectiles` (`true`)
- `enchantableEnderChest` (`false`)

## Building

Requires **JDK 25**.

```bash
./gradlew build   # jar lands in build/libs/
```

## Status / known limitations

The mod compiles cleanly and mixin targets validate against the 26.2 classpath, but it has
**not been runtime-tested inside a live Minecraft instance** yet. Please test in-game before
relying on it. Known simplifications versus a full implementation:

- The `generate*` config switches are applied statically via enchantment tags; toggling one at
  runtime does not dynamically gate trades/loot.
- "Refill while your own inventory is open" suppression is not enforced (it needs a client
  companion channel); refill currently always runs on the tick trigger.
- The Ender Chest can participate in container searches when `enchantableEnderChest` is on, but
  the enchantment cannot be *applied* to an ender chest through the anvil (only the 17 shulker
  boxes are in the supported-items tag).

## License

MIT — see [LICENSE](LICENSE).
