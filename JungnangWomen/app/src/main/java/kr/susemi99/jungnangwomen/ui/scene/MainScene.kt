package kr.susemi99.jungnangwomen.ui.scene

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import kr.susemi99.jungnangwomen.R
import kr.susemi99.jungnangwomen.api.ApiException
import kr.susemi99.jungnangwomen.model.RowItem
import java.io.IOException

@Composable
fun MainScene() {
  val viewModel = hiltViewModel<MainSceneViewModel>()
  MainContent(listItems = viewModel.list.collectAsLazyPagingItems())
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContent(listItems: LazyPagingItems<RowItem>) {
  val scrollState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  // 상태바·내비게이션바뿐 아니라 디스플레이 컷아웃(가로 모드 측면 노치)까지 피하도록 safeDrawing 사용
  Scaffold(
    contentWindowInsets = WindowInsets.safeDrawing,
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        modifier = Modifier.shadow(4.dp),
        windowInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        actions = {
          IconButton(onClick = {
            listItems.refresh()
            coroutineScope.launch {
              scrollState.scrollToItem(0)
            }
          }) {
            Icon(imageVector = Icons.Filled.Refresh, contentDescription = "refresh")
          }
        })
    }) { padding ->
    val refreshState = listItems.loadState.refresh
    Box(modifier = Modifier.fillMaxSize()) {
      if (refreshState is LoadState.Error) {
        ErrorView(
          message = errorMessage(refreshState.error),
          onRetry = { listItems.retry() },
          modifier = Modifier.padding(padding)
        )
      } else if (listItems.itemCount == 0 && refreshState is LoadState.NotLoading && listItems.loadState.prepend.endOfPaginationReached) {
        NoResultView(modifier = Modifier.padding(padding))
      } else {
        // contentPadding으로 넘겨 리스트가 내비게이션바 뒤까지 스크롤되되 마지막 항목은 가려지지 않게 한다.
        LazyColumn(state = scrollState, contentPadding = padding) {
          items(
            count = listItems.itemCount,
            key = listItems.itemKey { it.key },
          ) { index ->
            val it = listItems[index]
            val titleColor = MaterialTheme.colorScheme.primary
            Column(
              modifier = Modifier
              .fillMaxWidth()
              .clickable { it?.url?.let(context::openLink) }
              .padding(10.dp)) {
              Text(
                buildAnnotatedString {
                  withStyle(style = SpanStyle(color = titleColor, fontSize = 16.sp)) { append("${it?.difficulty} ") }
                  withStyle(style = SpanStyle(color = titleColor, fontSize = 20.sp)) { append("${it?.className}") }
                }
              )
              RowView("신청기간", "${it?.receivePeriod}")
              RowView("교육기간", "${it?.educatePeriod} ${it?.educateDays}")
              RowView("잔여", "${it?.remainNumber}")
              RowView("수강료", "${it?.fee}")
              RowView("접수", "${it?.howToRegister}")
            }
            HorizontalDivider()
          }
          val appendState = listItems.loadState.append
          if (appendState is LoadState.Error) {
            item(key = "append_error") {
              AppendErrorView(message = errorMessage(appendState.error), onRetry = { listItems.retry() })
            }
          }
        }
      }
      if (refreshState is LoadState.Loading) {
        LoadingView(modifier = Modifier.padding(padding))
      }
    }
  }
}

/** URL이 비었거나 열 수 있는 앱이 없어도 앱이 종료되지 않도록 안내만 표시한다. */
private fun Context.openLink(url: String) {
  try {
    startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
  } catch (_: ActivityNotFoundException) {
    Toast.makeText(this, R.string.link_open_error, Toast.LENGTH_SHORT).show()
  }
}

/** 서버가 보낸 [ApiException]의 메시지만 그대로 보여 주고, 나머지 예외의 원문은 안내 문구로 바꾼다. */
@Composable
private fun errorMessage(error: Throwable): String = when (error) {
  is ApiException -> error.message
  is IOException -> stringResource(id = R.string.network_error)
  else -> null
} ?: stringResource(id = R.string.unknown_error)

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
  val description = stringResource(id = R.string.loading)
  Box(contentAlignment = Alignment.Center, modifier = modifier.fillMaxSize()) {
    CircularProgressIndicator(modifier = Modifier.semantics { contentDescription = description })
  }
}

@Composable
fun NoResultView(modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.Center,
    modifier = modifier.fillMaxSize()
  ) {
    Text(
      text = stringResource(id = R.string.no_result),
      textAlign = TextAlign.Center,
      fontSize = 20.sp,
      modifier = Modifier.fillMaxWidth()
    )
  }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    verticalArrangement = Arrangement.Center,
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxSize()
      .padding(20.dp)
  ) {
    Text(
      text = stringResource(id = R.string.load_error),
      textAlign = TextAlign.Center,
      fontSize = 20.sp,
      modifier = Modifier.fillMaxWidth()
    )
    Text(
      text = message,
      textAlign = TextAlign.Center,
      fontSize = 14.sp,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 10.dp)
    )
    Button(onClick = onRetry, modifier = Modifier.padding(top = 20.dp)) {
      Text(text = stringResource(id = R.string.retry))
    }
  }
}

/** 다음 페이지를 불러오지 못했을 때 목록 맨 아래에 표시한다. */
@Composable
fun AppendErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = modifier
      .fillMaxWidth()
      .padding(20.dp)
  ) {
    Text(text = message, textAlign = TextAlign.Center, fontSize = 14.sp)
    Button(onClick = onRetry, modifier = Modifier.padding(top = 10.dp)) {
      Text(text = stringResource(id = R.string.retry))
    }
  }
}

@Composable
fun RowView(title: String, value: String) {
  Row(modifier = Modifier.padding(top = 10.dp, start = 0.dp)) {
    RowTitle(text = title)
    RowValue(text = value)
  }
}

@Composable
fun RowTitle(text: String) {
  Text(
    text = "$text: ",
    textAlign = TextAlign.End,
    maxLines = 1,
    fontSize = 16.sp,
    fontWeight = FontWeight.Bold,
    modifier = Modifier.defaultMinSize(70.dp)
  )
}

@Composable
fun RowValue(text: String) {
  Text(
    text = text,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal
  )
}
