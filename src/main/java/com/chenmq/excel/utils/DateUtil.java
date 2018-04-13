package com.chenmq.excel.utils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Auth chenmq
 */

public class DateUtil {

    /**
     * 格式化日期
     *
     * @return
     */
    public static Date fomatDate(String date,String simpleDateFormat) {
        DateFormat fmt = new SimpleDateFormat(simpleDateFormat);
        try {
            return fmt.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

}
