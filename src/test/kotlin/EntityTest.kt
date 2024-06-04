import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class EntityTest {

    @Test
    fun `test invalid entity name throws exception`() {
        val exception = assertThrows<IllegalArgumentException> {
            Entity("invalid entity name!")
        }
        assertEquals("Invalid name: Names can only start with letters and can only contain letters, numbers.", exception.message)
    }

    @Test
    fun `test add children`() {
        val parent = Entity("parent")
        val child = Entity("child")

        parent.addChildren(child)

        assertTrue(parent.children.contains(child))
        assertEquals(parent, child.parent)
    }

    @Test
    fun `test add attribute`() {
        val entity = Entity("entity")
        val attribute = Attribute("attr", "value")

        entity.addAttribute(attribute)

        assertTrue(entity.attributes.contains(attribute))
    }

    @Test
    fun `test remove children`() {
        val parent = Entity("parent")
        val child = Entity("child")
        parent.addChildren(child)

        parent.removeChildren("child")

        assertTrue(parent.children.isEmpty())
    }

    @Test
    fun `test remove attribute`() {
        val entity = Entity("entity")
        val attribute = Attribute("attr", "value")
        entity.addAttribute(attribute)

        entity.removeAttribute("attr")

        assertTrue(entity.attributes.isEmpty())
    }

    @Test
    fun `test has attribute`() {
        val entity = Entity("entity")
        val attribute = Attribute("attr", "value")
        entity.addAttribute(attribute)

        assertTrue(entity.hasAttribute("attr"))
        assertFalse(entity.hasAttribute("nonexistent"))
    }

    @Test
    fun `test rename children`() {
//        val parent = Entity("parent")
//        val child = Entity("child")
//        parent.addChildren(child)
//
//        parent.renameChildren("child", "newChild")
//
//        assertEquals("newChild", child.name)
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)
        val entity1 = Entity("child")
        val entity2 = Entity("child2")

        document.addEntity(entity1)
        entity1.addChildren(entity2)

        document.printDocumentStructure()
        document.printDocumentStructure()

        val exception = assertThrows<java.lang.IllegalArgumentException> {
            document.renameEntity("", "newChild")
        }
        //println(exception.message)
        assert(exception.message!!.contains("Entity names cannot be empty."))

        //assertEquals("newChild", root.children.first().name, "The child entity should have its name changed to 'newChild'.")
    }

    @Test
    fun `test print entity`() {
        val entity = Entity("entity")
        val attribute = Attribute("attr", "value")
        entity.addAttribute(attribute)

        val stringBuilder = StringBuilder()
        entity.printEntity(entity, stringBuilder, 0)
        val result = stringBuilder.toString().trim()

        val expectedXml = """
            <entity attr="value"/>
        """.trimIndent()

        assertEquals(expectedXml, result)
    }
}
