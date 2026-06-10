package org.example.project

import androidx.lifecycle.ViewModel
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openDirectoryPicker
import io.github.vinceglb.filekit.path
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.Database.Companion.connect
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.deleteAll
import org.jetbrains.exposed.v1.jdbc.insert
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO


object Folder : Table() {
    val id = varchar("id", 10)
    val name = varchar("name", length = 50)
}

class AppViewModel() : ViewModel() {

    private val _selectedFolder = MutableStateFlow<FolderData?>(null)
    val selectedFolder: StateFlow<FolderData?> = _selectedFolder.asStateFlow()

    data class FolderData(val id: String, val name: String)

    init {
        connect("jdbc:sqlite:myapp.db", driver = "org.sqlite.JDBC")

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Folder)
        }

        loadLastFolder()
    }

    fun saveFolder(id: String, name: String) {
        transaction {
            Folder.deleteAll()
            Folder.insert {
                it[Folder.id] = id
                it[Folder.name] = name
            }
        }
        loadLastFolder()
    }

    private fun loadLastFolder() {
        val folder = transaction {
            Folder.selectAll().firstOrNull()?.let {
                FolderData(
                    id = it[Folder.id],
                    name = it[Folder.name]
                )
            }
        }
        _selectedFolder.value = folder
    }



    private val _listImage = MutableStateFlow<List<File>>(emptyList())
    val listImage: StateFlow<List<File>> = _listImage.asStateFlow()
    fun addImage(list: List<File>) {
        _listImage.value += list
    }

    private val _listVideo = MutableStateFlow<List<File>>(emptyList())
    val listVideo: StateFlow<List<File>> = _listVideo.asStateFlow()
    fun addVideo(list: List<File>) {
        _listVideo.value += list
    }

    private val _isStatus = MutableStateFlow<List<String>>(emptyList())
    val isStatus: StateFlow<List<String>> = _isStatus.asStateFlow()
    private val _translationProgress = MutableStateFlow(0f)
    val translationProgress: StateFlow<Float> = _translationProgress


    fun clearList() {
        _listImage.value = emptyList()
        _listVideo.value = emptyList()
        _translationProgress.value = 0f
    }


    private suspend fun smoothProgressUpdate(target: Float) {
        val current = _translationProgress.value
        val steps = 10
        val stepSize = (target - current) / steps
        repeat(steps) {
            _translationProgress.value += stepSize
            delay(20)
        }
        _translationProgress.value = target
    }

    suspend fun openFolderPicker(dialogSettings: FileKitDialogSettings): File? {
        val home = System.getProperty("user.home")
        val downloads = Paths.get(home, "Downloads").toFile().apply {
            if (!exists()) mkdirs()
        }

        val picked = FileKit.openDirectoryPicker(
            directory = PlatformFile(downloads.absolutePath),
            dialogSettings = dialogSettings
        )


        return picked?.path?.let { File(it) }
    }


    fun convertImageSimple(inputFile: File, outputFile: File) {
        try {
            ImageIO.scanForPlugins()

            val outputExt = outputFile.extension.lowercase()

            val image = ImageIO.read(inputFile)
            if (image == null) {
                println("❌ ERROR: File is not a readable image: ${inputFile.name}")
                return
            }

            println("✅ Image successfully read: ${inputFile.name}")

            println(
                "Доступные форматы для записи: ${
                    ImageIO.getWriterFormatNames().distinct().sorted()
                }"
            )

            when (outputExt) {
                "jpg" -> {
                    ImageIO.write(image, "JPEG", outputFile)
                }

                "png" -> {
                    ImageIO.write(image, "PNG", outputFile)
                }

                "webp" -> {
                    val writers = ImageIO.getImageWritersByMIMEType("image/webp")

                    if (!writers.hasNext()) {
                        println("❌ WebP writer not found")
                        return
                    }

                    val writer = writers.next()

                    ImageIO.createImageOutputStream(outputFile).use { ios ->
                        writer.output = ios
                        writer.write(image)
                        writer.dispose()
                    }
                }

                else -> {
                    println("❌ Unsupported output format: $outputExt")
                    return
                }
            }

            println("✅ Image saved: ${outputFile.absolutePath}")

        } catch (e: Exception) {
            println("❌ Exception: ${e.message}")
            e.printStackTrace()
        }
    }

    fun convertImagesBatch(inputs: List<File>, outputs: List<File>) = runBlocking {
        require(inputs.size == outputs.size) { "Input and output lists must match." }
        val total = inputs.size
        _translationProgress.value = 0f
        _isStatus.value = List(total) { i -> "\uD83D\uDD01 Конвертация: ${inputs[i].name}" }

        inputs.indices.map { i ->
            async(Dispatchers.IO) {
                val inF = inputs[i]
                val outF = outputs[i]
                val status = try {
                    convertImageSimple(inF, outF)
                    "✅ Успех: ${inF.name}"
                } catch (e: Exception) {
                    "❌ Ошибка: ${inF.name} - ${e.message}"
                }

                val updatedStatus = _isStatus.value.toMutableList().apply {
                    this[i] = status
                }
                _isStatus.value = updatedStatus

                val progress = updatedStatus.count { it.startsWith("✅") }.toFloat() / total
                launch { smoothProgressUpdate(progress) }
            }
        }.awaitAll()
    }


    fun convertVideoSimple(inputFile: File, outputFile: File) = runBlocking {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR)

        val ext = outputFile.extension.lowercase()

        require(ext in listOf("mp4", "avi", "mov", "gif")) {
            "Unsupported format: $ext"
        }

        FFmpegFrameGrabber(inputFile.absolutePath).use { grabber ->
            grabber.start()

            val audioChannels = if (ext == "gif") 0 else grabber.audioChannels

            FFmpegFrameRecorder(
                outputFile.absolutePath,
                grabber.imageWidth,
                grabber.imageHeight,
                audioChannels
            ).use { recorder ->

                recorder.format = ext

                recorder.frameRate =
                    if (ext == "gif")
                        minOf(grabber.frameRate, 15.0)
                    else
                        grabber.frameRate

                if (grabber.videoBitrate > 0) {
                    recorder.videoBitrate = grabber.videoBitrate
                }

                when (ext) {
                    "mp4", "mov" -> {
                        recorder.videoCodec = avcodec.AV_CODEC_ID_H264
                        recorder.audioCodec = avcodec.AV_CODEC_ID_AAC
                        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P

                        if (grabber.audioChannels > 0) {
                            recorder.sampleRate = grabber.sampleRate
                            recorder.audioBitrate =
                                maxOf(grabber.audioBitrate, 128_000)
                        }
                    }

                    "avi" -> {
                        recorder.videoCodec = avcodec.AV_CODEC_ID_MPEG4

                        // Вместо MP3 часто лучше работает PCM
                        recorder.audioCodec = avcodec.AV_CODEC_ID_PCM_S16LE

                        recorder.pixelFormat = avutil.AV_PIX_FMT_YUV420P

                        if (grabber.audioChannels > 0) {
                            recorder.sampleRate = grabber.sampleRate
                        }
                    }

                    "gif" -> {
                        recorder.format = "gif"
                        recorder.videoCodec = avcodec.AV_CODEC_ID_GIF
                        recorder.pixelFormat = avutil.AV_PIX_FMT_RGB8
                    }
                }

                recorder.start()

                while (true) {
                    val frame = grabber.grab() ?: break

                    if (ext == "gif") {
                        if (frame.image != null) {
                            recorder.record(frame)
                        }
                    } else {
                        recorder.timestamp = grabber.timestamp
                        recorder.record(frame)
                    }
                }

                recorder.stop()
            }

            grabber.stop()
        }
    }


    fun convertVideosBatch(inputs: List<File>, outputs: List<File>) = runBlocking {
        require(inputs.size == outputs.size) { "Inputs and outputs must match in size" }
        val total = inputs.size
        _translationProgress.value = 0f
        _isStatus.value = List(total) { i -> "\uD83D\uDD01 Конвертация: ${inputs[i].name}" }

        inputs.indices.map { i ->
            async(Dispatchers.IO) {
                val inF = inputs[i]
                val outF = outputs[i]
                val status = try {
                    convertVideoSimple(inF, outF)
                    "✅ Успех: ${inF.name}"
                } catch (e: Exception) {
                    "❌ Ошибка: ${inF.name}"
                }
                synchronized(_isStatus) {
                    val list = _isStatus.value.toMutableList()
                    list[i] = status
                    _isStatus.value = list
                    val progress = list.count { it.startsWith("✅") }.toFloat() / total
                    launch { smoothProgressUpdate(progress) }
                }
            }
        }.awaitAll()
    }

}