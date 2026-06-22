package com.tl.global.file.component;

import java.io.File;
import java.time.LocalDate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.FileDataVO;

@Component
public class FileComponent {
	
	/**
	 * 파일 유효성 검사
	 * 파일 이름, docType검사
	 * 
	 * @param file
	 * @return 불값
	 */
	public String isValid(FileDataVO file,
			String docType, LocalDate expireOn) throws Exception{
		
		// 파일 이름 내놔
		String originalName = file.getOriginalName();

		// 파일 이름 이상하면 가세요라
		if (!Pattern.matches(FileRegexp.ORIGINAL_NAME_NO_REGEXP, originalName)
				) throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 서류 타입 이상하면 가세요라
		if(docType == null
				|| !Pattern.matches(FileRegexp.DOC_TYPE_REGEXP, docType))
			throw new CustomException(ErrorCodeEnum.DOC_TYPE_FORBIDDEN);
		
		// 만료일이 이상해요
		if(expireOn == null
				|| expireOn.isAfter(LocalDate.now())) {
			/*
			 * 통과!
			 * !expireOn.isAfter(LocalDate.now())처럼 사용하면
			 * 오늘 이후(오늘 포함)이 되요
			 * 오늘이 포함되면 안 되니까 이르케 쓸게요
			 */
		}else throw new CustomException(ErrorCodeEnum.FILE_EXPIRE_ON_FORBIDDEN);
		
		// 파일명 이스케이프
		return FileNameEscapeEnum.escapeAll(originalName);
	}
	
	/**
	 * 폴더 디렉토리와 파일 이름으로 진짜 경로 만들기
	 * 
	 * @param savePath
	 * @param changedName
	 * @return 진짜 파일 경로
	 */
	public String createPath(String savePath, String changedName) {
		return savePath + File.separator + changedName;
	}
}
