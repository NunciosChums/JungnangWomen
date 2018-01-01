package kr.susemi99.jungnangwomen;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import java.util.List;

import kr.susemi99.jungnangwomen.adapters.ClassListAdapter;
import kr.susemi99.jungnangwomen.listeners.EndlessRecyclerViewScrollListener;
import kr.susemi99.jungnangwomen.models.RowItem;
import kr.susemi99.jungnangwomen.models.WomenResourcesClassParentItem;
import kr.susemi99.jungnangwomen.networks.WomenService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {
  private static final int OFFSET = 20;

  private ClassListAdapter adapter;
  private TextView emptyTextView;
  private SwipeRefreshLayout refreshLayout;

  private int startIndex, endIndex;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    refreshLayout = findViewById(R.id.layout_refresh);
    refreshLayout.setOnRefreshListener(() -> {
      resetIndex();
      load();
    });

    adapter = new ClassListAdapter(itemClickListener);
    emptyTextView = findViewById(android.R.id.empty);

    RecyclerView listView = findViewById(R.id.list);
    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
    listView.setLayoutManager(linearLayoutManager);
    listView.setAdapter(adapter);
    listView.addOnScrollListener(new EndlessRecyclerViewScrollListener(linearLayoutManager) {
      @Override
      public void onLoadMore(int page, int totalItemsCount) {
        startIndex = endIndex + 1;
        endIndex += OFFSET;
        load();
      }
    });

    resetIndex();
    load();
  }

  private void resetIndex() {
    startIndex = 1;
    endIndex = OFFSET;
    adapter.clear();
  }

  private void load() {
    emptyTextView.setVisibility(View.GONE);

    WomenService.api().list(startIndex, endIndex).enqueue(new Callback<WomenResourcesClassParentItem>() {
      @Override
      public void onResponse(Call<WomenResourcesClassParentItem> call, Response<WomenResourcesClassParentItem> response) {
        refreshLayout.setRefreshing(false);

        if (response == null || !response.isSuccessful() || response.body() == null) {
          displayErrorString(getString(R.string.no_result));
          return;
        }

        WomenResourcesClassParentItem item = response.body();
        if (item == null || item.classItem == null || item.classItem.rows.length == 0) {
          displayErrorString(getString(R.string.no_result));
          return;
        }

        for (RowItem row : item.classItem.rows) {
          adapter.add(row);
        }
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onFailure(Call<WomenResourcesClassParentItem> call, Throwable t) {
        refreshLayout.setRefreshing(false);
        displayErrorString(t.getLocalizedMessage());
      }
    });
  }

  private void displayErrorString(String string) {
    emptyTextView.setText(string);
    emptyTextView.setVisibility(View.VISIBLE);
  }

  /*******************
   * listener
   *******************/
  private View.OnClickListener itemClickListener = view -> {
    String url = (String) view.getTag();
    String PACKAGE_NAME = "com.android.chrome";

    CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder().setShowTitle(true).build();
    customTabsIntent.intent.setData(Uri.parse(url));

    List<ResolveInfo> resolveInfoList = getPackageManager().queryIntentActivities(customTabsIntent.intent, PackageManager.MATCH_DEFAULT_ONLY);

    for (ResolveInfo resolveInfo : resolveInfoList) {
      String packageName = resolveInfo.activityInfo.packageName;
      if (PACKAGE_NAME.equals(packageName)) { customTabsIntent.intent.setPackage(PACKAGE_NAME); }
    }

    customTabsIntent.launchUrl(getApplicationContext(), Uri.parse(url));
  };
}
