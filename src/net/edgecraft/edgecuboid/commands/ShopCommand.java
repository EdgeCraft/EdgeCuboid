package net.edgecraft.edgecuboid.commands;

import java.util.Arrays;
import java.util.List;

import net.edgecraft.edgeconomy.EdgeConomyAPI;
import net.edgecraft.edgeconomy.economy.BankAccount;
import net.edgecraft.edgeconomy.economy.Economy;
import net.edgecraft.edgecore.EdgeCore;
import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.command.AbstractCommand;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.User;
import net.edgecraft.edgecuboid.EdgeCuboidAPI;
import net.edgecraft.edgecuboid.cuboid.Cuboid;
import net.edgecraft.edgecuboid.cuboid.CuboidHandler;
import net.edgecraft.edgecuboid.cuboid.types.CuboidType;
import net.edgecraft.edgecuboid.other.EdgeItemStack;
import net.edgecraft.edgecuboid.shop.Shop;
import net.edgecraft.edgecuboid.shop.Shop.ShopType;
import net.edgecraft.edgecuboid.shop.ShopHandler;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopCommand extends AbstractCommand {
	
	private static final ShopCommand instance = new ShopCommand();
	
	private ShopCommand() { super(); }
	
	public static final ShopCommand getInstance() {
		return instance;
	}
	
	private final ShopHandler shopHandler = EdgeCuboidAPI.shopAPI();
	
	@Override
	public Level getLevel() {
		return Level.DEVELOPER;
	}

	@Override
	public String[] getNames() {
		return new String[] { "shop" };
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
				
				if (!shopHandler.existsShop(args[2])) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				Shop shop = shopHandler.getShop(player.getName());
				
				if (!shop.isBuyable()) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_notbuyable"));
					return true;
				}
				
				if (shop.isOwner(player.getName())) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_alreadyowner"));
					return true;
				}
				
				BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(user.getID());
				
				if (acc == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noaccount"));
					return true;
				}
				
				if (acc.getBalance() < shop.getPrice()) {
					player.sendMessage(lang.getColoredMessage(userLang, "notenoughmoney"));
					return true;
				}
				
				shop.switchOwner(user);
				acc.updateBalance(acc.getBalance() - shop.getPrice());
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_buy_success").replace("[0]", args[2]).replace("[1]", shop.getPrice() + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("rent")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				if (!shopHandler.existsShop(args[2])) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				Shop shop = shopHandler.getShop(player.getName());
				
				if (!shop.isRentable()) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_notrentable"));
					return true;
				}
				
				if (shop.isOwner(player.getName())) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_alreadyowner"));
					return true;
				}
				
				BankAccount acc = EdgeConomyAPI.economyAPI().getAccount(user.getID());
				
				if (acc == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noaccount"));
					return true;
				}
				
				if (acc.getBalance() < shop.getRental()) {
					player.sendMessage(lang.getColoredMessage(userLang, "notenoughmoney"));
					return true;
				}
				
				shop.switchOwner(user);
				acc.updateBalance(acc.getBalance() - shop.getRental());
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_rent_success").replace("[0]", args[2]).replace("[1]", shop.getPrice() + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("additem")) {
				if (args.length == 3) {
					
					if (shopHandler.getShop(player.getName()) == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
						return true;
					}
					
					Shop shop = shopHandler.getShop(player.getName());					
					addItem(player, user, shop, player.getItemInHand(), Double.parseDouble(args[2]));
					
					return true;
				}
				
				if (args.length == 4) {
					
					if (shopHandler.getShop(player.getName()) == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
						return true;
					}
					
					Shop shop = shopHandler.getShop(player.getName());					
					addItem(player, user, shop, player.getItemInHand(), Double.parseDouble(args[2]));
					
					return true;
				}
			}
			
			if (args[1].equalsIgnoreCase("getitem")) {
				if (args.length == 3) {
					
					if (shopHandler.getShop(player.getName()) == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
						return true;
					}
					
					Shop shop = shopHandler.getShop(player.getName());					
					getItem(player, user, shop, player.getItemInHand());
					
					return true;
				}
				
				if (args.length == 4) {
					
					if (shopHandler.getShop(player.getName()) == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
						return true;
					}
					
					Shop shop = shopHandler.getShop(player.getName());					
					getItem(player, user, shop, player.getItemInHand());
					
					return true;
				}
			}
			
			if (args[1].equalsIgnoreCase("itemprice")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				Shop shop = shopHandler.getShop(player.getName());
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
					return true;
				}
				
				ItemStack item = new ItemStack(Material.valueOf(args[2].toUpperCase()));
				
				if (item == null || item.getType() == Material.AIR) {
					player.sendMessage(lang.getColoredMessage(userLang, "invaliditem"));
					return true;
				}
				
				if (!shop.getGuiItems().containsKey(new EdgeItemStack(item))) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_getitem_notused"));
					return true;
				}
				
				double price = Double.parseDouble(args[3]);
				
				shop.addItem(new EdgeItemStack(item), price);
				player.sendMessage(lang.getColoredMessage(userLang, "shop_itemprice_success").replace("[0]", args[2]).replace("[1]", price + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("near")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				if (Double.parseDouble(args[2]) > 450) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoohigh"));
					return true;
				}
				
				if (Double.parseDouble(args[2]) <= 0) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
					return true;
				}
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_near_success")
						.replace("[0]", shopHandler.getNearShops(player, Double.parseDouble(args[2])).get(0).getCuboid().getName())
						.replace("[1]", Double.parseDouble(args[2]) + ""));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("providing")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				if (new ItemStack(Material.valueOf(args[2].toUpperCase())).getType() == Material.AIR) {
					player.sendMessage(lang.getColoredMessage(userLang, "invaliditem"));
					return true;
				}
				
				if (Double.parseDouble(args[3]) > 450) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoohigh"));
					return true;
				}
				
				if (Double.parseDouble(args[3]) <= 0) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
					return true;
				}
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_providing_success")
						.replace("[0]", shopHandler.getProvidingShops(player, new EdgeItemStack(new ItemStack(Material.valueOf(args[2]))), Double.parseDouble(args[2])).get(0).getCuboid().getName())
						.replace("[1]", Double.parseDouble(args[2]) + "")
						.replace("[2]", args[3]));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("overview")) {
				if (args.length != 2) {
					sendUsage(player);
					return true;
				}
				
				if (shopHandler.getShop(player.getName()) == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "noshop"));
					return true;
				}
				
				Shop shop = shopHandler.getShop(player.getName());
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_title").replace("[0]", shop.getCuboid().getName()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_name").replace("[0]", shop.getCuboid().getName()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_owner").replace("[0]", shop.getOwner()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_type").replace("[0]", shop.getType().name()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_price").replace("[0]", shop.getPrice() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_area").replace("[0]", shop.getCuboid().getArea() + "")
						.replace("[1]", shop.getCuboid().getSizeX() + "")
						.replace("[2]", shop.getCuboid().getSizeY() + "")
						.replace("[3]", shop.getCuboid().getSizeZ() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_income").replace("[0]", shop.getIncome() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_lastcostumer").replace("[0]", shop.getLastCostumer()));
				
				return  true;
			}
			
			if (!Level.canUse(user, Level.ARCHITECT)) {
				player.sendMessage(lang.getColoredMessage(userLang, "nopermission"));
				return true;
			}
			
			if (args[1].equalsIgnoreCase("create")) {
				
				if (args.length == 4) {
					
					if (shopHandler.existsShop(args[2])) {
						player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_create_alreadyexists"));
						return true;
					}
					
					Cuboid cuboid = CuboidHandler.getInstance().getCuboid(args[2]);
					
					if (cuboid == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "unknowncuboid").replace("[0]", args[2]));
						return true;
					}
					
					if (cuboid.getCuboidType() != CuboidType.Shop.getTypeID()) {
						player.sendMessage(lang.getColoredMessage(userLang, "invalidshoptype"));
						return true;
					}
					
					ShopType type = ShopType.valueOf(args[3]);
					
					shopHandler.registerShop(cuboid, type, player.getName(), false, 0, false, null, 0, false);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_create_success").replace("[0]", args[2]));
					
					return true;
				}
				
				if (args.length == 5) {
					
					if (shopHandler.existsShop(args[2])) {
						player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_create_alreadyexists"));
						return true;
					}
					
					Cuboid cuboid = CuboidHandler.getInstance().getCuboid(args[2]);
					
					if (cuboid == null) {
						player.sendMessage(lang.getColoredMessage(userLang, "unknowncuboid").replace("[0]", args[2]));
						return true;
					}
					
					if (cuboid.getCuboidType() != CuboidType.Shop.getTypeID()) {
						player.sendMessage(lang.getColoredMessage(userLang, "invalidshoptype"));
						return true;
					}
					
					if (!users.exists(args[4])) {
						player.sendMessage(lang.getColoredMessage(userLang, "notfound"));
						return true;
					}
					
					ShopType type = ShopType.valueOf(args[3]);
					
					shopHandler.registerShop(cuboid, type, args[4], false, 0, false, null, 0, false);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_create_success_owner").replace("[0]", args[2]).replace("[1]", args[4]));
					
					return true;
				}
			}
			
			if (args[1].equalsIgnoreCase("recreate")) {
				player.sendMessage(lang.getColoredMessage(userLang, "pluginexception").replace("[0]", "EdgeCuboid"));
				return true;
			}
			
			if (args[1].equalsIgnoreCase("delete")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				Shop shop = shopHandler.getShop(CuboidHandler.getInstance().getCuboid(args[2]));
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				if (!shop.isOwner(player.getName())) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_notowner"));
					return true;
				}
				
				if (player.getInventory().firstEmpty() == -1) {
					player.sendMessage(lang.getColoredMessage(userLang, "shop_getitem_nospace"));
					return true;
				}
				
				for (EdgeItemStack item : shop.getGuiItems().keySet()) {
					if (item != null)
						player.getInventory().addItem(item.toBukkitItemStack());
				}
				
				shopHandler.deleteShop(shopHandler.getShop(CuboidHandler.getInstance().getCuboid(args[2])).getCuboidID());
				player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_delete_success").replace("[0]", args[2]));
				
				return true;					
			}
			
			if (args[1].equalsIgnoreCase("setowner")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				Shop shop = shopHandler.getShop(args[2]);
				User newOwner = EdgeCoreAPI.userAPI().getUser(args[3]);
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				if (newOwner == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "notfound"));
					return true;
				}
				
				if (shop.isOwner(args[3])) {
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setowner_alreadyowner"));
					return true;
				}
				
				if (shopHandler.getShop(args[3]) != null) {
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setowner_alreadyshop"));
					return true;
				}
				
				shop.switchOwner(newOwner);
				player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setowner_success").replace("[0]", args[2]).replace("[1]", args[3]));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("setbuyable")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				if (!args[3].equalsIgnoreCase("false") || !args[3].equalsIgnoreCase("true")) {
					player.sendMessage(lang.getColoredMessage(userLang, "argumentexception"));
					return true;
				}
				
				Shop shop = shopHandler.getShop(args[2]);
				boolean var = Boolean.parseBoolean(args[3]);
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				if (!var) {
					
					shop.setBuyable(false);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setbuyable_false").replace("[0]", args[2]));
					
					return true;
					
				} else {
					
					shop.setBuyable(true);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setbuyable_true").replace("[0]", args[2]));
					
					return true;
					
				}
			}
			
			if (args[1].equalsIgnoreCase("setrental")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				Shop shop = shopHandler.getShop(args[2]);
				double rental = Double.parseDouble(args[3]);
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				if (rental <= 0) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoolow"));
					return true;
				}
				
				if (rental >= shop.getPrice()) {
					player.sendMessage(lang.getColoredMessage(userLang, "amounttoohigh"));
					return true;
				}
				
				shop.updateRental(rental);
				player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setrental_success").replace("[0]", args[2]).replace("[1]", args[3]));
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("setrentable")) {
				if (args.length != 4) {
					sendUsage(player);
					return true;
				}
				
				if (!args[3].equalsIgnoreCase("false") || !args[3].equalsIgnoreCase("true")) {
					player.sendMessage(lang.getColoredMessage(userLang, "argumentexception"));
					return true;
				}
				
				Shop shop = shopHandler.getShop(args[2]);
				boolean var = Boolean.parseBoolean(args[3]);
				
				if (shop == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				if (!var) {
					
					shop.setRentable(false);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setrentable_false").replace("[0]", args[2]));
					
					return true;
					
				} else {
					
					shop.setRentable(true);
					player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_setrentable_true").replace("[0]", args[2]));
					
					return true;
					
				}
			}
			
			if (args[1].equalsIgnoreCase("info")) {
				if (args.length != 3) {
					sendUsage(player);
					return true;
				}
				
				if (shopHandler.getShop(args[2]) == null) {
					player.sendMessage(lang.getColoredMessage(userLang, "unknownshop").replace("[0]", args[2]));
					return true;
				}
				
				Shop shop = shopHandler.getShop(CuboidHandler.getInstance().getCuboid(args[2]));
				
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_title").replace("[0]", shop.getCuboid().getName()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_name").replace("[0]", shop.getCuboid().getName()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_owner").replace("[0]", shop.getOwner()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_type").replace("[0]", shop.getType().name()));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_price").replace("[0]", shop.getPrice() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_area").replace("[0]", shop.getCuboid().getArea() + "")
						.replace("[1]", shop.getCuboid().getSizeX() + "")
						.replace("[2]", shop.getCuboid().getSizeY() + "")
						.replace("[3]", shop.getCuboid().getSizeZ() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_income").replace("[0]", shop.getIncome() + ""));
				player.sendMessage(lang.getColoredMessage(userLang, "shop_info_lastcostumer").replace("[0]", shop.getLastCostumer()));
			}
			
			if (args[1].equalsIgnoreCase("types")) {
				if (args.length != 2) {
					sendUsage(player);
					return true;
				}
				
				StringBuilder sb = new StringBuilder();
				
				for (ShopType type : ShopType.values()) {
					if (sb.length() > 0)
						sb.append(", ");
					
					sb.append(ChatColor.GOLD + type.name());
				}
				
				player.sendMessage(lang.getColoredMessage(userLang, "admin_shop_type_list").replace("[0]", sb.toString()));
				
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
		
		sender.sendMessage(EdgeCore.usageColor + "/shop buy <shop>");
		sender.sendMessage(EdgeCore.usageColor + "/shop rent <shop>");
		sender.sendMessage(EdgeCore.usageColor + "/shop additem §m[<amount>]§c <price>");
		sender.sendMessage(EdgeCore.usageColor + "/shop getitem §m[<amount>]");
		sender.sendMessage(EdgeCore.usageColor + "/shop itemprice <item> <price>");
		sender.sendMessage(EdgeCore.usageColor + "/shop near <distance>");
		sender.sendMessage(EdgeCore.usageColor + "/shop providing <item> <distance>");
		sender.sendMessage(EdgeCore.usageColor + "/shop overview");
		
		User u = EdgeCoreAPI.userAPI().getUser(sender.getName());
		
		if (u == null || !Level.canUse(u, Level.ARCHITECT)) return;
		
		sender.sendMessage(EdgeCore.usageColor + "/shop create <name> <type> [<owner>]");
		sender.sendMessage(EdgeCore.usageColor + "/shop recreate <shop> [<type>]");
		sender.sendMessage(EdgeCore.usageColor + "/shop delete <shop>");
		sender.sendMessage(EdgeCore.usageColor + "/shop setowner <shop> <owner>");
		sender.sendMessage(EdgeCore.usageColor + "/shop setbuyable <shop> <boolean>");
		sender.sendMessage(EdgeCore.usageColor + "/shop setrental <shop> <rental>");
		sender.sendMessage(EdgeCore.usageColor + "/shop setrentable <shop> <boolean>");
		sender.sendMessage(EdgeCore.usageColor + "/shop info <shop>");
		sender.sendMessage(EdgeCore.usageColor + "/shop types");
	}

	@Override
	public boolean sysAccess(CommandSender arg0, String[] arg1) {
		return true;
	}

	@Override
	public boolean validArgsRange(String[] args) {
		return (args.length > 1 && args.length <= 5);
	}
	
	private void addItem(Player sender, User user, Shop shop, ItemStack item, double price) {
		if (sender == null || user == null || shop == null) return;
		
		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "invaliditem"));
			return;
		}
		
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(ChatColor.AQUA + item.getType().name());

		List<String> lore = Arrays.asList(new String[] { ChatColor.BLUE + Economy.getCurrency() + price});
		meta.setLore(lore);
		
		item.setItemMeta(meta);
		
		EdgeItemStack guiItem = new EdgeItemStack(item);
		
		if (shop.getGuiItems().containsKey(guiItem)) {
			sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "shop_additem_alreadyused"));
			return;
		}
		
		shop.addItem(new EdgeItemStack(item), price);
		sender.getInventory().remove(item);
		
		sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "shop_additem_success").replace("[0]", item.getType().name()).replace("[1]", shop.getCuboid().getName()));
	}
	
	private void getItem(Player sender, User user, Shop shop, ItemStack item) {
		if (sender == null || user == null || shop == null) return;
		
		if (item == null || item.getType() == Material.AIR) {
			sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "invaliditem"));
			return;
		}
		
		EdgeItemStack guiItem = new EdgeItemStack(item);
		
		if (!shop.getGuiItems().containsKey(guiItem)) {
			sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "shop_getitem_notused"));
			return;
		}
		
		if (sender.getInventory().firstEmpty() == -1) {
			sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "shop_getitem_nospace"));
			return;
		}
		
		shop.removeItem(new EdgeItemStack(item));		
		sender.getInventory().addItem(item);
		
		sender.sendMessage(lang.getColoredMessage(user.getLanguage(), "shop_getitem_success").replace("[0]", item.getType().name()).replace("[1]", shop.getCuboid().getName()));
	}
}
