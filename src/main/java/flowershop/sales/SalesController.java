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
    public String sell(Model model, @RequestParam(value = "color", required = false) String selectedItem){

        List<Flower> flowers = productCatalog.findAll()
        .filter(product -> product instanceof Flower)
        .map(product -> (Flower) product).toList();

        if (selectedItem != null && !selectedItem.isEmpty()) {
            flowers = flowers.stream()
                    .filter(flower -> flower.getColor().equalsIgnoreCase(selectedItem))
                    .collect(Collectors.toList());
        }

        Set<String> colors = flowers.stream()
        .map(Flower::getColor)
        .collect(Collectors.toSet());

        
        model.addAttribute("typeList", colors);
        model.addAttribute("selectedItem", selectedItem);
        model.addAttribute("flowers", flowers);

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

    @GetMapping("/")
    public String index(){
        return "redirect:sell";
    }

}
 