package com.tl.global.file;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.company.CompanyStatusEnum;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.CompanyDocVO.HandOver;
import com.tl.global.file.component.DocTypeEnum;
import com.tl.global.file.component.FileComponent;
import com.tl.global.file.component.FileRegexp;
import com.tl.global.file.component.FileStatusEnum;
import com.tl.global.file.component.ImageEnum;
import com.tl.global.file.component.RootSavePathEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyDocService {
	private final CompanyDocMapper companyDocMapper;
	private final FileMapper fileMapper;
	private final FileComponent fileComponent;

	/**
	 * 업체 서류 등록
	 */
	@Transactional
	public void insert(HandOver request) throws Exception {
		
		// docType이 LOGO(회사 로고, 다른 곳에서 업로드받음)이면 안돼요
		if(request.getDocType().equals(DocTypeEnum.LOGO.name()))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		
		// 파일 이름 내놔
		String originalName = request.getFile().getOriginalName();

		// 파일 이름 이상하면 가세요라
		if (originalName == null ||
				!Pattern.matches(FileRegexp.ORIGINAL_NAME_NO_REGEXP, originalName)
				) throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 서류 타입 이상하면 가세요라
		if(request.getDocType() == null
				|| !Pattern.matches(FileRegexp.DOC_TYPE_REGEXP, request.getDocType()))
			throw new CustomException(ErrorCodeEnum.DOC_TYPE_FORBIDDEN);
		
		// 만료일이 이상해요
		if(request.getExpireOn() == null
				|| request.getExpireOn().isAfter(LocalDate.now())) {
			/*
			 * 통과!
			 * !expireOn.isAfter(LocalDate.now())처럼 사용하면
			 * 오늘 이후(오늘 포함)이 되요
			 * 오늘이 포함되면 안 되니까 이르케 쓸게요
			 */
		}else throw new CustomException(ErrorCodeEnum.FILE_EXPIRE_ON_FORBIDDEN);
		
		
		// 저장
		FileInfoVO.HandOver handOver = new FileInfoVO.HandOver(
				request.getFile(), request.getMemberNo(), RootSavePathEnum.COMPANY_DOC);
		fileComponent.save(handOver,
				fileNo ->{
					CompanyDocVO.Insert insert = CompanyDocVO.Insert.builder()
							.companyNo(request.getMemberNo())
							.fileNo(fileNo)
							.docType(request.getDocType())
							.expireOn(request.getExpireOn())
							.build();
					companyDocMapper.insertCompanyDoc(insert);
				}
			);
		
	}

	/**
	 * 업체 서류 메타데이터 조회
	 */
	public List<CompanyDocVO.Detail> getInfo(int companyNo) {
		return companyDocMapper.selectInfo(companyNo);
	}

	/**
	 * 업체 서류 파일 보기
	 */
	public String getFile(int companyNo, int fileNo) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.Basic basic = companyDocMapper.selectBasic(companyNo, fileNo);
	    if (basic == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // S3에서 파일 url 추출
	    String url = fileComponent.getSavedUrl(basic);
	    
	    return url;
	}

	/**
	 * 파일 삭제
	 * 
	 * 상태값 DELETED로 변경, FILE_HISTORY 기록
	 */
	@Transactional
	public void deleteDoc(int companyNo, int fileNo, int memberNo) {
		
		// 기존 상태 조회
		FileInfoVO.History history = fileMapper.selectInfoForHistory(fileNo);
		
		// 파일 번호 null로 두기
		history.setFileNo(null);
		
		// 나머지 값 채워넣기
		history.setAction(FileStatusEnum.DELETED);
		history.setActionBy(memberNo);
		
		// 지워
		companyDocMapper.deleteDoc(fileNo);
		fileMapper.insertHistory(history);
	}

	/**
	 * 업체 로고 삽입
	 */
	@Transactional
	public void insertLogo(FileDataVO file, int companyNo, int memberNo) throws Exception {
		
		// 이거 이미지 맞나요?
		if(file.getMime() == null || !ImageEnum.isImage(file.getMime()))
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// DB에 이미 로고 파일이 있나요?
		FileInfoVO.History oldHistory = companyDocMapper.selectInfoForLogoHistory(companyNo);
		
		// 아니삣삐야지금로고파일이있다고한거니??있으면밀어버리고새로넣어야지모하는거야
		if(oldHistory != null) {
			
			// 삭제 기록 추가
			oldHistory.setAction(FileStatusEnum.DELETED);
			oldHistory.setActionBy(memberNo);
			
			int result1 = fileMapper.insertHistory(oldHistory);
			if(result1==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			
			// 지워.
			int result2 = companyDocMapper.deleteLogo(oldHistory.getFileNo());
			if(result2==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// 저장
		FileInfoVO.HandOver handOver = new FileInfoVO.HandOver(
				file, memberNo, RootSavePathEnum.COMPANY_LOGO);
		fileComponent.save(handOver,
				fileNo ->{
					CompanyDocVO.Insert insert = CompanyDocVO.Insert.builder()
							.companyNo(companyNo)
							.fileNo(fileNo)
							.docType(DocTypeEnum.LOGO.name())
							.expireOn(null)
							.build();
					companyDocMapper.insertCompanyDoc(insert);
				}
			);
	}
	
	
	/**
	 * 업체 로고 조회
	 */
	public String getSavePath(int companyNo, boolean isAll) {
		
		FileInfoVO.Basic basic = companyDocMapper.selectLogoSavePath(companyNo, isAll);
		
		// 뭐야 없잖아...
		if(basic==null || basic.getSavePath()==null) return null;
		
		// S3에서 url가져오기
		return fileComponent.getSavedUrl(basic);
	}

	/**
	 * 업체 소개문 summernote 사진 첨부
	 *  
	 * @param memberNo
	 * @param companyNo
	 * @param file
	 * @return 변경된 파일 이름
	 */
	@Transactional
	public String insertIntroImage(FileDataVO file, int companyNo, int memberNo) throws Exception {
		
		// 이거 이미지 맞나요?
		if(file.getMime() == null || !ImageEnum.isImage(file.getMime()))
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 저장
		FileInfoVO.HandOver handOver = new FileInfoVO.HandOver(
				file, memberNo, RootSavePathEnum.COMPANY_INTRO);
		String changeName = fileComponent.save(handOver,
				fileNo ->{
					CompanyDocVO.Insert insert = CompanyDocVO.Insert.builder()
							.companyNo(companyNo)
							.fileNo(fileNo)
							.docType(DocTypeEnum.INTRO.name())
							.expireOn(null)
							.build();
					companyDocMapper.insertCompanyDoc(insert);
				}
			);
		
		return changeName;
	}

	/**
	 * 업체 소개문 이미지 조회
	 */
	public String getIntroImage(int companyNo, String changedName, CompanyStatusEnum status) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.Basic basic = companyDocMapper.selectIntroImage(companyNo, changedName, status);
	    if (basic == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // S3에서 url 추출
	    String url = fileComponent.getSavedUrl(basic);
		
	    return url;
	}
}
