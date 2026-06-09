package com.tl.global.file;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import org.springframework.web.util.UriUtils;

import com.tl.company.CompanyVO;
import com.tl.global.file.component.FileValidateComponent;
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
	private final FileService fileService;
	private final CompanyDocService companyDocService;
	private final CryptoComponent cryptoComponent;
	public final FileValidateComponent fileValidateComponent;
	
	@Value("${file.upload.address}")
	private String uploadAddress;
	//  D:/Dev/upload/
	
	/**
	 * 업체 로고 등록
	 * 관리자
	 */
	@PostMapping("/{encryptedCompanyNo}/logo")
	public ResponseEntity<Void> getLogo(
			@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@RequestParam MultipartFile file) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		companyDocService.insertLogo(companyNo, file, memberNo);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 이미지 파일 접근
	 * 관리자 : 모든 파일 제한 없이 접근
	 * 관리자 아님 : 활성화된 회사 및 파일에만 접근 가능
	 */
	@GetMapping("/{encryptedCompanyNo}/logo")
	public ResponseEntity<Resource> getImage(@PathVariable String encryptedCompanyNo,
			@AuthenticationPrincipal CustomUserDetails userDetails) throws Exception{
		
		// 업체 식별번호 복호화
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		FileInfoVO.SavePath path;
		if(userDetails==null ||
				!userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자 권한이 없으면 활성화된 회사/파일만 볼 수 있어요
			path = fileService.getSavePath(companyNo, false);
		}else path = fileService.getSavePath(companyNo, true);
		
		// 없으면 가세요라
		if(path == null || path.getSavePath() == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		
		// 파일 꺼내오는 중...
		Resource resource = new FileSystemResource(path.getSavePath());
		
		// 아 없으면 좀 가라고
		if(!resource.exists()) return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
		
		return ResponseEntity.ok().contentType(MediaType.parseMediaType(path.getMime())).body(resource);
	}
	
	/**
	 * 업체 서류 등록
	 * 관리자
	 */
	@PostMapping("/{encryptedCompanyNo}/doc")
	public ResponseEntity<Void> docRegistration(
			@PathVariable String encryptedCompanyNo,
			@RequestParam MultipartFile file,
			@RequestParam String docType,
			@RequestParam(required = false) LocalDate expireOn,
			@AuthenticationPrincipal CustomUserDetails userDetails
			) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		
		// 등록 중...
		CompanyVO.DocRegistor docRegistor = CompanyVO.DocRegistor.builder()
				.memberNo(Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo())))
				.companyNo(companyNo)
				.file(file)
				.docType(docType)
				.expireOn(expireOn).build();
		companyDocService.registor(docRegistor);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 업체 서류 조회
	 * @param encryptedDocNo
	 */
	@GetMapping("{encryptedCompanyNo}/doc/{encryptedDocNo}")
	public ResponseEntity<Resource> getDoc(
			@PathVariable String encryptedCompanyNo,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		
		FileInfoVO.FileResult result = companyDocService.getFile(companyNo, docNo);
		
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
	@DeleteMapping("{encryptedCompanyNo}/doc/{encryptedDocNo}")
	public ResponseEntity<Void> deleteDoc(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String encryptedCompanyNo,
			@PathVariable String encryptedDocNo) throws Exception{
		
		int companyNo = Integer.valueOf(cryptoComponent.decrypt(encryptedCompanyNo));
		int docNo = Integer.valueOf(cryptoComponent.decrypt(encryptedDocNo));
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		companyDocService.deleteDoc(companyNo, docNo, memberNo);
		
		return ResponseEntity.noContent().build();
	}
}
