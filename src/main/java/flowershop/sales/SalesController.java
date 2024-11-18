package flowershop.sales;

import flowershop.product.Flower;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import flowershop.product.ProductCatalog;
import jakarta.servlet.http.HttpServletRequest;
@SessionAttributes("basket")
@Controller
public class SalesController {

    private final ProductCatalog productCatalog;
    SalesController(ProductCatalog productCatalog){
        this.productCatalog = productCatalog;
    }

    @ModelAttribute("basket")
    public List<BasketItem> createBasket() {
        return new ArrayList<>();
    }

    @GetMapping("/sell")
    public String sell(Model model, 
        @RequestParam(required = false) String filterItem, 
        @RequestParam(required = false) String searchInput,
        @ModelAttribute("basket") List<BasketItem> basket) {
    
        List<Flower> flowers = productCatalog.findAll()
            .filter(product -> product instanceof Flower)
            .map(product -> (Flower) product)
            .toList();
        
        System.out.println(basket);

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
        model.addAttribute("basket", basket);
    
        return "sell";
    }
    
    @GetMapping("/buy")
    public String buy(Model model,
        @RequestParam(required = false) String filterItem, 
        @RequestParam(required = false) String searchInput,
        @ModelAttribute("basket") List<BasketItem> basket) {
    
            List<Flower> flowers = productCatalog.findAll()
                .filter(product -> product instanceof Flower)
                .map(product -> (Flower) product)
                .toList();
            
            System.out.println(basket);
    
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
            model.addAttribute("basket", basket);

        return "buy";
    }

    @PostMapping("/add-to-basket")
    public String addToBasket(
        Model model,
        @RequestParam String productName,
        @RequestParam(required = false) String redirectPage,
        @ModelAttribute("basket") List<BasketItem> basket
    ) {
        Flower flower = productCatalog.findAll()
            .filter(product -> product instanceof Flower)
            .map(product -> (Flower) product)
            .filter(f -> f.getName().equalsIgnoreCase(productName))
            .stream()
            .findFirst()
            .orElse(null);

        Optional<BasketItem> basketItem = basket.stream()
        .filter(item -> item.getProduct().equals(flower))
        .findFirst();

        if (flower != null) {
            if(basketItem.isPresent()){
                basketItem.get().increaseQuantity();
            } else {
                basket.add(new BasketItem(flower, 1));
            }
        }

        model.addAttribute("basket", basket);

        return "redirect:/" + redirectPage;
    }

    @PostMapping("/remove-from-basket")
    public String removeFromBasket(
        @RequestParam String productName,
        @ModelAttribute("basket") List<BasketItem> basket,
        HttpServletRequest request
    ) {
        String referer = request.getHeader("Referer").split("http://localhost:8080/")[1];
        basket.removeIf(b -> b.getProduct().getName().equalsIgnoreCase(productName));
        
        return "redirect:/" + (referer == null? "/sell": referer);
    }

    @GetMapping("/")
    public String index(){
        return "redirect:sell";
    }

}
 