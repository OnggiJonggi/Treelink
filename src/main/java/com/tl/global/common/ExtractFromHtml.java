package com.tl.global.common;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

/**
 * summernote로 입력받은 문자열 에서
 * 파일 이름 추출하기
 * 
 */
@Component
public class ExtractFromHtml {

	/**
	 * 회사 소개에서 이미지 이름 추출
	 */
	public List<String> fileNameFromIntro(String intro) {
		
	    Document doc = Jsoup.parse(intro);
	    Elements imgTags = doc.select("img[src]");

	    Pattern pattern = Pattern.compile("/intro/(.+)$");
	    
		List<String> fileNames = new ArrayList<>();
		for (Element img : imgTags) {
			String src = img.attr("src");
			Matcher matcher = pattern.matcher(src);
			if (matcher.find()) {
				fileNames.add(matcher.group(1));
			}
		}
		
		return fileNames;
	}
}
