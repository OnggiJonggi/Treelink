package com.tl.global.file;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FileMapper {

	public void insertHistory(FileInfoVO.History insertHistory);

	public FileInfoVO.SavePath selectLogoSavePath(
			@Param("companyNo")int companyNo,
			@Param("isAdmin")boolean isAdmin);
}
