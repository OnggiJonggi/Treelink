package website.treelink.member;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import website.treelink.global.exception.CustomException;
import website.treelink.global.exception.ErrorCode;

/**
 * 회원 계정 관리 클라쓰
 * 로그인/로그아웃은 MemberSecurityService가서 찾아라
 */
@Service
@RequiredArgsConstructor
public class MemberAccountServiceImpl implements MemberAccountService{
	private final MemberDao memberDao;
	private final PasswordEncoder passwordEncoder;
	
	/**
	 * 회원 가입
	 */
	@Transactional
	public void join(MemberVO.Join memberJoin) {
		
		// 아이디 중복 검사
		if(memberDao.selectCheckId(memberJoin.getUserId()) > 0 )
			throw new CustomException(ErrorCode.ID_IS_DUPLICATED);
		
		// 비번 암호화
		memberJoin.setUserPwd(passwordEncoder.encode(memberJoin.getUserPwd()));
		
		// DB에 추가하기
		if(memberDao.insertMember(memberJoin) ==0)
			throw new CustomException(ErrorCode.CANNOT_CREATE_MEMBER);

		return;
	}

	/**
	 * 아이디 중복확인
	 */
	@Override
	public void checkId(String userId) {
		if(memberDao.selectCheckId(userId) > 0)
			throw new CustomException(ErrorCode.ID_IS_DUPLICATED);
	}
	
}
