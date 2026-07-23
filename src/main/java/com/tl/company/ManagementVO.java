package com.tl.company;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.common.SearchPageVO;
import com.tl.global.common.SearchResultVO;
import com.tl.global.location.LocationVO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

public class ManagementVO {
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@ToString
	public static class Detail{
		private String name;
		private String memo;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate startOn;
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate endOn;
		
		private ManagementVisibleEnum visible;
		private ManagementStatusEnum status;
		
		// LOCATION 테이블
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int locationNo;
		private String encLocationNo;
		
		private double latitude;
		private double longitude;
		private String address;
		private String sido;
		private String sigungu;
		
		
		public void setLocationNo(int locationNo) {
			this.locationNo = locationNo;
		}
		public void setEncLocationNo(String encLocationNo) {
			this.encLocationNo = encLocationNo;
		}
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@Setter
	public static class Search extends SearchPageVO{
		private int companyNo;
		private boolean visible; // VISIBLE에 따른 조회 범위
	}
	
	/**
	 * 조회 수
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	public static class SearchCount{
		private int totalCount;
		private int semiCount;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@SuperBuilder
	public static class SearchResult{
		private SearchResultVO<Detail> result;
		
		// VISIBLE이 SEMI인 놈 수
		// Search.visible이 true이면 -1
		private int semiCount;
		
		// VISIBLE이 ALL인 놈 수
		// Search.visible이 true이면 -1
		// int allCount; - getter만 존재. 
		
		public int getAllCount() {
			
			// semiCount가 음수이면 야도 음수로 해요.
			if(this.semiCount<0) return -1;
			
			return result.getTotalCount() - this.semiCount;
		}
	}
	
	/**
	 * 작업 현황 추가 / 수정
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@ToString
	public static class Insert{
		private int companyNo;
		
		@NotBlank(message="현황 이름을 작성해 주세요")
		@Pattern(regexp = ManagementRegexp.NAME, message = "현황 이름이 이상해요")
		private String name;
		
		@NotBlank(message="상세 내용을 작성해 주세요")
		@Pattern(regexp = ManagementRegexp.NAME, message = "상세 내용이 이상해요")
		private String memo;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate startOn;
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate endOn;
		
		private ManagementVisibleEnum visible;
		private ManagementStatusEnum status;
		
		private LocationVO.Insert location;
	}
}
