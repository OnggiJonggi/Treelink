package com.tl.global.file;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * Muiltipartfile 객체 변환용
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@SuperBuilder
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
