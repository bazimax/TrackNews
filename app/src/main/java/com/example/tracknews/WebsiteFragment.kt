package com.example.tracknews

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.databinding.FragmentWebsiteBinding

class WebsiteFragment : Fragment() {

    lateinit var url: String

    //lateinit var vBrowser: String
    lateinit var binding: FragmentWebsiteBinding
    private val dataModel: DataModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentWebsiteBinding.inflate(inflater)
        return binding.root

        //return inflater.inflate(R.layout.fragment_website, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        url = "https://yandex.ru/"
        //vBrowser.loadUrl(url)
        //dataModel.url2 = "124"


        /*Log.d("TAG1", "1 = ${dataModel.messageFact}")
        Log.d("TAG1", "2 = ${dataModel.url2}")
        Log.d("TAG1", "3 = ${dataModel.url.value}")
        Log.d("TAG1", "4 = ${dataModel.statusLandscape.value}")*/

        dataModel.url.observe(activity as LifecycleOwner){

        }

        binding.fragWebsiteWebView.loadUrl(url)
        //Log.d("TAG1", "vBrowser = $vBrowser")
    }

    companion object {
        @JvmStatic
        fun newInstance() = WebsiteFragment()
    }
}