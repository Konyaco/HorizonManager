package org.wvt.horizonmgr.ui.news

import android.util.Log
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import io.noties.markwon.Markwon
import io.noties.markwon.ext.latex.JLatexMathPlugin
import io.noties.markwon.ext.tables.TablePlugin
import io.noties.markwon.image.AsyncDrawable
import io.noties.markwon.image.coil.CoilImagesPlugin
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin
import io.noties.markwon.syntax.Prism4jSyntaxHighlight
import io.noties.markwon.syntax.Prism4jTheme
import io.noties.markwon.syntax.SyntaxHighlightPlugin
import io.noties.prism4j.GrammarLocator
import io.noties.prism4j.Prism4j
import org.wvt.horizonmgr.ui.components.ErrorPage
import org.wvt.horizonmgr.ui.components.MarkdownContent
import org.wvt.horizonmgr.ui.components.NetworkImage
import org.wvt.horizonmgr.ui.theme.AppBarBackgroundColor

@Composable
fun NewsContent(
    vm: NewsContentViewModel,
    onNavClick: () -> Unit
) {
    val news by vm.content.collectAsState()

    Column {
        TopAppBar(
            modifier = Modifier.zIndex(4.dp.value),
            navigationIcon = {
                IconButton(onClick = onNavClick) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "返回")
                }
            }, title = {
                Text("资讯正文")
            }, backgroundColor = AppBarBackgroundColor
        )
        Crossfade(news) {
            when (it) {
                NewsContentViewModel.Result.Loading -> Box(Modifier.fillMaxSize()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                }
                NewsContentViewModel.Result.NetworkError -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("网络错误，请稍后再试") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                NewsContentViewModel.Result.NewsNotFound -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("该资讯可能已被删除") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                NewsContentViewModel.Result.OtherError -> {
                    ErrorPage(
                        modifier = Modifier.fillMaxSize(),
                        message = { Text("未知错误，请稍后再试") },
                        onRetryClick = { vm.refresh() }
                    )
                }
                is NewsContentViewModel.Result.Succeed -> {
                    val content = it.value
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            Column(Modifier.fillParentMaxWidth()) {
                                if (content.coverImage != null) {
                                    NetworkImage(
                                        modifier = Modifier
                                            .padding(start = 16.dp, top = 16.dp, end = 16.dp)
                                            .fillParentMaxWidth()
                                            .aspectRatio(16f / 9f)
                                            .clip(RoundedCornerShape(4.dp)),
                                        url = content.coverImage,
                                        contentDescription = "封面",
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                // Title
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.padding(
                                            start = 16.dp,
                                            top = 16.dp,
                                            end = 16.dp
                                        ),
                                        text = content.title, style = MaterialTheme.typography.h5
                                    )
                                }

                                // Brief
                                SelectionContainer {
                                    Text(
                                        modifier = Modifier.padding(
                                            start = 24.dp,
                                            end = 24.dp,
                                            top = 16.dp
                                        ),
                                        text = content.brief,
                                        style = MaterialTheme.typography.body2,
                                        color = MaterialTheme.colors.onSurface.copy(0.54f)
                                    )
                                }

                                Divider(
                                    Modifier
                                        .padding(top = 24.dp, start = 24.dp, end = 24.dp)
                                        .fillParentMaxWidth()
                                        .align(Alignment.CenterHorizontally)
                                )

                                // Content
                                /*SelectionContainer {
                                    Text(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp, vertical = 24.dp)
                                            .fillParentMaxWidth(),
                                        text = content.content,
                                        style = MaterialTheme.typography.body1,
                                        color = MaterialTheme.colors.onSurface.copy(0.74f)
                                    )
                                }*/
                                MarkdownContent(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp, vertical = 24.dp),
                                    md = content.content
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Label(
    text: String,
    modifier: Modifier = Modifier
) {
    Column(modifier.wrapContentSize(unbounded = true)) {
        Text(modifier = Modifier.wrapContentWidth(), text = text)
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = MaterialTheme.colors.secondary,
            content = {}
        )
    }
}

