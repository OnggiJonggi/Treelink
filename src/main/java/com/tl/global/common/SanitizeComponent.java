package com.tl.global.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 사용자 입력값 소독
 */
@Component
public class SanitizeComponent {

	/**
	 * 사용자가 HTML 태그 사용이 가능한 입력칸에 멋대로 나쁜 태그를 으쌰으쌰하면 떽!하는 메소드지요
	 * 
	 * JSOUP 라이브러리가 쓰이지요
	 */
	private static final Safelist POLICY = Safelist.relaxed()
		    .removeProtocols("img", "src", "http", "https")
		    .addProtocols("a", "href", "http", "https")
		    .addAttributes("a", "target")
		    .addEnforcedAttribute("a", "rel", "noopener noreferrer")
		    .addTags("s", "u", "font")
		    .addAttributes("font", "color")
		    .addAttributes("u", "style")
		    .addAttributes("p", "style")
		    .addAttributes("span", "style")
		    .addAttributes("div", "style")
		    .addAttributes(":all", "class");

	public String sanitize(String rawHtml) {
		
		// 없는데 여길 왜 와?
		if (rawHtml == null || rawHtml.isBlank())
			return "";

		// 임시용
		String cleaned = Jsoup.clean(rawHtml, "/", POLICY.preserveRelativeLinks(true));
		
		// 배포용
//		String cleaned = Jsoup.clean(rawHtml, "https://axc~", POLICY);

		// data: URI 이미지 제거 필터
		Document doc = Jsoup.parse(cleaned);
		doc.select("img[src~=(?i)^data:]").remove();

		return doc.body().html();
	}
	
	/**
	 * 검색어 소독
	 * 오라클의 like 예약어 %,_ 이스케이프 및 길이 제한
	 * 
	 * @param 검색 문자열
	 * @param 최대 허용 길이
	 * @return 소독된 문자열
	 */
	public String searchKeyword(String keyword, int maxLength) {
		
		// 없으면 가라
		if (keyword == null) return null;

		// trim()
		keyword = keyword.trim();

		// 길면 가
		if (keyword.length() > maxLength)
			keyword = keyword.substring(0, maxLength);

		// 이스케이프 문자 : '/'
		keyword = keyword
				.replace("/", "//")
				.replace("%", "/%")
				.replace("_", "/_");

		return keyword;
	}
}
