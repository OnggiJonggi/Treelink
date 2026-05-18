package website.treelink.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import website.treelink.company.CompanyVO;
import website.treelink.global.api.BusinessNoCheckVO;

@Controller
@RequestMapping("/admin/company")
public class AdminCompanyController {
	@Autowired
	AdminCompanyService service;
	
	/**
	 * 사업체 등록 페이지로
	 */
	@GetMapping("/registor")
	public String goCompanyRegistor(Model model) {
		model.addAttribute("companyRegistor", new CompanyVO.Registor());
		model.addAttribute("BusinessNoCheckRequest", new BusinessNoCheckVO.request());
		return "admin/company/registor";
	}
	
	/**
	 * 사업체 등록
	 */
	@PostMapping("/registor")
	public String companyRegistor(
			@Valid CompanyVO.Registor companyRegistor
			,BindingResult bindingResult
			,Model model){
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("companyRegistor", new CompanyVO.Registor());
			model.addAttribute("BusinessNoCheckRequest", new BusinessNoCheckVO.request());
			return "admin/company/registor";
		}
		
		return "redirect:/company/view"+service.companyRegistor(companyRegistor);
	}
	

	
}
