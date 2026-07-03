package com.tl.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyLocationMapper {

	public List<CompanyVO.LocationDetail> selectCompanyLocations(int companyNo);

	public int insert(CompanyVO.InsertLocation companyLocation);

	public int delete(
			@Param("companyNo") int companyNo,
			@Param("locationNo") int locationNo);

}
