package com.ymatou.messagebus.test;

import java.util.HashMap;
import java.util.UUID;

import org.junit.runner.RunWith;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author wangxudong 2016年7月22日 下午3:40:09
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/applicationContext.xml"})
public class RestBaseTest {


    /**
     * 构建MockHeader
     * 
     * @return
     */
    protected HashMap<String, String> buildMockHeader() {
        HashMap<String, String> header = new HashMap<String, String>();
        header.put("mock", "1");
        header.put("mockId", UUID.randomUUID().toString());

        return header;
    }

    /**
     * 构建带Mock
     * 
     * @param servletRequest
     */
    protected MockHttpServletRequest buildServletRequestWithMock() {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest();
        servletRequest.addHeader("mock", 1);
        servletRequest.addHeader("mockId", UUID.randomUUID().toString());

        return servletRequest;
    }



}
