import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*


class DSLTest {

    @Test
    fun `test simple document creation`() {
        val document = xmlDocument {
            root("fuc") {
                attribute("versao", "1.0")
                entity("avaliacao") {
                    entity("componente") {
                        attribute("nome", "Dissertação")
                        attribute("peso", "30%")
                        attribute("nota", "18")
                    }
                }
            }
        }

        val expectedXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <fuc versao="1.0">
              <avaliacao>
                <componente nome="Dissertação" peso="30%" nota="18"/>
              </avaliacao>
            </fuc>
        """.trimIndent()

        assertEquals(expectedXml, document.prettyPrint().trimIndent())
    }

    @Test
    fun `test document with multiple entities`() {
        val document = xmlDocument {
            root("fuc") {
                attribute("versao", "1.0")
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

        val expectedXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <fuc versao="1.0">
              <avaliacao>
                <componente nome="Dissertação" peso="30%" nota="18"/>
                <componente nome="Apresentação" peso="40%" nota="18"/>
                <componente nome="Discussão" peso="50%" nota="18"/>
              </avaliacao>
            </fuc>
        """.trimIndent()

        assertEquals(expectedXml, document.prettyPrint().trimIndent())
    }

    @Test
    fun `test nested entities`() {
        val document = xmlDocument {
            root("school") {
                entity("class") {
                    attribute("name", "Math")
                    entity("student") {
                        attribute("name", "John")
                        attribute("grade", "A")
                    }
                    entity("student") {
                        attribute("name", "Jane")
                        attribute("grade", "B")
                    }
                }
            }
        }

        val expectedXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <school>
              <class name="Math">
                <student name="John" grade="A"/>
                <student name="Jane" grade="B"/>
              </class>
            </school>
        """.trimIndent()

        assertEquals(expectedXml, document.prettyPrint().trimIndent())
    }
}

