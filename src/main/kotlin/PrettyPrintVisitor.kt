/**
 * A visitor implementation for pretty-printing XML documents.
 */
class PrettyPrintVisitor : Visitor {
    private val stringBuilder = StringBuilder()
    private var depth = 0

    override fun visitDocument(doc: Document) {
        stringBuilder.append("<?xml version=\"${doc.version}\" encoding=\"${doc.encoding}\"?>\n")
        visitEntity(doc.entityRoot)
    }

    override fun visitEntity(entity: Entity) {
        appendIndent()
        stringBuilder.append("<${entity.name}")

        entity.attributes.forEach { it.accept(this) }

        if (entity.children.isEmpty() && entity.text.isNullOrEmpty()) {
            stringBuilder.append("/>\n")
        } else {
            stringBuilder.append(">")
            if (!entity.text.isNullOrEmpty()) {
                stringBuilder.append(entity.text)
            }
            if (entity.children.isNotEmpty()) {
                stringBuilder.append("\n")
                depth++
                entity.children.forEach { it.accept(this) }
                depth--
                appendIndent()
            }
            stringBuilder.append("</${entity.name}>\n")
        }
    }

    override fun visitAttribute(attribute: Attribute) {
        stringBuilder.append(" ${attribute.name}=\"${attribute.value}\"")
    }

    /**
     * Gets the pretty-printed XML document as a string.
     *
     * @return The pretty-printed XML string.
     */
    fun getPrettyPrintedDocument(): String {
        return stringBuilder.toString()
    }

    private fun appendIndent() {
        stringBuilder.append("  ".repeat(depth))
    }
}
