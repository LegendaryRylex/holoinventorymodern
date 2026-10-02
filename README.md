# Holo Inventory Modern

Look at a container and its contents float above it as a hologram, without opening it.

A NeoForge mod for Minecraft 26.1.2. Inspired by HoloInventory.

## What it shows

- Storage blocks and machines, vanilla or modded. Anything that holds items is read with no per-mod setup, and identical stacks are merged into one total.
- Entities that carry an inventory, such as storage minecarts, boats and pack animals.
- Your own ender chest, never another player's.

Empty containers show nothing. Unopened loot containers stay hidden unless the server turns them on, so looking at one never generates its loot by accident.

## Controls

A peek key and a toggle key, both rebindable. The hologram can show always, only while sneaking, or only while the peek key is held.

## Configuration

Everything is in the in-game config screen.

- Client: how the hologram looks, when it appears, and where it sits for blocks larger than one block.
- Server: how far away and how much is reported, a blacklist for blocks that should never be read, and a whitelist for single-slot blocks, which are hidden by default.

Any player can move the hologram with `/holoinventorymodern offset`: `center`, `top`, `above`, or `custom` with a left/right, up/down and forward/back distance in blocks. `/holoinventorymodern size` makes it up to three times larger.

Both lists can also be filled from a datapack with block tags, or in game by operators with `/holoinventorymodern`.

The mod is needed on both the client and the server.
