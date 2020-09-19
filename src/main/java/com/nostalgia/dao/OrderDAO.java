package com.nostalgia.dao;

import com.nostalgia.entity.Order;
import org.apache.ibatis.annotations.Options;
import org.springframework.stereotype.Repository;

/**
 * @author nostalgia
 * @date 2020/9/7 14:44
 */
@Repository
public interface OrderDAO {
    void createOrder(Order order);
}
