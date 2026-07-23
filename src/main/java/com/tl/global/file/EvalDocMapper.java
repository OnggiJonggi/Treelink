package com.tl.global.file;

import java.util.Set;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EvalDocMapper {
	public void insert(EvalDocVO.Insert insert);

	public int updateHistoryNo(
			@Param("list") Set<Integer> fileNos,
			@Param("historyNo") int historyNo);

	public FileInfoVO.Basic selectBasic(
			@Param("companyNo") int companyNo,
			@Param("fileNo") int fileNo,
			@Param("isAll") boolean isAll);
}
