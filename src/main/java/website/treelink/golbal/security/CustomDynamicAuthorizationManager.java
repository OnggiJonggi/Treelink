package website.treelink.golbal.security;

import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 클라이언트가 url접속을 시도하면 해당 url에 접근 권한이 있는지 확인
 */
@Component
public class CustomDynamicAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {
	
	private final RoleMapper roleMapper;
	public CustomDynamicAuthorizationManager(RoleMapper roleMapper) {
		this.roleMapper = roleMapper;
	}
	
	
	public AuthorizationDecision check(Supplier<? extends @Nullable Authentication> authentication2, 
                                       RequestAuthorizationContext context) {
        
		// url경로 확인
        HttpServletRequest request = context.getRequest();
        String requestUrl = request.getRequestURI();

        // 유저 확인
        Authentication authentication = authentication2.get();
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication2.get().getPrincipal();
        
        // 로그인 안 했으면 가세요라
        if (authentication == null || !authentication.isAuthenticated()) {
            return new AuthorizationDecision(false); 
        }

        // 회원번호 추출
        int userNo = customUserDetails.getUserNo();

        // 해당 주소에 접근이 가능하니
        // 접근 가능하면 true, 안돼면 false
        boolean isAllowed = false;
        RoleVO.UserNoUrl userNoUrl = new RoleVO.UserNoUrl(userNo, requestUrl);
        if(roleMapper.selectUrlIsAllowed(userNoUrl)==1) {
        	isAllowed = true;
        }
        
        return new AuthorizationDecision(isAllowed);
    }

	@Override
	public @Nullable AuthorizationResult authorize(Supplier<? extends @Nullable Authentication> authentication,
			RequestAuthorizationContext object) {
		return check(authentication, object);
	}
}