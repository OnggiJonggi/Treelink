package com.tl.company;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.location.LocationMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyLocationService {
	private final CompanyLocationMapper companyLocationMapper;
	private final LocationMapper locationMapper;

	/**
	 * 이 업체 위치 내놔
	 */
	public List<CompanyVO.LocationDetail> getLocaions(int companyNo) {

		List<CompanyVO.LocationDetail> result = companyLocationMapper.selectCompanyLocations(companyNo);

		return result;
	}

	/**
	 * 업체 위치 추가하실게요
	 */
	@Transactional
	public void insertLocation(CompanyVO.InsertLocation companyLocation) {
		
		// LOCATION 테이블 삽입
		int result1 = locationMapper.insert(companyLocation);
		if(result1 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR	);
		
		// COMPANY_LOCATION 테이블 삽입
		int result2 = companyLocationMapper.insert(companyLocation);
		if(result2 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR	);
		
	}

	/**
	 * 업체 위치 삭제
	 */
	@Transactional
	public void deleteLocation(int companyNo, int locationNo) {
		
		// LOCATION 테이블 삭제
		int result1 = companyLocationMapper.delete(companyNo, locationNo);
		if(result1 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR	);
		
		// COMPANY_LOCATION 테이블 삭제
		int result2 = locationMapper.delete(locationNo);
		if(result2 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR	);
	}
}
