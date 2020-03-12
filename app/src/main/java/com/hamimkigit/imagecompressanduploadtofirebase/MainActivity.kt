package com.hamimkigit.imagecompressanduploadtofirebase

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private val IMAGE_PICK_CODE = 1000
    private var TAG = "UploadPicture"

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var imgDp: ImageView
    private lateinit var imgUpload: ImageView
    private lateinit var imgCheck: ImageView
    private lateinit var progressBar: ProgressBar



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        databaseReference= FirebaseDatabase.getInstance().reference
        storageReference= FirebaseStorage.getInstance().reference

        imgDp=findViewById(R.id.imgDPUploadPicture)
        imgUpload=findViewById(R.id.imgUploadPicture)
        imgCheck=findViewById(R.id.imgCheckUploadPicture)
        progressBar=findViewById(R.id.pbUploadPicture)

        imgUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            if (intent.resolveActivity(packageManager) != null) {
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), IMAGE_PICK_CODE)
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        if (resultCode == Activity.RESULT_OK && requestCode == IMAGE_PICK_CODE){

            val bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, intent?.data)
            Toast.makeText(this@MainActivity, "Image Selected!", Toast.LENGTH_SHORT).show()
            imgDp!!.setImageBitmap(bitmap)
            setMyFilePath(intent?.data,bitmap)


        }
    }

    private fun setMyFilePath(dataUri: Uri?, bitmap: Bitmap) {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val data: ByteArray = byteArrayOutputStream.toByteArray()

        val filePath:StorageReference=storageReference.child("bytesdata")
        val uploadTask: UploadTask = filePath.child("after").putBytes(data)
        val uploadTaskDemo: UploadTask? = dataUri?.let { filePath.child("original").putFile(it) }


        uploadTask.addOnCompleteListener() {
            Log.d(TAG,"addOnCompleteListener")
            imgUpload.visibility=View.GONE
            uploadTask.addOnSuccessListener {
                Log.d(TAG,"addOnSuccessListener")
                progressBar.visibility=View.GONE
                imgUpload.visibility=View.GONE
                imgCheck.visibility=View.VISIBLE
                filePath.downloadUrl.addOnSuccessListener {
                    Log.d(TAG,"downloadUrl")
                }
            }.addOnFailureListener {
                Log.d(TAG,"addOnFailureListener")

                progressBar.visibility= View.GONE
                imgUpload.visibility=View.VISIBLE
                imgCheck.visibility=View.GONE
            }
        }.addOnCanceledListener {
            Log.d(TAG,"addOnCanceledListener")

            progressBar.visibility= View.GONE
            imgUpload.visibility=View.VISIBLE
            imgCheck.visibility=View.GONE
            Toast.makeText(this@MainActivity, "addOnCanceledListener", Toast.LENGTH_SHORT).show()
        }.addOnProgressListener {
            Log.d(TAG,"addOnProgressListener")

            progressBar.visibility= View.VISIBLE
            imgUpload.visibility=View.GONE
            imgCheck.visibility= View.GONE


        }
        uploadTaskDemo?.addOnCompleteListener() {
            Log.d(TAG,"original")

            uploadTask.addOnSuccessListener {
                filePath.downloadUrl.addOnSuccessListener {task ->
                    databaseReference.child("uploaded").push().setValue(task.toString())

                }
            }.addOnFailureListener {
                Toast.makeText(this@MainActivity, "addOnFailureListener", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
