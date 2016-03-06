package kr.susemi99.jungnangwomen.networks;

import kr.susemi99.jungnangwomen.R;
import kr.susemi99.jungnangwomen.application.MyApp;
import kr.susemi99.jungnangwomen.models.WomenResourcesClassParentItem;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Path;

public final class WomenService
{
  public static ListAPI api()
  {
    String host = "http://openapi.seoul.go.kr:8088/" + MyApp.context().getString(R.string.my_key) + "/json/SeoulJungNangWomenResourcesClass/";
    Retrofit retrofit = new Retrofit.Builder().baseUrl(host).addConverterFactory(GsonConverterFactory.create()).build();
    return retrofit.create(ListAPI.class);
  }

  public interface ListAPI
  {
    @GET("{start_index}/{end_index}")
    Call<WomenResourcesClassParentItem> list(@Path("start_index") int startIndex, @Path("end_index") int endIndex);
  }
}
