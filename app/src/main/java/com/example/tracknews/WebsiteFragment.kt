package com.example.tracknews

import android.app.PendingIntent.getActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.classes.Constants
import com.example.tracknews.databinding.FragmentWebsiteBinding
import com.google.android.material.internal.ContextUtils.getActivity
import okhttp3.*
import java.io.IOException

class WebsiteFragment : Fragment() {
    private val logNameClass = "WebsiteFragment"

    lateinit var url: String
    lateinit var binding: FragmentWebsiteBinding
    private val vm: ViewModel by activityViewModels()

    var okHttpClient: OkHttpClient = OkHttpClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentWebsiteBinding.inflate(inflater)
        return binding.root
    }

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        init()
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        loadWebsite()

        binding.buttonBack?.setOnClickListener {
            activity!!.onBackPressed()
            Log.d(Constants.TAG_DEBUG, "$logNameClass >f buttonBack > buttonBack > ${activity?.findViewById<View>(R.id.fabButtonSearch)?.visibility}")
            //activity?.findViewById<View>(R.id.fabButtonSearch)?.visibility = View.VISIBLE
        }

        vm.newsItemUpdateItem.observe(activity as LifecycleOwner) {
            swapButtonsSave()
        }

        vm.tempNewsItemOpenWebsite.observe(activity as LifecycleOwner) {
            swapButtonsSave()
        }

        binding.buttonSaveNews?.setOnClickListener {
            //Log.d(Constants.TAG_DATA, "$logNameClass > buttonSaveNews > vm.newsItemUpdateItem.value: ${vm.newsItemUpdateItem.value}")
            //??
            /*if (vm.tempNewsItemOpenWebsite.value != null) {
                vm.newsItemUpdateItem.value = vm.tempNewsItemOpenWebsite.value
            }*/
            vm.newsItemUpdateItem.value = vm.tempNewsItemOpenWebsite.value
            //Log.d(Constants.TAG_DATA, "$logNameClass > buttonSaveNews > UPDATE > vm.newsItemUpdateItem.value: ${vm.newsItemUpdateItem.value}")
            binding.buttonSaveNews?.visibility = View.GONE
            binding.buttonUnSaveNews?.visibility = View.VISIBLE
        }

        binding.buttonUnSaveNews?.setOnClickListener {
            //Log.d(Constants.TAG_DATA, "$logNameClass > buttonUnSaveNews > vm.newsItemUpdateItem.value: ${vm.newsItemUpdateItem.value}")
            vm.newsItemUpdateItem.value = vm.tempNewsItemOpenWebsite.value
            binding.buttonSaveNews?.visibility = View.VISIBLE
            binding.buttonUnSaveNews?.visibility = View.GONE
            //Log.d(Constants.TAG_DATA, "$logNameClass > buttonUnSaveNews > UPDATE > vm.newsItemUpdateItem.value: ${vm.newsItemUpdateItem.value}")
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = WebsiteFragment()
    }

    fun swapButtonsSave(){
        if (vm.tempNewsItemOpenWebsite.value?.statusSaved == "true") {
            binding.buttonSaveNews?.visibility = View.VISIBLE
            binding.buttonUnSaveNews?.visibility = View.GONE
        }
        else {
            binding.buttonSaveNews?.visibility = View.GONE
            binding.buttonUnSaveNews?.visibility = View.VISIBLE
        }
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        //прогрессбар
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun init(){
        //activity!!.findViewById<View>(R.id.fabButtonSearch).visibility = View.GONE
        /*vm.newsItemUpdateItem.observe(activity as LifecycleOwner) {
            if (vm.newsItemUpdateItem.value?.statusSaved == "true") {
                binding.buttonSaveNews?.visibility = View.VISIBLE
                binding.buttonUnSaveNews?.visibility = View.GONE
            }
            else {
                binding.buttonSaveNews?.visibility = View.GONE
                binding.buttonUnSaveNews?.visibility = View.VISIBLE
            }
        }*/

    }

    private fun loadWebsite() {
        Log.d(Constants.TAG_DEBUG, "$logNameClass >f loadWebsite >  vm.newsItemUpdateItem.value: ${vm.tempNewsItemOpenWebsite.value}")

        /*if (vm.tempNewsItemOpenWebsite.value?.statusSaved == "true") {
            binding.buttonSaveNews?.visibility = View.VISIBLE
            binding.buttonUnSaveNews?.visibility = View.GONE
        }
        else {
            binding.buttonSaveNews?.visibility = View.GONE
            binding.buttonUnSaveNews?.visibility = View.VISIBLE
        }*/
        //прогрессбар, продолжение
        //val url = vm.tempWebsiteLink.value.toString()
        //val url = vm.newsItemUpdateItem.value?.link ?: ""

        //??
        val url = vm.tempNewsItemOpenWebsite.value?.link ?: ""

        //Log.d("TAG1", "fragWebsite >f loadWebsite > url: $url")
        //Log.d("TAG1", "fragWebsite >f loadWebsite > vm.tempUrl: ${vm.tempWebsiteLink.value}")
        val messageLoadWebsite = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)

        runOnUiThread {
            binding.fragWebsiteProgressBar.visibility = View.VISIBLE
        }

        //val testRequest: Request = Request.Builder().url("https://www.google.com/").build()

        if (url != "") {
            val request: Request = Request.Builder().url(url).build()

            okHttpClient.newCall(request).enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    //vm.messageLoadWebsite.value = messageLoadWebsite
                    binding.fragWebsiteProgressBar.visibility = View.GONE
                    binding.fragWebsiteTextView.text = messageLoadWebsite
                    binding.fragWebsiteTextView.visibility = View.VISIBLE
                    Toast.makeText(view?.context, "Bad connection", Toast.LENGTH_SHORT).show()
                    //vm.messageFact.value = "Fail"
                }

                override fun onResponse(call: Call, response: Response) {
                    runOnUiThread {
                        binding.fragWebsiteProgressBar.visibility = View.GONE
                        binding.fragWebsiteWebView.loadUrl(url)
                        //dataModel.messageFact.value = Html.fromHtml(txt).toString()
                        //vm.messageLoadWebsite.value = "Good"
                        //Toast.makeText(view?.context, "website load", Toast.LENGTH_SHORT).show()
                    }
                }
            })
        }
    }
}