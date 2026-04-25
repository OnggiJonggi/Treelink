package website.treelink.member.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.validation.Valid;
import website.treelink.member.model.service.MemberAccountService;
import website.treelink.member.model.vo.Member;

/**
 * 계정 관리 클라쓰
 * 로그인/로그아웃은 쓰뿌륑-콘트롤라가 대신함
 */
@Controller
@RequestMapping("/member")
public class MemberAccountController {
	@Autowired
	private MemberAccountService service;
	
	/**
	 * 회원 가입 페이지로 가세요라
	 * @return 회원가입 페이지
	 */
	@GetMapping("/new-account")
	public String newAccount(Model model){
		// thymeleaf에서 th:object로 받아갈 빈 객체 보내기
		model.addAttribute("newAccount", new Member.NewAccount());
		return "member/new-account";
	}
	

	/**
	 * @param member
	 * @param bindingResult
	 * BindingResult : @Valid 뒤에 붙여나와 오류 발생 시 결과 저장
	 * @return 실패하면 기존 페이지, 성공하면 메인화면
	 */
	@PostMapping("/new-account")
	public String newAccount(@Valid Member.NewAccount member
			,BindingResult bindingResult){
		
		// 유효성 검사 실패하면 가세요라
		if(bindingResult.hasErrors()) return "member/new-account";
		
		service.newAccount(member);
		return "redirect:/";
	}
	
}
