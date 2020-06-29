package pt.lisomatrix.safevault.ui.welcome

import android.content.Context
import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import pt.lisomatrix.safevault.R
import pt.lisomatrix.safevault.databinding.WelcomeFragmentBinding
import pt.lisomatrix.safevault.ui.welcome.adapter.ScreenSlidePagerAdapter

class WelcomeFragment : Fragment() {

    private val viewModel: WelcomeViewModel by viewModels()

    private lateinit var binding: WelcomeFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.welcome_fragment, container, false)

        // Sliding layouts
        var layouts = intArrayOf(
            R.layout.welcome_page_one,
            R.layout.welcome_page_two,
            R.layout.welcome_page_three,
            R.layout.welcome_page_four
        )

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = ScreenSlidePagerAdapter(requireContext(), layouts)
        binding.pager.adapter = pagerAdapter

        // Update accept button visibility
        updateAcceptButton(binding.pager.currentItem)

        // Listen to page changes
        binding.pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {

            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }
            override fun onPageSelected(position: Int) {
                // Update accept button visibility
               updateAcceptButton(position)
            }
        })

        // Check for accept press
        binding.acceptTxt.setOnClickListener {
            findNavController()
                .navigate(WelcomeFragmentDirections.actionWelcomeFragmentToAccountFragment())
        }

        binding.nextArrowBtn.setOnClickListener {
            val next = binding.pager.currentItem + 1
            if (next <= 3) {
                binding.pager.currentItem = next
            }
        }

        binding.backArrowBtn.setOnClickListener {
            val next = binding.pager.currentItem - 1
            if (next >= 0) {
                binding.pager.currentItem = next

            }
        }

        return binding.root
    }

    private fun updateAcceptButton(position: Int) {
        if (position == 3)
            binding.acceptTxt.visibility = View.VISIBLE
        else
            binding.acceptTxt.visibility = View.INVISIBLE
    }
}