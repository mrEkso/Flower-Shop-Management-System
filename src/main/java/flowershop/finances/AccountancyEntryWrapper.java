package flowershop.finances;

import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.sales.SimpleOrder;
import flowershop.sales.WholesalerOrder;
import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import jakarta.persistence.*;
import org.salespointframework.accountancy.AccountancyEntry;
import org.salespointframework.catalog.Product;
import org.salespointframework.order.ChargeLine;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderLine;
import org.salespointframework.order.Totalable;
import org.salespointframework.quantity.Quantity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


/**
 * This class is used to adapt Order to AccountancyEntry to then be used in CashRegisterService (Accountancy child class)
 */
@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AccountancyEntryWrapper extends AccountancyEntry {
	// For every entry:
	//  	* timestamp (parent class has a very stupid way of setting it. Or maybe I'm too stupid for it)
	//      - income / spending (parent class)
	//      * category
	//      * items (as separate rows)
	//      * quantity (as separate rows)
	//      - Amount (parent class)


	@ElementCollection
	private Map<String, Quantity> nameQuantityMap = new HashMap<String, Quantity>();

	@ElementCollection
	private Map<Product, Quantity> productQuantityMap = new HashMap<>();

	private Category category;
	private LocalDateTime timestamp;
	private String clientName;

	@Transient
	private ProductService productService;


	/**
	 *
	 * @param category
	 * @return the names of categories of orders in German
	 */
	public static String categoryToString(Category category) {
		return category.toString().replace('_', ' ');
	}

	public String getCategory() {
		return this.categoryToString(this.category);
	}

	public String getClientName() {
		if(this.clientName == null) {
			return "";
		}
		return this.clientName;
	}

	/**
	 * USE THIS METHOD INSTEAD OF getDate()!
	 * @return the time when the order was paid.
	 */
	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	/**
	 *
	 * @return the map where keys are name of the products, and values - their quantity
	 */
	public Map<String, Quantity> getItems() {
		/*
		return productQuantityMap.entrySet().stream()
			.collect(Collectors.toMap(
				entry -> entry.getKey().getName(),
				Map.Entry::getValue
			));

		 */
		return nameQuantityMap;
	}

	protected AccountancyEntryWrapper() {
	}

	public AccountancyEntryWrapper(Order order, LocalDateTime time, ProductService productService) {
		super(order.getTotal());
		this.productService = productService;
		this.timestamp = time;
		if (order instanceof WholesalerOrder) {
			this.category = Category.Einkauf;
		} else if (order instanceof ContractOrder) {
			this.clientName = ((ContractOrder) order).getClient().getName();
			this.category = Category.Vertraglicher_Verkauf;
		} else if (order instanceof EventOrder) {
			this.clientName = ((EventOrder) order).getClient().getName();
			this.category = Category.Veranstaltung_Verkauf;
		} else if (order instanceof ReservationOrder) {
			this.clientName = ((ReservationOrder) order).getClient().getName();
			this.category = Category.Reservierter_Verkauf;
		} else if (order instanceof SimpleOrder) {
			this.category = Category.Einfacher_Verkauf;
		} else {
			throw new IllegalArgumentException("Order is not recognized");
		}
		Totalable<OrderLine> kindaItemQuantityMap = order.getOrderLines();
		for (OrderLine orderLine : kindaItemQuantityMap) {
			nameQuantityMap.put(orderLine.getProductName(), orderLine.getQuantity());

			if(order instanceof WholesalerOrder) {
				String name = orderLine.getProductName();
				List<Flower> lst = productService.findFlowersByName(name);
				productQuantityMap.put(lst.getFirst(), orderLine.getQuantity());
			}
		}
		Totalable<ChargeLine> extraFees = order.getAllChargeLines();
		for (ChargeLine chargeLine : extraFees) {
			nameQuantityMap.put(chargeLine.getDescription(), Quantity.of(1));
		}
	}

	public Map<Product, Quantity> getFlowers(){
		return productQuantityMap;
	}

}
