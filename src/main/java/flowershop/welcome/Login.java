package flowershop.welcome;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class Login {

	@GetMapping("/login")
	public String login() {
		System.out.println("------------------------ Login -----------------------");
		return "login";
	}

	@PostMapping("/login")
	public String loginSubmit(@RequestParam("username") String username,
							  @RequestParam("password") String password,
							  Model model) {
		username = username.trim();
		password = password.trim();
		System.out.println("---------------------"+username+"----------------"+password+"---------------------");
		if ((username.equals("Frau Floris") && password.equals("password123")) ||
			(username.equals("Nichte") && password.equals("0000"))) {
			return "redirect:/inventory";
		} else {
			model.addAttribute("error", "Invalid username or password");
			return "login";
		}
	}

	/*@GetMapping("/my-inventory")
	public String inventory() {
		return "inventory";
	}*/

}
