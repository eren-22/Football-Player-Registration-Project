package com.erenoz.teamjersey

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.erenoz.teamjersey.databinding.ActivityJerseyBinding
import com.erenoz.teamjersey.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var footballList : ArrayList<Football>
    private lateinit var footballAdapter : FootballAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        footballList = ArrayList<Football>()

        footballAdapter = FootballAdapter(footballList)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = footballAdapter


        try {

            val database = openOrCreateDatabase("Football" , MODE_PRIVATE , null)

            val cursor = database.rawQuery("SELECT * FROM players" , null)
            val playerNameIx = cursor.getColumnIndex("playerName")
            val idIx = cursor.getColumnIndex("id")

            while (cursor.moveToNext()) {
                val name = cursor.getString(playerNameIx)
                val id = cursor.getInt(idIx)

                val football = Football(name , id)
                footballList.add(football)
            }

            footballAdapter.notifyDataSetChanged()  //footballAdaptere veri setinin değiştiğini ve yeni verilerin geldiğini söylüyor.

            cursor.close()


        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {  //main activityi menüye bağladı.

        //inflater
        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.player_menu , menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {  //tıklanınca ne olacağı...

        if (item.itemId == R.id.add_item) {
            val intent = Intent(this@MainActivity , JerseyActivity::class.java)
            intent.putExtra("info" , "new")
            startActivity(intent)
        }

        return super.onOptionsItemSelected(item)
    }

}