package com.tl.global.file;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.file.component.FileStatusEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

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
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
		
		private int companyNo;
		
		private String docType; // 서류 종류, 식별 아이디 등등
	}
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	@ToString
	public static class Detail{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		private String encryptedFileNo;
		
		private String originalName;
		private long fileSize;
		
		@DateTimeFormat(pattern = "yyyy-MM-dd")
		private LocalDate expireOn;
		
		private String docType;
	}
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	public static class GetFile{
		private String originalName;
		private String changedName;
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
	
	// FILE_HISTORY 삽입
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class History {
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private Integer fileNo; // 파일 삭제할 때 null (fk참조 안 함)
		
		private String originalName;
		private String changedName;
		private String savePath;
		private LocalDateTime actionAt;
		private FileStatusEnum action;
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int actionBy;
	}
	
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Getter
	@ToString
	public static class SavePath{
		private String changedName;
		private String mime;
		private String savePath;
		
		
		public void setSavePath(String savePath) {
			this.savePath = savePath;
		}
	}
}
