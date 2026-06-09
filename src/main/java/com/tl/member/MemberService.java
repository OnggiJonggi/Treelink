package com.tl.member;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.common.SearchResultVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.security.CryptoComponent;
import com.tl.global.security.RoleEnum;

import lombok.RequiredArgsConstructor;

/**
 * 회원 계정 관리 클라쓰
 * 로그인/로그아웃은 MemberSecurityService가서 찾아라
 */
@Service
@RequiredArgsConstructor
public class MemberService{
	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	private final CryptoComponent cryptoComponent;
	
	/**
	 * 회원 가입
	 */
	@Transactional
	public void join(MemberVO.Join memberJoin) {
		// 아이디 중복 검사
		if(memberMapper.selectCheckId(memberJoin.getUserId()) > 0 )
			throw new CustomException(ErrorCodeEnum.ID_IS_DUPLICATED);
		
		// 닉네임 중복 검사
		if(memberMapper.selectCheckNickname(memberJoin.getNickname()) > 0)
			throw new CustomException(ErrorCodeEnum.NICKNAME_IS_DUPLICATED);
		
		// 비번 암호화
		memberJoin.setUserPwd(passwordEncoder.encode(memberJoin.getUserPwd()));
		
		// DB에 추가하기
		if(memberMapper.insertJoin(memberJoin) ==0)
			throw new CustomException(ErrorCodeEnum.CANNOT_CREATE_MEMBER);

		return;
	}

	/**
	 * 아이디 중복확인
	 */
	public void checkId(String userId) {
		if(memberMapper.selectCheckId(userId) > 0)
			throw new CustomException(ErrorCodeEnum.ID_IS_DUPLICATED);
	}
	
	/**
	 * 닉네임 중복 확인
	 */
	public void checkNick(String nickname) {
		if(memberMapper.selectCheckNickname(nickname) > 0)
			throw new CustomException(ErrorCodeEnum.NICKNAME_IS_DUPLICATED);
	}
	

	/**
	 * 회원 목록 확인
	 */
	public SearchResultVO<MemberVO.Detail> getList(MemberVO.Search search) throws Exception {
		
		// 목록 조회
		List<MemberVO.Detail> result = memberMapper.selectMemberList(search);
		
		// 검색 결과 수
		int totalCount = memberMapper.selectMemberListTotalCount(search);

		// SearchResultVO로 감싸기
		SearchResultVO<MemberVO.Detail> searchResult = new SearchResultVO<MemberVO.Detail>(
				result, totalCount, search.getPage());
		
		// 회원 식별번호 암호화
		for(MemberVO.Detail member : searchResult.getList()) {
			member.setEncryptedMemberNo(cryptoComponent.encrypt(String.valueOf(member.getMemberNo())));
			member.setMemberNo(0);
		}
		
		return searchResult;
	}

	/**
	 * 회원 기본 정보 조회
	 */
	public MemberVO.Detail getBasicInfo(int memberNo) {
		return memberMapper.selectMember(memberNo);
	}

	/**
	 * 회원 기본 정보 수정
	 */
	public void updateMemberBasicInfo(MemberVO.Update member) {
		
		// 별명 중복 확인
		if(memberMapper.selectUpdatedNickname(member.getMemberNo(), member.getNickname()) > 0)
			throw new CustomException(ErrorCodeEnum.NICKNAME_IS_DUPLICATED);
		
		// 비번 암호화
		if(member.getUserPwd()!=null
				&& !member.getUserPwd().isEmpty())
			member.setUserPwd(passwordEncoder.encode(member.getUserPwd()));

		int result = memberMapper.updateMember(member);
		if(result==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 회원 권한 수정 
	 */
	public void updateMemberRole(int memberNo, RoleEnum role) {
		int result = memberMapper.updateRole(memberNo, role);
		if(result==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 회원 상태 수정
	 */
	public void updateMemberStatus(int memberNo, MemberStatusEnum status) {
		int result = memberMapper.updateStatus(memberNo,status);
		if(result==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	}

	/**
	 * 닉네임 중복 확인 (수정용)
	 */
	public void checkUpdatedNickname(int memberNo, String nickname) {
		if(memberMapper.selectUpdatedNickname(memberNo, nickname) > 0)
			throw new CustomException(ErrorCodeEnum.NICKNAME_IS_DUPLICATED);
	}
}
