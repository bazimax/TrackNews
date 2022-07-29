package com.example.tracknews

//import android.R
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.tracknews.databinding.FragmentMainBinding

import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.LifecycleOwner
import com.example.tracknews.TestFragments.TestFragment1
import com.example.tracknews.TestFragments.TestFragment2
import com.example.tracknews.TestFragments.TestFragment3
import com.example.tracknews.TestFragments.TestFragment4
import com.example.tracknews.databinding.FragmentNewsBinding
import com.example.tracknews.databinding.FragmentTest1Binding


class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding
    private val dataModel: DataModel by activityViewModels()
    //private val dataModel: DataModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(inflater)
        return binding.root
        //return inflater.inflate(R.layout.fragment_main, container, false) //вместо binding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //retainInstance = true

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d("TAG1", "0 - MainFrag ${parentFragmentManager}")

        // 1- проверка на поворот экрана. Работает от обратного.
        if (binding.frameLayoutMainFragmentLand != null) {
            dataModel.statusLandscape.value = "false"
        }
        else dataModel.statusLandscape.value = "true"
        // 1- конец проверки

        // > тестовые поля для проверок, удалить после окончания разработки
        dataModel.url.observe(activity as LifecycleOwner) {
            binding.fragMainTextView?.text = it
        }
        dataModel.url.observe(activity as LifecycleOwner) {
            binding.fragMainLandTextView?.text = it
        }
        binding.fragMainButtonTest1?.setOnClickListener {
            loadFragmentToActivity(TestFragment1.newInstance())
        }
        binding.fragMainButtonTest2?.setOnClickListener {
            loadFragmentToActivity(TestFragment2.newInstance())
        }
        binding.fragMainButtonTest3?.setOnClickListener {
            loadFragmentToActivity(TestFragment3.newInstance())
        }
        binding.fragMainButtonTest4?.setOnClickListener {
            loadFragmentToActivity(TestFragment4.newInstance())
        }


        // ^ тестовые поля для проверок, удалить после окончания разработки

    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    private fun loadFragmentToActivity(fragment: Fragment){
        //загружаем нужный фрагмент через активити
        activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayoutMainFragment, fragment)
            .addToBackStack("main")
            .commit()
        /*activity!!.supportFragmentManager
            .beginTransaction()
            .replace(R.id.frameLayoutMainFragment, TestFragment4.newInstance())
            .addToBackStack("main")
            .commit()*/

    }
}