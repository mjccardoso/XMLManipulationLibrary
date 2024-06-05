/**
 * This is an Entity Class, represent a tag in the xml document.
 * instance should inform the name
 * after that in the class are couple of methods useful to manipulate the class according to the logic requested
 * @property name is a string representing the name of entity.
 * @property text is a string optionally informing the text as content of the tag.
 * @property attributes inform the attributes, is mutableList of an Attribute type.
 * @property children children is a member of tag - represent the list of entity or tags.
 * @property parent the last one is parent this important to inform if the entity belongs a specific tag
 * @constructor Creates an entity with name.
 */
class Entity(var name: String) {

    var text: String? = ""
    var attributes = mutableListOf<Attribute>()
    var children = mutableListOf<Entity>()
    var parent: Entity? = null

    init {
        parametersVerification()
    }

    /**
     * Accepts a visitor to perform operations on the entity.
     *
     * @param visitor The visitor to accept.
     */
    fun accept(visitor: Visitor) {
        visitor.visitEntity(this)
    }

    /**
     * Adds a child entity.
     *
     * @param newChild The child entity to add.
     * @throws IllegalArgumentException If the child is a descendant of the current entity or an equivalent entity already exists.
     */
    fun addChildren(newChild: Entity) {
        if (newChild == this || newChild.isDescendantOf(this)) {
            throw IllegalArgumentException("Invalid adding: Cannot add a child that is a descendant")
        }
        if (this.text?.isNotEmpty() == true) {
            throw IllegalArgumentException("Invalid Adding: The Entity $name with text should not have children.")
        }
        if (this.containsEquivalent(newChild)) {
            throw IllegalArgumentException("Invalid adding: An equivalent entity already exists in the structure")
        }
        children.add(newChild)
        newChild.parent = this
    }

    /**
     * Sets the parent entity.
     *
     * @param defineParent The parent entity to set.
     */
//    fun setParent(defineParent: Entity?) {
//        this.parent = defineParent
//    }

    /**
     * Removes child entities by name.
     *
     * @param removeEntityName The name of the entity to remove.
     * @throws IllegalArgumentException If the entity name is empty.
     * @throws NoSuchElementException If no entity is found with the specified name.
     */
    fun removeChildren(removeEntityName: String) {
        if (removeEntityName.isEmpty()) {
            throw IllegalArgumentException("Entity name cannot be empty.")
        }
        val wasRemoved = removeChildrenRecursively(this, removeEntityName)
        if (!wasRemoved) {
            throw NoSuchElementException("No entity found with the name: $removeEntityName")
        }
    }

    /**
     * Renames child entities.
     *
     * @param oldEntityName The old name of the entity.
     * @param newEntityName The new name of the entity.
     * @throws IllegalArgumentException If the entity names are empty or the names are the same.
     * @throws NoSuchElementException If no children with the old name are found.
     */
    fun renameChildren(oldEntityName: String, newEntityName: String) {
        if (oldEntityName.isEmpty() || newEntityName.isEmpty()) {
            throw IllegalArgumentException("Entity names cannot be empty.")
        }

        if (oldEntityName == newEntityName) {
            throw IllegalArgumentException("New entity name must be different from the old entity name.")
        }

        val filteredChildren = this.children.filter { it.name == oldEntityName }

        if (filteredChildren.isEmpty()) {
            throw NoSuchElementException("No children with the name '$oldEntityName' were found.")
        }

        filteredChildren.forEach { it.name = newEntityName }
        this.children.forEach { it.renameChildren(oldEntityName, newEntityName) }
    }

    /**
     * Adds an attribute to the entity.
     *
     * @param newAttribute The attribute to add.
     * @throws IllegalArgumentException If the attribute already exists.
     */
    fun addAttribute(newAttribute: Attribute) {
        val existingAttribute = attributes.find { it.name == newAttribute.name }
        if (existingAttribute != null) {
            throw IllegalArgumentException("Attribute '${newAttribute.name}' already exists in entity '${this.name}'.")
        } else {
            attributes.add(newAttribute)
        }
    }

    /**
     * Adds an attribute recursively to all child entities.
     *
     * @param attribute The attribute to add.
     */
    fun addAttributeRecursively(attribute: Attribute) {
        addAttributeIfNotExist(attribute)
        children.forEach { it.addAttributeRecursively(attribute) }
    }

    /**
     * Removes an attribute recursively from all child entities.
     *
     * @param attributeName The name of the attribute to remove.
     * @throws IllegalArgumentException If the attribute name is empty.
     * @throws NoSuchElementException If no attribute is found with the specified name.
     */
    fun removeAttributeRecursively(attributeName: String) {
        if (attributeName.isEmpty()) {
            throw IllegalArgumentException("Attribute name cannot be empty.")
        }

        val wasRemoved = removeAttribute(attributeName)
        children.forEach { it.removeAttributeRecursively(attributeName) }

        if (!wasRemoved && children.all { !it.attributeRemovedRecently }) {
            throw NoSuchElementException("No attribute found with the name: $attributeName to remove.")
        }
    }

    var attributeRemovedRecently = false

    fun removeAttribute(attributeName: String): Boolean {
        val initialSize = attributes.size
        attributes.removeIf { it.name == attributeName }
        attributeRemovedRecently = attributes.size != initialSize
        return attributeRemovedRecently
    }

    /**
     * Renames an attribute recursively in all child entities.
     *
     * @param oldName The old name of the attribute.
     * @param newName The new name of the attribute.
     * @throws IllegalArgumentException If the attribute names are empty or the names are the same.
     * @throws NoSuchElementException If no attribute is found with the old name to rename.
     */
    fun renameAttributeRecursively(oldName: String, newName: String) {
        if (oldName.isEmpty() || newName.isEmpty()) {
            throw IllegalArgumentException("Attribute names cannot be empty.")
        }
        if (oldName == newName) {
            throw IllegalArgumentException("New attribute name must be different from the old name.")
        }

        val wasRenamed = renameAttribute(oldName, newName)
        children.forEach { it.renameAttributeRecursively(oldName, newName) }

        if (!wasRenamed && children.all { !it.attributeRenamedRecently }) {
            throw NoSuchElementException("No attribute found with the name '$oldName' to rename.")
        }
    }

    private var attributeRenamedRecently = false

    private fun renameAttribute(oldName: String, newName: String): Boolean {
        val attribute = attributes.find { it.name == oldName }
        if (attribute != null) {
            if (attributes.any { it.name == newName }) {
                throw IllegalArgumentException("An attribute with the new name '$newName' already exists.")
            }
            attribute.name = newName
            attributeRenamedRecently = true
            return true
        }
        attributeRenamedRecently = false
        return false
    }

    /**
     * Updates the name of an attribute.
     *
     * @param oldName The old name of the attribute.
     * @param newName The new name of the attribute.
     * @throws IllegalArgumentException If the new name is empty or already exists.
     * @throws NoSuchElementException If the attribute with the old name is not found.
     */
    fun updateAttributeName(oldName: String, newName: String) {
        if (newName.isEmpty() || newName.toString() != "") {
            throw IllegalArgumentException("New attribute name cannot be empty.")
        }
        if (attributes.any { it.name == newName }) {
            throw IllegalArgumentException("An attribute with the name '$newName' already exists.")
        }
        val attribute = attributes.find { it.name == oldName }
            ?: throw NoSuchElementException("Attribute with name '$oldName' not found.")

        attribute.name = newName
    }

    /**
     * Updates the value of an attribute.
     *
     * @param attributeName The name of the attribute.
     * @param newValue The new value of the attribute.
     * @throws IllegalArgumentException If the attribute with the specified name is not found.
     */
    fun updateAttributeValue(attributeName: String, newValue: String) {
        val attribute = attributes.find { it.name == attributeName }
            ?: throw IllegalArgumentException("Attribute with name '$attributeName' not found.")

        attribute.value = newValue
    }

    fun numberOfAttribute(): Int {
        return attributes.size
    }

    /**
     * Generates an XML string representation of the entity.
     *
     * @return The XML string representation of the entity.
     */
    fun toXmlString(): String {
        val attributesString = attributes.joinToString(" ") { "${it.name}=\"${it.value}\"" }
        return if (children.isEmpty() && text.isNullOrEmpty()) {
            if (attributesString.isEmpty()) {
                "<$name/>"
            } else {
                "<$name $attributesString/>"
            }
        } else {
            val childrenString = children.joinToString("") { it.toXmlString() }
            val content = (text ?: "") + childrenString
            if (attributesString.isEmpty()) {
                "<$name>$content</$name>"
            } else {
                "<$name $attributesString>$content</$name>"
            }
        }
    }

    /**
     * Checks if the entity has an attribute with the specified name.
     *
     * @param attributeName The name of the attribute.
     * @return True if the entity has the attribute, false otherwise.
     */
    fun hasAttribute(attributeName: String): Boolean {
        return hasAttributeRecursively(this, attributeName)
    }

    private fun hasAttributeRecursively(entity: Entity, attributeName: String): Boolean {
        if (entity.attributes.any { it.name == attributeName }) {
            return true
        }
        for (child in entity.children) {
            if (hasAttributeRecursively(child, attributeName)) {
                return true
            }
        }
        return false
    }

    /**
     * Prints the structure of the entity with attributes.
     *
     * @param prefix The prefix to use for indentation.
     */
    fun printStructureWithAttributes(prefix: String = "") {
        println("$prefix$name ${text.orEmpty()}")
        attributes.forEach { attribute ->
            println("$prefix ${attribute.name}: ${attribute.value}")
        }
        children.forEach { child ->
            child.printStructureWithAttributes("$prefix  ")
        }
    }

    // AUX FUNCs -------------------------------------------------------------------------------------------

    private fun parametersVerification() {
        val allowedNameRegex = "^[a-zA-Z][a-zA-Z0-9]+$".toRegex()
        if (!allowedNameRegex.matches(name)) {
            throw IllegalArgumentException("Invalid name: Names can only start with letters and can only contain letters, numbers.")
        }
    }

    private fun isDescendantOf(entity: Entity): Boolean {
        var currentEntity = this
        while (currentEntity.parent != null) {
            currentEntity = currentEntity.parent!!
            if (currentEntity == entity) {
                return true
            }
        }
        return false
    }

    fun isEquivalent(other: Entity): Boolean {
        if (this.name != other.name) return false
        if (this.name == other.name && this.text != other.text) return false

        if (this.attributes.size != other.attributes.size) return false

        return other.attributes.all { otherAttr ->
            this.attributes.any { thisAttr ->
                thisAttr.name == otherAttr.name && thisAttr.value == otherAttr.value
            }
        }
    }

    fun containsEquivalent(entity: Entity): Boolean {
        if (this.isEquivalent(entity)) {
            return true
        }
        return children.any { it.containsEquivalent(entity) }
    }

    fun contains(entity: Entity): Boolean {
        if (this == entity) {
            return true
        }
        return children.any { it.contains(entity) }
    }

    private fun removeChildrenRecursively(entity: Entity, removeEntityName: String): Boolean {
        val childrenToRemove = entity.children.filter { it.name == removeEntityName }
        if (childrenToRemove.isEmpty() && entity.children.none { removeChildrenRecursively(it, removeEntityName) }) {
            return false
        }
        entity.children.removeAll(childrenToRemove)
        childrenToRemove.forEach { it.parent = null }
        entity.children.forEach { removeChildrenRecursively(it, removeEntityName) }
        return true
    }

    private fun addAttributeIfNotExist(attribute: Attribute) {
        if (attributes.none { it.name == attribute.name }) {
            attributes.add(attribute)
        } else {
            val existingAttribute = attributes.first { it.name == attribute.name }
            existingAttribute.value = attribute.value
        }
    }

    /**
     * Prints the entity and its children as an XML string.
     *
     * @param entity The entity to print.
     * @param stringBuilder The StringBuilder to append the XML string to.
     * @param depth The current depth of the entity in the hierarchy.
     */
    fun printEntity(entity: Entity, stringBuilder: StringBuilder, depth: Int) {
        val indent = "  ".repeat(depth)
        stringBuilder.append("$indent<${entity.name}")

        entity.attributes.forEach { attr ->
            stringBuilder.append(" ${attr.name}=\"${attr.value}\"")
        }

        if (entity.children.isEmpty() && entity.text.isNullOrEmpty()) {
            stringBuilder.append("/>\n")
        } else {
            stringBuilder.append(">")
            if (!entity.text.isNullOrEmpty()) {
                stringBuilder.append("${entity.text}")
            }
            if (entity.children.isNotEmpty()) {
                stringBuilder.append("\n")
                entity.children.forEach { child ->
                    printEntity(child, stringBuilder, depth + 1)
                }
                stringBuilder.append(indent)
            }
            stringBuilder.append("</${entity.name}>\n")
        }
    }
}
