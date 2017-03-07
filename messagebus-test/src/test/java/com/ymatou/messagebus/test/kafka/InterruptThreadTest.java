/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.messagebus.test.kafka;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author luoshiqian 2017/2/20 16:58
 */
public class InterruptThreadTest {

    @Test
    public void test1(){
        Date date = new Date(1488605598150L);
        System.out.println(DateFormatUtils.format(date,"yyyy-MM-dd hh:mm:ss "));
        System.out.println(date);
    }

    /**
     * 可以看出超时 没有报异常是 直接执行下面的
     */
    @Test
    public void countDownTest(){

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            countDownLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("time out exception");
            e.printStackTrace();
        }
        System.out.println("normal code");

    }

    /**
     * InterruptedException 是 调用Thread.interrupt()引起的
     */
    @Test
    public void countDownTest2(){
        Thread thread = Thread.currentThread();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        TimeUnit.SECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    thread.interrupt();
                }
            }).start();
            countDownLatch.await(10,TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("time out exception");
            e.printStackTrace();
        }
        System.out.println("normal code");

    }

    @Test
    public void countDownTest3(){
        Thread thread = Thread.currentThread();
        final AtomicBoolean a = new AtomicBoolean(true);
        while (a.get())
        {
            CountDownLatch countDownLatch = new CountDownLatch(1);
            try {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            TimeUnit.SECONDS.sleep(10);
                        } catch (InterruptedException e) {
                            System.out.println("InterruptedException");
                            e.printStackTrace();
                        }
                        countDownLatch.countDown();
                        a.set(false);
                    }
                }).start();
               boolean b =  countDownLatch.await(6,TimeUnit.SECONDS);
               System.out.println("timeout:"+b);
            } catch (InterruptedException e) {
                System.out.println("time out exception");
                e.printStackTrace();
            }

        }


    }

    @Test
    public void test() {

        TestInterruptingThread4 t1 = new TestInterruptingThread4();
        TestInterruptingThread4 t2 = new TestInterruptingThread4();

        t1.start();
        t1.interrupt();

        t2.start();

        try {
            TimeUnit.SECONDS.sleep(2L);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    static class TestInterruptingThread4 extends Thread {

        public void run() {
            for (int i = 1; i <= 2; i++) {

                if (Thread.interrupted()) {
                    System.out.println(Thread.currentThread().getName()+"code for interrupted thread");
                } else {
                    System.out.println(Thread.currentThread().getName()+"code for normal thread");
                }

            }//end of for loop
        }
    }
}


