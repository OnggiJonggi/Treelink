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
public class CompanyFileApiController {
	private final CompanyFileService companyFileService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 업체 로고 등록
	 * 관리자
	 */
	@PostMapping("{encryptedCompanyNo}/logo")
	public ResponseEntity<Void> getLogo(
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam MultipartFile file) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		companyFileService.insertLogo(fileData, companyNo, memberNo);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 로고 보기
	 * 관리자
	 * 전체 : 활성화된 업체 로고만 조회 가능
	 */
	@GetMapping("{encryptedCompanyNo}/logo")
	public ResponseEntity<String> getImage(
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		// 업체 식별번호 복호화
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		String url;
		if(userDetails==null ||
				!userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한이 없으면 활성화된 회사만 조회 가능
			url = companyFileService.getSavePath(companyNo, false);
		}else url = companyFileService.getSavePath(companyNo, true);
		
		// 없으면 가세요
		if(url == null || url.isEmpty())
			return ResponseEntity.notFound().build();
		
		return ResponseEntity.ok().body(url);
	}
	
	/**
	 * 업체 서류 등록
	 * 관리자
	 */
	@PostMapping("{encryptedCompanyNo}/doc")
	public ResponseEntity<Void> docRegistration(
			@PathVariable String encryptedCompanyNo,
			@RequestParam MultipartFile file,
			@RequestParam String docType,
			@RequestParam(required = false) LocalDate expireOn,
			@AuthenticationPrincipal CustomUserDetails userDetails
			) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		// 등록 중...
		CompanyFileVO.HandOver handOver = CompanyFileVO.HandOver.builder()
				.memberNo(Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo())))
				.companyNo(companyNo)
				.file(fileData)
				.docType(docType)
				.expireOn(expireOn).build();
		companyFileService.insert(handOver);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 서류 조회
	 * 관리자
	 */
	@GetMapping("{encryptedCompanyNo}/doc/{encryptedDocNo}")
	public ResponseEntity<String> getDoc(
			@PathVariable String encryptedCompanyNo,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		
		String url = companyFileService.getFile(companyNo, docNo);

		return ResponseEntity.ok().body(url);
	}
	
	/**
	 * 파일 삭제 요청
	 * 관리자
	 * 
	 * @param CompanyUuid
	 * @param encryptedDocNo
	 * @return 204
	 */
	@DeleteMapping("{encryptedCompanyNo}/doc/{encryptedDocNo}")
	public ResponseEntity<Void> deleteDoc(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String encryptedCompanyNo,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		companyFileService.deleteDoc(companyNo, docNo, memberNo);
		
		return ResponseEntity.noContent().build();
	}
	
	/**
	 * 업체 소개문 summernote이미지 삽입
	 * 관리자
	 */
	@PostMapping("{encryptedCompanyNo}/intro")
	public ResponseEntity<String> insertIntroImage(
			@PathVariable String encryptedCompanyNo,
			@RequestParam MultipartFile file,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));

		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		String changedName = companyFileService.insertIntroImage(fileData, companyNo, memberNo);
		
		// 변경된 이름 반환
		return ResponseEntity.ok(changedName);
	}
	
	/**
	 * 소개문 이미지 조회
	 * 관리자 : 비활성된 업체 조회 가능
	 */
	@GetMapping("{encryptedCompanyNo}/intro/{changedName}")
	public ResponseEntity<String> getIntroImage(
			@PathVariable String encryptedCompanyNo,
			@PathVariable String changedName,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		// 조회
		String url;
		if(userDetails==null ||
				!userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한이 없으면 활성화된 회사만 조회 가능
			url = companyFileService.getIntroImage(companyNo, changedName, CompanyStatusEnum.ACTIVE);
			
			// 권한 있으면 제한 없음
		}else url = companyFileService.getIntroImage(companyNo, changedName, null);
		
		return ResponseEntity.ok().body(url);
	}
}
