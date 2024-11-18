package flowershop.sales;

import flowershop.product.Flower;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import flowershop.product.ProductCatalog;

@Controller
public class SalesController {

    private final ProductCatalog productCatalog;
    SalesController(ProductCatalog productCatalog){
        this.productCatalog = productCatalog;
    }

    @GetMapping("/sell")
    public String sell(Model model, 
        @RequestParam(required = false) String filterItem, 
        @RequestParam(required = false) String searchInput) {
    
        List<Flower> flowers = productCatalog.findAll()
            .filter(product -> product instanceof Flower)
            .map(product -> (Flower) product)
            .toList();
        
        // System.out.println(filterItem);

        if (filterItem != null && !filterItem.isEmpty()) {
            flowers = flowers.stream()
                    .filter(flower -> flower.getColor().equalsIgnoreCase(filterItem))
                    .toList();
        }
    
        if (searchInput != null && !searchInput.isEmpty()) {
            flowers = flowers.stream()
                    .filter(flower -> flower.getName().toLowerCase().contains(searchInput.toLowerCase()))
                    .toList();
        }

        Set<String> colors = productCatalog.findAll()
        .filter(product -> product instanceof Flower)
        .map(product -> (Flower) product)
        .toList().stream().map(Flower::getColor)
        .collect(Collectors.toSet());
    
        model.addAttribute("typeList", colors);
        model.addAttribute("filterItem", filterItem);
        model.addAttribute("searchInput", searchInput);
        model.addAttribute("flowers", flowers);
    
        return "sell";
    }
    
    @GetMapping("/buy")
    public String buy(Model model,
        @RequestParam(required = false) String filterItem, 
        @RequestParam(required = false) String searchInput) {

        List<Flower> flowers = productCatalog.findAll()
            .filter(product -> product instanceof Flower)
            .map(product -> (Flower) product)
            .toList();
        
        // System.out.println(filterItem);

        if (filterItem != null && !filterItem.isEmpty()) {
            flowers = flowers.stream()
                    .filter(flower -> flower.getColor().equalsIgnoreCase(filterItem))
                    .toList();
        }

        if (searchInput != null && !searchInput.isEmpty()) {
            flowers = flowers.stream()
                    .filter(flower -> flower.getName().toLowerCase().contains(searchInput.toLowerCase()))
                    .toList();
        }

        Set<String> colors = productCatalog.findAll()
        .filter(product -> product instanceof Flower)
        .map(product -> (Flower) product)
        .toList().stream().map(Flower::getColor)
        .collect(Collectors.toSet());

        model.addAttribute("typeList", colors);
        model.addAttribute("filterItem", filterItem);
        model.addAttribute("searchInput", searchInput);
        model.addAttribute("flowers", flowers);

        return "buy";
    }

    @GetMapping("/")
    public String index(){
        return "redirect:sell";
    }

}
 