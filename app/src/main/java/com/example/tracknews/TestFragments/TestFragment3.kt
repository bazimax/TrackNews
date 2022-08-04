package com.example.tracknews.TestFragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.R
import com.example.tracknews.ViewModel
import com.example.tracknews.databinding.FragmentTest3Binding
import java.util.zip.Inflater

class TestFragment3 : Fragment() {
    private val vm: ViewModel by activityViewModels()
    lateinit var binding: FragmentTest3Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentTest3Binding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_test3, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fragTest3TextViewText.text = vm.newsItem.value.toString()
        vm.newsItemTemp.observe(activity as LifecycleOwner){
            binding.fragTest3TextViewText.text = vm.newsItem.value.toString()
        }
    }

    companion object {

        @JvmStatic
        fun newInstance() = TestFragment3()
    }
}