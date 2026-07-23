package com.tl.company;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.tl.global.common.SearchResultVO;
import com.tl.global.file.EvalDocMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EvalService {
	private final EvalMapper evalMapper;
	private final EvalDocMapper evalDocMapper;
	private final EvalValidComponent evalValidComponent;
	
	/**
	 * 평가 조회
	 * 
	 * @param isAll : 전체조회 / 활성화된 회사만 조회 여부
	 * 
	 * 각 평가 항목의 최신 리비전 반영 사항을 조회
	 */
	public EvalVO.Detail getEval(int companyNo, boolean isAll) {
		return evalMapper.selectEval(companyNo, isAll);
	}

	/**
	 * 평가 추가 / 수정
	 * 
	 * @param changedName : 서류파일 uuid.
	 * 
	 * 상위 항목, 최종 종합 점수는 프론트에서 보낸 값 사용하지 않음
	 * 평가 수정 - DB에서 모든 항목 점수를 전부 긁어와 점수가 수정되었는지 확인함
	 * 하위 항목을 수정했으면 상위 항목 점수까지 전부 수정해서 리비전에 반영
	 * 
	 * TODO : 파일을 업로드해두고 사용하지 않으면(업로드를 취소하면),
	 * 	업체 소개문 사진 로직과 달리 EVALUATION_DOC는
	 * 	업로드 시점에서 어떤 업체의 파일인지 구별 불가능
	 * 	그러니 HISTORY_NO가 빈 EVALUATION_DOC 행을 지우는 배치 필요
	 */
	@Transactional
	public void insert(EvalVO.Insert insert) {
		
		// 리비전 번호 확인
		int revisionNo = evalMapper.selectRevisionNo(insert.getEvaluationNo());
		
		// 리비전 번호 추가
		insert.setRevisionNo(revisionNo + 1);
		
		if(revisionNo == 0) {
			// 신규 추가
			
			// 유효성 검사
			if(!evalValidComponent.isScoreValid(insert.getScores(), null))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			
			// EVALUATION 테이블 추가
			int result1 = evalMapper.insert(insert.getEvaluationNo());
			if(result1 == 0)
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			
			// EVALUATION_SCORE 테이블 추가
			int result2 = evalMapper.insertScore(insert);
			if(result2 != insert.getScores().size())
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
		}else {
			// 수정
			
			// 기존 평가 점수 긁어오기
			List<EvalVO.ScoreDetail> oldScores = evalMapper.selectOldScores(insert.getEvaluationNo());
			
			// 유효성 검사
			if(!evalValidComponent.isScoreValid(insert.getScores(), oldScores))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
			
			// EVALUATION_SCORE 테이블 수정
			for(EvalVO.InsertScore item : insert.getScores()) {
				int result1 = evalMapper.update(insert.getEvaluationNo(), item);
				if(result1 == 0)
					throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
			}
		}
		
		// EVALUATION_HISTORY 테이블 추가
		int result3 = evalMapper.insertHistory(insert);
		if(result3 == 0)
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		
		// scores가 있을 때
		if(insert.getScores() != null && insert.getScores().size() != 0) {
			
			// EVALUATION_SCORE_HISTORY 테이블 추가
			int result4 = evalMapper.insertHistoryScore(insert);
			if(result4 != insert.getScores().size())
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
		
		// 파일 있냐?
		if(insert.getFileNos() != null && !insert.getFileNos().isEmpty()) {
			
			// 최신 HISTORY_NO 추출
			int historyNo = evalMapper.selectHistoryNo(insert.getEvaluationNo());
			
			// EVALUATION_DOC에 변경된 파일 정보 업데이트
			int result5 = evalDocMapper.updateHistoryNo(insert.getFileNos(), historyNo);
			if(result5 != insert.getFileNos().size())
				throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	/**
	 * 평가 기록 목록 조회
	 */
	public SearchResultVO<EvalVO.HistoryDetail> search(EvalVO.Search search) {
		
		// 목록 조회
		List<EvalVO.HistoryDetail> result = evalMapper.selectHistoryList(search);
		
		// 검색 결과 수
		int totalCount = evalMapper.selectHistoryTotalCount(search);
		
		// SearchResultVO로 감싸기
		SearchResultVO<EvalVO.HistoryDetail> searchResult = new SearchResultVO<EvalVO.HistoryDetail>(
				result, totalCount, search.getPage()
				);
		
		return searchResult;
	}

	/**
	 * 리비전 조회
	 * 
	 * 각 평가 항목의 최신 리비전 반영 사항을 조회
	 * 
	 * @param isAll : true이면 모든 회사 대상, false이면 활성화된 회사만 대상
	 */
	public EvalVO.HistoryDetail getRevision(int companyNo, int revisionNo, boolean isAll) {
		return evalMapper.selectRevision(companyNo, revisionNo, isAll);
	}

}
