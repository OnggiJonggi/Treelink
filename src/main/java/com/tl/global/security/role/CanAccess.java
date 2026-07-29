package com.tl.global.security.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클라이언트 권한 확인
 * 
 * CheckAccessAspect에 구현됨
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CanAccess {
	RoleEnum[] value();
}
