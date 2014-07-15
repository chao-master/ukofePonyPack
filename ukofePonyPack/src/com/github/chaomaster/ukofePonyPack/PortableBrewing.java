/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.chaomaster.ukofePonyPack;

import java.util.Arrays;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 *
 * @author stuart
 */
public class PortableBrewing implements Listener {
    private HumanEntity owner;
    private Inventory inventory;
    private Plugin plugin;
    final BukkitTask brewTask;
    private int progress;
    private ItemStack[] lastSlots = {new ItemStack(Material.AIR),new ItemStack(Material.AIR),new ItemStack(Material.AIR),new ItemStack(Material.AIR)};
    private short[] brewTo = {-1,-1,-1};
    private boolean isBrewing;
    
    public PortableBrewing(HumanEntity owner,final Plugin plugin){
        this.owner = owner;
        this.inventory = owner.getServer().createInventory(null, InventoryType.BREWING);
        this.plugin = plugin;
        
        this.brewTask = new BukkitRunnable() {
            public void run() {
                boolean changedIngredient = !comapreUpdate(3);
                for(int i=0;i<3;i++){
                    if (!comapreUpdate(i) || changedIngredient){
                        brewTo[i] = brewHelper(i);
                    }
                }
                if (brewTo[0] == -1 && brewTo[1] == -1 && brewTo[2] == -1){
                    isBrewing = false;
                    progress = 0;
                } else {
                    isBrewing = true;
                    if(changedIngredient){
                        progress = 0;
                    } else if (progress >= 400) {
                        progress = 0;
                        inventory.clear(3);
                        for(int i=0;i<3;i++){
                            if (brewTo[i] != -1){
                                inventory.setItem(i, new ItemStack(Material.POTION,1,brewTo[i]));
                            }
                            brewTo[i] = -1;
                        }
                    } else {
                        progress ++;
                    }
                }
                for(HumanEntity viewer: inventory.getViewers()){
                    viewer.setWindowProperty(InventoryView.Property.BREW_TIME,401-progress);
                }
            };
        }.runTaskTimer(plugin, 1,1);
    }
    
    public void showOwner(){
        owner.openInventory(inventory);
    }
    
    private short brewHelper(int slot){
        ItemStack potion = this.inventory.getItem(slot);
        ItemStack ingredient = inventory.getItem(3);
        if (potion == null || ingredient == null || potion.getType() != Material.POTION){
            return -1;
        }
        short rtn = (short) brewHelper(ingredient,potion.getDurability());
        this.plugin.getLogger().info("Slot "+slot+": "+potion.getDurability()+"+"+ingredient.getType().toString()+"="+rtn);
        return rtn;
    }
    
    private boolean comapreUpdate(int i){
        ItemStack inv = inventory.getItem(i);
        if (inv == null){
            inv = new ItemStack(Material.AIR);
        }
        ItemStack stor = lastSlots[i];
        lastSlots[i] = inv;
        return stor.isSimilar(lastSlots[i]);
    }
    
    static private int brewHelper(ItemStack ingredient,int current){
        int effect = current & 0xf;
        boolean awkard = (current & 0x10) == 0x10;
        boolean boosted = (current & 0x20) == 0x20;
        boolean extended = (current & 0x40) == 0x40;
        boolean drinkable = (current & 0x2000) == 0x2000;
        boolean splash = (current & 0x4000) == 0x4000;
        
        if(ingredient.getType() == Material.FERMENTED_SPIDER_EYE){
            switch (effect){
                case 0: case 1: case 9: effect = 8; break; //Water/Awk, Regen, Str -> Weakness
                case 2: case 3: effect = 10; break; //Swift, Fire resist -> Slowness
                case 4: case 5: case 13: effect = 12; break; //Posion, Healing, Water Beath -> Damaging
                case 6: effect = 14; break; //Night vision -> Invisibility
            }
        } else if(ingredient.getType() == Material.GLOWSTONE_DUST){
            boosted = true;
            extended = false;
        } else if(ingredient.getType() == Material.REDSTONE){
            extended = true;
            boosted = false;
        } else if(ingredient.getType() == Material.SULPHUR){
            if (effect == 0 || splash){
                return -1;
            } else {
                splash = true;
            }
        } else if (effect == 0){
            switch (ingredient.getType()){
                case MAGMA_CREAM:   effect = 3; break;
                case SUGAR:         effect = 2; break;
                case RAW_FISH:
                    if (ingredient.getDurability() == 3){
                        effect = 13;
                    } else {
                        effect = -1;
                    }; break;
                case SPECKLED_MELON:effect = 5; break;
                case SPIDER_EYE:    effect = 4; break;
                case GOLDEN_CARROT: effect = 6; break;
                case GHAST_TEAR:    effect = 1; break;
                case BLAZE_POWDER:  effect = 9; break;
                case NETHER_STALK:
                    return current==0?16:-1; //Special Case for awkard
                default: return -1;
            }
            if (!awkard & !boosted & !extended){
                return 0x2000; //Mundane potion of weakness from non-wart
            }
        } else {
            return -1;
        }
        if (effect != 0 || boosted || extended){
            awkard = false;
        }
        return effect+(awkard?0x10:0)+(boosted?0x20:0)+(extended?0x40:0)+(drinkable?0x2000:0)+(splash?0x4000:0);
    }
}
