package com.ymatou.messagebus.facade;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 自身内容能以可读方式输出
 * @author wangxudong
 *
 */
public abstract class PrintFriendliness implements Serializable {

	/**
	 * 序列化版本
	 */
	private static final long serialVersionUID = -8504829055186474478L;

	@Override
    public String toString() {
        return ReflectionToStringBuilder.reflectionToString(this, SHORT_PREFIX_WITH_FORMATTED_DATE_STYLE);
    }
    
    
    /**
     * 
     * <p>{@link ToStringStyle#SHORT_PREFIX_STYLE} with date string formatted</p>
     * @author wangxudong
     */
    public static class ShortPrefixWithFormattedDateStyle extends ToStringStyle {
        private static final long serialVersionUID = 1L;

        /**
         * <p>Constructor.</p>
         *
         * <p>Use the static constant rather than instantiating.</p>
         */
        private ShortPrefixWithFormattedDateStyle() {
            super();
            this.setUseShortClassName(true);
            this.setUseIdentityHashCode(false);
        }

        @Override
        protected void appendDetail(StringBuffer buffer, String fieldName, Object value) {
            if (value instanceof Date) {
                //readable date format
                value = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
            }
            super.appendDetail(buffer, fieldName, value);
        }
        
        /**
         * <p>Ensure <code>Singleton</ode> after serialization.</p>
         * @return the singleton
         */
        private Object readResolve() {
            return SHORT_PREFIX_WITH_FORMATTED_DATE_STYLE;
        }

    }
    
    public static final ToStringStyle SHORT_PREFIX_WITH_FORMATTED_DATE_STYLE = new ShortPrefixWithFormattedDateStyle( );
}
