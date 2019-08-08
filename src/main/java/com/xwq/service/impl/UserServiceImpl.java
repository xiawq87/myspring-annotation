package com.xwq.service.impl;

import com.xwq.annotation.Autowired;
import com.xwq.annotation.Service;
import com.xwq.dao.UserDao;
import com.xwq.entity.User;
import com.xwq.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserDao userDao;

    public User getUser(Long userId) {
        System.out.println("UserServiceImpl.getUser -- userId: " + userId);
        return userDao.getUser(userId);
    }
}
