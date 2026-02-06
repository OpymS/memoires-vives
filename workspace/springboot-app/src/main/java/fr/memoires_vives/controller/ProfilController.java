package fr.memoires_vives.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import fr.memoires_vives.bll.ActivationService;
import fr.memoires_vives.bll.MemoryUrlService;
import fr.memoires_vives.bll.UserService;
import fr.memoires_vives.bo.User;
import fr.memoires_vives.dto.MemoryView;
import fr.memoires_vives.exception.EntityNotFoundException;
import fr.memoires_vives.exception.InvalidTokenException;
import fr.memoires_vives.exception.ValidationException;
import fr.memoires_vives.mapper.MemoryViewMapper;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/profil")
public class ProfilController {
	private final UserService userService;
	private final ActivationService activationService;
	private final MemoryViewMapper memoryViewMapper;

	public ProfilController(UserService userService, ActivationService activationService, MemoryUrlService memoryUrlService, MemoryViewMapper memoryViewMapper) {
		this.userService = userService;
		this.activationService = activationService;
		this.memoryViewMapper = memoryViewMapper;
	}

	@GetMapping
	public String showProfilPage(@RequestParam(name = "userId", required = false) Long userId, Model model) {
		User userToDisplay = userService.getUserOrCurrent(userId);
		userToDisplay.setPassword(null);
		List<MemoryView> views = userToDisplay.getMemories().stream().map(memoryViewMapper::toView).toList();
		model.addAttribute("views", views);
		model.addAttribute("userToDisplay", userToDisplay);
		return "profil";
	}

	@GetMapping("/modify")
	public String showModifyProfilForm(@RequestParam(name = "userId", required = false) Long userId, Model model) {
		User user = (userService.isAdmin() && userId != null) ? userService.getUserById(userId)
				: userService.getCurrentUser();
		model.addAttribute("user", user);
		return "profil-modify";
	}

	@PostMapping("/modify")
	public String modifyProfil(@Valid @ModelAttribute("user") User updatedUser, BindingResult bindingResult,
			@RequestParam(name = "currentPassword", required = false) String currentPassword,
			@RequestParam(name = "image", required = false) MultipartFile fileImage,
			@RequestParam(name = "removeImage", defaultValue = "false") boolean removeImage) {
		if (bindingResult.hasErrors()) {
			return "profil-modify";
		}

		try {
			userService.updateProfile(updatedUser, currentPassword, fileImage, removeImage);
			return "redirect:/profil?userId=" + updatedUser.getUserId();
		} catch (ValidationException ve) {
			ve.getGlobalErrors().forEach(err -> {
				ObjectError error = new ObjectError("globalError", err);
				bindingResult.addError(error);
			});

			ve.getFieldErrors().forEach(err -> {
				FieldError error = new FieldError("user", err.getField(), null, false, null, null, err.getMessage());
				bindingResult.addError(error);
			});
			return "profil-modify";
		}
	}

	@GetMapping("/activation")
	public String activateUserWithToken(@RequestParam(name = "token") String token, Model model,
			RedirectAttributes redirectAttributes) {
		try {
			User activatedUser = activationService.activateUser(token);
			redirectAttributes.addFlashAttribute("message", "Votre compte a été activé. Vous pouvez vous connecter.");
			redirectAttributes.addFlashAttribute("loginUser", activatedUser.getPseudo());
		} catch (InvalidTokenException e) {
			model.addAttribute("message", e.getMessage());
			return "activation-error";
		}
		return "redirect:/login";
	}

	@GetMapping("/activation/resend")
	public String handleFormResendActivation(@RequestParam(name = "username") String username,
			RedirectAttributes redirectAttributes) {
		User user;

		try {
			user = userService.getUserByPseudo(username);
		} catch (EntityNotFoundException e) {
			redirectAttributes.addFlashAttribute("error", e.getMessage());
			return "redirect:/login";
		}

		if (user.isActivated()) {
			redirectAttributes.addFlashAttribute("error", "Ce compte est déjà activé.");
			return "redirect:/login";
		}

		activationService.regenerateTokenAndSendMail(user);

		redirectAttributes.addFlashAttribute("message", "Un email d’activation vient de vous être envoyé.");

		return "redirect:/login";
	}

}
