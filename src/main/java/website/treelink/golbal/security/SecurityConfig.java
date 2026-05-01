package website.treelink.golbal.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 비밀번호 해싱
	@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // filterChain : CSRF방어, 세션/로그인/http요청 관리
    // 타임리프는 <form th:action>으로 토큰 자동 생성
	@Bean
    public SecurityFilterChain filterChain(HttpSecurity http
    		,CustomDynamicAuthorizationManager customAuthManager) throws Exception {

        // http요청 관리
        http.authorizeHttpRequests(auth -> auth
            .requestMatchers("/templates/**", "/favicon.ico").permitAll()
            .requestMatchers("/", "/member/join", "/member/login").permitAll()
            .anyRequest().permitAll()
//            .anyRequest().access(customAuthManager) // 커스텀한 클래스에서 결정
        );
        // requestMatchers(url경로) : 해당 경로
        // permitAll() : 접근 제한 없음
        // hasRole(권한) : 관계자 왜 출입금지?
        // anyRequest() : 그 외 모든 요청
        // authenticated(): 로그인된 사람만 (기본값)

        
        // 로그인 페이지 연결
        http.formLogin(form -> form
            .loginPage("/member/login") // 로그인 페이지
            .loginProcessingUrl("/member/loginProc") // 로그인 버튼 누르면 작동하는 페이지
            .usernameParameter("userId") // form태그 아이디 속성값 (기본값 username)
            .passwordParameter("userPw") // form태그 비번 속성값 (기본값 password)
            .defaultSuccessUrl("/") // 로그인 성공 시 이동할 페이지
            .failureUrl("/") // 로그인 실패 시 이동할 페이지 (기본값 로그인페이지+?error)
            .permitAll() // 접근 제한 없음
        );

        // 로그아웃 페이지 연결
        http.logout(logout -> logout
            .logoutUrl("/member/logout") //로그아웃 페이지
            .logoutSuccessUrl("/") //로그아웃 성공 시 이동할 페이지
            .invalidateHttpSession(true) //세션 삭제
            .deleteCookies("JSESSIONID") //쿠키 삭제
            //로그아웃 실패할 경우 400 혹은 403에러
        );

        return http.build();
    }
}

