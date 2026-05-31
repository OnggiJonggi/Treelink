package com.tl.company;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tl.global.exception.CustomException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyService {
	private final CompanyMapper companyMapper;
	
	@Transactional
	public String companyRegistor(CompanyVO.Registor companyRegistor) {
		
		// 회사 식별 Uuid생성
		String uuid = UUID.randomUUID().toString();
		companyRegistor.setCompanyUuid(uuid);
		
		// 회사 등록
		companyMapper.insertCompany(companyRegistor);
		
		// 주 종목 등록
		if(companyRegistor.getOption() != null) {
			companyMapper.insertCompanySpecialty(
					companyRegistor.getCompanyNo()
					,companyRegistor.getOption()
					,companyRegistor.getEtcMemo());
		}
		
		return uuid;
	}

	/**
	 * 회사 기본 정보 조회
	 * @param companyNo
	 * @return 회사 기본 정보
	 */
	public CompanyVO.Detail getCompanyBasicInfo(String companyUuid) {
		
		CompanyVO.Detail result = companyMapper.selectCompanyDetail(companyUuid);
		
		if(result == null) throw new CustomException(null);
		
		return result;
	}


	/**
	 * 회사 기본정보 업데이트
	 * 트랜잭션 안 할꺼에요
	 * @param company
	 */
	public void updateCompany(CompanyVO.Registor company) {
		
		// 기본정보 업데이트 및 회사 식별번호 추출
		companyMapper.updateCompany(company);
		
		// 주 종목 업데이트
		companyMapper.updateCompanySpecialty(
				company.getCompanyNo(),
				company.getOption(),
				company.getEtcMemo());
	}

}
