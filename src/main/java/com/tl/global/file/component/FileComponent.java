package com.tl.global.file.component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.FileInfoVO;
import com.tl.global.file.FileMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileComponent {
	private final FileMapper fileMapper;
	private final S3Config s3Config;
	
	@Value("${aws.s3.bucket}")
	private String bucketName;
	
	@Value("${aws.s3.upload-path}")
	private String uploadPath;
	
	
	/**
	 * 폴더 디렉토리와 파일 이름으로 진짜 경로 만들기
	 */
	public String createPath(String savePath, String changedName) {
		return savePath + "/" + changedName;
	}
	
	
	/**
	 * S3에 파일 저장, FILE_INFO / FILE_HISTORY에 메타데이터 저장
	 * 
	 * 변경된 이름 반환
	 */
	public String save(FileInfoVO.HandOver file, Consumer<Integer> callback) {
		
		// 파일 없으면 가세요
		if (!file.isValid()) throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 지금 몇 시에요?
		LocalDateTime now = LocalDateTime.now();

		// 저장 경로 만들기
		String path = uploadPath
				+ file.getRootSavePath().getFolder()
				+ now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));

		// 파일 이름 이스케이프
		String originalName = FileNameEscapeEnum.escapeAll(file.getOriginalName());
		
		// 이름도 바꿔부러
		String changeName = UUID.randomUUID().toString()
				+ file.getOriginalName().substring(file.getOriginalName().lastIndexOf("."));

		// S3에 업로드할 경로
		String key = createPath(path, changeName); 

		// S3에 파일 업로드
		PutObjectRequest putRequest = PutObjectRequest.builder()
		        .bucket(bucketName)
		        .key(key)
		        .contentType(file.getMime())
		        .build();
		s3Config.s3Client().putObject(putRequest, RequestBody.fromBytes(file.getBytes()));


		// FILE_INFO 객체 생성
		FileInfoVO.Insert fileInfo = FileInfoVO.Insert.builder()
				.originalName(originalName)
				.changedName(changeName)
				.mime(file.getMime())
				.fileSize(file.getSize())
				.savePath(path).build();
		
		// DB에 메타데이터 저장
		int result1 = fileMapper.insertInfo(fileInfo);
		if(result1 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// DB 맵핑 테이블에 저장
		callback.accept(fileInfo.getFileNo()); // 콜백 함수
		
		// FILE_HISTORY 객체 생성
		FileInfoVO.History insertHistory = FileInfoVO.History.builder()
				.fileNo(fileInfo.getFileNo())
				.originalName(originalName)
				.changedName(changeName)
				.savePath(path)
				.action(FileStatusEnum.ACTIVE)
				.actionAt(now)
				.actionBy(file.getMemberNo()).build();

		// DB에 파일 로그 저장
		int result2 = fileMapper.insertHistory(insertHistory);
		if(result2 == 0) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		return changeName;
	}
	
	
	/**
	 * S3에서 저장된 파일 url 얻어내기
	 * Basic에 mime가 있으면 inline추가 -> orginalName 필요해요! 없으면 오류남
	 */
	public String getSavedUrl(FileInfoVO.Basic basic) {

	    String key = createPath(basic.getSavePath(), basic.getChangedName());

	    // 브라우저에서 이 파일을 이르케 사용해줘요
	    String disposition = null;
	    if (basic.getMime() != null && basic.getOriginalName() != null) {
	    	
	    	// 파일 원본 url 형식으로 변환
	        String encodedName = URLEncoder.encode(basic.getOriginalName(), StandardCharsets.UTF_8)
	                .replaceAll("\\+", "%20");
	        
	        // 인라인인지 다운로드인지 판별
	        String dispositionType = InlineMimeTypeEnum.isInline(basic.getMime()) ? "inline" : "attachment";
	        
	        // 전체 disposition 헤더
	        disposition = dispositionType + "; filename*=UTF-8''" + encodedName;
	    }

	    GetObjectRequest.Builder requestBuilder = GetObjectRequest.builder()
	            .bucket(bucketName)
	            .key(key);

	    if (disposition != null)
	        requestBuilder.responseContentDisposition(disposition);
	    
	    GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
	            .signatureDuration(Duration.ofMinutes(5))
	            .getObjectRequest(requestBuilder.build())
	            .build();

	    return s3Config.s3Presigner().presignGetObject(presignRequest).url().toString();
	}
}
