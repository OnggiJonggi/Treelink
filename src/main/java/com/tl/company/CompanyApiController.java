package com.tl.company;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tl.global.api.BusinessNoCheckService;
import com.tl.global.api.BusinessNoCheckVO;
import com.tl.global.common.SearchResultVO;
import com.tl.global.file.CompanyFileService;
import com.tl.global.file.component.FileComponent;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.RoleEnum;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyApiController {
	public final BusinessNoCheckService businessNoCheckService;
	public final CompanyService companyService;
	public final CompanyFileService companyDocService;
	public final CryptoComponent cryptoComponent;
	public final FileComponent fileValidateComponent;
	
	/**
	 * 사업자 등록번호 진위확인
	 * 관리자
	 * @param businessNoCheckRequest
	 * @param bindingResult
	 */
	@GetMapping("/check-businessno")
	public ResponseEntity<String> checkBusinessNo(
			@Valid BusinessNoCheckVO.request businessNoCheckRequest
			,BindingResult bindingResult){
		
		if(bindingResult.hasErrors())
			return ResponseEntity.badRequest().build();
		
		businessNoCheckService.checkBusinessNo(businessNoCheckRequest);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 목록
	 */
	@GetMapping("")
	public ResponseEntity<SearchResultVO<CompanyVO.Detail>> goCompanyList(CompanyVO.Search companySearch,
			@AuthenticationPrincipal CustomUserDetails userDetails
			) throws Exception{
		
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
		
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 업체 수정
	 * 관리자
	 */
	@PutMapping("/{encryptedCompanyNo}")
	public ResponseEntity<Void> updateCompany(
			@PathVariable String encryptedCompanyNo,
			@Valid CompanyVO.Registor company) throws Exception{
		
		// 회사 식별번호 복호화
		company.setCompanyNo(Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo)));
		
		companyService.updateCompany(company);
		
		return ResponseEntity.ok().build();
	}

	
	/**
	 * 회사 소개 생성/수정
	 * 관리자
	 */
	@PutMapping("/{encryptedCompanyNo}/intro")
	public ResponseEntity<Void> insertIntro(
			@RequestParam String intro,
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails
			) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));

		companyService.updateIntro(intro, companyNo, memberNo);
		
		return ResponseEntity.ok().build();
	}
	
}
