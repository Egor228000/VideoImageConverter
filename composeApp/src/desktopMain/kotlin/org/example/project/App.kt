package org.example.project

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.skiko.Cursor
import videoimageconverter.composeapp.generated.resources.Res
import videoimageconverter.composeapp.generated.resources.downarrow
import videoimageconverter.composeapp.generated.resources.folder
import videoimageconverter.composeapp.generated.resources.uparrow
import java.io.File


@Composable
fun App(appViewModel: AppViewModel, dialogSettings: FileKitDialogSettings) {
    val listImage by appViewModel.listImage.collectAsStateWithLifecycle()
    val listVideo by appViewModel.listVideo.collectAsStateWithLifecycle()
    val translationProgress by appViewModel.translationProgress.collectAsStateWithLifecycle()
    val isStatus by appViewModel.isStatus.collectAsStateWithLifecycle()

    val listFormatsImage = remember {
        mutableStateListOf(
            "psd", "bmp", "tif", "tiff", "gif", "png",
            "jpg", "jpeg", "webp"
        )
    }
    val listFormatsVideo = remember {
        mutableStateListOf(
            "mp4", "mkv", "avi", "mov", "webm", "gif"
        )
    }

    var openListImage = remember { mutableStateOf(false) }
    var openListVideo = remember { mutableStateOf(false) }
    val selectedFormatImage = remember { mutableStateOf("jpeg") }
    val selectedFormatVideo = remember { mutableStateOf("mp4") }
    var selectedFolder by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val stateVertical = rememberScrollState(0)



    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .padding(16.dp)
    ) {
        Text(
            "\uD83D\uDCC1 Исходные файлы",
            fontSize = 18.sp,
            color = Color.White
        )
        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .weight(0.25f),
            colors = CardDefaults.cardColors(Color.Transparent),
            border = BorderStroke(1.dp, Color.White)

        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxSize()
            ) {
                FileDropZone(
                    onFileDropped = { files -> appViewModel.addImage(files) },
                    selectedFile = listImage,
                    isImage = true,
                    textTypeFile = "Перетащите изображение",
                    weight = 0.25f
                )
            }
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .weight(0.30f)
                    .fillMaxSize()
            ) {
                FileDropZone(
                    onFileDropped = { files -> appViewModel.addVideo(files) },
                    selectedFile = listVideo,
                    isImage = false,
                    textTypeFile = "Перетащите видео",
                    weight = 0.25f
                )
            }


        }
        Text(
            "\uD83D\uDEE0\uFE0F Настройки конвертации",
            fontSize = 18.sp,
            color = Color.White
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.28f),
            colors = CardDefaults.cardColors(Color.Transparent),
            border = BorderStroke(1.dp, Color.White)
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {

                Box(modifier = Modifier.weight(1f)) {
                    FormatsList(
                        expanded = openListImage,
                        listFormats = listFormatsImage,
                        selectedFormat = selectedFormatImage,
                        text = "\uD83C\uDF04 Формат изображения"
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    FormatsList(
                        expanded = openListVideo,
                        listFormats = listFormatsVideo,
                        selectedFormat = selectedFormatVideo,
                        text = "\uD83C\uDFAC Формат видео"
                    )
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(0.5f)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                ) {
                    Text(
                        "\uD83D\uDDC2\uFE0F Папка для сохранения",
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                OutlinedTextField(
                    value = selectedFolder,
                    onValueChange = {
                    },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    val folder = appViewModel.openFolderPicker(dialogSettings)
                                    if (folder != null) {
                                        selectedFolder = folder.absolutePath
                                    }
                                }


                            },
                            modifier = Modifier
                                .pointerHoverIcon(
                                    PointerIcon(
                                        Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
                                    )
                                )
                        ) {
                            Icon(
                                painterResource(Res.drawable.folder),
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)

                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.DarkGray,
                        unfocusedContainerColor = Color.DarkGray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White

                    ),
                    shape = RoundedCornerShape(10.dp)
                )

            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(0.5f)
                ) {
                    CustomButton(
                        "Очистить",
                        color = Color(147, 34, 5),
                        onClick = {
                            appViewModel.clearList()
                        }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(0.5f)
                ) {
                    CustomButton(
                        "Конвертировать",
                        color = Color(0, 147, 0),
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                if (selectedFolder.isBlank()) return@launch
                                println("→ Saving into directory: $selectedFolder")

                                val outDir = File(selectedFolder).apply {
                                    if (!exists()) mkdirs()
                                }

                                if (listImage.isNotEmpty()) {
                                    val imgOutputs = listImage.map { inputFile ->
                                        File(outDir, "${inputFile.nameWithoutExtension}.${selectedFormatImage.value}")
                                    }
                                    imgOutputs.forEach { println("  → Will write image: ${it.absolutePath}") }
                                    appViewModel.convertImagesBatch(listImage, imgOutputs)
                                }

                                if (listVideo.isNotEmpty()) {
                                    val vidOutputs = listVideo.map { inputFile ->
                                        File(outDir, "${inputFile.nameWithoutExtension}.${selectedFormatVideo.value}")
                                    }
                                    vidOutputs.forEach { println("  → Will write video: ${it.absolutePath}") }
                                    appViewModel.convertVideosBatch(listVideo, vidOutputs)
                                }
                                println("Selected folder: $selectedFolder")
                                println("Saving into: ${File(selectedFolder).absolutePath}")
                            }

                        }
                    )
                }
            }
        }
        Text(
            "⏳ Процесс",
            fontSize = 18.sp,
            color = Color.White
        )
        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .weight(0.15f),
            colors = CardDefaults.cardColors(Color.Transparent),
            border = BorderStroke(1.dp, Color.White)


        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize()
            ) {
            Column(
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(top = 30.dp)
                    .verticalScroll(stateVertical)
                    .fillMaxWidth()


            ) {
                isStatus.forEach { status ->
                    Text(
                        status,
                        fontSize = 16.sp,
                        color = Color.White,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }
            }
                VerticalScrollbar(
                    modifier = Modifier.align(Alignment.BottomEnd)
                        .fillMaxHeight(),
                    adapter = rememberScrollbarAdapter(stateVertical),
                    style = ScrollbarStyle(
                        minimalHeight = 20.dp,
                        thickness = 10.dp,
                        shape = RoundedCornerShape(10.dp),
                        hoverDurationMillis = 10,
                        unhoverColor = Color.LightGray,
                        hoverColor = Color.Black
                    )
                )
                Box(
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth(1f)
                ) {
                    Slider(
                        value = translationProgress,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = SliderDefaults.colors(
                            disabledThumbColor = Color(0, 147, 0),
                            disabledActiveTrackColor = Color.DarkGray,
                            disabledInactiveTrackColor = Color(190, 190, 190)
                        ),
                        enabled = false
                    )
                }


        }



        }
    }
}

@Composable
fun CustomButton(text: String, color: Color, onClick: () -> Unit) {

    Button(
        onClick = { onClick() },
        modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(color),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            text,
            fontSize = 16.sp,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatsList(
    expanded: MutableState<Boolean>,
    listFormats: List<String>,
    selectedFormat: MutableState<String>,
    text: String,
) {
    Column(

    ) {
        Text(
            text,
            fontSize = 16.sp,
            color = Color.White
        )
        Spacer(modifier = Modifier.padding(top = 8.dp))
        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = !expanded.value },
            modifier = Modifier

                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = selectedFormat.value,
                onValueChange = {},
                readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = { expanded.value }
                    ) {
                        if (expanded.value) {
                            Icon(
                                painterResource(Res.drawable.downarrow),
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Icon(
                                painterResource(Res.drawable.uparrow),
                                null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )

                        }
                    }
                },
                modifier = Modifier
                    .menuAnchor(
                        ExposedDropdownMenuAnchorType.PrimaryNotEditable,
                        true
                    )

                    .fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.DarkGray,
                    unfocusedContainerColor = Color.DarkGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White

                ),
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                listFormats.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            selectedFormat.value = option
                            expanded.value = false
                        },
                    )
                }
            }
        }
    }
}
