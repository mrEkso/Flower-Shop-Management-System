package flowershop.controllers;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.embedded.Pricing;
import flowershop.models.product.Flower;

@Controller
public class FlowerShopController {

    private final ProductCatalog productCatalog;

    FlowerShopController(ProductCatalog productCatalog){
        this.productCatalog = productCatalog;
    }

    @GetMapping("/sell")
    public String sell(Model model){
        model.addAttribute("flowers", productCatalog.findAll().filter(product -> product instanceof Flower).toList());
        
        List<String> typeList = new ArrayList<>();
        typeList.addAll(List.of("Red", "Yellow", "White"));
        model.addAttribute("typeList", typeList);

        String selectedItem = "";
        model.addAttribute("selectedItem", selectedItem);

        return "sell";
    }
}
