interface Visitor {
    fun visitDocument(doc: Document)
    fun visitEntity(entity: Entity)
    fun visitAttribute(attribute: Attribute)
}