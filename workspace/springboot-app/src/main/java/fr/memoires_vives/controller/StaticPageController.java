package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class StaticPageController {

	@GetMapping("/about")
	public String showAboutPage() {
		return "about";
	}

	@GetMapping("/legal-notices")
	public String showLegalNotices() {
		return "legal-notices";
	}

	@GetMapping("/privacy-policy")
	public String showPrivacyPolicy() {
		return "privacy-policy";
	}

	@GetMapping("/conditions")
	public String showConditions() {
		return "conditions";
	}
}
