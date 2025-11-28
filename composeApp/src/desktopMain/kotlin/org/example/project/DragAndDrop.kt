package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.awtTransferable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.skydoves.landscapist.ImageOptions
import com.skydoves.landscapist.coil3.CoilImage
import java.awt.datatransfer.DataFlavor
import java.io.File

sealed interface FileType {
    val extensions: List<String>
    val promptText: String

    data object Image : FileType {
        override val extensions = listOf("psd", "bmp", "tif", "tiff", "gif", "png", "jpg", "jpeg", "webp")
        override val promptText = "Перетащите сюда изображения"
    }

    data object Video : FileType {
        override val extensions = listOf("mp4", "mkv", "avi", "mov", "webm", "gif")
        override val promptText = "Перетащите сюда видео"
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileDropZone(
    modifier: Modifier = Modifier,
    fileType: FileType,
    onFileDropped: (List<File>) -> Unit,
    selectedFiles: List<File>?,
) {
    var isHovering by remember { mutableStateOf(false) }

    val dropTarget = remember(fileType, onFileDropped) {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isHovering = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isHovering = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isHovering = false
                val droppedFiles = (event.awtTransferable
                    .getTransferData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.filterIsInstance<File>()
                    ?: return false

                val validFiles = droppedFiles.filter { file ->
                    file.extension.lowercase() in fileType.extensions
                }

                if (validFiles.isNotEmpty()) {
                    onFileDropped(validFiles)
                    return true
                }
                return false
            }
        }
    }

    Card(
        modifier = modifier
            .dragAndDropTarget(
                shouldStartDragAndDrop = { true },
                target = dropTarget
            ),
        border = BorderStroke(
            width = if (isHovering) 2.dp else 1.dp,
            color = if (isHovering) Color.Blue else Color.Gray
        ),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isHovering) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (selectedFiles.isNullOrEmpty()) {
                Text(
                    text = fileType.promptText,
                    textAlign = TextAlign.Center,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                when (fileType) {
                    is FileType.Image -> ImagePreview(images = selectedFiles)
                    is FileType.Video -> VideoList(videos = selectedFiles)
                }
            }
        }
    }
}

@Composable
private fun ImagePreview(images: List<File>) {
    val scrollState = rememberScrollState()
    Box {
        Row(
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            images.forEach { image ->
                CoilImage(
                    imageModel = { image.absolutePath },
                    imageOptions = ImageOptions(
                        contentScale = ContentScale.Crop,
                        alignment = Alignment.Center
                    ),
                    modifier = Modifier.size(80.dp)
                )
            }
        }

        HorizontalScrollbar(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            adapter = rememberScrollbarAdapter(scrollState),
            style = ScrollbarStyle(
                minimalHeight = 20.dp,
                thickness = 10.dp,
                shape = RoundedCornerShape(10.dp),
                hoverDurationMillis = 10,
                unhoverColor = MaterialTheme.colorScheme.onPrimary,
                hoverColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}

@Composable
private fun VideoList(videos: List<File>) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(250.dp),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(videos) { video ->
            Text(
                text = video.name,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 16.sp,
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
