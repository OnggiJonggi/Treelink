package website.treelink.member;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import website.treelink.golbal.security.CustomUserDetails;

/**
 * spring security에서 사용하는 로그인 서비스 로직
 * 사실상 로그인 전용
 */
@Service
@RequiredArgsConstructor // final이 사용된 필드의 생성자@autowired 자동 생성
public class MemberSecurityService implements UserDetailsService{
	
	//sqltemplate는 서비스 단계에서 쓰면 안돼요!
	private final MemberDao memberDao;

	// 로그인
	@Override
	public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        
		// id로 회원 추출
		MemberVO.Detail memberDetail = memberDao.selectMemberById(userId);
        
        if (memberDetail == null)
        	throw new UsernameNotFoundException("그런 사람 없다는데요");

        return new CustomUserDetails(memberDetail);
	}
}
