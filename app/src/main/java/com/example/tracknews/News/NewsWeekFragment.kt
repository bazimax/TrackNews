package com.example.tracknews.News

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.FragmentNewsWeekBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection
import kotlinx.coroutines.*
import org.jsoup.select.Elements


class NewsWeekFragment : Fragment() {

    lateinit var binding: FragmentNewsWeekBinding
    //private val vm: ViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentNewsWeekBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news_week, container, false)
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsWeekFragment()
    }
}