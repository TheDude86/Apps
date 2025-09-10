package com.mcmlr.blocks.api.data

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.io.InputStream

abstract class Repository<T: ConfigModel>(private val dataFolder: File? = null) {

    lateinit var model: T

    fun <U: ConfigModel> generateModel(path: String, name: String, default: U, onCompleteListener: (suspend (U) -> Unit)? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()

            val folder = File(dataFolder, path)
            val file = File(folder, "$name.json")
            var model = default

            if (file.exists()) {
                val homeInputStream: InputStream = file.inputStream()
                val homeInputString = homeInputStream.bufferedReader().use { it.readText() }

                model = Gson().fromJson(homeInputString, default::class.java)

            } else {
                if (!folder.exists()) folder.mkdirs()

                val modelString = gson.toJson(default)
                val fileWriter = FileWriter(File(folder, "$name.json"))
                fileWriter.append(modelString)
                fileWriter.close()
            }

            model.root = dataFolder
            model.filePath = path
            model.fileName = name
            onCompleteListener?.invoke(model)
        }
    }

    fun loadModel(path: String, name: String, default: T, onCompleteListener: ((T) -> Unit)? = null) {
        model = default
        CoroutineScope(Dispatchers.IO).launch {
            val gson = GsonBuilder()
                .setPrettyPrinting()
                .create()

            val folder = File(dataFolder, path)
            val file = File(folder, "$name.json")

            if (file.exists()) {
                val homeInputStream: InputStream = file.inputStream()
                val homeInputString = homeInputStream.bufferedReader().use { it.readText() }

                model = Gson().fromJson(homeInputString, default::class.java)

            } else {
                if (!folder.exists()) folder.mkdirs()

                val modelString = gson.toJson(default)
                val fileWriter = FileWriter(File(folder, "$name.json"))
                fileWriter.append(modelString)
                fileWriter.close()
            }

            model.root = dataFolder
            model.filePath = path
            model.fileName = name
            onCompleteListener?.invoke(model)
        }
    }

    fun save(callback: () -> Unit): Job? {
        callback.invoke()
        val file = model.root ?: return null
        return saveFile(file, model.filePath, model.fileName, model)
    }

    private fun saveFile(root: File, path: String, name: String, model: Any, finishedCallback: () -> Unit = {}) = CoroutineScope(Dispatchers.IO).launch {
        val file = File(root, path)

        val gson = GsonBuilder()
            .setPrettyPrinting()
            .create()

        val fileConfigString = gson.toJson(model)
        val fileWriter = FileWriter(File(file.path, "$name.json"))
        fileWriter.append(fileConfigString)
        fileWriter.close()

        finishedCallback()
    }
}


//fun <T: ConfigModel> T.save(callback: T.() -> Unit): T {
//    val factory = configFactory
//    if (factory != null) {
//        saveFile(filePath, fileName, this, callback)
//    } else {
//        callback.invoke(this)
//    }
//
//    return this
//}
//
//fun <T: ConfigModel> T.saveFile(path: String, name: String, model: Any, finishedCallback: T.() -> Unit = {}) {
//    CoroutineScope(Dispatchers.IO).launch {
//        val file = File(root, path)
//
//        val gson = GsonBuilder()
//            .setPrettyPrinting()
//            .create()
//
//        val fileConfigString = gson.toJson(model)
//        val fileWriter = FileWriter(File(file.path, "$name.json"))
//        fileWriter.append(fileConfigString)
//        fileWriter.close()
//
//        finishedCallback()
//    }
//}