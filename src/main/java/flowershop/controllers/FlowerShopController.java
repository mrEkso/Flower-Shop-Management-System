package flowershop.controllers;

import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.javamoney.moneta.Money;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import flowershop.models.embedded.Pricing;
import flowershop.models.product.Flower;

@Controller
public class FlowerShopController {
    @GetMapping("/sell")
    public String sell(Model model){
        List<Flower> flowers = new ArrayList<>();
        flowers.addAll(List.of(
            new Flower("Rose", new Pricing(Money.of(2, "EUR") , Money.of(5, "EUR")), "Red"),
            new Flower("Lilac", new Pricing(Money.of(3, "EUR") , Money.of(9, "EUR")), "White"),
            new Flower("Iris", new Pricing(Money.of(1.3, "EUR") , Money.of(4.5, "EUR")), "Purple"))
            
        );
        model.addAttribute("flowers", flowers);
        return "sell";
    }
}
