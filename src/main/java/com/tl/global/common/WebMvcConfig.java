package com.tl.global.common;

import java.util.List;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.tl.global.security.role.ExtractRoleArgumentResolver;
import com.tl.global.security.role.HasRoleArgumentResolver;

import lombok.RequiredArgsConstructor;

/**
 * ArgumentResolver 등록
 * security.role 위치의 두 권한제어 리졸버
 * 
 * ArgumentResolver : 컨트롤러 파라미터에 값을 자동으로 채워 넣음 
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final HasRoleArgumentResolver hasRoleArgumentResolver;
    private final ExtractRoleArgumentResolver extractRoleArgumentResolver;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(hasRoleArgumentResolver);
        resolvers.add(extractRoleArgumentResolver);
    }
}