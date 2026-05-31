package com.tl.global.security;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper {
	public List<String> selectMemberRole(int number);
}
