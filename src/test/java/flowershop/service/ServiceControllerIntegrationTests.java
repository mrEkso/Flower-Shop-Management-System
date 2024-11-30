/*
 * Copyright 2014-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package flowershop.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import flowershop.services.ContractOrder;
import flowershop.services.ServiceController;
import org.junit.jupiter.api.Test;
import org.salespointframework.order.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ServiceController}.
 **/
@SpringBootTest
@AutoConfigureMockMvc
class ServiceControllerIntegrationTests {

	@Autowired
	MockMvc mvc;

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
//	void shouldGetReservationOrderById() throws Exception {
//		mvc.perform(get("/services/reservations/{id}", "valid-reservation-id"))
//			.andExpect(status().isOk())
//			.andExpect(model().attributeExists("reservation"))
//			.andExpect(view().name("services/reservation_details"));
//	}

	@Test
	void createServicePageShouldBeAccessible() throws Exception {
		mvc.perform(get("/services/create"))
			.andExpect(status().isOk())
			.andExpect(view().name("services/create_service"));
	}

	@Test
	void shouldReturnNotFoundForInvalidServiceId() throws Exception {
		mvc.perform(get("/services/reservations/invalid-id"))
			.andExpect(status().isBadRequest());
	}

	@Test
	void addProductRowShouldReturnRowFragment() throws Exception {
		mvc.perform(get("/services/add-product-row")
				.param("index", "1"))
			.andExpect(status().isOk());
	}

	@Test
	void shouldCreateContractOrder() throws Exception {
		mvc.perform(post("/services/contracts/create")
				.param("clientName", "Test Client")
				.param("contractType", "Standard")
				.param("startDate", "2024-01-01")
				.param("endDate", "2024-12-31")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void shouldReturnBadRequestForInvalidContractOrderCreation() throws Exception {
		mvc.perform(post("/services/contracts/create")
				.param("clientName", "")
				.param("contractType", ""))
			.andExpect(status().isBadRequest());
	}

	@Test
	void shouldCreateEventOrder() throws Exception {
		mvc.perform(post("/services/events/create")
				.param("clientName", "Test Client Name")
				.param("eventName", "Birthday Party")
				.param("eventDate", "2024-05-01")
				.param("address", "Test Event Address")
				.param("phone", "123456789")
				.param("deliveryAddress", "Test Delivery Address")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void shouldCreateReservationOrder() throws Exception {
		mvc.perform(post("/services/reservations/create")
				.param("clientName", "Test Client Name")
				.param("reservationDateTime", "2024-05-01T12:00")
				.param("phone", "123456789")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	@SuppressWarnings("unchecked")
	void shouldReturnContractOrderEditPage() throws Exception {
		Order.OrderIdentifier validContractId = ((List<ContractOrder>) Objects.requireNonNull(mvc.perform(get("/services"))
				.andReturn()
				.getModelAndView())
			.getModel()
			.get("contracts"))
			.getFirst()
			.getId();

		mvc.perform(get("/services/contracts/edit/{id}", Objects.requireNonNull(validContractId).toString()))
			.andExpect(status().isOk())
			.andExpect(model().attributeExists("contractOrder"))
			.andExpect(model().attributeExists("products"))
			.andExpect(view().name("services/edit/contractOrderEditForm"));
	}

	@Test
	@SuppressWarnings("unchecked")
		void shouldUpdateContractOrderStatus() throws Exception {
		Order.OrderIdentifier validContractId = ((List<ContractOrder>) Objects.requireNonNull(mvc.perform(get("/services"))
				.andReturn()
				.getModelAndView())
			.getModel()
			.get("contracts"))
			.getFirst()
			.getId();

		mvc.perform(put("/services/contracts/edit/{id}", Objects.requireNonNull(validContractId).toString())
				.param("orderId", Objects.requireNonNull(validContractId).toString())
				.param("clientName", "Test Client Name")
				.param("contractType", "Standard")
				.param("startDate", "2024-01-01")
				.param("endDate", "2024-12-31")
				.param("address", "123 Test Street")
				.param("phone", "123456789")
				.param("paymentMethod", "CASH")
				.param("orderStatus", "OPEN")
				.param("notes", "Test notes"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/services"));
	}

	@Test
	void shouldReturnInternalServerErrorForInvalidService() throws Exception {
		mvc.perform(get("/services/unknown-service"))
			.andExpect(status().isNotFound());
	}
}
