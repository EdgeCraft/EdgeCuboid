package net.edgecraft.edgecuboid;

import java.util.logging.Logger;

import net.edgecraft.edgecore.EdgeCore;
import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.command.CommandCollection;
import net.edgecraft.edgecore.command.CommandHandler;
import net.edgecraft.edgecuboid.cuboid.CuboidHandler;
import net.edgecraft.edgecuboid.events.HandleCommandEvents;
import net.edgecraft.edgecuboid.events.HandleCuboidEvents;
import net.edgecraft.edgecuboid.events.HandleCuboidFlags;
import net.edgecraft.edgecuboid.other.ConfigHandler;
import net.edgecraft.edgecuboid.other.CuboidCommands;
import net.edgecraft.edgecuboid.other.CuboidSynchronizationTask;
import net.edgecraft.edgecuboid.other.EventTask;
import net.edgecraft.edgecuboid.shop.HandleShopEvents;
import net.edgecraft.edgecuboid.shop.ShopHandler;
import net.edgecraft.edgecuboid.world.HandleWorldEvents;
import net.edgecraft.edgecuboid.world.WorldManager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class EdgeCuboid extends JavaPlugin {
	
	public static final String cuboidbanner = "[EdgeCuboid] ";
	
	public static final Logger log = EdgeCore.log;
	private static EdgeCuboid instance;
	
	private static final CuboidHandler cuboidAPI = CuboidHandler.getInstance();
	private static final WorldManager worldAPI = WorldManager.getInstance();
	private static final ShopHandler shopAPI = ShopHandler.getInstance();
	private final CommandHandler commands = EdgeCoreAPI.commandsAPI();
	private final ConfigHandler config = ConfigHandler.getInstance(this);
	
	private static boolean eventTaskReady = true;
	
	/**
	 * Is used when the plugin is going to shut down
	 */
	public void onDisable() {
		cuboidAPI.synchronizeCuboidManagement(true, true);
		shopAPI.synchronizeShops();
		
		log.info(cuboidbanner + "Plugin wurde erfolgreich beendet!");
	}
	
	/**
	 * Is used when the plugin starts up
	 */
	public void onEnable() {
		registerData();
		
		log.info(cuboidbanner + "Plugin wurde erfolgreich gestartet!");
	}
	
	/**
	 * Is used before onEnable(), e.g. to pre-load needed functions
	 */
	public void onLoad() {
		instance = this;
		
		this.config.loadConfig();
		this.config.update();
	}
	
	/**
	 * Registers data the plugin will use
	 */
	private void registerData() {
		getServer().getPluginManager().registerEvents(new HandleCuboidEvents(), this);
		getServer().getPluginManager().registerEvents(new HandleWorldEvents(), this);
		getServer().getPluginManager().registerEvents(new HandleCuboidFlags(), this);
		getServer().getPluginManager().registerEvents(new HandleCommandEvents(), this);
		getServer().getPluginManager().registerEvents(new HandleShopEvents(), this);
		
		commands.registerCommand( new CommandCollection(CuboidCommands.getInstance()) );
		
		@SuppressWarnings("unused") BukkitTask eventTask = new EventTask().runTaskTimer(this, 0L, 20L);
		@SuppressWarnings("unused") BukkitTask syncTask = new CuboidSynchronizationTask().runTaskTimer(this, 0, 20L * 60 * 10);
	}
	
	/**
	 * Returns an instance of this class
	 * @return EdgeCuboid
	 */
	public static EdgeCuboid getInstance() {
		return instance;
	}
	
	/**
	 * Returns an instance of the CuboidAPI
	 * @return CuboidHandler
	 */
	public static CuboidHandler getCuboidAPI() {
		return cuboidAPI;
	}
	
	/**
	 * Returns an instance of the WorldAPI
	 * @return WorldManager
	 */
	public static WorldManager getWorldAPI() {
		return worldAPI;
	}
	
	/**
	 * Returns an instance of the ShopAPI
	 * @return ShopHandler
	 */
	public static ShopHandler getShopAPI() {
		return shopAPI;
	}
	
	/**
	 * Returns if the event task is ready to go
	 * @return true/false
	 */
	public static boolean isEventTaskReady() {
		return eventTaskReady;
	}
	
	/**
	 * Sets the cancellation of the event task
	 * @param var
	 */
	public static void setEventTask(boolean var) {
		eventTaskReady = var;
	}
}
