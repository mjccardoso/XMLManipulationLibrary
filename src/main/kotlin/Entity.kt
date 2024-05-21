class Entity (var name: String) {
    init {
        parametersVerification()
    }
    var text: String? = ""
    val attributes = mutableListOf<Attribute>()
    val children = mutableListOf<Entity>()
    private var parent: Entity? = null

    fun accept(visitor: Visitor) {
        visitor.visitEntity(this)
    }

    // MAIN FUNCs ------------------------------------------------------------------------------------------

    fun addChildren(newChild: Entity) {
        if (newChild == this || newChild.isDescendantOf(this)) {
            throw IllegalArgumentException("Invalid adding: Cannot add a child that is a descendant")
        }
        if (this.text?.isNotEmpty() == true) {
            throw IllegalStateException("Invalid Adding: The Entity $name with text should not have children.")
        }
        if (this.containsEquivalent(newChild)) {
            throw IllegalArgumentException("Invalid adding: An equivalent entity already exists in the structure")
        }
        children.add(newChild)
        newChild.parent = this
    }

    fun setParent(defineParent: Entity?) {
        this.parent = defineParent
    }

    fun removeChildren(removeEntityName: String) {
        if (removeEntityName.isEmpty()) {
            throw IllegalArgumentException("Entity name cannot be empty.")
        }
        val wasRemoved = removeChildrenRecursively(this, removeEntityName)
        if (!wasRemoved) {
            throw NoSuchElementException("No entity found with the name: $removeEntityName")
        }
    }

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

    fun addAttribute(newAttribute: Attribute) {
        val existingAttribute = attributes.find { it.name == newAttribute.name }
        if (existingAttribute != null) {
            throw IllegalArgumentException("Attribute '${newAttribute.name}' already exists in entity '${this.name}'.")
        } else {
            attributes.add(newAttribute)
        }
    }

    fun addAttributeRecursively(attribute: Attribute) {
        addAttributeIfNotExist(attribute)
        children.forEach { it.addAttributeRecursively(attribute) }
    }


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

    // Helper flag to indicate if an attribute was recently removed in the current recursive call
    private var attributeRemovedRecently = false

    private fun removeAttribute(attributeName: String): Boolean {
        val initialSize = attributes.size
        attributes.removeIf { it.name == attributeName }
        attributeRemovedRecently = attributes.size != initialSize
        return attributeRemovedRecently
    }

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

    fun updateAttributeValue(attributeName: String, newValue: String) {
        val attribute = attributes.find { it.name == attributeName }
            ?: throw NoSuchElementException("Attribute with name '$attributeName' not found.")

        attribute.value = newValue
    }


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

    private fun isEquivalent(other: Entity): Boolean {
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

    fun printStructureWithAttributes(prefix: String = "") {

        println("$prefix$name ${text.orEmpty()}")

        attributes.forEach { attribute ->
            println("$prefix ${attribute.name}: ${attribute.value}")
        }
        children.forEach { child ->
            child.printStructureWithAttributes("$prefix  ")
        }
    }
}