package net.edgecraft.edgecuboid.cuboid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import net.edgecraft.edgecuboid.cuboid.types.HabitatType;

public class Habitat implements Serializable {
	
	private static final long serialVersionUID = 1l;
	
	private Cuboid cuboid;
	private int cuboidID;
	private HabitatType type;
	private String owner;
	private String tenant;
	
	private double worth;
	private boolean buyable;
	
	private double rental;
	private boolean rentable;
	
	private Map<Upgrade, Integer> upgrades;
	
	protected Habitat() { /* ... */ }
	
	protected Habitat(Cuboid cuboid, HabitatType type, String owner, String tenant, double worth, boolean buyable, double rental, boolean rentable, Map<Upgrade, Integer> upgrades) {
		
		setCuboid(cuboid);
		setHabitatType(type);
		setOwner(owner);
		setTenant(tenant);
		setWorth(worth);
		setBuyableStatus(buyable);
		setRental(rental);
		setRentableStatus(rentable);
		setUpgrades(upgrades);
		
	}
	
	/**
	 * Turns a byte[] back to an habitat instance
	 * @param byteArray
	 * @return Habitat
	 */
	public static Habitat toHabitat(byte[] byteArray) {
		try {
			
			ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(byteArray);
			ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);

			@SuppressWarnings("unchecked")
			Map<String, Object> infoMap = (Map<String, Object>) objectInputStream.readObject();
			
			Habitat habitat = new Habitat();
			habitat.deserialize(infoMap);
			
			return habitat;
			
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Writes this habitat instance into a byte[]
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
	 * Serializes the current habitat instance
	 * @return Map<String, Object>
	 */
	private Map<String, Object> serialize() {
		Map<String, Object> infoMap = new LinkedHashMap<String, Object>();
		infoMap.put("object-type", "Habitat");
		
		// Put information
		infoMap.put("cuboid", getCuboidID());
		infoMap.put("habitat-type", getType().getTypeID());
		infoMap.put("owner", owner);
		infoMap.put("tenant", tenant);
		
		// Put price information
		infoMap.put("worth", worth);
		infoMap.put("buyable", buyable);
		infoMap.put("rental", rental);
		infoMap.put("rentable", rentable);
		infoMap.put("upgrades", upgrades);
		
		return infoMap;
	}
	
	/**
	 * Deserializes the given habitat serialization
	 * @param infoMap
	 */
	@SuppressWarnings("unchecked")
	private void deserialize(Map<String, Object> infoMap) {
		if (!infoMap.containsKey("object-type") || !infoMap.get("object-type").equals("Habitat")) throw new java.util.UnknownFormatFlagsException("No Habitat");
		
		setCuboid(CuboidHandler.getInstance().getCuboid((int) infoMap.get("cuboid")));
		setHabitatType(HabitatType.getType((int) infoMap.get("habitat-type")));
		setOwner((String) infoMap.get("owner"));
		setTenant((String) infoMap.get("tenant"));
		
		setWorth((double) infoMap.get("worth"));
		setBuyableStatus((boolean) infoMap.get("buyable"));
		setRental((double) infoMap.get("rental"));
		setRentableStatus((boolean) infoMap.get("rentable"));
		setUpgrades((Map<Upgrade, Integer>) infoMap.get("upgrades"));
	}

	/**
	 * Returns the HabitatType
	 * @return HabitatType
	 */
	public HabitatType getType() {
		return type;
	}
	
	/**
	 * Returns the habitats' owner
	 * @return String
	 */
	public String getOwner() {
		return owner;
	}
	
	/**
	 * Checks if the given user is the owner
	 * @param user
	 * @return true/false
	 */
	public boolean isOwner(String user) {
		return user.equals(getOwner());
	}

	/**
	 * Returns the habitats' tenant (if given)
	 * @return String
	 */
	public String getTenant() {
		return tenant;
	}
	
	/**
	 * Checks if the given user is the tenant
	 * @param user
	 * @return true/false
	 */
	public boolean isTenant(String user) {
		return user.equals(getTenant());
	}
	
	/**
	 * Checks if the habitat is inhabited
	 * @return true/false
	 */
	public boolean isInhabited() {
		return tenant == null;
	}

	/**
	 * Returns the habitats' worth
	 * @return Double
	 */
	public double getWorth() {
		return worth;
	}

	/**
	 * Checks if the habitat is able to get bought
	 * @return true/false
	 */
	public boolean isBuyable() {
		return buyable;
	}

	/**
	 * Returns the height of the habitats' rental
	 * @return Double
	 */
	public double getRental() {
		return rental;
	}

	/**
	 * Checks if the habitat is able to get rented
	 * @return true/false
	 */
	public boolean isRentable() {
		return rentable;
	}
	
	/**
	 * Returns the taxes which have to be paid
	 * @return Double
	 */
	public double getTaxes() {
		return Math.round((double) (getCuboid().getArea() / 100) * (getWorth() / 100));
	}
	
	/**
	 * Returns the cuboid connected with this habitat
	 * @return Cuboid
	 */
	public Cuboid getCuboid() {
		return cuboid;
	}
	
	/**
	 * Returns the ID of the cuboid connected with this habitat
	 * @return Integer
	 */
	public int getCuboidID() {
		return cuboidID;
	}
	
	/**
	 * Returns a Map of all unlocked upgrades
	 * @return Map<Upgrade, Integer>
	 */
	public Map<Upgrade, Integer> getUpgrades() {
		return upgrades;
	}
	
	/**
	 * Unlocks the given upgrade or adds one if already unlocked
	 * @param upgrade
	 */
	public void unlockUpgrade(Upgrade upgrade) {
		if(!upgrade.multipleUsage()) return;
		if (getType() != HabitatType.House || getType() != HabitatType.Villa) return;
		
		if (getUpgrades().containsKey(upgrade)) {
			if (upgrade.multipleUsage())
				getUpgrades().put(upgrade, getUpgrades().get(upgrade) + 1);
			
		} else {
			
			getUpgrades().put(upgrade, 1);
			
		}
	}
	
	/**
	 * Removes an upgrade
	 * @param upgrade
	 */
	public void removeUpgrade(Upgrade upgrade) {
		if (!getUpgrades().containsKey(upgrade)) return;
		
		if (getUpgrades().get(upgrade) == 1)
			getUpgrades().remove(upgrade);
		else
			getUpgrades().put(upgrade, getUpgrades().get(upgrade) - 1);
	}

	/**
	 * Sets the type
	 * @param i
	 */
	protected void setHabitatType(HabitatType type) {
		if (type != null)
			this.type = type;
	}

	/**
	 * Sets the owner
	 * @param owner
	 */
	protected void setOwner(String owner) {
		if (owner != null)
			this.owner = owner;
	}
	
	/**
	 * Switches the owner
	 * @param newOwner
	 */
	public void switchOwner(String newOwner) {
		setOwner(newOwner);
	}

	/**
	 * Sets the tenant
	 * @param tenant
	 */
	protected void setTenant(String tenant) {
		if (tenant != null)
			this.tenant = tenant;
	}
	
	/**
	 * Switches the tenant
	 * @param newTenant
	 */
	public void switchTenant(String newTenant) {
		setTenant(newTenant);
	}

	/**
	 * Sets the worth
	 * @param worth
	 */
	protected void setWorth(double worth) {
		if (worth > 0)
			this.worth = worth;
	}
	
	/**
	 * Updates the worth (should might be calculated in own methods)
	 * @param worth
	 */
	public void updateWorth(double worth) {
		setWorth(worth);
	}
	
	/**
	 * Sets the availability of being able to get bought
	 * @param buyable
	 */
	protected void setBuyableStatus(boolean buyable) {
		this.buyable = buyable;
	}
	
	/**
	 * Sets the availability of being able to get bought
	 * @param var
	 */
	public void setBuyable(boolean var) {
		setBuyableStatus(var);
		
		if (isBuyable())
			setRentableStatus(false);
		else
			setRentableStatus(isRentable());
	}

	/**
	 * Sets the rental
	 * @param rental
	 */
	protected void setRental(double rental) {
		if (rental > getWorth())
			this.rental = getWorth();
		
		if (rental > 0)
			this.rental = rental;
	}
	
	/**
	 * Updates the rental
	 * @param rental
	 */
	public void updateRental(double rental) {
		setRental(rental);
	}

	/**
	 * Sets the availability of being able to get rented
	 * @param rentable
	 */
	protected void setRentableStatus(boolean rentable) {
		this.rentable = rentable;
	}
	
	/**
	 * Updates the availability of being able to get rented
	 * @param var
	 */
	public void setRentable(boolean var) {
		setRentableStatus(var);
	}
	
	/**
	 * Sets the cuboid which will get connected with this habitat
	 * @param c
	 */
	protected void setCuboid(Cuboid c) {
		if (c != null) {
			this.cuboid = c;
			setCuboidID(c.getID());
		}
	}
	
	/**
	 * Sets the upgrades
	 * @param upgrades
	 */
	protected void setUpgrades(Map<Upgrade, Integer> upgrades) {
		if (upgrades == null)
			this.upgrades = new HashMap<Upgrade, Integer>();
		else
			this.upgrades = upgrades;
	}
	
	/**
	 * Sets the cuboid ID
	 * @param id
	 */
	private void setCuboidID(int id) {
		this.cuboidID = id;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) return true;
		if (obj == null) return false;
		if (getClass() != obj.getClass()) return false;
		
		final Habitat another = (Habitat) obj;
		
		if (getCuboidID() == another.getCuboidID()) {
			return true;
		}
		
		return false;
	}
	
	@Override
	public String toString() {
		return "Habitat {" + getCuboidID() + "," + getOwner() + "," + getTenant() + ",$" + getWorth() + "}";
	}
}
