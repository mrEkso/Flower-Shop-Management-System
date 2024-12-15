package flowershop.inventory;

import flowershop.AbstractIntegrationTests;
import flowershop.product.Bouquet;
import flowershop.product.Flower;
import flowershop.product.Pricing;
import flowershop.product.ProductService;
import org.javamoney.moneta.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.ui.Model;
import org.springframework.ui.ExtendedModelMap;
import static org.assertj.core.api.Assertions.*;

import java.util.Map;
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
  }

  @Test
  public void testShowDeletedProducts() {
    Model model = new ExtendedModelMap();

    String viewName = inventoryController.showDeletedProducts(model);

    assertThat(viewName).isEqualTo("inventory");
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
  }

  @Test
  public void testAddFlowerToBouquetWithInvalidFlowerID() {
    Model model = new ExtendedModelMap();
    UUID invalidFlowerId = UUID.randomUUID();


    String viewName = inventoryController.addFlowerToBouquet(invalidFlowerId, 5, model);

    assertThat(viewName).isEqualTo("inventory");
  }

  @Test
  public void testDeleteProductWithInsufficientQuantity() {
    String productName = "Rose";
    int excessiveQuantity = 999;

    String viewName = inventoryController.deleteProduct(productName, excessiveQuantity);

    assertThat(viewName).isEqualTo("redirect:/inventory");
  }

  @Test
  public void testShowChooseModalWithNonFlowerProduct() {
    Model model = new ExtendedModelMap();
    UUID nonFlowerProductId = UUID.randomUUID();

    productService.addBouquet(new Bouquet("BouquetTest", Map.of(), Money.of(10, "EUR"), 1));

    String viewName = inventoryController.showChooseModal(nonFlowerProductId, model);

    assertThat(viewName).isEqualTo("inventory");
    assertThat(model.asMap().get("error")).isEqualTo("Product not found.");
  }

  @Test
  public void testCreateCustomBouquetWithoutFlowers() {
    Model model = new ExtendedModelMap();
    String bouquetName = "Empty Bouquet";

    String viewName = inventoryController.createCustomBouquet(bouquetName, model);

    assertThat(viewName).isEqualTo("inventory");
  }

  @Test
  public void testShowDeletedProductsWithNoDeletedItems() {
    Model model = new ExtendedModelMap();

    String viewName = inventoryController.showDeletedProducts(model);

    assertThat(viewName).isEqualTo("inventory");
    assertThat(model.asMap().get("deletedProducts")).isNotNull();
    assertThat(model.asMap().get("totalLossSum")).isEqualTo(0.0);
  }

  @Test
  public void testAddFlowerToBouquetExceedingQuantity() {
    Model model = new ExtendedModelMap();
    UUID flowerId = UUID.randomUUID();
    Pricing pricing = new Pricing(Money.of(1.5, "EUR"), Money.of(2, "EUR"));
    Flower testFlower = new Flower("Tulip", pricing, "Red", 5);
    productService.addFlower(testFlower);

    String viewName = inventoryController.addFlowerToBouquet(flowerId, 10, model);

    assertThat(viewName).isEqualTo("inventory");
  }

  @Test
  public void testDeleteProductAllStock() {
    String productName = "Rose";
    Pricing pricing = new Pricing(Money.of(1.5, "EUR"), Money.of(2, "EUR"));
    Flower flower = new Flower("Tulip", pricing, "Red", 5);
    productService.addFlower(flower);

    String viewName = inventoryController.deleteProduct(productName, 5);

    assertThat(viewName).isEqualTo("redirect:/inventory");
  }

  @Test
  public void testDeleteNonExistentProduct() {
    String viewName = inventoryController.deleteProduct("NonExistent", 1);

    assertThat(viewName).isEqualTo("redirect:/inventory");
  }

}
