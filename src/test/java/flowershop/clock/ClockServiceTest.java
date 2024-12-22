package flowershop.clock;


import flowershop.finances.CashRegister;
import flowershop.finances.CashRegisterRepository;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import flowershop.services.MonthlyBillingService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.catalog.Product;
import org.salespointframework.quantity.Quantity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ClockServiceTest {

	private ClockService clockService;
	private CashRegister cashRegister;
	private CashRegisterRepository cashRegisterRepository;
	private MonthlyBillingService mockMonthlyBillingService;
	private ProductService productService;

	@BeforeEach
	public void setUp() {
		this.cashRegister = new CashRegister(new HashSet<>(), Money.of(100, "UAH"));
		cashRegister.setOpen(false);
		this.cashRegisterRepository = mock(CashRegisterRepository.class);
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.ofNullable(cashRegister));
		this.mockMonthlyBillingService = mock(MonthlyBillingService.class);
		this.productService = mock(ProductService.class);
		clockService = new ClockService(cashRegisterRepository, mockMonthlyBillingService, productService);
		//when(clockService.getCashRegister()).thenReturn(cashRegister);

	}

	@Test
	public void testGetCurrentDate_CashRegisterInitialized() {
		LocalDate expectedDate = LocalDate.of(2024, 12, 22);
		cashRegister.setInGameDate(expectedDate);

		LocalDate result = clockService.getCurrentDate();

		// Assert
		assertEquals(expectedDate, result, "The method should return the in-game date from the CashRegister");
	}

	@Test
	public void testGetCurrentDate_CashRegisterNotInitialized() {
		// Arrange
		when(cashRegisterRepository.findFirstByOrderById()).thenReturn(Optional.ofNullable(null));
		//when(clockService.getCashRegister()).thenReturn(null); // Mock the getCashRegister method to return null

		// Act & Assert
		Exception exception = assertThrows(IllegalStateException.class, () -> clockService.getCurrentDate());
		assertEquals("CashRegister instance not found", exception.getMessage(),
			"The method should throw an IllegalStateException when CashRegister is null");
	}

	@Test
	public void testOpenOrClose_OpensSuccessfully() {
		cashRegister.setInGameDate(LocalDate.of(2024, 12, 22));
		//when(cashRegister.getInGameDate()).thenReturn(LocalDate.of(2024, 12, 22));
		//when(clockService.getCashRegister()).thenReturn(cashRegister);
		LocalDate nextDay = LocalDate.of(2024, 12, 23);
		//when(clockService.nextWorkingDay()).thenReturn(nextDay);

		clockService.openOrClose();

		// Assert
		assertTrue(cashRegister.getOpen(), "Shop shold be open");
		//verify(cashRegister).setOpen(true);
		assertEquals(cashRegister.getInGameDate(), nextDay);
		//verify(cashRegister).setInGameDate(nextDay);

		//verify(cashRegister).setNewDayStarted(any(LocalDateTime.class));
		assertEquals(cashRegisterRepository.findFirstByOrderById().get(),cashRegister);
		verify(cashRegisterRepository).save(cashRegister);
	}

	@Test
	public void testOpenOrClose_ClosesSuccessfully() {
		// Arrange
		cashRegister.setOpen(true);
		//when(clockService.getCashRegister()).thenReturn(cashRegister);

		// Act
		clockService.openOrClose();

		// Assert
		assertFalse(cashRegister.getOpen());
		verify(cashRegisterRepository).save(cashRegister);
		verifyNoInteractions(mockMonthlyBillingService, productService);
	}

	@Test
	public void testOpenOrClose_TriggersMonthlyCharges() {
		cashRegister.setInGameDate(LocalDate.of(2024, 11, 30));

		//when(clockService.getCashRegister()).thenReturn(cashRegister);
		LocalDate nextDay = LocalDate.of(2024, 12, 2); // New month (1.12 was sunday)
		//when(clockService.nextWorkingDay()).thenReturn(nextDay);

		// Act
		clockService.openOrClose();

		// Assert
		verify(mockMonthlyBillingService).addMonthlyCharges();
		assertEquals(cashRegister.getInGameDate(), nextDay);
		verify(cashRegisterRepository).save(cashRegister);
	}

	@Test
	public void testOpenOrClose_PendingOrdersSplitCorrectly() {
		// Arrange
		LocalDate today = LocalDate.of(2024, 12, 22);
		//cashRegister.setOpen(false);
		//when(clockService.getCashRegister()).thenReturn(cashRegister);
		cashRegister.setInGameDate(today);
		//when(cashRegister.getInGameDate()).thenReturn(today);

		// Mock pending orders
		PendingOrder orderToday = mock(PendingOrder.class);
		when(orderToday.getDueDate()).thenReturn(cashRegister.getInGameDate());
		PendingOrder orderLater = mock(PendingOrder.class);
		when(orderLater.getDueDate()).thenReturn(cashRegister.getInGameDate().plusDays(4));

		Quantity flowerQuant = mock(Quantity.class);
		when(flowerQuant.getAmount()).thenReturn(BigDecimal.valueOf(10));
		Map<Product, Quantity> itemMap = Map.of(
			mock(Flower.class), flowerQuant
		);
		when(orderToday.getItemQuantityMap()).thenReturn(itemMap);

		Set<PendingOrder> pendingOrders = new HashSet<>(Set.of(orderToday, orderLater));
		cashRegister.setPendingOrders(pendingOrders);
		//when(cashRegister.getPendingOrders()).thenReturn(pendingOrders);

		// Act
		clockService.openOrClose();

		// Assert
		verify(productService).addDeliveredFlowersFromWholesaler(anyMap());
		Set<PendingOrder> updatedPendingOrders = cashRegister.getPendingOrders();
		assertNotNull(updatedPendingOrders, "Pending orders should not be null after calling openOrClose.");
		assertTrue(updatedPendingOrders.contains(orderLater), "Pending orders should contain orders with later due dates.");
		assertFalse(updatedPendingOrders.contains(orderToday), "Pending orders should not contain today's orders.");
	}

	@Test
	public void testNextWorkingDay_Friday() {
		// Arrange
		LocalDate currentDate = LocalDate.of(2024, 12, 20); // Friday
		ClockService spyInstance = spy(clockService);
		doReturn(currentDate).when(spyInstance).getCurrentDate();

		// Act
		LocalDate result = spyInstance.nextWorkingDay();

		// Assert
		assertEquals(LocalDate.of(2024, 12, 23), result); // Monday
	}

	@Test
	public void testNextWorkingDay_Saturday() {
		// Arrange
		LocalDate currentDate = LocalDate.of(2024, 12, 21); // Saturday
		ClockService spyInstance = spy(clockService);
		doReturn(currentDate).when(spyInstance).getCurrentDate();

		// Act
		LocalDate result = spyInstance.nextWorkingDay();

		// Assert
		assertEquals(LocalDate.of(2024, 12, 23), result); // Monday
	}

	@Test
	public void testNextWorkingDay_Sunday() {
		// Arrange
		LocalDate currentDate = LocalDate.of(2024, 12, 22); // Sunday
		ClockService spyInstance = spy(clockService);
		doReturn(currentDate).when(spyInstance).getCurrentDate();

		// Act
		LocalDate result = spyInstance.nextWorkingDay();

		// Assert
		assertEquals(LocalDate.of(2024, 12, 23), result); // Monday
	}
}
