package com.tl.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tl.global.file.FileInfoVO;

@Mapper
public interface CompanyDocMapper {

	List<FileInfoVO.Detail> selectInfo(String companyUuid);
	
	void insertInfo(FileInfoVO.Registor fileInfo);

	FileInfoVO.GetFile selectGetFile(
			@Param("companyUuid") String companyUuid,
			@Param("docNo") int docNo);

	void updateDocStatus(FileInfoVO.UpdateStatus updateStatus);

	FileInfoVO.InsertHistory selectInfoForHistory(int docNo);
	
	void insertHistory(FileInfoVO.InsertHistory before);


}
