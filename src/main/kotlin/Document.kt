import java.io.File

/**
 * Represents an XML document.
 *
 * @property version The version of the XML document.
 * @property encoding The encoding used for the XML document.
 * @property entityRoot The root entity of the XML document.
 */
class Document(val version: String = "1.0", val encoding: String = "UTF-8", val entityRoot: Entity) {

    init {
        parametersVerification()
    }

    /**
     * Accepts a visitor to perform operations on the document.
     *
     * @param visitor The visitor to accept.
     */
    fun accept(visitor: Visitor) {
        visitor.visitDocument(this)
    }

    /**
     * Adds an entity to the document.
     *
     * @param entity The entity to add.
     * @throws IllegalArgumentException If an equivalent entity already exists.
     */
    fun addEntity(entity: Entity) {
        if (entityRoot.containsEquivalent(entity)) {
            throw IllegalArgumentException("Invalid Entity: An equivalent entity already exists in the document")
        }
        entityRoot.addChildren(entity)
        entity.setParent(entityRoot)
    }

    /**
     * Removes entities globally by name.
     *
     * @param entityName The name of the entity to remove.
     */
    fun removeEntitiesGlobally(entityName: String) {
        removeEntitiesRecursively(entityRoot, entityName)
    }

    private fun removeEntitiesRecursively(entity: Entity, entityName: String) {
        entity.removeChildren(entityName)
    }

    /**
     * Renames entities globally by name.
     *
     * @param oldEntityName The old name of the entity.
     * @param newEntityName The new name of the entity.
     */
    fun renameEntity(oldEntityName: String, newEntityName: String) {
        entityRoot.renameChildren(oldEntityName, newEntityName)
    }

    /**
     * Adds an attribute globally to all entities in the document.
     *
     * @param attribute The attribute to add.
     */
    fun addGlobalAttribute(attribute: Attribute) {
        entityRoot.addAttributeRecursively(attribute)
    }

    /**
     * Removes an attribute globally by name.
     *
     * @param attributeName The name of the attribute to remove.
     */
    fun removeGlobalAttribute(attributeName: String) {
        entityRoot.removeAttributeRecursively(attributeName)
    }

    /**
     * Renames an attribute globally by name.
     *
     * @param oldName The old name of the attribute.
     * @param newName The new name of the attribute.
     */
    fun renameGlobalAttribute(oldName: String, newName: String) {
        entityRoot.renameAttributeRecursively(oldName, newName)
    }

    /**
     * Updates the name of an attribute globally.
     *
     * @param oldName The old name of the attribute.
     * @param newName The new name of the attribute.
     * @throws IllegalArgumentException If the new attribute name is empty.
     */
    fun updateGlobalAttributeName(oldName: String, newName: String) {
        if (newName.isEmpty()) {
            throw IllegalArgumentException("New attribute name cannot be empty.")
        }
        updateAttributeNameRecursively(entityRoot, oldName, newName)
    }

    /**
     * Updates the value of an attribute globally.
     *
     * @param attributeName The name of the attribute.
     * @param newValue The new value of the attribute.
     */
    fun updateGlobalAttributeValue(attributeName: String, newValue: String) {
        updateAttributeValueRecursively(entityRoot, attributeName, newValue)
    }

    private fun updateAttributeNameRecursively(entity: Entity, oldName: String, newName: String) {
        try {
            entity.updateAttributeName(oldName, newName)
        } catch (e: NoSuchElementException) {
            // Ignore if the attribute isn't found in the current entity
        }
        entity.children.forEach {
            updateAttributeNameRecursively(it, oldName, newName)
        }
    }

    private fun updateAttributeValueRecursively(entity: Entity, attributeName: String, newValue: String) {
        try {
            entity.updateAttributeValue(attributeName, newValue)
        } catch (e: NoSuchElementException) {
            // Ignore if the attribute isn't found in the current entity
        }
        entity.children.forEach {
            updateAttributeValueRecursively(it, attributeName, newValue)
        }
    }

    /**
     * Generates a pretty-printed XML string representation of the document.
     *
     * @param document The document to pretty print.
     * @return The pretty-printed XML string.
     */
    fun prettyPrintDocument(document: Document): String {
        val stringBuilder = StringBuilder()
        stringBuilder.append("<?xml version=\"1.0\" encoding=\"${document.encoding}\"?>\n")
        entityRoot.printEntity(document.entityRoot, stringBuilder, 0)
        return stringBuilder.toString()
    }

    /**
     * Saves the XML document content to a file.
     *
     * @param documentContent The content of the document to save.
     * @param filePath The file path where the document will be saved.
     */
    fun saveDocumentToFile(documentContent: String, filePath: String) {
        try {
            File(filePath).writeText(documentContent)
            println("Document saved successfully to $filePath")
        } catch (e: Exception) {
            println("Failed to save document: ${e.message}")
        }
    }

    /**
     * Queries the document using a simple XPath-like expression.
     *
     * @param expression The XPath expression.
     * @return A list of XML string fragments that match the expression.
     */
    fun queryMicroXPath(expression: String): List<String> {
        val parts = expression.split("/")
        val entities = queryEntitiesRecursively(entityRoot, parts, 0)
        return entities.map { it.toXmlString() }
    }

    // AUX FUNCs -------------------------------------------------------------------------------------------
    private fun queryEntitiesRecursively(entity: Entity, parts: List<String>, index: Int): List<Entity> {
        if (index >= parts.size) return emptyList()
        val currentPart = parts[index]
        val matchedEntities = entity.children.filter { it.name == currentPart }

        if (index == parts.size - 1) {
            return matchedEntities
        }

        val result = mutableListOf<Entity>()
        matchedEntities.forEach { matchedEntity ->
            result.addAll(queryEntitiesRecursively(matchedEntity, parts, index + 1))
        }
        return result
    }

    private fun parametersVerification() {
        val allowedVersionNameRegex = "^\\d\\.\\d?$".toRegex()
        val allowedVersion: List<String> = listOf("1.0")
        val allowedEncoding: List<String> = listOf("UTF-8")

        if (!allowedVersion.contains(version) || !allowedVersionNameRegex.matches(version))
            throw IllegalArgumentException("Invalid version parameter: $version. This API Library only prepared for the standard version 1.0")

        if (!allowedEncoding.contains(encoding))
            throw IllegalArgumentException("Invalid encoding parameter: $encoding. This API Library only prepared for the Encoding 'UTF-8', others will be added in future versions")
    }

    /**
     * Prints the structure of the document.
     */
    fun printDocumentStructure() {
        println("Document (Version: $version, Encoding: $encoding)")
        entityRoot.printStructureWithAttributes()
    }
}
