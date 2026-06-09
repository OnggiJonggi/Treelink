package com.tl.global.file;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FileService {
	private final FileMapper fileMapper;

	public FileInfoVO.SavePath getSavePath(int companyNo, boolean isAdmin) {
		return fileMapper.selectLogoSavePath(companyNo, isAdmin);
	}

}
