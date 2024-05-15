import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
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

        assert(sameEntityNameException.message!!.contains("An equivalent entity already exists"))
        assert(sameChildrenEntityNameWithDifferentTextException.message!!.contains("An equivalent entity already exists"))
        assert(sameEntityNameDocumentLevelException.message!!.contains("An equivalent entity already exists"))
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
        assert(exception.message!!.contains("New attribute name cannot be empty."))

    }

//    @Test
//    fun `update global attribute name`() {
//        val root = Entity("root")
//        val child1 = Entity("child1")
//        val child2 = Entity("child2")
//        root.children.add(child1)
//        child1.children.add(child2)
//
//        child1.attributes.add(Attribute("oldName", "value1"))
//        child2.attributes.add(Attribute("oldName", "value2"))
//
//        val document = Document("1.0", "UTF-8", root)
//        document.updateGlobalAttributeName("oldName", "newName")
//
//        assertNull(child1.attributes.find { it.name == "oldName" })
//        assertEquals("value1", child1.attributes.find { it.name == "newName" }?.value)
//        assertNull(child2.attributes.find { it.name == "oldName" })
//        assertEquals("value2", child2.attributes.find { it.name == "newName" }?.value)
//    }
//
//    @Test
//    fun `update global attribute value`() {
//        val root = Entity("root")
//        val child1 = Entity("child1")
//        root.children.add(child1)
//
//        child1.attributes.add(Attribute("name", "oldValue"))
//        root.attributes.add(Attribute("name", "rootOldValue"))
//
//        val document = Document("1.0", "UTF-8", root)
//        document.updateGlobalAttributeValue("name", "newValue")
//
//        assertEquals("newValue", root.attributes.find { it.name == "name" }?.value)
//        assertEquals("newValue", child1.attributes.find { it.name == "name" }?.value)
//    }

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
        document.saveDocumentToFile(documentContent, "/Users/mauurao/Desktop/ISCTE/MEI/2º Semestre/Programação Avançada/Projeto/XMLManipulationLibrary/src/main/resources.xml")

    }

}

// Functionality Testing

/*

    private val rootEntity = Entity("root")
    private val document = Document("1.0", "UTF-8", rootEntity)
    private val child1 = Entity("child1")
    private val child2 = Entity("child2")
    private val child1Sub1 = Entity("child1Sub1")
    private val child2Sub1 = Entity("child2Sub1")

    @Test
    fun test_adding_entity_in_document() {

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)

        document.printDocumentStructure()

        // Checks that the entity has been correctly added as a child of the root entity
        assertEquals(2, rootEntity.numberOfDirectChildren())
    }

   /*
    @Test
    fun testRemovingEntities() {

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)

        // Before removal
        assertEquals(2, rootEntity.numberOfDirectChildren())
        assertEquals(1, child1.numberOfDirectChildren())
        assertEquals(1, child2.numberOfDirectChildren())

        document.printDocumentStructure()

        // Perform removal
        document.removeEntitiesGlobally("child1")

        document.printDocumentStructure()

        // After removal
        assertEquals(1, rootEntity.numberOfDirectChildren()) // child2 should remain
        assertTrue(!rootEntity.hasChildWithName("child1")) // No "child1" should exist at the root level
        assertEquals(1, child2.numberOfDirectChildren())
    }

    @Test
    fun testRenameEntityGlobally() {

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)

        document.printDocumentStructure()

        document.renameEntity("child1", "grown1")

        document.printDocumentStructure()

        assertTrue(rootEntity.hasChildWithName("grown1"))
        assertFalse(rootEntity.hasChildWithName("child1"))
        assertTrue(child1.hasChildWithName("child1Sub1")) // Verify if child1Sub1 it's not affected

        document.renameEntity("child2", "grown2")

        assertTrue(child2.getName() == "grown2")
        assertFalse(rootEntity.hasChildWithName("child2"))

        document.printDocumentStructure()
    }

    @Test
    fun test_add_attribute() { // Talvez mudar o nome do teste e da funcao - Falar com Fernando

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)
        //rootEntity.printStructureWithAttributes()
        document.addAttributeGlobally("child1", "attr1", "value1")
        child1.printStructureWithAttributes()

        //rootEntity.addAttribute(Attribute("attr1", "value1"))
        //rootEntity.addAttribute(Attribute("attr2", "value2"))

        //rootEntity.printStructureWithAttributes()

        //assertEquals(2, rootEntity.numberOfAttribute())
    }

    @Test
    fun test_add_attribute_recursively() { // Talvez mudar o nome do teste e da funcao - Falar com Fernando

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)

        document.printDocumentStructure()


        child1.addAttributeRecursively("child1", "attr1", "value1")

        child1.printStructureWithAttributes()

        assertTrue(child1.hasAttribute("attr1"))
        assertFalse(child2.hasAttribute("attr1"))
    }

    @Test
    fun testRemoveAttributesGlobally() {

        document.addEntity(child1)
        document.addEntity(child2)
        child1.addChildren(child1Sub1)
        child2.addChildren(child2Sub1)

        child1.attributes.add(Attribute("class", "new"))
        child2.attributes.add(Attribute("class", "new"))

        child1.printStructureWithAttributes()

        document.removeAttributesGlobally("child1", "class")

        child1.printStructureWithAttributes()

        assertFalse(child1.hasAttribute("class"), "child1 should not have 'class' attribute anymore.")
        //assertTrue(child2.hasAttribute("class"), "child2 should have 'class' attribute.")

    }
*/