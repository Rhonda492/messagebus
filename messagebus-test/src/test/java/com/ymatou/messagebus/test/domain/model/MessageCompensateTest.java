/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.domain.model;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.Date;

import org.junit.Test;

import com.ymatou.messagebus.domain.model.MessageCompensate;
import com.ymatou.messagebus.test.BaseTest;

/**
 * @author wangxudong 2016年9月13日 下午1:49:43
 *
 */
public class MessageCompensateTest extends BaseTest {

    @Test
    public void testCalcRetryTime() {
        String retryPolicy = "1m-2h-3d-e4h-eh-ed";

        Calendar lstRetryTime = Calendar.getInstance();
        Date calcRetryTime = MessageCompensate.calcRetryTime(lstRetryTime.getTime(), 0, null);

        lstRetryTime.add(Calendar.MINUTE, 1);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);

        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 0, null);

        lstRetryTime.add(Calendar.MINUTE, 1);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);

        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 0, retryPolicy);

        lstRetryTime.add(Calendar.MINUTE, 1);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);



        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 1, retryPolicy);

        lstRetryTime.add(Calendar.HOUR, 2);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);



        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 2, retryPolicy);

        lstRetryTime.add(Calendar.DATE, 3);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);



        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 3, retryPolicy);

        lstRetryTime.add(Calendar.HOUR, 4);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);



        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 4, retryPolicy);

        lstRetryTime.add(Calendar.HOUR, 1);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);



        lstRetryTime = Calendar.getInstance();
        calcRetryTime = MessageCompensate.calcRetryTime(null, 5, retryPolicy);

        lstRetryTime.add(Calendar.DATE, 1);
        assertEquals(lstRetryTime.getTime(), calcRetryTime);
    }

    @Test
    public void testCalcNextRetryMinute() {
        String retryPolicy = "2m-2h-3d-e4h-eh-ed";

        int m1 = MessageCompensate.calcNextRetryMinute(0, retryPolicy);
        int m2 = MessageCompensate.calcNextRetryMinute(1, retryPolicy);
        int m3 = MessageCompensate.calcNextRetryMinute(2, retryPolicy);
        int m4 = MessageCompensate.calcNextRetryMinute(3, retryPolicy);
        int m5 = MessageCompensate.calcNextRetryMinute(4, retryPolicy);
        int m6 = MessageCompensate.calcNextRetryMinute(5, retryPolicy);
        int m7 = MessageCompensate.calcNextRetryMinute(6, retryPolicy);


        assertEquals(2, m1);
        assertEquals(2 * 60, m2);
        assertEquals(3 * 60 * 24, m3);
        assertEquals(4 * 60, m4);
        assertEquals(60, m5);
        assertEquals(60 * 24, m6);
        assertEquals(60 * 24, m7);
    }

    @Test
    public void testNeedRetry() {
        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.MINUTE, -1);
        MessageCompensate messageCompensate = new MessageCompensate();
        messageCompensate.setRetryTimeout(calendar.getTime());

        assertEquals(false, messageCompensate.needRetry(null));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        messageCompensate.setRetryTimeout(calendar.getTime());
        assertEquals(true, messageCompensate.needRetry(null));

        calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 1);
        messageCompensate.setRetryTimeout(calendar.getTime());
        assertEquals(true, messageCompensate.needRetry("ed"));

        calendar.add(Calendar.MINUTE, 1);
        messageCompensate.setRetryTimeout(calendar.getTime());
        assertEquals(true, messageCompensate.needRetry("e1h"));

        calendar.add(Calendar.MINUTE, 1);
        messageCompensate.setRetryTime(calendar.getTime());
        assertEquals(true, messageCompensate.needRetry("1m"));

        messageCompensate.incCompensateCount();
        assertEquals(false, messageCompensate.needRetry("1m"));

        messageCompensate.incCompensateCount();
        assertEquals(false, messageCompensate.needRetry("1m-1d"));

        messageCompensate.incCompensateCount();
        assertEquals(true, messageCompensate.needRetry("1m-1d-1h-2d"));

    }
}
