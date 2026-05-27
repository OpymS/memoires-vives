package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;


@Controller
@RequestMapping("/sracn")
public class SracnController {

	
	@GetMapping
    public String home(Model model) {

        model.addAttribute("groups", "toto");
        model.addAttribute("news", "tata");

        return "sracn/index";
    }
	
	@GetMapping("/contact")
	public String getContact() {
		return "sracn/contact";
	}
	
}
