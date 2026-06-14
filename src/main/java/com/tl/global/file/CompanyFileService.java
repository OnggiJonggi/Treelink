package com.tl.global.file;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.tl.company.CompanyStatusEnum;
import com.tl.company.CompanyVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.component.DocTypeEnum;
import com.tl.global.file.component.FileComponent;
import com.tl.global.file.component.FileNameEscapeEnum;
import com.tl.global.file.component.FileStatusEnum;
import com.tl.global.file.component.ImageEnum;
import com.tl.global.file.component.InlineMimeTypeEnum;
import com.tl.global.file.component.SavePathEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyFileService {
	private final CompanyFileMapper companyFileMapper;
	private final FileMapper fileMapper;
	private final FileComponent fileComponent;

	@Value("${file.upload.address}")
	private String uploadAddress;
	// 개발용 : D:/Dev/upload

	/**
	 * 업체 서류 등록
	 */
	@Transactional
	public void registor(CompanyVO.DocRegistor docRegistor) throws Exception {
		
		// docType이 LOGO(회사 로고, 다른 곳에서 업로드받음)이면 안돼요
		if(docRegistor.getDocType().equals(DocTypeEnum.LOGO.name()))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		
		// 파일 이름, 서류 종류 유효성 검사 및 이스케이프
		fileComponent.isValid(docRegistor.getFile(),
				docRegistor.getDocType(), docRegistor.getExpireOn());
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress
				+ SavePathEnum.COMPANY_DOC.getFolder()
				+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 이름도 바꿔부러
		String changedName = UUID.randomUUID().toString()
				+ docRegistor.getFile().getOriginalFilename().substring(docRegistor.getFile().getOriginalFilename().lastIndexOf("."));

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(changedName);
		docRegistor.getFile().transferTo(targetFile.toFile());

		// FILE_INFO 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(docRegistor.getFile().getOriginalFilename())
				.changedName(changedName)
				.mime(docRegistor.getFile().getContentType())
				.fileSize(docRegistor.getFile().getSize())
				.savePath(path)
				.expireOn(docRegistor.getExpireOn())
				.companyNo(docRegistor.getCompanyNo())
				.docType(docRegistor.getDocType()).build();
		
		// DB에 메타데이터 저장
		companyFileMapper.insertInfo(fileInfo);
		companyFileMapper.insertCompanyDoc(fileInfo);
		
		// FILE_HISTORY 객체 생성
		FileInfoVO.History insertHistory = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(docRegistor.getFile().getOriginalFilename())
				.changedName(changedName)
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(docRegistor.getMemberNo()).build();

		// DB에 파일 로그 저장
		fileMapper.insertHistory(insertHistory);
	}

	/**
	 * 업체 서류 메타데이터 조회
	 * 
	 * @param companyNo
	 * @return 회사 서류들
	 */
	public List<FileInfoVO.Detail> getInfo(int companyNo) {
		return companyFileMapper.selectInfo(companyNo);
	}

	/**
	 * 업체 서류 파일 보기
	 * @param companyNo
	 * @param docNo
	 */
	public FileInfoVO.FileResult getFile(int companyNo, int docNo) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.GetFile getFile = companyFileMapper.selectGetFile(companyNo, docNo);
	    if (getFile == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // 파일 경로
	    Path filePath = Paths.get(fileComponent.createPath(getFile.getSavePath(), getFile.getChangedName())).normalize();
	    
	    // 파일 가져오기
	    Resource resource;
	    try {
	        resource = new UrlResource(filePath.toUri());
	    } catch (MalformedURLException e) {
	        throw new CustomException(ErrorCodeEnum.FILE_NOT_FOUND);
	    }
	    
	    // inline여부 확인
	    boolean inline = InlineMimeTypeEnum.isInline(getFile.getMime());
		
	    return FileInfoVO.FileResult.builder()
	    		.resource(resource)
	    		.originalName(getFile.getOriginalName())
	    		.mimeType(getFile.getMime())
	    		.inline(inline).build();
	}

	/**
	 * 파일 삭제
	 * 상태값 DELETED로 변경, FILE_HISTORY 기록
	 * @param companyUuid
	 * @param docNo
	 * @param memberNo
	 */
	@Transactional
	public void deleteDoc(int companyNo, int docNo, int memberNo) {
		
		// 기존 상태 조회
		FileInfoVO.History history = companyFileMapper.selectInfoForHistory(docNo);
		
		// 파일 번호 null로 두기
		history.setFileNo(null);
		
		// 나머지 값 채워넣기
		history.setAction(FileStatusEnum.DELETED);
		history.setActionBy(memberNo);
		
		// 지워
		companyFileMapper.deleteDoc(docNo);
		fileMapper.insertHistory(history);
	}

	/**
	 * 업체 로고 삽입
	 */
	@Transactional
	public void insertLogo(int companyNo, MultipartFile file, int memberNo) throws Exception {
		
		// 파일 비었으면 가세요라
		if (file == null || file.isEmpty())
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 이거 이미지 맞나요?
		if(!ImageEnum.isImage(file.getContentType())) {
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		}
		
		// 파일 이름 내놔
		String originalName = file.getOriginalFilename();
		
		// 이름 이스케이프
		originalName = FileNameEscapeEnum.escapeAll(originalName);
		
		// 변경된 이름
		String changeName = companyNo
				+ "호"
				+ UUID.randomUUID().toString()
				+ originalName.substring(originalName.lastIndexOf("."));
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress
				+ SavePathEnum.COMPANY_LOGO.getFolder() 
				+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(changeName);
		file.transferTo(targetFile.toFile());

		// FILE_INFO 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(originalName)
				.changedName(changeName)
				.mime(file.getContentType())
				.fileSize(file.getSize())
				.savePath(path)
				.companyNo(companyNo)
				.docType(DocTypeEnum.LOGO.name()).build();
		
		// DB에 이미 로고 파일이 있나요?
		FileInfoVO.History oldHistory = companyFileMapper.selectInfoForLogoHistory(companyNo);
		
		// 아니삣삐야지금로고파일이있다고한거니??있으면밀어버리고새로넣어야지모하는거야
		if(oldHistory != null) {
			
			// 삭제 기록 추가
			oldHistory.setAction(FileStatusEnum.DELETED);
			oldHistory.setActionBy(memberNo);
			
			int result1 = fileMapper.insertHistory(oldHistory);
			if(result1==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			
			
			// 지워.
			int result2 = companyFileMapper.deleteLogo(oldHistory.getFileNo());
			if(result2==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}

		// DB에 메타데이터 저장
		int result3 = companyFileMapper.insertInfo(fileInfo);
		if(result3==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		int result4 = companyFileMapper.insertCompanyDoc(fileInfo);
		if(result4==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// FILE_HISTORY 객체 생성
		FileInfoVO.History history = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(originalName)
				.changedName(changeName)
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(memberNo).build();
		
		// DB에 파일 로그 저장
		int result5 = fileMapper.insertHistory(history);
		if(result5==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
	}
	
	
	/**
	 * 업체 로고 조회
	 */
	public FileInfoVO.SavePath getSavePath(int companyNo, boolean isAdmin) {
		
		FileInfoVO.SavePath result = companyFileMapper.selectLogoSavePath(companyNo, isAdmin);
		
		// 뭐야 없잖아...
		if(result==null || result.getSavePath()==null)
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		
		// 폴더 경로와 파일 이름으로 진짜 경로 완성
		result.setSavePath(fileComponent.createPath(result.getSavePath(), result.getChangedName()));
		
		return result;
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
	public String insertIntroImage(int memberNo, int companyNo, MultipartFile file) throws Exception {
		
		// 파일 비었으면 가세요라
		if (file == null || file.isEmpty())
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 이거 이미지 맞나요?
		if(!ImageEnum.isImage(file.getContentType()))
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 파일 이름 내놔
		String originalName = file.getOriginalFilename();
		
		// 이름 이스케이프
		originalName = FileNameEscapeEnum.escapeAll(originalName);
		
		// 변경된 이름
		String changeName = companyNo
				+ UUID.randomUUID().toString()
				+ originalName.substring(originalName.lastIndexOf("."));
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress
				+ SavePathEnum.COMPANY_INTRO.getFolder() 
				+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(changeName);
		file.transferTo(targetFile.toFile());

		// FILE_INFO 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(originalName)
				.changedName(changeName)
				.mime(file.getContentType())
				.fileSize(file.getSize())
				.savePath(path)
				.companyNo(companyNo)
				.docType(DocTypeEnum.INTRO.name()).build();
		
		// DB에 메타데이터 저장
		int result1 = companyFileMapper.insertInfo(fileInfo);
		if(result1==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		int result2 = companyFileMapper.insertCompanyDoc(fileInfo);
		if(result2==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);

		// FILE_HISTORY 객체 생성
		FileInfoVO.History history = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(originalName)
				.changedName(changeName)
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(memberNo).build();
		
		// DB에 파일 로그 저장
		int result3 = fileMapper.insertHistory(history);
		if(result3==0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// 파일 식별번호 반환
		return changeName;
	}

	/**
	 * 업체 소개문 이미지 조회
	 * 
	 * @param memberNo
	 * @param companyNo
	 * @param changedName
	 * @param status
	 * @return FileResult
	 */
	public FileInfoVO.FileResult getIntroImage(int companyNo, String changedName, CompanyStatusEnum status) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.GetFile getFile = companyFileMapper.selectIntroImage(companyNo, changedName, status);
	    if (getFile == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // 파일 경로
	    Path filePath = Paths.get(fileComponent.createPath(getFile.getSavePath(), getFile.getChangedName())).normalize();
	    
	    // 파일 가져오기
	    Resource resource;
	    try {
	        resource = new UrlResource(filePath.toUri());
	    } catch (MalformedURLException e) {
	        throw new CustomException(ErrorCodeEnum.FILE_NOT_FOUND);
	    }
		
	    return FileInfoVO.FileResult.builder()
	    		.resource(resource)
	    		.originalName(getFile.getOriginalName()) // 원본 이름 쓸 데가 있냐?
	    		.mimeType(getFile.getMime())
	    		.build();
	}
}
