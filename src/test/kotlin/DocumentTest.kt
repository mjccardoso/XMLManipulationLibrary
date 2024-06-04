import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows

class DocumentTest {


    @Test
    fun testRenamingAttributeToExistingNameThrowsException() {
        val root = Entity("root")
        val document = Document("1.0", "UTF-8", root)

        val attribute2 = Attribute("key1", "value1")

        document.addGlobalAttribute(attribute2)
        document.printDocumentStructure()

        val exception = assertThrows<IllegalArgumentException> {
            document.renameGlobalAttribute("key1", "key1")
        }
        assert(exception.message!!.contains("New attribute name must be different from the old name."))
    }

    @Test
    fun updateAttributeNameUnsuccessfully() {
        val root = Entity("root")
        val document = Document("1.0", "UTF-8", root)
        val entity1 = Entity("child")
        document.addEntity(entity1)

        val attribute2 = Attribute("key1", "value1")
        entity1.addAttribute(attribute2)
        document.printDocumentStructure()

        val exception = assertThrows<IllegalArgumentException> {
            entity1.updateAttributeName("", "key0")
        }
        assert(exception.message!!.contains("New attribute name cannot be empty."))

    }

    @Test
    fun updateAttributeValueUnsuccessfully() {
        val root = Entity("root")
        val document = Document("1.0", "UTF-8", root)
        val entity1 = Entity("child")
        document.addEntity(entity1)

        val attribute2 = Attribute("key1", "value1")
        entity1.addAttribute(attribute2)
        document.printDocumentStructure()

        val exception = assertThrows<IllegalArgumentException> {
            entity1.updateAttributeValue("", "key0")
        }
        assert(exception.message!!.contains("not found."))

    }


    @Test
    fun testSaveFile(){
        val rootEntity = Entity("plano")
        val document = Document("1.0", "UTF-8", rootEntity)
        val course = Entity("curso")
        course.text = "Mestrado em Engenharia Informática"
        val fuc = Entity("fuc")
        val fucCodigo = Attribute("codigo","M4310")
        fuc.addAttribute(fucCodigo)
        val nome = Entity("nome")
        nome.text = "Programação Avançada"
        val ects = Entity("ects")
        ects.text = "6.0"
        val avaliacao = Entity("avaliacao")
        val componente1 = Entity("componente")
        val componente2 = Entity("componente")
        val componenteAtrr11 = Attribute("nome","Quizzes")
        val componenteAtrr12 = Attribute("peso","20%")
        componente1.addAttribute(componenteAtrr11)
        componente1.addAttribute(componenteAtrr12)
        val componente2Atrr11 = Attribute("nome","Quizzes")
        val componente2Atrr12 = Attribute("peso","80%")
        componente2.addAttribute(componente2Atrr11)
        componente2.addAttribute(componente2Atrr12)


        rootEntity.addChildren(course)
        rootEntity.addChildren(fuc)
        fuc.addChildren(nome)
        fuc.addChildren(ects)
        fuc.addChildren(avaliacao)
        avaliacao.addChildren(componente1)
        avaliacao.addChildren(componente2)

        val documentContent = document.prettyPrintDocument(document)
        document.saveDocumentToFile(documentContent, "src/main/resources/save_file_test.xml")

    }

    @Test
    fun testPrettyPrintVisitor() {
        val root = Entity("plano")
        val document = Document("1.0", "UTF-8", root)

        val course = Entity("curso")
        course.text = "Mestrado em Engenharia Informática"
        document.addEntity(course)

        val fuc = Entity("fuc")
        fuc.addAttribute(Attribute("codigo", "M4310"))
        document.addEntity(fuc)

        val nome = Entity("nome")
        nome.text = "Programação Avançada"
        fuc.addChildren(nome)

        val ects = Entity("ects")
        ects.text = "6.0"
        fuc.addChildren(ects)

        val avaliacao = Entity("avaliacao")
        fuc.addChildren(avaliacao)

        val componente1 = Entity("componente")
        componente1.addAttribute(Attribute("nome", "Quizzes"))
        componente1.addAttribute(Attribute("peso", "20%"))
        avaliacao.addChildren(componente1)

        val componente2 = Entity("componente")
        componente2.addAttribute(Attribute("nome", "Projeto"))
        componente2.addAttribute(Attribute("peso", "80%"))
        avaliacao.addChildren(componente2)

        val visitor = PrettyPrintVisitor()
        document.accept(visitor)

        val formattedXml = visitor.getPrettyPrintedDocument()
        println(formattedXml)
        document.saveDocumentToFile(formattedXml, "src/main/resources/visitor_file_test.xml")

        val expectedOutput = """
            <?xml version="1.0" encoding="UTF-8"?>
            <plano>
              <curso>Mestrado em Engenharia Informática</curso>
              <fuc codigo="M4310">
                <nome>Programação Avançada</nome>
                <ects>6.0</ects>
                <avaliacao>
                  <componente nome="Quizzes" peso="20%"/>
                  <componente nome="Projeto" peso="80%"/>
                </avaliacao>
              </fuc>
            </plano>
            """.trimIndent()

        kotlin.test.assertEquals(expectedOutput, formattedXml.trim())
    }

    @Test
    fun microXpathTest() {
        val root = Entity("plano")
        val document = Document("1.0", "UTF-8", root)

        val course = Entity("curso")
        course.text = "Mestrado em Engenharia Informática"
        document.addEntity(course)

        val fuc = Entity("fuc")
        fuc.addAttribute(Attribute("codigo", "M4310"))
        document.addEntity(fuc)

        val nome = Entity("nome")
        nome.text = "Programação Avançada"
        fuc.addChildren(nome)

        val ects = Entity("ects")
        ects.text = "6.0"
        fuc.addChildren(ects)

        val avaliacao = Entity("avaliacao")
        fuc.addChildren(avaliacao)

        val componente1 = Entity("componente")
        componente1.addAttribute(Attribute("nome", "Quizzes"))
        componente1.addAttribute(Attribute("peso", "20%"))
        avaliacao.addChildren(componente1)

        val componente2 = Entity("componente")
        componente2.addAttribute(Attribute("nome", "Projeto"))
        componente2.addAttribute(Attribute("peso", "80%"))
        avaliacao.addChildren(componente2)

        val componente3 = Entity("componente")
        componente3.addAttribute(Attribute("nome", "Dissertação"))
        componente3.addAttribute(Attribute("peso", "60%"))
        avaliacao.addChildren(componente3)

        val componente4 = Entity("componente")
        componente4.addAttribute(Attribute("nome", "Apresentação"))
        componente4.addAttribute(Attribute("peso", "20%"))
        avaliacao.addChildren(componente4)

        val componente5 = Entity("componente")
        componente5.addAttribute(Attribute("nome", "Discussão"))
        componente5.addAttribute(Attribute("peso", "20%"))
        avaliacao.addChildren(componente5)


        val queryResult1 = document.queryMicroXPath("fuc/avaliacao/componente")
        println("Consulta 1: Componentes de Avaliação")
        //queryResult1.forEach {println(it)}.toString()
        val textQueryResult1 = queryResult1.joinToString("\n")
        println(textQueryResult1)

        val queryResult2 = document.queryMicroXPath("curso")
        println("\nConsulta 2: Cursos")
        //queryResult2.forEach { println(it)}
        val textQueryResult2 = queryResult2.joinToString("\n")
        println(textQueryResult2)

        val queryResult3 = document.queryMicroXPath("fuc/nome")
        println("\nConsulta 3: Nome de Cadeiras")
        //queryResult3.forEach { println(it)}
        val textQueryResult3 = queryResult3.joinToString("\n")
        println(textQueryResult3)

        val q1ExpectedOutput = """
            <componente nome="Quizzes" peso="20%"/>
            <componente nome="Projeto" peso="80%"/>
            <componente nome="Dissertação" peso="60%"/>
            <componente nome="Apresentação" peso="20%"/>
            <componente nome="Discussão" peso="20%"/>
            """.trimIndent()

        val q2ExpectedOutput = """
            <curso>Mestrado em Engenharia Informática</curso>
            """.trimIndent()

        val q3ExpectedOutput = """
            <nome>Programação Avançada</nome>
            """.trimIndent()

        kotlin.test.assertEquals(q1ExpectedOutput, textQueryResult1)
        kotlin.test.assertEquals(q2ExpectedOutput, textQueryResult2)
        kotlin.test.assertEquals(q3ExpectedOutput, textQueryResult3)
    }







    @Test
    fun `test add entity`() {
        val root = Entity("root")
        val document = Document(entityRoot = root)
        val child = Entity("child")

        document.addEntity(child)

        assertTrue(root.children.contains(child))
        assertEquals(root, child.parent)
    }

    @Test
    fun `test add attribute globally`() {
        val root = Entity("root")
        val child = Entity("child")
        val attribute = Attribute("attr", "value")
        root.addChildren(child)
        val document = Document(entityRoot = root)

        document.addGlobalAttribute(attribute )

        assertTrue(child.attributes.any { it.name == "attr" && it.value == "value" })
    }

    @Test
    fun `test remove entities globally`() {
        val root = Entity("root")
        val child = Entity("child")
        root.addChildren(child)
        val document = Document(entityRoot = root)

        document.removeEntitiesGlobally("child")

        assertTrue(root.children.isEmpty())
    }

    @Test
    fun `test remove attributes globally`() {
        val root = Entity("root")
        val child = Entity("child")
        child.addAttribute(Attribute("attr", "value"))
        root.addChildren(child)
        val document = Document(entityRoot = root)

        document.removeGlobalAttribute("attr")

        assertFalse(child.attributes.any { it.name == "attr" })
    }
}

