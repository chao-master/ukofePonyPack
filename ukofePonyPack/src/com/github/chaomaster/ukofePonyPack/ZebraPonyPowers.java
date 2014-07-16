/**
 * Copyright (C) 2013 chao-master
 * 
 * This file is part of ukofePonyPack.
 * 
 *     ukofePonyPack is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     ukofePonyPack is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with ukofePonyPack.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.chaomaster.ukofePonyPack;

import java.io.File;
import java.util.HashMap;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;

public class ZebraPonyPowers extends PonyPowers {

    private HashMap<Player,PortableBrewing> portableBrewingMap;
    
    //TODO: Load and save the brewing infomation to a file
	public ZebraPonyPowers(ukofePonyPack plugin) {
		super(plugin);
        portableBrewingMap = new HashMap<Player,PortableBrewing>();
	}
    
	public boolean reloadConfig() {
		File configFile = new File(this.plugin.getDataFolder(),
				"ZebraConfig.yml");
		YamlConfiguration config = YamlConfiguration
				.loadConfiguration(configFile);
		return super.reloadConfig(config);
	}

	@EventHandler
	public void onPotionSplashEvent(PotionSplashEvent event) {
		if (isOfActiveType((Entity) event.getPotion().getShooter())) {
			potionSplash(event);
		}
	}

	private void potionSplash(PotionSplashEvent event) {
		for (PotionEffect e : event.getPotion().getEffects()) {
			PotionEffect toGive = new PotionEffect(e.getType(),
					e.getDuration(), e.getAmplifier() + 1);
			for (LivingEntity p : event.getAffectedEntities()) {
				p.addPotionEffect(toGive, true);
			}
		}
		event.setCancelled(true);
	}
    
    @EventHandler
	public void onPlayerQuitEvent(PlayerQuitEvent event) {
		if (portableBrewingMap.containsKey(event.getPlayer())){
            portableBrewingMap.get(event.getPlayer()).saveAndEnd();
            portableBrewingMap.remove(event.getPlayer());
        }
	}

	@EventHandler
	public void onPlayerKickEvent(PlayerKickEvent event) {
		if (portableBrewingMap.containsKey(event.getPlayer())){
            portableBrewingMap.get(event.getPlayer()).saveAndEnd();
            portableBrewingMap.remove(event.getPlayer());
        }
	}
    
    @EventHandler
    public void onCloseInventory(InventoryCloseEvent event){
        final Player player = (Player) event.getPlayer();
        if (isOfActiveType(event.getPlayer())) {
            if (event.getInventory().getType() == InventoryType.CRAFTING){
                if (!portableBrewingMap.containsKey(player)){
                    portableBrewingMap.put(player, new PortableBrewing(player,plugin));
                }
                new BukkitRunnable() {
                    public void run() {
                        portableBrewingMap.get(player).showOwner();
                    }
                }.runTask(plugin);
            }
        }
    }
}