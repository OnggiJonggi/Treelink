package website.treelink.company;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CompanyMapper {

	public int insertCompany(CompanyVO.Registor companyRegistor);

	public void insertCompanySpecialty(
			@Param("companyNo") int companyNo
			,@Param("option") List<Integer> option
			,@Param("etcMemo") String etcMemo);

}
