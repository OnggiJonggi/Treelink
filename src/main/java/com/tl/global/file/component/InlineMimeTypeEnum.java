package com.tl.global.file.component;

import lombok.Getter;

/**
 * 브라우저에서 바로 열람이 가능한 파일 형식
 */
@Getter
public enum InlineMimeTypeEnum {

	    // PDF
	    PDF("application/pdf"),

	    // 이미지
	    IMAGE_JPEG("image/jpeg"),
	    IMAGE_PNG("image/png"),
	    IMAGE_GIF("image/gif"),
	    IMAGE_WEBP("image/webp"),
	    IMAGE_SVG("image/svg+xml"),
	    IMAGE_BMP("image/bmp"),

	    // 영상
	    VIDEO_MP4("video/mp4"),
	    VIDEO_WEBM("video/webm"),

	    // 오디오
	    AUDIO_MPEG("audio/mpeg"),
	    AUDIO_OGG("audio/ogg"),

	    // 텍스트
	    TEXT_PLAIN("text/plain"),
	    TEXT_HTML("text/html");

	    private final String mimeType;

	    /**
	     * mime타입이 인라인 뷰가 가능한지 판별
	     * @param mimeType
	     * @return 불값
	     */
	    public static boolean isInline(String mimeType) {
	    	
	        if (mimeType == null) return false;
	        
	        for (InlineMimeTypeEnum value : values()) {
	            if (value.mimeType.equalsIgnoreCase(mimeType)) {
	                return true;
	            }
	        }
	        return false;
	    }

	    
		private InlineMimeTypeEnum(String mimeType) {
			this.mimeType = mimeType;
		}
}
