package com.penguinwebstudio.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserController {
	
	@Value("${google.recaptcha.key.production.site}")
	private String recaptchaSiteKey;
	@Value("${google.recaptcha.key.production.secret}")
	private String recaptchaSecretKey;
	
	@GetMapping(path="/account/login")
	public String login(LoginForm loginForm, Model model) {
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		return "login";
	}
	
}
