package flowershop.finances;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import javax.money.MonetaryAmount;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
public class FinanceControllerWebIntegrationTests {

	@Autowired private MockMvc mvc;


	@Test
	@WithMockUser(roles = "EMPLOYEE")
	void unauthorizedAccessByEmployee_ReturnsForbidden() throws Exception {
		mvc.perform(get("/filterDates")
				.param("date1", "2023-01-01")
				.param("date2", "2023-12-31"))
			.andExpect(status().isForbidden());
	}
	@Test
	@WithMockUser(roles = "BOSS")
	void filterDates_ReturnsFinacesForValid() throws Exception {
		mvc.perform(get("/filterDates")
				.param("date1", "2023-01-01")
				.param("date2", "2023-12-31"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"))
			.andExpect(model().attributeExists("transactions"));

	}
	@Test
	@WithMockUser(roles = "BOSS")
	void resetDates_ClearsFiltersAndReturnsFinances() throws Exception {
		mvc.perform(get("/resetDates"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"))
			.andExpect(model().attributeExists("transactions"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void resetCategory_ClearsCategoryFiltersAndReturnsFinances() throws Exception {
		mvc.perform(get("/resetCategory"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"))
			.andExpect(model().attributeExists("transactions"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void filterCategories_ValidCategory_ReturnsUpdatedFinances() throws Exception {
		mvc.perform(get("/filterCategories")
				.param("filter", "income"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"))
			.andExpect(model().attributeExists("transactions"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void filterCategories_InvalidCategory_DoesNotFail() throws Exception {
		mvc.perform(get("/filterCategories")
				.param("filter", "invalid-category"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"))
			.andExpect(model().attributeExists("transactions"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void filterDates_InvalidDates_ReturnsFinancesWithoutFilter() throws Exception {
		mvc.perform(get("/filterDates")
				.param("date1", "2023-12-31")
				.param("date2", "2023-01-01"))
			.andExpect(status().isOk())
			.andExpect(view().name("finances"));
	}


	@Test
	@WithMockUser(roles = "BOSS")
	void dayReport_FutureDate_ReturnsBadRequest() throws Exception {
		mvc.perform(get("/dayReport")
				.param("day", LocalDate.now().plusDays(1).toString()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void monthReport_InvalidMonthFormat_ReturnsBadRequest() throws Exception {
		mvc.perform(get("/monthReport")
				.param("month", "invalid_format"))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void askForDay_ReturnsAskForDayView() throws Exception {
		mvc.perform(get("/askForDay"))
			.andExpect(status().isOk())
			.andExpect(view().name("finance/askForDay"));
	}

	@Test
	@WithMockUser(roles = "BOSS")
	void askForMonth_ReturnsAskForMonthView() throws Exception {
		mvc.perform(get("/askForMonth"))
			.andExpect(status().isOk())
			.andExpect(view().name("finance/askForMonth"));
	}
	//Bad bcz is empty
	@Test
	@WithMockUser(roles ="BOSS")
	void dayReportPDF_ReturnsBadRequest() throws Exception {
		mvc.perform(get("/dayReport")
			.param("day", LocalDate.now().toString()))
			.andExpect(status().isBadRequest());
		mvc.perform(get("/dayReport")
			.param("day", LocalDate.now().plusDays(100).toString()))
			.andExpect(status().isBadRequest());
	}

	@Test
	@WithMockUser(roles ="BOSS")
	void monthReportPDF_ReturnsBadRequest() throws Exception {
		mvc.perform(get("/monthReport")
				.param("month", YearMonth.now().toString()))
			.andExpect(status().isBadRequest());
		mvc.perform(get("/monthReport")
				.param("month", YearMonth.now().plusMonths(100).toString()))
			.andExpect(status().isBadRequest());
	}



	//TO-DO: add test cases for when pdfs are returned
}
