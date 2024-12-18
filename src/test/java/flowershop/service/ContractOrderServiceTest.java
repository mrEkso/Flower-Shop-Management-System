package flowershop.service;

import flowershop.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.salespointframework.order.Order;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class ContractOrderServiceTest {
	@Mock
	ContractOrderRepository contractOrderRepository;

	@Mock
	private OrderManagement<ContractOrder> orderManagement;

	@Mock
	private UserAccountManagement userAccountManagement;

	@InjectMocks
	private OrderFactory orderFactory;

	@InjectMocks
	ContractOrderService contractOrderService;

	private UserAccount mockUserAccount;
	private Client mockClient;

	@BeforeEach
	public void setUp() {
		MockitoAnnotations.openMocks(this);

		mockUserAccount = mock(UserAccount.class);
		when(mockUserAccount.getId()).thenReturn(UserAccount.UserAccountIdentifier.of("mock-user-id"));

		mockClient = new Client(1L, "John Doe", "123456789");
	}

	@Test
	void testFindAll() {
		when(contractOrderRepository.findAll()).thenReturn(Collections.emptyList());

		var result = contractOrderService.findAll();

		assertEquals(Collections.emptyList(), result);
		verify(contractOrderRepository, times(1)).findAll();
	}


	@Test
	public void testGetById() throws Exception {
		UUID uuid = UUID.randomUUID();
		Order.OrderIdentifier orderId = Order.OrderIdentifier.of(uuid.toString());

		ContractOrder mockOrder = new ContractOrder(mockUserAccount, null, null, null, null, null, null, "notes");

		when(contractOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));

		Optional<ContractOrder> result = contractOrderService.getById(uuid);

		assertTrue(result.isPresent());
		assertEquals(mockOrder, result.get());
		verify(contractOrderRepository, times(1)).findById(orderId);
	}

	@Test
	void testSaveContractOrder() {
		LocalDateTime startDate = LocalDate.now().atStartOfDay();
		LocalDateTime endDate = LocalDate.of(2026, 1, 1).atStartOfDay();
		Client mockClient = mock(Client.class);
		ContractOrder contractOrder = mock(ContractOrder.class);

		when(userAccountManagement.findByUsername("shop_worker")).thenReturn(Optional.of(mockUserAccount));

		ContractOrder mockOrder = orderFactory.createContractOrder(
			"recurring", "once a week", startDate, endDate, mockClient);

		when(orderManagement.save(any(ContractOrder.class))).thenReturn(mockOrder);
		when(contractOrderRepository.save(any(ContractOrder.class))).thenReturn(mockOrder);

		ContractOrder result = contractOrderService.save(mockOrder, Collections.emptyMap());


		assertEquals(mockOrder, result);
		verify(contractOrderRepository, times(1)).save(mockOrder);
	}

	@Test
	void testUpdate() {
		LocalDateTime startDate = LocalDateTime.of(2024, Month.DECEMBER, 15, 19, 16, 6);
		LocalDateTime endDate = LocalDateTime.of(2024, Month.DECEMBER, 15, 19, 16, 6);

		ContractOrder mockOrder = new ContractOrder(mockUserAccount, "recurring", null, startDate, endDate, "address", mockClient, "notes");

		// Simulated products with product IDs as keys and quantities as values
		Map<String, String> products = Map.of(
			"product-1", "10",
			"product-2", "5"
		);

		String orderStatus = "OPEN";
		String cancelReason = "none";

		when(contractOrderRepository.save(any(ContractOrder.class))).thenReturn(mockOrder);

		ContractOrder result = contractOrderService.update(mockOrder, products, 0, orderStatus, cancelReason);

		assertEquals(result, mockOrder);
		verify(contractOrderRepository, times(1)).save(mockOrder);
	}


	@Test
	public void testDelete() throws Exception {
		ContractOrder mockOrder = new ContractOrder(mockUserAccount, null, null, null, null, null, null, "notes");

		contractOrderService.delete(mockOrder);

		verify(contractOrderRepository, times(1)).delete(mockOrder);
	}

	@Test
	public void testRemoveProductFromOrder() throws Exception {
		UUID orderUuid = UUID.randomUUID();
		Order.OrderIdentifier orderId = Order.OrderIdentifier.of(orderUuid.toString());
		UUID productUuid = UUID.randomUUID();

		ContractOrder mockOrder = new ContractOrder(mockUserAccount, null, null, null, null, null, null, "notes");
		when(contractOrderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
		when(contractOrderRepository.save(any(ContractOrder.class))).thenReturn(mockOrder);

		contractOrderService.removeProductFromOrder(orderUuid, productUuid);

		verify(contractOrderRepository, times(1)).save(mockOrder);
	}


}
