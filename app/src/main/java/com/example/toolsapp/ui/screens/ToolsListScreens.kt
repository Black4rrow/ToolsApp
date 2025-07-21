package com.example.toolsapp.ui.screens

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.ViewModelProvider
import com.example.toolsapp.model.classes.Theme
import com.example.toolsapp.model.classes.ToolsDestination
import kotlin.random.Random
import com.example.toolsapp.R
import com.example.toolsapp.model.Utils
import com.example.toolsapp.model.advancedShadow
import com.example.toolsapp.model.classes.AppSettingsManager
import com.example.toolsapp.viewModels.PreferencesViewModel
import com.example.toolsapp.viewModels.UserViewModel

@Composable
fun ToolsScreen(
    tools: List<ToolsDestination>,
    onToolClick: (ToolsDestination) -> Unit
) {
    val theme: Theme = Theme.getTheme(AppSettingsManager.settings.selectedTheme)
    var selectedIndex by remember { mutableStateOf(0) }

    val context= LocalActivity.current as ComponentActivity
    val preferencesViewModel = ViewModelProvider(context)[PreferencesViewModel::class.java]
    val userViewModel = ViewModelProvider(context)[UserViewModel::class.java]

    val favoriteTools by preferencesViewModel.favoriteTools.collectAsState()

    LaunchedEffect(Unit) {
        preferencesViewModel.observeToolsFavorites(userViewModel.getCurrentUserId())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
        ){
            val options = listOf("LIST", "MAP")

            Column(
                Modifier
                    .padding(top = 16.dp)
                    .clip(shape = ShapeDefaults.Large)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = .9f))
                    .zIndex(1f)
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ){
                Text(stringResource(R.string.choose_mode))

                SingleChoiceSegmentedButtonRow(

                ) {
                    options.forEachIndexed{index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            onClick = { selectedIndex = index },
                            selected = index == selectedIndex,
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically){
                                    when(label){
                                        "MAP" -> {
                                            if (index != selectedIndex) {
                                                Image(painter = painterResource(R.drawable.map), contentDescription = "Map icon")
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                            Text(stringResource(R.string.map_label))
                                        }

                                        "LIST" -> {
                                            if (index != selectedIndex) {
                                                Image(painter = painterResource(R.drawable.view_list), contentDescription = "List icon")
                                                Spacer(modifier = Modifier.width(16.dp))
                                            }
                                            Text(stringResource(R.string.list_label))
                                        }
                                    }
                                }
                            },
                            colors = SegmentedButtonDefaults.colors(
                                inactiveContainerColor = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }
            }

            if(selectedIndex == 1){
                ToolsMapScreen(
                    tools = tools,
                    onToolClick = onToolClick,
                    theme = theme
                )
            }else{
                ToolsListScreen(
                    tools = tools,
                    onToolClick = onToolClick,
                    theme = theme,
                    favoriteTools = favoriteTools,
                    onToggleFavorite = { toolName ->
                        preferencesViewModel.toggleToolFavorite(userViewModel.getCurrentUserId(), toolName)
                    }
                )
            }
        }
    }

}

@Composable
fun ToolsListScreen(
    tools: List<ToolsDestination>,
    onToolClick: (ToolsDestination) -> Unit,
    theme: Theme,
    favoriteTools: List<String>,
    onToggleFavorite: (String) -> Unit
){
    LazyVerticalGrid(
        modifier = Modifier
            .padding(top = 128.dp)
            .fillMaxSize(),
        columns = GridCells.Adaptive(minSize = 128.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(16.dp)
    ){
        val sortedTools = tools.sortedWith(compareBy ({ it.title !in favoriteTools }, {it.title}))

        items(sortedTools) { tool ->
            ToolCard(
                tool,
                onClick = onToolClick,
                isFavorite = tool.title in favoriteTools,
                onToggleFavorite = onToggleFavorite
            )
        }
    }
}

@Composable
fun ToolsMapScreen(
    tools: List<ToolsDestination>,
    onToolClick: (ToolsDestination) -> Unit,
    theme: Theme
){
    val zoomIn = 2
    val zoomOut = 2
    val xSizePixels = 10000
    val ySizePixel = 10000

    val xSizeForTools = 300
    val ySizeForTools = 300

    val screenWidthPx = LocalWindowInfo.current.containerSize.width.dp.value
    val screenHeightPx = LocalWindowInfo.current.containerSize.height.dp.value

    val offset = remember { mutableStateOf(Offset.Zero) }
    var scale by remember { mutableFloatStateOf(1f) }

    val toolPositions = remember(tools) {
        tools.associateWith {
            Offset(
                x = Random.nextInt(xSizeForTools).toFloat(),
                y = Random.nextInt(ySizeForTools).toFloat()
            )

        }
    }

    val imageBitmap = ImageBitmap.imageResource(theme.backgroundTexture)
    val backgroundBrush = remember(imageBitmap) {
        ShaderBrush(ImageShader(imageBitmap, TileMode.Repeated, TileMode.Repeated))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .requiredSize(xSizePixels.dp, ySizePixel.dp)
                .pointerInput(Unit){
                    detectTransformGestures{ centroid, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn((1f/zoomOut), zoomIn.toFloat())
                        scale = newScale

                        offset.value += pan
                    }
                }
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    translationX = offset.value.x
                    translationY = offset.value.y
                }
                .drawWithCache {
                    onDrawWithContent {
                        drawRect(backgroundBrush)
                        drawContent()
                    }
                }
        ){

            Image(
                painter = painterResource(R.drawable.compass),
                contentDescription = "Compass in the middle",
                modifier = Modifier
                    .align(Alignment.Center)
                    .alpha(.5f),
                colorFilter = ColorFilter.tint(theme.recommendedColor)
            )

            Box(
                modifier = Modifier
                    .requiredSize(xSizeForTools.dp, ySizeForTools.dp)
                    .align(Alignment.Center)
            ) {
                tools.forEach { tool ->
                    val position = toolPositions[tool]
                    if ((tool.requireConnection && Utils.connectedToInternet.value) || !tool.requireConnection ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .offset(
                                    x = (position?.x ?: 0f).dp,
                                    y = (position?.y ?: 0f).dp
                                )
                        ){
                            Image(
                                painter = painterResource(theme.itemsTexture),
                                contentDescription = tool.title,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset(0.dp, (-24).dp)
                                    .clickable { onToolClick(tool) }
                            )

                            Text(
                                text = tool.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = theme.recommendedColor
                                ),
                                textAlign = TextAlign.Center,
                                overflow = TextOverflow.Visible,
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .requiredWidth(IntrinsicSize.Max)
                            )
                        }
                    }

                }
            }

        }

        Button(
            onClick = {
                scale = 1f
                offset.value = Offset.Zero
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .size(64.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.gps_fixed),
                contentDescription = "Go back to middle button",
                modifier = Modifier
                    .requiredSize(32.dp)
            )
        }
    }
}

@Composable
fun ToolCard(
    tool: ToolsDestination,
    onClick: (ToolsDestination) -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: (String) -> Unit
){
    Card(
        modifier = Modifier
            .fillMaxSize()
            .aspectRatio(1f)
            .clickable { onClick(tool) }
            .advancedShadow(color = Color.Black, shadowBlurRadius = 8.dp, cornersRadius = 8.dp, offsetX = 4.dp, offsetY = 4.dp, alpha = .25f)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .align(Alignment.TopEnd),
                horizontalArrangement = Arrangement.End
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = { onToggleFavorite(tool.title) },
                            indication = ripple(bounded = true, radius = 32.dp),
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Star
                        else Icons.Filled.StarBorder,
                        contentDescription = "Is Favorite icon",
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    painter = painterResource(tool.iconId),
                    contentDescription = "${tool.title} icon"
                )
                Text(text = tool.title)
            }
        }
    }
}