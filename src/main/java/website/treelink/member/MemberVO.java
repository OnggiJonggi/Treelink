package website.treelink.member;

import java.util.List;

import org.apache.ibatis.type.Alias;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import website.treelink.global.regex.MemberRegexp;
@Builder
public class MemberVO {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Alias("MemberLogin")
	public static class Login{
		private String userId;
		private String userPwd;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Alias("MemberDetail")
	public static class Detail{
		private int memberNo;
		private String userId;
		private String userPwd;
		private String name;
		private String nickName;
		private List<String> roles;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Setter
	@Alias("MemberJoin")
	public static class Join{
		private int memberNo;
		
		@NotBlank(message="아이디를 입력해라.")
		@Pattern(regexp = MemberRegexp.ID_REGEXP, message="아이디가 이상해요.")
		private String userId;
		
		@NotBlank(message="비밀번호 입력해.")
		@Pattern(regexp = MemberRegexp.PWD_REGEXP, message="비번 유효성에 안 맞잖아!")
		private String userPwd;
		
		@NotBlank(message="이름없는 고객이 트리링크를 떠돈다.")
		@Pattern(regexp = MemberRegexp.NAME_REGEXP, message="이름이 이상해요.")
		private String name;
		
		@NotBlank(message="별명없는 고객이 트리링크를 떠돈다.")
		@Pattern(regexp = MemberRegexp.NAME_REGEXP, message="제대로 된 별명 주세요.")
		private String nickName;
	}

}
