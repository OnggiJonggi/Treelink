package com.tl.global.file;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper {

	public int insertHistory(FileInfoVO.History insertHistory);
}
