package net.edgecraft.edgecuboid.cuboid;

import java.sql.Blob;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.rowset.serial.SerialBlob;

import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.db.DatabaseHandler;
import net.edgecraft.edgecuboid.EdgeCuboid;
import net.edgecraft.edgecuboid.cuboid.types.CuboidType;
import net.edgecraft.edgecuboid.cuboid.types.HabitatType;

import org.bukkit.Location;

import com.google.common.collect.Lists;

public class CuboidHandler {
	
	public static String cuboidTable = "edgecuboid_cuboids";
	public static String habitatTable = "edgecuboid_habitats";
	
	private HashMap<Integer, Cuboid> cuboids = new LinkedHashMap<>();
	private HashMap<Integer, Habitat> habitats = new LinkedHashMap<>();
	private Map<String, Cuboid> creatingPlayers = new HashMap<>();
	private List<String> searchingPlayers = new ArrayList<>();
	
	private static final CuboidHandler instance = new CuboidHandler();
	private final DatabaseHandler db = EdgeCoreAPI.databaseAPI();
	
	protected CuboidHandler() { /* ... */ }
	
	/**
	 * Returns a static instance of this class
	 * @return CuboidHandler
	 */
	public static CuboidHandler getInstance() {
		return instance;
	}
	
	/**
	 * Returns a HashMap containing all stored cuboids
	 * @return HashMap<Integer, Cuboid>
	 */
	public HashMap<Integer, Cuboid> getCuboids() {
		return cuboids;
	}
	
	/**
	 * Returns ???? @lush
	 * @param type
	 * @param use
	 * @return Cuboid
	 */
	public Cuboid getCuboid(CuboidType type, boolean use) {
		
		List<Cuboid> shuffled = Lists.newArrayList(cuboids.values());
		Collections.shuffle(shuffled);
		
		for (Cuboid cuboid : shuffled) {
			
			if (use && cuboid.getCuboidType() == type.getTypeID())
				return cuboid;
			
			if (!use && cuboid.getCuboidType() != type.getTypeID())
				return cuboid;
			
		}
		
		return null;
	}
	
	/**
	 * Returns the nearest cuboid to the given location of the given type
	 * @param type
	 * @param loc
	 * @return Cuboid
	 */
	public Cuboid getNearestCuboid(CuboidType type, Location loc) {
		if (type == null || loc == null) return null;
		
		double nearest = 0;
		Cuboid cuboid = null;
		
		for (Cuboid c : cuboids.values()) {
			if (c.getCuboidType() != type.getTypeID()) continue;
			if (nearest == 0) {
				nearest = loc.distanceSquared(c.getCenter());
				cuboid = c;
			}
			
			if (loc.distanceSquared(c.getCenter()) < nearest) {
				nearest = loc.distanceSquared(c.getCenter());
				cuboid = c;
			}
		}
		
		return cuboid;
	}
	
	/**
	 * Returns a HashMap containing all stored habitats
	 * @return HashMap<Integer, Habitat>
	 */
	public HashMap<Integer, Habitat> getHabitats() {
		return habitats;
	}
	
	/**
	 * Returns a List containing all currently creating players
	 * @return List<String>
	 */
	public Map<String, Cuboid> getCreatingPlayers() {
		return creatingPlayers;
	}
	
	/**
	 * Returns a List containg all currently searching players
	 * @return List<String>
	 */
	public List<String> getSearchingPlayers() {
		return searchingPlayers;
	}
	
	/**
	 * Checks if the given player is creating anything
	 * @param player
	 * @return true/false
	 */
	public boolean isCreating(String player) {
		return getCreatingPlayers().containsKey(player);
	}
	
	/**
	 * Checks if the given player is searching anything
	 * @param player
	 * @return true/false
	 */
	public boolean isSearching(String player) {
		return getSearchingPlayers().contains(player);
	}
	
	/**
	 * Returns the amount of existing (non-habitat) cuboids
	 * @return Integer
	 */
	public int amountOfCuboids() {
		return cuboids.size() - habitats.size();
	}
	
	/**
	 * Returns the amount of existing habitats
	 * @return Integer
	 */
	public int amountOfHabitats() {
		return habitats.size();
	}
	
	/**
	 * Returns the amount of currently inhabited habitats
	 * @return Integer
	 */
	public int percentageOfInhabitedHabitats() {
		int amount = 0;
		
		for (Habitat h : habitats.values()) {
			if (h != null) {
				if (h.isInhabited()) amount++;
			}
		}
		
		return amount;
	}
	
	/**
	 * Returns the percentage of green area in the world
	 * @return Integer
	 */
	public int percentageOfGreenArea() {
		int amount = 0;
		
		for (Cuboid c : cuboids.values()) {
			if (c != null)
				if (CuboidType.getType(c.getCuboidType()) == CuboidType.Park || CuboidType.getType(c.getCuboidType()) == CuboidType.Sight) amount++;
		}
		
		return (int) ((double) amount / amountOfCuboids() * 100);
	}
	
	/**
	 * Returns the percentage of dirty area in the world
	 * @return Integer
	 */
	public int percentageOfDirtyArea() {
		int amount = 0;
		
		for (Cuboid c : cuboids.values()) {
			if (c != null)
				if (CuboidType.getType(c.getCuboidType()) == CuboidType.Street || CuboidType.getType(c.getCuboidType()) == CuboidType.Rail) amount++;
		}
		
		return (int) ((double) amount / amountOfCuboids() * 100);
	}
	
	/**
	 * Returns the percentage of public area in the world
	 * @return
	 */
	public int percentageOfPublicAreas() {
		int amount = percentageOfGreenArea();
		
		for (Cuboid c : cuboids.values()) {
			if (c != null)
				if (CuboidType.getType(c.getCuboidType()) == CuboidType.Public || 
				CuboidType.getType(c.getCuboidType()) == CuboidType.Hospital || 
				CuboidType.getType(c.getCuboidType()) == CuboidType.PoliceStation ||
				CuboidType.getType(c.getCuboidType()) == CuboidType.FireStation) amount++;
		}
		
		return (int) ((double) amount / amountOfCuboids() * 100);
	}
	
	/**
	 * Registers a new cuboid
	 * @param name
	 * @param owner
	 * @param type
	 * @param min
	 * @param max
	 * @param enterMsg
	 * @param leaveMsg
	 * @param events
	 * @param flags
	 * @throws Exception 
	 */
	public void registerCuboid(String name, int owner, CuboidType type, Level modifyLevel, Location spawn, Location min, Location max, String enterMsg, String leaveMsg, 
			List<String> participants, List<CuboidEvent> events, HashMap<Flag, List<String>> flags, List<String> commands) throws Exception {
		
		if (existsCuboid(name)) return;
		
		Cuboid c = new Cuboid(name, generateID(), owner, type, modifyLevel, spawn, min, max, min.getWorld(), enterMsg, leaveMsg, participants, events, flags, commands);
		registerCuboid(c);
	}
	
	private void registerCuboid(Cuboid cuboid) {
		try {
			
			byte[] cuboidByteArray = cuboid.toByteArray();
			
			Blob blob = null;
			blob = new SerialBlob(cuboidByteArray);
			
			PreparedStatement registerCuboid = db.prepareUpdate("INSERT INTO " + CuboidHandler.cuboidTable + " (id, cuboid) VALUES (?, ?);");
			registerCuboid.setInt(1, cuboid.getID());
			registerCuboid.setBlob(2, blob);
			registerCuboid.executeUpdate();
			
			synchronizeCuboid(cuboid.getID());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes an existing cuboid
	 * @param id
	 */
	public void deleteCuboid(int id) {
		if (id <= 0) return;
		if (!existsCuboid(id)) return;
		
		try {
			
			PreparedStatement deleteCuboid = db.prepareUpdate("DELETE FROM " + CuboidHandler.cuboidTable + " WHERE id = '" + id + "';");
			deleteCuboid.executeUpdate();
			
			getCuboids().remove(id);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Registers a new habitat
	 * @param c
	 * @param type
	 * @param owner
	 * @param tenant
	 * @param worth
	 * @param buyable
	 * @param rental
	 * @param rentable
	 */
	public void registerHabitat(Cuboid c, HabitatType type, String owner, String tenant, double worth, boolean buyable, double rental, boolean rentable, Map<Upgrade, Integer> upgrades) {
		if (existsHabitat(c.getID()) || existsHabitat(c.getName()) || getHabitatByOwner(owner) != null || getHabitatByTenant(tenant) != null) return;
		
		try {
			
			Habitat h = new Habitat(c, type, owner, tenant, worth, buyable, rental, rentable, upgrades);
			byte[] habitatByteArray = h.toByteArray();
			Blob blob = new SerialBlob(habitatByteArray);
			
			PreparedStatement registerHabitat = db.prepareUpdate("INSERT INTO " + CuboidHandler.habitatTable + " (cuboidid, habitat) VALUES (?, ?);");
			registerHabitat.setInt(1, c.getID());
			registerHabitat.setBlob(2, blob);
			registerHabitat.executeUpdate();
			
			synchronizeHabitat(h.getCuboidID());
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Deletes an existing habitat
	 * @param id
	 */
	public void deleteHabitat(int id) {
		if (id <= 0) return;
		if (!existsHabitat(id)) return;
		
		try {
		 	
			PreparedStatement deleteHabitat = db.prepareUpdate("DELETE FROM " + CuboidHandler.habitatTable + " WHERE cuboidid = '" + id + "';");
			deleteHabitat.executeUpdate();
			
			getHabitats().remove(id);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates a new ID
	 * @return Integer
	 * @throws Exception
	 */
	public int generateID() throws Exception {
		if (amountOfCuboids() <= 0) return 1;
		
		return greatestID() + 1;
	}
	
	/**
	 * Returns the greatest ID of the cuboid database table
	 * @return Integer
	 * @throws Exception
	 */
	public int greatestID() throws Exception {
		List<Map<String, Object>> tempVar = db.getResults("SELECT COUNT(id) AS amount FROM " + CuboidHandler.cuboidTable);
		int tempID = Integer.parseInt(String.valueOf(tempVar.get(0).get("amount")));
		
		if (tempID <= 0) return 1;

		return tempID;
	}
	
	/**
	 * Checks if the given cuboid exists
	 * @param c
	 * @return true/false
	 */
	public boolean existsCuboid(Cuboid c) {
		return getCuboids().containsValue(c);
	}
	
	/**
	 * Checks if the given cuboid exists
	 * @param id
	 * @return true/false
	 */
	public boolean existsCuboid(int id) {
		return getCuboids().containsKey(id);
	}
	
	/**
	 * Checks if the given cuboid exists
	 * @param name
	 * @return true/false
	 */
	public boolean existsCuboid(String name) {
		for (Cuboid c : getCuboids().values()) {
			if (c != null) 
				return c.getName().equalsIgnoreCase(name);
		}
		
		return false;
	}
	
	/**
	 * Checks if the given habitat exists
	 * @param h
	 * @return true/false
	 */
	public boolean existsHabitat(Habitat h) {
		return getHabitats().containsValue(h);
	}
	
	/**
	 * Checks if the given habitat exists
	 * @param id
	 * @return true/false
	 */
	public boolean existsHabitat(int id) {
		return getHabitats().containsKey(id);
	}
	
	/**
	 * Checks if the given habitat exists
	 * @param name
	 * @return true/false
	 */
	public boolean existsHabitat(String name) {
		for (Habitat h : getHabitats().values()) {
			if (h.getCuboid().getName().equals(name)) return true;
		}
		
		return false;
	}
	
	/**
	 * Returns the given cuboid
	 * @param id
	 * @return Cuboid
	 */
	public Cuboid getCuboid(int id) {
		return getCuboids().get(id);
	}
	
	/**
	 * Returns the given cuboid
	 * @param name
	 * @return Cuboid
	 */
	public Cuboid getCuboid(String name) {
		for (Cuboid c : getCuboids().values()) {
			if (c != null)
				if (c.getName().equalsIgnoreCase(name)) return c;
		}
		
		return null;
	}
	
	/**
	 * Returns the given habitat
	 * @param id
	 * @return Habitat
	 */
	public Habitat getHabitat(int id) {
		return getHabitats().get(id);
	}
	
	/**
	 * Returns the given habitat
	 * @param name
	 * @return Habitat
	 */
	public Habitat getHabitat(String name) {
		for (Habitat h : getHabitats().values()) {
			if (h != null)
				if (h.getCuboid().getName().equalsIgnoreCase(name)) return h;
		}
		
		return null;
	}
	
	/**
	 * Returns the habitat connected with the given owner
	 * @param owner
	 * @return Habitat
	 */
	public Habitat getHabitatByOwner(String owner) {
		for (Habitat h : getHabitats().values()) {
			if (h != null)
				if (h.getOwner().equalsIgnoreCase(owner)) return h;
		}
		
		return null;
	}
	
	/**
	 * Returns the habitat connected with the given tenant
	 * @param tenant
	 * @return Habitat
	 */
	public Habitat getHabitatByTenant(String tenant) {
		for (Habitat h : getHabitats().values()) {
			if (h != null)
				if (h.getTenant().equalsIgnoreCase(tenant)) return h;
		}
		
		return null;
	}
	
	/**
	 * Synchronizes the complete cuboid management
	 * Synchronized data can be turned on/off
	 * @param cuboids
	 * @param habitats
	 */
	public void synchronizeCuboidManagement(boolean cuboids, boolean habitats) {
		try {
			
			if (cuboids)
				for (int i = 1; i <= greatestID(); i++) {
					synchronizeCuboid(i);
				}
			
			if (habitats)
				for (int i = 1; i <= greatestID(); i++) {
					synchronizeHabitat(i);
				}
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Synchronizes the given cuboid
	 * @param id
	 */
	public synchronized void synchronizeCuboid(int id) {
		try {
			
			if (existsCuboid(id)) {
				
				byte[] array = getCuboid(id).toByteArray();
				
				PreparedStatement sync = db.prepareUpdate("UPDATE " + CuboidHandler.cuboidTable + " SET cuboid = ? WHERE id = '" + id + "';");
				Blob blob = new SerialBlob(array);
				
				sync.setBlob(1, blob);
				sync.executeUpdate();
				
				Cuboid synced = Cuboid.toCuboid(getCuboid(id).toByteArray());
				getCuboids().put(id, synced);
				
				return;
			}
			
			List<Map<String, Object>> results = db.getResults("SELECT * FROM " + CuboidHandler.cuboidTable + " WHERE id = '" + id + "';");
			
			if (results.isEmpty()) {
				EdgeCuboid.log.info(EdgeCuboid.cuboidbanner + "No Synchronizable Cuboid Entries Found! Cancelling synchronization..");
				return;
			}
			
			byte[] byteToCuboid = (byte[]) results.get(0).get("cuboid");
			
			Cuboid cuboid = Cuboid.toCuboid(byteToCuboid);			
			getCuboids().put(cuboid.getID(), cuboid);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Synchronizes the given habitat
	 * @param id
	 */
	public synchronized void synchronizeHabitat(int id) {
		try {
			
			if (existsHabitat(id)) {
				byte[] array = getHabitat(id).toByteArray();
				
				PreparedStatement sync = db.prepareUpdate("UPDATE " + CuboidHandler.habitatTable + " SET habitat = ? WHERE cuboidid = '" + id + "';");
				Blob blob = new SerialBlob(array);
				
				sync.setBlob(1, blob);
				sync.executeUpdate();
				
				Habitat synced = Habitat.toHabitat(getHabitat(id).toByteArray());
				getHabitats().put(id, synced);
				
				return;
			}
			
			List<Map<String, Object>> results = db.getResults("SELECT * FROM " + CuboidHandler.habitatTable + " WHERE cuboidid = '" + id + "';");
			
			if (results.isEmpty()) {
				EdgeCuboid.log.info(EdgeCuboid.cuboidbanner + "No Synchronizable Habitat Entries Found! Cancelling synchronization..");
				return;
			}
			
			byte[] byteToHabitat = (byte[]) results.get(0).get("habitat");
			
			Habitat habitat = Habitat.toHabitat(byteToHabitat);			
			getHabitats().put(habitat.getCuboidID(), habitat);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
