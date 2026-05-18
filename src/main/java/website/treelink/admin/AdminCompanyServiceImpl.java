package website.treelink.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import website.treelink.company.CompanyMapper;
import website.treelink.company.CompanyVO;

@Service
public class AdminCompanyServiceImpl implements AdminCompanyService{

	private final CompanyMapper companyMapper;
	public AdminCompanyServiceImpl( CompanyMapper companyMapper) {
		this.companyMapper = companyMapper;
	}
	
	@Override
	@Transactional
	public int companyRegistor(CompanyVO.Registor companyRegistor) {
		// 회사 등록
		companyMapper.insertCompany(companyRegistor);
		
		// 주 종목 등록
		if(companyRegistor.getOption() != null) {
			companyMapper.insertCompanySpecialty(
					companyRegistor.getCompanyNo()
					,companyRegistor.getOption()
					,companyRegistor.getEtcMemo());
		}
		
		return companyRegistor.getCompanyNo();
	}

}
