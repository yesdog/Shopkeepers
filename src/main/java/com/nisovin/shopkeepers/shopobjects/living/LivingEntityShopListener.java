package com.nisovin.shopkeepers.shopobjects.living;

import java.util.Collection;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.entity.CreeperPowerEvent;
import org.bukkit.event.entity.CreeperPowerEvent.PowerCause;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent.Action;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTeleportEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.PigZapEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.entity.SheepDyeWoolEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedEnterEvent.BedEnterResult;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.projectiles.ProjectileSource;

import com.nisovin.shopkeepers.Settings;
import com.nisovin.shopkeepers.api.shopobjects.DefaultShopObjectTypes;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopkeeper.SKShopkeeperRegistry;
import com.nisovin.shopkeepers.util.Log;
import com.nisovin.shopkeepers.util.Utils;

class LivingEntityShopListener implements Listener {

	// the radius around lightning strikes in which villagers turn into witches
	private static final int VILLAGER_ZAP_RADIUS = 7; // minecraft wiki says 3-4, we use 7 to be safe

	private final SKShopkeeperRegistry shopkeeperRegistry;

	LivingEntityShopListener(SKShopkeeperRegistry shopkeeperRegistry) {
		this.shopkeeperRegistry = shopkeeperRegistry;
	}

	@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
	void onEntityInteract(PlayerInteractEntityEvent event) {
		if (!(event.getRightClicked() instanceof LivingEntity)) return;
		LivingEntity shopEntity = (LivingEntity) event.getRightClicked();
		Player player = event.getPlayer();
		String playerName = player.getName();
		Log.debug("Player " + playerName + " is interacting with entity at " + shopEntity.getLocation());

		// also checks for citizens npc shopkeepers:
		AbstractShopkeeper shopkeeper = shopkeeperRegistry.getShopkeeperByEntity(shopEntity);
		if (shopkeeper == null) {
			Log.debug("  Non-shopkeeper");
			return;
		}

		if (event.isCancelled() && !Settings.bypassShopInteractionBlocking) {
			Log.debug("  Cancelled by another plugin");
			return;
		}

		// only trigger shopkeeper interaction for main-hand events:
		if (event.getHand() == EquipmentSlot.HAND) {
			shopkeeper.onPlayerInteraction(player);
		}

		// if citizens npc: don't cancel the event, let Citizens perform other actions as appropriate
		if (shopkeeper.getShopObject().getType() != DefaultShopObjectTypes.CITIZEN()) {
			// always cancel interactions with shopkeepers, to prevent any default behavior:
			event.setCancelled(true);

			// update inventory in case interaction would trigger feeding normally:
			player.updateInventory();
		}
	}

	// TODO many of those behaviors might no longer be active, once all entities use noAI (once legacy mob behavior is
	// no longer supported)

	@EventHandler(ignoreCancelled = true)
	void onEntityTarget(EntityTargetEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity()) || shopkeeperRegistry.isShopkeeper(event.getTarget())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onEntityDamage(EntityDamageEvent event) {
		Entity entity = event.getEntity();
		if (!shopkeeperRegistry.isShopkeeper(entity)) return;

		// block damaging of shopkeepers
		event.setCancelled(true);
		if (event instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent evt = (EntityDamageByEntityEvent) event;
			if (evt.getDamager() instanceof Monster) {
				Monster monster = (Monster) evt.getDamager();
				// reset target, future targeting should get prevented somewhere else:
				if (entity.equals(monster.getTarget())) {
					monster.setTarget(null);
				}
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onEntityEnterVehicle(VehicleEnterEvent event) {
		Entity entity = event.getEntered();
		if (shopkeeperRegistry.isShopkeeper(entity)) {
			event.setCancelled(true);
		}
	}

	// ex: creepers

	@EventHandler(ignoreCancelled = true)
	void onExplodePrime(ExplosionPrimeEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onExplode(EntityExplodeEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
			Log.debug("Cancelled event for living shop: " + event.getEventName());
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onCreeperCharged(CreeperPowerEvent event) {
		if (event.getCause() == PowerCause.LIGHTNING && shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	// ex: enderman

	@EventHandler(ignoreCancelled = true)
	void onEntityChangeBlock(EntityChangeBlockEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onEntityTeleport(EntityTeleportEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onEntityPortalTeleport(EntityPortalEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onPigZap(PigZapEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onSheepDyed(SheepDyeWoolEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onLightningStrike(LightningStrikeEvent event) {
		// workaround: preventing lightning strikes near villager shopkeepers
		// because they would turn into witches
		Location loc = event.getLightning().getLocation();
		for (Entity entity : Utils.getNearbyEntities(loc, VILLAGER_ZAP_RADIUS, EntityType.VILLAGER)) {
			if (shopkeeperRegistry.isShopkeeper(entity)) {
				event.setCancelled(true);
			}
		}
	}

	@EventHandler(ignoreCancelled = true)
	void onPotionSplash(PotionSplashEvent event) {
		for (LivingEntity entity : event.getAffectedEntities()) {
			if (shopkeeperRegistry.isShopkeeper(entity)) {
				event.setIntensity(entity, 0.0D);
			}
		}
	}

	// allow sleeping if the only nearby monsters are shopkeepers:
	// note: cancellation state also reflects default behavior
	@EventHandler(ignoreCancelled = false)
	void onPlayerEnterBed(PlayerBedEnterEvent event) {
		// bed entering prevented due to nearby monsters?
		if (event.getBedEnterResult() != BedEnterResult.NOT_SAFE) return;

		// find nearby monsters that prevent bed entering (see MC EntityHuman):
		Block bedBlock = event.getBed();
		Collection<Entity> monsters = bedBlock.getWorld().getNearbyEntities(bedBlock.getLocation(), 8.0D, 5.0D, 8.0D, (entity) -> {
			// TODO bukkit API to check if monster prevents sleeping? ie. pigzombies only prevent sleeping if angered
			return (entity instanceof Monster) && (!(entity instanceof PigZombie) || ((PigZombie) entity).isAngry());
		});

		for (Entity entity : monsters) {
			if (!shopkeeperRegistry.isShopkeeper(entity)) {
				// found non-shopkeeper entity: do nothing (keep bed entering prevented)
				return;
			}
		}
		// sleeping is only prevented due to nearby shopkeepers -> bypass and allow sleeping:
		Log.debug("Allowing sleeping of player '" + event.getPlayer().getName() + "': The only nearby monsters are shopkeepers.");
		event.setUseBed(Result.ALLOW);
	}

	// ex: blazes or skeletons

	@EventHandler(ignoreCancelled = true)
	void onEntityLaunchProjectile(ProjectileLaunchEvent event) {
		ProjectileSource source = event.getEntity().getShooter();
		if (source instanceof LivingEntity && shopkeeperRegistry.isShopkeeper((LivingEntity) source)) {
			event.setCancelled(true);
		}
	}

	// ex: snowmans

	@EventHandler(ignoreCancelled = true)
	void onEntityBlockForm(EntityBlockFormEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	// ex: chicken laying eggs

	@EventHandler(ignoreCancelled = true)
	void onEntityDropItem(EntityDropItemEvent event) {
		if (shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}

	// prevent shopkeeper entities from being affected by potion effects
	@EventHandler(ignoreCancelled = true)
	void onEntityPotionEffectEvent(EntityPotionEffectEvent event) {
		if (event.getAction() == Action.ADDED && shopkeeperRegistry.isShopkeeper(event.getEntity())) {
			event.setCancelled(true);
		}
	}
}
