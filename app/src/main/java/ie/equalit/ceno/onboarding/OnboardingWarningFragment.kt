package ie.equalit.ceno.onboarding

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentOnboardingWarningBinding
import ie.equalit.ceno.ext.requireComponents

/**
 * A simple [Fragment] subclass.
 * Use the [OnboardingWarningFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnboardingWarningFragment : Fragment() {
    private var _binding: FragmentOnboardingWarningBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingWarningBinding.inflate(inflater, container,false);
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_splash_background)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.text.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                /* Choose which text is displayed based on permissions allowed */
                if (!requireComponents.permissionHandler.isAllowingPostNotifications() &&
                    !requireComponents.permissionHandler.isIgnoringBatteryOptimizations()) {
                    getString(R.string.onboarding_warning_text_v33_2)
                }
                else if(!requireComponents.permissionHandler.isAllowingPostNotifications()) {
                    getString(R.string.onboarding_warning_text_v33_1)
                }
                else {
                    getString(R.string.onboarding_warning_text)
                }
            }
            else {
                getString(R.string.onboarding_warning_text)
            }
        binding.button.setOnClickListener {
            findNavController().navigate(R.id.action_onboardingWarningFragment_to_onboardingThanksFragment)
        }
    }
}