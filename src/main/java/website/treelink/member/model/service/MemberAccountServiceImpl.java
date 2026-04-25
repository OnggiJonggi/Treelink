package website.treelink.member.model.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import website.treelink.common.exception.CustomException;
import website.treelink.common.exception.ErrorCode;
import website.treelink.member.model.dao.MemberDao;
import website.treelink.member.model.vo.Member;

/**
 * 회원 계정 관리 클라쓰
 * 로그인/로그아웃은 MemberSecurityService가서 찾아라
 */
@Service
@RequiredArgsConstructor
public class MemberAccountServiceImpl implements MemberAccountService{
	private final MemberDao memberDao;
	
	/**
	 * 회원 가입
	 */
	@Transactional
	public void newAccount(Member.NewAccount newAccount) {
		// 비번 암호화
		
		// DB에 추가하기
		if(memberDao.insertMember(newAccount) ==0)
			throw new CustomException(ErrorCode.CANNOT_CREATE_MEMBER);

		return;
	}
}
