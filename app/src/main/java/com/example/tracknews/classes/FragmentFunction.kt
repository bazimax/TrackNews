package com.example.tracknews.classes

import android.util.Log
import com.example.tracknews.MainActivity
import com.example.tracknews.ViewModel
import com.example.tracknews.WebsiteFragment

class FragmentFunction(viewModel: ViewModel) {

    companion object {
        //log
        const val TAG = Constants.TAG
        const val TAG_DEBUG = Constants.TAG_DEBUG

        //Имена файлов
        //const val FILE_SEARCH_ITEM = Constants.FILE_SEARCH_ITEM
    }

    private val vm = viewModel

    fun loadWebsite(url: String, activity: MainActivity) {
        Log.d("TAG1", "FragmentFunction >f loadWebsite")
        vm.tempWebsiteLink.value = url
        //vm.url2 = url
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > url: $url")
        //Log.d("TAG1", "fragNewsSaved >f loadWebsite > vm.tempUrl: ${vm.tempWebsiteLink.value}")

        if (vm.statusLandscape.value == "true") {
            //загрузка интернет страницы
            //2й вариант - замена одного фрагмента на другой - WebSiteFragment
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutSuperMain, WebsiteFragment.newInstance())
                .addToBackStack("main")
                .commit()
        }
        else {
            //bindingWeb.fragWebsiteWebView.loadUrl("https://www.google.ru/")
            activity.supportFragmentManager
                .beginTransaction()
                .replace(com.example.tracknews.R.id.frameLayoutMainFragmentLand, WebsiteFragment.newInstance())
                .commit()
        }
    }
}