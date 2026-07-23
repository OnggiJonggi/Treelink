package com.tl.company;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

/**
 * 업체 평가 추가 / 수정의 EvalVO.Insert.scores의 유효성 검사
 * 
 * 평가 추가 -
 * 점수가 주어진 범위 이내인지 확인
 * 하위 항목 점수 합산해 상위 항목 점수 계산
 * 상위 항목 점수 합산해 최종 종합 점수 계산
 * 
 * 평가 수정 -
 * + 평가 추가 로직
 * 하위 항목 점수가 변경되면 상위 항목 점수 재산정,
 * 상위 항목이 없다면 scores컬랙션에 추가
 * 
 */
@Component
public class EvalValidComponent {
	
	private static final Set<Integer> TOP_ITEMS = Set.of(10, 20, 30, 40);

	// 최상위/최종 항목 field명은 마스터 데이터라 고정값으로 관리
	private static final Map<Integer, String> TOP_FIELD_MAP = Map.of(
			10, "투명성 종합",
			20, "책임성 종합",
			30, "신뢰성 종합",
			40, "보너스 종합",
			1, "최종 종합",
			2, "최종 참조 종합");

	public boolean isScoreValid(List<EvalVO.InsertScore> newScores, List<EvalVO.ScoreDetail> oldScores) {

		// 항목 입력이 없음 == 파일만 바뀜 -> 건너뜀
		if(newScores==null || newScores.isEmpty()) return true;
		
		// 상위/최종 항목의 reson과 "명시적으로 제출됐는지" 여부를 지우기 전에 보관
		Map<Integer, String> submittedTopResons = new HashMap<>();
		for (EvalVO.InsertScore s : newScores) {
			if (isTopOrFinal(s.getItemNo())) {
				submittedTopResons.put(s.getItemNo(), s.getReson());
			}
		}

		// 상위/최종 항목의 점수는 계산값이므로 프론트가 보낸 점수는 신뢰하지 않고 제거
		newScores.removeIf(s -> isTopOrFinal(s.getItemNo()));

		Map<Integer, Integer> submitted = new HashMap<>();
		for (EvalVO.InsertScore s : newScores) {
			if (!isRangeValid(s.getItemNo(), s.getScore())) {
				return false;
			}
			submitted.put(s.getItemNo(), s.getScore());
		}

		if (oldScores == null) {
			appendComputedScores(newScores, submitted, TOP_ITEMS, null, submittedTopResons);
			return true;
		}

		Map<Integer, EvalVO.ScoreDetail> oldMap = new HashMap<>();
		for (EvalVO.ScoreDetail o : oldScores) {
			oldMap.put(o.getItemNo(), o);
		}

		Set<Integer> changedGroups = new HashSet<>();
		for (Map.Entry<Integer, Integer> e : submitted.entrySet()) {
			EvalVO.ScoreDetail old = oldMap.get(e.getKey());
			if (old == null) {
				return false;
			}
			if (old.getScore() != e.getValue()) {
				changedGroups.add((e.getKey() / 10) * 10);
			}
		}

		// 점수 변경 없이 상위 항목 reson만 명시적으로 제출된 경우도 재계산(포함) 대상에 추가
		for (int top : TOP_ITEMS) {
			if (submittedTopResons.containsKey(top)) {
				changedGroups.add(top);
			}
		}

		boolean finalTotalSubmitted = submittedTopResons.containsKey(1);
		boolean finalRefSubmitted = submittedTopResons.containsKey(2);

		if (changedGroups.isEmpty() && !finalTotalSubmitted && !finalRefSubmitted) {
			return true; // 정말 아무것도 바뀐 게 없음
		}

		Map<Integer, Integer> merged = new HashMap<>();
		for (EvalVO.ScoreDetail o : oldScores) {
			merged.put(o.getItemNo(), o.getScore());
		}
		merged.putAll(submitted);

		appendComputedScores(newScores, merged, changedGroups, oldMap, submittedTopResons, finalTotalSubmitted,
				finalRefSubmitted);
		return true;
	}

	private boolean isTopOrFinal(int itemNo) {
		return TOP_ITEMS.contains(itemNo) || itemNo == 1 || itemNo == 2;
	}

	private void appendComputedScores(List<EvalVO.InsertScore> target, Map<Integer, Integer> scoreMap,
			Set<Integer> targetGroups, Map<Integer, EvalVO.ScoreDetail> oldMap,
			Map<Integer, String> submittedTopResons) {
		// 신규 등록용 오버로드: 최종 종합/최종 참조 종합도 항상 포함
		appendComputedScores(target, scoreMap, targetGroups, oldMap, submittedTopResons, true, true);
	}

	private void appendComputedScores(List<EvalVO.InsertScore> target, Map<Integer, Integer> scoreMap,
			Set<Integer> targetGroups, Map<Integer, EvalVO.ScoreDetail> oldMap, Map<Integer, String> submittedTopResons,
			boolean forceFinalTotal, boolean forceFinalRef) {

		Map<Integer, Integer> topScores = new HashMap<>();
		for (int top : TOP_ITEMS) {
			int sum = 0;
			for (Map.Entry<Integer, Integer> e : scoreMap.entrySet()) {
				int itemNo = e.getKey();
				if (itemNo != top && itemNo / 10 == top / 10) {
					sum += e.getValue();
				}
			}
			topScores.put(top, sum);
			if (targetGroups.contains(top)) {
				target.add(new EvalVO.InsertScore(top, sum, resonOf(top, oldMap, submittedTopResons),
						TOP_FIELD_MAP.get(top)));
			}
		}

		boolean finalTotalAffected = forceFinalTotal || targetGroups.contains(10) || targetGroups.contains(20)
				|| targetGroups.contains(30);
		boolean finalRefAffected = forceFinalRef || finalTotalAffected || targetGroups.contains(40);

		int finalTotal = topScores.get(10) + topScores.get(20) + topScores.get(30);
		if (oldMap == null || finalTotalAffected) {
			target.add(new EvalVO.InsertScore(1, finalTotal, resonOf(1, oldMap, submittedTopResons),
					TOP_FIELD_MAP.get(1)));
		}

		int finalRefTotal = finalTotal + topScores.get(40);
		if (oldMap == null || finalRefAffected) {
			target.add(new EvalVO.InsertScore(2, finalRefTotal, resonOf(2, oldMap, submittedTopResons),
					TOP_FIELD_MAP.get(2)));
		}
	}

	private String resonOf(int itemNo, Map<Integer, EvalVO.ScoreDetail> oldMap,
			Map<Integer, String> submittedTopResons) {
		String submitted = submittedTopResons.get(itemNo);
		if (submitted != null && !submitted.isBlank()) {
			return submitted;
		}
		if (oldMap != null && oldMap.containsKey(itemNo)) {
			return oldMap.get(itemNo).getReson();
		}
		return "";
	}

	private boolean isRangeValid(int itemNo, int score) {
		int max = 10;
		if (itemNo == 24 || itemNo == 34) {
			max = 5;
		} else if (itemNo / 10 == 4) {
			max = 3; // 보너스 세부 항목 41,42,43
		}
		return score >= 0 && score <= max;
	}

}
