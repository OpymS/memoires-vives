package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;


@Controller
public class ErrorRedirectController {

	@GetMapping("/upload-error")
	public String handleUploadError(HttpServletRequest request, Model model) {
		
		String referer = request.getHeader("Referer");
		if (referer == null || referer.isBlank()) {
			referer = "/";
		}
		
		model.addAttribute("backUrl", referer);
		
		return "/error/413";
	}
	
}
