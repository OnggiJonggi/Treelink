package com.tl.company;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import com.tl.global.api.BusinessNoCheckService;
import com.tl.global.api.BusinessNoCheckVO;
import com.tl.global.file.FileInfoVO;
import com.tl.global.file.FileValidateComponent;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/company")
@RequiredArgsConstructor
public class CompanyApiController {
	public final BusinessNoCheckService businessNoCheckService;
	public final CompanyService companyService;
	public final CompanyDocService companyDocService;
	public final CryptoComponent cryptoComponent;
	public final FileValidateComponent fileValidateComponent;
	
	/**
	 * 사업자 등록번호 진위확인
	 * 관리자
	 * @param businessNoCheckRequest
	 * @param bindingResult
	 */
	@GetMapping("/check-businessno")
	public ResponseEntity<String> checkBusinessNo(@Valid BusinessNoCheckVO.request businessNoCheckRequest
			,BindingResult bindingResult){
		
		if(bindingResult.hasErrors())
			return ResponseEntity.badRequest().build();
		
		businessNoCheckService.checkBusinessNo(businessNoCheckRequest);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 회사 수정
	 * 관리자
	 * @param companyNo
	 */
	@PutMapping("{companyUuid}")
	public ResponseEntity<Void> updateCompany(
			@PathVariable String companyUuid,
			@Valid CompanyVO.Registor company){
		
		/*
		 * thymeleaf는 url에 접근이 어려워서
		 * 프론트에서 companyUuid를 알려면 웹 서버에서 model로 건네줘야 해요
		 * 아래처럼 하면 매우 쉬운데 그죠???
		 */
		company.setCompanyUuid(companyUuid);
		
		companyService.updateCompany(company);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 서류 등록
	 * 관리자
	 * @param companyNo
	 * @param file
	 */
	@PostMapping("/{companyUuid}/doc")
	public ResponseEntity<Void> docRegistration(
			@PathVariable String companyUuid, MultipartFile file, String docType,
			@RequestParam(required = false) LocalDate expireOn) throws Exception{
		
		// 파일 이름, 서류 종류 유효성 검사
		fileValidateComponent.isValid(file, docType, expireOn);
		
		companyDocService.registor(companyUuid, docType, expireOn, file);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 서류 조회
	 * @param encryptedDocNo
	 */
	@GetMapping("{companyUuid}/doc/{encryptedDocNo}")
	public ResponseEntity<Resource> getDoc(
			@PathVariable String CompanyUuid,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		
		FileInfoVO.FileResult result = companyDocService.getFile(CompanyUuid, docNo);
		
		// Content-Disposition 구성
		String encodedName = UriUtils.encode(result.getOriginalName(), StandardCharsets.UTF_8);
		String disposition = (result.isInline() ? "inline" : "attachment") + "; filename=\"" + encodedName + "\"";

		// MIME 타입 -> Content-Type 변환
		MediaType mediaType;
		try {
			mediaType = MediaType.parseMediaType(result.getMimeType());
		} catch (Exception e) {
			// octet-stream : 뭔지 모르는 바이너리 데이터
			mediaType = MediaType.APPLICATION_OCTET_STREAM;
		}

		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
				.contentType(mediaType)
				.body(result.getResource());
	}
	
	/**
	 * 파일 삭제 요청
	 * @param CompanyUuid
	 * @param encryptedDocNo
	 * @return 204
	 */
	@DeleteMapping("{companyUuid}/doc/{encryptedDocNo}")
	public ResponseEntity<Void> deleteDoc(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String CompanyUuid,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		companyDocService.deleteDoc(CompanyUuid, docNo, memberNo);
		
		return ResponseEntity.noContent().build();
	}
	
	
}
