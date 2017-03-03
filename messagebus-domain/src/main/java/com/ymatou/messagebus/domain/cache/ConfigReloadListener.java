/*
 *
 *  (C) Copyright 2017 Ymatou (http://www.ymatou.com/).
 *  All rights reserved.
 *
 */

package com.ymatou.messagebus.domain.cache;

/**
 * @author luoshiqian 2017/2/27 19:30
 */
@FunctionalInterface
public interface ConfigReloadListener {
    void callback();
}
