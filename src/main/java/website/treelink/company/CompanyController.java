package website.treelink.company;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/company")
public class CompanyController {
	
	@GetMapping("/view/{companyNo}")
	public String goView() {
		
		
		return "company/view";
	}
}
