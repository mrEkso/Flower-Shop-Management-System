package flowershop.service;

import flowershop.calendar.CalendarService;
import flowershop.services.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.salespointframework.order.OrderManagement;
import org.salespointframework.useraccount.UserAccount;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;

class MonthlyBillingServiceTest {
	@Mock
	ContractOrderService contractOrderService;
	@Mock
	ApplicationEventPublisher eventPublisher;
	@Mock
	OrderManagement<ContractOrder> orderManagement;
	@InjectMocks
	MonthlyBillingService monthlyBillingService;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
	}

	@Test
	void testAddMonthlyCharges() {
		// Mock the UserAccountManagement
		CalendarService calendarService = mock(CalendarService.class);
		UserAccountManagement userAccountManagement = mock(UserAccountManagement.class);
		UserAccount userAccount = mock(UserAccount.class);
		when(userAccount.getId()).thenReturn(UserAccount.UserAccountIdentifier.of("user-id")); // Ensure getId() returns a non-null value
		when(userAccountManagement.findByUsername("shop_worker"))
			.thenReturn(java.util.Optional.of(userAccount));

		// Create an instance of OrderFactory
		OrderFactory orderFactory = new OrderFactory(userAccountManagement, calendarService);

		// Use OrderFactory to create ContractOrder
		ContractOrder contractOrder = orderFactory.createContractOrder(
			"one-time",
			"weekly",
			LocalDate.of(2024, 11, 12).atStartOfDay(),
			LocalDate.of(2026, 1, 1).atStartOfDay(),
			"test address",
			new Client(),
			"test notes"
		);
		when(contractOrderService.findAllActiveLastMonth()).thenReturn(List.of(contractOrder));
		when(orderManagement.payOrder(any(ContractOrder.class))).thenReturn(true);
		when(contractOrderService.findAllActiveLastMonth()).thenReturn(List.of(contractOrder));
		when(orderManagement.payOrder(any(ContractOrder.class))).thenReturn(true);

		monthlyBillingService.addMonthlyCharges();
		verify(eventPublisher).publishEvent(any(Object.class));
	}
}