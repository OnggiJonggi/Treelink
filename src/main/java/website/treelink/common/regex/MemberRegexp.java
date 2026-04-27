package website.treelink.common.regex;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MemberRegexp {
	
	public static final String ID_REGEXP = "[A-Za-z0-9]{4,12}$";
	public static final String PWD_REGEXP = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[@$!%*#?&])[A-Za-z\\\\d@$!%*#?&]{4,20}$";
	public static final String NAME_REGEXP = "/^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$/";
}
