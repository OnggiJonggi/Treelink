package com.tl.global.file.component;

import java.time.LocalDate;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;

@Component
public class FileValidateComponent {
	
	/**
	 * 파일 유효성 검사
	 * 파일 이름, docType검사
	 * 
	 * @param file
	 * @return 불값
	 */
	public String isValid(MultipartFile file,
			String docType, LocalDate expireOn) throws Exception{
		
		// 파일 비었으면 가세요라
		if (file == null || file.isEmpty())
			throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);

		// 파일 이름 내놔
		String originalName = file.getOriginalFilename();

		// 파일 이름 이상하면 가세요라
		if (originalName == null 
				|| originalName.isBlank()
				|| !Pattern.matches(FileRegexp.ORIGINAL_NAME_NO_REGEXP, originalName)
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
}
