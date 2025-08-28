package org.jetbrains.plugins.template.weatherApp.ui.components

import org.jetbrains.jewel.ui.painter.PainterProviderScope
import org.jetbrains.jewel.ui.painter.PainterSvgPatchHint
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.IOException
import java.io.StringWriter
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerException
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

object EmbeddedToInlineCssSvgTransformerHint : PainterSvgPatchHint {
    private val CSS_STYLEABLE_TAGS = listOf(
        "linearGradient", "radialGradient", "pattern",
        "filter", "clipPath", "mask", "symbol",
        "marker", "font", "image"
    )

    override fun PainterProviderScope.patch(element: Element) {
        element.inlineEmbeddedStylesCSS()
    }

    private fun Element.inlineEmbeddedStylesCSS(): Element {
        val svgElement = this

        svgElement.moveStyleableElementsToDefsNode(CSS_STYLEABLE_TAGS)

        val cache = svgElement.parseCssDefinitionsInStylesElement()

        svgElement.inlineStyleDeclarations(cache)

        return svgElement
    }
}

private fun Element.getElementsWithAttributeXPath(attributeName: String): List<Element> {
    val xPath = XPathFactory.newInstance().newXPath()

    val eligibleNodes = xPath.evaluate(
        "//*[@$attributeName]",
        this,
        XPathConstants.NODESET
    ) as NodeList

    return buildList {
        for (i in 0 until eligibleNodes.length) {
            eligibleNodes.item(i)
                .let { node -> if (node is Element) add(node) }
        }
    }
}


private fun Element.inlineStyleDeclarations(cache: Map<String, Map<String, String>>) {
    val classAttributeName = "class"
    val styleElementName = "style"

    for (element in getElementsWithAttributeXPath(classAttributeName)) {
        if (element.hasAttribute(classAttributeName)) {
            val cssClassId = element.getAttribute(classAttributeName)
            if (cssClassId.isBlank()) continue

            element.removeAttribute(classAttributeName)

            // Set a new "style" attribute (example value)
            val styleAttributesCache = cache[cssClassId] ?: continue
            val styleAttributes = styleAttributesCache.entries.joinToString(";") { "${it.key}:${it.value}" }
            element.setAttribute(styleElementName, styleAttributes)
        }
    }

    this.getSingleChildElement(styleElementName)
        ?.let { styleNode -> this.removeChild(styleNode) }
}

private fun Element.moveStyleableElementsToDefsNode(stylableElementTags: List<String>) {
    // Find or create <defs> element
    val defs = ensureDefsNodeExists()

    // For each tag, find all elements and move those not already inside defs
    stylableElementTags.forEach { tag ->
        val nodes = getElementsByTagName(tag)
        (0..<nodes.length)
            .map { nodes.item(it) to defs }
            .forEach { (nodeToMove, newParentNode) ->
                if (nodeToMove.parentNode != newParentNode) {
                    newParentNode.appendChild(nodeToMove)
                }
            }
    }
}

/**
 * See: https://www.w3.org/TR/2018/CR-SVG2-20181004/struct.html#DefsElement
 */
private fun Element.ensureDefsNodeExists(): Node {
    var defsNode = getElementsByTagName("defs").item(0)

    if (defsNode == null) {
        defsNode = this.ownerDocument.createElement("defs")
        insertBefore(defsNode, this.firstChild)
    }
    return defsNode
}

private fun Element.parseCssDefinitionsInStylesElement(): Map<String, Map<String, String>> {
    val styleNode = this.getChildElements("style")
        .firstOrNull() ?: return emptyMap()

    val cssClassIdRegex = Regex("""\.([^\s{]+)\s*\{\s*([^}]+)\s*}""")

    return buildMap {
        cssClassIdRegex.findAll(styleNode.textContent).forEach { match ->
            val styleId = match.groups[1]?.value ?: return@forEach
            val styleAttributes = match.groups[2]?.value ?: return@forEach

            val styleAttributesMap = styleAttributes
                .split(";")
                .filter { it.isNotBlank() }
                .associate { attributeKeyValue ->
                    val (key, value) = attributeKeyValue.trim().split(":")
                    key.trim() to value.trim()
                }

            this[styleId] = styleAttributesMap
        }
    }
}

private fun Element.getChildElements(tagName: String): List<Element> {
    val childNodes = childNodes
    val result = ArrayList<Element>()
    for (i in 0 until childNodes.length) {
        val node = childNodes.item(i)
        if (node is Element && tagName == node.tagName) {
            result.add(node)
        }
    }
    return result
}

private fun Element.getSingleChildElement(tagName: String): Element? {
    return getChildElements(tagName).getOrNull(0)
}

private class PrintableElement(private val element: Element) {

    fun writeToString(): String {
        return element.ownerDocument.writeToString()
    }

    private fun Document.writeToString(): String {
        val tf = TransformerFactory.newInstance()
        val transformer: Transformer

        try {
            transformer = tf.newTransformer()
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")

            val writer = StringWriter()
            transformer.transform(DOMSource(this), StreamResult(writer))
            return writer.buffer.toString()
        } catch (e: TransformerException) {
            error("Unable to render XML document to string: ${e.message}")
        } catch (e: IOException) {
            error("Unable to render XML document to string: ${e.message}")
        }
    }
}
