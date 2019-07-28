abstract class AbstractData(
    val id: Int = 123,
    val name: String = "Sier",
    val elements: List<Element> = listOf(Element())
)

class DataOptional(
    id: Int = 123,
    name: String = "Sier",
    elements: List<Element> = listOf(Element()),
    val optional: String = "Added"
) : AbstractData(id, name, elements)

class Data(
    id: Int = 123,
    name: String = "Sier",
    elements: List<Element> = listOf(Element())
) : AbstractData(id, name, elements)

data class Element(
    val inner: Boolean = true
)