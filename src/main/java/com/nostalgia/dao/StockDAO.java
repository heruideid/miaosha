package com.nostalgia.dao;

import com.nostalgia.entity.Stock;
import org.springframework.stereotype.Repository;

/**
 * @author nostalgia
 * @date 2020/9/7 14:44
 */
@Repository
public interface StockDAO {
    Stock checkStock(Integer id);//校验库存
    Integer updateSale(Stock stock);//扣除库存
}
