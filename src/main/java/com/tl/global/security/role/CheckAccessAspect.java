package com.tl.global.security.role;

import java.util.Arrays;
import java.util.List;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.security.CustomUserDetails;

import lombok.extern.slf4j.Slf4j;

@Aspect
@Component
@Slf4j
public class CheckAccessAspect {
	
	/**
	 * 권한 확인
	 * 없으면 403
	 */
	@Before("@annotation(canAccess)")
	public void canAccess(CanAccess canAccess)
			throws Throwable {
		CustomUserDetails userDetails = RoleUtil.getCurrentUserDetails();
		
		List<String> allowedPrefixes = Arrays.stream(canAccess.value()).map(RoleEnum::getPrefix).toList();
		
		if(userDetails == null || !userDetails.getAuthorities().stream()
				.anyMatch(a -> allowedPrefixes.contains(a.getAuthority())))
			throw new ResponseStatusException(HttpStatus.FORBIDDEN);
	}
}
