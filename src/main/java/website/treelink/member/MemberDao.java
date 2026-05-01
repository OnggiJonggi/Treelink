package website.treelink.member;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDao {
	
	@Autowired
	private SqlSessionTemplate sqlSession;

	public MemberVO.Detail selectMemberById(String userId) {
		return sqlSession.selectOne("memberMapper.selectMemberById", userId);
	}
	
	public int selectCheckId(String userId) {
		return sqlSession.selectOne("memberMapper.selectCheckId", userId);
	}
	
	public int insertMember(MemberVO.Join memberJoin) {
		return sqlSession.insert("memberMapper.insertMember", memberJoin);
	}

	
}
