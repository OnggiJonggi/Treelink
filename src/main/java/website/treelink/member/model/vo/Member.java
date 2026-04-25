package website.treelink.member.model.vo;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import website.treelink.common.regex.MemberRegexp;
@Builder
public class Member {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class Login{
		private int userNo;
		private String userId;
		private String userPwd;
		private String name;
		private List<String> roles;
		private String state;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class NewAccount{
		private int memberNo;
		
		@NotBlank(message="아이디를 입력해라.")
		@Pattern(regexp = MemberRegexp.ID_REGEXP, message="아이디가 이상해요.")
		private String userId;
		
		@NotBlank(message="비밀번호 입력해.")
		@Pattern(regexp = MemberRegexp.PWD_REGEXP, message="비번 유효성에 안 맞잖아!")
		private String userPwd;
		
		@NotBlank(message="이름없는 고객이 트리링크를 떠돈다.")
		private String name;
	}

}
