package com.tl.global.security.role;

import java.util.Collection;
import java.util.Comparator;

import org.springframework.security.core.GrantedAuthority;

/**
 * ROLE_INFO 테이블의 NAME
 */
public enum RoleEnum {
	SUPER_ADMIN,
	ADMIN,
	EVALUATOR,
	VIEWER,
	
	NULL, // 비 로그인 사용자
	;
	
	/**
	 * spring security 형태로 바꾸기
	 * @return ROLE_ 추가
	 */
	public String getPrefix() {
		return "ROLE_"+this.name();
	}
	
	/**
	 * 권한 중 하나 불러오기.
	 * 여러 권한이 있다면, 위에 적힌 권한이 우선됨
	 */
	public static RoleEnum extractRole(Collection<? extends GrantedAuthority> authorities) {
		
		if (authorities == null || authorities.isEmpty()) return NULL;

		return authorities.stream()
				.map(GrantedAuthority::getAuthority)
				.map(RoleEnum::fromPrefix)
				.filter(role -> role != null)
				.min(Comparator.comparingInt(Enum::ordinal))
				.orElse(NULL);
	}
	
	/**
	 * prefix문자열을 RoleEnum으로 변경
	 */
	private static RoleEnum fromPrefix(String prefix) {
		for (RoleEnum role : values()) {
			if (role.getPrefix().equals(prefix)) {
				return role;
			}
		}
		return null;
	}
}
