package com.tl.global.file;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.RoleEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/file/eval")
@RequiredArgsConstructor
@Slf4j
public class EvalDocApiController {
	private final EvalDocService evalDocService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 평가 서류 추가
	 * 
	 * @param memo : 메모도 이 때 넣어둠
	 * @return 암호화된 FILE_NO
	 */
	@PostMapping("")
	public ResponseEntity<String> insertEvalDoc(
			@RequestParam MultipartFile file,
			@RequestParam String memo,
			@AuthenticationPrincipal CustomUserDetails userDetails
			)throws Exception{
		
		int memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());

		// MultipartFile을 FileDataVO로 변환
		FileDataVO fileData = FileDataVO.builder()
				.originalName(file.getOriginalFilename())
				.mime(file.getContentType())
				.size(file.getSize())
				.bytes(file.getBytes()).build();
		
		// 저장
		int fileNo = evalDocService.insertEvalDoc(fileData, memo, memberNo);
		if(fileNo==0) return ResponseEntity.internalServerError().build();
		
		return ResponseEntity.ok().body(cryptoComponent.encrypt(fileNo));
	}
	
	/**
	 * 파일 얻어내기
	 * 
	 * 모든 권한 : 활성화된 업체 파일
	 * 관리자 : 모든 업체 파일
	 */
	@GetMapping("{encFileNo}")
	public ResponseEntity<String> getFile(
			@PathVariable String encFileNo,
			@RequestParam String encCompanyNo,
			@AuthenticationPrincipal UserDetails userDetails) throws Exception{
		
		int fileNo = cryptoComponent.decrypt(encFileNo);

		String url;
		if(userDetails != null &&
				userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			// 관리자면 모든 업체 조회 가능
			url = evalDocService.getSavePath(0, fileNo, true);
			
		}else {
			
			// 관리자 아니면 활성화된 업체만 조회 가능
			int companyNo = cryptoComponent.decrypt(encCompanyNo);
			url = evalDocService.getSavePath(companyNo, fileNo, false);
		}
		
		// 없으면 가세요
		if(url == null || url.isEmpty())
			return ResponseEntity.notFound().build();
		
		return ResponseEntity.ok().body(url);
	}
}
