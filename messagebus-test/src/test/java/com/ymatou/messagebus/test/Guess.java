/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test;

/** 在n次机会随机猜骰子点数 ,结果保存到数据库中 */
public class Guess {
    private int maxTryTime; // 最大重试次数
    private int tryTime = 0; // 当前重试次数
    private int number = (int) (Math.random() * 6); // 目标数字
    private GuessDAO guessDAO; // 持久化依赖

    public Guess(int maxRetryTime) {
        this.maxTryTime = maxRetryTime;
    }

    public void doit() {
        while (tryTime++ < maxTryTime && !tryIt()) {
            // 到达最大尝试次数仍不成功则调用handle
            if (tryTime == maxTryTime) {
                failHandle();
            }
        }
    }

    public boolean tryIt() { // 最坏情况下调用maxRetryTime次tryIt(),猜中则保存信息
        if (number == randomGuess()) {
            guessDAO.saveResult(true, number);
            return true;
        }
        return false;
    }

    public void failHandle() { // 失败处理，猜不中时调用
        guessDAO.saveResult(false, number);
    }

    private int randomGuess() { // 猜的随机过程
        return (int) (Math.random() * 6);
    }

    /**
     * @return the maxTryTime
     */
    public int getMaxTryTime() {
        return maxTryTime;
    }

    /**
     * @param maxTryTime the maxTryTime to set
     */
    public void setMaxTryTime(int maxTryTime) {
        this.maxTryTime = maxTryTime;
    }

    public int guessNum() {
        return guessDAO.getNum();
    }
}
