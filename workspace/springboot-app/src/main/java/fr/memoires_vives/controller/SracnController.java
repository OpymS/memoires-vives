package fr.memoires_vives.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import fr.memoires_vives.bll.SracnContactService;
import fr.memoires_vives.bo.SracnContactSubject;
import fr.memoires_vives.dto.SracnContactForm;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/sracn")
public class SracnController {
	
	private final SracnContactService sracnContactService;
	
	public SracnController(SracnContactService sracnContactService) {
		this.sracnContactService = sracnContactService;
	}

	@GetMapping
	public String home(Model model) {

		model.addAttribute("groups", "toto");
		model.addAttribute("news", "tata");

		return "sracn/index";
	}

	@GetMapping("/contact")
	public String getContact(@RequestParam(name = "subject", required = false) SracnContactSubject subject, Model model) {
		SracnContactForm form = new SracnContactForm();
		form.setSubject(subject);
		
		model.addAttribute("contactForm", form);
		model.addAttribute("subjects", SracnContactSubject.values());
		return "sracn/contact";
	}

	@PostMapping("/contact")
    public String submitContact(
            @Valid @ModelAttribute("contactForm") SracnContactForm form,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("subjects", SracnContactSubject.values());
            return "sracn/contact";
        }

        sracnContactService.send(form);

        return "redirect:/sracn/contact?success";
    }
	
}
