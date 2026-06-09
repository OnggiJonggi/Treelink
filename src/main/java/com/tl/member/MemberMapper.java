package com.tl.member;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tl.global.security.RoleEnum;

@Mapper
public interface MemberMapper {

	public MemberVO.Detail selectMemberById(String userId);
	
	public int selectCheckId(String userId);
	
	public int insertJoin(MemberVO.Join memberJoin);

	public int selectCheckNickname(String nickname);

	public List<MemberVO.Detail> selectMemberList(MemberVO.Search search);

	public int selectMemberListTotalCount(MemberVO.Search search);

	public MemberVO.Detail selectMember(int memberNo);

	public int updateMember(MemberVO.Update member);
	
	public int updateRole(@Param("memberNo") int memberNo,
			@Param("role") RoleEnum role);
	
	public int updateStatus(@Param("memberNo") int memberNo,
			@Param("status") MemberStatusEnum status);

	public int selectUpdatedNickname(int memberNo, String nickname);
}
