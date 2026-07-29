package com.tl.global.location;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tl.global.api.GeocodingService;
import com.tl.global.security.role.CanAccess;
import com.tl.global.security.role.RoleEnum;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/location")
@RequiredArgsConstructor
@Slf4j
public class LocationApiController {
	private final GeocodingService geocodingService;
	
	/**
	 * 지오코딩
	 * 
	 * 관리자
	 */
	@CanAccess(RoleEnum.ADMIN)
	@GetMapping("geocoding")
	public ResponseEntity<LocationVO.Detail> getCoordinate(
			@RequestParam String address){
		
		LocationVO.Detail result = geocodingService.getCoordinate(address);
		
		return ResponseEntity.ok(result);
	}
}
