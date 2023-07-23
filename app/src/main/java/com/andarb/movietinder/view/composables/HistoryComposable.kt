package com.andarb.movietinder.view.composables

import android.content.res.Resources
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.andarb.movietinder.R
import com.andarb.movietinder.model.Movie
import com.andarb.movietinder.util.ClickType
import com.andarb.movietinder.util.POSTER_URL
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.SortedMap


/**
 * HistoryFragment content composable
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalGlideComposeApi::class)
@Composable
fun HistoryContent(
    movies: SortedMap<LocalDate, List<Movie>>,
    clickListener: (Movie, ClickType) -> Unit,
    resources: Resources
) {
    val df: DateTimeFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)

    LazyColumn(
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        movies.forEach { (date, sortedMovies) ->
            stickyHeader {
                Row(
                    modifier = Modifier
                        .alpha(alpha = 0.97F)
                        .background(Color.White),
                    verticalAlignment = Alignment.CenterVertically,
                )
                {
                    Text(
                        text = df.format(date),
                        modifier = Modifier.padding(start = 16.dp, end = 8.dp)
                    )
                    Divider(modifier = Modifier.padding(end = 16.dp))
                }
            }

            items(sortedMovies, key = { it.id }) { movie ->
                ConstraintLayout {
                    val (posterImage, titleText, dateText, ratingText, overviewText, likeIcon, deleteIcon) = createRefs()

                    GlideImage(
                        model = POSTER_URL + movie.posterUrl,
                        contentDescription = resources.getString(R.string.description_poster),
                        modifier = Modifier
                            .size(128.dp)
                            .constrainAs(posterImage) {
                                start.linkTo(parent.start)
                                top.linkTo(parent.top)
                            }
                    )

                    Text(text = movie.title,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .constrainAs(titleText) {
                                start.linkTo(posterImage.end)
                                top.linkTo(parent.top)
                            })
                    Text(text = movie.date, modifier = Modifier
                        .constrainAs(dateText) {
                            start.linkTo(titleText.start)
                            top.linkTo(titleText.bottom, margin = 8.dp)
                        })
                    Text(text = movie.rating.toString(), modifier = Modifier
                        .constrainAs(ratingText) {
                            start.linkTo(dateText.end, margin = 8.dp)
                            top.linkTo(dateText.top)
                        })
                    Text(text = movie.overview, maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .wrapContentWidth()
                            .constrainAs(overviewText) {
                                width = Dimension.preferredWrapContent
                                start.linkTo(titleText.start)
                                end.linkTo(parent.end, margin = 8.dp)
                                bottom.linkTo(posterImage.bottom)
                                top.linkTo(dateText.bottom, margin = 8.dp)
                            })

                    Icon(
                        painter = painterResource(R.drawable.baseline_star_outline_24),
                        contentDescription = resources.getString(R.string.description_like),
                        modifier = Modifier
                            .clickable {
                                clickListener(
                                    movie,
                                    ClickType.LIKE
                                )
                            }
                            .constrainAs(likeIcon) {
                                end.linkTo(deleteIcon.start, margin = 16.dp)
                                top.linkTo(deleteIcon.top)
                            }
                    )
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24),
                        contentDescription = resources.getString(R.string.description_delete),
                        modifier = Modifier
                            .clickable {
                                clickListener(
                                    movie,
                                    ClickType.DELETE
                                )
                            }
                            .constrainAs(deleteIcon) {
                                end.linkTo(parent.end, margin = 16.dp)
                                top.linkTo(parent.top)
                                bottom.linkTo(overviewText.top)
                            }
                    )
                }
            }
        }
    }
}