package website.treelink.admin;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import website.treelink.company.CompanyMapper;
import website.treelink.company.CompanyVO;
import website.treelink.global.api.BusinessNoCheckService;

@Service
public class AdminCompanyServiceImpl implements AdminCompanyService{

	private final CompanyMapper companyMapper;
	private final BusinessNoCheckService businessNoCheckService;
	public AdminCompanyServiceImpl( CompanyMapper companyMapper
			,BusinessNoCheckService checkBusinessNo) {
		this.companyMapper = companyMapper;
		this.businessNoCheckService = checkBusinessNo;
	}
	
	@Override
	@Transactional
	public int companyRegistor(CompanyVO.Registor companyRegistor) {
		// 회사 등록
		int companyNo = companyMapper.insertCompany(companyRegistor);
		
		// 주 종목 등록
		if(companyRegistor.getOption() != null) {
			companyMapper.insertCompanySpecaility(
					companyNo
					,companyRegistor.getOption()
					,companyRegistor.getEtcMemo());
		}
		
		return companyNo;
	}

}
