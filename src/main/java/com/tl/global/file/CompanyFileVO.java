package com.tl.global.file;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class CompanyFileVO {
	
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
}
