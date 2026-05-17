package website.treelink.global.api;

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

import website.treelink.global.exception.CustomException;
import website.treelink.global.exception.ErrorCode;

@Service
public class BusinessNoCheckService {
	
	private final RestTemplate restTemplate;
	public BusinessNoCheckService(RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	
	@Value("${public-data.key}")
	private String keyStr;
	
	/**
	 * 국세청 사업자등록정보 진위확인 API
	 */
	public void checkBusinessNo(BusinessNoCheckVO.request request) {
		BusinessNoCheckVO.apiRequest apiRequest = BusinessNoCheckVO.apiRequest.builder()
		.b_no(request.getBusinessNo())
		.start_dt(request.getCreateDate()
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
    		|| response.getBody().getData() == null
    		|| response.getBody().getData().isEmpty()
        	|| !response.getBody().getData().get(0).getValid().equals("01"))
        	throw new CustomException(ErrorCode.API_BADREQUEST);
	}
	
}
