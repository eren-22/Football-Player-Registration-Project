package com.erenoz.teamjersey

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.erenoz.teamjersey.databinding.ActivityJerseyBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.sql.SQLData

class JerseyActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJerseyBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>   //galeriye gitmek için
    private lateinit var permissionLauncher: ActivityResultLauncher<String>  //izin almak için
    var selectedBitmap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJerseyBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Football" , MODE_PRIVATE , null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){  //yeni futbolcuyu kaydetmeye çalışıyor
            binding.playerNameText.setText("")
            binding.teamNameText.setText("")
            binding.seasonText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.selectimage)
        }
        else {     //kaydedilen veriyi gösterecek.
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id" , 1)

            val cursor = database.rawQuery("SELECT * FROM players where id = ?" , arrayOf(selectedId.toString()))  //  ? ile eşleştirir

            val playerNameIx = cursor.getColumnIndex("playerName")
            val teamNameIx = cursor.getColumnIndex("teamName")
            val seasonIx = cursor.getColumnIndex("season")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()) {
                binding.playerNameText.setText(cursor.getString(playerNameIx))
                binding.teamNameText.setText(cursor.getString(teamNameIx))
                binding.seasonText.setText(cursor.getString(seasonIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray , 0 , byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }

            cursor.close()

        }

    }

    fun saveButton (view : View){

        //   **** SQLite ****

        val playerName = binding.playerNameText.text.toString()
        val teamName = binding.teamNameText.text.toString()
        val season = binding.seasonText.text.toString()

        if (selectedBitmap != null) {
            val smallBitmap = makeSmallerBitmap(selectedBitmap!! , 300)

            val outputStream = ByteArrayOutputStream()   //görseli byte'a çevirme
            smallBitmap.compress(Bitmap.CompressFormat.PNG , 50 , outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                //val database = this.openOrCreateDatabase("Football" , MODE_PRIVATE , null)
                database.execSQL("CREATE TABLE IF NOT EXISTS players (id INTEGER PRIMARY KEY , playerName VARCHAR , teamName VARCHAR , season VARCHAR , image BLOB)")

                val sqlString = "INSERT INTO players (playerName , teamName , season , image) VALUES (? , ? , ? , ?)"
                val statement = database.compileStatement(sqlString) //compileStatement metodu sou işaretlerine bağlar.

                statement.bindString(1 , playerName) //indis 1'den başlıyor.
                statement.bindString(2 , teamName)
                statement.bindString(3 , season)
                statement.bindBlob(4 , byteArray)  //fotoğraf byte'a çevrilmişti.
                statement.execute()

            }
            catch (e: Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@JerseyActivity , MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)  //bundan önceki açık activityleri kapatır ve maine geri döner
            startActivity(intent)

        }

    }

    private fun makeSmallerBitmap (image : Bitmap , maxSize : Int) : Bitmap { //SQLite'dan dolayı fotoğraf küçültüldü

        //ornatılı küçültme algoritması
        var width = image.width
        var height = image.height

        var bitmapRatio : Double = width.toDouble() / height.toDouble() //orana göre yatay dikeyliğe karar verir.

        if(bitmapRatio > 1){
            //yatay
            width = maxSize
            val scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }
        else {
            //dikey
            height = maxSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()
        }

        return Bitmap.createScaledBitmap(image , 100 , 100 , true)

    }

    fun selectImage (view : View) {
        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {     //izin yoksa izin al

            if (ActivityCompat.shouldShowRequestPermissionRationale(this , Manifest.permission.READ_EXTERNAL_STORAGE)) {    //izin alma mantığını kullanıcıya göster
                // true dönerse rationali göster (mantığı göster) , bu işlemi yapması için izin vermesi gerekir.
                Snackbar.make(view , "Permission needed for gallery" , Snackbar.LENGTH_INDEFINITE).setAction("Give Permission" , View.OnClickListener {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }
            else {
                    //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
        else{
            val intentToGallery = Intent (Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //telefonda nerede kayıtlı olduğunu bulup ordan çekmek için
        //intent
            activityResultLauncher.launch(intentToGallery)
        }

    }

    private fun registerLauncher() {

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

            if (result.resultCode == RESULT_OK) {
                val intentFromResult = result.data
                if (intentFromResult != null) {
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)


                  if (imageData != null) {
                      try {
                          if (Build.VERSION.SDK_INT >= 28) {
                              val source = ImageDecoder.createSource(this@JerseyActivity.contentResolver, imageData)
                              selectedBitmap = ImageDecoder.decodeBitmap(source)
                              binding.imageView.setImageBitmap(selectedBitmap)
                          }
                          else {
                              selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver , imageData) //28 sürümünden düşük olduğu için
                              binding.imageView.setImageBitmap(selectedBitmap)
                          }
                      } catch (e: Exception) {
                          e.printStackTrace()
                      }
                   }
                }
            }
        }

         permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
             if(result){
                 //izin verildi
                 val intentToGallery = Intent (Intent.ACTION_PICK , MediaStore.Images.Media.EXTERNAL_CONTENT_URI) //telefonda nerede kayıtlı olduğunu bulup ordan çekmek için
                 activityResultLauncher.launch(intentToGallery)
             }
             else{
                 //izin verilmedi
                 Toast.makeText(this@JerseyActivity , "Permission needed!" , Toast.LENGTH_LONG).show()
             }
         }

    }
}