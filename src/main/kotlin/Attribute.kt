/**
 * This class represents the attributes in Entity or Tag in XML Document;
 * One logic is configured, make sure the name of attribute it is according to XML rule.
 *
 * @property name is a string representing the name of attribute.
 * @property value is a string represented the value of attribute will be assigned.
 * @throws IllegalArgumentException If the attribute name is invalid.
 * @constructor Creates an attribute with name and value.
 */
class Attribute(var name: String, var value: String) {

    /**
     * Init function to check the name of attribute when new object attribute is created.
     */
    init {
        if(!validateAttributeName(this.name)){
            throw IllegalArgumentException("The name of Attribute is invalid.")
        }
    }

    /**
     * Accepts a visitor to perform operations on the attribute.
     *
     * @param visitor The visitor to accept.
     */
    fun accept(visitor: Visitor) {
        visitor.visitAttribute(this)
    }

    /**
     * Function to check the name of attribute according to regex.
     * @param name is a string to be validated.
     * @return the boolean result according to regular expression that Only allow alphanumeric characters, underscores, and dash.
     */
    // Regex to match alphanumeric characters, underscores and dash is allowed
    private fun validateAttributeName(name: String): Boolean {
        val regex = Regex("[a-zA-Z0-9_-]+") // Only allow alphanumeric characters and underscores
        return regex.matches(name)
    }
}
