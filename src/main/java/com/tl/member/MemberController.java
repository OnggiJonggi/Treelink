package com.tl.member;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tl.global.common.SearchResultVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.CustomUserDetails;
import com.tl.global.security.role.CanAccess;
import com.tl.global.security.role.HasRole;
import com.tl.global.security.role.RoleEnum;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@Controller
@RequestMapping("/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {
	private final MemberService memberService;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 로그인 페이지로
	 */
	@GetMapping("/login")
	public String goLogin() {
		return "member/login";
	}
	
	/**
	 * 회원 가입 페이지로 가세요라
	 */
	@GetMapping("/join")
	public String goJoin(Model model){
		// thymeleaf에서 th:object로 받아갈 빈 객체 보내기
		model.addAttribute("memberJoin", new MemberVO.Join());
		return "member/join";
	}
	
	/**
	 * 회원 가입 시도
	 */
	@PostMapping("/join")
	public String join(@Valid MemberVO.Join memberJoin
			,BindingResult bindingResult
			,Model model){
		
		// 유효성 검사 실패하면 가세요라
		// BindingResult는 html파일 경로를 반환해도 됨
		if(bindingResult.hasErrors()) {
			model.addAttribute("memberJoin", new MemberVO.Join());
			return "member/join";
		}
		
		memberService.join(memberJoin);
		return "redirect:/";
	}
	

	/**
	 * 회원 목록 페이지로
	 * 
	 * 관리자
	 */
	@CanAccess(RoleEnum.ADMIN)
	@GetMapping("")
	public String getList(Model model) throws Exception {
		
		// thymeleaf에서 th:object로 받아갈 빈 객체 보내기
		MemberVO.Search search = new MemberVO.Search();
		model.addAttribute("memberSearch", search);
		
		SearchResultVO<MemberVO.Detail> result = memberService.getList(search);
		model.addAttribute("memberList", result);
		
		return "member/list";
	}
	
	
	/**
	 * 회원 상세 정보 보기
	 * 
	 * 모든 회원
	 */
	@PreAuthorize("isAuthenticated()")
	@GetMapping({"/myinfo","/{encMemberNo}"})
	public String myInfo(@PathVariable(required = false) String encMemberNo,
			@AuthenticationPrincipal CustomUserDetails userDetails,
			Model model) throws Exception {
		
		// 대상 회원 번호 추출
		int memberNo;
		if(encMemberNo == null) {
			memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());
		}else {
			memberNo = cryptoComponent.decrypt(encMemberNo);
		}
		
		MemberVO.Detail result = memberService.getBasicInfo(memberNo);
		
		// 대상 회원 번호 암호화
		result.setEncMemberNo(cryptoComponent.encrypt(result.getMemberNo()));
		result.setMemberNo(0);
		
		model.addAttribute("encMemberNo", encMemberNo);
		model.addAttribute("memberDetail", result);
		model.addAttribute("memberUpdate", new MemberVO.Update());
		
		return "member/basicinfo";
	}
	
	/**
	 * 계정 삭제
	 * 
	 * 관리자를 제외한 모든 권한
	 */
	@PostMapping("/delete")
	public String deleteMember(
			@AuthenticationPrincipal CustomUserDetails userDetails,
			@HasRole(RoleEnum.ADMIN) boolean isAdmin,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

		// 누구세요
		int memberNo = cryptoComponent.decrypt(userDetails.getEncMemberNo());
		
		// 관리자 계정은 탈퇴가 안 되셔요
		if(isAdmin) {
			log.warn("관리자가 자신의 계정을 탈퇴하려 합니다. memberNo : "+memberNo);
			throw new CustomException(ErrorCodeEnum.CANNOT_DELETE_ADMIN_ACCOUNT);
		}
		
		// 가세요
		memberService.updateMemberStatus(memberNo, MemberStatusEnum.DELETED);
		
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication != null) {
	        new SecurityContextLogoutHandler().logout(request, response, authentication);
	    }
		
		return "redirect:/";
	}
}
