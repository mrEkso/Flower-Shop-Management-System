package flowershop.clock;

import flowershop.product.Flower;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PendingOrderTest {

	private Map<Product, Quantity> itemQuantityMap;
	private LocalDate dueDate;
	private PendingOrder pendingOrder;

	@BeforeEach
	public void setUp() {
		itemQuantityMap = new HashMap<>();
		Product product = mock(Flower.class);
		when(product.getName()).thenReturn("Rose");
		Quantity quantity = Quantity.of(10);
		itemQuantityMap.put(product, quantity);
		dueDate = LocalDate.of(2024, 12, 25);
		pendingOrder = new PendingOrder(itemQuantityMap, dueDate);
	}

	@Test
	public void testConstructorAndGetters() {
		assertEquals(itemQuantityMap, pendingOrder.getItemQuantityMap(), "ItemQuantityMap should be set correctly in the constructor.");
		assertEquals(dueDate, pendingOrder.getDueDate(), "DueDate should be set correctly in the constructor.");
	}

	@Test
	public void testDefaultConstructor() {
		PendingOrder defaultPendingOrder = new PendingOrder();
		assertNotNull(defaultPendingOrder, "Default constructor should create a non-null instance.");
		assertNull(defaultPendingOrder.getDueDate(), "Default constructor should initialize dueDate as null.");
		assertTrue(defaultPendingOrder.getItemQuantityMap().isEmpty(), "Default constructor should initialize an empty itemQuantityMap.");
	}

	@Test
	public void testSetAndGetDueDate() {
		LocalDate newDueDate = LocalDate.of(2025, 1, 1);
		pendingOrder.setDueDate(newDueDate);
		assertEquals(newDueDate, pendingOrder.getDueDate(), "setDueDate should correctly update the dueDate.");
	}

	@Test
	public void testSetAndGetItemQuantityMap() {
		Map<Product, Quantity> newMap = new HashMap<>();
		Product newProduct = mock(Flower.class);
		when(newProduct.getName()).thenReturn("Tulip");
		Quantity newQuantity = Quantity.of(20);
		newMap.put(newProduct, newQuantity);

		pendingOrder.setItemQuantityMap(newMap);
		assertEquals(newMap, pendingOrder.getItemQuantityMap(), "setItemQuantityMap should correctly update the itemQuantityMap.");
	}

	@Test
	public void testGetAndSetId() {
		Long id = 123L;
		pendingOrder.setId(id);
		assertEquals(id, pendingOrder.getId(), "setId should correctly update the ID.");
	}

}
