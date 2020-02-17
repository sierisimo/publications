---
title: Let's talk DSL with Kotlin
published: true
description: Let's understand what and how a DSL works on Kotlin
tags: kotlin,functions,dsl,meta-programming
cover_image:
series:
---

## What is a DSL

A DSL is one way to wrap common operations that you do or a group of people do frequently.

For example you go every day for coffee and you order the same (double espresso with ice in my case), this means you have this common operation that your barista and you already know. So eventually instead of going explicitly saying: "Hey Jhoon, give me double espresso with ice" you simply say: "Hey Jhoon, I'll take the usual". Jhoon and you already know that the usual is a _double espresso with ice_ so you can skip some details and make it easy for both of you to order.

Another example is ordering sandwiches in Mexico, instead of asking for: "One with ham, cheese and sausage" you just ask for a "Spanish sandwich". Yes, probably the first time you see this it will seem weird to you, but the thing is that is a simpler way for the sandwich place to know what you want to order. They already have a whole menu based on countries and what ingredients they contain. But maybe you want your sandwich without onions, that's fair some people don't like onions (I personally love them) so you will say: "A Spanish sandwich without Onions". This means that even when the recipe is already defined you can customize part of it.

A DSL is just that, some local way to define (or execute) something. In our examples, a meal order. In other cases is a configuration, a build definition, a way to create a JSON, a network call, an asynchronous job, etc.

DSL makes easier define common operations or complex operations using a locally defined language… actually… DSL means that: _Domain-Specific Language_. So… requesting food to your sandwiches place? That's _Domain-Specific_! as the names only apply to sandwiches places, if you order the same at a pasta restaurant they probably don't get it.

## DSL to the rescue

When you are developing something you eventually start repeating things: need a thumbnail for that article? Create a network call pointing to the image, need to build data for a login request? Build a JSON and pass it to the network call, defining an alert to the user? build the dialog with a builder…

And these aren't the only cases, you probably already abstracted the way to achieve this kind of operations: with a function, with some design patterns that make easier the task, with a class wrapping most of the process/logic, etc.

But a DSL goes even further: it adds a quicker way to complete tasks/achieve something in your project without having to worry about the implementation details. This means a coworker, a friend or some contributor to your project could easily start adding functionality using your DSL without knowing what's happening internally. Also if you later decide to change the HTTP library for something lightweight or change the JSON parser or some part of your software needs improvement but you don't want to mess with all the usages, you can change internally how your DSL achieve things and keep the DSL the same, so fixing and improving won't affect how others achieve things with the DSL.

## Common use case for DSL

We use some DSL frequently without noticing: _RegExp_ (Regular Expressions), _AWK, SQL, CSS, SASS, XML,_ etc. They are considered DSL as they apply to an specific kind of task. For example, you cannot use a _RegExp_ to open a file or to perform a network call, these aren't things _RegExp_ can do, it's main purpose is to search and process text. As well as you cannot use CSS to process an audio file and add new sounds to it. These examples work on specific, limited, and detailed areas and they work quite well defining a simpler way to achieve their purpose than programming all the logic by ourselves.

You can create a DSL for your project defining a group of functions, data types, classes and resources. They need to provide a way to make some tedious task easier and should be a simpler way to read through code.

Some languages provide easy ways to create DSLs and provide with tools to validate and help your DSLs work properly.

If you're a reading this, I hope you are familiar with Kotlin and you want to take your coding to the next level: write less and do more. Well, let's see how Kotlin helps us building DSLs and validating the information within.

### Let's see some examples

As mentioned before, we can define a DSL for common tasks, let's take the previously mentioned ideas:

#### Create a network call pointing to the image

```kotlin
fetchImage {
    src = "https://via.placeholder.com/350x150"

    onDone { img ->
        myThumbnail.image = img
    }

    default = resources.default_thumbnail
}
```

#### Build a JSON for that login

```kotlin
val loginInfoJson: Json = buildJson {
    property(key = "username") {
        value = "Sierisimo"
    }

    property {
        key = "password"
        value = "supersecret"
    }

    property {
        key = "meta"
        value = buildJson {
            property(key = "service") {
                value = "google"
            }

            property(key = "token") {
                value = store.token
            }
        }
    }

    property(key = "emails"){
        value = arrayOf("sierisimo@mail.com", "notmyrealemail@placeholder.com")
    }

    minified = true
}
```

#### Show a message to the user

```kotlin
showMessage {
    title = "Hey friend!"
    message = "We notice you are working too hard and we are proud of you. Take five minutes back and relax, you deserve it!"

    confirmButton {
        text = "You're right!"
        onClick { dialog ->
            dialog.dismiss()
        }
    }

    cancelButton {
        text = "No, let's keep the hard work"
        onClick { dialog ->
            dialog.dismiss()
        }
    }
}
```

These examples are just hypothetical and can change for your needs… and that's another awesome part of a DSL: it will adapt to your needs! As you'll build it for solving your Domain problems.

## Ok, you got me. Show me how

First thing you should do is find a common thing you do in your code. Something that you already notice you repeat like launching something, opening something, making a request, building something complex, etc.

For example, Kotlin Standard Library already includes a minimal DSL for building a String, it makes easy to create a single `StringBuilder` and add things to it using a _lambda function_. It also adds functions to have a `vararg` arguments instead of individual calls.

```kotlin
val text = buildString {
    append("Hi", "my", "friend")

    append("more text")
}
//text: Himyfriendmore text
```

There's an official DSL library for building HTML with kotlin, [you can check it here](https://github.com/Kotlin/kotlinx.html). A short example is shown below:

```kotlin
createHTML().html {
    body {
        div {
            a("https://dev.to") {
                +"Main site"
            }
        }
    }
}
```

This will generate the next html:

```html
<html>
  <body>
    <div><a href="https://dev.to">Main site</a></div>
  </body>
</html>
```

Let's create a DSL for creating JSON objects.

## Our first approach

We already defined how our DSL will work a few lines above. We know the first thing we will need is a function that will create a Json but also that it will return a `Json` type. Let's define both:

```kotlin
class Json

fun buildJson(): Json {
    return Json()
}
```

Additionally we would like to take use `buildJson { … }` which is actually a call to a function passing a lambda as parameter. Inside that lambda we will invoke multiple functions to add properties to the Json and to modify/add values. This will require that all operation are referring to the final `Json` object that will be generated. We can achieve this using a **lambda with receiver**:

```kotlin
fun buildJson(buildBlock: Json.() -> Unit): Json {
    val json = Json()
    json.apply(buildBlock)
    return json
}
```

This tells us that now we have to pass a lambda to the function in order to build the `Json` and also this lambda will have access to the information of the `Json`. In some cases you want to create an intermediate class to remove the mutability of the final object… but we will discuss that approach on a later article.

Now we have the beginning of our DSL.

## Adding inner functions

For every property on our final `Json` we want to allow a call on our DSL to something like: `addProperty` or just `property` to make it more readable, as our function is `buildJson` the `property` function can be understood as adding a new property more than querying.

To make this possible we can create independant functions or simply add them as methods to the `Json` class:

```kotlin
class Json {
    fun property(
        key: String,
        value: Any? = null
    ){
        //Logic to add a property will live here
    }
}
```

This new method inside of the `Json` class allows our DSL to behave in this way:

```kotlin
buildJson {
    property(key = "name", value = "Sinuhe")
}
```

But this is limited as we cannot do many computations or things in an expressive way. Our DSL should allow the properties to be "dynamic" in a way they are actually calculated. Once again we go with a **lambda with receiver**:

```kotlin
class Json {
    fun property(
        key: String = "",
        value: Any? = null,
        propBlock: JsonProperty.() -> Unit = { }
    ) {
        val jsonProperty = JsonProperty(key, value)
        jsonProperty.apply(propBlock)
        //More logic here for registering the property somewhere
    }
}

data class JsonProperty(
    var key: String,
    var value: Any?
)
```

As you can notice (and not mentioned before), we are adding default values, this means that invocations of our DSL can skip some values in order to let the DSL take the decission for trivial values or implementations:

```kotlin
//Example 1
buildJson {
    property(key = "name", value = "Sinuhe")

    //Also valid:
    property(key = "") {
        value = 0
    }

    property {
        key = "pwd"
        value = "Some Complicated password"
    }
}
```

## DSL Should be safer

So far our final `Json` doesn't actually hold or contains any information. We should add some properties to it on each invoke of `property` to keep track of these values:

```kotlin
class Json {
    private val properties: MutableMap<String, Any?> = mutableMapOf()

    val keys: Set<String>
        get() = properties.keys

    fun property(
            key: String = "",
            value: Any? = null,
            propBlock: JsonProperty.() -> Unit = {}
    ) {
        val jsonProperty = JsonProperty(key, value)
        jsonProperty.apply(propBlock)

        properties[jsonProperty.key] = jsonProperty.value
    }
}
```

But now we have something not so cool on our DSL, the DSL can be used like this:

```kotlin
buildJson {
    property(key = "name") {
        value = "Sinuhe"

        println(keys)
    }
}
```

This is a mistake as we don't expect to have access to `keys` which is a property of `Json` inside of the `property` block. This is less readable as we (the owners of the DSL) are aware that `keys` belongs to the top block but the users (maybe us in 2 months or maybe our coworkers or someone using our public library) won't be aware of why `keys` exists here _"why a `property` has keys?_.

To avoid this type of errors, Kotlin provides us with a simple way to let the compiler check if something is available or accessible in a block. We fisrt need to create an annotation marked with `@DslMarker`:

```kotlin
@DslMarker
annotation class JsonDSL
```

To let the Kotlin compiler to check usages of our DSL we need to mark the types (classes and interfaces) involved on our DSL. This check will validate that all **lambdas with receiver** limit their access to just the **receiver**, the rule in case of two or more receiver are involved or even a lambda without receiver is present is that "the top closest one wins" this means the type closest in a top level function allows the access on it's fields to the block. In that way we can hide fields and other functions from inner blocks, this will happen at compile time, making the usages safer.

```kotlin
@DslMarker
annotation class JsonDSL

@JsonDSL
data class JsonProperty(…)

@JsonDSL
class Json {
    …
}
```

## What's next

From here we can extend our DSL for other operations, common suggestions are to override operators, add extension functions inside of our class, add get/set functions for accessing, etc. to make the DSL even more cool:

```kotlin
class Json {
    infix fun String.toValue(value: Any?) {
        properties[this] = value
    }
}

//On the DSL:

buildJson {
    "lastName" toValue 5

    "innerJson" toValue buildJson {

    }
}
```

## Conclusions

There's no specific rules for how to build a DSL or what elements to add. As a DSL is for specific operationgs or rules on a certain domain (as the name says), some rules or naming conventions can differ.

Also notice that designing and building a DSL takes some time so it's a solution in the long term that will make writting code easier for you and the projects using it. The final user of your application/service/software probably won't notice the presence of a DSL but developers will (unless you are writting a library/framework in which case your final users are developers).

There's out there some DSL already built with these techniques that you can check an take inspiration from:

- [Anko](https://github.com/Kotlin/anko) (Deprecated but still something good to read)
- [Kotlinx.HTML](https://github.com/Kotlin/kotlinx.html)
- [KTON](https://github.com/Jire/KTON)
- [Skrape.it](https://github.com/skrapeit/skrape.it)
- Others not mentioned (but if you give me the links I'll include them)

/////////////////////////////////////

Reach me with questions, comments or just to show off your DSLs at @sierisimo in both [Github](https://github.com/sierisimo) and [Twitter](https://twitter.com/sierisimo).
