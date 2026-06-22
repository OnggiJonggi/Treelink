package com.tl.global.common;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

/**
 * 사용자가 HTML 태그 사용이 가능한 입력칸에 멋대로 나쁜 태그를 으쌰으쌰하면 떽!하는 메소드지요
 * 
 * JSOUP 라이브러리가 쓰이지요
 */
@Component
public class SanitizeComponent {

	private static final Safelist POLICY = Safelist.relaxed()
			.removeProtocols("img", "src", "http", "https") // 링크, 이미지 링크 삭제
			
			.addProtocols("a", "href", "http", "https")
			.addAttributes("a", "target")
			.addEnforcedAttribute("a", "rel", "noopener noreferrer")
			.addTags("s", "u")
			.addAttributes("p", "style")
			.addAttributes("span", "style")
			.addAttributes("div", "style");

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
}
