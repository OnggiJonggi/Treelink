package website.treelink.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import website.treelink.global.api.BusinessNoCheckService;
import website.treelink.global.api.BusinessNoCheckVO;

@RestController
@RequestMapping("/api/admin")
public class AdminApiCompanyController {
	@Autowired
	BusinessNoCheckService businessNoCheckService;
	
	/**
	 * 사업자 등록번호 진위확인
	 * @param businessNoCheckRequest
	 * @param bindingResult
	 */
	@GetMapping("/company/check-businessno")
	public ResponseEntity<String> checkBusinessNo(@Valid BusinessNoCheckVO.request businessNoCheckRequest
			,BindingResult bindingResult){
		
		if(bindingResult.hasErrors())
			return ResponseEntity.badRequest().build();
		
		businessNoCheckService.checkBusinessNo(businessNoCheckRequest);
		
		return ResponseEntity.ok().build();
	}
}
