package flowershop.inventory;

import flowershop.product.Bouquet;
import flowershop.product.Flower;
// import flowershop.product.Product;
import flowershop.product.ProductService;

import org.salespointframework.catalog.Product;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class InventoryController {

    private final List<Product> products; // FIXME: no need to store it here. Use ProductService where it is already handled!
    //private final List<Bouquet> bouquets = new ArrayList<>();
    private final List<DeletedProduct> deletedProducts = new ArrayList<>();
    private final List<Flower> selectedFlowersForBouquet = new ArrayList<>(); // Store selected flowers for the bouquet

    private final ProductService productService;
    private final DeletedProductService deletedProductService;
    private final InventoryService inventoryService;

    public InventoryController(ProductService productService, DeletedProductService deletedProductService, InventoryService inventoryService) {
        this.productService = productService;
        this.products = productService.getAllProducts();
        this.deletedProductService = deletedProductService;
        this.inventoryService = inventoryService;
    }

    @GetMapping("/inventory")
    public String inventoryMode(@RequestParam(required = false) String search,
                                @RequestParam(required = false, defaultValue = "all") String filter,
                                Model model) {
        List<Product> filteredProducts = products;

        if (search != null && !search.isEmpty()) {
            filteredProducts = productService.findProductsByName(search);
        }

        // FIXME it's not gonna work:
        if (!filter.equals("all")) {
            filteredProducts = productService.getAllProducts();
        }

        model.addAttribute("products", filteredProducts);
        model.addAttribute("createBouquetMode", false);
        model.addAttribute("showModal", false);
        model.addAttribute("showDeletedModal", false);
        return "inventory";
    }

    @GetMapping("/inventory/create-bouquet")
    public String createBouquetMode(Model model) {
        List<Flower> flowersOnly = productService.findAllFlowers().stream().toList();

        model.addAttribute("products", flowersOnly);
        model.addAttribute("createBouquetMode", true);
        model.addAttribute("showModal", false);
        model.addAttribute("showDeletedModal", false);
        return "inventory";
    }

    @PostMapping("/inventory/add-flower")
    public String addFlowerToBouquet(@RequestParam UUID flowerID,
                                     @RequestParam int chooseQuantity,
                                     Model model) {

        Flower selectedFlower = (Flower) productService.getFlowerById(flowerID).get();

        if (selectedFlower.getQuantity() >= chooseQuantity) {
            selectedFlower.setQuantity(selectedFlower.getQuantity() - chooseQuantity);

            selectedFlowersForBouquet.add(selectedFlower);
            model.addAttribute("success", "Flower added to bouquet.");
        } else {
            model.addAttribute("error", "Not enough stock or invalid quantity.");
        }

        model.addAttribute("createBouquetMode", true);
        model.addAttribute("products", products);
        model.addAttribute("selectedFlowersForBouquet", selectedFlowersForBouquet);
        return "inventory";
    }

    @PostMapping("/create-custom-bouquet")
    public String createCustomBouquet(@RequestParam String bouquetName,
                                      Model model) {
        if (bouquetName == null || selectedFlowersForBouquet.isEmpty() || bouquetName.isEmpty()) {
            model.addAttribute("error", "Bouquet name is required, and at least one flower must be added.");
        } else {
            // TODO: create HashMap<Flower, int> flowers for a new bouquet

            Product customBouquet = new Bouquet(bouquetName, );

            productService.addBouquet(customBouquet); // TODO: USE productService.addBouquet() !!!

            products.add(customBouquet);

            selectedFlowersForBouquet.clear();
            model.addAttribute("success", "Custom bouquet created successfully.");
        }

        model.addAttribute("createBouquetMode", false);
        model.addAttribute("products", products);
        return "inventory";
    }


    @GetMapping("/inventory/choose-flower")
    public String showChooseModal(@RequestParam UUID flowerID, Model model) {
        Optional<Flower> selectedFlower = productService.getFlowerById(flowerID);

        selectedFlower.ifPresent(flower -> {
            model.addAttribute("showChooseModal", true);
            model.addAttribute("selectedFlower", flower);
        });

        model.addAttribute("createBouquetMode", true);
        model.addAttribute("products", products);
        return "inventory";
    }

    @GetMapping("/inventory/delete")
    public String showDeleteModal(@RequestParam UUID productID,
                                  Model model) {
        Optional<Product> selectedProductOpt = productService.getProductById(productID);

        selectedProductOpt.ifPresent(product -> model.addAttribute("selectedProduct", product));

        model.addAttribute("showModal", true);
        model.addAttribute("createBouquetMode", false);
        model.addAttribute("products", products);
        return "inventory";
    }

    @PostMapping("/delete-product")
    public String deleteProduct(
            @RequestParam UUID productID,
            @RequestParam int deleteQuantity,
            Model model) {

        inventoryService.deleteProduct(productID, deletedProducts); // TODO: use inventoryService to hide complicated logic. Use this line as an example.

        return "redirect:/inventory";
    }

    @GetMapping("/inventory/deleted-products")
    public String showDeletedProducts(Model model) {

        double totalLossSum = deletedProductService.getTotalLossSum(deletedProducts); // Hidden this logic into the service

        model.addAttribute("deletedProducts", deletedProducts);
        model.addAttribute("totalLossSum", totalLossSum);
        model.addAttribute("showDeletedModal", !deletedProducts.isEmpty());

        model.addAttribute("createBouquetMode", false);
        model.addAttribute("showModal", false);
        model.addAttribute("products", products);
        return "inventory";
    }

}