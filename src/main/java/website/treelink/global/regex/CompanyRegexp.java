package website.treelink.global.regex;

public class CompanyRegexp {
	// private생성자로 외부(Spring 포함)에서 객체 생성 못 하게 막음
	private CompanyRegexp() {}
	
	public static final String BUSINESS_NO_REGEXP = "^[0-9]{10}$";
	public static final String COMPANY_NAME_REGEXP = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,100}$";
	public static final String REPRESENTATIVE_NAME_REGEXP = "^[ㄱ-ㅎ가-힣a-zA-Z0-9]{1,10}$";
	public static final String PHONE_REGEXP = "^[0-9]{1,15}$";
	public static final String EMAIL_REGEXP = "^(?=.{1,100}$)[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}$";
 	public static final String ETC_MEMO_REGEXP = "^.{1,20}$";
}
