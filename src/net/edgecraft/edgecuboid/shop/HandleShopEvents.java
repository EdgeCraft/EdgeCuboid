package net.edgecraft.edgecuboid.shop;

import net.edgecraft.edgeconomy.EdgeConomyAPI;
import net.edgecraft.edgeconomy.economy.BankAccount;
import net.edgecraft.edgeconomy.economy.EconomyPlayer;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.UserManager;
import net.edgecraft.edgecuboid.cuboid.Cuboid;
import net.edgecraft.edgecuboid.other.EdgeItemStack;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;

public class HandleShopEvents implements Listener {
	
	@EventHandler
	public void openShopGUI(PlayerInteractEvent event) {
		if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
			
			Player player = event.getPlayer();
			Block block = event.getClickedBlock();
			Cuboid cuboid = Cuboid.getCuboid(player.getLocation());
			
			if (!Level.canUse(UserManager.getInstance().getUser(player.getName()), Level.USER))
				return;
			
			if (cuboid != null) {
				if (!cuboid.isInside(block.getLocation()))
					return;
				
				Shop shop = ShopHandler.getInstance().getShop(cuboid);
				
				if (shop == null)
					return;
				
				if (!ShopHandler.getInstance().getShopFrames().contains(block.getType()))
					return;
				
				player.openInventory(shop.getGui());
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void handleItemPurchase(InventoryClickEvent event) {
		if (event.getClick() == ClickType.LEFT) {
			
			Player player = (Player) event.getWhoClicked();
			Inventory inventory = event.getInventory();
			Cuboid cuboid = Cuboid.getCuboid(player.getLocation());
			
			if (!Level.canUse(UserManager.getInstance().getUser(player.getName()), Level.USER))
				return;
			
			if (cuboid != null) {
				
				Shop shop = ShopHandler.getInstance().getShop(cuboid);
				
				if (shop == null)
					return;
				
				if (inventory.getName().equals(shop.getGui().getName())) {
					
					event.setCancelled(true);
					player.updateInventory();
					
					if (EdgeConomyAPI.economyAPI().getAccount(player.getName()) == null) {
						
						EconomyPlayer ep = EdgeConomyAPI.economyAPI().getEconomyPlayer(player.getName());
						EdgeItemStack item = new EdgeItemStack(event.getCurrentItem());
						
						shop.buyItem(ep, item);
						
					} else {
						
						BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(player.getName());
						EdgeItemStack item = new EdgeItemStack(event.getCurrentItem());
						
						shop.buyItem(acc, item);
						
					}
				}
			}
		}
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void handleItemSale(InventoryClickEvent event) {
		if (event.getClick() == ClickType.RIGHT) {
			
			Player player = (Player) event.getWhoClicked();
			Inventory inventory = event.getInventory();
			Cuboid cuboid = Cuboid.getCuboid(player.getLocation());
			
			if (!Level.canUse(UserManager.getInstance().getUser(player.getName()), Level.USER))
				return;
			
			if (cuboid != null) {
				
				Shop shop = ShopHandler.getInstance().getShop(cuboid);
				
				if (shop == null)
					return;
				
				if (inventory.getName().equals(shop.getGui().getName())) {
					
					event.setCancelled(true);
					player.updateInventory();
					
					if (EdgeConomyAPI.economyAPI().getAccount(player.getName()) == null) {
						
						EconomyPlayer ep = EdgeConomyAPI.economyAPI().getEconomyPlayer(player.getName());
						EdgeItemStack item = new EdgeItemStack(event.getCurrentItem());
						
						shop.sellItem(ep, item);
						
					} else {
						
						BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(player.getName());
						EdgeItemStack item = new EdgeItemStack(event.getCurrentItem());
						
						shop.sellItem(acc, item);
						
					}
				}
			}
		}
	}
}