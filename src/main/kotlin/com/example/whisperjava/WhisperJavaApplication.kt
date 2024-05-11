package com.example.whisperjava

import io.github.givimad.whisperjni.WhisperFullParams
import io.github.givimad.whisperjni.WhisperJNI
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.io.File
import java.nio.file.Path
import javax.sound.sampled.AudioSystem
import kotlin.math.pow


@SpringBootApplication
class WhisperJavaApplication

fun main(args: Array<String>) {
//    runApplication<WhisperJavaApplication>(*args)

    WhisperJNI.loadLibrary()
    WhisperJNI.setLibraryLogger(null)
    val whisper = WhisperJNI()
    val audioSamples: FloatArray = readAudioFileSamples("src/main/resources/audios/papagaio.wav")
    var model = whisper.init(Path.of("src/main/resources/models/ggml-large-v3.bin"))
    val params = WhisperFullParams()
    params.language = "pt-BR"
    val result = whisper.full(model, params, audioSamples, audioSamples.size)
    if (result != 0) {
        throw RuntimeException("Transcription failed with code $result")
    }
    val numSegments = whisper.fullNSegments(model)
    println("Number of segments: $numSegments")
    for (i in 0 until numSegments) {
        println(whisper.fullGetSegmentText(model, i))
    }
}
//todo: descobrir melhor forma de extrair os samples do arquivo de audio
fun readAudioFileSamples(filePath: String): FloatArray {
    val audioInputStream = AudioSystem.getAudioInputStream(File(filePath))
    val audioFormat = audioInputStream.format
    val bytesPerSample = audioFormat.sampleSizeInBits / 8
    val buffer = ByteArray(1024 * bytesPerSample)
    val samples = mutableListOf<Float>()

    var bytesRead = 0
    while (audioInputStream.read(buffer).also { bytesRead = it } != -1) {
        for (i in 0 until bytesRead step bytesPerSample) {
            var sample: Int = 0
            // convert bytes to integer
            for (j in 0 until bytesPerSample) {
                sample = sample or (buffer[i + j].toInt() shl (8 * j))
            }
            // convert integer to float
            samples.add(sample / 2.0f.pow(audioFormat.sampleSizeInBits - 1))
        }
    }

    return samples.toFloatArray()
}
