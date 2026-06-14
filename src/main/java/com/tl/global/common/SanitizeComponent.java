package com.tl.global.common;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 사용자가 HTML 태그 사용이 가능한 입력칸에
 * 멋대로 나쁜 태그를 으쌰으쌰하면 떽!하는 메소드지요
 * 
 * 이 클래스에만 JSOUP 라이브러리가 쓰이지요
 */
@Component
public class SanitizeComponent {
	
	// 허용할 태그/속성 정책
	private static final Safelist POLICY = Safelist.relaxed()
			
			// 외부 이미지 가세요라
			.removeProtocols("img", "src", "http", "https")

			// 외부 링크 허용. 'javascript:' 차단
			.addProtocols("a", "href", "http", "https").addAttributes("a", "target") // target="_blank" 허용
			.addEnforcedAttribute("a", "rel", "noopener noreferrer") // 보안 속성 자동 추가

			// summernote 전용 태그
			.addTags("s", "u") // 취소선, 밑줄
			.addAttributes(":all", "style"); // 인라인 스타일 허용

	
	/**
	 * 문자열 소독
	 */
	public String sanitize(String rawHtml) {
		
		// 없는데 여길 왜 왔어?
		if (rawHtml == null || rawHtml.isBlank()) return "";

		// '/'로 시작하는 상대경로는 신뢰하고, 절대 주소로 바꾸지 마
		String cleaned = Jsoup.clean(rawHtml, "/", POLICY.preserveRelativeLinks(true));
		
		return cleaned;
	}
}
