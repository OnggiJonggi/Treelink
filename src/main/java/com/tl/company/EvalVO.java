package com.tl.company;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tl.global.common.SearchPageVO;
import com.tl.global.file.EvalDocVO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class EvalVO {

	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@ToString
	public static class Detail{
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int evaluationNo;
		
		// EVALUATION_SCORE 테이블
		private List<ScoreDetail> scores;
		
		// EVALUATION_DOC 테이블
		private List<EvalDocVO.Detail> files;
		
		public void setEvaluationNo(int evaluationNo) {
			this.evaluationNo = evaluationNo;
		}
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@ToString
	public static class ScoreDetail{
		private int score;
		private String reson;
		
		// EVALUATION_ITEM 테이블
		private int itemNo;
		private String field;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@ToString
	public static class Insert{
		private Integer evaluationNo;
		
		// EVALUATION_SCORE 테이블
		private List<InsertScore> scores;
		
		// EVALUATION_HISTORY 테이블
		private Integer historyNo;
		@NotBlank(message="평가 수정 이유가 없어요")
		@Pattern(regexp = EvalRegexp.ACTION_RESON, message="이유가 이상해요")
		private String actionReson;
		
		private Integer revisionNo;
		private Integer actionBy;
		
		// FILE_INFO 테이블
		private Set<String> encFileNos;
		private Set<Integer> fileNos;
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@ToString
	public static class InsertScore{
		private Integer itemNo;
		private Integer score;
		
		@NotBlank(message="점수 배정 이유가 없어요")
		@Pattern(regexp = EvalRegexp.RESON, message="이유가 이상해요")
		private String reson;
		
		// EVALUATION_ITEM 테이블
		@NotBlank(message="점수 항목이 없어요")
		private String field;
	}
	
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Getter
	@ToString
	public static class HistoryDetail{
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int historyNo;
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int evaluationNo;
		@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
		private int evaluateBy;
		
		private int revisionNo;
		private LocalDateTime updateAt;
		private String actionReson;
		
		// EVALUATION_SCORE_HISTORY 테이블
		private List<ScoreDetail> scores;
		
		// MEMBER 테이블
		private String nickname;
		
		// EVALUATION_DOC 테이블
		private List<EvalDocVO.Detail> files;
		
		
		public void setHistoryNo(int historyNo) {
			this.historyNo = historyNo;
		}
		public void setEvaluationNo(int evaluationNo) {
			this.evaluationNo = evaluationNo;
		}
		public void setEvaluateBy(int evaluateBy) {
			this.evaluateBy = evaluateBy;
		}
	}
	
	@AllArgsConstructor
	@NoArgsConstructor
	@Data
	@ToString(callSuper = true)
	@EqualsAndHashCode(callSuper = true)
	public static class Search extends SearchPageVO{
		private int evaluationNo;
		private boolean isAll; // 검색 범위 제한용
	}
	
}
