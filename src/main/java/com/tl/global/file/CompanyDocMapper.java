package com.tl.global.file;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyDocMapper {

	public List<FileInfoVO.Detail> selectInfo(int companyNo);
	
	public void insertInfo(FileInfoVO.Registor fileInfo);
	
	public FileInfoVO.GetFile selectGetFile(
			@Param("companyNo") int companyNo,
			@Param("docNo") int docNo);

	public void deleteDoc(int fileNo);
	
	public FileInfoVO.History selectInfoForHistory(int fileNo);
}
