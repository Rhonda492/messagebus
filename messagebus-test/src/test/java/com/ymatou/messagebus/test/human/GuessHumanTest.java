/**
 * (C) Copyright 2016 Ymatou (http://www.ymatou.com/).
 *
 * All rights reserved.
 */
package com.ymatou.messagebus.test.human;

import org.junit.Test;

import com.ymatou.messagebus.test.Guess;
import com.ymatou.messagebus.test.GuessDAO;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;

public class GuessHumanTest {

    @Tested
    @Mocked
    Guess guess = new Guess(3);

    @Injectable
    GuessDAO guessDAO;



    @Test
    public void testfail3times() {
        new Expectations() {
            {
                guess.tryIt();
                result = false;

            }
        };

        guess.doit();

        new Verifications() {
            {
                guessDAO.saveResult(false, anyInt);
                times = 1;
                maxTimes = 1;
            }
        };
    }
}
