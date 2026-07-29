package com.tl.global.security.role;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.tl.global.security.CustomUserDetails;

public class RoleUtil {
	
	/**
	 * 컨트롤러 외부에서 UserDetails사용하기
	 * 
	 * CanAccessAspect,
	 * ExtractRoleArgumentResolver,
	 * HasRoleArgumentResolver 에서 공통 사용
	 */
	public static CustomUserDetails getCurrentUserDetails() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails)) {
			return null;
		}
		return (CustomUserDetails) authentication.getPrincipal();
	}
}
