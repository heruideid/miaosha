package com.nostalgia.controller;

import com.google.common.util.concurrent.RateLimiter;
import com.nostalgia.entity.Order;
import com.nostalgia.service.OrderService;
import com.nostalgia.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * @author nostalgia
 * @date 2020/9/7 14:59
 */
@Slf4j
@RestController
public class MainController {
    @Autowired
    OrderService orderService;

    @Autowired
    UserService userService;
    private RateLimiter rateLimiter=RateLimiter.create(100);

//    @PostMapping("/createOrder")
//    public String craeteOrder(@RequestBody Order order){
//        orderService.createOrder();
//        return order.toString();
//    }

    //生成md5值的方法
    @RequestMapping("md5")
    public String getMd5(Integer id, Integer userId) {
        String md5;
        try {

            md5 = orderService.getMd5(id, userId);
        }catch (Exception e){
            e.printStackTrace();
            return "获取md5失败: "+e.getMessage();
        }
        return "获取md5信息为: "+md5;
    }

    //秒杀方法
    @GetMapping("sale")
    public String sale(Integer id,Integer userId){
        int orderId = 0;
        long begin=System.currentTimeMillis();
        if(!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)){
            log.info("当前请求被限流！");
            return "当前请求被限流！";
        }
        log.info("等待时间 : {} ms",System.currentTimeMillis()-begin);
        try{
            //根据商品id创建订单,返回创建订单的id
            orderId =  orderService.createOrder(id);
            return "orderId = " + orderId;
        }catch (Exception e){
            log.info(e.getMessage());
            return e.getMessage();
        }
    }

    //开发一个秒杀方法 乐观锁防止超卖+ 令牌桶算法限流
    @GetMapping("killtokenmd5")
    public String killtoken(Integer id,Integer userid,String md5) {
        System.out.println("秒杀商品的id = " + id);
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)) {
            log.info("抛弃请求: 抢购失败,当前秒杀活动过于火爆,请重试");
            return "抢购失败,当前秒杀活动过于火爆,请重试!";
        }
        try {
            //根据秒杀商品id 去调用秒杀业务
            int orderId = orderService.kill(id,userid,md5);
            return "秒杀成功,订单id为: " + String.valueOf(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }

    //开发一个秒杀方法 乐观锁防止超卖+ 令牌桶算法限流
    @GetMapping("killtokenmd5limit")
    public String killtokenlimit(Integer id,Integer userid,String md5) {
        //加入令牌桶的限流措施
        if (!rateLimiter.tryAcquire(3, TimeUnit.SECONDS)) {
            log.info("抛弃请求: 抢购失败,当前秒杀活动过于火爆,请重试");
            return "抢购失败,当前秒杀活动过于火爆,请重试!";
        }
        try {
            //加入单用户限制调用频率
            int count = userService.saveUserCount(userid);
            log.info("用户截至该次的访问次数为: [{}]", count);
            boolean isBanned = userService.getUserCount(userid);
            if (isBanned) {
                log.info("购买失败,超过频率限制!");
                return "购买失败，超过频率限制!";
            }
            //根据秒杀商品id 去调用秒杀业务
            int orderId = orderService.kill(id,userid,md5);
            return "秒杀成功,订单id为: " + String.valueOf(orderId);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
