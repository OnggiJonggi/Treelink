	package website.treelink.company;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import website.treelink.global.regex.CompanyRegexp;

@Builder
public class CompanyVO {
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class Registor {
		
		@NotBlank(message = "사업자 번호가 뭔가요")
		@Pattern(regexp = CompanyRegexp.BUSINESS_NO_REGEXP, message = "사업자 번호가 이상해요")
		private String businessNo;

		@NotBlank(message = "회사 이름이 뭐에요")
		@Pattern(regexp = CompanyRegexp.COMPANY_NAME_REGEXP, message = "회사 이름이 이상해요")
		private String companyName;

		@NotBlank(message = "대표 이름이 뭐에요")
		@Pattern(regexp = CompanyRegexp.REPRESENTATIVE_NAME_REGEXP, message = "대표 이름이 이상해요")
		private String representativeName;

		@NotBlank(message = "전화번호가 뭐에요")
		@Pattern(regexp = CompanyRegexp.PHONE_REGEXP, message = "회사 이름이 이상해요")
		private String phone;

		@Email(regexp = CompanyRegexp.EMAIL_REGEXP, message = "이메일이 이상해요")
		private String email;

		@NotNull(message = "언제인가요")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		@Past(message = "날짜가 이상해요")
		private LocalDate createDate;

		@Size(max = 3, message = "3개까지. 4개부터는 떽! 이야")
		private List<@Min(1) @Max(12) Integer> option;

		@Pattern(regexp = CompanyRegexp.ETC_MEMO_REGEXP, message = "기타 메모가 이상해요")
		private String etcMemo;
	}
}
