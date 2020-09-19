package com.nostalgia.service;

import com.nostalgia.entity.Stock;

/**
 * @author nostalgia
 * @date 2020/9/7 15:15
 */
public interface OrderService {
    Integer createOrder(Integer id);

    String getMd5(Integer id, Integer userid);

    int kill(Integer id, Integer userid, String md5);
}
