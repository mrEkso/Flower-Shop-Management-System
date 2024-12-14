package flowershop.inventory;

import flowershop.AbstractIntegrationTests;
import flowershop.product.Flower;
import flowershop.product.ProductService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.salespointframework.useraccount.UserAccountManagement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.*;
import java.util.UUID;

@WithMockUser(username = "boss", roles = {"BOSS", "USER"})
public class InventoryControllerIntegrationTests extends AbstractIntegrationTests {

	@Autowired
	private InventoryController inventoryController;

	@Autowired
	private ProductService productService;


	@Test
	public void testInventoryMode() {
		Model model = new ExtendedModelMap();

		String viewName = inventoryController.inventoryMode(null, "all", model);

		assertThat(viewName).isEqualTo("inventory");

		@SuppressWarnings("unchecked")
		Iterable<Object> products = (Iterable<Object>) model.asMap().get("products");
		assertThat(products).hasSize(13);
	}

	@Test
	public void testSearchFunctionality() {
		Model model = new ExtendedModelMap();

		String viewName = inventoryController.inventoryMode("Rose", "all", model);

		assertThat(viewName).isEqualTo("inventory");

		@SuppressWarnings("unchecked")
		Iterable<Object> products = (Iterable<Object>) model.asMap().get("products");

		assertThat(products).allMatch(product -> product.toString().toLowerCase().contains("rose"));
	}

	@Test
	public void testCreateBouquetMode() {
		Model model = new ExtendedModelMap();

		String viewName = inventoryController.createBouquetMode(model);

		assertThat(viewName).isEqualTo("inventory");

		assertThat(model.asMap().get("createBouquetMode")).isEqualTo(true);
		assertThat(model.asMap().get("showModal")).isEqualTo(false);
		assertThat(model.asMap().get("showDeletedModal")).isEqualTo(false);
		assertThat(model.asMap().get("products")).isNotNull();
	}

	@Test
	public void testInventoryModeWithFilter() {
		Model model = new ExtendedModelMap();

		String viewName = inventoryController.inventoryMode(null, "Flower", model);

		assertThat(viewName).isEqualTo("inventory");

		@SuppressWarnings("unchecked")
		Iterable<Object> products = (Iterable<Object>) model.asMap().get("products");

		assertThat(products).allMatch(product -> product.toString().contains("Flower"));
	}

	@Test
	public void testDeleteProduct() {
		String productName = "Rose";
		int quantity = 2;

		String viewName = inventoryController.deleteProduct(productName, quantity);

		assertThat(viewName).isEqualTo("redirect:/inventory");
		//TODO: FIX THIS!!!!
		// assertThat(inventoryController.deletedProducts).anyMatch(deletedProduct -> deletedProduct.getName().equals(productName));
	}

	@Test
	public void testShowDeletedProducts() {
		Model model = new ExtendedModelMap();

		//TODO: FIX THIS!!!
		// inventoryController.deletedProducts.add(new DeletedProduct("Rose", 2.5, 5, 12.5));

		// String viewName = inventoryController.showDeletedProducts(model);

		// assertThat(viewName).isEqualTo("inventory");
		// assertThat(model.asMap().get("deletedProducts")).isNotNull();
		// assertThat(model.asMap().get("totalLossSum")).isEqualTo(12.5);
	}

	@Test
	public void testCreateCustomBouquet() {
		Model model = new ExtendedModelMap();
		String bouquetName = "Spring Mix";

		UUID roseId = UUID.randomUUID();
		UUID tulipId = UUID.randomUUID();

		inventoryController.addFlowerToBouquet(roseId, 5, model);
		inventoryController.addFlowerToBouquet(tulipId, 7, model);

		String viewName = inventoryController.createCustomBouquet(bouquetName, model);

		assertThat(viewName).isEqualTo("inventory");
		// TODO: FIX THIS!!
		// assertThat(inventoryController.selectedFlowersForBouquet).isEmpty();
	}

}
