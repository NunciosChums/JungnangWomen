package kr.susemi99.jungnangwomen.ui.scene

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import kotlinx.coroutines.launch
import kr.susemi99.jungnangwomen.R
import kr.susemi99.jungnangwomen.ui.theme.RowTitleColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScene() {
  val viewModel = viewModel<MainSceneViewModel>()
  val listItems = viewModel.list.collectAsLazyPagingItems()
  val scrollState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()
  val context = LocalContext.current

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(text = stringResource(id = R.string.app_name)) },
        modifier = Modifier.shadow(4.dp),
        actions = {
          IconButton(onClick = {
            listItems.refresh()
            coroutineScope.launch {
              scrollState.scrollToItem(0)
            }
          }) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = "refresh")
          }
        })
    }) { padding ->
    if (listItems.itemCount == 0 && listItems.loadState.prepend.endOfPaginationReached) {
      NoResultView()
    } else {
      LazyColumn(state = scrollState, modifier = Modifier.padding(padding)) {
        items(
          count = listItems.itemCount,
          key = listItems.itemKey { it.id },
        ) { index ->
          val it = listItems[index]
          Column(modifier = Modifier
            .fillMaxWidth()
            .clickable { Intent(Intent.ACTION_VIEW, Uri.parse(it?.url)).also { context.startActivity(it) } }
            .padding(10.dp)) {
            Text(
              buildAnnotatedString {
                withStyle(style = SpanStyle(color = RowTitleColor, fontSize = 16.sp)) { append("${it?.difficulty} ") }
                withStyle(style = SpanStyle(color = RowTitleColor, fontSize = 20.sp)) { append("${it?.className}") }
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
      }
    }
  }
}

@Composable
fun NoResultView() {
  Column(
    verticalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize()
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