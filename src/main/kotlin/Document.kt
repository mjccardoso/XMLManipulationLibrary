import java.io.File

class Document (val version: String = "1.0", val encoding: String = "UTF-8", val entityRoot: Entity) {
    init {
        parametersVerification()
    }

    fun accept(visitor: Visitor) {
        visitor.visitDocument(this)
    }

    // MAIN FUNCs ------------------------------------------------------------------------------------------

    fun addEntity(entity: Entity) {
        if (entityRoot.containsEquivalent(entity)) {
            throw IllegalArgumentException("Invalid Entity: An equivalent entity already exists in the document")
        }
        entityRoot.addChildren(entity)
        entity.setParent(entityRoot)
    }

    fun removeEntitiesGlobally(entityName: String) {
        removeEntitiesRecursively(entityRoot, entityName)
    }

    private fun removeEntitiesRecursively(entity: Entity, entityName: String) {
        entity.removeChildren(entityName)
    }

    fun renameEntity(oldEntityName: String, newEntityName: String) {
        entityRoot.renameChildren(oldEntityName, newEntityName)
    }

    fun addGlobalAttribute(attribute: Attribute) {
        entityRoot.addAttributeRecursively(attribute)
    }

    fun removeGlobalAttribute(attributeName: String) {
        entityRoot.removeAttributeRecursively(attributeName)
    }

    fun renameGlobalAttribute(oldName: String, newName: String) {
        entityRoot.renameAttributeRecursively(oldName, newName)
    }

    fun updateGlobalAttributeName(oldName: String, newName: String) {
        if (newName.isEmpty()) {
            throw IllegalArgumentException("New attribute name cannot be empty.")
        }
        updateAttributeNameRecursively(entityRoot, oldName, newName)
    }

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


    fun prettyPrintDocument(document: Document): String {
            val stringBuilder = StringBuilder()
            stringBuilder.append("<?xml version=\"1.0\" encoding=\"${document.encoding}\"?>\n")
            entityRoot.printEntity(document.entityRoot, stringBuilder, 0)
        return stringBuilder.toString()
    }

    fun saveDocumentToFile(documentContent: String, filePath: String) {
        try {
            File(filePath).writeText(documentContent)
            println("Document saved successfully to $filePath")
        } catch (e: Exception) {
            println("Failed to save document: ${e.message}")
        }
    }
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

        if (!allowedVersion.contains(version) or !allowedVersionNameRegex.matches(version))
            throw IllegalArgumentException("Invalid version parameter: $version. This API Library only prepared for the standard version 1.0")

        if (!allowedEncoding.contains(encoding))
            throw IllegalArgumentException("Invalid encoding parameter: $encoding. This API Library only prepared for the Encoding 'UTF-8', others will be added in future versions")
    }

    fun printDocumentStructure() {
        println("Document (Version: $version, Encoding: $encoding)")
        entityRoot.printStructureWithAttributes()
    }
}