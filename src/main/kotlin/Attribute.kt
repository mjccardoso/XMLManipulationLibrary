/**
 * Represents an attribute of an XML entity.
 *
 * @property name The name of the attribute.
 * @property value The value of the attribute.
 */
class Attribute(var name: String, var value: String) {

    /**
     * Accepts a visitor to perform operations on the attribute.
     *
     * @param visitor The visitor to accept.
     */
    fun accept(visitor: Visitor) {
        visitor.visitAttribute(this)
    }
}
