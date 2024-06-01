/**
 * Defines the visitor interface for traversing and operating on XML documents.
 */
interface Visitor {

    /**
     * Visits a document.
     *
     * @param doc The document to visit.
     */
    fun visitDocument(doc: Document)

    /**
     * Visits an entity.
     *
     * @param entity The entity to visit.
     */
    fun visitEntity(entity: Entity)

    /**
     * Visits an attribute.
     *
     * @param attribute The attribute to visit.
     */
    fun visitAttribute(attribute: Attribute)
}
