package flowershop.inventory;

import org.junit.jupiter.api.Test;
import org.springframework.ui.Model;
import static org.assertj.core.api.Assertions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ExtendedModelMap;
import flowershop.AbstractIntegrationTests;

public class InventoryControllerIntegrationTests extends AbstractIntegrationTests {

	@Autowired
	InventoryController inventoryController;

	@Test
	public void testInventoryMode() {
		Model model = new ExtendedModelMap();

		String viewName = inventoryController.inventoryMode(null, "all", model);

		assertThat(viewName).isEqualTo("inventory");

		@SuppressWarnings("unchecked")
		Iterable<Object> products = (Iterable<Object>) model.asMap().get("products");
		assertThat(products).hasSize(13);
	}
}
