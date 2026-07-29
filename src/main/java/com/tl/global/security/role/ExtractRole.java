package com.tl.global.security.role;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 클라이언트의 권한에 따른 셋 이상의 컨트롤러 분기 요청
 * 
 * 소유한 권한 중 가장 높은 권한 하나만 추출
 * 
 * CheckRoleAspect의 resolveRole()에 구현됨
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface ExtractRole {
}