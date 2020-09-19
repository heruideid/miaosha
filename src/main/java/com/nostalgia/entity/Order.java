package com.nostalgia.entity;

import lombok.Data;

import java.util.Date;

/**
 * @author nostalgia
 * @date 2020/9/7 14:45
 */
@Data
public class Order {
    Integer id;
    Integer sid;
    String name;
    Date createTime;
}
