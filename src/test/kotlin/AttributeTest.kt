import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.Test

class AttributeTest {
    @Test
    fun `test add attribute`() {
        val entity = Entity("entity")
        entity.addAttribute(Attribute("attr1", "value1"))
        entity.addAttribute(Attribute("attr2", "value2"))

        Assertions.assertEquals(2, entity.numberOfAttribute())
    }

    @Test
    fun `test invalid attribute name throws exception`() {
        val entity = Entity("entity")
        val exception = assertThrows<IllegalArgumentException> {
            entity.addAttribute(Attribute("invalid attribute name!", "value"))
        }
        assertEquals("The name of Attribute is invalid.", exception.message)
    }
}