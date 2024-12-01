package flowershop.sales;

import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;

import flowershop.AbstractIntegrationTests;

public class SalesControllerIntegrationTests extends AbstractIntegrationTests {
    @Autowired SalesController controller;

    @Test
	@SuppressWarnings("unchecked")
	public void sampleControllerIntegrationTest() {

		Model model = new ExtendedModelMap();

		String sellView = controller.sellCatalog(model, null, null, null);
		assertThat(sellView).isEqualTo("sales/sell");
		Iterable<Object> sellObject = (Iterable<Object>) model.asMap().get("products");
		assertThat(sellObject).hasSize(8);

        String buyView = controller.buyCatalog(model, null, null, null);
		assertThat(buyView).isEqualTo("sales/buy");
		Iterable<Object> buyObject = (Iterable<Object>) model.asMap().get("flowers");
		assertThat(buyObject).hasSize(11);

	}
}
