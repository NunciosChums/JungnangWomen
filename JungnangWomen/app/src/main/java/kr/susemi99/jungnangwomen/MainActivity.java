package kr.susemi99.jungnangwomen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import kr.susemi99.jungnangwomen.adapters.ClassListAdapter;
import kr.susemi99.jungnangwomen.listeners.EndlessScrollListener;
import kr.susemi99.jungnangwomen.models.RowItem;
import kr.susemi99.jungnangwomen.models.WomenResourcesClassParentItem;
import kr.susemi99.jungnangwomen.networks.WomenService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity
{
  private static final int OFFSET = 20;

  private ClassListAdapter adapter;
  private TextView emptyTextView;
  private SwipeRefreshLayout refreshLayout;

  private int startIndex, endIndex;

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    refreshLayout = (SwipeRefreshLayout) findViewById(R.id.layout_refresh);
    refreshLayout.setOnRefreshListener(() -> {
      resetIndex();
      load();
    });

    adapter = new ClassListAdapter();
    emptyTextView = (TextView) findViewById(android.R.id.empty);

    ListView listView = (ListView) findViewById(android.R.id.list);
    listView.setAdapter(adapter);
    listView.setOnItemClickListener(itemClickListener);
    listView.setOnScrollListener(endlessScrollListener);

    resetIndex();
    load();
  }

  private void resetIndex()
  {
    startIndex = 1;
    endIndex = OFFSET;
    adapter.clear();
  }

  private void load()
  {
    emptyTextView.setVisibility(View.GONE);

    WomenService.api().list(startIndex, endIndex).enqueue(new Callback<WomenResourcesClassParentItem>()
    {
      @Override
      public void onResponse(Call<WomenResourcesClassParentItem> call, Response<WomenResourcesClassParentItem> response)
      {
        refreshLayout.setRefreshing(false);

        if (response == null || !response.isSuccessful() || response.body() == null)
        {
          displayErrorString(getString(R.string.no_result));
          return;
        }

        WomenResourcesClassParentItem item = response.body();

        if (item.classItem == null)
        {
          return;
        }

        if (item.classItem.rows.length == 0)
        {
          displayErrorString(getString(R.string.no_result));
          return;
        }

        for (RowItem row : item.classItem.rows)
        {
          adapter.add(row);
        }
        adapter.notifyDataSetChanged();
      }

      @Override
      public void onFailure(Call<WomenResourcesClassParentItem> call, Throwable t)
      {
        refreshLayout.setRefreshing(false);
        displayErrorString(t.getLocalizedMessage());
      }
    });
  }

  private void displayErrorString(String string)
  {
    emptyTextView.setText(string);
    emptyTextView.setVisibility(View.VISIBLE);
  }

  private AdapterView.OnItemClickListener itemClickListener = (parent, view, position, id) -> {
    RowItem item = (RowItem) parent.getItemAtPosition(position);
    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.url));
    startActivity(intent);
  };

  private EndlessScrollListener endlessScrollListener = new EndlessScrollListener()
  {
    @Override
    public boolean onLoadMore(int page, int totalItemsCount)
    {
      startIndex = endIndex + 1;
      endIndex += OFFSET;
      load();
      return true;
    }
  };
}
