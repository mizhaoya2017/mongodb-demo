package com.example.mongodbdemo.data.bo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @Author xieyitong  <xieyitong@situdata.com>
 * @Date 2020/8/21 14:09
 **/

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserInfoCacheBO {
    private Integer id;
    private String userName;
    private String realName;
    private String telephone;
    private String token;
    private Integer role;
    private String businessType;
    private String channel;
}
