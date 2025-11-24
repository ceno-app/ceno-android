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
import ie.equalit.ceno.BrowserActivity
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.FragmentSiteContentGroupBinding
import ie.equalit.ceno.settings.adapters.CachedGroupAdapter
import kotlinx.coroutines.Job

class SiteContentGroupFragment : Fragment(), CachedGroupAdapter.GroupClickListener {

    private var _binding: FragmentSiteContentGroupBinding? = null
    private val binding get() = _binding!!
    private var job: Job? = null

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
            /*binding.downloadButton.isGone = false
            binding.downloadButton.setOnClickListener {
                // confirmation nudge
                AlertDialog.Builder(requireContext()).apply {
                    setTitle(getString(R.string.confirm_groups_file_download))
                    setMessage(getString(R.string.confirm_groups_file_download_desc))
                    setNegativeButton(getString(R.string.ceno_clear_dialog_cancel)) { _, _ -> }
                    setPositiveButton(getString(R.string.onboarding_battery_button)) { _, _ ->
                        downloadGroups()
                    }
                    create()
                }.show()
            }*/
        }

    }

    private fun convertToMap(groups: String): List<CachedGroupAdapter.GroupItem> {

        val urls = groups.split("\n")

        val map = mutableMapOf<String, MutableList<String>>()

        for (url in urls) {
            val parts = url.split("/")
            val baseUrl = parts.first()
            val subUrl = "$baseUrl/" + parts.drop(1).joinToString("/")

            map[baseUrl] = if (map[baseUrl].isNullOrEmpty()){
                mutableListOf<String>().apply {
                    add(subUrl)
                }
            } else {
                map[baseUrl].apply { this!!.add(subUrl) }!!
            }
        }

        val result = mutableListOf<CachedGroupAdapter.GroupItem>()
        map.keys.forEach { result.add(CachedGroupAdapter.GroupItem(it, map[it]!!.toList())) }

        return result
    }

    override fun onLinkClicked(url: String) {
        (activity as BrowserActivity).openToBrowser(url = url, newTab = true)
    }

    private fun getActionBar() = (activity as AppCompatActivity).supportActionBar!!

    companion object {
        private const val TAG = "SiteContentGroupFragment"
    }
}