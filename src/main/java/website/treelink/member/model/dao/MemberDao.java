package website.treelink.member.model.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import website.treelink.member.model.vo.Member;

@Repository
public class MemberDao {
	
	@Autowired
	private SqlSessionTemplate sqlSession;

	public Member.Login selectMemberById(String userId) {
		return sqlSession.selectOne("memberMapper.selectMemberById", userId);
	}
	
	public int insertMember(Member.NewAccount newAccount) {
		return sqlSession.insert("memberMapper.insertMember", newAccount);
	}

	
}
