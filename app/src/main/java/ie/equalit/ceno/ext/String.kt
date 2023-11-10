/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 *  License, v. 2.0. If a copy of the MPL was not distributed with this
 *  file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package ie.equalit.ceno.ext

/**
 * Replaces the keys with the values with the map provided.
 */
fun String.replace(pairs: Map<String, String>): String {
    var result = this
    pairs.iterator().forEach { (l, r) -> result = result.replace(l, r) }
    return result
}

fun String.extractLinks(): List<String> {
    val pattern = Regex("""<a[^>]*>.*?</a>""")
    val matches = pattern.findAll(this)
    return matches.map { it.value }.toList()
}