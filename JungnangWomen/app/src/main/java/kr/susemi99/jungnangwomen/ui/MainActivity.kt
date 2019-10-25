package kr.susemi99.jungnangwomen.ui

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.main_activity.*
import kr.susemi99.jungnangwomen.R
import kr.susemi99.jungnangwomen.application.App
import kr.susemi99.jungnangwomen.network.WomenService

class MainActivity : AppCompatActivity() {
  companion object {
    const val OFFSET = 20
  }

  private val classListAdapter = ClassListAdapter()
  private var startIndex = 1
  private var endIndex = OFFSET
  private val disposable = CompositeDisposable()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    classListView.apply {
      adapter = classListAdapter

      val currentLayoutManager = layoutManager as LinearLayoutManager
      val decoration = DividerItemDecoration(App.instance, currentLayoutManager.orientation)
      addItemDecoration(decoration)
      addOnScrollListener(object : EndlessRecyclerViewScrollListener(OFFSET, currentLayoutManager) {
        override fun onLoadMore(page: Int, totalItemsCount: Int, view: RecyclerView) {
          startIndex = endIndex + 1
          endIndex += OFFSET
          loadData()
        }
      })
    }

    loadData()
  }

  override fun onDestroy() {
    super.onDestroy()
    disposable.clear()
  }

  override fun onCreateOptionsMenu(menu: Menu?): Boolean {
    menuInflater.inflate(R.menu.main_menu, menu)
    return super.onCreateOptionsMenu(menu)
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    if (R.id.refreshMenuItem == item.itemId) {
      reset()
    }

    return super.onOptionsItemSelected(item)
  }

  private fun reset() {
    startIndex = 1
    endIndex = OFFSET
    errorLabel.isGone = true
    classListAdapter.clear()
    disposable.clear()

    loadData()
  }

  private fun loadData() {
    WomenService.list(startIndex, endIndex)
      .subscribe(
        {
          if (it.result == null) {
            classListAdapter.addAll(it.classItem.rows)
          }

          if (classListAdapter.itemCount == 0) {
            displayErrorLabel(it.result?.message)
          }
        },
        {
          if (classListAdapter.itemCount == 0) {
            displayErrorLabel(it.localizedMessage)
          }
        }
      ).addTo(disposable)
  }

  private fun displayErrorLabel(string: String? = getString(R.string.no_result)) {
    errorLabel.apply {
      text = string
      isVisible = true
    }
  }
}
