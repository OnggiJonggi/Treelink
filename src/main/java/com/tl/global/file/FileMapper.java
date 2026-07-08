package com.tl.global.file;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {

	public int insertInfo(FileInfoVO.Insert fileInfo);
	
	public int insertHistory(FileInfoVO.History insertHistory);
	
	public FileInfoVO.History selectInfoForHistory(int fileNo);
}
