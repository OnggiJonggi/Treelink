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
import com.tl.global.common.SearchResultVO;
import com.tl.global.file.CompanyFileService;
import com.tl.global.file.FileInfoVO;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.RoleEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Controller
@RequestMapping("/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyController {
	private final CompanyService companyService;
	private final CompanyFileService companyDocService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 업체 목록 페이지로
	 * 관리자 : 업체 상태에 따른 검색 기능
	 */
	@GetMapping("")
	public String goCompanyList(Model model,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		CompanyVO.Search companySearch = new CompanyVO.Search();
		model.addAttribute("companySearch", companySearch);
		
		// 관리자 권한에 따라 조회 범위가 달라요
		if(userDetails == null ||
				!userDetails.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 권한 없으면 활성화된 상태만 조회 가능해요
			companySearch.setStatus(CompanyStatusEnum.ACTIVE);
		}
		
		SearchResultVO<CompanyVO.Detail> result = companyService.getCompanyList(companySearch);
		
		// 식별번호 암호화
		for(CompanyVO.Detail company : result.getList()) {
			company.setEncryptedCompanyNo(cryptoComponent.encrypt(String.valueOf(company.getCompanyNo())));
			company.setCompanyNo(0);
		}
		
		model.addAttribute("companyList", result);
		
		return "company/list";
	}
	
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
			,Model model) throws Exception{
		
		if(bindingResult.hasErrors()) {
			model.addAttribute("companyRegistor", new CompanyVO.Registor());
			model.addAttribute("BusinessNoCheckRequest", new BusinessNoCheckVO.request());
			return "admin/company/registor";
		}
		
		// DB저장 및 업체 식별번호 추출
		int companyNo = companyService.companyRegistor(companyRegistor);
		
		// 암호화
		String encryptedCompanyNo = cryptoComponent.encrypt(String.valueOf(companyNo)); 
		
		return "redirect:/company/"+encryptedCompanyNo;
	}
	
	/**
	 * 사업체 상세 페이지
	 * 관리자(서류 열람, 중단/종료된 사업체 상세 조회)
	 */
	@GetMapping("/{encryptedCompanyNo}")
	public String goView(
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) throws Exception {
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "basic");
		
		// 데이터 조회
		CompanyVO.Detail detail = companyService.getCompanyBasicInfo(companyNo);
		
		// 회사 식별번호 암호화
		detail.setEncryptedCompanyNo(cryptoComponent.encrypt(String.valueOf(detail.getCompanyNo())));
		detail.setCompanyNo(0);
		
		model.addAttribute("companyDetail", detail);
		
		// 관리자면 각종 서류도 열람 가능하게 보냄
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			List<FileInfoVO.Detail> docs = companyDocService.getInfo(companyNo);
			
			// 파일 식별번호 암호화
			for(FileInfoVO.Detail doc : docs) {
				doc.setEncryptedFileNo(cryptoComponent.encrypt(String.valueOf(doc.getFileNo())));
				doc.setFileNo(0);
			}
			
			model.addAttribute("companyDocs", docs);
			
			// 사업체 정보 수정용 객체 전달
			model.addAttribute("companyRegistor", new CompanyVO.Registor());
			
		}else {
			// 관리자가 아닌 사람이, 상태가 ACTIVE가 아닌 회사 데이터에 접근하면 떽! 이야
			if(detail.getStatus() != CompanyStatusEnum.ACTIVE)
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		return "company/view/main";
	}
	
	/**
	 * 업체 소개 페이지 조각
	 * 관리자 : 비활성 업체 조회 가능
	 */
	@GetMapping("/{encryptedCompanyNo}/intro")
	public String getIntro(
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) throws Exception {
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "intro");
		model.addAttribute("encryptedCompanyNo", encryptedCompanyNo);
		
		// 소개문 조회
		String intro;
		
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한 : 전체 조회
			intro = companyService.getIntro(companyNo, null);
			
			// 권한 없으면 손가락이나 빠쇼
		}else intro = companyService.getIntro(companyNo, CompanyStatusEnum.ACTIVE);
			
		model.addAttribute("companyIntro", intro);
		
		return "company/view/intro :: content";
	}
	
}
