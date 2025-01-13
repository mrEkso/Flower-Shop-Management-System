package flowershop.finances;

import flowershop.clock.ClockService;
import flowershop.product.Bouquet;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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


	@ElementCollection(fetch = FetchType.EAGER)
	private Map<String, Quantity> nameQuantityMap = new HashMap<String, Quantity>();

	@ElementCollection
	private Map<Product, Quantity> productQuantityMap = new HashMap<>();

	private Category category;
	private LocalDateTime timestamp;
	private String clientName;
	private String clientPhone;
	private String adress;
	private LocalDateTime date1;
	private LocalDateTime date2;
	private String frequency;
	private LocalDate reservationExecution;
	private String notes;
	private String paymentMethod;


	@Transient
	private ProductService productService;


	public String getClientPhone(){
		if(clientPhone != null) return clientPhone;
		return "";
	}
	public String getAdress(){
		if(adress != null) return adress;
		return "";
	}
	public LocalDateTime getDate1(){
		if(date1 != null) return date1;
		return null;
	}
	public LocalDateTime getDate2(){
		if(date2 != null) return date2;
		return null;
	}
	public String getFrequency(){
		if(frequency != null) return frequency;
		return "";
	}

	public String getNotes() {
		if(notes != null) return notes;
		return "";
	}

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

	public String getTimestampStr()
	{
		StringBuilder str = new StringBuilder(this.timestamp.getDayOfMonth() + ".");
		str.append(this.timestamp.getMonthValue() + ".")
			.append(this.timestamp.getYear() + " ")
			.append(this.timestamp.getHour() + ":");
		if(this.timestamp.getMinute() < 10)
		{
			str.append("0");
		}
		str.append(this.timestamp.getMinute());
		return str.toString();
	}

	/**
	 * USE THIS METHOD INSTEAD OF getDate()!
	 * @return the time when the order was paid.
	 */
	public LocalDateTime getTimestamp() {
		return this.timestamp;
	}

	public LocalDate getDeliveryDate() {
		return reservationExecution;
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
		this.paymentMethod = order.getPaymentMethod().toString();
		if (order instanceof WholesalerOrder) {
			this.category = Category.Einkauf;
			String notes = ((WholesalerOrder) order).getNotes();
			if(notes != null) {
				this.reservationExecution = LocalDate.parse(notes);
				this.date1 = this.reservationExecution.atTime(9,0);
			}
			else{
				this.date1 = ClockService.nextWorkingDay(this.timestamp.toLocalDate()).atTime(9, 0);
			}
		} else if (order instanceof ContractOrder) {
			this.clientName = ((ContractOrder) order).getClient().getName();
			this.clientPhone = ((ContractOrder) order).getClient().getPhone();
			this.adress = ((ContractOrder) order).getAddress();
			this.category = Category.Vertraglicher_Verkauf;
			this.date1 = ((ContractOrder) order).getStartDate();
			this.date2 = ((ContractOrder) order).getEndDate();
			this.frequency = ((ContractOrder) order).getFrequency();
			this.notes = ((ContractOrder) order).getNotes();
		} else if (order instanceof EventOrder) {
			this.clientName = ((EventOrder) order).getClient().getName();
			this.clientPhone = ((EventOrder) order).getClient().getPhone();
			this.adress = ((EventOrder) order).getDeliveryAddress();
			this.category = Category.Veranstaltung_Verkauf;
			this.date1 = ((EventOrder) order).getEventDate();
			this.reservationExecution = this.date1.toLocalDate();
			this.notes = ((EventOrder) order).getNotes();
		} else if (order instanceof ReservationOrder) {
			this.clientName = ((ReservationOrder) order).getClient().getName();
			this.clientPhone = ((ReservationOrder) order).getClient().getPhone();
			this.category = Category.Reservierter_Verkauf;
			this.date1 = ((ReservationOrder) order).getReservationDateTime();
			this.reservationExecution = this.date1.toLocalDate();
			this.notes = ((ReservationOrder) order).getNotes();
		} else if (order instanceof SimpleOrder) {
			this.category = Category.Einfacher_Verkauf;
		} else {
			throw new IllegalArgumentException("Order is not recognized");
		}
		Totalable<OrderLine> kindaItemQuantityMap = order.getOrderLines();
		for (OrderLine orderLine : kindaItemQuantityMap) {
			nameQuantityMap.put(orderLine.getProductName(), orderLine.getQuantity());

			if(order instanceof WholesalerOrder || order instanceof EventOrder || order instanceof ReservationOrder) {
				String name = orderLine.getProductName();
				List<Flower> lst = productService.findFlowersByName(name);
				if(!lst.isEmpty()) {
					if(lst.getFirst().getName().equals(name)) {
						productQuantityMap.merge(lst.getFirst(), orderLine.getQuantity(), Quantity::add);
					}
					continue;
				}
				if (order instanceof EventOrder || order instanceof ReservationOrder) {
					List<Bouquet> bouquetList = productService.findBouquetsByName(name);
					if(!bouquetList.isEmpty()) {
						if(bouquetList.getFirst().getName().equals(name)) {
							Bouquet bouquet = bouquetList.getFirst();
							for (Map.Entry<Flower, Integer> eventBouquettePair : bouquet.getFlowers().entrySet()) {
								productQuantityMap.merge(eventBouquettePair.getKey(),
									Quantity.of(eventBouquettePair.getValue())
										.times(orderLine.getQuantity().getAmount().intValue()),
									Quantity::add);
							}
						}
					}
				}
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
