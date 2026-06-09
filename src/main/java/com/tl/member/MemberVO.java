package com.tl.member;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.common.SearchPageVO;
import com.tl.global.security.RoleEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class MemberVO {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class Login{
		private String userId;
		private String userPwd;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class Detail{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int memberNo;
		
		private String encryptedMemberNo;
		private String userId;
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private String userPwd;
		private String name;
		private String nickname;
		private String phone;
		private MemberStatusEnum status;
		
		private List<String> role;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class Join{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
		private String nickname;
	}
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Search extends SearchPageVO{
		private String userId;
		private String name;
		private String nickname;
		private String phone;
		private MemberStatusEnum status;
		private RoleEnum role;
	}
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@ToString
	public static class Update{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int memberNo;
		
		private String encryptedMemberNo;
		
		@Pattern(regexp = MemberRegexp.PWD_REGEXP, message="비번 유효성에 안 맞잖아!")
		private String userPwd;
		
		@NotBlank(message="이름없는 고객이 트리링크를 떠돈다.")
		@Pattern(regexp = MemberRegexp.NAME_REGEXP, message="이름이 이상해요.")
		private String name;
		
		@NotBlank(message="별명없는 고객이 트리링크를 떠돈다.")
		@Pattern(regexp = MemberRegexp.NAME_REGEXP, message="제대로 된 별명 주세요.")
		private String nickname;
		
		@Pattern(regexp = MemberRegexp.PHONE_REGEXP, message="전화번호 이게 맞아요?")
		private String phone;
	}

}
