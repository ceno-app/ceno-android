package ie.equalit.ceno.utils

import android.content.Context
import android.content.res.XmlResourceParser
import ie.equalit.ceno.ext.extractATags
import ie.equalit.ceno.home.RssAnnouncementResponse
import ie.equalit.ceno.home.RssItem
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader

object XMLParser {

    fun parseRssXml(xmlString: String): RssAnnouncementResponse? {

        try {
            var index = 0

            // Replace all a-tags in the description string with a placeholder string
            var formattedXML: String? = xmlString
            val descriptionUrls = xmlString.extractATags()
            descriptionUrls.forEach {
                formattedXML = formattedXML?.replace(it, CENO_CUSTOM_PLACEHOLDER)
            }

            // Initialize parser objects for processing the XML String
            val factory: XmlPullParserFactory = XmlPullParserFactory.newInstance()
            val parser: XmlPullParser = factory.newPullParser()

            parser.setInput(StringReader(formattedXML))

            // Variable for tracking the current tag while looping across the XML String
            var tag = ""

            var currentRssItem: RssItem? = null
            val rssFeedItems = mutableListOf<RssItem>()

            var rssFeedTitle = ""
            var rssFeedLink = ""
            var rssFeedDescription = ""

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        tag = parser.name
                        when (tag) {
                            "item" -> {
                                currentRssItem = RssItem(
                                    "",
                                    "",
                                    "",
                                    "",
                                    ""
                                )
                            }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        val text = parser.text.trim()
                        when (tag) {
                            "title" -> {
                                if (currentRssItem == null) {
                                    rssFeedTitle = text
                                } else {
                                    currentRssItem.title = text
                                }
                            }

                            "link" -> {
                                if (currentRssItem == null) {
                                    rssFeedLink = text
                                } else {
                                    currentRssItem.link = text
                                }
                            }

                            "description" -> {
                                if (currentRssItem == null) {
                                    rssFeedDescription = text
                                } else {
                                    val occurrences = text.split(CENO_CUSTOM_PLACEHOLDER).size - 1
                                    var result = text
                                    for (i in 0 until occurrences) {
                                        result = result.replaceFirst(
                                            CENO_CUSTOM_PLACEHOLDER,
                                            descriptionUrls[index]
                                        )
                                        index++
                                    }
                                    currentRssItem.description = result
                                }
                            }

                            "guid" -> {
                                currentRssItem?.guid = text
                            }

                            "pubDate" -> {
                                currentRssItem?.pubDate = text
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        tag = ""
                        when (parser.name) {
                            "item" -> {
                                currentRssItem?.let {
                                    rssFeedItems.add(it)
                                }
                            }

                            else -> {} // do nothing
                        }
                    }
                }
            }


            // If any of the vital fields is null, return a null response, thus hiding the view

            if (rssFeedTitle.isEmpty()
                || rssFeedLink.isEmpty()
                || rssFeedDescription.isEmpty()
                || rssFeedItems.isEmpty()
            ) {
                return null
            }

            return RssAnnouncementResponse(
                rssFeedTitle,
                rssFeedLink,
                rssFeedDescription,
                rssFeedItems
            )
        } catch (_: XmlPullParserException) {
            return null
        }
    }

    fun parseTopsitesXml(
        parser: XmlResourceParser,
        context: Context
    ): MutableList<Pair<String, String>>? {

        try {
            var currentTopsiteUrl = ""
            var currentTopsiteTitle = ""
            val topsiteItems = mutableListOf<Pair<String, String>>()

            var tag = ""
            var isTitleFromAttribute = false

            while (parser.next() != XmlPullParser.END_DOCUMENT) {
                when (parser.eventType) {
                    XmlPullParser.START_TAG -> {
                        tag = parser.name
                        when (tag) {
                            "topsite" -> {
                                currentTopsiteTitle = ""
                                currentTopsiteUrl = ""
                            }

                            "title" -> {
                                if (parser.attributeCount > 0) {
                                    val stringResource = parser.getAttributeResourceValue(0, 0)
                                    currentTopsiteTitle = context.getString(stringResource)
                                    isTitleFromAttribute = true
                                }
                            }
                        }
                    }

                    XmlPullParser.TEXT -> {
                        when (tag) {
                            "title" -> {
                                if (!isTitleFromAttribute)
                                    currentTopsiteTitle = parser.text.trim()
                                isTitleFromAttribute = false
                            }

                            "url" -> {
                                currentTopsiteUrl = parser.text.trim()
                            }
                        }
                    }

                    XmlPullParser.END_TAG -> {
                        tag = ""
                        when (parser.name) {
                            "topsite" -> {
                                topsiteItems.add(
                                    Pair(currentTopsiteTitle, currentTopsiteUrl)
                                )
                            }

                            else -> {}
                        }
                    }
                }
            }
            return topsiteItems
        } catch (_: XmlPullParserException) {
            return null
        }
    }

    const val CENO_CUSTOM_PLACEHOLDER = "ceno_custom_placeholder"
}
