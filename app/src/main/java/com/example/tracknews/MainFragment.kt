package com.example.tracknews

//import android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.tracknews.databinding.FragmentMainBinding

import androidx.fragment.app.activityViewModels


class MainFragment : Fragment() {

    lateinit var binding: FragmentMainBinding
    private val vm: ViewModel by activityViewModels()
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

    /*override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //retainInstance = true

    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //Log.d("TAG1", "0 - MainFrag ${parentFragmentManager}")

        // 1- проверка на поворот экрана. Работает от обратного.
        if (binding.frameLayoutMainFragmentLand != null) {
            vm.statusLandscape.value = "false"
        }
        else vm.statusLandscape.value = "true"
        // 1- конец проверки

//        // > тестовые поля для проверок, удалить после окончания разработки
//        vm.url.observe(activity as LifecycleOwner) {
//            binding.fragMainTextView?.text = it
//        }
//        vm.url.observe(activity as LifecycleOwner) {
//            binding.fragMainLandTextView?.text = it
//        }
//
//
//        // ^ тестовые поля для проверок, удалить после окончания разработки

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