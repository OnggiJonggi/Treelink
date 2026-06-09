package com.tl.member;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.common.SearchResultVO;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.RoleEnum;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/member")
@RequiredArgsConstructor
@Slf4j
public class MemberApiController {
	private final MemberService memberService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 아이디 중복 확인
	 * @param member
	 * @return 정상 200
	 */
	@GetMapping("/check-id")
	public ResponseEntity<Void> checkId(String userId) {
		memberService.checkId(userId);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 닉네임 중복 확인
	 * @param nickname
	 * @return 정상 200, 이상해요 400
	 */
	@GetMapping("/check-nickname")
	public ResponseEntity<Void> checkNickName(String nickname) {
		memberService.checkNick(nickname);
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 회원 목록
	 * 관리자 권한
	 */
	@GetMapping("")
	public ResponseEntity<SearchResultVO<MemberVO.Detail>> getList(MemberVO.Search search) throws Exception{
		SearchResultVO<MemberVO.Detail> result = memberService.getList(search);
		return ResponseEntity.ok(result);
	}
	
	/**
	 * 회원 기본정보 수정
	 * 관리자, 본인 계정
	 */
	@PutMapping("/{encryptedMemberNo}/update")
	public ResponseEntity<Void> updateMemberBasicInfo(MemberVO.Update member,
			@PathVariable String encryptedMemberNo) throws Exception{
		
		// 회원 식별번호 추출
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(encryptedMemberNo));
		member.setMemberNo(memberNo);
		
		memberService.updateMemberBasicInfo(member);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 권한 수정
	 * 관리자 : 본인 계정 권한 수정 불가능, 관리자 권한 접근 불가능
	 * 최고 관리자 : 본인 계정 권한 수정 불가능
	 * @return : 200 정상, 403 권한 없음
	 */
	@PutMapping("/{encryptedMemberNo}/update/role")
	public ResponseEntity<Void> updateMemberRole(@NotNull RoleEnum role,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String encryptedMemberNo) throws Exception{
		
		// 피수정 회원 식별번호
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(encryptedMemberNo));
		
		// 수정 회원 식별번호
		int myMemberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		// 최고 관리자 권한은 죽었다 깨어나도 떽! 이야.
		if(role==RoleEnum.SUPER_ADMIN) {
			
			log.warn("최고 관리자 권한 생성 시도 발견 memberNo : "+myMemberNo);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		// 감히 어딜 관리자 따위가 새로운 관리자를 만드려 하는가
		if(role==RoleEnum.ADMIN
				&& !userDetails.getAuthorities().stream()
				.anyMatch(a -> a.getAuthority().equals(RoleEnum.SUPER_ADMIN.getPrefix()))) {
			
			log.warn("관리자 권한 생성 시도 발견 memberNo : "+myMemberNo);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		// 자추는 추하지;;
		if(myMemberNo==memberNo) {
			
			log.warn("자신의 권한을 바꾸려 시도합니다 memberNo : "+myMemberNo);
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
		}
		
		memberService.updateMemberRole(memberNo, role);
		
		return ResponseEntity.ok().build();
	}
	
	
	/**
	 * 상태값 수정
	 * 최고 관리자 상태값 수정 불가
	 * 관리자 : 본인 계정 수정 불가
	 * @return : 200 정상, 403 권한 없음
	 */
	@PutMapping("/{encryptedMemberNo}/update/status")
	public ResponseEntity<Void> updateMemberBasicInfo(@NotNull MemberStatusEnum status,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@PathVariable String encryptedMemberNo) throws Exception{
		
		// 피수정자 회원 식별번호 추출
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(encryptedMemberNo));
		
		// 수정자 식별번호 추출
		int myMemberNo = Integer.valueOf(cryptoComponent.decrypt(userDetails.getEncryptedMemberNo()));
		
		// 관리자 계정이면 본인 계정 수정 불가능
		if(userDetails.getAuthorities().stream()
		        .anyMatch(a -> a.getAuthority().equals(RoleEnum.ADMIN.getPrefix()))) {
			
			if(myMemberNo==memberNo) {
				log.warn("관리자가 자신의 계정을 수정하려 시도중입니다. memberNo : "+myMemberNo);
				throw new ResponseStatusException(HttpStatus.FORBIDDEN);
			}
		}
		
		// 실시!
		memberService.updateMemberStatus(memberNo, status);
		
		return ResponseEntity.ok().build();
	}
	
	/**
	 * 별명 중복 확인(수정용)
	 */
	@GetMapping("/check-updatednickname")
	public ResponseEntity<Void> checkUpdatedNickname(String nickname, String encryptedMemberNo) throws Exception {
		int memberNo = Integer.valueOf(cryptoComponent.decrypt(encryptedMemberNo));
		memberService.checkUpdatedNickname(memberNo, nickname);
		return ResponseEntity.ok().build();
	}
}
