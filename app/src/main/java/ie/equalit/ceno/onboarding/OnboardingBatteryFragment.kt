package ie.equalit.ceno.onboarding

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import ie.equalit.ceno.R
import ie.equalit.ceno.components.ceno.PermissionHandler
import ie.equalit.ceno.databinding.FragmentOnboardingBatteryBinding
import ie.equalit.ceno.ext.requireComponents
import mozilla.components.support.base.feature.ActivityResultHandler

/**
 * A simple [Fragment] subclass.
 * Use the [OnboardingBatteryFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OnboardingBatteryFragment : Fragment(), ActivityResultHandler {
    private var _binding: FragmentOnboardingBatteryBinding? = null
    private val binding get() = _binding!!
    private var isActivityResumed = false
    private var lastCall: (() -> Unit)? = null
    private lateinit var pHandler : PermissionHandler

    protected val sessionId: String?
        get() = arguments?.getString(SESSION_ID)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentOnboardingBatteryBinding.inflate(inflater, container,false);
        container?.background = ContextCompat.getDrawable(requireContext(), R.drawable.onboarding_splash_background)
        pHandler = PermissionHandler(requireContext())
        return binding.root
    }

    @SuppressLint("BatteryLife")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.button.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                /* This is Android 13 or later, register POST_NOTIFICATION permission contract
                *  and start ouinetBackground, which will create a notification channel,
                *  triggering the permission request */
                pHandler.requestPostNotificationPermission(
                    requireActivity() as AppCompatActivity
                ) { isGranted ->
                    if (isGranted) {
                        /* If POST_NOTIFICATION permission is granted,
                         * then ask to disable battery optimization as well */
                        disableBatteryOptimization()
                    } else {
                        updateView(fragmentWarning)
                    }
                }
                requireComponents.ouinet.background.startup()
            }
            else {
                /* This is NOT Android 13, just ask to disable battery optimization */
                disableBatteryOptimization()
            }
        }

        binding.button2.setOnClickListener {
            OnboardingThanksFragment.transitionToFragment(requireActivity(), sessionId)
        }
    }

    override fun onPause() {
        super.onPause()
        isActivityResumed = false
    }

    override fun onResume() {
        super.onResume()
        isActivityResumed = true
        //If we have some fragment to show do it now then clear the queue
        if(lastCall != null){
            updateView(lastCall!!)
            lastCall = null
        }
    }

    private fun disableBatteryOptimization() {
        if (!pHandler.requestBatteryOptimizationsOff(requireActivity())) {
            binding.root.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.onboarding_splash_background
            )
            OnboardingFragment.transitionToHomeFragment(
                requireContext(),
                requireActivity(),
                sessionId
            )
        }
    }

    private val fragmentWarning : () -> Unit = {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.slide_out,
                R.anim.slide_back_in,
                R.anim.slide_back_out
            )
            replace(
                R.id.container,
                OnboardingWarningFragment.create(sessionId),
                OnboardingWarningFragment.TAG
            )
            addToBackStack(null)
            commit()
        }
    }

    private fun updateView(action: () -> Unit){
        //If the activity is in background we register the transaction
        if(!isActivityResumed){
            lastCall = action
        } else {
            //Else we just invoke it
            action.invoke()
        }
    }

    override fun onActivityResult(requestCode: Int, data: Intent?, resultCode: Int): Boolean {
        super.onActivityResult(requestCode, resultCode, data)
        if (pHandler.onActivityResult(requestCode, data, resultCode)) {
            OnboardingThanksFragment.transitionToFragment(requireActivity(), sessionId)
        }
        else {
            updateView(fragmentWarning)
        }
        return true
    }

    companion object {
        private const val SESSION_ID = "session_id"

        @JvmStatic
        protected fun Bundle.putSessionId(sessionId: String?) {
            putString(SESSION_ID, sessionId)
        }

        const val TAG = "ONBOARD_BATTERY"
        fun create(sessionId: String? = null) = OnboardingBatteryFragment().apply {
            arguments = Bundle().apply {
                putSessionId(sessionId)
            }
        }
    }

}