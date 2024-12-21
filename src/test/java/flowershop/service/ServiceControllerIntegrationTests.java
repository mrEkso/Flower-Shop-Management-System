package flowershop.service;

import flowershop.services.ContractOrder;
import flowershop.services.EventOrder;
import flowershop.services.ReservationOrder;
import flowershop.services.ServiceController;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

	@Test
	void showsAllServices() throws Exception {
		mvc.perform(get("/services"))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("contracts"))
			.andExpect(model().attributeExists("events"))
			.andExpect(model().attributeExists("reservations"))
			.andExpect(view().name("services/services"));
	}

//	@Test
//	void shouldGetContractOrderById() throws Exception {
//		mvc.perform(get("/services/contracts/{id}", getFirstValidContractId()))
//			.andExpect(status().isOk())
//			.andExpect(model().attributeExists("reservation"))
//			.andExpect(view().name("services/reservation_details"));
//	}
//
//	@Test
//	void shouldGetEventOrderById() throws Exception {
//		mvc.perform(get("/services/reservations/{id}", getFirstValidEventId()))
//			.andExpect(status().isOk())
//			.andExpect(model().attributeExists("reservation"))
//			.andExpect(view().name("services/reservation_details"));
//	}
//
//	@Test
//	void shouldGetReservationOrderById() throws Exception {
//		mvc.perform(get("/services/reservations/{id}", getFirstValidReservationId()))
//			.andExpect(status().isOk())
//			.andExpect(model().attributeExists("reservation"))
//			.andExpect(view().name("services/reservation_details"));
//	}

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
			.andExpect(model().attribute("index", 1))
			.andExpect(model().attributeExists("products"));
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
	void createContractOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/contracts/create")
				.param("clientName", "Test Client")
				.param("contractType", "Standard")
				.param("startDate", "2024-05-01T12:00")
				.param("endDate", "2024-12-01T12:00")
				.param("address", "123 Test Street")
				.param("phone", "invalid-phone")
				.param("notes", "Test notes")
				.param("servicePrice", "100"))
			.andExpect(status().is(302));
	}

	@Test
	void createEventOrderShouldCreateEventOrder() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2024-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "123456789")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void createEventOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2024-05-01T12:00")
				.param("address", "Test Event Address")
				.param("phone", "invalid-phone")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes")
				.param("deliveryPrice", "50"))
			.andExpect(status().is(302));
	}

	@Test
	void createReservationOrderShouldCreateReservationOrder() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "123456789")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void createReservationOrderShouldReturnErrorForInvalidPhoneNumber() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "invalid-phone")
				.param("notes", "Test notes"))
			.andExpect(status().is(302));
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
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
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
}
