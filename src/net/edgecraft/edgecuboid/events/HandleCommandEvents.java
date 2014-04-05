package net.edgecraft.edgecuboid.events;

import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.lang.LanguageHandler;
import net.edgecraft.edgecore.user.User;
import net.edgecraft.edgecuboid.cuboid.Cuboid;
import net.edgecraft.edgecuboid.cuboid.CuboidHandler;
import net.edgecraft.edgecuboid.cuboid.types.CuboidType;
import net.edgecraft.edgecuboid.world.WorldManager;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class HandleCommandEvents implements Listener {
	
	private final LanguageHandler lang = EdgeCoreAPI.languageAPI();
	
	@EventHandler
	public void onCuboidCreation(PlayerInteractEvent event) {
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			Player player = event.getPlayer();
			User user = EdgeCoreAPI.userAPI().getUser(player.getName());
			
			try {
				
				if (user != null && CuboidHandler.getInstance().isCreating(player.getName()) && event.getItem() != null) {
					
					Material material = event.getItem().getType();
					
					if (material == WorldManager.getInstance().getCreationItem()) {
						
						Cuboid creation = CuboidHandler.getInstance().getCreatingPlayers().get(player.getName());
						
						Location spawn = creation.getSpawn();
						Location minLoc = creation.getMinLocation();
						Location maxLoc = creation.getMaxLocation();
						
						if (minLoc != null && maxLoc != null) {
							
							event.setCancelled(true);
							
							spawn = event.getClickedBlock().getLocation().clone();
							String x = maxLoc.getBlockX() + "";
							String y = maxLoc.getBlockY() + "";
							String z = maxLoc.getBlockZ() + "";
							
							creation.updateSpawn(spawn);
							player.sendMessage(lang.getColoredMessage(user.getLanguage(), "cuboid_creation_point").replace("[0]", "Spawn").replace("[1]", x).replace("[2]", y).replace("[3]", z));
							
							CuboidHandler.getInstance().registerCuboid(creation.getName(), creation.getOwnerID(), 
									CuboidType.getType(creation.getCuboidType()), creation.getModifyLevel(), creation.getSpawn(), creation.getMinLocation(), 
									creation.getMaxLocation(), "", "", null, null, null, null);

							CuboidHandler.getInstance().getCreatingPlayers().remove(player.getName());

							player.sendMessage(lang.getColoredMessage(user.getLanguage(), "cuboid_creation_success").replace("[0]", creation.getName()));
						}
						
						if (minLoc != null && maxLoc == null) {
							
							event.setCancelled(true);
							
							maxLoc = event.getClickedBlock().getLocation().clone();
							String x = maxLoc.getBlockX() + "";
							String y = maxLoc.getBlockY() + "";
							String z = maxLoc.getBlockZ() + "";
							
							creation.updateMaxLocation(maxLoc);
							player.sendMessage(lang.getColoredMessage(user.getLanguage(), "cuboid_creation_point").replace("[0]", "2").replace("[1]", x).replace("[2]", y).replace("[3]", z));
							
						}
						
						if (minLoc == null && maxLoc == null) {
							
							event.setCancelled(true); 
							
							minLoc = event.getClickedBlock().getLocation().clone();
							String x = minLoc.getBlockX() + "";
							String y = minLoc.getBlockY() + "";
							String z = minLoc.getBlockZ() + "";
							
							creation.updateMinLocation(minLoc);
							player.sendMessage(lang.getColoredMessage(user.getLanguage(), "cuboid_creation_point").replace("[0]", "1").replace("[1]", x).replace("[2]", y).replace("[3]", z));
							
						}
					}
				}
				
			} catch(Exception e) {
				e.printStackTrace();
				player.sendMessage(lang.getColoredMessage(user.getLanguage(), "globalerror"));
			}
		}
	}	
	
	@EventHandler
	public void onCuboidFind(PlayerInteractEvent event) {
		
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			Player player = event.getPlayer();
			User user = EdgeCoreAPI.userAPI().getUser(player.getName());
			
			if (user != null && CuboidHandler.getInstance().isSearching(player.getName()) && event.getItem() != null) {
				
				Material material = event.getItem().getType();
				String userLang = user.getLanguage();
				
				if (material == WorldManager.getInstance().getFindItem()) {
					
					Cuboid cuboid = Cuboid.getCuboid(event.getClickedBlock().getLocation());
					
					if (cuboid == null) {						
						event.setCancelled(true);
						player.sendMessage(lang.getColoredMessage(user.getLanguage(), "cuboid_find_nocuboid"));
						return;
					}
										
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_title").replace("[0]", cuboid.getName()));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_name").replace("[0]", cuboid.getName()));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_type").replace("[0]", CuboidType.getType(cuboid.getCuboidType()).name()));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_owner").replace("[0]", cuboid.getOwner().getName()));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_center").replace("[0]", cuboid.getCenter().getBlockX() + "")
																							.replace("[1]", cuboid.getCenter().getBlockY() + "")
																							.replace("[2]", cuboid.getCenter().getBlockZ() + ""));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_blocks").replace("[0]", cuboid.getArea() + "").replace("[1]", cuboid.getVolume() + ""));
					player.sendMessage(lang.getColoredMessage(userLang, "admin_cuboid_info_participants").replace("[0]", cuboid.getParticipants().size() + ""));
					
				}
			}
		}
	}
}
