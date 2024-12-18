package flowershop.clock;

import jakarta.persistence.*;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
public class PendingOrder {

	@ElementCollection
	private Map<Product, Quantity> itemQuantityMap = new HashMap<Product, Quantity>();
	private LocalDate dueDate;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public PendingOrder(Map<Product, Quantity> itemQuantityMap, LocalDate dueDate) {
		this.itemQuantityMap = itemQuantityMap;
		this.dueDate = dueDate;
	}
	protected PendingOrder() {}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}
	public Map<Product, Quantity> getItemQuantityMap() {
		return itemQuantityMap;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setItemQuantityMap(Map<Product, Quantity> itemQuantityMap) {
		this.itemQuantityMap = itemQuantityMap;
	}
}
