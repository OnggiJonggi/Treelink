	package com.tl.company;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.common.SearchPageVO;
import com.tl.global.location.LocationVO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class CompanyVO {
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	public static class Registor {
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int companyNo;
		
		@NotBlank(message = "사업자 번호가 뭔가요")
		@Pattern(regexp = CompanyRegexp.BUSINESS_NO_REGEXP, message = "사업자 번호가 이상해요")
		private String businessNo; // 사업자 등록번호. 하이픈 없이 숫자만.

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
		private LocalDate createdOn;

		@Size(max = 3, message = "3개까지. 4개부터는 떽! 이야")
		private List<@Min(1) @Max(99) Integer> option;

		@Pattern(regexp = CompanyRegexp.ETC_MEMO_REGEXP, message = "기타 메모가 이상해요")
		private String etcMemo;
		
		private CompanyStatusEnum status;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@ToString
	public static class Detail {
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int companyNo;
		private String encryptedCompanyNo;
		
		private String businessNo; // 사업자 등록번호. 하이픈 없이 숫자만.
		private String companyName;
		private String representativeName;
		private String phone;
		private String email;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate createdOn;
		private CompanyStatusEnum status;
		private List<String> option;
		private String etcMemo;
		
		
		public void setCompanyNo(int companyNo) {
			this.companyNo = companyNo;
		}
		public void setEncryptedCompanyNo(String encryptedCompanyNo) {
			this.encryptedCompanyNo = encryptedCompanyNo;
		}
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Search extends SearchPageVO{
		private String businessNo;
		private String companyName;
		private String representativeName;
		private CompanyStatusEnum status; // 관리자만
		
		private String option;
		private String etcMemo;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class InsertLocation extends LocationVO.Insert{
		private int companyNo;
		
		@Pattern(regexp = CompanyRegexp.LOCATION_NAME_REGEXP, message = "업체 위치 이름이 이상해요")
		private String name;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class LocationDetail extends LocationVO.Detail{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int companyNo;
		private String encComopanyNo;
		
		private String name;
		
	}
}
