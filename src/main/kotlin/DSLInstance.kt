/**
 * DSL - Internal DSL to instantiate XML models intuitively.
 * Couple of Builders
 */

class EntityBuilder(val name: String) {
    private val attributes = mutableListOf<Attribute>()
    private val children = mutableListOf<Entity>()

    fun attribute(name: String, value: String) {
        attributes.add(Attribute(name, value))
    }

    fun entity(name: String, init: EntityBuilder.() -> Unit): Entity {
        val childBuilder = EntityBuilder(name)
        childBuilder.init()
        val child = childBuilder.build()
        children.add(child)
        return child
    }

    fun build(): Entity {
        val entity = Entity(name)
        attributes.forEach { entity.addAttribute(it) }
        children.forEach { entity.children.add(it) }
        return entity
    }
}

class XMLDocumentBuilder {
    private lateinit var root: Entity

    fun root(name: String, init: EntityBuilder.() -> Unit) {
        val rootBuilder = EntityBuilder(name)
        rootBuilder.init()
        root = rootBuilder.build()
    }

    fun build(): Document {
        return Document(entityRoot = root)
    }
}

fun xmlDocument(init: XMLDocumentBuilder.() -> Unit): Document {
    val builder = XMLDocumentBuilder()
    builder.init()
    return builder.build()
}






fun main() {
    val document = xmlDocument {
        root("fuc") {
            attribute("code", "mei24")
            entity("avaliacao") {
                entity("componente") {
                    attribute("nome", "Dissertação")
                    attribute("peso", "30%")
                    attribute("nota", "18")
                }
                entity("componente") {
                    attribute("nome", "Apresentação")
                    attribute("peso", "40%")
                    attribute("nota", "18")
                }
                entity("componente") {
                    attribute("nome", "Discussão")
                    attribute("peso", "50%")
                    attribute("nota", "18")
                }
            }
        }
    }

    println(document.prettyPrint())
}
