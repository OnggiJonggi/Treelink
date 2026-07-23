package com.tl.global.file;

import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;
import com.tl.global.file.component.FileComponent;
import com.tl.global.file.component.FileRegexp;
import com.tl.global.file.component.RootSavePathEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvalDocService {
	private final EvalDocMapper evalDocMapper;
	private final FileComponent fileComponent;
	
	/**
	 * 평가 서류 추가
	 * 
	 * EVALUATION_DOC의 HISTORY_NO를 제외하고
	 * S3, FILE_INFO, FILE_HISTORY, EVALUATION_DOC에 파일 저장
	 * HISTORY_NO는 평가 제출 시 연결
	 * 
	 * @return FILE_NO
	 */
	public int insertEvalDoc(FileDataVO file, String memo, int memberNo) {
		
		// 파일 이름 이상하면 가세요라
		if (file.getOriginalName() == null ||
				!Pattern.matches(FileRegexp.ORIGINAL_NAME_NO_REGEXP, file.getOriginalName())
				) throw new CustomException(ErrorCodeEnum.FILE_FORBIDDEN);
		
		// 메모 이상하면 가세요라
		if(memo == null
				|| !Pattern.matches(FileRegexp.EVAL_MEMO, memo))
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		
		// 저장
		EvalDocVO.Insert insert = new EvalDocVO.Insert();
		insert.setMemo(memo);
		
		FileInfoVO.HandOver handOver = new FileInfoVO.HandOver(
				file, memberNo, RootSavePathEnum.EVAL_DOC);
		fileComponent.save(handOver,
				fileNo ->{
					insert.setFileNo(fileNo);
					evalDocMapper.insert(insert);
				}
			);
		
		return insert.getFileNo();
	}

	/**
	 * 업체 평가 서류 내놔
	 */
	public String getSavePath(int companyNo, int fileNo, boolean isAll) {
		
		// 원본 이름, 경로, MIME 얻어내기
		FileInfoVO.Basic basic = evalDocMapper.selectBasic(companyNo, fileNo, isAll);
	    if (basic == null)
	    	throw new CustomException(ErrorCodeEnum.FILE_INFO_NOT_FOUND);
		
	    // S3에서 파일 url 추출
	    String url = fileComponent.getSavedUrl(basic);
	    
	    return url;
	}
}
