package website.treelink.common.regex;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class MemberRegexp {
	
	public static final String ID_REGEXP = "[A-Za-z0-9]{4,12}$";
	public static final String PWD_REGEXP = "^(?=.*[A-Za-z])(?=.*\\\\d)(?=.*[@$!%*#?&])[A-Za-z\\\\d@$!%*#?&]{4,20}$";
}
