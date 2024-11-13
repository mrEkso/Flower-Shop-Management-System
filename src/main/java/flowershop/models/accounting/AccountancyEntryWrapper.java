package flowershop.models.accounting;

import flowershop.models.order.*;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.Totalable;
import org.salespointframework.quantity.Quantity;

import java.util.HashMap;
import java.util.Map;

public class AccountancyEntryWrapper extends AccountancyEntry {
	// For every entry:
	//  	- timestamp (parent class)
	//      - income / spending (parent class)
	//      * category
	//      * items (as separate rows)
	//      * quantity (as separate rows)
	//      - Amount (parent class)
	private Map<String, Quantity> itemQuantityMap = new HashMap<String,Quantity>();
	private Category category;
	// PLEASE DONT CHANGE THIS FORMAT OF ENUMS!!!
	enum Category{
		Einfacher_Verkauf,
		Reservierter_Verkauf,
		Veranstaltung_Verkauf,
		Vertraglicher_Verkauf,
		Einkauf
	}
	public String categoryToString(Category category){
		return category.toString().replace('_',' ');
	}

	public Category getCategory() {
		return category;
	}

	public Map<String, Quantity> getItemQuantityMap() {
		return itemQuantityMap;
	}

	public AccountancyEntryWrapper(AbstractOrder order) {
		super(order.getTotal());
		if(order == null){
			throw new IllegalArgumentException("Order is null, couldn't create an AccountancyEntryWrapper");
		}
		if(order instanceof WholesalerOrder){
			this.category = Category.Einkauf;
		}
		else if(order instanceof ContractOrder){
			this.category = Category.Vertraglicher_Verkauf;
		}
		else if(order instanceof EventOrder){
			this.category = Category.Veranstaltung_Verkauf;
		}
		else if(order instanceof ReservationOrder){
			this.category = Category.Reservierter_Verkauf;
		}
		else if(order instanceof SimpleOrder){
			this.category = Category.Einfacher_Verkauf;
		}
		else{
			throw new IllegalArgumentException("Order is not recognized");
		}
		Totalable<OrderLine> kindaItemQuantityMap = order.getOrderLines();
		for (OrderLine orderLine : kindaItemQuantityMap) {
			itemQuantityMap.put(orderLine.getProductName(),orderLine.getQuantity());
		}
		Totalable<ChargeLine> extraFees = order.getAllChargeLines();
		for (ChargeLine chargeLine : extraFees) {
			itemQuantityMap.put(chargeLine.getDescription(),Quantity.of(1));
		}
	}

}
