package com.tl.global.security.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클라이언트의 권한에 따른 컨트롤러 분기 요청
 * 
 * 소유한 권한 중 그 권한이 있는지 확인
 * 
 * HasRoleArgumentResolver에 구현됨
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface HasRole {
	RoleEnum[] value();
}
