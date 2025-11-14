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
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.bytedeco.ffmpeg.global.avcodec
import org.bytedeco.ffmpeg.global.avutil
import org.bytedeco.javacv.FFmpegFrameGrabber
import org.bytedeco.javacv.FFmpegFrameRecorder
import org.docx4j.Docx4J
import org.docx4j.openpackaging.packages.WordprocessingMLPackage
import java.awt.image.BufferedImage
import java.io.File
import java.io.FileOutputStream
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
        try {
            val inputExt = inputFile.extension.lowercase()
            val outputExt = outputFile.extension.lowercase()
            val image: BufferedImage = ImageIO.read(inputFile)
                ?: error("Cannot read image ${inputFile.name}")

            println("Successfully read image: ${inputFile.absolutePath}")

            val outExt = outputFile.extension.lowercase()
            println("Saving image as: $outExt")

            if (inputExt in listOf("doc", "docx") && outputExt == "pdf") {
                val success = convertWordToPdf(inputFile, outputFile)
                if (!success) println("Ошибка конвертации Word в PDF")
                return
            }


            if (inputExt == "pdf" && outputExt in listOf("doc", "docx")) {
                val success = convertPdfToDocx_simple(inputFile, outputFile)
                if (!success) println("Ошибка конвертации PDF в Word")
                return
            }

            val isSaved = when (outExt) {
                "jpg", "jpeg" -> {
                    println("Attempting to save as JPEG...")
                    ImageIO.write(image, "JPEG", outputFile)
                }
                "png" -> {
                    println("Attempting to save as PNG...")
                    ImageIO.write(image, "PNG", outputFile)
                }
                "gif" -> {
                    println("Attempting to save as GIF...")
                    ImageIO.write(image, "GIF", outputFile)
                }
                "tiff", "tif" -> {
                    println("Attempting to save as TIFF...")
                    ImageIO.write(image, "TIFF", outputFile)
                }
                "bmp" -> {
                    println("Attempting to save as BMP...")
                    ImageIO.write(image, "BMP", outputFile)
                }
                "psd" -> {
                    println("Attempting to save as PSD...")
                    ImageIO.write(image, "PSD", outputFile)
                }
                "webp" -> {
                    println("Attempting to save as WEBP...")
                    val result = ImageIO.write(image, "WEBP", outputFile)
                    if (!result) {
                        println("Error: Failed to save as WEBP")
                    }
                    result
                }
                else -> {
                    println("Unsupported output format: $outExt")
                    false
                }
            }

            if (isSaved) {
                println("Image successfully saved as: ${outputFile.absolutePath}")
            } else {
                println("Error: Failed to save image.")
            }
        } catch (e: Exception) {
            println("An error occurred: ${e.message}")
            e.printStackTrace()
        }
    }

    fun convertWordToPdf(inputFile: File, outputFile: File): Boolean {
        return try {
            when (inputFile.extension.lowercase()) {
                "docx" -> convertDocxToPdf(inputFile, outputFile)
                "doc" -> convertDocToPdf(inputFile, outputFile)
                else -> false
            }
        } catch (e: Exception) {
            println("Ошибка конвертации: ${e.message}")
            false
        }
    }
    fun convertDocxToPdf(inputFile: File, outputFile: File): Boolean {
        return try {
            val wordMLPackage = WordprocessingMLPackage.load(inputFile)

            FileOutputStream(outputFile).use { output ->
                Docx4J.toPDF(wordMLPackage, output)
            }
            true
        } catch (e: Exception) {
            println("Ошибка DOCX->PDF: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    private fun convertDocToPdf(inputFile: File, outputFile: File): Boolean {
        println("Конвертация DOC требует Apache POI: poi-scratchpad")
        return false
    }

    fun convertPdfToDocx_simple(inputFile: File, outputFile: File): Boolean {
        return try {
            val document = PDDocument.load(inputFile)
            val stripper = org.apache.pdfbox.text.PDFTextStripper()
            val text = stripper.getText(document)
            document.close()

            val wp = WordprocessingMLPackage.createPackage()
            wp.mainDocumentPart.addParagraphOfText(text)
            wp.save(outputFile)
            true
        } catch (e: Exception) {
            println("Ошибка PDF->DOCX: ${e.message}")
            false
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
                    "webm"-> {
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