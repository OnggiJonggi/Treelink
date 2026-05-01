package website.treelink.golbal.security;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper {
	public int selectUrlIsAllowed(RoleVO.UserNoUrl userNoUrl);
}
