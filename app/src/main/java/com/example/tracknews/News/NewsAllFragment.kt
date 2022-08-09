package com.example.tracknews.News

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.ViewModel
import com.example.tracknews.classes.NewsItem
import com.example.tracknews.classes.NewsItemAdapter
import com.example.tracknews.databinding.FragmentNewsAllBinding
import com.example.tracknews.parseSite.ParserSites
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import javax.net.ssl.HttpsURLConnection

class NewsAllFragment : Fragment() {

    lateinit var binding: FragmentNewsAllBinding

    /*var urlYaRu = "https://ya.ru/"
    var testUrlYaSearch = "https://ya.ru/"
    var urlYandexSearch = ""
    var urlGoogleSearch = ""
    var line: String = ""
    //lateinit var docYaRu: Document
    private val newsItemAdapter = NewsItemAdapter()*/

    private val vm: ViewModel by activityViewModels()

    private var parserSites = ParserSites()

    //var newsItemListParser = ArrayList<NewsItem>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsAllBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news_all, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        vm.webSiteData.value = "!!0000"
        vm.webSiteData.observe(activity as LifecycleOwner) {
            binding.testFragNewsAllTextView.text = it
        }

        binding.testFragNewsAllButton.setOnClickListener{
            Log.d("TAG1", "Click - Button")
            vm.newsItemTempYa.value = parserSites.parse(binding.testFragNewsAllEditText.text.toString())
            Log.d("TAG1", "vm.newsItemTempYa: ${vm.newsItemTempYa.value}")
            Log.d("TAG1", "End button click -----")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsAllFragment()
    }
}