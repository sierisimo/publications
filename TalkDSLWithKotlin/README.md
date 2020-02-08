# Let's talk DSL with Kotlin

## What's a DSL

A DSL Is one way to wrap common operations that you do or a group of people do frequently.

For example you go Every day for coffee and you order the same usually (double espresso with ice in my case), this means you have this common operation that your barista and you already know. So eventually instead of going explicitly saying: "Hey Jhoon, give me double espresso with ice" you simply say: "Hey Jhoon, I'll take the usual". Jhoon and you already know that the usual is a _double espresso with ice_ so you can skip some details and make it easy for both of you to order.

Another example is ordering some sandwiches in Mexico, instead of asking for: "One with ham, cheese and sausage" you just ask for a "Spanish sandwich". Yes, probably the first time you see this it will seem weird to you, but the thing is that is a simpler way for the sandwich place to know what you want to order. They already have a whole menu based on countries and what ingredients they contain. But maybe you want your sandwich without onions, that's fair some people don't like onions (I personally love them) so you wan say: "A Spanish sandwich without Onions". This means that even when
the recipe is already defined you can customize part of it.

A DSL is just that, some local way to define something. In our examples, a meal order. In other cases is a configuration, a build definition, a way to create a JSON, a network call, an asynchronous job, etc.

DSL makes easier define common operations or complex operations using a locally defined language… actually… DSL means that: _Domain-Specific Language_. So… requesting food to your sandwiches place? That's _Domain-Specific_! as the names only apply to sandwiches places, if you order the same at a pasta restaurant they probably don't get it.

## DSL to the rescue

When you are developing something you eventually start repeating things: need a thumbnail for that article? Create a network call pointing to the image, need to build data for a login request? Build a JSON and pass it to the network call, defining an alert to the user? build the dialog with a builder…

And these aren't the only cases, you probably already abstracted the way to achieve this kind of operations: with a function, with some design patterns that make easier the task, with a class wrapping most of the process/logic, etc.

But a DSL goes even further: it adds a quicker way to complete tasks/achieve something in your project without having to worry about the implementation details. This means a coworker, a friend or some contributor to your project could easily start adding functionality using your DSL without knowing what's happening internally. Also if you later decide to change the HTTP library for something lightweight or change the JSON parser or some part of your softwar needs improvement but you don't want to mess with all the usages, you can change internally how your DSL achieve things and keep the DSL the same, so fixing and improving won't affect how others achieve things with the DSL.

## Common use case for DSL

We use some DSL frequently without noticing: RegExp (Regular Expressions), AWK, SQL, CSS, SASS, XML, etc. They are considered DSL as they apply to an specific kind of task. For example, you cannot use a RegExp to open a file or to perform a network call, these aren't things RegExp can do, it's main purpose is to process and search text. As well as you cannot use CSS to process an audio file and add new sounds to it. These examples work on specific, limited, and detailed areas and they work quite well defining a simpler way to achieve their purpose than programming all the logic by ourselves.

You can create a DSL for your project defining a group of functions, data types, classes and resources. They need to provide a way to make some tedious task easier and should be a simpler way to read through code.

Some languages provide easy ways to create DSLs and provide with tools to validate and help your DSLs work properly.

If you're a reading this, I hope you are familiar with Kotlin and you want to take your coding to the next level: write less and do more. Well, let's see how Kotlin helps us building DSLs and validating the information.

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

## Your first approach

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

This tells us that now we have to pass a lambda to the function in order to make it work. 