package com.tl.global.security.role;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.tl.global.security.CustomUserDetails;

@Component
public class ExtractRoleArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(ExtractRole.class) && parameter.getParameterType().equals(RoleEnum.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		CustomUserDetails userDetails = RoleUtil.getCurrentUserDetails();

		return (userDetails == null) ? RoleEnum.NULL : RoleEnum.extractRole(userDetails.getAuthorities());
	}
}