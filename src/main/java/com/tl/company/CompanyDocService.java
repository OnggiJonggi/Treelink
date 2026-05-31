package com.tl.company;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.FileInfoVO;
import com.tl.global.file.FileStatusEnum;
import com.tl.global.file.InlineMimeTypeEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CompanyDocService {
	private final CompanyDocMapper companyDocMapper;

	@Value("${file.upload.address}")
	private String uploadAddress;

	/**
	 * 업체 서류 등록
	 * 
	 * @param companyNo
	 * @param docType
	 * @param file
	 */
	public void registor(String companyUuid, String docType,
			LocalDate expireOn, MultipartFile file) throws Exception {
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadAddress + "company/" + now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 이름도 바꿔부러
		String changedName = UUID.randomUUID().toString();

		// 저장할 경로 생성
		Path targetDir = Paths.get(path);
		Files.createDirectories(targetDir);

		// 파일 저장
		Path targetFile = targetDir.resolve(changedName);
		file.transferTo(targetFile.toFile());

		// FileInfoRegistor 객체 생성
		FileInfoVO.Registor fileInfo = FileInfoVO.Registor.builder()
				.originalName(file.getName())
				.changedName(changedName)
				.mime(file.getContentType())
				.fileSize(file.getSize())
				.savePath(path)
				.savedAt(now)
				.expireOn(expireOn)
				.companyUuid(companyUuid)
				.docType(docType).build();

		// DB에 메타데이터 저장
		companyDocMapper.insertInfo(fileInfo);
	}

	/**
	 * 업체 서류 메타데이터 조회
	 * 
	 * @param companyNo
	 * @return 회사 서류들
	 */
	public List<FileInfoVO.Detail> getInfo(String companyUuid) {
		return companyDocMapper.selectInfo(companyUuid);
	}

	/**
	 * 업체 서류 파일 보기
	 * @param companyUuid
	 * @param docNo
	 */
	public FileInfoVO.FileResult getFile(String companyUuid, int docNo) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.GetFile getFile = companyDocMapper.selectGetFile(companyUuid, docNo);
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
	public void deleteDoc(String companyUuid, int docNo, int memberNo) {
		
		// 기존 상태 조회
		FileInfoVO.InsertHistory before = companyDocMapper.selectInfoForHistory(docNo);
		
		// 나머지 값 채워넣기
		before.setAction(FileStatusEnum.DELETED.name());
		before.setActionBy(memberNo);
		
		// 상태값 DELETED
		companyDocMapper.updateDocStatus(
				FileInfoVO.UpdateStatus.builder()
				.companyUuid(companyUuid)
				.docNo(docNo)
				.status(FileStatusEnum.DELETED.name()).build()
				);
		
		// FILE_HISTORY에 기록
		companyDocMapper.insertHistory(before);
	}
}
