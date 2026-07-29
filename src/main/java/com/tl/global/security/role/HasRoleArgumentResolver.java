package com.tl.global.security.role;

import java.util.Arrays;
import java.util.List;

import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.tl.global.security.CustomUserDetails;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class HasRoleArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(HasRole.class)
				&& parameter.getParameterType().equals(boolean.class);
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {

		HasRole annotation = parameter.getParameterAnnotation(HasRole.class);

		CustomUserDetails userDetails = RoleUtil.getCurrentUserDetails();
		List<String> allowedPrefixes = Arrays.stream(annotation.value()).map(RoleEnum::getPrefix).toList();

		boolean hasRole = userDetails != null
				&& userDetails.getAuthorities().stream().anyMatch(a -> allowedPrefixes.contains(a.getAuthority()));

		return hasRole;
	}
}