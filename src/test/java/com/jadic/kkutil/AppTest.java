package com.jadic.kkutil;

import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Mockito.*;
/**
 * Unit test for simple App.
 */
public class AppTest {
    
    final static Logger logger = LoggerFactory.getLogger(AppTest.class); 

    @Test
    public void test() {
        List mock = mock(List.class);
        when(mock.get(anyInt())).thenReturn("aba");
        logger.error("{}", mock.get(100));
        logger.error("{}", mock.get(11));
        mock.add("a");
        assert(mock.size()==20);
        verify(mock).add("a");
        when(mock.get(1)).thenReturn("aaa");
        logger.error("{}",mock.size());
    }
    
    public void testA() {
        LoggerFactory.getLogger(this.getClass().getName()).error("testA");        
    }
}
