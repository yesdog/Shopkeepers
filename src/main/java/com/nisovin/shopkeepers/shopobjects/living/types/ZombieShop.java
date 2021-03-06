package com.nisovin.shopkeepers.shopobjects.living.types;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingShops;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObject;
import com.nisovin.shopkeepers.shopobjects.living.SKLivingShopObjectType;

public class ZombieShop extends SKLivingShopObject {

	private boolean baby = false;

	public ZombieShop(	LivingShops livingShops, SKLivingShopObjectType<ZombieShop> livingObjectType,
						AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingShops, livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		baby = configSection.getBoolean("baby");
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		configSection.set("baby", baby);
	}

	@Override
	public Zombie getEntity() {
		assert super.getEntity().getType() == EntityType.ZOMBIE;
		return (Zombie) super.getEntity();
	}

	// SUB TYPES

	@Override
	protected void applySubType() {
		super.applySubType();
		if (!this.isActive()) return;
		this.getEntity().setBaby(baby);
	}

	@Override
	public ItemStack getSubTypeItem() {
		return new ItemStack(Material.ZOMBIE_SPAWN_EGG, 1);
	}

	@Override
	public void cycleSubType() {
		shopkeeper.markDirty();
		baby = !baby;
		this.applySubType();
	}
}
