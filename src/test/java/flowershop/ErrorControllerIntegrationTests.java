package flowershop;

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

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for {@link ErrorController}.
 **/
@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "boss", roles = {"BOSS", "USER"})
class ErrorControllerIntegrationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void accessDeniedShouldReturn403View() throws Exception {
		mockMvc.perform(get("/403"))
			.andExpect(status().isOk())
			.andExpect(view().name("403"));
	}

	@Test
	void notFoundShouldReturn404View() throws Exception {
		mockMvc.perform(get("/404"))
			.andExpect(status().isOk())
			.andExpect(view().name("404"));
	}
}
