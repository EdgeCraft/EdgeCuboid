package net.edgecraft.edgecuboid.cuboid;

public enum CuboidEvent {
	
	PvP(0),
	God(1),
	Mob(2),
	Heal(3),
	Hurt(4),
	Invis(5),
	NoChat(6),
	NoEnter(7);
	
	private int typeID;
	
	private CuboidEvent(int typeID) {
		this.typeID = typeID;
	}
	
	public int getTypeID() {
		return this.typeID;
	}
	
	public static CuboidEvent[] getCuboidEvents() {
		
		CuboidEvent[] types = { CuboidEvent.PvP, CuboidEvent.God, CuboidEvent.Mob, CuboidEvent.Heal, CuboidEvent.Hurt, CuboidEvent.Invis, CuboidEvent.NoChat, CuboidEvent.NoEnter };
		return types;
		
	}
	
	public static CuboidEvent getType(int id) {
		
		CuboidEvent[] types = getCuboidEvents();
		
		for (int i = 0; i < types.length; i++) {
			if (id == types[i].getTypeID()) {
				return types[i];
			}
		}
		
		return null;
	}
	
	/**
	 * Checks if an event is enabled in the given cuboid
	 * @param c
	 * @param event
	 * @return true/false
	 */
	public static boolean hasEvent(Cuboid c, CuboidEvent event) {
		if (c == null || event == null) return false;
		
		return c.getEvents().contains(event);
	}
	
	/**
	 * Enables the given event in the given cuboid
	 * @param c
	 * @param event
	 */
	public static void enableEvent(Cuboid c, CuboidEvent event) {
		if (c == null || event == null) return;
		if (c.getEvents().contains(event)) return;
		
		c.getEvents().add(event);
	}
	
	/**
	 * Disabled the given event in the given cuboid
	 * @param c
	 * @param event
	 */
	public static void disableEvent(Cuboid c, CuboidEvent event) {
		if (c == null || event == null) return;
		if (!c.getEvents().contains(event)) return;
		
		c.getEvents().remove(event);
	}
}
