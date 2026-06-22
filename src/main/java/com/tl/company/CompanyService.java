package com.tl.company;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.company.CompanyVO.Search;
import com.tl.global.common.ExtractFromHtml;
import com.tl.global.common.SanitizeComponent;
import com.tl.global.common.SearchResultVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.CompanyFileMapper;
import com.tl.global.file.FileInfoVO;
import com.tl.global.file.FileMapper;
import com.tl.global.file.component.FileStatusEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {
	private final CompanyMapper companyMapper;
	private final CompanyFileMapper companyFileMapper;
	private final FileMapper fileMapper;
	private final SanitizeComponent sanitizeComponent;
	private final ExtractFromHtml extractFromHtml;
	
	/**
	 * 업체 조회
	 */
	public SearchResultVO<CompanyVO.Detail> getCompanyList(Search companySearch) {
		
		// 목록 조회
		List<CompanyVO.Detail> result = companyMapper.selectList(companySearch);
		
		// 검색 수
		int totalCount = companyMapper.selectListTotalCount(companySearch);
		
		// searchResult로 감싸기
		SearchResultVO<CompanyVO.Detail> searchResult = new SearchResultVO<CompanyVO.Detail>(
				result, totalCount, companySearch.getPage());
		
		return searchResult;
	}

	
	/**
	 * 업체 등록
	 */
	@Transactional
	public int companyRegistor(CompanyVO.Registor companyRegistor) {
		// 업체 등록
		int result1 = companyMapper.insertCompany(companyRegistor);
		
		// 오류!
		if(result1 == 0)
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		
		// 주 종목 등록
		if(companyRegistor.getOption() != null && !companyRegistor.getOption().isEmpty()) {
			
			int result2 = companyMapper.insertCompanySpecialty(
					companyRegistor.getCompanyNo()
					,companyRegistor.getOption()
					,companyRegistor.getEtcMemo());
			
			if(result2 == 0)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// 업체 식별번호 반환
		return companyRegistor.getCompanyNo();
	}

	/**
	 * 회사 기본 정보 조회
	 * @param companyNo
	 * @return 회사 기본 정보
	 */
	public CompanyVO.Detail getCompanyBasicInfo(int companyNo) {
		
		CompanyVO.Detail result = companyMapper.selectCompanyDetail(companyNo);
		
		if(result==null) throw new CustomException(ErrorCodeEnum.COMPANY_NOT_FOUND);
		
		return result;
	}


	/**
	 * 회사 기본정보 업데이트
	 * @param company
	 */
	@Transactional
	public void updateCompany(CompanyVO.Registor company) {
		
		// 기본정보 업데이트 및 회사 식별번호 추출
		int result1 = companyMapper.updateCompany(company);
		
		// 오류!
		if(result1 == 0)
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// 주 종목 삭제
		companyMapper.deleteCompanySpecialty(company.getCompanyNo());
		
		// 갈아치울 주 종목이 있으면 갈아버려
		if(company.getOption()!=null && !company.getOption().isEmpty()) {
			
			// 주 종목 새로 삽입
			int result3 = companyMapper.insertCompanySpecialty(
					company.getCompanyNo(),
					company.getOption(),
					company.getEtcMemo());
			
			// 오류!
			if(result3 == 0)
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}


	/**
	 * 회사 소개문 보기
	 * @param status 
	 */
	public String getIntro(int companyNo, CompanyStatusEnum status) {
		
		String intro = companyMapper.selectIntro(companyNo, status);
		
		return intro;
	}


	/**
	 * 회사 소개문 생성/수정
	 * @param companyNo
	 * @param intro 
	 */
	@Transactional
	public void updateIntro(String intro, int companyNo, int memberNo) {
		// 나쁜 태그 대롱대롱 하지요
		intro = sanitizeComponent.sanitize(intro);
		
		// 소개문 업데이트
		int result = companyMapper.updateIntro(companyNo, intro);
		if(result==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// 소개문에서 이미지 파일 이름 추출하기
		List<String> imageName = extractFromHtml.fileNameFromIntro(intro);
		
		// 비활성 대상 파일 정보 불러오기
		List<FileInfoVO.History> unusedIntroImage = companyFileMapper.selectUnusedIntro(companyNo, imageName);
		
		// 비활성 대상 파일이 있을 경우
		if(unusedIntroImage !=null && !unusedIntroImage.isEmpty()) {
			
			// 파일 식별번호만 추출 
			List<Integer> fileNo = unusedIntroImage.stream()
					.filter(h -> h.getFileNo() != null) // null제외
			        .map(FileInfoVO.History :: getFileNo)
			        .toList(); 
			
			// 사용하지 않은 이미지 파일 FILE_INFO에서 지우기
			int result1 = companyFileMapper.deleteUnusedIntroImage(fileNo);
			
			// 비활성 대상 파일과 비활성화 한 파일 수가 같지 않으면 오류!
			if(result1 != fileNo.size()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			
			
			// 파일 로그
			int result2 = 0;
			for(FileInfoVO.History item : unusedIntroImage) {
				item.setFileNo(null);
				item.setActionBy(memberNo);
				item.setAction(FileStatusEnum.UNUSED);
				
				// FILE_HISTORY 추가
				// 시퀀스 + insert all 혹은 foreach + insert는 오류 발생
				// 그러니 개별 행 삽입 요청
				int result3 = fileMapper.insertHistory(item);
				if(result3 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
				
				result2 ++;
			}
					
			if(result2 != fileNo.size()) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			
		}
		
		
	}

}
