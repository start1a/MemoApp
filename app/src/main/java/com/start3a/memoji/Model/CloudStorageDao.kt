package com.start3a.memoji.Model

import android.net.Uri
import android.util.Log
import com.google.firebase.storage.StorageReference
import com.start3a.memoji.data.MemoImageFilePath
import com.start3a.memoji.repository.Repository
import java.io.File

// 디렉토리 구조
// Users/useID/filesDir/Memos/memoID/imagePath/image.jpg
class CloudStorageDao(private val mStorageRef: StorageReference) {

    companion object {
        private const val TAG = "CloudStorageDao"
    }

    fun createFileOnLocalDB(
        ref: StorageReference,
        pathParent: String,
        dirOfDestFile: String
    ) {
        val imagePath = "$pathParent/${dirOfDestFile}"
        File(imagePath).mkdirs()

        ref.child(dirOfDestFile).listAll().addOnSuccessListener { images ->
            images.items.forEach {
                val localFile = File(imagePath, it.name)

                it.getFile(localFile)
                    .addOnSuccessListener {
                        Log.d(TAG, "file created : " + localFile.name)
                    }.addOnCanceledListener {
                        // 임시 파일 삭제
                        localFile.deleteOnExit()
                        Log.d(TAG, "file failed : " + localFile.name)
                    }
            }
        }.addOnFailureListener {
            Log.d(TAG, "item connect failed!!")
            it.printStackTrace()
        }
    }

    fun getImageFile(
        pathStorage: String,
        pathLocal: String,
        notifyListener: () -> Unit
    ) {
        val ref = mStorageRef.child(pathStorage)
        ref.listAll()
            .addOnSuccessListener { dirs ->
                dirs.prefixes.forEach {
                    val memoIdPath = "$pathLocal/${it.name}"
                    createFileOnLocalDB(it, memoIdPath, Repository.THUMBNAIL_PATH)
                    createFileOnLocalDB(it, memoIdPath, Repository.ORIGINAL_PATH)
                    notifyListener()
                }
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }

    private fun UploadImage(path: String, uri: Uri) {
        mStorageRef.child(path).putFile(uri)
            .addOnSuccessListener { Log.d(TAG, "Storage Upload successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error uploading Image File", e) }
    }

    fun saveImageFileOfMemo(pathParent: String, fileList: MutableList<MemoImageFilePath>) {
        fileList.forEach {
            UploadImage(
                "$pathParent/${it.thumbnailPath}",
                Uri.fromFile(File(it.thumbnailPath))
            )
            UploadImage(
                "$pathParent/${it.originalPath}",
                Uri.fromFile(File(it.originalPath))
            )
        }
    }

    fun deleteImageOfMemo(pathParent: String, deleteList: MutableList<MemoImageFilePath>) {
        val ref = mStorageRef.child(pathParent)
        deleteList.forEach {
            deleteFile(ref.child(it.thumbnailPath))
            deleteFile(ref.child(it.originalPath))
        }
    }

    fun deleteAllImageOfMemo(path: String) {
        val ref = mStorageRef.child(path)

        ref.listAll().addOnSuccessListener {
            // 디렉토리 내 파일 제거
            it.items.forEach {
                deleteFile(ref.child(it.name))
            }
            // 하위 디렉토리로 이동
            // 비어있는 디렉토리는 자동 제거됨
            it.prefixes.forEach {
                deleteAllImageOfMemo(it.path)
            }
        }
    }

    private fun deleteFile(ref: StorageReference) {
        ref.delete()
            .addOnSuccessListener {
                Log.d(TAG, "successfully delete Image File")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
            }
    }
}