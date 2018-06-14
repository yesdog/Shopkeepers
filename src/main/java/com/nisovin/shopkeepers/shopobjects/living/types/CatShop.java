package com.nisovin.shopkeepers.shopobjects.living.types;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.inventory.ItemStack;

import com.nisovin.shopkeepers.api.shopkeeper.ShopCreationData;
import com.nisovin.shopkeepers.shopkeeper.AbstractShopkeeper;
import com.nisovin.shopkeepers.shopobjects.living.LivingEntityObjectType;
import com.nisovin.shopkeepers.shopobjects.living.LivingEntityShop;

public class CatShop extends LivingEntityShop {

	private Ocelot.Type catType = Ocelot.Type.WILD_OCELOT;

	public CatShop(LivingEntityObjectType<CatShop> livingObjectType, AbstractShopkeeper shopkeeper, ShopCreationData creationData) {
		super(livingObjectType, shopkeeper, creationData);
	}

	@Override
	public void load(ConfigurationSection configSection) {
		super.load(configSection);
		String catTypeName = configSection.getString("catType");
		try {
			catType = Ocelot.Type.valueOf(catTypeName);
		} catch (Exception e) {
		}
	}

	@Override
	public void save(ConfigurationSection configSection) {
		super.save(configSection);
		configSection.set("catType", catType.name());
	}

	// SUB TYPES

	@Override
	protected void applySubType() {
		super.applySubType();
		if (!this.isActive()) return;
		assert entity.getType() == EntityType.OCELOT;
		((Ocelot) entity).setCatType(catType);
	}

	@Override
	public ItemStack getSubTypeItem() {
		return new ItemStack(Material.WOOL, 1, this.getSubItemData(catType));
	}

	@Override
	public void cycleSubType() {
		shopkeeper.markDirty();
		int id = catType.getId();
		catType = Ocelot.Type.getType(++id);
		if (catType == null) {
			catType = Ocelot.Type.WILD_OCELOT; // id 0
		}
		assert catType != null;
		this.applySubType();
	}

	private short getSubItemData(Ocelot.Type catType) {
		switch (catType) {
		case BLACK_CAT:
			return DyeColor.BLACK.getWoolData();
		case RED_CAT:
			return DyeColor.RED.getWoolData();
		case SIAMESE_CAT:
			return DyeColor.SILVER.getWoolData();

		case WILD_OCELOT:
		default:
			return DyeColor.ORANGE.getWoolData();
		}
	}
}