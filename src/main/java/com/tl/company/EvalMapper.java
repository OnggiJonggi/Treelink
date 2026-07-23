package com.tl.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvalMapper {

	public EvalVO.Detail selectEval(
			@Param("companyNo") int companyNo,
			@Param("isAll") boolean isAll);

	public int selectRevisionNo(int evaluationNo);

	public int insert(int evaluationNo);
	
	public int insertScore(EvalVO.Insert insert);

	public int insertHistory(EvalVO.Insert insert);
	
	public int insertHistoryScore(EvalVO.Insert insert);
	
	public int update(
			@Param("evaluationNo") int evaluationNo,
			@Param("insertScore") EvalVO.InsertScore insertScore);

	public List<EvalVO.HistoryDetail> selectHistoryList(EvalVO.Search search);

	public int selectHistoryTotalCount(EvalVO.Search search);

	public EvalVO.HistoryDetail selectRevision(
			@Param("companyNo") int companyNo,
			@Param("revisionNo") int revisionNo,
			@Param("isAll") boolean isAll);

	public int selectHistoryNo(int evaluationNo);

	public List<EvalVO.ScoreDetail> selectOldScores(int evaluationNo);
}
