package com.dung.madfamilytree.views.activities

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.dung.madfamilytree.R
import com.dung.madfamilytree.adapters.ImageItemAdapter
import com.dung.madfamilytree.databinding.ActivityCreateNewAlbumBinding
import com.dung.madfamilytree.dtos.AlbumDTO
import com.dung.madfamilytree.dtos.ImageDTO
import com.dung.madfamilytree.dtos.InvokingDTO
import com.dung.madfamilytree.models.Image
import com.dung.madfamilytree.utility.Utility
import com.dung.madfamilytree.viewmodels.CreateNewAlbumViewModel
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateNewAlbumActivity : BaseActivity() {
    private val db = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityCreateNewAlbumBinding
    private lateinit var viewModel: CreateNewAlbumViewModel
    private val pickImages =
        registerForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
            handleResult(uris)
        }
    val adapter = ImageItemAdapter {
        if (it != null) {
            viewModel.deleteImage(it)
        } else {
            pickImages.launch("image/*")
        }
    }

    fun handleResult(Uris: List<Uri>) {
        val imageList = mutableListOf<Image>()
        for (uri in Uris) {
            imageList.add(Image("", uri))
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
        viewModel.setImageList(listOf(Image("hello", null)))
        binding.imageRecycleView.adapter = adapter

        binding.createAlbumBtn.setOnClickListener {
            var valid = true
            if (binding.albumName.text.toString().equals("")) {
                binding.albumName.setHintTextColor(Color.RED)
                valid = false
            }
            if (binding.albumPlace.text.toString().equals("")) {
                binding.albumPlace.setHintTextColor(Color.RED)
                valid = false
            }
            if (binding.albumStory.text.toString().equals("")) {
                binding.albumStory.setHintTextColor(Color.RED)
                valid = false
            }
            if (valid) {
                binding.loadingView.visibility = View.VISIBLE
                lifecycleScope.launch(Dispatchers.IO) {
                    viewModel.upLoadImages(this@CreateNewAlbumActivity)
                    Utility.db?.collection("Album")?.add(
                        hashMapOf(
                            "name" to binding.albumName.text.toString(),
                            "place" to binding.albumPlace.text.toString(),
                            "story" to binding.albumStory.text.toString(),
                            "owner" to Utility.db?.collection("Account")
                                ?.document(Utility.accountId)
                        )
                    )?.addOnSuccessListener { albumRef ->
//                    var count = 0
                        Utility.db?.collection("Invoking")
                            ?.add(
                                InvokingDTO(
                                    albumRef,
                                    Utility.db?.collection("Account")?.document(Utility.accountId)
                                )
                            )
                            ?.addOnSuccessListener {
                                var count = 0
                                for (imageUrl in viewModel.imageUrlList) {
                                    count++
                                    Utility.db?.collection("Image")
                                        ?.add(
                                            hashMapOf(
                                                "url" to imageUrl,
                                                "album" to albumRef,
                                                "uploadTime" to FieldValue.serverTimestamp()
                                            )
                                        )
                                        ?.addOnSuccessListener { imageRef ->
                                            if (count == viewModel.imageUrlList.size) {
                                                binding.loadingView.visibility = View.GONE
                                                finish()
                                            }
                                        }

                                }
                                if (viewModel.imageUrlList.size == 0) {
                                    binding.loadingView.visibility = View.GONE
                                    finish()
                                }
                            }

                    }
                }
            }
        }

    }

    fun setUpToolBar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title =
            ContextCompat.getString(this, R.string.create_new_album_activity_title)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        finish()
        return super.onOptionsItemSelected(item)
    }
}