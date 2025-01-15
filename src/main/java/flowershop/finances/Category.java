package flowershop.finances;

/**
 * Types of orders
 */
public enum Category {
	/**
	 * Simple purchase at the store
	 */
	EINFACHER_VERKAUF,
	/**
	 * Purchase with a reservation
	 */
	RESERVIERTER_VERKAUF,
	/**
	 * Purchase for the event
	 */
	VERANSTALTUNG_VERKAUF,
	/**
	 * Contract
	 */
	VERTRAGLICHER_VERKAUF,
	/**
	 * Purchase by the shop from the wholesaler
	 */
	EINKAUF
}
