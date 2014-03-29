package net.edgecraft.edgecuboid.commands;

import net.edgecraft.edgeconomy.EdgeConomyAPI;
import net.edgecraft.edgeconomy.economy.BankAccount;
import net.edgecraft.edgecore.EdgeCore;
import net.edgecraft.edgecore.command.AbstractCommand;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.User;
import net.edgecraft.edgecuboid.EdgeCuboid;
import net.edgecraft.edgecuboid.EdgeCuboidAPI;
import net.edgecraft.edgecuboid.cuboid.CuboidHandler;
import net.edgecraft.edgecuboid.cuboid.Habitat;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HomeCommand extends AbstractCommand {
	
	private final CuboidHandler cuboidHandler = EdgeCuboidAPI.cuboidAPI();
	
	private static final HomeCommand instance = new HomeCommand();
	
	private HomeCommand() { super(); }
	
	public static final HomeCommand getInstance() {
		return instance;
	}
	
	@Override
	public Level getLevel() {
		return Level.valueOf(EdgeCuboid.getInstance().getConfig().getString("Command.home"));
	}

	@Override
	public String[] getNames() {
		String[] names = { "home", "h" };
		return names;
	}

	@Override
	public boolean runImpl(Player player, User user, String[] args) throws Exception {
		
		String userLang = user.getLanguage();
		
		try {
			
			if (args[1].equalsIgnoreCase("buy")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				if (!cuboidHandler.existsHabitat(args[2])) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownhabitat").replace("[0]", args[2]));
					return true;
				}
				
				Habitat habitat = cuboidHandler.getHabitat(args[2]);
				
				if (!habitat.isBuyable()) {
					player.sendMessage(lang.getColoredMessage(userLang, "home_buy_notbuyable"));
					return true;
				}
				
				if (habitat.isOwner(player.getName())) {
					player.sendMessage(lang.getColoredMessage(userLang, "home_buy_ownhome"));
					return true;
				}
				
				if (habitat.isTenant(player.getName())) {
					player.sendMessage(lang.getColoredMessage(userLang, "home_buy_alreadytenant"));
					return true;
				}
				
				BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(player.getName());
				
				if (acc == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noaccount"));
					return true;
				}
				
				if (acc.getBalance() < habitat.getWorth()) {
					player.sendMessage(lang.getColoredMessage(userLang, "notenoughmoney"));
					return true;
				}
				
				habitat.switchOwner(player.getName());
				habitat.setBuyable(false);
				habitat.setRentable(false);
				
				acc.updateBalance(acc.getBalance() - habitat.getWorth());
				
				player.sendMessage(lang.getColoredMessage(userLang, "home_buy_success").replace("[0]", args[2]).replace("[1]", habitat.getWorth() + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("sell")) {
				if (args.length == 3) {
										
					Habitat habitat = cuboidHandler.getHabitatByOwner(player.getName());
					
					if (habitat == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_sell_nohome"));
						return true;
					}
										
					if (habitat.isBuyable()) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_sell_alreadyforsale"));
						return true;
					}
					
					double price = Double.parseDouble(args[2]);
										
					if (price <= 0) {
						player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
						return true;
					}
					
					habitat.setBuyable(true);
					habitat.updateWorth(price);
					
					player.sendMessage(lang.getColoredMessage(userLang, "home_sell_success").replace("[0]", habitat.getCuboid().getName()).replace("[1]", price + ""));
					
					return true;
				}
				
				if (args.length == 4) {
					if (!Level.canUse(user, Level.ARCHITECT)) {
						player.sendMessage(lang.getColoredMessage(userLang, "nopermission"));
						return true;
					}
					
					if (!cuboidHandler.existsHabitat(args[3])) {
						player.sendMessage(lang.getColoredMessage(userLang, "unknownhabitat").replace("[0]", args[3]));
						return true;
					}
					
					Habitat habitat = cuboidHandler.getHabitat(args[3]);
					
					if  (!habitat.isOwner(player.getName())) {
						player.sendMessage(lang.getColoredMessage(userLang, "admin_home_notowner"));
					}
					
					if (habitat.isBuyable()) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_sell_alreadyforsale"));
						return true;
					}
					
					double price = Double.parseDouble(args[2]);
										
					if (price <= 0) {
						player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
						return true;
					}
					
					if (Bukkit.getPlayerExact(habitat.getOwner()) != null) {
						Bukkit.getPlayerExact(habitat.getOwner()).sendMessage(lang.getColoredMessage(userLang, "admin_home_sell_ownerwarning").replace("[0]", player.getName()).replace("[1]", args[3]));
					}
					
					habitat.setBuyable(true);
					habitat.updateWorth(price);
					
					player.sendMessage(lang.getColoredMessage(userLang, lang.getColoredMessage(userLang, "admin_home_sell_success").replace("[0]", args[3]).replace("[1]", habitat.getOwner())));
					
					return true;
				}
			}
			
			if (args[1].equalsIgnoreCase("lease")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				if (!cuboidHandler.existsHabitat(args[2])) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownhabitat").replace("[0]", args[2]));
					return true;
				}
				
				Habitat habitat = cuboidHandler.getHabitat(args[2]);
				BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(player.getName());
				
				if (acc == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noaccount"));
					return true;
				}
				
				if (!habitat.isRentable()) {
					player.sendMessage(lang.getColoredMessage(userLang, "home_lease_notleasable"));
					return true;
				}
				
				if (habitat.getRental() > acc.getBalance()) {
					player.sendMessage(lang.getColoredMessage(userLang, "notenoughmoney"));
					return true;
				}
				
				if (Bukkit.getPlayerExact(habitat.getOwner()) != null) {
					Bukkit.getPlayerExact(habitat.getOwner()).sendMessage(lang.getColoredMessage(userLang, "home_lease_ownerinfo").replace("[0]", args[2]).replace("[1]", player.getName()));
				}
				
				habitat.setRentable(false);
				habitat.switchTenant(player.getName());
				
				player.sendMessage(lang.getColoredMessage(userLang, "home_lease_success").replace("[0]", args[2]).replace("[1]", habitat.getRental() + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("rent")) {
				if (args.length == 3) {
										
					Habitat habitat = cuboidHandler.getHabitatByOwner(player.getName());
					
					if (habitat == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_nohome"));
						return true;
					}
										
					if (habitat.isRentable()) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_rent_alreadyforrent"));
						return true;
					}
					
					double price = Double.parseDouble(args[2]);
										
					if (price <= 0) {
						player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
						return true;
					}
					
					habitat.setRentable(true);
					habitat.updateRental(price);
					
					player.sendMessage(lang.getColoredMessage(userLang, "home_rent_success").replace("[0]", habitat.getCuboid().getName()).replace("[1]", price + ""));
					
					return true;
				}
				
				if (!Level.canUse(user, Level.ARCHITECT)) {
					player.sendMessage(lang.getColoredMessage(userLang, "nopermission"));
					return true;
				}
				
				if (args.length == 4) {
					
					if (!cuboidHandler.existsHabitat(args[3])) {
						player.sendMessage(lang.getColoredMessage(userLang, "unknownhabitat").replace("[0]", args[3]));
						return true;
					}
					
					Habitat habitat = cuboidHandler.getHabitat(args[3]);
					
					if  (!habitat.isOwner(player.getName())) {
						player.sendMessage(lang.getColoredMessage(userLang, "admin_home_notowner"));
					}
					
					if (habitat.isRentable()) {
						player.sendMessage(lang.getColoredMessage(userLang, "home_rent_alreadyforrent"));
						return true;
					}
					
					double price = Double.parseDouble(args[2]);
										
					if (price <= 0) {
						player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
						return true;
					}
					
					if (Bukkit.getPlayerExact(habitat.getOwner()) != null) {
						Bukkit.getPlayerExact(habitat.getOwner()).sendMessage(lang.getColoredMessage(userLang, "admin_home_rentownerwarning").replace("[0]", player.getName()).replace("[1]", args[3]));
					}
					
					habitat.setRentable(true);
					habitat.updateRental(price);
					
					player.sendMessage(lang.getColoredMessage(userLang, lang.getColoredMessage(userLang, "admin_home_rent_success").replace("[0]", args[3]).replace("[1]", price + "")));
					
					return true;
				}
			}
			
			if (args[1].equalsIgnoreCase("info")) {
				player.sendMessage(lang.getColoredMessage(userLang, "pluginexception").replace("[0]", "EdgeCuboid"));
				return true;
			}
									
		} catch(NumberFormatException e) {
			player.sendMessage(lang.getColoredMessage(userLang, "numberformatexception"));
		}
		
		return true;
	}

	@Override
	public void sendUsageImpl(CommandSender sender) {
		if (!(sender instanceof Player)) return;
		
		sender.sendMessage(EdgeCore.usageColor + "/home buy <habitat>");
		sender.sendMessage(EdgeCore.usageColor + "/home sell <price> [<habitat>]");
		sender.sendMessage(EdgeCore.usageColor + "/home lease <habitat>");
		sender.sendMessage(EdgeCore.usageColor + "/home rent <rental> [<habitat>]");
		sender.sendMessage(EdgeCore.usageColor + "/home info [<habitat>]");
		
	}

	@Override
	public boolean sysAccess(CommandSender sender, String[] args) {
		return true;
	}

	@Override
	public boolean validArgsRange(String[] args) {
		return (args.length > 1 && args.length <= 5);
	}

}
