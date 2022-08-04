package com.example.tracknews.News

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.ViewModel
import com.example.tracknews.MainActivity
import com.example.tracknews.R
import com.example.tracknews.WebsiteFragment
import com.example.tracknews.databinding.ActivityMainBinding
import com.example.tracknews.databinding.FragmentNewsTodayBinding
import com.example.tracknews.databinding.FragmentWebsiteBinding
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

class NewsTodayFragment : Fragment() {

    private lateinit var binding: FragmentNewsTodayBinding
    private lateinit var bindingMainActivity: MainActivity
    private lateinit var bindingActivity: ActivityMainBinding
    lateinit var bindingWebsite: WebsiteFragment
    lateinit var bindingWeb: FragmentWebsiteBinding
    private val vm: ViewModel by activityViewModels()

    private val siteURL = "https://yandex.ru/"

    private val URL = "https://yandex.ru/"
    var okHttpClient: OkHttpClient = OkHttpClient()
    var request: Disposable? = null //для контроля утечки памяти, не добавлено

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentNewsTodayBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_news_today, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        vm.messageFact.observe(activity as LifecycleOwner) {
            binding.factTv.text = it
        }

        binding.nextBtn.setOnClickListener {

            loadRandomFact() //прогрессбар
            loadWebsite() //загрузка интеренет страницы

            /*//тест соединения
            o.subscribe({
                dataModel.messageFact.value = if (it == "check_OK") "Ok" else "Failed"
            },{
                dataModel.messageFact.value = "Failed"
            })*/

            /*//тест соединения2 ???
            request = o.subscribe({
                dataModel.messageFact.value = if (it == "check_OK") "Ok" else "Failed"
            },{
                dataModel.messageFact.value = "Failed"
            })*/


            Log.d("TAG1", "Test 111")
            //dataModel.messageFact.value = "1234"
            //dataModel.url2 = "0001"

            /*Log.d("TAG1", "1 = ${dataModel.messageFact}")
            Log.d("TAG1", "2 = ${dataModel.url2}")
            Log.d("TAG1", "3 = ${dataModel.url.value}")
            Log.d("TAG1", "4 = ${dataModel.statusLandscape.value}")*/
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = NewsTodayFragment()
    }

    private fun loadWebsiteV0() {
        //загрузка интернет страницы
        //1й вариант - открытие в новой страничке
        val i = Intent(Intent.ACTION_VIEW)
        i.data = Uri.parse(siteURL)
        startActivity(i)
    }

    private fun loadWebsite() {
        if (vm.statusLandscape.value == "true") {
            //загрузка интернет страницы
            //2й вариант - замена одного фрагмента на другой - WebSiteFragment
            activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayoutMainFragment, WebsiteFragment.newInstance())
                .addToBackStack("main")
                .commit()

            /*// разное, можно удалить
            Fragment newFragment = new ExampleFragment(); //в java
            val newFragment: Fragment = WebsiteFragment() //в kotlin

            //
            FragmentTransaction transaction = getFragmentManager().beginTransaction(); //в java
            val transaction: FragmentTransaction = parentFragmentManager.beginTransaction() //в kotlin

            transaction.replace(R.id.frameLayoutMainFragment, newFragment)
            transaction.addToBackStack(null)
            transaction.commit();*/

            //val testActivity = activity!!.findViewById<View>(R.id.frameLayoutMainFragment)
        }
        else {
            //bindingWeb.fragWebsiteWebView.loadUrl("https://www.google.ru/")
            activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
                .addToBackStack("main")
                .commit()
        }
    }


    val o = io.reactivex.Observable.create<String>{
        //net
        //проверка соединения

        val url = siteURL //ссылка
        val urlConnection = URL(url).openConnection() as HttpsURLConnection
        try {
            urlConnection.connect()
            if(urlConnection.responseCode == HttpURLConnection.HTTP_OK)
                it.onNext("check_OK") //передаём it в o.subscribe
            else
                it.onNext("check_Failed") //передаём it в o.subscribe
        }finally {
            urlConnection.disconnect()
        }

    }.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        //прогрессбар
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun loadRandomFact() {
        //прогрессбар, продолжение
        runOnUiThread {
            binding.progressBar.visibility = View.VISIBLE
        }

        val request: Request = Request.Builder().url(URL).build()
        okHttpClient.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call?, e: IOException?) {
                //dataModel.messageFact.value = "Fail"
            }

            override fun onResponse(call: Call?, response: Response?) {
                val json = response?.body()?.string()
                //val txt = (JSONObject(json).getJSONObject("value").get("joke")).toString()

                //binding.factTv.text = Html.fromHtml(txt)
                //binding.factTv.text = "123"
                //dataModel.messageFact.value = "123"
                runOnUiThread {
                    binding.progressBar.visibility = View.GONE
                    //dataModel.messageFact.value = Html.fromHtml(txt).toString()
                    vm.messageFact.value = "Good"
                }
            }
        })
    }

}

