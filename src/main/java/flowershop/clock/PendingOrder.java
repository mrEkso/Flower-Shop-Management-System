package flowershop.clock;

import jakarta.persistence.*;
import org.salespointframework.quantity.Quantity;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
public class PendingOrder {

	@ElementCollection
	private Map<String, Quantity> itemQuantityMap = new HashMap<String, Quantity>();
	private LocalDate dueDate;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	public PendingOrder(Map<String, Quantity> itemQuantityMap, LocalDate dueDate) {
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
	public Map<String, Quantity> getItemQuantityMap() {
		return itemQuantityMap;
	}

	public void setDueDate(LocalDate dueDate) {
		this.dueDate = dueDate;
	}
	public LocalDate getDueDate() {
		return dueDate;
	}

	public void setItemQuantityMap(Map<String, Quantity> itemQuantityMap) {
		this.itemQuantityMap = itemQuantityMap;
	}
}
