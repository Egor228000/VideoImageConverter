package org.example.project

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import videoimageconverter.composeapp.generated.resources.*
import java.awt.Cursor
import java.io.File

@Composable
fun App(appViewModel: AppViewModel, dialogSettings: FileKitDialogSettings) {
    val listImage by appViewModel.listImage.collectAsStateWithLifecycle()
    val listVideo by appViewModel.listVideo.collectAsStateWithLifecycle()
    val translationProgress by appViewModel.translationProgress.collectAsStateWithLifecycle()
    val isStatus by appViewModel.isStatus.collectAsStateWithLifecycle()

    val listFormatsImage = remember {
        mutableStateListOf(
            "png", "jpg", "webp"
        )
    }
    val listFormatsVideo = remember {
        mutableStateListOf(
            "mp4", "avi", "mov", "gif"
        )
    }

    var openListImage = remember { mutableStateOf(false) }
    var openListVideo = remember { mutableStateOf(false) }
    val selectedFormatImage = remember { mutableStateOf("jpg") }
    val selectedFormatVideo = remember { mutableStateOf("mp4") }
    var selectedFolder by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val stateVertical = rememberScrollState(0)

    val savedFolder by appViewModel.selectedFolder.collectAsState()
    LaunchedEffect(savedFolder) {
        savedFolder?.let {
            selectedFolder = it.name
        }
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .background(MaterialTheme.colorScheme.secondary)
            .padding(16.dp)
    ) {
        RowIconText(
            resourceText = Res.string.source_files,
            colorText = MaterialTheme.colorScheme.primary,
            fontSize = 18.sp,
            colorIcon = MaterialTheme.colorScheme.primary,
            resourceIcon = Res.drawable.folder,
        )

        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .weight(0.25f),
            colors = CardDefaults.cardColors(Color.Transparent),

        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                FileDropZone(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    fileType = FileType.Image,
                    onFileDropped = { files -> appViewModel.addImage(files) },
                    selectedFiles = listImage,
                    textType = Res.string.image_text,
                    textDescription = Res.string.image_type,
                    iconType = Res.drawable.photo_alt,
                    iconColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                )
                FileDropZone(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    fileType = FileType.Video,
                    onFileDropped = { files -> appViewModel.addVideo(files) },
                    selectedFiles = listVideo,
                    textType = Res.string.video_text,
                    textDescription = Res.string.video_type,
                    iconType = Res.drawable.movie,
                    iconColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                )
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.28f),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
            ) {
                RowIconText(
                    resourceText = Res.string.conversion_settings,
                    colorText = MaterialTheme.colorScheme.primary,
                    fontSize = 18.sp,
                    colorIcon = MaterialTheme.colorScheme.secondaryContainer,
                    resourceIcon = Res.drawable.settings,
                )
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
                            resourceText = Res.string.image_format,
                            resourceIcon = Res.drawable.photo_alt,

                            )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(modifier = Modifier.weight(1f)) {
                        FormatsList(
                            expanded = openListVideo,
                            listFormats = listFormatsVideo,
                            selectedFormat = selectedFormatVideo,
                            resourceText = Res.string.Video_format,
                            resourceIcon = Res.drawable.video,
                        )
                    }
                }
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .weight(0.5f)
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {

                    RowIconText(
                        resourceText = Res.string.folder_to_save,
                        colorText = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 14.sp,
                        colorIcon = MaterialTheme.colorScheme.onPrimary,
                        resourceIcon = Res.drawable.folder_open,
                        iconSize = 18.dp
                    )
                    Spacer(Modifier.height(4.dp))


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
                                            appViewModel.saveFolder(
                                                id = "1",
                                                name = selectedFolder
                                            )
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
                                    painterResource(Res.drawable.folder_open),
                                    null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)

                                )
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f),
                            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            unfocusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            focusedTextColor = MaterialTheme.colorScheme.primary,
                            unfocusedTextColor = MaterialTheme.colorScheme.primary

                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

            }
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.weight(0.3f)
                ) {
                    CustomButton(
                        "Очистить",
                        colorButton = MaterialTheme.colorScheme.tertiary,
                        colorTextButton = MaterialTheme.colorScheme.onSecondaryContainer,
                        icon = Res.drawable.`trash (1)`,
                        onClick = {
                            appViewModel.clearList()
                        }
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(
                    modifier = Modifier.weight(0.7f)
                ) {
                    CustomButton(
                        "Конвертировать",
                        colorButton = MaterialTheme.colorScheme.secondaryContainer,
                        colorTextButton = MaterialTheme.colorScheme.primaryContainer,
                        icon = Res.drawable.bolt,
                        onClick = {
                            scope.launch(Dispatchers.IO) {
                                if (selectedFolder.isBlank()) return@launch
                                println("→ Saving into directory: $selectedFolder")

                                val outDir = File(selectedFolder).apply {
                                    if (!exists()) mkdirs()
                                }

                                // Конвертация изображений
                                if (listImage.isNotEmpty()) {
                                    val imgOutputs = listImage.map { inputFile ->
                                        // Добавляем суффикс videoImageConverter в конец имени файла
                                        val newName = "${inputFile.nameWithoutExtension}_videoImageConverter.${selectedFormatImage.value}"
                                        File(outDir, newName)
                                    }
                                    imgOutputs.forEach { println("  → Will write image: ${it.absolutePath}") }
                                    appViewModel.convertImagesBatch(listImage, imgOutputs)
                                }

                                // Конвертация видео
                                if (listVideo.isNotEmpty()) {
                                    val vidOutputs = listVideo.map { inputFile ->
                                        // Добавляем суффикс videoImageConverter в конец имени файла
                                        val newName = "${inputFile.nameWithoutExtension}_videoImageConverter.${selectedFormatVideo.value}"
                                        File(outDir, newName)
                                    }
                                    vidOutputs.forEach { println("  → Will write video: ${it.absolutePath}") }
                                    appViewModel.convertVideosBatch(listVideo, vidOutputs)
                                }

                                println("Selected folder: $selectedFolder")
                                println("Saving into: ${File(selectedFolder).absolutePath}")
                                }

                        },

                    )
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth(1f)
                .weight(0.15f),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.primaryContainer),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.3f)),


            ) {

            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(resource = Res.string.process),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.primary
                )


                isStatus.forEach { status ->
                    Text(
                        status,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .padding(top = 16.dp)
                    )
                }
            }

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
                        disabledThumbColor = Color.Transparent,
                        disabledActiveTrackColor = Color.DarkGray,
                        disabledInactiveTrackColor = Color(190, 190, 190)
                    ),
                    enabled = false
                )
            }


        }
    }
}

@Composable
fun CustomButton(
    text: String,
    colorButton: Color,
    colorTextButton: Color,
    icon: DrawableResource,
    onClick: () -> Unit
) {

    Button(
        onClick = { onClick() },
        modifier = Modifier.fillMaxHeight(0.3f).fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(colorButton),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, colorTextButton)
        ) {
        Row(
            modifier = Modifier.align(Alignment.CenterVertically),
        ) {
            Icon(
                painterResource(
                    resource = icon
                ),
                tint = colorTextButton,
                modifier = Modifier.padding(top = 3.dp).size(20.dp),
                contentDescription = ""
            )

            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text,
                fontSize = 16.sp,
                color = colorTextButton,
                fontWeight = FontWeight.Bold
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormatsList(
    expanded: MutableState<Boolean>,
    listFormats: List<String>,
    selectedFormat: MutableState<String>,
    resourceText: StringResource,
    resourceIcon: DrawableResource
) {
    Column(

    ) {
        RowIconText(
            resourceText = resourceText,
            colorText = MaterialTheme.colorScheme.onPrimary,
            fontSize = 14.sp,
            colorIcon = MaterialTheme.colorScheme.onPrimary,
            resourceIcon = resourceIcon,
            iconSize = 18.dp
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
                        Icon(
                            painterResource(Res.drawable.downarrow),
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.scale(scaleX = 1f, scaleY = if (expanded.value) -1f else 1f).size(14.dp)
                        )

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
                    focusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.tertiary,
                    focusedTextColor = MaterialTheme.colorScheme.primary,
                    unfocusedTextColor = MaterialTheme.colorScheme.primary

                ),
                shape = RoundedCornerShape(10.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                listFormats.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(option, color = MaterialTheme.colorScheme.tertiary)
                        },
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


@Composable
fun RowIconText(
    resourceText: StringResource,
    colorText: Color,
    fontSize: TextUnit,
    colorIcon: Color,
    resourceIcon: DrawableResource,
    iconSize: Dp = 25.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painterResource(
                resource = resourceIcon
            ),
            tint = colorIcon,
            modifier = Modifier.size(iconSize),
            contentDescription = ""
        )
        Text(
            text = stringResource(resource = resourceText),
            fontSize = fontSize,
            color = colorText
        )
    }

}