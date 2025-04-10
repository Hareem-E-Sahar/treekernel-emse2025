package com.ao.model.worldobject.factory;

import java.util.HashMap;
import com.ao.model.worldobject.AbstractItem;
import com.ao.model.worldobject.Accessory;
import com.ao.model.worldobject.AgilityPotion;
import com.ao.model.worldobject.Ammunition;
import com.ao.model.worldobject.Armor;
import com.ao.model.worldobject.Backpack;
import com.ao.model.worldobject.Boat;
import com.ao.model.worldobject.DeathPotion;
import com.ao.model.worldobject.Door;
import com.ao.model.worldobject.Drink;
import com.ao.model.worldobject.EmptyBottle;
import com.ao.model.worldobject.FilledBottle;
import com.ao.model.worldobject.Food;
import com.ao.model.worldobject.Forum;
import com.ao.model.worldobject.Gold;
import com.ao.model.worldobject.GrabableProp;
import com.ao.model.worldobject.HPPotion;
import com.ao.model.worldobject.Helmet;
import com.ao.model.worldobject.Ingot;
import com.ao.model.worldobject.Key;
import com.ao.model.worldobject.ManaPotion;
import com.ao.model.worldobject.Mine;
import com.ao.model.worldobject.Mineral;
import com.ao.model.worldobject.MusicalInstrument;
import com.ao.model.worldobject.Parchment;
import com.ao.model.worldobject.PoisonPotion;
import com.ao.model.worldobject.Prop;
import com.ao.model.worldobject.RangedWeapon;
import com.ao.model.worldobject.Shield;
import com.ao.model.worldobject.Sign;
import com.ao.model.worldobject.Staff;
import com.ao.model.worldobject.StrengthPotion;
import com.ao.model.worldobject.Teleport;
import com.ao.model.worldobject.Tree;
import com.ao.model.worldobject.Weapon;
import com.ao.model.worldobject.Wood;
import com.ao.model.worldobject.WorldObject;
import com.ao.model.worldobject.WorldObjectType;
import com.ao.model.worldobject.properties.WorldObjectProperties;

/**
 * Factory to create instances of items based on their properties.
 * @author itirabasso
 */
public class WorldObjectFactory {

    protected static final HashMap<WorldObjectType, Class<? extends WorldObject>> worldObjectMapper;

    static {
        worldObjectMapper = new HashMap<WorldObjectType, Class<? extends WorldObject>>();
        worldObjectMapper.put(WorldObjectType.ACCESSORY, Accessory.class);
        worldObjectMapper.put(WorldObjectType.AGILITY_POTION, AgilityPotion.class);
        worldObjectMapper.put(WorldObjectType.AMMUNITION, Ammunition.class);
        worldObjectMapper.put(WorldObjectType.ARMOR, Armor.class);
        worldObjectMapper.put(WorldObjectType.BACKPACK, Backpack.class);
        worldObjectMapper.put(WorldObjectType.BOAT, Boat.class);
        worldObjectMapper.put(WorldObjectType.DEATH_POTION, DeathPotion.class);
        worldObjectMapper.put(WorldObjectType.DOOR, Door.class);
        worldObjectMapper.put(WorldObjectType.DRINK, Drink.class);
        worldObjectMapper.put(WorldObjectType.EMPTY_BOTTLE, EmptyBottle.class);
        worldObjectMapper.put(WorldObjectType.FILLED_BOTTLE, FilledBottle.class);
        worldObjectMapper.put(WorldObjectType.FOOD, Food.class);
        worldObjectMapper.put(WorldObjectType.FORUM, Forum.class);
        worldObjectMapper.put(WorldObjectType.GRABABLE_PROP, GrabableProp.class);
        worldObjectMapper.put(WorldObjectType.HELMET, Helmet.class);
        worldObjectMapper.put(WorldObjectType.HP_POTION, HPPotion.class);
        worldObjectMapper.put(WorldObjectType.INGOT, Ingot.class);
        worldObjectMapper.put(WorldObjectType.KEY, Key.class);
        worldObjectMapper.put(WorldObjectType.MANA_POTION, ManaPotion.class);
        worldObjectMapper.put(WorldObjectType.MINE, Mine.class);
        worldObjectMapper.put(WorldObjectType.MINERAL, Mineral.class);
        worldObjectMapper.put(WorldObjectType.MONEY, Gold.class);
        worldObjectMapper.put(WorldObjectType.MUSICAL_INSTRUMENT, MusicalInstrument.class);
        worldObjectMapper.put(WorldObjectType.PARCHMENT, Parchment.class);
        worldObjectMapper.put(WorldObjectType.POISON_POTION, PoisonPotion.class);
        worldObjectMapper.put(WorldObjectType.PROP, Prop.class);
        worldObjectMapper.put(WorldObjectType.RANGED_WEAPON, RangedWeapon.class);
        worldObjectMapper.put(WorldObjectType.SHIELD, Shield.class);
        worldObjectMapper.put(WorldObjectType.SIGN, Sign.class);
        worldObjectMapper.put(WorldObjectType.STAFF, Staff.class);
        worldObjectMapper.put(WorldObjectType.STRENGTH_POTION, StrengthPotion.class);
        worldObjectMapper.put(WorldObjectType.TELEPORT, Teleport.class);
        worldObjectMapper.put(WorldObjectType.TREE, Tree.class);
        worldObjectMapper.put(WorldObjectType.WEAPON, Weapon.class);
        worldObjectMapper.put(WorldObjectType.WOOD, Wood.class);
    }

    /**
	 * Creates a new instance of the appropriate AbstractItem given it's properties.
	 * @param woProperties The properties from which to create an object.
	 * @param amount The amount of the given object to create.
	 * @return The newly created object.
	 * @throws WorldObjectFactoryException
	 */
    public AbstractItem getWorldObject(WorldObjectProperties woProperties, int amount) throws WorldObjectFactoryException {
        @SuppressWarnings("unchecked") Class<? extends AbstractItem> woClass = (Class<? extends AbstractItem>) worldObjectMapper.get(woProperties.getType());
        try {
            return woClass.getConstructor(woProperties.getClass(), int.class).newInstance(woProperties, amount);
        } catch (Exception e) {
            throw new WorldObjectFactoryException(e);
        }
    }

    /**
	 * Creates a new instance of the appropriate WorldObject given it's properties.
	 * @param woProperties The properties from which to create an object.
	 * @return The newly created object.
	 * @throws WorldObjectFactoryException
	 */
    public WorldObject getWorldObject(WorldObjectProperties woProperties) throws WorldObjectFactoryException {
        Class<? extends WorldObject> woClass = worldObjectMapper.get(woProperties.getType());
        try {
            return woClass.getConstructor(woProperties.getClass()).newInstance(woProperties);
        } catch (Exception e) {
            throw new WorldObjectFactoryException(e);
        }
    }
}
