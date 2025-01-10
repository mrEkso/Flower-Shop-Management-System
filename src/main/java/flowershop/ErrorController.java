package flowershop;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ErrorController {
    @GetMapping("403")
	public String accessDenied() {
		return "403"; 
	}

	@GetMapping("404")
	public String notFound() {
		System.out.println("404 handler called");
		return "404"; 
	}
}
