package com.penguinwebstudio.building;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.google.gson.Gson;
import com.penguinwebstudio.utils.HttpRequests;
import com.penguinwebstudio.utils.RecaptchaResponse;

@Controller
public class BuildingController {

	@Value("${google.recaptcha.key.production.site}")
	private String recaptchaSiteKey = System.getenv().get("google.recaptcha.key.production.site");
	@Value("${google.recaptcha.key.production.secret}")
	private String recaptchaSecretKey = System.getenv().get("google.recaptcha.key.production.secret");
	
	private final BuildingService buildingService;
	
	@Autowired
	public BuildingController(BuildingService buildingService) {
		this.buildingService = buildingService;
	}
	
	public static boolean isAdmin() {
		List<SimpleGrantedAuthority> authorities = (List<SimpleGrantedAuthority>) SecurityContextHolder.getContext().getAuthentication().getAuthorities();
		for (SimpleGrantedAuthority authority : authorities) {
			if (authority.getAuthority().equals("ROLE_ADMIN")) {
				return true;
			}
		}
		return false;
	}
	
	@GetMapping(path="")
	public String home(Model model) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		return "home";
	}
	
	@ResponseBody
	@GetMapping(path="/all")
	public int getAll() {
		Page<Building> buildings = buildingService.getPagedBuildings(Pageable.unpaged());
		return buildings.getContent().size();
	}
	
	@ResponseBody
	@GetMapping(path="/buildings")
	public List<Building> getBuildings(Pageable pageable) {
		Page<Building> buildings = buildingService.getPagedBuildings(pageable);
		return buildings.getContent();
	}
	
	@GetMapping(path="/add")
	public String getAdd(AddForm addForm, Model model) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		addForm.setaAddress(new Date());
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		return "add";
	}
	
	@PostMapping(path="/add")
	public String postAdd(
			@Valid @ModelAttribute AddForm addForm, 
			BindingResult bindingResult, 
			Model model, 
			@RequestParam("aFile") MultipartFile imageFile,
			@NotBlank @Size(min=1, max=500) @Pattern(regexp="^[A-Za-z0-9\\-_]+$") @RequestParam(value = "g-recaptcha-response") String recaptcha,
			RedirectAttributes attr
	) {
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "add";
		}
		if (bindingResult.hasErrors() || !addForm.getaEmail().isEmpty()) {			
			List<FieldError> errors = bindingResult.getFieldErrors();
			model.addAttribute("errors", errors);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			return "add";
		} else if (
				(!imageFile.getContentType().equals("image/png") && 
						!imageFile.getContentType().equals("image/jpeg") && 
						!imageFile.getContentType().equals("image/gif") && 
						!imageFile.isEmpty()) || 
				(imageFile.getSize() > 1000000)
				) {
			List<FieldError> errors = bindingResult.getFieldErrors();
			model.addAttribute("errors", errors);
			model.addAttribute("imageFileError", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			return "add";
		} else {			
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + recaptcha;
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("add")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);		
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				model.addAttribute("currentDateTime", dateTimeFormat.format(date));
				return "add";
			}
			PendingAddition pendingAddition = new PendingAddition(
					addForm.getaPhone(),
					addForm.getaFirstName(),
					addForm.getaWebsite(),
					addForm.getaLastName(),
					addForm.getaCreated()
			);
			try {
				if (!imageFile.isEmpty()) {
					pendingAddition.setImage(imageFile.getInputStream().readAllBytes());
				} else {
					pendingAddition.setImage(null);
				}
			} catch (Exception e) {
				pendingAddition.setImage(null);
			}
			buildingService.addBuildingPending(pendingAddition, !imageFile.isEmpty());
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			attr.addFlashAttribute("successMessage", "Addition awaiting admin approval!");
			return "redirect:/additions";
		}
	}
	
	@GetMapping(path="/view/{buildingId}")
	public String getView(
			Model model,
			@Pattern(regexp="^[A-Za-z0-9]+$") @PathVariable String buildingId) {
		model.addAttribute("pending", false);
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
			model.addAttribute("loggedIn", false);
		} else {
			model.addAttribute("loggedIn", true);
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		Building building = buildingService.getBuildingById(Long.parseLong(buildingId)).orElse(null);
		if (building == null) {
			model.addAttribute("buildingNotFoundError", true);
		} else {
			model.addAttribute("buildingNotFoundError", false);
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = building.getLastUpdated();
			model.addAttribute("lastUpdated", dateTimeFormat.format(date));
		}
		model.addAttribute("building", building);
		model.addAttribute("notAdminError", false);
		return "view";
	}
	
	@GetMapping(path="/view/pending/{buildingId}")
	public String getViewPending(
			Model model,
			@Pattern(regexp="^[A-Za-z0-9]+$") @PathVariable String buildingId) {
		model.addAttribute("pending", true);
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
			model.addAttribute("loggedIn", false);
		} else {
			model.addAttribute("loggedIn", true);
		}
		if (!isAdmin()) {
			model.addAttribute("notAdminError", true);
			model.addAttribute("buildingNotFoundError", false);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "view";
		} else {
			model.addAttribute("notAdminError", false);
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		PendingAddition building = buildingService.getPendingBuildingById(Long.parseLong(buildingId));
		if (building == null) {
			model.addAttribute("buildingNotFoundError", true);
		} else {
			model.addAttribute("buildingNotFoundError", false);
			DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
			Date date = building.getLastUpdated();
			model.addAttribute("lastUpdated", dateTimeFormat.format(date));
		}
		model.addAttribute("building", building);
		return "view";
	}
	
	@GetMapping(path="/edit/{buildingId}")
	public String getEdit(
			AddForm addForm, 
			Model model,
			@Pattern(regexp="^[A-Za-z0-9]+$") @PathVariable String buildingId) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		addForm.setaAddress(new Date());
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		Building building = buildingService.getBuildingById(Long.parseLong(buildingId)).orElse(null);
		if (building == null) {
			model.addAttribute("buildingNotFoundError", true);
		} else {
			model.addAttribute("buildingNotFoundError", false);
			addForm.setaPhone(building.getName());
			addForm.setaFirstName(building.getOccupancy());
			addForm.setaWebsite(building.getType());
			addForm.setaLastName(building.getTiles());
			addForm.setaCreated(building.getStyle());
			addForm.setaAddress(new Date());
			model.addAttribute("building", building);
		}
		return "edit";
	}
	
	@PostMapping(path="/edit/{buildingId}")
	public String postEdit(
			@Valid @ModelAttribute AddForm addForm, 
			BindingResult bindingResult, 
			Model model, 
			@RequestParam("aFile") MultipartFile imageFile,
			@NotBlank @Size(min=1, max=500) @Pattern(regexp="^[A-Za-z0-9\\-_]+$") @RequestParam(value = "g-recaptcha-response") String recaptcha,
			@Pattern(regexp="^[A-Za-z0-9]+$") @PathVariable String buildingId,
			RedirectAttributes attr
	) {
		Building building = buildingService.getBuildingById(Long.parseLong(buildingId)).orElse(null);
		if (building == null) {
			model.addAttribute("buildingNotFoundError", true);
		} else {
			model.addAttribute("buildingNotFoundError", false);
			model.addAttribute("building", building);
		}
		DateFormat dateTimeFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "edit";
		}
		if (bindingResult.hasErrors() || !addForm.getaEmail().isEmpty()) {			
			List<FieldError> errors = bindingResult.getFieldErrors();
			model.addAttribute("errors", errors);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			return "edit";
		} else if (
				(!imageFile.getContentType().equals("image/png") && 
						!imageFile.getContentType().equals("image/jpeg") && 
						!imageFile.getContentType().equals("image/gif") && 
						!imageFile.isEmpty()) || 
				(imageFile.getSize() > 1000000)
				) {
			List<FieldError> errors = bindingResult.getFieldErrors();
			model.addAttribute("errors", errors);
			model.addAttribute("imageFileError", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("currentDateTime", dateTimeFormat.format(date));
			return "edit";
		} else {			
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + recaptcha;
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("edit")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);		
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				model.addAttribute("currentDateTime", dateTimeFormat.format(date));
				return "edit";
			}
			
			PendingAddition pendingAddition = new PendingAddition(
					addForm.getaPhone(),
					addForm.getaFirstName(),
					addForm.getaWebsite(),
					addForm.getaLastName(),
					addForm.getaCreated()
			);
			try {
				if (!imageFile.isEmpty()) {
					pendingAddition.setImage(imageFile.getInputStream().readAllBytes());
				} else {
					pendingAddition.setImage(null);
				}
			} catch (IOException e) {
				pendingAddition.setImage(null);
			}
			buildingService.addBuildingPending(pendingAddition, !imageFile.isEmpty());
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			attr.addFlashAttribute("successMessage", "Edit awaiting admin approval!");
			return "redirect:/additions";
		}
	}
	
	@GetMapping(path="/additions")
	public String getAddition(Model model) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		List<PendingAddition> buildings = buildingService.getAllPendingBuildings();
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("buildings", buildings);
		return "additions";
	}
	
	@PostMapping(path="/additions")
	public String postAddition(
			@Valid @ModelAttribute AdditionForm additionForm,
			BindingResult bindingResult,
			Model model, 
			RedirectAttributes attr
	) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser") || !isAdmin()) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "additions";
		}
		List<PendingAddition> buildings = buildingService.getAllPendingBuildings();
		model.addAttribute("buildings", buildings);
		boolean success = false;
		if (bindingResult.hasErrors() || !additionForm.getaEmail().isEmpty()) {
			model.addAttribute("errors", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			return "additions";
		} else {
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + additionForm.getRecaptcha();
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("addition")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				return "additions";
			}
			success = buildingService.setBuildingAddition(Long.parseLong(additionForm.getBuildingId()));
		}
		if (success) {
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			attr.addFlashAttribute("successMessage", "Building added successfully!");
			return "redirect:/";
		} else {
			model.addAttribute("additionError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "additions";
		}

	}
	
	@PostMapping(path="/additions/remove")
	public String postRemoveAddition(
			@Valid @ModelAttribute RemoveAdditionForm removeAdditionForm,
			BindingResult bindingResult,
			Model model, 
			RedirectAttributes attr
	) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser") || !isAdmin()) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "additions";
		}
		List<PendingAddition> buildings = buildingService.getAllPendingBuildings();
		model.addAttribute("buildings", buildings);
		if (bindingResult.hasErrors() || !removeAdditionForm.getaPhone().isEmpty()) {
			model.addAttribute("errors", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			return "additions";
		} else {
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + removeAdditionForm.getRecaptcha();
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("removeAddition")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				return "additions";
			}
			buildingService.unsetBuildingAddition(Long.parseLong(removeAdditionForm.getBuildingId()));
			buildings = buildingService.getAllPendingBuildings();
			model.addAttribute("buildings", buildings);
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("errors", null);
		attr.addFlashAttribute("successMessage", "Addition removed successfully!");
		return "redirect:/additions";
	}
	
	@GetMapping(path="/delete")
	public String getDelete(Model model) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		List<Building> buildings = buildingService.getAllBuildings();
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("buildings", buildings);
		return "delete";
	}
	
	@PostMapping(path="/delete")
	public String postDelete(
			@Valid @ModelAttribute DeleteForm deleteForm,
			BindingResult bindingResult,
			Model model, 
			RedirectAttributes attr
	) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "delete";
		}
		List<Building> buildings = buildingService.getAllBuildings();
		model.addAttribute("buildings", buildings);
		if (bindingResult.hasErrors() || !deleteForm.getdEmail().isEmpty()) {
			model.addAttribute("errors", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			return "delete";
		} else {
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + deleteForm.getRecaptcha();
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("delete")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				return "delete";
			}
			buildingService.setBuildingDeletion(Long.parseLong(deleteForm.getBuildingId()));
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("errors", null);
		attr.addFlashAttribute("successMessage", "Deletion awaiting admin approval!");
		return "redirect:/deletions";
	}
	
	@GetMapping(path="/deletions")
	public String getDeletion(Model model) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser")) {
			loggedInAs = null;
		}
		List<Building> buildings = buildingService.getAllBuildingsByDeletion();
		model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("buildings", buildings);
		return "deletions";
	}
	
	@PostMapping(path="/deletions")
	public String postDeletion(
			@Valid @ModelAttribute DeleteForm deleteForm,
			BindingResult bindingResult,
			Model model, 
			RedirectAttributes attr
	) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser") || !isAdmin()) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "deletions";
		}
		List<Building> buildings = buildingService.getAllBuildingsByDeletion();
		model.addAttribute("buildings", buildings);
		if (bindingResult.hasErrors() || !deleteForm.getdEmail().isEmpty()) {
			model.addAttribute("errors", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			return "deletions";
		} else {
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + deleteForm.getRecaptcha();
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("deletion")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				return "deletions";
			}
			buildingService.deleteBuildingById(Long.parseLong(deleteForm.getBuildingId()));
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("errors", null);
		attr.addFlashAttribute("successMessage", "Building deleted successfully!");
		return "redirect:/";
	}
	
	@PostMapping(path="/deletions/remove")
	public String postRemoveDeletion(
			@Valid @ModelAttribute RemoveDeletionForm removeDeletionForm,
			BindingResult bindingResult,
			Model model, 
			RedirectAttributes attr
	) {
		String loggedInAs = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
		if (loggedInAs.equals("anonymousUser") || !isAdmin()) {
			loggedInAs = null;
			model.addAttribute("notLoggedInError", true);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			model.addAttribute("errors", null);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			return "deletions";
		}
		List<Building> buildings = buildingService.getAllBuildingsByDeletion();
		model.addAttribute("buildings", buildings);
		if (bindingResult.hasErrors() || !removeDeletionForm.getdPhone().isEmpty()) {
			model.addAttribute("errors", true);
			model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
			model.addAttribute("loggedInAs", loggedInAs);
			model.addAttribute("admin", isAdmin());
			return "deletions";
		} else {
			String url = "https://www.google.com/recaptcha/api/siteverify";
			String data = "secret=" + recaptchaSecretKey + "&" + "response=" + removeDeletionForm.getRecaptcha();
			String json = null;
			try {
				json = HttpRequests.postJSON(url, data);
			} catch (Exception e) {
				json = null;
			}
			Gson g = new Gson();
			RecaptchaResponse recaptchaResponse = g.fromJson(json, RecaptchaResponse.class);
			if (!recaptchaResponse.isSuccess() || recaptchaResponse.getScore() < 0.7 || !recaptchaResponse.getAction().equals("removeDeletion")) {
				model.addAttribute("errors", null);
				model.addAttribute("recaptchaError", true);
				model.addAttribute("recaptchaSiteKey", recaptchaSiteKey);
				model.addAttribute("loggedInAs", loggedInAs);
				model.addAttribute("admin", isAdmin());
				return "deletions";
			}
			buildingService.unsetBuildingDeletion(Long.parseLong(removeDeletionForm.getBuildingId()));
			buildings = buildingService.getAllBuildingsByDeletion();
			model.addAttribute("buildings", buildings);
		}
		model.addAttribute("loggedInAs", loggedInAs);
		model.addAttribute("admin", isAdmin());
		model.addAttribute("errors", null);
		attr.addFlashAttribute("successMessage", "Deletion removed successfully!");
		return "redirect:/deletions";
	}
	
}
