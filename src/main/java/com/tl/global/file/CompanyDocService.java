package com.tl.global.file;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
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

import com.tl.company.CompanyVO;
import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.component.DocTypeEnum;
import com.tl.global.file.component.FileNameEscapeEnum;
import com.tl.global.file.component.FileStatusEnum;
import com.tl.global.file.component.FileValidateComponent;
import com.tl.global.file.component.ImageEnum;
import com.tl.global.file.component.InlineMimeTypeEnum;
import com.tl.global.file.component.SavePathEnum;
import com.tl.global.security.CryptoComponent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyDocService {
	private final CompanyDocMapper companyDocMapper;
	private final FileMapper fileMapper;
	public final CryptoComponent cryptoComponent;
	public final FileValidateComponent fileValidateComponent;

	@Value("${file.upload.address}")
	private String uploadAddress;

	/**
	 * 업체 서류 등록
	 * 
	 * @param companyNo
	 * @param docType
	 * @param file
	 */
	@Transactional
	public void registor(CompanyVO.DocRegistor docRegistor) throws Exception {
		
		// docType이 DocTypeEnum와 충돌하면 안돼요
		if(Arrays.stream(DocTypeEnum.values())
		        .anyMatch(e -> e.name().equals(docRegistor.getDocType())))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		
		// 파일 이름, 서류 종류 유효성 검사
		fileValidateComponent.isValid(docRegistor.getFile(),
				docRegistor.getDocType(), docRegistor.getExpireOn());
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress + SavePathEnum.COMPANY.getFolder()
			+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 이름도 바꿔부러
		String changedName = UUID.randomUUID().toString();

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(changedName);
		docRegistor.getFile().transferTo(targetFile.toFile());

		// FILE_INFO 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(docRegistor.getFile().getName())
				.changedName(changedName)
				.mime(docRegistor.getFile().getContentType())
				.fileSize(docRegistor.getFile().getSize())
				.savePath(path)
				.expireOn(docRegistor.getExpireOn())
				.companyNo(docRegistor.getCompanyNo())
				.docType(docRegistor.getDocType()).build();
		
		// FILE_HISTORY 객체 생성
		FileInfoVO.History insertHistory = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(docRegistor.getFile().getName())
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(docRegistor.getMemberNo()).build();

		// DB에 메타데이터 저장
		companyDocMapper.insertInfo(fileInfo);
		fileMapper.insertHistory(insertHistory);
	}

	/**
	 * 업체 서류 메타데이터 조회
	 * 
	 * @param companyNo
	 * @return 회사 서류들
	 */
	public List<FileInfoVO.Detail> getInfo(int companyNo) {
		return companyDocMapper.selectInfo(companyNo);
	}

	/**
	 * 업체 서류 파일 보기
	 * @param companyNo
	 * @param docNo
	 */
	public FileInfoVO.FileResult getFile(int companyNo, int docNo) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.GetFile getFile = companyDocMapper.selectGetFile(companyNo, docNo);
	    if (getFile == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // 파일 경로
	    Path filePath = Paths.get(getFile.getSavePath()).normalize();
	    
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
		FileInfoVO.History history = companyDocMapper.selectInfoForHistory(docNo);
		
		// 나머지 값 채워넣기
		history.setFileNo(docNo);
		history.setAction(FileStatusEnum.DELETED);
		history.setActionBy(memberNo);
		
		// 지워
		companyDocMapper.deleteDoc(docNo);
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
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress + SavePathEnum.COMPANY_LOGO.getFolder() 
				+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(String.valueOf(companyNo));
		file.transferTo(targetFile.toFile());

		// FILE_INFO 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(file.getName())
				.changedName(String.valueOf(companyNo))
				.mime(file.getContentType())
				.fileSize(file.getSize())
				.savePath(path)
				.companyNo(companyNo)
				.docType(DocTypeEnum.LOGO.name()).build();

		
		// FILE_HISTORY 객체 생성
		FileInfoVO.History history = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(file.getName())
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(memberNo).build();
		
		// DB에 메타데이터 저장
		companyDocMapper.insertInfo(fileInfo);
		fileMapper.insertHistory(history);
	}
}
