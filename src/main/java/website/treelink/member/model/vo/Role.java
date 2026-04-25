package website.treelink.member.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
public class Role {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class UserNoUrl{
		public int userNo;
		public String url;
	}
}
