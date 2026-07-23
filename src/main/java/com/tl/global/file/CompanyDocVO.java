package com.tl.global.file;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class CompanyDocVO {
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@ToString
	@Builder
	public static class HandOver{
		private int memberNo;
		private int companyNo;
		private FileDataVO file;
		private String docType;
		private LocalDate expireOn;
	}
	
	/**
	 * COMPANY_DOC 삽입용
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@Builder
	public static class Insert {
		private int companyNo;
		private int fileNo;
		private String docType; // 서류 종류, 식별 아이디 등등
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
	}
	
	
	/**
	 * COMPANY_DOC 조회용
	 */
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	@ToString
	public static class Detail{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		private String encFileNo;
		
		private String originalName;
		private long fileSize;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
		
		private String docType;
	}
}
