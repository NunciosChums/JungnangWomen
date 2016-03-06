package kr.susemi99.jungnangwomen.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateFormatter
{
  public static String parseDate(String dateStr, String pattern)
  {
    String result = dateStr;

    try
    {
      SimpleDateFormat formatter = new SimpleDateFormat(pattern);
      Date date = formatter.parse(dateStr);
      result = format(date.getTime(), "yyyy.MM.dd");
    } catch (Exception e)
    {
      e.printStackTrace();
    }

    return result;
  }

  private static String format(long timeInMills, String pattern)
  {
    SimpleDateFormat formatter = new SimpleDateFormat(pattern);
    return formatter.format(timeInMills);
  }
}
