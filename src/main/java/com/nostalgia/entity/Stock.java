package com.nostalgia.entity;

import lombok.Data;

/**
 * @author nostalgia
 * @date 2020/9/7 14:45
 */
@Data
public class Stock {
    Integer id;
    String name;
    Integer count;
    Integer sale;
    Integer version;
}
