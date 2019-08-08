package com.xwq.dao.impl;

import com.xwq.annotation.Repository;
import com.xwq.dao.UserDao;
import com.xwq.entity.User;

@Repository
public class UserDaoImpl implements UserDao {
    public User getUser(Long userId) {
        User user = new User();
        user.setUserId(3L);
        user.setUsername("guanyunzhang");
        user.setPassword("1234");
        return user;
    }
}
