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
import org.bytedeco.javacv.Java2DFrameConverter
import java.awt.image.BufferedImage
import java.io.File
import java.nio.file.Paths
import javax.imageio.ImageIO


class AppViewModel(): ViewModel() {
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
            title = "Выберите папку",
            directory = PlatformFile(downloads.absolutePath),
            dialogSettings = dialogSettings
        )

        return picked?.path?.let { File(it) }
    }




    fun convertImageSimple(inputFile: File, outputFile: File) {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR)

        val frame = when (inputFile.extension.lowercase()) {
            "gif" -> {
                FFmpegFrameGrabber(inputFile.absolutePath).use { grabber ->
                    grabber.format = "gif"
                    grabber.start()
                    val f = grabber.grabImage()
                    grabber.stop()
                    f ?: error("Cannot grab frame from GIF ${inputFile.name}")
                }
            }
            else -> {
                val img: BufferedImage = ImageIO.read(inputFile)
                    ?: error("Cannot read image ${inputFile.name}")
                Java2DFrameConverter().convert(img)
            }
        }

        FFmpegFrameRecorder(outputFile.absolutePath, frame.imageWidth, frame.imageHeight).use { recorder ->
            val outExt = outputFile.extension.lowercase()
            recorder.format = outExt
            recorder.frameRate = 1.0

            when (outExt) {
                "jpg", "jpeg" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_MJPEG
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_YUVJ420P
                }
                "png" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_PNG
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_RGB24
                }
                "gif" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_GIF
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_RGB8
                    recorder.setVideoOption("loop", "0")
                }
                "tiff", "tif" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_TIFF
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_RGB24
                }
                "webp" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_WEBP
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_YUVA420P
                }
                "bmp" -> {
                    recorder.videoCodec   = avcodec.AV_CODEC_ID_BMP
                    recorder.pixelFormat  = avutil.AV_PIX_FMT_RGB24
                }
                else -> error("Unsupported output format: $outExt")
            }

            recorder.start()
            recorder.record(frame)
            recorder.stop()
        }
    }

    fun convertImagesBatch(inputs: List<File>, outputs: List<File>) = runBlocking {
        require(inputs.size == outputs.size) { "Input and output lists must match." }
        val total = inputs.size
        _translationProgress.value = 0f
        _isStatus.value = List(total) { i -> "\uD83D\uDD01 Конвертация: ${inputs[i].name}" }

        inputs.indices.map { i ->
            async(Dispatchers.Default) {
                val inF = inputs[i]
                val outF = outputs[i]
                val status = try {
                    convertImageSimple(inF, outF)
                    "✅ Успех: ${inF.name}"
                } catch (e: Exception) {
                    "❌ Ошибка: ${inF.name}"
                }
                synchronized(_isStatus) {
                    // update status list
                    val list = _isStatus.value.toMutableList()
                    list[i] = status
                    _isStatus.value = list
                    val progress = list.count { it.startsWith("✅") }.toFloat() / total
                    launch { smoothProgressUpdate(progress) }
                }
            }
        }.awaitAll()
    }








    fun convertVideoSimple(inputFile: File, outputFile: File) = runBlocking {
        avutil.av_log_set_level(avutil.AV_LOG_ERROR)

        val ext = outputFile.extension.lowercase()
        require(ext in listOf("mp4","mkv","avi","mov","webm")) {
            "Unsupported format: $ext"
        }

        FFmpegFrameGrabber(inputFile.absolutePath).use { grabber ->
            grabber.start()

            FFmpegFrameRecorder(
                outputFile.absolutePath,
                grabber.imageWidth,
                grabber.imageHeight,
                grabber.audioChannels
            ).apply {
                format      = ext

                when (ext) {
                    "mp4", "mov", "mkv" -> {
                        videoCodec = avcodec.AV_CODEC_ID_H264
                        audioCodec = avcodec.AV_CODEC_ID_AAC
                        pixelFormat = avutil.AV_PIX_FMT_YUV420P
                    }
                    "avi" -> {
                        videoCodec = avcodec.AV_CODEC_ID_MPEG4
                        audioCodec = avcodec.AV_CODEC_ID_MP3
                        pixelFormat = avutil.AV_PIX_FMT_YUV420P
                        videoBitrate = grabber.videoBitrate.takeIf { it > 0 } ?: 2_000_000
                    }
                    "webm" -> {
                        videoCodec = avcodec.AV_CODEC_ID_VP9
                        audioCodec = avcodec.AV_CODEC_ID_VORBIS
                        pixelFormat = avutil.AV_PIX_FMT_YUVA420P
                        setAudioOption("flags", "+global_header")
                    }

                }
            }.use { rec ->
                rec.start()
                var frame = grabber.grab()
                while (frame != null) {
                    rec.record(frame)
                    frame = grabber.grab()
                }
                rec.stop()
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