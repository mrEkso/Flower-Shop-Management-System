package flowershop.service;

import flowershop.clock.ClockService;
import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import flowershop.services.ServiceController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ServiceController}.
 **/
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "boss", roles = {"BOSS", "USER"})
class ServiceControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

	@MockBean
	ClockService clockService;

	@BeforeEach
	void setUp() {
		when(clockService.isOpen()).thenReturn(true);
	}

	@Test
	void showsAllServicesShouldReturnContractsAndEventsAndReservationsAndServicePage() throws Exception {
		mvc.perform(get("/services"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("contracts"))
			.andExpect(model().attributeExists("events"))
			.andExpect(model().attributeExists("reservations"))
			.andExpect(view().name("services/services"));
	}

	@Test
	void getContractOrderViewPageShouldReturnContractOrderAndContractOrderPage() throws Exception {
		mvc.perform(get("/services/contracts/view/{id}", getFirstValidContractId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("contractOrder"))
			.andExpect(view().name("services/view/contractOrderViewForm"));
	}

	@Test
	void shouldGetEventOrderById() throws Exception {
		mvc.perform(get("/services/events/view/{id}", getFirstValidEventId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("eventOrder"))
			.andExpect(view().name("services/view/eventOrderViewForm"));
	}

	@Test
	void shouldGetReservationOrderById() throws Exception {
		mvc.perform(get("/services/reservations/view/{id}", getFirstValidReservationId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("reservationOrder"))
			.andExpect(view().name("services/view/reservationOrderViewForm"));
	}

	@Test
	void getEmptyResponseShouldReturnOk() throws Exception {
		mvc.perform(get("/services/empty-response"))
			.andExpect(status().isOk());
	}

	@Test
	void getNewOrderPageShouldBeAccessible() throws Exception {
		mvc.perform(get("/services/create"))
			.andExpect(status().isOk())
			.andExpect(view().name("services/create_service"));
	}

	@Test
	void addProductRowShouldReturnRowFragment() throws Exception {
		mvc.perform(get("/services/add-product-row")
				.param("index", "1"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("index"))
			.andExpect(model().attributeExists("products"));
	}

	@Test
	void chooseFrequencyOptionsShouldReturnFrequencyOptionsContainerForRecurring() throws Exception {
		mvc.perform(get("/services/contracts/choose-frequency-options")
				.param("contractType", "Recurring"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "Recurring"))
			.andExpect(view().name("fragments/frequency-options :: frequencyOptionsContainer"));
	}

	@Test
	void chooseFrequencyOptionsShouldReturnEmptyFrequencyOptionsForOneTime() throws Exception {
		mvc.perform(get("/services/contracts/choose-frequency-options")
				.param("contractType", "One-Time"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "One-Time"))
			.andExpect(view().name("fragments/empty-frequency-options :: empty-frequency-options"));
	}

	@Test
	void chooseFrequencyOptionsShouldReturnEmptyFrequencyOptionsForNullContractType() throws Exception {
		mvc.perform(get("/services/contracts/choose-frequency-options"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "One-Time"))
			.andExpect(view().name("fragments/empty-frequency-options :: empty-frequency-options"));
	}

	@Test
	void chooseCustomFrequencyOptionsShouldReturnCustomOptionsContainerForCustomFrequency() throws Exception {
		mvc.perform(get("/services/contracts/choose-custom-frequency-options")
				.param("frequency", "custom")
				.param("customFrequency", "5")
				.param("customUnit", "days"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "Recurring"))
			.andExpect(model().attribute("frequency", "custom"))
			.andExpect(model().attribute("customFrequency", 5))
			.andExpect(model().attribute("customUnit", "days"))
			.andExpect(view().name("fragments/frequency-options :: customOptionsContainer"));
	}

	@Test
	void chooseCustomFrequencyOptionsShouldReturnEmptyCustomOptionsForNonCustomFrequency() throws Exception {
		mvc.perform(get("/services/contracts/choose-custom-frequency-options")
				.param("frequency", "weekly"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "Recurring"))
			.andExpect(model().attribute("frequency", "weekly"))
			.andExpect(model().attribute("customFrequency", (Integer) null))
			.andExpect(model().attribute("customUnit", (String) null))
			.andExpect(view().name("fragments/empty-frequency-options :: empty-custom-options"));
	}

	@Test
	void chooseCustomFrequencyOptionsShouldReturnEmptyCustomOptionsForNullFrequency() throws Exception {
		mvc.perform(get("/services/contracts/choose-custom-frequency-options"))
			.andExpect(status().isOk())
			.andExpect(model().attribute("contractType", "Recurring"))
			.andExpect(model().attribute("frequency", ""))
			.andExpect(model().attribute("customFrequency", (Integer) null))
			.andExpect(model().attribute("customUnit", (String) null))
			.andExpect(view().name("fragments/empty-frequency-options :: empty-custom-options"));
	}

	@Test
	void createContractOrderShouldCreateContractOrder() throws Exception {
		mvc.perform(post("/services/contracts/create")
				.param("clientName", "Test Client")
				.param("contractType", "Standard")
				.param("startDate", "2024-05-01T12:00")
				.param("endDate", "2024-12-01T12:00")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("notes", "Test notes")
				.param("servicePrice", "100"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void createContractOrderShouldReturnErrorWhenShopIsClosed() throws Exception {
		// Mock the clockService to return false for isOpen()
		when(clockService.isOpen()).thenReturn(false);

		mvc.perform(post("/services/contracts/create")
				.param("clientName", "Test Client")
				.param("contractType", "Standard")
				.param("startDate", "2024-05-01T12:00")
				.param("endDate", "2024-12-01T12:00")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("servicePrice", "100"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "The shop is closed"));
	}

	@Test
	void createContractOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/contracts/create")
				.param("clientName", "Test Client")
				.param("contractType", "One-Time")
				.param("startDate", "2042-05-01T12:00")
				.param("endDate", "2042-12-01T12:00")
				.param("address", "123 Test Street")
				.param("phone", "invalid-phone")
				.param("notes", "Test notes")
				.param("servicePrice", "100"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "Invalid phone number format"));
	}

	@Test
	void createEventOrderShouldCreateEventOrder() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2042-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "123456789")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"));
	}

	@Test
	void createEventOrderShouldReturnErrorWhenShopIsClosed() throws Exception {
		// Mock the clockService to return false for isOpen()
		when(clockService.isOpen()).thenReturn(false);

		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2042-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "123456789")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "The shop is closed"));
	}

	@Test
	void createEventOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2042-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "invalid-phone")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "Invalid phone number format"));
	}

	@Test
	void createEventOrderShouldReturnErrorForStartDateIsAfterEndDate() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2020-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "123456789")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "Event date and time cannot be in the past"));
	}

	@Test
	void createReservationOrderShouldCreateReservationOrder() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2042-05-01T12:00")
				.param("phone", "123456789")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"));
	}

	@Test
	void createReservationOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2042-05-01T12:00")
				.param("phone", "invalid-phone")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "Invalid phone number format"));
	}

	@Test
	void createReservationOrderShouldReturnErrorForStartDateIsAfterEndDate() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2020-05-01T12:00")
				.param("phone", "123456789")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services/create"))
			.andExpect(flash().attribute("error", "Reservation date and time cannot be in the past"));
	}

	@Test
	void shouldReturnContractOrderEditPage() throws Exception {
		mvc.perform(get("/services/contracts/edit/{id}", getFirstValidContractId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("contractOrder"))
			.andExpect(model().attributeExists("products"))
			.andExpect(view().name("services/edit/contractOrderEditForm"));
	}

	@Test
	void editContractOrderShouldUpdateDetails() throws Exception {
		mvc.perform(put("/services/contracts/edit/{id}", getFirstValidContractId())
				.param("clientName", "Test Client Name")
				.param("contractType", "Standard")
				.param("startDate", "2024-01-01T12:30")
				.param("endDate", "2024-12-01T12:30")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("paymentMethod", "CASH")
				.param("orderStatus", "OPEN")
				.param("servicePrice", "100")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection());
		// .andExpect(redirectedUrl("/services"));
	}

	@Test
	void editContractOrderShouldReturnErrorForNonExistingOrder() throws Exception {
		mvc.perform(put("/services/contracts/edit/{id}", UUID.randomUUID())
				.param("clientName", "Test Client Name")
				.param("contractType", "Standard")
				.param("startDate", "2024-01-01T12:30")
				.param("endDate", "2024-12-01T12:30")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("paymentMethod", "CASH")
				.param("orderStatus", "OPEN")
				.param("servicePrice", "100")
				.param("notes", "Test notes"))
			.andExpect(status().is(302));
	}

	@Test
	void editContractOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(put("/services/contracts/edit/{id}", getFirstValidContractId())
				.param("clientName", "Updated Client")
				.param("contractType", "one-time")
				.param("startDate", "2024-01-01T12:30")
				.param("endDate", "2025-01-01T12:30")
				.param("address", "Updated Address")
				.param("phone", "invalid-phone")
				.param("paymentMethod", "Cash")
				.param("orderStatus", "OPEN")
				.param("notes", "Updated notes")
				.param("servicePrice", "100"))
			.andExpect(status().is(302));

	}

	@Test
	void shouldReturnEventOrderEditPage() throws Exception {
		mvc.perform(get("/services/events/edit/{id}", getFirstValidEventId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("eventOrder"))
			.andExpect(model().attributeExists("products"))
			.andExpect(view().name("services/edit/eventOrderEditForm"));
	}

	@Test
	void editEventOrderShouldUpdateDetails() throws Exception {
		mvc.perform(put("/services/events/edit/{id}", getFirstValidEventId())
				.param("clientName", "Updated Client")
				.param("eventDate", "2024-01-01T12:30")
				.param("phone", "123456789")
				.param("deliveryAddress", "Updated Address")
				.param("paymentMethod", "Cash")
				.param("orderStatus", "OPEN")
				.param("notes", "Updated notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void editEventOrderShouldReturnErrorForNonExistingOrder() throws Exception {
		mvc.perform(put("/services/events/edit/{id}", UUID.randomUUID())
				.param("clientName", "Updated Client")
				.param("eventDate", "2024-01-01T12:30")
				.param("phone", "123456789")
				.param("deliveryAddress", "Updated Address")
				.param("paymentMethod", "Cash")
				.param("orderStatus", "OPEN")
				.param("notes", "Updated notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is(302));
	}

	@Test
	void editEventOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(put("/services/events/edit/{id}", getFirstValidEventId())
				.param("clientName", "Updated Client")
				.param("eventDate", "2024-05-01T12:30")
				.param("phone", "invalid-phone")
				.param("deliveryAddress", "Updated Address")
				.param("paymentMethod", "Cash")
				.param("orderStatus", "OPEN")
				.param("notes", "Updated notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is(302));
	}

	@Test
	void shouldReturnReservationOrderEditPage() throws Exception {
		mvc.perform(get("/services/reservations/edit/{id}", getFirstValidReservationId()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("reservationOrder"))
			.andExpect(model().attributeExists("products"))
			.andExpect(view().name("services/edit/reservationOrderEditForm"));
	}

	@Test
	void editReservationOrderShouldUpdateDetails() throws Exception {
		mvc.perform(put("/services/reservations/edit/{id}", getFirstValidReservationId())
				.param("clientName", "Updated Client")
				.param("paymentMethod", "Cash")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "123456789")
				.param("orderStatus", "OPEN")
				.param("reservationStatus", "IN_PROCESS")
				.param("notes", "Updated notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void editReservationOrderShouldReturnErrorForNonExistingOrder() throws Exception {
		mvc.perform(put("/services/reservations/edit/{id}", UUID.randomUUID())
				.param("clientName", "Updated Client")
				.param("paymentMethod", "Cash")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "123456789")
				.param("orderStatus", "OPEN")
				.param("reservationStatus", "IN_PROCESS")
				.param("notes", "Updated notes"))
			.andExpect(status().is(302));
	}

	@Test
	void editReservationOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(put("/services/reservations/edit/{id}", getFirstValidReservationId())
				.param("clientName", "Updated Client")
				.param("paymentMethod", "Cash")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "invalid-phone")
				.param("orderStatus", "OPEN")
				.param("reservationStatus", "IN_PROCESS")
				.param("notes", "Updated notes"))
			.andExpect(status().is(302));
	}

	@Test
	void getServicePageShouldReturnNotFoundForInvalidService() throws Exception {
		mvc.perform(get("/services/unknown-service"))
			.andExpect(status().isNotFound());
	}

	@SuppressWarnings("unchecked")
	private <T> T getFirstValidOrderByType(Class<T> orderClass, String type) throws Exception {
		return ((List<T>) Objects.requireNonNull(mvc.perform(get("/services"))
				.andReturn()
				.getModelAndView())
			.getModel()
			.get(type))
			.getFirst();
	}

	private Order.OrderIdentifier getFirstValidContractId() throws Exception {
		return getFirstValidOrderByType(ContractOrder.class, "contracts").getId();
	}

	private Order.OrderIdentifier getFirstValidEventId() throws Exception {
		return getFirstValidOrderByType(EventOrder.class, "events").getId();
	}

	private Order.OrderIdentifier getFirstValidReservationId() throws Exception {
		return getFirstValidOrderByType(ReservationOrder.class, "reservations").getId();
	}
}
