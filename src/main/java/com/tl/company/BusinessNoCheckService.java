package com.tl.company;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.tl.global.exception.CustomException;
import com.tl.global.exception.ErrorCodeEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BusinessNoCheckService {
	private final RestTemplate restTemplate;
	
	@Value("${public-data.key}")
	private String keyStr;
	
	/**
	 * 국세청 사업자등록정보 진위확인 API
	 * https://www.data.go.kr/data/15107737/standard.do
	 * 
	 * 사업자 등록번호, 대표 이름, 창립일을 입력받아
	 * 실존하는 사업자 등록번호인지 확인
	 * 사업자인지만 판별하지 업종이 뭔지는 모름
	 * 업종까지 판별하려면 다른 유료 서비스 사용하거나
	 * 일정 계약금액 이상이면 건설 면허가 필요하다는 점을 활용하던가.
	 */
	public void checkBusinessNo(BusinessNoCheckVO.request request) {
		BusinessNoCheckVO.apiRequest apiRequest = BusinessNoCheckVO.apiRequest.builder()
		.b_no(request.getBusinessNo())
		.start_dt(request.getCreatedOn()
			.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
		.p_nm(request.getRepresentativeName())
		.p_nm2("")
		.b_nm("")
		.corp_no("")
		.b_sector("")
		.b_type("")
		.build();
		
		// 요청 body
		Map<String, Object> requestBody = new HashMap<>();
		requestBody.put("businesses", List.of(apiRequest));
		
		// Http 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
        
        // 요청 url
        String url = "https://api.odcloud.kr/api/nts-businessman/v1/validate?serviceKey=" + keyStr;
        
        // API 요청
        ResponseEntity<BusinessNoCheckVO.apiResponseWrapper> response = restTemplate.exchange(
				url,
				HttpMethod.POST,
				httpEntity,
				BusinessNoCheckVO.apiResponseWrapper.class
        		);
        
        if (response.getBody() == null
        	|| response.getBody().getCode() != null
    		|| response.getBody().getData() == null
    		|| response.getBody().getData().isEmpty())
    		throw new CustomException(ErrorCodeEnum.BUSINESS_NO_API_NOT_WORKING);
        
        else if(response.getBody().getData().get(0).getValid().equals("02"))
        	throw new CustomException(ErrorCodeEnum.BUSINESS_NO_NULL);
        
        else if(!response.getBody().getData().get(0).getValid().equals("01"))
        	throw new CustomException(ErrorCodeEnum.BUSINESS_NO_API_NOT_WORKING);
        	
	}
	
}
