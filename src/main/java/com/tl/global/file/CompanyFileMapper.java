package com.tl.global.file;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tl.company.CompanyStatusEnum;

@Mapper
public interface CompanyFileMapper {

	public List<FileInfoVO.Detail> selectInfo(int companyNo);
	
	public int deleteLogoInfo(int companyNo);
	
	public int insertCompanyDoc(CompanyFileVO.Insert fileInfo);
	
	public FileInfoVO.Basic selectBasic(
			@Param("companyNo") int companyNo,
			@Param("docNo") int docNo);

	public int deleteDoc(int fileNo);

	public FileInfoVO.Basic selectLogoSavePath(
			@Param("companyNo")int companyNo,
			@Param("isAdmin")boolean isAdmin);
	
	public FileInfoVO.History selectInfoForLogoHistory(int companyNo);

	public int deleteLogo(int fileNo);

	public FileInfoVO.Basic selectIntroImage(
			@Param("companyNo") int companyNo,
			@Param("changedName") String changedName,
			@Param("status") CompanyStatusEnum status);

	public List<FileInfoVO.History> selectUnusedIntro(
			@Param("companyNo") int companyNo,
			@Param("list") List<String> imageName);

	public int deleteUnusedIntroImage(List<Integer> list);
}
