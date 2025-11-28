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

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun FileDropZone(
    onFileDropped: (List<File>) -> Unit,
    selectedFile: List<File>?,
    isImage: Boolean,
    textTypeFile: String,
    weight: Float,
) {

    var isHovering by remember { mutableStateOf(false) }
    val stateVertical = rememberScrollState(0)

    val dropTarget = remember {
        object : DragAndDropTarget {
            override fun onStarted(event: DragAndDropEvent) {
                isHovering = true
            }

            override fun onEnded(event: DragAndDropEvent) {
                isHovering = false
            }

            override fun onDrop(event: DragAndDropEvent): Boolean {
                isHovering = false
                val dropped = (event.awtTransferable
                    .getTransferData(DataFlavor.javaFileListFlavor) as? List<*>)
                    ?.filterIsInstance<File>()
                    ?: emptyList()


                val validFiles = dropped.filter { file ->
                    if (isImage) {
                        file.extension.lowercase() in listOf(
                            "psd", "bmp", "tif", "tiff", "gif", "png",
                            "jpg", "jpeg", "webp"
                        )
                    } else {
                        file.extension.lowercase() in listOf(
                            "mp4", "mkv", "avi", "mov", "webm", "gif"
                        )
                    }
                }
                if (validFiles.isNotEmpty()) {
                    onFileDropped(validFiles)
                    return true
                }
                return false
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxWidth(1f)
            .padding(16.dp)

    ) {
        Card(
            modifier = Modifier
                .weight(weight)
                .dragAndDropTarget(
                    shouldStartDragAndDrop = { true },
                    target = dropTarget
                ),
            border = BorderStroke(
                width = if (isHovering) 2.dp else 1.dp,
                color = if (isHovering) Color.Blue else Color.Gray
            ),

            ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isHovering) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primaryContainer)
            ) {

                if (selectedFile.isNullOrEmpty()) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = textTypeFile,
                            textAlign = TextAlign.Center,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (isImage) {
                            Box {
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(stateVertical)
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                ) {
                                    selectedFile.forEach { image ->
                                        CoilImage(
                                            imageModel = { image.absolutePath },
                                            imageOptions = ImageOptions(
                                                contentScale = ContentScale.Crop,
                                                alignment = Alignment.Center
                                            ),
                                            modifier = Modifier
                                                .size(80.dp)
                                        )
                                    }
                                }

                                HorizontalScrollbar(
                                    modifier = Modifier.align(Alignment.BottomCenter)
                                        .fillMaxWidth(),
                                    adapter = rememberScrollbarAdapter(stateVertical),
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


                        } else {
                            LazyVerticalGrid(
                                contentPadding = PaddingValues(16.dp),
                                columns = GridCells.Adaptive(250.dp),
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                items(selectedFile) { name ->
                                    Text(
                                        text = name.name.toString(),
                                        textAlign = TextAlign.Center,
                                        maxLines = 1,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 16.sp
                                    )
                                }
                            }

                        }
                    }
                }
            }
        }
    }
}

