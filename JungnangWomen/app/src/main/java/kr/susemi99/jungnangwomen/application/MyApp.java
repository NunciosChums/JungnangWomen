package kr.susemi99.jungnangwomen.application;

import android.app.Application;
import android.content.Context;

public class MyApp extends Application
{
  private static Context context;

  @Override
  public void onCreate()
  {
    super.onCreate();
    context = getApplicationContext();
  }

  public static Context context()
  {
    return context;
  }
}
