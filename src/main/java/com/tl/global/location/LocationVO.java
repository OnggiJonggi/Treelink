package com.tl.global.location;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class LocationVO {

	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@ToString
	@Builder
	public static class Insert {
		private int locationNo;
		private double latitude;
		private double longitude;
		
		@NotBlank(message="주소를 입력해 주세요")
		@Pattern(regexp = LocationRegexp.ADDRESS_REGEXP, message = "도로롱주소가 이상해요")
		private String address;
		
		@NotBlank(message="시/도를 입력해 주세요")
		@Pattern(regexp = LocationRegexp.SIDO_REGEXP, message = "시/도 이름이 이상해요")
		private String sido;
		
		@Pattern(regexp = LocationRegexp.SIGUNGU_REGEXP, message = "시/군/구 이름이 이상해요")
		private String sigungu;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@ToString
	public static class Detail {
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int locationNo;
		private String encLocationNo; // locationNo 암호화

		private double latitude;
		private double longitude;
		private String address;
		private String sido;
		private String sigungu;

		// api 요청으로 받은 값 변환
		public Detail(GeocodingApiResponse input) {
			this.address = input.getAddress_name();
			this.sido = input.getRegion_1depth_name();
			this.sigungu = input.getRegion_2depth_name();
			
			// 위/경도 소숫점 7자리만 남기고 버림
			BigDecimal latBd = new BigDecimal(input.getY());
			this.latitude = latBd.setScale(7, RoundingMode.DOWN).doubleValue();
			BigDecimal longBd = new BigDecimal(input.getX());
			this.longitude = longBd.setScale(7, RoundingMode.DOWN).doubleValue();
		}

		public void setLocationNo(int locationNo) {
			this.locationNo = locationNo;
		}
		public void setEncLocationNo(String encLocationNo) {
			this.encLocationNo = encLocationNo;
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GeocodingApiResponseWrapper1 {
		private Meta meta;
		private List<GeocodingApiResponseWrapper2> documents;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Meta {
		private int total_count;
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GeocodingApiResponseWrapper2 {
		private GeocodingApiResponse address; // 지번
		private GeocodingApiResponse road_address; // 도로롱
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	public static class GeocodingApiResponse {
		private String address_name; // 도로명 주소
		private String region_1depth_name; // 시/도
		private String region_2depth_name; // 시/군/구
		private String x; // 경도
		private String y; // 위도
	}

}
