package com.nisovin.shopkeepers.shopkeeper;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldSaveEvent;
import org.bukkit.event.world.WorldUnloadEvent;

import com.nisovin.shopkeepers.SKShopkeepersPlugin;
import com.nisovin.shopkeepers.api.shopkeeper.Shopkeeper;
import com.nisovin.shopkeepers.api.shopobjects.living.LivingShopObject;
import com.nisovin.shopkeepers.util.Log;

class WorldListener implements Listener {

	private final SKShopkeepersPlugin plugin;
	private final SKShopkeeperRegistry shopkeeperRegistry;

	WorldListener(SKShopkeepersPlugin plugin, SKShopkeeperRegistry shopkeeperRegistry) {
		this.plugin = plugin;
		this.shopkeeperRegistry = shopkeeperRegistry;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	void onChunkLoad(ChunkLoadEvent event) {
		Chunk chunk = event.getChunk();
		Bukkit.getScheduler().runTaskLater(plugin, () -> {
			if (chunk.isLoaded()) {
				shopkeeperRegistry.loadShopkeepersInChunk(chunk);
			}
		}, 2);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	void onChunkUnload(ChunkUnloadEvent event) {
		// living shopkeeper entities might get pushed into different chunks, so during chunk unloads, we check for and
		// remove all living shopkeeper entities that got pushed from their original chunk into this chunk:
		Chunk chunk = event.getChunk();
		for (Entity entity : chunk.getEntities()) {
			Shopkeeper shopkeeper = shopkeeperRegistry.getShopkeeperByEntity(entity);
			if (shopkeeper != null && (shopkeeper.getShopObject() instanceof LivingShopObject) && !shopkeeper.getChunkCoords().isSameChunk(chunk)) {
				Log.debug("Removing shop entity which was pushed away from shop's chunk at (" + shopkeeper.getPositionString() + ")");
				entity.remove();
			}
		}

		shopkeeperRegistry.unloadShopkeepersInChunk(chunk);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	void onWorldSave(WorldSaveEvent event) {
		World world = event.getWorld();
		UUID worldUID = world.getUID();
		Log.debug("World '" + world.getName() + "' is about to get saved: Unloading all shopkeepers in that world.");
		shopkeeperRegistry.unloadShopkeepersInWorld(world);
		Bukkit.getScheduler().runTask(plugin, () -> {
			// check if the world is still loaded:
			World loadedWorld = Bukkit.getWorld(worldUID);
			if (loadedWorld != null) {
				Log.debug("World '" + loadedWorld.getName() + "' was saved. Reloading all shopkeepers in that world.");
				shopkeeperRegistry.loadShopkeepersInWorld(loadedWorld);
			}
		});
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onWorldLoad(WorldLoadEvent event) {
		shopkeeperRegistry.loadShopkeepersInWorld(event.getWorld());
	}

	@EventHandler(priority = EventPriority.HIGH)
	void onWorldUnload(WorldUnloadEvent event) {
		shopkeeperRegistry.unloadShopkeepersInWorld(event.getWorld());
	}
}
