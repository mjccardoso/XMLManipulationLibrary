import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.findAnnotation

/**
 * Annotation to define XML element names.
 *
 * @property name The name of the XML element.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlElement(val name: String = "")

/**
 * Annotation to define XML attribute names.
 *
 * @property name The name of the XML attribute.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlAttribute(val name: String = "")

/**
 * Annotation to define a custom string transformer for XML attributes or elements.
 *
 * @property transformer The class of the string transformer.
 */
@Target(AnnotationTarget.PROPERTY)
annotation class XmlString(val transformer: KClass<out (Any) -> String>)

/**
 * Annotation to define an XML adapter for post-mapping modifications.
 *
 * @property adapter The class of the XML adapter.
 */
@Target(AnnotationTarget.CLASS)
annotation class XmlAdapter(val adapter: KClass<out XmlAdapterBase>)

/**
 * Interface for XML adapters to customize the XML mapping.
 */
interface XmlAdapterBase {

    /**
     * Adapts the entity after automatic mapping.
     *
     * @param entity The entity to adapt.
     * @return The adapted entity.
     */
    fun adapt(entity: Entity): Entity
}

/**
 * An example adapter that performs no modifications.
 */
class FUCAdapter : XmlAdapterBase {
    override fun adapt(entity: Entity): Entity {
        return entity
    }
}

/**
 * A sample string transformer that adds a percentage sign.
 */
class AddPercentage : (Any) -> String {
    override fun invoke(input: Any): String {
        return "$input%"
    }
}

@XmlAdapter(FUCAdapter::class)
data class FUC(
    @XmlAttribute val codigo: String,
    @XmlElement val nome: String,
    @XmlElement val ects: Double,
    @XmlElement val observacoes: String,
    @XmlElement val avaliacao: List<ComponenteAvaliacao>
)

data class ComponenteAvaliacao(
    @XmlElement val nome: String,
    @XmlString(AddPercentage::class) @XmlElement val peso: Int
)

/**
 * Converts an object to an XML entity based on its properties and annotations.
 *
 * @param instance The object instance to convert.
 * @return The converted XML entity.
 */
fun toXmlEntity(instance: Any): Entity {
    val kClass = instance::class
    val entityName = kClass.simpleName!!.toLowerCase()
    val entity = Entity(entityName)

    kClass.memberProperties.forEach { prop ->
        val value = prop.call(instance) ?: return@forEach

        val elementAnnotation = prop.findAnnotation<XmlElement>()
        val attributeAnnotation = prop.findAnnotation<XmlAttribute>()
        val stringAnnotation = prop.findAnnotation<XmlString>()

        val stringValue = if (stringAnnotation != null) {
            stringAnnotation.transformer.constructors.first().call().invoke(value)
        } else {
            value.toString()
        }

        when {
            attributeAnnotation != null -> {
                val attribute = Attribute(attributeAnnotation.name.ifEmpty { prop.name }, stringValue)
                if (!entity.attributes.any { it == attribute }) {
                    entity.attributes.add(attribute)
                }
            }
            elementAnnotation != null -> {
                if (value is List<*>) {
                    value.forEach { item ->
                        if (item != null) {
                            val childEntity = toXmlEntity(item)
                            if (!entity.children.any { it.isEquivalent(childEntity) }) {
                                entity.addChildren(childEntity)
                            }
                        }
                    }
                } else {
                    val childEntity = Entity(elementAnnotation.name.ifEmpty { prop.name })
                    childEntity.text = stringValue
                    if (!entity.children.any { it.isEquivalent(childEntity) }) {
                        entity.addChildren(childEntity)
                    }
                }
            }
        }
    }

    kClass.findAnnotation<XmlAdapter>()?.adapter?.let { adapterClass ->
        val adapter = adapterClass.constructors.first().call()
        return adapter.adapt(entity)
    }

    return entity
}

