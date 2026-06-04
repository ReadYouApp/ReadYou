package me.ash.reader.infrastructure.html

import android.util.Log
import net.dankito.readability4j.extended.Readability4JExtended
import net.dankito.readability4j.extended.processor.PostprocessorExtended
import net.dankito.readability4j.extended.util.RegExUtilExtended
import net.dankito.readability4j.model.ReadabilityOptions
import net.dankito.readability4j.processor.MetadataParser
import net.dankito.readability4j.processor.Preprocessor
import org.jsoup.nodes.Element

object Readability {

    fun parseToText(htmlContent: String?, uri: String?): String {
        htmlContent ?: return ""
        return try {
            Readability4JExtended(uri, htmlContent).parse().textContent?.trim() ?: ""
        } catch (e: Exception) {
            Log.e("RLog", "Readability.parseToText '$uri' is error: ", e)
            ""
        }
    }

    fun parseToElement(htmlContent: String?, uri: String?): Element? {
        htmlContent ?: return null
        return Readability4JExtended(uri, htmlContent).parse().articleContent
    }

    private fun Readability4JExtended(uri: String?, html: String): Readability4JExtended {
        val options = ReadabilityOptions()
        val regExUtil = RegExUtilExtended()
        return Readability4JExtended(
            uri = uri ?: "",
            html = html,
            options = options,
            regExUtil = regExUtil,
            preprocessor = Preprocessor(regExUtil),
            metadataParser = MetadataParser(regExUtil),
            articleGrabber = RYArticleGrabberExtended(options, regExUtil),
            postprocessor = PostprocessorExtended(),
        )
    }
}
