package net.edgecraft.edgecuboid.cuboid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.edgecraft.edgecore.EdgeCoreAPI;
import net.edgecraft.edgecore.command.AbstractCommand;
import net.edgecraft.edgecore.command.CommandHandler;
import net.edgecraft.edgecore.command.Level;
import net.edgecraft.edgecore.user.User;
import net.edgecraft.edgecuboid.cuboid.types.CuboidType;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;

public class Cuboid implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String name;
	private int id;
	private int ownerID;
	private int cuboidType;
	private Level modifyLevel;
	
	private List<String> participants;
	private List<CuboidEvent> events;
	private HashMap<Flag, List<String>> flags;
	private List<String> commands;
	
	private Location spawn;
	private Location center;
	private Location minLocation;
	private Location maxLocation;
	private String world;
	
	private int area;
	private int volume;
	
	private String enterMessage;
	private String leaveMessage;
	
	public Cuboid() { /* ... */ }
	
	protected Cuboid(String name, int id, int ownerID, CuboidType cuboidType, Level modifyLevel,
					Location spawn, Location minLocation, Location maxLocation, World world, String enterMessage, String leaveMessage,
					List<String> participants, List<CuboidEvent> events, HashMap<Flag, List<String>> flags, List<String> commands) {
		
		setName(name);
		setID(id);
		setOwnerID(ownerID);
		setCuboidType(cuboidType);
		setModifyLevel(null);
		
		setSpawnLocation(spawn);
		setMinLocation(minLocation);
		setMaxLocation(maxLocation);
		setWorld(world);
		calculateCenter();
		calculateArea();
		calculateVolume();
		
		setEnterMessage(enterMessage);
		setLeaveMessage(leaveMessage);
		
		setParticipants(participants);
		setEvents(events);
		getEvents().addAll(cuboidType.getEventList());
		setFlags(flags);
		setCommands(commands);
		
	}
	
	/**
	 * Turns a byte[] back to a cuboid instance
	 * @param byteArray
	 * @return Cuboid
	 */
	public static Cuboid toCuboid(byte[] byteArray) {
		try {
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
			
			@SuppressWarnings("unchecked")
			Map<String, Object> infoMap = (Map<String, Object>) objectInputStream.readObject();
			
			Cuboid cuboid = new Cuboid();
			cuboid.deserialize(infoMap);
			
			return cuboid;
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Writes this cuboid instance into a byte[]
	 * @return byte[]
	 */
	public byte[] toByteArray() {
		try {
			
			ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
			objectOutputStream.writeObject(this.serialize());
			
			return byteArrayOutputStream.toByteArray();
			
		} catch (Exception exc) {
			exc.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Serializes the current cuboid instance
	 * @return Map<String, Object>
	 */
	private Map<String, Object> serialize() {
		Map<String, Object> infoMap = new LinkedHashMap<String, Object>();		
		infoMap.put("object-type", "Cuboid");
		
		// Put information
		infoMap.put("id", id);
		infoMap.put("name", name);
		infoMap.put("owner-id", ownerID);
		infoMap.put("cuboid-type", getCuboidType());
		infoMap.put("modifylevel", getModifyLevel().value());
		
		// Put location information
		infoMap.put("spawn-x", getSpawn().getBlockX());
		infoMap.put("spawn-y", getSpawn().getBlockY());
		infoMap.put("spawn-z", getSpawn().getBlockZ());
		infoMap.put("spawn-yaw", getSpawn().getYaw());
		infoMap.put("spawn-pitch", getSpawn().getPitch());
		infoMap.put("minlocation-x", getMinLocation().getBlockX());
		infoMap.put("minlocation-y", getMinLocation().getBlockY());
		infoMap.put("minlocation-z", getMinLocation().getBlockZ());
		infoMap.put("maxlocation-x", getMaxLocation().getBlockX());
		infoMap.put("maxlocation-y", getMaxLocation().getBlockY());
		infoMap.put("maxlocation-z", getMaxLocation().getBlockZ());
		infoMap.put("world", world);
		
		// Put cuboid specific information
		infoMap.put("participants", participants);
		infoMap.put("entermessage", enterMessage);
		infoMap.put("leavemessage", leaveMessage);
		
		infoMap.put("events", events);
		infoMap.put("flags", flags);
		infoMap.put("commands", commands);
		
		
		return infoMap;
	}
	
	/**
	 * Deserializes the given cuboid serialization
	 * @param infoMap
	 */
	@SuppressWarnings("unchecked")
	private void deserialize(Map<String, Object> infoMap) {		
		if (!infoMap.containsKey("object-type") || !infoMap.get("object-type").equals("Cuboid")) throw new java.util.UnknownFormatFlagsException("No Cuboid");
		
		setID((int) infoMap.get("id"));
		setName((String) infoMap.get("name"));
		setOwnerID((int) infoMap.get("owner-id"));
		setCuboidType(CuboidType.getType((int) infoMap.get("cuboid-type")));
		setModifyLevel(Level.getInstance((int) infoMap.get("modifylevel")));
		
		Location spawn = new Location(((World) Bukkit.getWorld((String) infoMap.get("world"))), 
										(int) infoMap.get("spawn-x"), (int) infoMap.get("spawn-y"), (int) infoMap.get("spawn-z"), 
										(float) infoMap.get("spawn-yaw"), (float) infoMap.get("spawn-pitch"));
		
		Location min = new Location(((World) Bukkit.getWorld((String) infoMap.get("world"))), (int) infoMap.get("minlocation-x"), (int) infoMap.get("minlocation-y"), (int) infoMap.get("minlocation-z"));
		Location max = new Location(((World) Bukkit.getWorld((String) infoMap.get("world"))), (int) infoMap.get("maxlocation-x"), (int) infoMap.get("maxlocation-y"), (int) infoMap.get("maxlocation-z"));
		
		setSpawnLocation(spawn);
		setMinLocation(min);
		setMaxLocation(max);
		setWorld((World) Bukkit.getWorld((String) infoMap.get("world")));
		calculateCenter();
		calculateArea();
		calculateVolume();

		setParticipants((List<String>) infoMap.get("participants"));		
		setEvents((List<CuboidEvent>) infoMap.get("events"));
		setFlags((HashMap<Flag, List<String>>) infoMap.get("flags"));
		setCommands((List<String>) infoMap.get("commands"));
	}
	
	/**
	 * Returns the cuboids' name
	 * @return String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the cuboids' ID
	 * @return Integer
	 */
	public int getID() {
		return id;
	}
	
	/**
	 * Returns the cuboids' owner ID
	 * @return Integer
	 */
	public int getOwnerID() {
		return ownerID;
	}
	
	/**
	 * Returns the cuboids' owner
	 * @return Integer
	 */
	public User getOwner() {
		return EdgeCoreAPI.userAPI().getUser(getOwnerID());
	}
	
	/**
	 * Checks if the given user is the owner
	 * @param user
	 * @return true/false
	 */
	public boolean isOwner(User user) {
		return user.getID() == getOwnerID();
	}
	
	/**
	 * Returns the cuboids' type
	 * @return Integer
	 */
	public int getCuboidType() {
		return cuboidType;
	}
	
	/**
	 * Returns the level which is at least needed to modify this cuboid
	 * @return Level
	 */
	public Level getModifyLevel() {
		return modifyLevel;
	}
	
	/**
	 * Returns a list of all participants of this cuboid
	 * @return List<String>
	 */
	public List<String> getParticipants() {
		return participants;
	}
	
	/**
	 * Checks if the given player is a participant
	 * @param player
	 * @return true/false
	 */
	public boolean isParticipant(String player) {
		return getParticipants().contains(player);
	}
	
	/**
	 * Returns a list of all enabled events of this cuboid
	 * @return List<CuboidEvent>
	 */
	public List<CuboidEvent> getEvents() {
		return events;
	}
	
	/**
	 * Returns the list of all enabled events as a String
	 * @return String
	 */
	public String getEventList() {
		StringBuilder sb = new StringBuilder();
		
		for (CuboidEvent e : CuboidEvent.values()) {
			if (sb.length() > 0) {
				sb.append(ChatColor.RED);
				sb.append(",");
			}
			
			sb.append(hasEvent (e) ? ChatColor.GREEN : ChatColor.RED);
			sb.append(e.name());
		}
		
		return sb.toString();
	}
	
	/**
	 * Checks if the given event is enabled
	 * @param event
	 * @return true/false
	 */
	public boolean hasEvent(CuboidEvent event) {
		return CuboidEvent.hasEvent(this, event);
	}
	
	/**
	 * Returns a HashMap<Flag, List<String>> of all flags
	 * @return HashMap<Flag, List<String>>
	 */
	public HashMap<Flag, List<String>> getFlags() {
		return this.flags;
	}
	
	/**
	 * Returns a list of all allowed commands of this cuboid
	 * @return List<String>
	 */
	public List<String> getAllowedCommands() {
		return this.commands;
	}
	
	/**
	 * Returns the list of all allowed commands as a String
	 * @return String
	 */
	public String getCommands() {
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0; i < getAllowedCommands().size(); i++) {
			if (sb.length() > 0)
				sb.append(",");
			
			sb.append(getAllowedCommands().get(i));
		}
		
		return sb.toString();
	}
	
	/**
	 * Allows the given command
	 * @param cmd
	 */
	public void allowCommand(String cmd) {
		if (cmd == null || getAllowedCommands().contains(cmd)) return;
		
		getAllowedCommands().add(cmd);
	}
	
	/**
	 * Allows the given command
	 * @param cmd
	 */
	public void allowCommand(AbstractCommand cmd) {
		if (cmd == null || getAllowedCommands().contains(cmd.getName())) return;
		
		getAllowedCommands().add(cmd.getName());
	}
	
	/**
	 * Disallows the given command
	 * @param cmd
	 */
	public void disallowCommand(String cmd) {
		if (cmd == null || !getAllowedCommands().contains(cmd)) return;
		
		getAllowedCommands().remove(cmd);
	}
	
	/**
	 * Disallows the given command
	 * @param cmd
	 */
	public void disallowCommand(AbstractCommand cmd) {
		if (cmd == null || !getAllowedCommands().contains(cmd.getName())) return;
		
		getAllowedCommands().remove(cmd.getName());
	}
	
	/**
	 * Checks if the given command is allowed
	 * @param cmd
	 * @return true/false
	 */
	public boolean isCommandAllowed(String cmd) {
		return getAllowedCommands().contains(cmd);
	}
	
	/**
	 * Checks if the given command is allowed
	 * @param cmd
	 * @return true/false
	 */
	public boolean isCommandAllowed(AbstractCommand cmd) {
		return getAllowedCommands().contains(cmd.getName());
	}
	
	/**
	 * Returns the location where teleported players will spawn at
	 * @return Location
	 */
	public Location getSpawn() {
		return spawn == null ? Bukkit.getWorlds().get(0).getSpawnLocation() : spawn;
	}
	
	/**
	 * Returns the location of the cuboids' center
	 * @return SerializableLocation
	 */
	public Location getCenter() {
		calculateCenter();
		return center;
	}
	
	/**
	 * Recalculates the center location of the cuboid
	 */
	public void calculateCenter() {
		int x1 = getMaxLocation().getBlockX() + 1;
		int y1 = getSizeY();
		int z1 = getMaxLocation().getBlockZ() + 1;
		Location center = new Location(getMinLocation().getWorld(), getMinLocation().getBlockX() + (x1 - getMinLocation().getBlockX()) / 2.0D,
																	getMinLocation().getBlockY() + (y1 - getMinLocation().getBlockY()) / 2.0D,
																	getMinLocation().getBlockZ() + (z1 - getMinLocation().getBlockZ()) / 2.0D);
		
		setCenter(center);
	}
	
	/**
	 * Checks if the given location is inside the cuboid
	 * @param loc
	 * @return true/false
	 */
	public boolean isInside(Location loc) {
		if (!loc.getWorld().getName().equals(getWorld())) return false;
		
		int minX = Math.min(minLocation.getBlockX(), maxLocation.getBlockX());
		int minY = Math.min(minLocation.getBlockY(), maxLocation.getBlockY());
		int minZ = Math.min(minLocation.getBlockZ(), maxLocation.getBlockZ());
		int maxX = Math.max(minLocation.getBlockX(), maxLocation.getBlockX());
		int maxY = Math.max(minLocation.getBlockY(), maxLocation.getBlockY());
		int maxZ = Math.max(minLocation.getBlockZ(), maxLocation.getBlockZ());
		
		if ((loc.getBlockX() >= minX) && (loc.getBlockX() <= maxX)
			&& (loc.getBlockY() >= minY) && (loc.getBlockY() <= maxY)
			&& (loc.getBlockZ() >= minZ) && (loc.getBlockZ() <= maxZ)) {
			
			return true;
		}
		
		return false;
	}

	/**
	 * Returns the cuboid the given player is currently in
	 * @param p
	 * @return Cuboid
	 */
	public static Cuboid getCuboid(Location loc) {		
		for (Cuboid c : CuboidHandler.getInstance().getCuboids().values()) {
			
			if (c.isInside(loc)) {
				return c;
			}
		}
		
		return null;
	}
	
	/**
	 * Returns the cuboids' 'minLocation'
	 * @return SerializableLocation
	 */
	public Location getMinLocation() {
		return minLocation;
	}

	/**
	 * Returns the cuboids' 'maxLocation'
	 * @return SerializableLocation
	 */
	public Location getMaxLocation() {
		return maxLocation;
	}
	
	/**
	 * Returns the world the cuboid is located in
	 * @return String
	 */
	public String getWorld() {
		return world;
	}
	
	/**
	 * Returns the world instance of this cuboid
	 * @return World
	 */
	public World getWorldInstance() {
		return Bukkit.getWorld(getWorld());
	}
	
	/**
	 * Returns the calculated size of the X-Axis
	 * @return Integer
	 */
	public int getSizeX() {
		return getMinLocation().getBlockX() - getMaxLocation().getBlockX() + 1;
	}
	
	/**
	 * Returns the calculated size of the Y-Axis
	 * @return Integer
	 */
	public int getSizeY() {
		return getMinLocation().getBlockY() - getMaxLocation().getBlockY() + 1;
	}
	
	/**
	 * Returns the calculated size of the Z-Axis
	 * @return Integer
	 */
	public int getSizeZ() {
		return getMinLocation().getBlockZ() - getMaxLocation().getBlockZ() + 1;
	}

	/**
	 * Returns the size of the cuboids' 2D-Area (Length * Width)
	 * @return Integer
	 */
	public int getArea() {
		return area;
	}
	
	/**
	 * Recalculates the 2D-Area of the cuboid using X*Z
	 */
	public void calculateArea() {
		setArea(getSizeX() * getSizeZ());
	}

	/**
	 * Returns the cuboids' volume (Length * Width * Height)
	 * @return Integer
	 */
	public int getVolume() {
		return volume;
	}
	
	/**
	 * Recalculates the cuboids' volume using X*Y*Z
	 */
	public void calculateVolume() {
		setVolume(getSizeX() * getSizeY() * getSizeZ());
	}

	/**
	 * Returns the message a player will receive while entering the cuboid
	 * @return String
	 */
	public String getEnterMessage() {
		return enterMessage;
	}

	/**
	 * Returns the message a player will receive while leaving the cuboid
	 * @return String
	 */
	public String getLeaveMessage() {
		return leaveMessage;
	}
	
	/**
	 * Sets the cuboids' name
	 * @param name
	 */
	public void setName(String name) {
		if (name != null)
			this.name = name;
	}

	/**
	 * Sets the cuboids' ID
	 * @param id
	 */
	protected void setID(int id) {
		if (id >= 0)
			this.id = id;
	}
	
	/**
	 * Switches the cuboids' owner
	 * @param newOwner
	 */
	public void switchOwner(int newOwner) {
		setOwnerID(newOwner);
	}
	
	/**
	 * Sets the cuboid owners' id
	 * @param ownerID
	 */
	protected void setOwnerID(int ownerID) {
		if (ownerID > 0)
			this.ownerID = ownerID;
	}
	
	/**
	 * Updates the CuboidType
	 * @param type
	 */
	public void updateCuboidType(CuboidType type) {
		setCuboidType(type);
	}
	
	/**
	 * Sets the cuboids' type
	 * @param cuboidType
	 */
	protected void setCuboidType(CuboidType cuboidType) {
		if (cuboidType != null) {
			
			if (getEvents() == null) events = new ArrayList<CuboidEvent>();
			
			for (CuboidEvent event : CuboidType.getType(getCuboidType()).getEventList()) {
				if (CuboidEvent.hasEvent(this, event)) CuboidEvent.disableEvent(this, event);
			}
			
			this.cuboidType = cuboidType.getTypeID();
			
			for (CuboidEvent event : CuboidType.getType(getCuboidType()).getEventList()) {
				if (!CuboidEvent.hasEvent(this, event)) CuboidEvent.enableEvent(this, event);
			}
		}
	}
	
	/**
	 * Updates the modify level
	 * @param level
	 */
	public void updateModifyLevel(Level level) {
		setModifyLevel(level);
	}
	
	/**
	 * Sets the modify level
	 * -> null sets it to the owners user level
	 * @param level
	 */
	protected void setModifyLevel(Level level) {
		if (level == null)
			this.modifyLevel = Level.USER;
		else
			this.modifyLevel = level;
	}
	
	/**
	 * Sets the cuboids' participants
	 * @param participants
	 */
	protected void setParticipants(List<String> participants) {
		if (participants == null)
			this.participants = new ArrayList<String>();
		else
			this.participants = participants;
	}
	
	/**
	 * Sets the cuboids' events
	 * @param events
	 */
	protected void setEvents(List<CuboidEvent> events) {
		if (events == null)
			this.events = new ArrayList<CuboidEvent>();
		else
			this.events = events;
	}
	
	/**
	 * Sets the cuboids' flags
	 * @param flags
	 */
	protected void setFlags(HashMap<Flag, List<String>> flags) {
		if (flags == null)
			this.flags = new HashMap<Flag, List<String>>();
		else
			this.flags = flags;
	}
	
	/**
	 * Sets the cuboids' allowed commands
	 * @param cmds
	 */
	protected void setCommands(List<String> cmds) {
		if (cmds == null) {
			if (commands == null)
				commands = new ArrayList<String>();
			
			for (String s : CommandHandler.getInstance().getCmdList().keySet()) {
				if (s != null) {
					commands.add(s);
				}
			}
		}
		
		this.commands = cmds;
	}
	
	/**
	 * Updates the cuboids' spawn
	 * @param spawn
	 */
	public void updateSpawn(Location spawn) {
		setSpawnLocation(spawn);
	}
	
	/**
	 * Sets the cuboids' spawn
	 * @param spawn
	 */
	protected void setSpawnLocation(Location spawn) {
		if (spawn != null)
			this.spawn = spawn;
	}
	
	/**
	 * Sets the cuboids' center
	 * @param center
	 */
	protected void setCenter(Location center) {
		if (center != null)
			this.center = center;
	}
	
	/**
	 * Updates the cuboids' 'minLocation'
	 * @param minLoc
	 */
	public void updateMinLocation(Location minLoc) {
		setMinLocation(minLoc);
	}
	
	/**
	 * Sets the cuboids' 'minLocation'
	 * @param minLocation
	 */
	protected void setMinLocation(Location minLocation) {
		if (minLocation != null)
			this.minLocation = minLocation;
	}

	/**
	 * Updates the cuboids' 'maxLocation'
	 * @param maxLoc
	 */
	public void updateMaxLocation(Location maxLoc) {
		setMaxLocation(maxLoc);
	}
	
	/**
	 * Sets the cuboids' 'maxLocation'
	 * @param maxLocation
	 */
	protected void setMaxLocation(Location maxLocation) {
		if (maxLocation != null)
			this.maxLocation = maxLocation;
	}
	
	/**
	 * Sets the cuboids' world
	 * @param w
	 */
	protected void setWorld(World w) {
		if (w != null)
			this.world = w.getName();
	}

	/**
	 * Sets the cuboids' area
	 * @param area
	 */
	protected void setArea(int area) {
		this.area = Math.abs(area);
	}

	/**
	 * Sets the cuboids' volume
	 * @param volume
	 */
	protected void setVolume(int volume) {
		this.volume = Math.abs(volume);
	}

	/**
	 * Sets the cuboids' enter message
	 * @param enterMessage
	 */
	protected void setEnterMessage(String enterMessage) {
		if (enterMessage != null)
			this.enterMessage = enterMessage;
	}
	
	/**
	 * Updates the cuboids' enter message
	 * @param msg
	 */
	public void updateEnterMessage(String msg) {
		setEnterMessage(msg);
	}

	/**
	 * Sets the cuboids' leave message
	 * @param leaveMessage
	 */
	protected void setLeaveMessage(String leaveMessage) {
		if (leaveMessage != null)
			this.leaveMessage = leaveMessage;
	}
	
	/**
	 * Updates the cuboids' leave message
	 * @param msg
	 */
	public void updateLeaveMessage(String msg) {
		setLeaveMessage(msg);
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		final Cuboid another = (Cuboid) obj;
		
		if (getID() == another.getID()) {
			if (getName().equals(another.getName())) {
				if (getOwnerID() == another.getOwnerID()) {
					if (getMinLocation().equals(another.getMinLocation())) {
						if (getMaxLocation().equals(another.getMaxLocation())) {
							
							return true;
							
						}
					}
				}
			}
		}
		
		return false;
	}
}
