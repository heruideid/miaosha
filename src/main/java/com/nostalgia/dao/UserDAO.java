package com.nostalgia.dao;

import com.nostalgia.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserDAO {
    User findById(Integer id);
}