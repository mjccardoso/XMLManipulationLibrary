class Attribute (var name: String, var value: String) {
    fun accept(visitor: Visitor) {
        visitor.visitAttribute(this)
    }
}