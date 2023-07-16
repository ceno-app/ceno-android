package ie.equalit.ceno.onboarding

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import ie.equalit.ceno.AppPermissionCodes.REQUEST_CODE_NOTIFICATION_PERMISSIONS
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentOnboardingBatteryBinding
import ie.equalit.ceno.ext.requireComponents

/**
 * A simple [Fragment] subclass.
 * Use the [OnboardingBatteryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnboardingBatteryFragment : Fragment() {
    private var _binding: FragmentOnboardingBatteryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBatteryBinding.inflate(inflater, container,false)
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_splash_background)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            /* This is Android 13 or later, ask for permission POST_NOTIFICATIONS */
            binding.text.text = getString(R.string.onboarding_battery_text_v33)
            binding.button.setOnClickListener {
                allowPostNotifications()
            }
        }
        else {
            /* This is NOT Android 13, just ask to disable battery optimization */
            binding.text.text = getString(R.string.onboarding_battery_text)
            binding.button.setOnClickListener {
                disableBatteryOptimization()
            }
        }
    }

    private fun disableBatteryOptimization() {
        if (!requireComponents.permissionHandler.requestBatteryOptimizationsOff(requireActivity())) {
            binding.root.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.onboarding_splash_background
            )
            findNavController().navigate(R.id.action_global_home)
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun allowPostNotifications() {
        if (!requireComponents.permissionHandler.requestPostNotificationsPermission(this)) {
            findNavController().navigate(R.id.action_onboardingBatteryFragment_to_onboardingThanksFragment)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == REQUEST_CODE_NOTIFICATION_PERMISSIONS) {
            requireComponents.ouinet.background.start()
            disableBatteryOptimization()
        }
        else {
            Log.e(TAG, "Unknown request code received: $requestCode")
            findNavController().navigate(R.id.action_onboardingBatteryFragment_to_onboardingThanksFragment)
        }
    }

    companion object {
        const val TAG = "ONBOARD_BATTERY"
    }
}