package com.tl.global.api;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;

import com.tl.global.location.LocationVO;
import com.tl.global.location.LocationVO.GeocodingApiResponse;
import com.tl.global.location.LocationVO.GeocodingApiResponseWrapper2;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GeocodingService {
	private final RestTemplate restTemplate;
	
	@Value("${kakao-rest.key}")
	private String keyStr;
	
	/**
	 * 카카오맵 로컬 api 지오코딩
	 * https://developers.kakao.com/
	 * 
	 * 네이버꺼 사용해서 구현해도 상관없음
	 */
	public LocationVO.Detail getCoordinate(String address) {
		
		// 요청 body
		Map<String, Object> requestBody = new HashMap<>();
		
		// Http 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "KakaoAK "+keyStr);
        HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(requestBody, headers);
        
        // uri생성
        URI uri = UriComponentsBuilder
				.fromUriString("https://dapi.kakao.com/v2/local/search/address")
				.queryParam("query", address)
				.build()
				.encode()
				.toUri();
        
        // API 요청
        ResponseEntity<LocationVO.GeocodingApiResponseWrapper1> response = restTemplate.exchange(
        		uri,
        		HttpMethod.GET,
        		httpEntity,
        		LocationVO.GeocodingApiResponseWrapper1.class);
        
        if(response.getBody() == null)
        	throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR);
        
        if (response.getBody().getMeta().getTotal_count() == 0)
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        
        GeocodingApiResponseWrapper2 doc = response.getBody().getDocuments().get(0);
        
        // 도로롱 주소 없으면 지번 주소 대신 사용
        GeocodingApiResponse target = (doc.getRoad_address() != null) 
                ? doc.getRoad_address() 
                : doc.getAddress();
        
        LocationVO.Detail result = new LocationVO.Detail(target);
        
        return result;
	}
}
