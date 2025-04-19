package com.dung.madfamilytree.views.activities

import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.ImageItemAdapter
import com.dung.madfamilytree.databinding.ActivityCreateNewAlbumBinding
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.viewmodels.CreateNewAlbumViewModel

class CreateNewAlbumActivity : BaseActivity() {
    private lateinit var binding: ActivityCreateNewAlbumBinding
    private lateinit var viewModel: CreateNewAlbumViewModel
    private val pickImages = registerForActivityResult(ActivityResultContracts.GetMultipleContents()){ uris ->
        handleResult(uris)
    }
    val adapter = ImageItemAdapter {
        if(it != null){
            viewModel.deleteImage(it)
        }
        else {
            pickImages.launch("image/*")
        }
    }
    fun handleResult(Uris: List<Uri>){
        val imageList = mutableListOf<Image>()
        for(uri in Uris){
            imageList.add(Image("",uri))
        }
        viewModel.addImageList(imageList)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateNewAlbumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpToolBar()

        viewModel = ViewModelProvider(this).get(CreateNewAlbumViewModel::class.java)
        viewModel.imageList.observe(this, Observer {
            it?.let {
                adapter.data = it
            }
        })
        viewModel.setImageList(listOf(Image("hello",null)))
        binding.imageRecycleView.adapter = adapter

    }
    fun setUpToolBar(){
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ContextCompat.getString(this,R.string.create_new_album_activity_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}