package com.tl.company;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
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

import com.tl.global.common.SearchResultVO;
import com.tl.global.file.CompanyDocService;
import com.tl.global.file.CompanyDocVO;
import com.tl.global.file.EvalDocVO;
import com.tl.global.location.LocationVO;
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
	private final CompanyDocService companyDocService;
	private final EvalService evalService;
	private final CryptoComponent cryptoComponent;

	// 카카오 맵 API js키
	@Value("${kakao-js.key}")
	private String kakaoKey;
	
	/**
	 * 업체 목록 페이지로
	 * 관리자 : 업체 상태에 따른 검색 기능
	 * 
	 * 전형적인 검색 필터 - 검색 결과 - 페이징 바
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
			company.setEncCompanyNo(cryptoComponent.encrypt(company.getCompanyNo()));
			company.setCompanyNo(0);
		}
		
		model.addAttribute("companyList", result);
		
		return "company/list";
	}
	
	/**
	 * 사업체 등록 페이지로
	 * 관리자
	 * 
	 * 1. 사업자 번호, 대표 이름, 창립일 입력받아 사업자 등록번호 진위확인
	 * 2. 회사 이름, 전화번호, 이메일, 주 종목 입력받기
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
			log.info("사업체 등록 오류! \n대상 : {}\n오류 : {}", companyRegistor, bindingResult);
			
			model.addAttribute("companyRegistor", new CompanyVO.Registor());
			model.addAttribute("BusinessNoCheckRequest", new BusinessNoCheckVO.request());
			return "company/registor";
		}
		
		// DB저장 및 업체 식별번호 추출
		int companyNo = companyService.companyRegistor(companyRegistor);
		
		// 암호화
		String encCompanyNo = cryptoComponent.encrypt(companyNo); 
		
		return "redirect:/company/"+encCompanyNo;
	}
	
	/**
	 * 사업체 상세 페이지
	 * 모든 권한
	 * 관리자 : 상태 조회, 로고 업로드, 기본 정보 수정, 서류 조회 / 등록 / 삭제, 비활성 업체 조회
	 */
	@GetMapping("{encCompanyNo}")
	public String goView(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) throws Exception {
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "basic");
		
		// 데이터 조회
		CompanyVO.Detail detail = companyService.getCompanyBasicInfo(companyNo);
		
		// 회사 식별번호 암호화
		detail.setEncCompanyNo(cryptoComponent.encrypt(detail.getCompanyNo()));
		detail.setCompanyNo(0);
		
		model.addAttribute("companyDetail", detail);
		
		// 관리자면 각종 서류도 열람 가능하게 보냄
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			List<CompanyDocVO.Detail> docs = companyDocService.getInfo(companyNo);
			
			// 파일 식별번호 암호화
			for(CompanyDocVO.Detail doc : docs) {
				doc.setEncFileNo(cryptoComponent.encrypt(doc.getFileNo()));
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
	 * 모든 권한
	 * 관리자 : 비활성 업체 조회 가능
	 * 
	 * summernote를 사용해 자유롭게 입력받아요
	 * 사진을 첨부하면 즉시 S3에다 비동기 삽입 후 일회용 링크 받아옴
	 */
	@GetMapping("{encCompanyNo}/intro")
	public String getIntro(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model) throws Exception {
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "intro");
		model.addAttribute("encCompanyNo", encCompanyNo);
		
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
	
	/**
	 * 업체 위치 조각
	 * 모든 권한
	 * 관리자 : 위치 추가 / 삭제
	 * 
	 * 카카오 js키를 프론트에 전달해서 카카오 맵 api사용
	 * 위치 추가 : 카카오 우편번호 조회 -> 지오코딩 -> 카카오 맵에 띄우기
	 */
	@GetMapping("{encCompanyNo}/location")
	public String goLocation(
			@PathVariable String encCompanyNo,
			Model model) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "location");
		model.addAttribute("encCompanyNo", encCompanyNo);
		
		// 위치 추출해서 넘겨주기
		List<CompanyVO.LocationDetail> locations = companyService.getLocaions(companyNo);
		for(LocationVO.Detail item : locations) {
			item.setEncLocationNo(cryptoComponent.encrypt(item.getLocationNo()));
			item.setLocationNo(0);
		}
		
		model.addAttribute("locations", locations);
		
		// 카카오 맵 api 키
		model.addAttribute("kakaoKey", kakaoKey);
		
		return "company/view/location :: content";
	}
	
	/**
	 * 작업 현황 조각
	 * 모든 권한
	 * 관리자 : 현황 추가/수정, 모든 작업 현황 조회
	 * 
	 * 업체가 진행 중 / 종료한 프로젝트 나열
	 * 현황 이름, 메모, 시작일, 종료일, 상태
	 * 공개 범위(VISIBLE)에 따라 보이는게 달라요
	 */
	@GetMapping("{encCompanyNo}/management")
	public String goManagement(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model)throws Exception {
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// 작업 현황
		ManagementVO.SearchResult result;
		
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한 : 전체 조회
			result = companyService.getManagement(new ManagementVO.Search(companyNo, true));
			
			// 권한 없으면 손가락이나 빠쇼
		}else result = companyService.getManagement(new ManagementVO.Search(companyNo, false));
		
		// 식별번호 암호화
		for(ManagementVO.Detail item : result.getResult().getList()) {
			item.setEncLocationNo(cryptoComponent.encrypt(item.getLocationNo()));
			item.setLocationNo(0);
		}
		
		// 넣어두기
		model.addAttribute("result", result);
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "management");
		model.addAttribute("encCompanyNo", encCompanyNo);
		
		// 카카오 맵 api 키
		model.addAttribute("kakaoKey", kakaoKey);
		
		return "company/view/management :: content";
	}
	
	
	/**
	 * 평가 조각
	 * 모든 권한 : 활성화된 업체 평가 조회
	 * 관리자 : 모든 업체 평가 조회 / 추가 / 수정
	 * 평가자 : 활성화된 업체 평가 추가 / 수정
	 * 
	 * @return EvalVO.Detail.files : 
	 * 	MEMO, FILE_NO, FILE_SIZE, ORIGINAL_NAME 조회
	 */
	@GetMapping("{encCompanyNo}/eval")
	public String goEval(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails,
			Model model)throws Exception {
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		EvalVO.Detail result;
		
		// 관리자 권한
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 모든 업체 조회
			 result = evalService.getEval(companyNo, true);
			
		} else {
			// 활성화된 업체만 조회
			result = evalService.getEval(companyNo, false);
		}
		
		// 검색 결과 있으면 식별번호 지우고 파일 식별번호 암호화
		if(result != null) {
			result.setEvaluationNo(0);
			
			if(result.getFiles() != null && !result.getFiles().isEmpty()) {
				for(EvalDocVO.Detail item : result.getFiles()) {
					item.setEncFileNo(cryptoComponent.encrypt(item.getFileNo()));
					item.setFileNo(0);
				}
			}
		}
		
		model.addAttribute("eval", result);
		
		// 네비 바에게 여기가 어디고 나는 누구인지 알려줌
		model.addAttribute("companyMenu", "eval");
		model.addAttribute("encCompanyNo", encCompanyNo);
		
		return "company/view/eval :: content";
	}
	
	
	
}
