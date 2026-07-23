package com.tl.global.file;

import java.time.LocalDate;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tl.company.CompanyStatusEnum;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.RoleEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/file/company")
@RequiredArgsConstructor
@Slf4j
public class CompanyDocApiController {
	private final CompanyDocService companyDocService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 업체 로고 등록
	 * 관리자
	 */
	@PostMapping("{encCompanyNo}/logo")
	public ResponseEntity<Void> getLogo(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam MultipartFile file) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		int memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());
		
		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		companyDocService.insertLogo(fileData, companyNo, memberNo);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 로고 보기
	 * 관리자
	 * 전체 : 활성화된 업체 로고만 조회 가능
	 */
	@GetMapping("{encCompanyNo}/logo")
	public ResponseEntity<String> getImage(
			@PathVariable String encCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		// 업체 식별번호 복호화
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		String url;
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
	            .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자이면 모든 업체 조회 가능
			url = companyDocService.getSavePath(companyNo, true);
			
		}else url = companyDocService.getSavePath(companyNo, false);
		
		// 없으면 가세요
		if(url == null || url.isEmpty())
			return ResponseEntity.notFound().build();
		
		return ResponseEntity.ok().body(url);
	}
	
	/**
	 * 업체 서류 등록
	 * 관리자
	 */
	@PostMapping("{encCompanyNo}/doc")
	public ResponseEntity<Void> docRegistration(
			@PathVariable String encCompanyNo,
			@RequestParam MultipartFile file,
			@RequestParam String docType,
			@RequestParam(required = false) LocalDate expireOn,
			@AuthenticationPrincipal CustomUserDetails userDetails
			) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		// 등록 중...
		CompanyDocVO.HandOver handOver = CompanyDocVO.HandOver.builder()
				.memberNo(cryptoComponent.decrypt(userDetails.getEncMemberNo()))
				.companyNo(companyNo)
				.file(fileData)
				.docType(docType)
				.expireOn(expireOn).build();
		companyDocService.insert(handOver);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 서류 조회
	 * 
	 * 관리자
	 */
	@GetMapping("{encCompanyNo}/doc/{encFileNo}")
	public ResponseEntity<String> getDoc(
			@PathVariable String encCompanyNo,
			@PathVariable String encFileNo) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		int fileNo = cryptoComponent.decrypt(encFileNo);
		
		String url = companyDocService.getFile(companyNo, fileNo);

		return ResponseEntity.ok().body(url);
	}
	
	/**
	 * 파일 삭제 요청
	 * 
	 * 관리자
	 */
	@DeleteMapping("{encCompanyNo}/doc/{encFileNo}")
	public ResponseEntity<Void> deleteDoc(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String encCompanyNo,
			@PathVariable String encFileNo) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		int fileNo = cryptoComponent.decrypt(encFileNo);
		int memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());
		
		companyDocService.deleteDoc(companyNo, fileNo, memberNo);
		
		return ResponseEntity.noContent().build();
	}
	
	/**
	 * 업체 소개문 summernote이미지 삽입
	 * 관리자
	 * 
	 * @return uuid+확장자로 변경된 이름
	 */
	@PostMapping("{encCompanyNo}/intro")
	public ResponseEntity<String> insertIntroImage(
			@PathVariable String encCompanyNo,
			@RequestParam MultipartFile file,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		int memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());

		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		String changedName = companyDocService.insertIntroImage(fileData, companyNo, memberNo);
		
		// 변경된 이름 반환
		return ResponseEntity.ok(changedName);
	}
	
	/**
	 * 소개문 이미지 조회
	 * 관리자 : 비활성된 업체 조회 가능
	 */
	@GetMapping("{encCompanyNo}/intro/{changedName}")
	public ResponseEntity<String> getIntroImage(
			@PathVariable String encCompanyNo,
			@PathVariable String changedName,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		int companyNo = cryptoComponent.decrypt(encCompanyNo);
		
		// 조회
		String url;
		if(userDetails==null ||
				!userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한이 없으면 활성화된 회사만 조회 가능
			url = companyDocService.getIntroImage(companyNo, changedName, CompanyStatusEnum.ACTIVE);
			
			// 권한 있으면 제한 없음
		}else url = companyDocService.getIntroImage(companyNo, changedName, null);
		
		return ResponseEntity.ok().body(url);
	}
}
