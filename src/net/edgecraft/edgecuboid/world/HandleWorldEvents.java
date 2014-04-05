package net.edgecraft.edgecuboid.world;

import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.User;
import net.edgecraft.edgecuboid.cuboid.Cuboid;
import net.edgecraft.edgecuboid.cuboid.Flag;
import net.edgecraft.edgecuboid.cuboid.types.CuboidType;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.material.Vine;
import org.bukkit.util.Vector;

public class HandleWorldEvents implements Listener {
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBorderCollision(PlayerMoveEvent event) {
		
		Player player = event.getPlayer();
		User user = EdgeCoreAPI.userAPI().getUser(player.getName());
		
		Location location = player.getLocation().clone();
		Location spawnLoc = player.getWorld().getHighestBlockAt(player.getWorld().getSpawnLocation()).getLocation();
		
		// Check if user exists and it's level > Architect
		if (user != null) {
			if (!Level.canUse(user, Level.ARCHITECT)) {
				
				// Get radius and distance to radius
				int radius = WorldManager.getInstance().getWorldBorder();
				double distance = location.distanceSquared(spawnLoc);
								
				if (distance >= Math.pow(radius, 2)) {
										
					// Check if the player's in a vehicle
					Entity vehicle = player.getVehicle();
					
					// Let the player leave the vehicle
					if (vehicle != null) {
						player.leaveVehicle();
						
						/*
						 * If the vehicle is an instance of LivingEntity (like horse or pig), teleport it to the from-location
						 * If not, remove the entity
						 */
						if (!(vehicle instanceof LivingEntity)) {
							
							vehicle.remove();
							
						} else {
							
							event.setTo(event.getFrom());							
							player.sendMessage(EdgeCoreAPI.languageAPI().getColoredMessage(user.getLanguage(), "radiusreached"));
						}
					}
					
					// Finally, after all checks, teleport the player to the location it's coming from and let him know why
					Vector playerVec = player.getLocation().toVector().clone();
					Vector spawnVec = spawnLoc.toVector().clone();
					
					Vector directionToSpawn = playerVec.clone().subtract(spawnVec).normalize();
					Vector nextPos = playerVec.add(directionToSpawn.clone().multiply(3));
					
					event.setTo(new Location(event.getFrom().getWorld(), nextPos.getBlockX(), nextPos.getBlockY(), nextPos.getBlockZ()));
					
					player.sendMessage(EdgeCoreAPI.languageAPI().getColoredMessage(user.getLanguage(), "radiusreached"));
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onBorderTeleport(PlayerTeleportEvent event) {
		
		Player player = event.getPlayer();
		User user = EdgeCoreAPI.userAPI().getUser(player.getName());
		
		if (user == null)
			return;
		
		if (event.getCause().equals(TeleportCause.ENDER_PEARL)) {
			if (event.getTo().distanceSquared(event.getTo().getWorld().getSpawnLocation()) >= Math.pow(WorldManager.getInstance().getWorldBorder(), 2)) {
				
				event.setCancelled(true);
				player.sendMessage(EdgeCoreAPI.languageAPI().getColoredMessage(user.getLang(), "radiusreached"));
				
			}			
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void onIceMelt(BlockFadeEvent event) {
		
		Block block = event.getBlock();
		
		if (!WorldManager.getInstance().isIceMeltAllowed()) {
			if (block.getType() == Material.ICE) {
				
				event.setCancelled(true);
				
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleBlockBurn(BlockBurnEvent event) {		
		if (!WorldManager.getInstance().isFireSpreadAllowed()) {
			
			if (Cuboid.getCuboid(event.getBlock().getLocation()) == null) {
				
				event.setCancelled(true);
				
			} else {
				
				Cuboid c = Cuboid.getCuboid(event.getBlock().getLocation());
				
				if (c.getCuboidType() != CuboidType.Survival.getTypeID() 
						|| c.getCuboidType() != CuboidType.Habitat.getTypeID() 
						|| c.getCuboidType() != CuboidType.Shop.getTypeID()) {
					
					event.setCancelled(true);
				}
			}			
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleFireSpread(BlockSpreadEvent event) {
		if (!WorldManager.getInstance().isFireSpreadAllowed()) {
			if (event.getSource().getType() == Material.FIRE || event.getSource().getType() == Material.LAVA || event.getSource().getType() == Material.STATIONARY_LAVA) {
				
				event.setCancelled(true);
				
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleStructureGrowing(StructureGrowEvent event) {
		if (!WorldManager.getInstance().isStructureGrowingAllowed()) {
			
			event.setCancelled(true);
			
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleItemFrameRotation(PlayerInteractEntityEvent event) {
		
		Player player = event.getPlayer();
		User user = EdgeCoreAPI.userAPI().getUser(player.getName());
		
		if (user == null)
			return;
		
		if (event.getRightClicked().getType() == EntityType.ITEM_FRAME) {
			
			ItemFrame frame = (ItemFrame) event.getRightClicked();
			
			if (frame.getItem() == null || frame.getItem().getType() == Material.AIR)
				return;
			
			if (!Level.canUse(user, Level.ARCHITECT)) {
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
	public void handleHangingEntities(HangingBreakByEntityEvent event) {
		
		if (!(event.getRemover() instanceof Player))
			return;
		
		Player player = (Player) event.getRemover();
		User user = EdgeCoreAPI.userAPI().getUser(player.getName());
		
		if (user == null)
			return;
		
		if (event.getEntity() instanceof ItemFrame || event.getEntity() instanceof Vine) {
			if (Cuboid.getCuboid(event.getEntity().getLocation()) != null) {
				
				Cuboid c = Cuboid.getCuboid(event.getEntity().getLocation());
				
				if (c.getCuboidType() == CuboidType.Survival.getTypeID())
					return;
				
				if (!Flag.hasFlag(c, Flag.BreakBlocks, player.getName())) {
					event.setCancelled(true);
					player.sendMessage(EdgeCoreAPI.languageAPI().getColoredMessage(user.getLanguage(), "cuboid_nopermission"));
				}
			}
		}
	}
}
