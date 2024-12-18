package flowershop.sales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "boss", roles = {"BOSS", "USER"})
public class SalesControllerWebIntegrationTests {
    @Autowired MockMvc mvc;
	@Autowired SalesController controller;

    @Test
	void sampleMvcIntegrationTest() throws Exception {

		mvc.perform(get("/sell")) //
				.andExpect(status().isOk()) //
				.andExpect(model().attributeExists("products")) //
				.andExpect(model().attribute("products", is(not(emptyIterable()))));

        mvc.perform(get("/buy")) //
				.andExpect(status().isOk()) //
				.andExpect(model().attributeExists("flowers")) //
				.andExpect(model().attribute("flowers", is(not(emptyIterable()))));
	}
}

