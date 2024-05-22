import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

//import kotlin.test.assertTrue


class UnitTests {

    // Exception Testing
    @Test
    fun testInvalidVersionThrowsException() {
        val exception = assertThrows<IllegalArgumentException> {
            Document("2.0", "UTF-8", Entity("root"))
        }
        assert(exception.message!!.contains("Invalid version parameter"))
    }

    @Test
    fun testInvalidVersionValueThrowsException() {
        val exception = assertThrows<IllegalArgumentException> {
            Document("2", "UTF-8", Entity("root"))
        }
        assert(exception.message!!.contains("Invalid version parameter"))
    }

    @Test
    fun testInvalidEncodingThrowsException() {
        val exception = assertThrows<IllegalArgumentException> {
            Document(version = "1.0", encoding = "ASCII", entityRoot = Entity("root"))
        }
        assert(exception.message!!.contains("Invalid encoding parameter"))
    }

    @Test
    fun testInvalidEntityNameThrowsException() {
        val numberException = assertThrows<IllegalArgumentException> {
            Entity("123Invalid")
        }
        val spaceException = assertThrows<IllegalArgumentException> {
            Entity("  Invalid")
        }
        assert(numberException.message!!.contains("Invalid name"))
        assert(spaceException.message!!.contains("Invalid name"))
    }

    @Test
    fun testAddSelfAsChildThrowsException() {
        val root = Entity("root")
        val child1 = Entity("child1")
        val rootLevelException = assertThrows<IllegalArgumentException> {
            root.addChildren(root)
        }
        val childLevelException = assertThrows<IllegalArgumentException> {
            child1.addChildren(child1)
        }

        assert(rootLevelException.message!!.contains("Cannot add a child that is a descendant"))
        assert(childLevelException.message!!.contains("Cannot add a child that is a descendant"))
    }

    @Test
    fun testAddChildToTextEntityThrowsException() {
        val childEntity = Entity("curso")
        childEntity.text = "Mestrado em Engenharia Informática"
        val subChildEntity = Entity("Ramo")

        val exception = assertThrows<IllegalStateException> {
            childEntity.addChildren(subChildEntity)
        }
        assert(exception.message!!.contains("should not have children"))

    }

    @Test
    fun testAddEquivalentEntityThrowsException() {
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)
        val entity1 = Entity("child")
        val entity2 = Entity("child")
        val entity3 = Entity("child")
        val entity4 = Entity("root")

        val sameEntityNameException = assertThrows<IllegalArgumentException> {
            rootEntity.addChildren(entity1)
            rootEntity.addChildren(entity2)
        }
        val sameChildrenEntityNameWithDifferentTextException = assertThrows<IllegalArgumentException> {
            rootEntity.addChildren(entity1)
            entity1.text = "Algo"
            rootEntity.addChildren(entity2)
            rootEntity.addChildren(entity3)
        }
        val sameEntityNameDocumentLevelException = assertThrows<IllegalArgumentException> {
            document.addEntity(entity4)
        }


        assert(sameEntityNameException.message!!.contains("Invalid adding: An equivalent entity already exists in the structure"))
        assert(sameChildrenEntityNameWithDifferentTextException.message!!.contains("Invalid adding: Cannot add a child that is a descendant"))
        assert(sameEntityNameDocumentLevelException.message!!.contains("Invalid Entity: An equivalent entity already exists in the document"))
    }

    @Test
    fun testRemoveEntitiesWithEmptyNameGloballyThrowsException() {
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)

        val exception = assertThrows<IllegalArgumentException> {
            document.removeEntitiesGlobally("")
        }
        assert(exception.message!!.contains("Entity name cannot be empty."))
    }

    @Test
    fun testRemoveNonExistentEntityGloballyThrowsException() {
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)
        val entity1 = Entity("child")

        document.addEntity(entity1)

        document.printDocumentStructure()
        document.removeEntitiesGlobally("child")
        document.printDocumentStructure()

        val exception = assertThrows<NoSuchElementException> {
            document.removeEntitiesGlobally("root")
        }
        //println(exception.message)
        assert(exception.message!!.contains("No entity found with the name"))
    }

    @Test
    fun testRenameExistingEntity() {
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
    fun testAddingDuplicateAttributeThrowsException() {
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)
        val entity1 = Entity("child")

        val attribute1 = Attribute("key1", "value1")
        val attribute2 = Attribute("key1", "value1")
        val attribute3 = Attribute("key3", "value3")

        entity1.addAttribute(attribute1)
        entity1.printStructureWithAttributes()
        entity1.addAttributeRecursively(attribute3)
        entity1.printStructureWithAttributes()
        document.addGlobalAttribute(attribute2)
        document.printDocumentStructure()

        val exception = assertThrows<IllegalArgumentException> {
            entity1.addAttribute(attribute2)
        }

        assert(exception.message!!.contains("already exists"))
    }

    @Test
    fun testRemovingNonExistentAttributeThrowsException() {
        val rootEntity = Entity("root")
        val document = Document("1.0", "UTF-8", rootEntity)
        val entity1 = Entity("child")

        val attribute1 = Attribute("key1", "value1")
        val attribute2 = Attribute("key1", "value1")

        entity1.addAttribute(attribute1)
        entity1.printStructureWithAttributes()
        entity1.removeAttributeRecursively("key1")
        entity1.printStructureWithAttributes()
        document.addGlobalAttribute(attribute2)
        document.printDocumentStructure()
        document.removeGlobalAttribute("key1")
        document.printDocumentStructure()

        val exception = assertThrows<NoSuchElementException> {
            rootEntity.removeAttributeRecursively("nonExistentAttribute")
        }
        assert(exception.message!!.contains("No attribute found with the name: nonExistentAttribute to remove"))
    }

    @Test
    fun testRemovingAttributeWithEmptyNameThrowsException() {
        val root = Entity("root")
        val exception = assertThrows<IllegalArgumentException> {
            root.removeAttributeRecursively("")
        }
        assert(exception.message!!.contains("Attribute name cannot be empty"))
    }

    @Test
    fun testRenamingNonExistentAttributeThrowsException() {
        val root = Entity("root")
        val document = Document("1.0", "UTF-8", root)
        document.printDocumentStructure()
        val exception = assertThrows<NoSuchElementException> {
            document.renameGlobalAttribute("oldName", "newName")
        }
        assert(exception.message!!.contains("No attribute found with the name 'oldName' to rename"))
    }

    @Test
    fun `test renaming attribute with empty names throws exception`() {
        val root = Entity("root")
        val document = Document("1.0", "UTF-8", root)

        val attribute2 = Attribute("key1", "value1")

        document.addGlobalAttribute(attribute2)
        document.printDocumentStructure()

        val exception = assertThrows<IllegalArgumentException> {
            document.renameGlobalAttribute("", "")
        }
        assert(exception.message!!.contains("Attribute names cannot be empty"))
    }


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

        assertEquals(expectedOutput, formattedXml.trim())
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
        val textQueryResult1 =  queryResult1.forEach {println(it)}

        val queryResult2 = document.queryMicroXPath("curso")
        println("\nConsulta 2: Cursos")
        queryResult2.forEach { println(it)}

        val queryResult3 = document.queryMicroXPath("fuc/nome")
        println("\nConsulta 3: Nome de Cadeiras")
        queryResult3.forEach { println(it)}

//        val q1ExpectedOutput = """
//            <componente nome="Quizzes" peso="20%"/>
//            <componente nome="Projeto" peso="80%"/>
//            <componente nome="Dissertação" peso="60%"/>
//            <componente nome="Apresentação" peso="20%"/>
//            <componente nome="Discussão" peso="20%"/>
//            """.trimIndent()
//
//        assertEquals(q1ExpectedOutput, textQueryResult1)

    }
}