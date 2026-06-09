package com.tl.global.file.component;

import lombok.Getter;

/**
 * 파일이 이미지인지 검토
 */
@Getter
public enum ImageEnum {
    IMAGE_JPEG("image/jpeg"),
    IMAGE_PNG("image/png"),
    IMAGE_GIF("image/gif"),
    IMAGE_WEBP("image/webp")
    ;
    // SVG : 보안 위험(스크립트 삽입) 

    private final String mimeType;

    /**
     * 이거 이미지 파일이에요?
     * @param MIME
     * @return 불값
     */
    public static boolean isImage(String mimeType) throws Exception{
    	
        for (ImageEnum value : values()) {
            if (value.mimeType.equalsIgnoreCase(mimeType)) return true;
        }
        
        return false;
    }

    
	private ImageEnum(String mimeType) {
		this.mimeType = mimeType;
	}
}
