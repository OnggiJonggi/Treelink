package com.tl.global.file;

import lombok.Builder;
import lombok.Getter;

/**
 * Muiltipartfile 객체 변환용
 */
@Getter
@Builder
public class FileDataVO {
	private String originalName;
	private String mime;
	private long size;
	private byte[] bytes;

	// 정상 파일인지 확인
	public boolean isValid() {
		if (originalName == null || originalName.isBlank()) return false;
		if (mime == null || mime.isBlank()) return false;
		if (size <= 0) return false;
		if (bytes == null || bytes.length == 0) return false;
		return true;
	}
}
