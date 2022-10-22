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
import com.example.tracknews.databinding.FragmentWebsiteBinding
import com.google.android.material.internal.ContextUtils.getActivity
import okhttp3.*
import java.io.IOException

class WebsiteFragment : Fragment() {

    lateinit var url: String

    //lateinit var vBrowser: String
    lateinit var binding: FragmentWebsiteBinding
    private val vm: ViewModel by activityViewModels()

    var okHttpClient: OkHttpClient = OkHttpClient()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentWebsiteBinding.inflate(inflater)
        return binding.root

        //return inflater.inflate(R.layout.fragment_website, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        init()

        binding.buttonBack?.setOnClickListener {
            activity!!.onBackPressed()
            Log.d("TAG1", "fragWebsite >f buttonBack > ${activity?.findViewById<View>(R.id.fabButtonSearch)?.visibility}")
            //activity?.findViewById<View>(R.id.fabButtonSearch)?.visibility = View.VISIBLE
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = WebsiteFragment()
    }

    fun Fragment?.runOnUiThread(action: () -> Unit) {
        //прогрессбар
        this ?: return
        if (!isAdded) return // Fragment not attached to an Activity
        activity?.runOnUiThread(action)
    }

    private fun init(){
        //activity!!.findViewById<View>(R.id.fabButtonSearch).visibility = View.GONE
        loadWebsite()
    }

    private fun loadWebsite() {
        //прогрессбар, продолжение
        val url = vm.tempWebsiteLink.value.toString()
        //Log.d("TAG1", "fragWebsite >f loadWebsite > url: $url")
        //Log.d("TAG1", "fragWebsite >f loadWebsite > vm.tempUrl: ${vm.tempWebsiteLink.value}")
        val messageLoadWebsite = resources.getString(com.example.tracknews.R.string.loadWebsiteFail)

        runOnUiThread {
            binding.fragWebsiteProgressBar.visibility = View.VISIBLE
        }

        val testRequest: Request = Request.Builder().url("https://www.google.com/").build()
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