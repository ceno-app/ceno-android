/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentSiteContentGroupBinding
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.coroutines.Job

class SiteContentGroupFragment : Fragment(), CachedGroupAdapter.GroupClickListener {

    private var _binding: FragmentSiteContentGroupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentSiteContentGroupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        getActionBar().apply {
            show()
            setTitle(R.string.preferences_ceno_groups_count)
            setDisplayHomeAsUpEnabled(true)
            setBackgroundDrawable(
                ContextCompat.getColor(requireContext(), R.color.ceno_action_bar).toDrawable())
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("groups")?.let { groups ->
            binding.groupListing.setAdapter(
                CachedGroupAdapter(
                    requireContext(),
                    convertToMap(
                        groups.trim()
                    ),
                    this
                )
            )
        }

    }

    private fun convertToMap(groups: String): List<CachedGroupAdapter.GroupItem> {

        val urls = groups.split("\n")
        var pinned_urls = mutableListOf<String>()
        CenoSettings.ouinetClientRequest(
            requireContext(),
            lifecycleScope,
            OuinetKey.PINNED_GROUPS,
            OuinetValue.OTHER,
            ouinetResponseListener = object:OuinetResponseListener{
                override fun onSuccess(message: String, data: Any?) {
                    pinned_urls = Json.decodeFromString<PinnedCacheGroup>(message).pinned_groups.toMutableList()
                }
                override fun onError() {

                }
            }
        )

        val map = mutableMapOf<String, MutableList<CachedGroupAdapter.GroupChildItem>>()
        for (url in urls) {
            val parts = url.split("/")
            val baseUrl = parts.first()

            val is_pinned = pinned_urls.contains(url)
            map[baseUrl] = if (map[baseUrl].isNullOrEmpty()){
                mutableListOf<CachedGroupAdapter.GroupChildItem>().apply {
                    add(CachedGroupAdapter.GroupChildItem(url, is_pinned))
                }
            } else {
                map[baseUrl].apply { this!!.add(CachedGroupAdapter.GroupChildItem(url, is_pinned)) }!!
            }
        }

        val result = mutableListOf<CachedGroupAdapter.GroupItem>()
        map.keys.forEach {
            result.add(CachedGroupAdapter.GroupItem(it, map[it]!!.toList()))
        }

        return result
    }

    override fun onLinkClicked(url: String) {
        (activity as BrowserActivity).openToBrowser(url = "https://$url", newTab = true)
    }

    override fun onPinChanged(url: String, isPinned: Boolean) {
        CenoSettings.ouinetClientRequest(
            requireContext(),
            lifecycleScope,
            if (isPinned) OuinetKey.PIN_TO_CACHE else OuinetKey.UNPIN_FROM_CACHE,
            OuinetValue.OTHER,
            url
        )
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "SiteContentGroupFragment"
    }

    @Serializable
    data class PinnedCacheGroup (
        val pinned_groups : Array<String>
    )
}