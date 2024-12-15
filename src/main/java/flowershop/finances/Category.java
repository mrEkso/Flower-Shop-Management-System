package flowershop.finances;

/**
 * Types of orders
 */
public enum Category {
	/**
	 * Simple purchase at the store
	 */
	Einfacher_Verkauf,
	/**
	 * Purchase with a reservation
	 */
	Reservierter_Verkauf,
	/**
	 * Purchase for the event
	 */
	Veranstaltung_Verkauf,
	/**
	 * Contract
	 */
	Vertraglicher_Verkauf,
	/**
	 * Purchase by the shop from the wholesaler
	 */
	Einkauf
}
