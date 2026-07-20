/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package ie.equalit.ceno.home.telegram_channels

import android.view.View
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import ie.equalit.ceno.R
import ie.equalit.ceno.databinding.TelegramChannelItemBinding
import ie.equalit.ceno.databinding.TopSiteItemBinding
import ie.equalit.ceno.ext.ceno.bitmapForUrl
import ie.equalit.ceno.ext.ceno.loadIntoView
import ie.equalit.ceno.ext.components
import ie.equalit.ceno.home.sessioncontrol.TopSiteInteractor
import ie.equalit.ceno.home.topsites.TopSiteItemMenu
import ie.equalit.ceno.utils.view.CenoViewHolder
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mozilla.components.feature.top.sites.TopSite

class TelegramChannelItemViewHolder(
    view: View,
    private val viewLifecycleOwner: LifecycleOwner,
    private val interactor: TopSiteInteractor
) : CenoViewHolder(view) {
    private lateinit var topSite: TopSite
    private val binding = TelegramChannelItemBinding.bind(view)

    @Suppress("LongMethod")
    fun bind(topSite: TopSite, position: Int) {
        binding.topSiteTitle.text = topSite.title
//        binding.topSiteTitle.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null)

        if (topSite is TopSite.Provided) {
            binding.topSiteSubtitle.isVisible = true

            viewLifecycleOwner.lifecycleScope.launch(IO) {
                itemView.context.components.core.client.bitmapForUrl(
                    url = topSite.imageUrl,
                )
                    ?.let { bitmap ->
                        withContext(Main) {
                            binding.faviconImage.setImageBitmap(bitmap)
                        }
                    }
            }
        } else {
            /* CENO: Load built-in icons for suggested telegram channels */
            val resources = itemView.context.resources
            when (topSite.url) {
                resources.getString(R.string.telegramchannel_bbcpersion_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_bbcpersian
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_euronewspe_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_euronewspe
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_Farsi_Iranwire_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_farsi_iranwire
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_Hengaw_Org_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_hengaw_org
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_hranews_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_hranews
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_IranintlTV_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_iranintltv
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_ManotoTV_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_manototv
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_paskoocheh_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_paskoocheh
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_radiofarda_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_radiofarda
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_tavaanatech_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_tavaanatech
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_VahidOnline_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_vahidonline
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_agentstvonews_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_agentstvonews
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_ArkHelps_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_arkhelps
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_asiansofrussia_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_asiansofrussia
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_cenochannel_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_ceno_channel
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_CorruptionTV_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_corruptiontv
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_deptone_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_deptone
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_eschulmann_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_eschulmann
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_femagainstwar_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_femagainstwar
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_freeburyatiafoundation_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_freeburyatiafoundation
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_iditelesom_help_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_iditelesom_help
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_kedr_media_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_kedr_media
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_kosa_media_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_kosa_media
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_meduzalive_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_meduzalive
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_no_torture_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_no_torture
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_novaya_pishet_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_novaya_pishet
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_ovdinfo_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_ovdinfo
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_parniplus_com_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_parniplus_com
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_peaceplea_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_peaceplea
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_russianlgbtnet_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_russianlgbtnet
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_spherequeer_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_spherequeer
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_truestorymedia_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_truestorymedia
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_zatelecom_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_zatelecom
                        )
                    )
                }
                resources.getString(R.string.telegramchannel_aljazeeraenglishnews_url) -> {
                    binding.faviconImage.setImageDrawable(
                        getDrawable(
                            itemView.context,
                            R.drawable.telegramchannel_aljazeeraenglishnews
                        )
                    )
                }
            }
        }

        this.topSite = topSite
    }
}
