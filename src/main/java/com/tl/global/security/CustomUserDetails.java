package com.tl.global.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.tl.member.MemberVO;

import lombok.RequiredArgsConstructor;

//spring security에서 사용하는 UserDetails 수정
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails{
	private final MemberVO.Detail memberDetail;
	private static final long serialVersionUID = 1L;
	
    //사용자 다중 권한 식별 장치
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (memberDetail.getRole() != null) {
            for (String role : memberDetail.getRole()) {
                authorities.add(new SimpleGrantedAuthority(role));
            }
        }
        return authorities;
    }

    // 비밀번호 검증
    @Override
    public String getPassword() {
        return memberDetail.getUserPwd();
    }

    // 아이디 검증
    @Override
    public String getUsername() {
        return memberDetail.getUserId();
    }
    
    // 이름 꺼내쓰기. 원 UserDetails 클래스에는 없는 기능.
    public String getName() {
    	return memberDetail.getName();
    }
    
    // 별명 꺼내쓰기
    public String getNickname() {
        return memberDetail.getNickname();
    }
    
    // 회원번호 꺼내기
    public String getEncryptedMemberNo() {
    	return memberDetail.getEncryptedMemberNo();
    }
    
    // 계정 만료 여부
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 계정 잠김 여부
    @Override
    public boolean isAccountNonLocked() {
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
