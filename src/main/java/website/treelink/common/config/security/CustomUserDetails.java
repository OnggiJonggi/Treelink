package website.treelink.common.config.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import website.treelink.member.model.vo.Member;

//spring security에서 사용하는 UserDetails 수정
public class CustomUserDetails implements UserDetails{
	
	private static final long serialVersionUID = 1L;
	
    private final Member.Login member;
    
    public CustomUserDetails(Member.Login member) {
        this.member = member;
    }


    //사용자 다중 권한 식별 장치
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (member.getRoles() != null) {
            for (String role : member.getRoles()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }
        return authorities;
    }

    // 비밀번호 검증
    @Override
    public String getPassword() {
        return member.getUserPwd();
    }

    // 아이디 검증
    @Override
    public String getUsername() {
        return member.getUserId();
    }
    
    // 이름 꺼내쓰기. 원 UserDetails 클래스에는 없는 기능.
    public String getNickName() {
        return member.getName();
    }
    
    // 회원번호 꺼내기. 오버라이드 없네?
    public int getUserNo() {
    	return member.getUserNo();
    }
    
    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
    	
    	String state = member.getState();
    	if(state!=null & "E".equals(state)) return false;
    	
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
    	
    	String state = member.getState();
    	if(state!=null & "L".equals(state)) return false;
    	
        return true;
    }

    // 비밀번호 만료 여부
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 계정 활성화 여부
    @Override
    public boolean isEnabled() {
        return true;
    }
}
