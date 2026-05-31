package com.tl.global.file;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class FileInfoVO {
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class Registor{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		
		private String originalName;
		
		private String changedName;
		private String mime;
		private long fileSize;
		private String savePath;
		private LocalDateTime savedAt; // java서버 기준
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
		
		private String companyUuid;
		
		private String docType;
	}
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class Detail{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		
		private String encryptedFileNo;
		private String originalName;
		private long fileSize;
		private LocalDateTime savedAt;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
		
		private String docType;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class GetFile{
		private String originalName;
		private String mime;
		private String savePath;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Builder
	public static class FileResult {
	    private Resource resource;
	    private String originalName;
	    private String mimeType;
	    private boolean inline; // true = 새 탭 렌더링, false = 다운로드
	}
	
	/**
	 * 파일 상태값 업테이트 전용
	 */
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@Builder
	public static class UpdateStatus {
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int companyNo; // selectKey로 조회
		
		private String companyUuid;
		
		private int docNo;
		private String status;
	}
	
	// FILE_HISTORY 삽입
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class InsertHistory {
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		
		private String originalName;
		private String savePath;
		private String action;
		
		private int actionBy;
	}
	
	
	
}
