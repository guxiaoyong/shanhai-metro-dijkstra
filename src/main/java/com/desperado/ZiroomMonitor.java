/*
 * Alibaba.com Inc.
 * Copyright (c) 2004-2020 All Rights Reserved.
 */
package com.desperado;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;

/**
 * 自如释放监控
 *
 * @author calvin.gxy
 * @version : ZiroomMonitor.java, v 0.1 2020年09月21日 16:33 calvin.gxy Exp $
 */
@Component
@Configuration      //1.主要用于标记配置类，兼备Component的效果。
@EnableScheduling   // 2.开启定时任务
public class ZiroomMonitor {

    //private String target   = "http://sh.ziroom.com/x/777155837.html";
    private String target = "http://sh.ziroom.com/x/792811637.html";
    //限制发送次数
    private int    count  = 5;

    private static void sendEmail() throws Exception {
        MailUtil.send();
    }

    //3.添加定时任务
    //@Scheduled(cron = "0/5 * * * * ?")
    //或直接指定时间间隔，例如：5秒
    @Scheduled(fixedRate = 5000)
    public void monitor() {
        if (count > 0) {
            try {
                String response = HttpUtils.sendGet(target, new HashMap<>());
                if (response.contains("status iconicon_release")) {
                    System.out.println(LocalDateTime.now() + ":still processing! wait!");
                } else {
                    System.out.println(LocalDateTime.now() + ":quick get it!");
                    MailUtil.send();
                    count--;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}