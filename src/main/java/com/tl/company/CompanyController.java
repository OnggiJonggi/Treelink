package com.tl.company;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.api.BusinessNoCheckVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.FileInfoVO;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.RoleEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
public class CompanyController {
	private final CompanyService companyService;
	private final CompanyDocService companyDocService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 사업체 등록 페이지로
	 * 관리자
	 */
	@GetMapping("/registor")
	public String goCompanyRegistor(Model model) {
		model.addAttribute("companyRegistor", new CompanyVO.Registor());
		model.addAttribute("BusinessNoCheckRequest", new BusinessNoCheckVO.request());
		return "company/registor";
	}
	
	/**
	 * 사업체 등록
	 * 관리자
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
		
		return "redirect:/company/"+companyService.companyRegistor(companyRegistor);
	}
	
	/**
	 * 사업체 상세 페이지
	 * 관리자(서류 열람, 중단/종료된 사업체 상세 조회)
	 */
	@GetMapping("/{companyUuid}")
	public String goView(@PathVariable String companyUuid, Model model
			,@AuthenticationPrincipal UserDetails userDetails) throws Exception {
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "basic");
		
		CompanyVO.Detail detail = companyService.getCompanyBasicInfo(companyUuid);
		if(detail==null) throw new CustomException(ErrorCodeEnum.COMPANY_NOT_FOUND);
		
		model.addAttribute("companyDetail", detail);
		
		
		// 관리자면 각종 서류도 열람 가능하게 보냄
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))
				) {
			List<FileInfoVO.Detail> docs = companyDocService.getInfo(companyUuid);
			
			// 파일 식별번호 암호화
			for(FileInfoVO.Detail doc : docs) {
				doc.setEncryptedFileNo(cryptoComponent.encrypt(String.valueOf(doc.getFileNo())));
				doc.setFileNo(0);
			}
			
			model.addAttribute("docs", docs);
			
			// 사업체 정보 수정용 객체 전달
			model.addAttribute("companyRegistor", new CompanyVO.Registor());
			
		}else {
			// 관리자가 아닌 사람이, 상태가 ACTIVE가 아닌 회사 데이터에 접근하면 떽! 이야
			if(!detail.getStatus().equals(CompanyStatusEnum.ACTIVE.name()))
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		return "company/view/main";
	}
	
	
}
