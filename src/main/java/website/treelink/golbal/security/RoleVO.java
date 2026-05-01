package website.treelink.golbal.security;

import org.apache.ibatis.type.Alias;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public class RoleVO {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Alias("RoleUserNoUrl")
	public static class UserNoUrl{
		public int userNo;
		public String url;
	}
}
