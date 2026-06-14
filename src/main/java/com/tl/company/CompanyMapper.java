package com.tl.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyMapper {

	public List<CompanyVO.Detail> selectList(CompanyVO.Search companySearch);
	
	public int selectListTotalCount(CompanyVO.Search companySearch);
	
	public int insertCompany(CompanyVO.Registor companyRegistor);

	public int insertCompanySpecialty(
			@Param("companyNo") int companyNo
			,@Param("option") List<Integer> option
			,@Param("etcMemo") String etcMemo);
	
	public CompanyVO.Detail selectCompanyDetail(int companyNo);

	public int updateCompany(CompanyVO.Registor company);

	public void deleteCompanySpecialty(int companyNo);

	public String selectIntro(
			@Param("companyNo") int companyNo,
			@Param("status") CompanyStatusEnum status);

	public int updateIntro(int companyNo, String intro);

}
