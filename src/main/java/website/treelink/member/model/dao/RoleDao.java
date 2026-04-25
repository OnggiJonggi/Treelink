package website.treelink.member.model.dao;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import website.treelink.member.model.vo.Role;

@Repository
public class RoleDao {
	@Autowired
	private SqlSessionTemplate sqlSession;

	public int selectUrlIsAllowed(Role.UserNoUrl userNoUrl) {
		return sqlSession.selectOne("roleMapper.selectUrlIsAllowed", userNoUrl);
	}
	
}
