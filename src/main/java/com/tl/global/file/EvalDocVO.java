package com.tl.global.file;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.file.component.FileRegexp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class EvalDocVO {
	
	/**
	 * EVALUATION_DOC 삽입용
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@Data
	public static class Insert {
		private int fileNo;
		
		@NotBlank(message="메모가 없어요")
		@Pattern(regexp = FileRegexp.EVAL_MEMO, message="메모가 이상해요")
		private String memo;
	}
	
	/**
	 * EVALUATION_DOC 조회용
	 */
	@AllArgsConstructor
	@NoArgsConstructor
	@ToString
	@Getter
	public static class Detail{
		private String memo;
		
		// FILE_INFO 테이블
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int fileNo;
		private String encFileNo;
		
		private String changedName;
		private String originalName;
		private long fileSize;
		
		
		public void setFileNo(int fileNo) {
			this.fileNo = fileNo;
		}
		public void setEncFileNo(String encFileNo) {
			this.encFileNo = encFileNo;
		}
	}
}
