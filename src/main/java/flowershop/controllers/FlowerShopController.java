package flowershop.controllers;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import flowershop.catalogs.ProductCatalog;
import flowershop.models.product.Flower;

@Controller
public class FlowerShopController {

    private final ProductCatalog productCatalog;
    FlowerShopController(ProductCatalog productCatalog){
        this.productCatalog = productCatalog;
    }

    @GetMapping("/sell")
    public String sell(Model model){

        List<Flower> flowers = productCatalog.findAll()
        .filter(product -> product instanceof Flower)
        .map(product -> (Flower) product).toList();

        Set<String> colors = flowers.stream()
        .map(Flower::getColor)
        .collect(Collectors.toSet());

        model.addAttribute("flowers", flowers);
        
        List<String> typeList = new ArrayList<>();
        typeList.addAll(colors);
        model.addAttribute("typeList", typeList);

        String selectedItem = "";
        model.addAttribute("selectedItem", selectedItem);

        return "sell";
    }

    @GetMapping("/buy")
    public String buy(Model model){
        
        List<Flower> flowers = productCatalog.findAll()
        .filter(product -> product instanceof Flower)
        .map(product -> (Flower) product).toList();

        Set<String> colors = flowers.stream()
        .map(Flower::getColor)
        .collect(Collectors.toSet());

        model.addAttribute("flowers", flowers);
        
        List<String> typeList = new ArrayList<>();
        typeList.addAll(colors);
        model.addAttribute("typeList", typeList);

        String selectedItem = "";
        model.addAttribute("selectedItem", selectedItem);

        return "buy";
    }
}
