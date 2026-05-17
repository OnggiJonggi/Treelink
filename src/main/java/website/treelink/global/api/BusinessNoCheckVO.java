package website.treelink.global.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import website.treelink.global.regex.CompanyRegexp;

public class BusinessNoCheckVO {
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class request{
		
		@NotBlank(message = "사업자 번호가 뭔가요")
		@Pattern(regexp = CompanyRegexp.BUSINESS_NO_REGEXP, message = "사업자 번호가 이상해요")
		private String businessNo;
		
		@NotBlank(message = "대표 이름이 뭐에요")
		@Pattern(regexp = CompanyRegexp.REPRESENTATIVE_NAME_REGEXP, message = "대표 이름이 이상해요")
		private String representativeName;
		
		@NotNull(message = "창립일이 언제에요")
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate createDate;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Builder
	@ToString
	public static class apiRequest{
	    private String b_no; // 사업자등록번호
	    private String start_dt; // 개업일자 YYYYMMDD
	    private String p_nm; // 대표자성명
	    private String p_nm2; // 대표자성명2 (선택)
	    private String b_nm; // 상호 (선택)
	    private String corp_no; // 법인등록번호 (선택)
	    private String b_sector; // 주업태명 (선택)
	    private String b_type; // 주종목명 (선택)
	}
	
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class apiResponseWrapper {
        private String status_code; // 응답 상태 코드
        private List<apiResponse> data; // 실제 데이터 목록
    }
    
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class apiResponse{
        private String b_no; // 사업자등록번호
        private String valid; // 01: 일치, 02: 불일치
        private String valid_msg; // 결과 메시지
        private String b_stt; // 사업자 상태 (계속사업자, 휴업자, 폐업자)
        private String b_stt_cd; // 상태코드 (01:계속, 02:휴업, 03:폐업)
        private String tax_type; // 과세유형
        private String end_dt; // 폐업일자
	}
}
