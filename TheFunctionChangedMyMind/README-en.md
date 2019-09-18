---
title: The Function that changed my mind
published: true
description: How a single function showed me a lot of Kotlin
tags: kotlin,functions,android,refactor
cover_image:
series:
---

When writing code, we should always have a place for improvement. Improvement can present itself in different ways. A proof of it, is the attention to CI (_Continuous Integration_) and CD (_Continuous Delivery_) has significantly grown in recent years. Same goes for new languages and technologies.

Nevertheless, this doesn't involve changing to a new language, technology, nor tool. Some of these improvements are so simple as changing how something works, i.e. adding tests/abstractions, changing patterns, etc. This is how I have proceeded in several projects I have worked in.

**Disclaimer**: Whether you have talked with me in person, or have attended to one of my talks at meetups, it's likely, you already have heard me talking about it, even on [YouTube with the GDG Santo Domingo (in spanish)](https://www.youtube.com/watch?v=0KoXxoPHV4o) or Kotlin/Everywhere Guadalajara and Mexico City.

**Another disclaimer**: This article is not about architecture –even though it's a hot topic in the Android community –, best practices or even about how to organize an Android Application. It's just about a set of changes that showed me some Kotlin concepts. Also, I started to implement this aforementioned strategies almost 3 years ago, therefor by the time this article is published, the libraries from Google –like `core-ktx`– will probably being updated or improved. And just to be clear: It's good to mention that the approaches mentioned in the following lines don't apply to all scenarios nor cases; the solutions mentioned in this article depend on scenarios or needs themselves, besides individual reasons to choose them over the others.

## But first… some _Context_

If you're an Android Developer or have worked with Android, you probably caught the pun intended by this title. If not, let me explain it to you quickly.

In Android development, everything happening on your application has a `Context` and to make some stuff work, you need to deal with a `Context` as well. This is no more than a class that Android uses to provide information and access to the app.

One specific case arises at the time it's required to start a new `Activity` on your app. This means -succinctly- launching a whole new screen or an external application. In order to accomplish it, firstly you should create an `Intent` based on your `Context` and pass the `Activity` you would like to launch:

```kotlin
val myIntent = Intent(context, MyNextActivity::class.java)
```

Then you can start `MyNextActivity` from another `Activity` simply using this intent: `startActivity(myIntent)`

If your application relies on activities for showing varied sections or views of your application, then probably you will end up with a lot of these calls distributed around. All of them with the same structure yet with several variants, such as:

```kotlin
val someIntent = Intent(context, SomeActivity::class.java)
//Do stuff with the intent
startActivity(someIntent)

//And if you don't do things with the intent and you are in an activity
startActivity(
    Intent(this, MyNextActivity::class.java)
)
```

## My first problem

To be honest, there is no problem with this code. Not at all. But I have seen this happening in a lot of applications, firstly you have these calls distributed around without messed around, then you suddenly have 2 or 3 repetitions of the same work in different places, setting the same `Intent` with a different variable name but same parameters and then doing the same call. To solve these, most people usually do one of two things:

1. Wrap it around a single method for each case
2. Put all under a single class/object and just pass needed parameters for repeated cases

This means, for case 1:

```kotlin
/** Inside your class that will have the calls **/
fun startNext() {
    startActivity(
        Intent(this, MyNextActivity::class.java)
    )
}

fun startSome() {
    val someIntent = Intent(this, SomeActivity::class.java)
    //Do things with intent
    startActivity(someIntent)
}
```

And for case 2:

```kotlin
object AppRouter {
    fun startNext(context: Context) {
        startActivity(
            Intent(context, MyNextActivity::class.java)
        )
    }

    fun startSome(context: Context) {
        val someIntent = Intent(context, SomeActivity::class.java)
        //Do things with intent
        startActivity(someIntent)
    }
}
```

You can add several layers of complexity, but this is the base for most solutions: **have a single point for launching/starting new activities**. In my case, I always ended up having case 1 all around. Having functions here and there seemed like a good solution, until I ended with 120 functions doing almost the same all around, which is not bad, but seeing that `::class.java` even inside of different methods really bothered me. Plus, seeing 3 or 4 functions inside the same class with almost the same body also feels wrong.

I wanted a way to remove that, to make that `::class.java` disappear from everywhere and have something more explicit or at least more readable without the 123193 wrapper functions all around.

## Generics

Coming from _Java Land_ and seeing that all my function looked the same with one single parameter changed made me thing: "I can do better thant this". So I did:

```kotlin
fun <T> launchActivity(context: Context, clazz: Class<T>) {
    val intent = Intent(context, clazz)
    startActivity(intent)
}
```

First problem solved: We have a single function to start our activities and we can leave it as **a global function**. As long as someone has access to a `Context` it can make use of this function. But…

1. Uncle Bob says that the less parameters, the better the function –and it's true, is easier to use it–
2. The `::class.java` will still be present all around
3. If we want to hide the `::class.java` and provide a more "business" related abstraction we still have a lot of functions

```kotlin
fun startNext(context: Context) {
    launchActivity(context, MyNextActivity::class.java)
}

fun startSome(context: Context) {
    launchActivity(context, SomeActivity::class.java)
}
```

And seeing this… well, the initial stuff that triggered me was the whole `::class.java` and is still present, this is not a great improvement…

## Inheritance

Well, having this **global function** also feels kind of wrong, and almost all the places using the function will be some kind of `Activity`. Maybe we can make the function available through inheritance…

```kotlin
open class BaseLaunchActivity: Activity() {
    fun <T> launchActivity(clazz: Class<T>) {
        val intent = Intent(this, clazz)
        startActivity(intent)
    }
}

//And then…
class MyActivity : BaseLaunchActivity() {
    fun onSomeClick() {
        launchActivity(SomeActivity::class.java)
    }
}
```

Cool! We got rid of one parameter, we hide where the magic is happening and made the function available to most of the parts using it.

But wait… _most_? We want **all**, not _most_… also this has other issues:

1. It is not always possible to inherit
2. Not all the classes in need of the code will be a kind of `Activity`
3. When some class has access to `Activity` it will need to be cast in an ugly way
4. `::class.java` is still present –ugh, it's ugly!

For the casting part we will have:

```kotlin
(getActivity() as BaseLaunchActivity).launchActivity(SomeActivity::class.java)
```

Which is **TURBO-SUPER-ULTRA DANGEROUS** –yes, I'm over reacting– because you can't be sure that from now and all the time, this cast will be successful. Why? Well, your class/component is depending on a container and the class itself does not have a mechanism to check all the time which type the container has –and even if it has it, it shouldn't be designed in that way– also if your functionality depends on the container being of some type… well, that's bad design.

## Extension functions to the rescue

Depending on inheritance for this simply functionality seems like producing more problems. Other approach will be using an interface:

```kotlin
interface ActivityLauncher {
    fun <T> launchActivity(context: Context, clazz: Class<T>) {
        val intent = Intent(context, clazz)
        startActivity(intent)
    }
}
```

But we got back to having 2 parameters… BUT on interfaces we can enforce properties:

```kotlin
interface ActivityLauncher {
    val launcherContext: Context

    fun <T> launchActivity(clazz: Class<T>) {
        val intent = Intent(launcherContext, clazz)
        startActivity(intent)
    }
}
```

But now every class that needs this function will need to both:

1. Implement the interface
2. Give a value to the `launcherContext` property

That's boring!!

What about a single extension function to remove the dependency over the property?

```kotlin
fun <T> Context.launchActivity(clazz: Class<T>) {
    val intent = Intent(this, clazz)
    startActivity(intent)
}
```

That's actually cool!

Now whatever `Context` existing around can simply invoke this function without issues, we no longer need wrapper functions but… the `::class.java` has not moved. Arghhh… that thing is making our code look ugly…

And we have a bigger problem… turns out that for this small function, the Kotlin compiler will generate some _bytecode_ that once existing in the JVM will look approximately –on java– like this:

```java
public final class ContextExtensions {
    public static final void launchActivity(@NotNull Context context, @NotNull Class clazz) {
        Intent intent = new Intent(context, clazz);
        startActivity(intent);
    }
}
```

This is not bad but is a caveat of having Kotlin extension functions and… well, if it is only to make things readable, we are not making too much progress: we only removed 1 line of code from our original block of code. What can save us from actually having this on the _bytecode_ and still adding readability to our codebase?

## INLINE

Adding a single reserved word to our function will make everything cool on the _bytecode_:

```kotlin
inline fun <T> Context.launchActivity(clazz: Class<T>) {
    val intent = Intent(this, clazz)
    startActivity(intent)
}
```

But if you are writing this on **AndroidStudio** or **IntelliJ** probably you'll notice a warning explaining that there's no actual optimization from _inlining_ this function. And that's true, `inline` works as an **_SMART-COPY-PASTE_** which mean it will do magic on compile time to paste the body of your function where is being called with actual params and stuff.

Talking about params… what about the `Intent`? We usually pass information between activities using the `Intent` right? With the current approach is not possible to pass any parameter… let's fix that first and we then can check the warning from the `inline`.

## Lambda with receiver

Because we don't want to increment the size of our parameter list, because the types of parameters can grow and because we don't want to have multiple wrapper functions just for the parameters, we need to find a way to "_configure_" our `Intent` _on demand_.

The _lambda with receiver_ is one way to fix this. Adding this parameter to our function we can allow each caller to configure during execution of our function the `Intent` and still keep things readable:

```kotlin
inline fun <T> Context.launchActivity(clazz: Class<T>, confBlock: Intent.() -> Unit) {
    val intent = Intent(this, clazz)
    intent.confBlock()
    startActivity(intent)
}
```

Adding this last parameter we are saying that our function can take a lambda that will execute in the scope of an `Intent` and can use properties and methods from the `Intent` object. This lambda is later used to set up our `Intent` and finally we start the activity with the already configured `Intent`.

This change allows the execution to be something like:

```kotlin
fun onSomeInteraction() {
    launchActivity(SomeActivity::class.java) {
        putExtra(MY_KEY, myValue)
        putExtra(SOME_KEY, someValue)
    }
}
```

Also, the warning about `inline` disappears with these changes as the optimization now makes sense. In compile time, the compiler will take the call to our function and replace it with the actual body of the function and replacing the lambda with a local variable. This will increase a little bit the size of our _bytecode_ but gave us more readability for usages.

The _bytecode_ for this call on Java will look something like:

```java
public void onSomeInteraction() {
    Intent intent = new Intent(this, SomeActivity.class);
    intent.putExtra(MY_KEY, myValue);
    intent.putExtra(SOME_KEY, someValue);
    startActivity(intent);
}
```

As you can see, no reference to external classes, no calls to external functions, all living there. This is a good thing, as the code stays simple while coding and has the real meaning when executing.

But still… while coding, the `::class.java` is present. Why have I been complaining about this if it is something present in Kotlin? Well, the main reason is because this operator and class is not nice, it's quite ugly and also can be confusing. There is more than one way to obtain a `Class<T>` from a class and this one is the easier one. There's a chance for error using the other ones and they are verbose, not what we are trying to avoid here.

## Reified for your types

We are currently having a generic – `<T>` – function that is _inlined_, these two elements can be combined to use a third element on our function: `reified`:

```kotlin
inline fun <reified Ty> Context.launchActivity(clazz: Class<T>, confBlock: Intent.() -> Unit) {
    val intent = Intent(this, clazz)
    intent.confBlock()
    startActivity(intent)
}
```

Adding this word will means that for every single call of our function, when inlining, the compiler will use the actual type instead of a generic or a super type in the caller –I'll go deeper on `reified` on next articles. This small change allows us to use the generic inside of our function as a real type, then we can remove the first parameter and replace with the real type:

```kotlin
inline fun <reified T> Context.launchActivity(confBlock: Intent.() -> Unit) {
    val intent = Intent(this, T::class.java)
    intent.confBlock()
    startActivity(intent)
}
```

Now all the calls to this function can be as clean as:

```kotlin
launchActivity<SomeActivity> {
    putExtra(MY_KEY, myValue)
}

launchActivity<MyNextActivity> {}
```

Some final touches to the signature will make the function even safer and even cooler when being called:

1. Make `T` have a constraint to `Activity` in a way only classes that extend from `Activity` are allowed
2. Add a default value to the _lambda with receiver_ to allow calls with an empty configuration

```kotlin
inline fun <reified T: Activity> Context.launchActivity(confBlock: Intent.() -> Unit = {}) {
    ...
}
```

Now our function is cooler than ever, won't add too much to the final _bytecode_ and also will make our code more readable. Wow! We traveled a long way doing a lot of changes to a single piece of code to make things cooler, but not just that, now other people developing along with us can benefit from this improvement as they can simply call our function without having to worry how it works. I have seen even people adding animations, making a way to hold results and transforming data using these kind of functions/approaches. Writing idiomatic Kotlin actually makes you think and develop different and cooler!

## Conclusion

We can apply this kind of approach by steps or go directly to other functions on our codebase, one example is the usage of `SharedPreferences` and making the _edition_ do an _auto-apply_ "magically" using the lambda with receiver. We can create ways to configure more stuff in less steps and this will make our writing of code improve as we will be adding tools to our _already-vast_ set of functions and tools on Kotlin. Thinking about what is repeated or can be improved allows us –the developers– to discover new worlds and make things cooler and sharper. Don't be scared of change.

Special thanks to my friends @annaelizleal, [@jhoon](https://github.com/jhoon) and [Alejandro Tellez](https://github.com/gambit135), they help me a lot with grammar, typos and fixing styles. And special thanks to you for reading.
