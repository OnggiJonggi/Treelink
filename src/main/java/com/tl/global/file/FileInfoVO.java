package com.tl.global.file;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.file.component.FileStatusEnum;
import com.tl.global.file.component.RootSavePathEnum;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

public class FileInfoVO {
	
	@NoArgsConstructor
	@AllArgsConstructor
	@Data
	@Builder
	public static class Insert{
		
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		
		private String originalName;
		private String changedName;
		private String mime;
		private long fileSize;
		private String savePath;
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
	@ToString
	public static class Basic{
		private String originalName;
		private String changedName;
		private String mime;
		private String savePath;
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
	
	// FileComponent.save() 전용 객체
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@SuperBuilder
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class HandOver extends FileDataVO{
		int memberNo;
		RootSavePathEnum rootSavePath;
		
		public HandOver(FileDataVO file, int memberNo, RootSavePathEnum rootSavePath) {
			super(file.getOriginalName(), file.getMime(), file.getSize(), file.getBytes());
			this.memberNo = memberNo;
			this.rootSavePath = rootSavePath;
		}
	}
}
