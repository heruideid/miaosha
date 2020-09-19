package com.nostalgia.service.impl;

import com.nostalgia.dao.OrderDAO;
import com.nostalgia.dao.StockDAO;
import com.nostalgia.dao.UserDAO;
import com.nostalgia.entity.Order;
import com.nostalgia.entity.Stock;
import com.nostalgia.entity.User;
import com.nostalgia.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author nostalgia
 * @date 2020/9/7 15:16
 */
@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    OrderDAO orderDAO;

    @Autowired
    private StockDAO stockDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional
    public  Integer createOrder(Integer id) {
        //redis校验抢购时间
        if(!stringRedisTemplate.hasKey("kill" + id)){
            throw new RuntimeException("秒杀超时,活动已经结束啦!!!");
        }
        //查询redis，看当前商品是否已过了秒杀活动时间
        final Object o = stringRedisTemplate.opsForSet().getOperations();
        //校验库存
        Stock stock = checkStock(id);
        //扣库存
        updateSale(stock);
        //下订单
        return createOrder(stock);
    }

    @Override
    public String getMd5(Integer id, Integer userid) {
        //检验用户的合法性
        User user = userDAO.findById(userid);
        if(user==null) {
            throw new RuntimeException("用户信息不存在!");
        }
        log.info("用户信息:[{}]",user.toString());
        //检验商品的合法行
        Stock stock = stockDAO.checkStock(id);
        if(stock==null) {
            throw new RuntimeException("商品信息不合法!");
        }
        log.info("商品信息:[{}]",stock.toString());
        //生成hashkey
        String hashKey = "KEY_"+userid+"_"+id;
        //生成md5//这里!QS#是一个盐 随机生成
        String key = DigestUtils.md5DigestAsHex((userid+id+"!Q*jS#").getBytes());
        stringRedisTemplate.opsForValue().set(hashKey, key, 3600, TimeUnit.SECONDS);
        log.info("Redis写入：[{}] [{}]", hashKey, key);
        return key;
    }

    @Override
    public int kill(Integer id, Integer userid, String md5) {

        //校验redis中秒杀商品是否超时
        //        if(!stringRedisTemplate.hasKey("kill"+id))
        //            throw new RuntimeException("当前商品的抢购活动已经结束啦~~");

        //先验证签名
        String hashKey = "KEY_"+userid+"_"+id;
        String s = stringRedisTemplate.opsForValue().get(hashKey);
        if (s==null) {
            throw  new RuntimeException("没有携带验证签名,请求不合法!");
        }
        if (!s.equals(md5)) {
            throw  new RuntimeException("当前请求数据不合法,请稍后再试!");
        }

        //校验库存
        Stock stock = checkStock(id);
        //更新库存
        updateSale(stock);
        //创建订单
        return createOrder(stock);
    }

    //校验库存
    private Stock checkStock(Integer id) {
        Stock stock = stockDAO.checkStock(id);
        if(stock==null){
            throw new RuntimeException("商品不存在");
        }

        if (stock.getSale().equals(stock.getCount())) {
            throw new RuntimeException("库存不足");
        }
        return stock;
    }

    //扣库存
    private void updateSale(Stock stock) {
        stock.setSale(stock.getSale() + 1);
        stock.setVersion(stock.getVersion()+1);
        Integer result = stockDAO.updateSale(stock);
        if(result==0) {
            throw new RuntimeException("抢购失败!");
        }
    }

    //插入订单
    private Integer createOrder(Stock stock) {
        Order order = new Order();
        order.setSid(stock.getId());
        order.setCreateTime(new Date());
        order.setName(stock.getName());
        orderDAO.createOrder(order);
        //System.out.println("===== "+order.getId());
        return order.getId();
    }
}
